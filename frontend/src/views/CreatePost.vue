<template>
  <div class="create-post container">
    <div class="create-layout">
      <div class="main-content">
        <div class="card">
          <h2 class="page-title">发布帖子</h2>

          <el-form :model="form" label-width="100px" style="max-width: 800px">
            <el-form-item label="帖子类型">
              <el-radio-group v-model="form.type">
                <el-radio :label="1">📝 体验分享</el-radio>
                <el-radio :label="2">❓ 问题求助</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="所属分类">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 300px">
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="帖子标题">
              <el-input
                v-model="form.title"
                placeholder="请输入标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <div v-if="form.type === 2" class="fault-template-wrap">
              <FaultTemplate
                ref="faultTemplateRef"
                v-model="faultData"
                :required-fields="currentRequiredFields"
                :category-id="form.categoryId"
                @apply="applyFaultContent"
              />
            </div>

            <el-form-item label="帖子内容">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="10"
                placeholder="请输入详细内容，分享你的使用体验或遇到的问题..."
                maxlength="5000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="上传图片" v-if="form.type === 1">
              <ImageUpload ref="imageUploadRef" v-model="images" :limit="9" @change="handleImageChange" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" @click="submit" :loading="submitting">
                发布帖子
              </el-button>
              <el-button size="large" @click="$router.back()">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <aside class="sidebar">
        <div class="card" v-if="form.type === 2 && currentRequiredFields.length > 0">
          <h3 class="sidebar-title">⚠️ 必填信息提示</h3>
          <div class="required-hint">
            <p class="hint-intro">当前分类（{{ currentCategoryName }}）的求助帖需填写以下信息：</p>
            <ul class="required-list">
              <li v-for="field in currentRequiredFields" :key="field">
                <span class="field-dot"></span>
                {{ fieldLabelMap[field] || field }}
              </li>
            </ul>
            <p class="hint-note">信息越完整，他人越容易帮你定位和解决问题</p>
          </div>
        </div>

        <div class="card">
          <h3 class="sidebar-title">📋 发布须知</h3>
          <ul class="tips-list">
            <li>请选择正确的分类，方便其他用户查找</li>
            <li>标题请简洁明了，概括内容要点</li>
            <li>内容请详细描述，分享真实使用感受</li>
            <li>图片清晰可见，有助于更好地交流</li>
            <li>禁止发布广告、交易、比价相关内容</li>
            <li>尊重他人，遵守社区公约</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCategories, createPost } from '@/api'
import { useUserStore } from '@/store/user'
import { usePostStore } from '@/store/post'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import FaultTemplate from '@/components/FaultTemplate.vue'

const fieldLabelMap = {
  deviceModel: '设备型号',
  accessoryModel: '配件型号',
  connectionType: '连接方式',
  platform: '使用平台',
  environment: '读写环境',
  symptoms: '出现症状',
  triedActions: '已尝试动作'
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const postStore = usePostStore()
const categories = ref([])
const images = ref([])
const imageInfo = ref({ successCount: 0, failedCount: 0, all: [] })
const submitting = ref(false)
const imageUploadRef = ref(null)
const faultTemplateRef = ref(null)
const faultData = ref({})

const form = reactive({
  type: 1,
  categoryId: null,
  title: '',
  content: ''
})

const currentRequiredFields = computed(() => {
  if (form.type !== 2 || !form.categoryId) return []
  const cat = categories.value.find(c => c.id === form.categoryId)
  if (!cat || !cat.requiredFields) return []
  try {
    return typeof cat.requiredFields === 'string'
      ? JSON.parse(cat.requiredFields)
      : cat.requiredFields
  } catch {
    return []
  }
})

const currentCategoryName = computed(() => {
  if (!form.categoryId) return ''
  const cat = categories.value.find(c => c.id === form.categoryId)
  return cat ? cat.name : ''
})

const handleImageChange = (info) => {
  imageInfo.value = info
}

const applyFaultContent = (content) => {
  if (form.content && !form.content.trim().endsWith('\n')) {
    form.content = form.content + '\n\n'
  }
  form.content = (form.content || '') + content
}

onMounted(() => {
  if (!userStore.checkLogin()) {
    ElMessageBox.alert('请先登录后再发布帖子', '提示', {
      confirmButtonText: '确定',
      callback: () => router.push('/')
    })
    return
  }
  loadCategories()
})

const loadCategories = async () => {
  categories.value = await getCategories()
}

const getRealtimeImageCounts = () => {
  const all = imageInfo.value.all || []
  return {
    failedCount: all.filter(i => i.status === 'failed').length,
    uploadingCount: all.filter(i => i.status === 'uploading').length,
    successCount: all.filter(i => i.status === 'success').length
  }
}

const submit = async () => {
  if (!form.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  if (form.type === 2 && faultTemplateRef.value) {
    const missing = faultTemplateRef.value.validate()
    if (missing.length > 0) {
      ElMessage.warning(`请补充必填信息：${missing.join('、')}`)
      return
    }
  }

  const { failedCount, uploadingCount } = getRealtimeImageCounts()

  if (uploadingCount > 0) {
    ElMessage.warning('图片正在上传中，请稍候...')
    return
  }

  if (failedCount > 0) {
    ElMessageBox.confirm(
      `有 ${failedCount} 张图片上传失败，是否继续发布？失败的图片将不会被包含。`,
      '提示',
      {
        confirmButtonText: '继续发布',
        cancelButtonText: '返回修改',
        type: 'warning'
      }
    ).then(async () => {
      imageUploadRef.value?.clearFailed()
      await doSubmit()
    }).catch(() => {})
    return
  }

  await doSubmit()
}

const doSubmit = async () => {
  if (!userStore.checkLogin()) {
    ElMessageBox.alert('登录状态已失效，请重新登录后再发布', '提示', {
      confirmButtonText: '确定',
      callback: () => router.push('/')
    })
    return
  }

  submitting.value = true
  try {
    const payload = {
      ...form,
      images: images.value
    }
    if (form.type === 2 && faultData.value) {
      payload.faultInfo = { ...faultData.value }
    }
    await createPost(payload)
    ElMessage.success('发布成功')

    postStore.markListDirty(form.type, form.categoryId)

    if (form.categoryId) {
      router.push({
        path: `/category/${form.categoryId}`,
        query: { type: form.type, refresh: Date.now() }
      })
    } else {
      router.push({
        path: '/',
        query: { refresh: Date.now() }
      })
    }
  } catch (e) {
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.create-post {
  .create-layout {
    display: flex;
    gap: 24px;
  }

  .main-content {
    flex: 1;
    min-width: 0;
  }

  .sidebar {
    width: 300px;
    flex-shrink: 0;
  }

  .page-title {
    font-size: 22px;
    font-weight: 600;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  .fault-template-wrap {
    margin-bottom: 18px;
    margin-left: 0;
  }

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  .required-hint {
    .hint-intro {
      font-size: 13px;
      color: #666;
      margin-bottom: 12px;
      line-height: 1.5;
    }

    .required-list {
      list-style: none;
      padding: 0;
      margin: 0 0 12px 0;

      li {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 0;
        font-size: 13px;
        color: #333;
        font-weight: 500;

        .field-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: #f56c6c;
          flex-shrink: 0;
        }
      }
    }

    .hint-note {
      font-size: 12px;
      color: #999;
      line-height: 1.5;
    }
  }

  .tips-list {
    list-style: none;
    font-size: 13px;
    color: #666;

    li {
      padding: 8px 0;
      padding-left: 20px;
      position: relative;
      line-height: 1.5;

      &::before {
        content: '✓';
        position: absolute;
        left: 0;
        color: #52c41a;
        font-weight: 600;
      }
    }
  }
}
</style>
