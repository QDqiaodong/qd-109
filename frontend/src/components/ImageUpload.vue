<template>
  <div class="image-upload" @paste="handlePaste">
    <div
      class="upload-area"
      :class="{ 'dragover': isDragover }"
      @dragover.prevent="handleDragover"
      @dragleave.prevent="handleDragleave"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        multiple
        style="display: none"
        @change="handleFileSelect"
      />
      <div class="upload-content">
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <p class="upload-text">点击或拖拽图片到此处上传</p>
        <p class="upload-hint">支持 jpg、png、gif 格式，单张超过 {{ formatSize(maxSize) }} 自动压缩，最多 {{ limit }} 张</p>
        <p class="upload-hint secondary">也可以直接粘贴图片 (Ctrl/Cmd + V)</p>
      </div>
    </div>

    <div v-if="imageList.length > 0" class="image-grid">
      <div
        v-for="(item, index) in imageList"
        :key="item.uid"
        class="image-item"
        :class="{
          'dragging': dragIndex === index,
          'drag-over-top': dragOverIndex === index && dragIndex > index,
          'drag-over-bottom': dragOverIndex === index && dragIndex < index
        }"
        draggable="true"
        @dragstart="handleDragStart(index, $event)"
        @dragend="handleDragEnd"
        @dragover.prevent="handleDragOver(index)"
        @dragleave="handleDragLeave"
        @drop.stop.prevent="handleDropReorder(index)"
      >
        <img :src="item.previewUrl" :alt="item.name" class="image-preview" @click="handlePreview(index)" />

        <div class="image-mask">
          <div class="mask-actions">
            <el-icon class="mask-btn" @click.stop="handlePreview(index)"><ZoomIn /></el-icon>
            <el-icon class="mask-btn delete-btn" @click.stop="handleRemove(index)"><Delete /></el-icon>
          </div>
        </div>

        <div v-if="item.status === 'uploading'" class="upload-progress">
          <el-progress type="circle" :percentage="item.progress" :width="48" :stroke-width="4" :stroke-color="'#409eff'" />
        </div>

        <div v-if="item.status === 'failed'" class="failed-overlay">
          <el-icon class="failed-icon"><WarningFilled /></el-icon>
          <span class="failed-text">上传失败</span>
          <el-button type="primary" size="small" @click.stop="handleRetry(index)">
            <el-icon><Refresh /></el-icon>
            <span>重试</span>
          </el-button>
        </div>

        <div class="image-badge">
          <span class="badge-index">{{ index + 1 }}</span>
          <span v-if="item.compressed" class="badge-compressed" :title="`原图 ${formatSize(item.originalSize)}，压缩后 ${formatSize(item.size)}`">
            已压缩
          </span>
        </div>
      </div>

      <div
        v-if="imageList.length < limit"
        class="image-item add-item"
        @click="triggerFileInput"
      >
        <el-icon class="add-icon"><Plus /></el-icon>
        <span class="add-text">添加图片</span>
      </div>
    </div>

    <div class="upload-stats" v-if="imageList.length > 0">
      <div class="stats-left">
        <div class="stats-main">
          <span class="stats-label">图片数量</span>
          <span class="stats-value">
            <em>{{ imageList.length }}</em> / {{ limit }} 张
          </span>
        </div>
        <div class="stats-detail">
          <span class="stat-item success">
            <el-icon><CircleCheckFilled /></el-icon>
            成功 {{ successCount }}
          </span>
          <span v-if="uploadingCount > 0" class="stat-item uploading">
            <el-icon><Loading /></el-icon>
            上传中 {{ uploadingCount }}
          </span>
          <span v-if="failedCount > 0" class="stat-item failed">
            <el-icon><WarningFilled /></el-icon>
            失败 {{ failedCount }}
          </span>
        </div>
      </div>
      <div class="stats-right">
        <span class="stats-size">总计 {{ formatSize(totalSize) }}</span>
        <el-button v-if="failedCount > 0" type="danger" size="small" link @click="retryAllFailed">
          重试全部失败
        </el-button>
      </div>
    </div>

    <el-dialog
      v-model="previewVisible"
      width="auto"
      align-center
      :show-close="true"
      :close-on-click-modal="true"
      class="preview-dialog"
      @keydown.left.prevent="handlePrevImage"
      @keydown.right.prevent="handleNextImage"
      @keydown.esc.prevent="previewVisible = false"
    >
      <template #header>
        <div class="preview-header">
          <span>{{ currentPreviewIndex + 1 }} / {{ imageList.length }}</span>
          <span class="preview-title">{{ previewImage?.name }}</span>
        </div>
      </template>
      <div class="preview-body">
        <el-icon class="nav-btn prev-btn" @click="handlePrevImage" v-if="imageList.length > 1">
          <ArrowLeft />
        </el-icon>
        <img
          :src="previewImage?.previewUrl"
          :alt="previewImage?.name"
          class="preview-dialog-img"
          @wheel.stop.prevent="handlePreviewWheel"
          :style="previewTransform"
        />
        <el-icon class="nav-btn next-btn" @click="handleNextImage" v-if="imageList.length > 1">
          <ArrowRight />
        </el-icon>
      </div>
      <template #footer>
        <div class="preview-footer">
          <span v-if="previewImage?.compressed" class="preview-compress-info">
            原图 {{ formatSize(previewImage?.originalSize) }} → 压缩后 {{ formatSize(previewImage?.size) }}
          </span>
          <span v-else class="preview-size-info">
            大小：{{ formatSize(previewImage?.size) }}
          </span>
          <el-button size="small" @click="handleRemove(currentPreviewIndex)" type="danger" plain>
            删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  UploadFilled,
  ZoomIn,
  Delete,
  Plus,
  WarningFilled,
  Refresh,
  CircleCheckFilled,
  Loading,
  ArrowLeft,
  ArrowRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  limit: {
    type: Number,
    default: 9
  },
  maxSize: {
    type: Number,
    default: 2 * 1024 * 1024
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const fileInputRef = ref(null)
const isDragover = ref(false)
const imageList = ref([])
const previewVisible = ref(false)
const previewImage = ref(null)
const currentPreviewIndex = ref(0)
const dragIndex = ref(-1)
const dragOverIndex = ref(-1)
const previewScale = ref(1)

const successCount = computed(() => imageList.value.filter(item => item.status === 'success').length)
const failedCount = computed(() => imageList.value.filter(item => item.status === 'failed').length)
const uploadingCount = computed(() => imageList.value.filter(item => item.status === 'uploading').length)
const totalSize = computed(() => imageList.value.reduce((sum, item) => sum + item.size, 0))

const previewTransform = computed(() => `scale(${previewScale.value})`)

let uidCounter = 0

const generateUid = () => {
  uidCounter++
  return `img_${Date.now()}_${uidCounter}`
}

const formatSize = (bytes) => {
  if (!bytes || bytes === 0) return '0B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + sizes[i]
}

const compressImage = (file) => {
  return new Promise((resolve, reject) => {
    if (file.size <= props.maxSize) {
      resolve({ file, compressed: false, originalSize: file.size })
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const ratio = img.width / img.height
        const maxPixels = 2073600

        let targetWidth = img.width
        let targetHeight = img.height

        if (img.width * img.height > maxPixels) {
          targetHeight = Math.sqrt(maxPixels / ratio)
          targetWidth = Math.round(targetHeight * ratio)
          targetHeight = Math.round(targetHeight)
        }

        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        canvas.width = targetWidth
        canvas.height = targetHeight
        ctx.drawImage(img, 0, 0, targetWidth, targetHeight)

        const findOptimalQuality = () => {
          let low = 0.1
          let high = 1.0
          let bestQuality = 0.5
          let bestFile = null

          for (let i = 0; i < 8; i++) {
            const mid = (low + high) / 2
            const dataUrl = canvas.toDataURL(file.type, mid)
            const size = Math.round(dataUrl.length * 3 / 4)

            if (size <= props.maxSize) {
              bestQuality = mid
              bestFile = dataUrl
              low = mid
            } else {
              high = mid
            }
          }

          if (!bestFile) {
            bestFile = canvas.toDataURL(file.type, 0.1)
          }

          return bestFile
        }

        const compressedDataUrl = findOptimalQuality()
        const binaryString = atob(compressedDataUrl.split(',')[1])
        const uint8Array = new Uint8Array(binaryString.length)
        for (let i = 0; i < binaryString.length; i++) {
          uint8Array[i] = binaryString.charCodeAt(i)
        }

        const compressedFile = new File([uint8Array], file.name, {
          type: file.type,
          lastModified: Date.now()
        })

        resolve({
          file: compressedFile,
          compressed: true,
          originalSize: file.size
        })
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target.result
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

const createPreviewUrl = (file) => {
  return URL.createObjectURL(file)
}

const simulateUpload = (item) => {
  return new Promise((resolve, reject) => {
    let progress = 0
    const interval = setInterval(() => {
      progress += Math.random() * 20
      if (progress >= 100) {
        progress = 100
        clearInterval(interval)
      }
      item.progress = Math.min(Math.round(progress), 99)
    }, 150)

    setTimeout(() => {
      clearInterval(interval)
      const isSuccess = Math.random() > 0.1
      if (isSuccess) {
        item.progress = 100
        item.status = 'success'
        item.url = item.previewUrl
        resolve(item)
      } else {
        item.progress = 0
        item.status = 'failed'
        reject(new Error('上传失败'))
      }
    }, 1200 + Math.random() * 800)
  })
}

const processFile = async (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning(`文件 ${file.name} 不是图片格式`)
    return null
  }

  const uid = generateUid()
  const item = {
    uid,
    name: file.name,
    size: file.size,
    originalSize: file.size,
    rawFile: file,
    previewUrl: '',
    status: 'uploading',
    progress: 0,
    url: '',
    compressed: false
  }

  imageList.value.push(item)

  try {
    const result = await compressImage(file)
    const compressedFile = result.file
    item.previewUrl = createPreviewUrl(compressedFile)
    item.size = compressedFile.size
    item.rawFile = compressedFile
    item.compressed = result.compressed
    item.originalSize = result.originalSize

    await simulateUpload(item)
  } catch (error) {
    item.status = 'failed'
    item.error = error.message
  }

  emitChange()
  return item
}

const handleFileSelect = (e) => {
  const files = Array.from(e.target.files)
  addFiles(files)
  e.target.value = ''
}

const addFiles = async (files) => {
  const remainingSlots = props.limit - imageList.value.length
  if (remainingSlots <= 0) {
    ElMessage.warning(`最多只能上传 ${props.limit} 张图片`)
    return
  }

  const imageFiles = files.filter(f => f.type.startsWith('image/'))
  if (imageFiles.length === 0) {
    ElMessage.warning('请选择图片文件')
    return
  }

  const filesToAdd = imageFiles.slice(0, remainingSlots)

  if (imageFiles.length > remainingSlots) {
    ElMessage.warning(`最多只能上传 ${props.limit} 张图片，已自动截取前 ${remainingSlots} 张`)
  }

  for (const file of filesToAdd) {
    processFile(file)
  }
}

const handleDragover = (e) => {
  isDragover.value = true
}

const handleDragleave = (e) => {
  isDragover.value = false
}

const handleDrop = (e) => {
  isDragover.value = false
  const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'))
  if (files.length === 0) {
    ElMessage.warning('请拖拽图片文件')
    return
  }
  addFiles(files)
}

const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return

  const files = []
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        files.push(file)
      }
    }
  }

  if (files.length > 0) {
    addFiles(files)
  }
}

const triggerFileInput = () => {
  if (imageList.value.length >= props.limit) {
    ElMessage.warning(`最多只能上传 ${props.limit} 张图片`)
    return
  }
  fileInputRef.value?.click()
}

const handlePreview = (index) => {
  currentPreviewIndex.value = index
  previewImage.value = imageList.value[index]
  previewScale.value = 1
  previewVisible.value = true
}

const handlePrevImage = () => {
  if (imageList.value.length === 0) return
  currentPreviewIndex.value = (currentPreviewIndex.value - 1 + imageList.value.length) % imageList.value.length
  previewImage.value = imageList.value[currentPreviewIndex.value]
  previewScale.value = 1
}

const handleNextImage = () => {
  if (imageList.value.length === 0) return
  currentPreviewIndex.value = (currentPreviewIndex.value + 1) % imageList.value.length
  previewImage.value = imageList.value[currentPreviewIndex.value]
  previewScale.value = 1
}

const handlePreviewWheel = (e) => {
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  previewScale.value = Math.max(0.5, Math.min(3, previewScale.value + delta))
}

const handleRemove = (index) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = imageList.value[index]
    if (item.previewUrl) {
      URL.revokeObjectURL(item.previewUrl)
    }
    imageList.value.splice(index, 1)

    if (previewVisible.value && index === currentPreviewIndex.value) {
      if (imageList.value.length === 0) {
        previewVisible.value = false
        previewImage.value = null
      } else {
        currentPreviewIndex.value = Math.min(index, imageList.value.length - 1)
        previewImage.value = imageList.value[currentPreviewIndex.value]
      }
    } else if (previewVisible.value && index < currentPreviewIndex.value) {
      currentPreviewIndex.value--
      previewImage.value = imageList.value[currentPreviewIndex.value]
    }

    emitChange()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleRetry = async (index) => {
  const item = imageList.value[index]
  if (!item || item.status !== 'failed') return

  item.status = 'uploading'
  item.progress = 0
  item.error = ''

  try {
    await simulateUpload(item)
  } catch (error) {
    item.status = 'failed'
    item.error = error.message
  }

  emitChange()
}

const retryAllFailed = async () => {
  const failedItems = imageList.value.filter(item => item.status === 'failed')
  if (failedItems.length === 0) return

  for (const item of failedItems) {
    item.status = 'uploading'
    item.progress = 0
    item.error = ''
    simulateUpload(item).then(() => {
      emitChange()
    }).catch(() => {
      emitChange()
    })
  }
}

const handleDragStart = (index, e) => {
  dragIndex.value = index
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('text/plain', index.toString())
}

const handleDragEnd = () => {
  dragIndex.value = -1
  dragOverIndex.value = -1
}

const handleDragOver = (index) => {
  dragOverIndex.value = index
}

const handleDragLeave = () => {
}

const handleDropReorder = (targetIndex) => {
  if (dragIndex.value === -1 || dragIndex.value === targetIndex) {
    return
  }

  const item = imageList.value.splice(dragIndex.value, 1)[0]
  imageList.value.splice(targetIndex, 0, item)

  dragIndex.value = -1
  dragOverIndex.value = -1
  emitChange()
}

const emitChange = () => {
  const successImages = imageList.value
    .filter(item => item.status === 'success')
    .map(item => item.url)

  emit('update:modelValue', successImages)
  emit('change', {
    images: successImages,
    all: imageList.value,
    successCount: successCount.value,
    failedCount: failedCount.value,
    uploadingCount: uploadingCount.value,
    total: imageList.value.length
  })
}

watch(
  () => props.modelValue,
  (newVal) => {
    if (Array.isArray(newVal) && newVal.length > 0 && imageList.value.length === 0) {
      imageList.value = newVal.map((url, index) => ({
        uid: generateUid(),
        name: `图片${index + 1}`,
        size: 0,
        originalSize: 0,
        previewUrl: url,
        status: 'success',
        progress: 100,
        url: url,
        compressed: false
      }))
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  imageList.value.forEach(item => {
    if (item.previewUrl && item.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(item.previewUrl)
    }
  })
})
</script>

<style lang="scss" scoped>
.image-upload {
  width: 100%;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 48px 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);

  &:hover,
  &.dragover {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  }

  &.dragover {
    border-style: solid;
    border-width: 2px;
    transform: scale(1.01);
  }
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 56px;
  color: #c0c4cc;
  margin-bottom: 8px;
  transition: color 0.3s;

  .upload-area:hover & {
    color: #409eff;
  }
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin: 0;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin: 0;

  &.secondary {
    color: #c0c4cc;
    font-size: 11px;
  }
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-top: 20px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(4, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 480px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.image-item {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  cursor: move;
  border: 2px solid transparent;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f5f7fa;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);

    .image-mask {
      opacity: 1;
    }
  }

  &.dragging {
    opacity: 0.4;
    transform: scale(0.92);
  }

  &.drag-over-top {
    border-top-color: #409eff;
    border-top-width: 3px;

    &::before {
      content: '';
      position: absolute;
      top: -2px;
      left: 0;
      right: 0;
      height: 3px;
      background: #409eff;
      z-index: 10;
      border-radius: 2px 2px 0 0;
    }
  }

  &.drag-over-bottom {
    border-bottom-color: #409eff;
    border-bottom-width: 3px;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      right: 0;
      height: 3px;
      background: #409eff;
      z-index: 10;
      border-radius: 0 0 2px 2px;
    }
  }

  &.add-item {
    border: 2px dashed #dcdfe6;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background: #fafafa;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background: #ecf5ff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);

      .add-icon,
      .add-text {
        color: #409eff;
      }
    }
  }
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.6) 100%);
  opacity: 0;
  transition: opacity 0.25s ease;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 16px;
}

.mask-actions {
  display: flex;
  gap: 20px;
}

.mask-btn {
  font-size: 22px;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 50%;
  backdrop-filter: blur(4px);

  &:hover {
    transform: scale(1.15);
    background: rgba(255, 255, 255, 0.3);
  }

  &.delete-btn:hover {
    color: #f56c6c;
    background: rgba(245, 108, 108, 0.25);
  }
}

.upload-progress {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 50%;
  backdrop-filter: blur(8px);
}

.failed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
}

.failed-icon {
  font-size: 32px;
  color: #f56c6c;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

.failed-text {
  font-size: 13px;
  font-weight: 500;
}

.image-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: calc(100% - 16px);
}

.badge-index {
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  line-height: 1.5;
  backdrop-filter: blur(4px);
  align-self: flex-start;
}

.badge-compressed {
  background: rgba(103, 194, 58, 0.9);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  line-height: 1.4;
  align-self: flex-start;
}

.add-icon {
  font-size: 36px;
  color: #c0c4cc;
  margin-bottom: 8px;
  transition: color 0.3s;
}

.add-text {
  font-size: 13px;
  color: #909399;
  transition: color 0.3s;
  font-weight: 500;
}

.upload-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  border-radius: 10px;
  font-size: 13px;
}

.stats-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stats-main {
  display: flex;
  align-items: baseline;
  gap: 8px;

  .stats-label {
    color: #909399;
    font-size: 12px;
  }

  .stats-value {
    color: #606266;
    font-size: 13px;

    em {
      font-style: normal;
      color: #409eff;
      font-weight: 700;
      font-size: 20px;
      margin: 0 2px;
    }
  }
}

.stats-detail {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;

  &.success {
    color: #67c23a;
  }

  &.uploading {
    color: #409eff;

    .el-icon {
      animation: spin 1s linear infinite;
    }
  }

  &.failed {
    color: #f56c6c;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.stats-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.stats-size {
  color: #909399;
  font-size: 12px;
  font-weight: 500;
}

.preview-dialog {
  :deep(.el-dialog__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 20px;
    border-top: 1px solid #f0f0f0;
  }
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 16px;

  span:first-child {
    color: #909399;
    font-size: 13px;
    background: #f5f7fa;
    padding: 4px 10px;
    border-radius: 4px;
  }

  .preview-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.preview-body {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1a1a1a;
  min-height: 200px;
  max-height: 70vh;
  overflow: hidden;
}

.preview-dialog-img {
  max-width: 90vw;
  max-height: 65vh;
  display: block;
  object-fit: contain;
  transition: transform 0.2s ease;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 36px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 16px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  transition: all 0.25s;
  z-index: 10;

  &:hover {
    color: #fff;
    background: rgba(0, 0, 0, 0.5);
    transform: translateY(-50%) scale(1.1);
  }

  &.prev-btn {
    left: 20px;
  }

  &.next-btn {
    right: 20px;
  }
}

.preview-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.preview-compress-info,
.preview-size-info {
  font-size: 12px;
  color: #909399;
}

.preview-compress-info {
  color: #67c23a;
}
</style>
