<template>
  <div class="image-group-editor">
    <div class="editor-header">
      <div class="header-info">
        <h3 class="editor-title">🎨 图片编排</h3>
        <p class="editor-desc">按节奏将图片拖拽到下方分组，让浏览者清晰理解使用体验</p>
      </div>
      <div class="header-stats">
        <span class="stat-chip">
          <el-icon><PictureFilled /></el-icon>
          已编排 {{ organizedCount }} / {{ allImages.length }}
        </span>
      </div>
    </div>

    <div class="unassigned-zone" v-if="unassignedImages.length > 0">
      <div class="zone-header">
        <span class="zone-label">📥 待编排图片</span>
        <span class="zone-hint">拖拽到下方分组</span>
      </div>
      <div class="image-strip">
        <div
          v-for="(img, idx) in unassignedImages"
          :key="img.uid"
          class="mini-image-item unassigned"
          :class="{ 'dragging': dragState.type === 'unassigned' && dragState.index === idx }"
          draggable="true"
          @dragstart="handleDragStart($event, 'unassigned', idx)"
          @dragend="handleDragEnd"
        >
          <img :src="img.previewUrl || img.url" :alt="img.name" />
          <div class="mini-index">{{ unassignedOriginalIndex(idx) + 1 }}</div>
          <div class="mini-tooltip">拖拽到分组</div>
        </div>
      </div>
    </div>
    <div class="unassigned-zone empty" v-else-if="allImages.length > 0">
      <el-icon><CircleCheckFilled /></el-icon>
      <span>全部图片已编排完成 ✨</span>
    </div>

    <div class="groups-container">
      <div
        v-for="(group, gIdx) in groups"
        :key="group.key"
        class="group-card"
        :class="{
          'drop-active': dragState.overGroupKey === group.key,
          'has-images': group.images.length > 0
        }"
        @dragover.prevent="handleGroupDragOver(group.key)"
        @dragleave="handleGroupDragLeave"
        @drop.stop.prevent="handleGroupDrop(group.key)"
      >
        <div class="group-card-header" :style="{ borderColor: group.color }">
          <div class="group-title-wrap">
            <span class="group-icon">{{ group.icon }}</span>
            <div class="group-title-inner">
              <span class="group-name" :style="{ color: group.color }">{{ group.label }}</span>
              <span class="group-sort">第 {{ gIdx + 1 }} 步</span>
            </div>
          </div>
          <div class="group-count-badge">
            {{ group.images.length }} 张
          </div>
        </div>

        <div class="group-card-body">
          <div class="group-hint" v-if="group.images.length === 0">
            <el-icon><Upload /></el-icon>
            <span>拖拽图片到此处</span>
          </div>

          <div class="group-images" v-else>
            <div
              v-for="(img, idx) in group.images"
              :key="img.uid"
              class="mini-image-item in-group"
              :class="{
                'dragging': dragState.type === 'group' && dragState.groupKey === group.key && dragState.index === idx,
                'drag-over-before': dragState.overGroupKey === group.key && dragState.overIndex === idx && dragState.direction === 'before',
                'drag-over-after': dragState.overGroupKey === group.key && dragState.overIndex === idx && dragState.direction === 'after'
              }"
              draggable="true"
              @dragstart="handleDragStart($event, 'group', idx, group.key)"
              @dragend="handleDragEnd"
              @dragover.prevent="handleImageDragOver(group.key, idx, $event)"
              @dragleave="handleImageDragLeave"
              @drop.stop.prevent="handleImageDrop(group.key, idx)"
            >
              <img :src="img.previewUrl || img.url" :alt="img.name" />
              <div class="group-image-index">{{ idx + 1 }}</div>
              <el-icon
                class="remove-btn"
                @click.stop="removeFromGroup(group.key, idx)"
                title="移出分组"
              ><Close /></el-icon>
            </div>
          </div>
        </div>

        <div class="group-card-footer">
          <span class="footer-tip">{{ group.tip }}</span>
        </div>
      </div>
    </div>

    <div class="preview-section" v-if="hasAnyGrouped">
      <div class="preview-header">
        <h4 class="preview-title">👁️ 预览完整图组节奏</h4>
        <span class="preview-sub">浏览者将按此顺序查看</span>
      </div>
      <div class="preview-flow">
        <div
          v-for="(group, gIdx) in groups.filter(g => g.images.length > 0)"
          :key="group.key"
          class="preview-group"
        >
          <div class="preview-step-dot" :style="{ background: group.color }">
            {{ gIdx + 1 }}
          </div>
          <div class="preview-group-content">
            <div class="preview-group-title" :style="{ color: group.color }">
              <span class="preview-icon">{{ group.icon }}</span>
              {{ group.label }}
            </div>
            <div class="preview-image-row">
              <div
                v-for="(img, iIdx) in group.images"
                :key="'p' + img.uid"
                class="preview-image-cell"
              >
                <img :src="img.previewUrl || img.url" :alt="img.name" />
                <span class="preview-img-num">{{ iIdx + 1 }}</span>
              </div>
              <el-empty
                v-if="group.images.length === 0"
                :image-size="40"
                description="暂无图片"
              />
            </div>
          </div>
          <div
            v-if="gIdx < groups.filter(g => g.images.length > 0).length - 1"
            class="preview-arrow"
          >
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive } from 'vue'
import {
  PictureFilled,
  CircleCheckFilled,
  Upload,
  Close,
  ArrowDown
} from '@element-plus/icons-vue'

const GROUP_DEFS = [
  { key: 'appearance', label: '外观', icon: '🎨', color: '#722ed1', sort: 0, tip: '展示产品整体外观、设计语言、做工细节' },
  { key: 'interface', label: '接口', icon: '🔌', color: '#13c2c2', sort: 1, tip: '展示接口类型、线材、按键、连接方式' },
  { key: 'usage', label: '上机效果', icon: '💻', color: '#1890ff', sort: 2, tip: '展示装在设备上的实际效果、使用场景' },
  { key: 'desk', label: '桌搭全景', icon: '🖼️', color: '#fa8c16', sort: 3, tip: '展示整体桌面环境、氛围灯光、搭配组合' }
]

const props = defineProps({
  imageList: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const groups = ref(
  GROUP_DEFS.map(def => ({
    ...def,
    images: []
  }))
)

const dragState = reactive({
  active: false,
  type: null,
  index: -1,
  groupKey: null,
  overGroupKey: null,
  overIndex: -1,
  direction: null
})

const allImages = computed(() => props.imageList.filter(i => i.status === 'success' || i.url))

const assignedUids = computed(() => {
  const set = new Set()
  groups.value.forEach(g => g.images.forEach(img => set.add(img.uid)))
  return set
})

const unassignedImages = computed(() =>
  allImages.value.filter(img => !assignedUids.value.has(img.uid))
)

const organizedCount = computed(() => assignedUids.value.size)

const hasAnyGrouped = computed(() =>
  groups.value.some(g => g.images.length > 0)
)

const unassignedOriginalIndex = (idx) => {
  const img = unassignedImages.value[idx]
  return allImages.value.findIndex(i => i.uid === img.uid)
}

watch(
  () => props.modelValue,
  (val) => {
    if (val && val.length > 0 && groups.value.every(g => g.images.length === 0)) {
      val.forEach(gData => {
        const group = groups.value.find(g => g.key === gData.key)
        if (group && gData.images && gData.images.length > 0) {
          const imgObjects = gData.images.map(url => {
            const existing = allImages.value.find(i => i.url === url || i.previewUrl === url)
            return existing || { uid: `restored_${Date.now()}_${Math.random()}`, url, previewUrl: url, name: '恢复图片' }
          })
          group.images = imgObjects
        }
      })
    }
  },
  { immediate: true }
)

watch(
  groups,
  () => {
    const output = groups.value
      .filter(g => g.images.length > 0)
      .map(g => ({
        key: g.key,
        label: g.label,
        sort: g.sort,
        images: g.images.map(img => img.url || img.previewUrl)
      }))
    emit('update:modelValue', output)
    emit('change', {
      groups: output,
      organizedCount: organizedCount.value,
      total: allImages.value.length
    })
  },
  { deep: true }
)

const handleDragStart = (e, type, index, groupKey = null) => {
  dragState.active = true
  dragState.type = type
  dragState.index = index
  dragState.groupKey = groupKey
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('text/plain', JSON.stringify({ type, index, groupKey }))
}

const handleDragEnd = () => {
  dragState.active = false
  dragState.type = null
  dragState.index = -1
  dragState.groupKey = null
  dragState.overGroupKey = null
  dragState.overIndex = -1
  dragState.direction = null
}

const handleGroupDragOver = (groupKey) => {
  if (!dragState.active) return
  dragState.overGroupKey = groupKey
  dragState.overIndex = -1
}

const handleGroupDragLeave = () => {
  if (dragState.overGroupKey) {
    dragState.overGroupKey = null
  }
}

const handleGroupDrop = (groupKey) => {
  if (!dragState.active) return

  const targetGroup = groups.value.find(g => g.key === groupKey)
  if (!targetGroup) return

  let img = null

  if (dragState.type === 'unassigned') {
    img = unassignedImages.value[dragState.index]
  } else if (dragState.type === 'group') {
    const srcGroup = groups.value.find(g => g.key === dragState.groupKey)
    if (srcGroup) {
      img = srcGroup.images[dragState.index]
      srcGroup.images.splice(dragState.index, 1)
    }
  }

  if (img && targetGroup.key !== dragState.groupKey) {
    targetGroup.images.push(img)
  }

  handleDragEnd()
}

const handleImageDragOver = (groupKey, index, e) => {
  if (!dragState.active) return
  const rect = e.currentTarget.getBoundingClientRect()
  const midY = rect.top + rect.height / 2
  const direction = e.clientY < midY ? 'before' : 'after'
  dragState.overGroupKey = groupKey
  dragState.overIndex = index
  dragState.direction = direction
}

const handleImageDragLeave = () => {}

const handleImageDrop = (groupKey, index) => {
  if (!dragState.active) return

  const targetGroup = groups.value.find(g => g.key === groupKey)
  if (!targetGroup) return

  let img = null
  const direction = dragState.direction || 'after'
  const insertIndex = direction === 'before' ? index : index + 1

  if (dragState.type === 'unassigned') {
    img = unassignedImages.value[dragState.index]
    if (img) {
      targetGroup.images.splice(insertIndex, 0, img)
    }
  } else if (dragState.type === 'group') {
    const srcGroup = groups.value.find(g => g.key === dragState.groupKey)
    if (srcGroup) {
      img = srcGroup.images[dragState.index]
      if (srcGroup.key === targetGroup.key) {
        const realSrcIdx = dragState.index
        const realDestIdx = insertIndex > realSrcIdx ? insertIndex - 1 : insertIndex
        if (realSrcIdx !== realDestIdx) {
          srcGroup.images.splice(realSrcIdx, 1)
          srcGroup.images.splice(realDestIdx, 0, img)
        }
      } else {
        srcGroup.images.splice(dragState.index, 1)
        if (img) {
          targetGroup.images.splice(insertIndex, 0, img)
        }
      }
    }
  }

  handleDragEnd()
}

const removeFromGroup = (groupKey, index) => {
  const group = groups.value.find(g => g.key === groupKey)
  if (group) {
    group.images.splice(index, 1)
  }
}
</script>

<style lang="scss" scoped>
.image-group-editor {
  width: 100%;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .editor-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 4px 0;
  }

  .editor-desc {
    font-size: 13px;
    color: #909399;
    margin: 0;
  }

  .stat-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
    color: #409eff;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;

    .el-icon {
      font-size: 15px;
    }
  }
}

.unassigned-zone {
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;

  &.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 20px;
    border-style: solid;
    border-color: #67c23a;
    background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
    color: #67c23a;
    font-size: 14px;
    font-weight: 500;

    .el-icon {
      font-size: 20px;
    }
  }

  .zone-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .zone-label {
      font-size: 14px;
      font-weight: 600;
      color: #606266;
    }

    .zone-hint {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}

.image-strip {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.mini-image-item {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  cursor: grab;
  flex-shrink: 0;
  border: 2px solid transparent;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &.unassigned {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    &:hover {
      transform: translateY(-2px) scale(1.03);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
      border-color: #409eff;
    }

    .mini-tooltip {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(64, 158, 255, 0.9);
      color: #fff;
      font-size: 9px;
      text-align: center;
      padding: 2px 0;
      transform: translateY(100%);
      transition: transform 0.2s;
    }

    &:hover .mini-tooltip {
      transform: translateY(0);
    }
  }

  &.in-group {
    width: 56px;
    height: 56px;

    &:hover {
      .remove-btn {
        opacity: 1;
      }
    }
  }

  &.dragging {
    opacity: 0.35;
    transform: scale(0.9);
  }

  &.drag-over-before {
    border-left-color: #409eff;
    border-left-width: 3px;
    margin-left: -1px;
  }

  &.drag-over-after {
    border-right-color: #409eff;
    border-right-width: 3px;
    margin-right: -1px;
  }

  .mini-index,
  .group-image-index {
    position: absolute;
    top: 4px;
    left: 4px;
    width: 18px;
    height: 18px;
    background: rgba(0, 0, 0, 0.65);
    color: #fff;
    font-size: 10px;
    font-weight: 600;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
  }

  .remove-btn {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 18px;
    height: 18px;
    background: rgba(245, 108, 108, 0.95);
    color: #fff;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    opacity: 0;
    transition: opacity 0.2s;
    cursor: pointer;
    z-index: 5;

    &:hover {
      background: #f56c6c;
      transform: scale(1.1);
    }
  }
}

.groups-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.group-card {
  background: #fff;
  border: 2px solid #ebeef5;
  border-radius: 14px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.drop-active {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
  }

  &.has-images {
    border-color: #e4e7ed;
  }

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }
}

.group-card-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, #fafbfc 0%, #f4f6f9 100%);
  border-bottom: 3px solid;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .group-title-wrap {
    display: flex;
    align-items: center;
    gap: 12px;

    .group-icon {
      font-size: 22px;
    }

    .group-title-inner {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .group-name {
        font-size: 16px;
        font-weight: 700;
      }

      .group-sort {
        font-size: 11px;
        color: #909399;
      }
    }
  }

  .group-count-badge {
    background: #fff;
    padding: 5px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    color: #606266;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }
}

.group-card-body {
  padding: 16px;
  min-height: 110px;

  .group-hint {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 90px;
    color: #c0c4cc;
    font-size: 13px;

    .el-icon {
      font-size: 28px;
      opacity: 0.5;
    }
  }
}

.group-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  min-height: 90px;
}

.group-card-footer {
  padding: 10px 18px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;

  .footer-tip {
    font-size: 11px;
    color: #909399;
    line-height: 1.5;
  }
}

.preview-section {
  background: linear-gradient(135deg, #f6f8fb 0%, #eef2f7 100%);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e4e7ed;
}

.preview-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;

  .preview-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0;
  }

  .preview-sub {
    font-size: 12px;
    color: #909399;
  }
}

.preview-flow {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-group {
  display: flex;
  gap: 16px;
  position: relative;
}

.preview-step-dot {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 50%;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.preview-group-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.preview-group-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;

  .preview-icon {
    font-size: 18px;
  }
}

.preview-image-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preview-image-cell {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .preview-img-num {
    position: absolute;
    bottom: 4px;
    right: 4px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 10px;
    font-weight: 600;
    padding: 2px 7px;
    border-radius: 8px;
  }
}

.preview-arrow {
  position: absolute;
  left: 17px;
  bottom: -16px;
  color: #c0c4cc;
  font-size: 16px;
  z-index: 2;
}
</style>
