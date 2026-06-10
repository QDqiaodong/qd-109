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

            <el-form-item label="故障现象" v-if="form.type === 2">
              <FaultTemplate v-model="faultTemplate" @apply="applyTemplateContent" />
            </el-form-item>

            <el-form-item label="帖子内容">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="10"
                :placeholder="form.type === 2 ? '请输入详细的问题描述，也可以使用上方故障现象模板快速填写...' : '请输入详细内容，分享你的使用体验或遇到的问题...'"
                maxlength="5000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="上传图片">
              <ImageUpload v-model="images" :limit="20" @change="handleImageChange" />
            </el-form-item>

            <el-form-item label="图片编排" v-if="form.type === 1 && allImageItems.length > 0">
              <ImageGroupEditor
                v-model="imageGroups"
                :image-list="allImageItems"
                @change="handleGroupChange"
              />
              <div class="form-item-tip" v-if="imageGroupInfo.organizedCount > 0 && imageGroupInfo.organizedCount < imageGroupInfo.total">
                <el-icon><InfoFilled /></el-icon>
                还有 <strong>{{ imageGroupInfo.total - imageGroupInfo.organizedCount }}</strong> 张图片未编排，未编排的图片不会按分组顺序展示
              </div>
              <div class="form-item-tip success" v-if="imageGroupInfo.organizedCount > 0 && imageGroupInfo.organizedCount === imageGroupInfo.total">
                <el-icon><CircleCheckFilled /></el-icon>
                全部 {{ imageGroupInfo.total }} 张图片已完成编排，浏览者将按你的节奏查看
              </div>
            </el-form-item>

            <el-form-item label="配件参数">
              <AccessoryCardEditor v-model="accessoryCards" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, createPost } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import ImageGroupEditor from '@/components/ImageGroupEditor.vue'
import AccessoryCardEditor from '@/components/AccessoryCardEditor.vue'
import FaultTemplate from '@/components/FaultTemplate.vue'

const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const images = ref([])
const imageInfo = ref({ successCount: 0, failedCount: 0, all: [] })
const allImageItems = computed(() => imageInfo.value.all || [])
const imageGroups = ref([])
const imageGroupInfo = reactive({ organizedCount: 0, total: 0, groups: [] })
const accessoryCards = ref([])
const faultTemplate = ref({})
const submitting = ref(false)

const form = reactive({
  type: 1,
  categoryId: null,
  title: '',
  content: ''
})

const handleImageChange = (info) => {
  imageInfo.value = info
}

const handleGroupChange = (info) => {
  imageGroupInfo.organizedCount = info.organizedCount || 0
  imageGroupInfo.total = info.total || 0
  imageGroupInfo.groups = info.groups || []
}

const applyTemplateContent = (content) => {
  if (form.content.trim()) {
    form.content = form.content + '\n\n' + content
  } else {
    form.content = content
  }
  ElMessage.success('已应用到帖子内容')
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

const submit = async () => {
  if (!form.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim() && !(form.type === 2 && hasFaultTemplateContent())) {
    ElMessage.warning('请输入内容或填写故障现象模板')
    return
  }

  if (imageInfo.value.failedCount > 0) {
    ElMessageBox.confirm(
      `有 ${imageInfo.value.failedCount} 张图片上传失败，是否继续发布？失败的图片将不会被包含。`,
      '提示',
      {
        confirmButtonText: '继续发布',
        cancelButtonText: '返回修改',
        type: 'warning'
      }
    ).then(async () => {
      await doSubmit()
    }).catch(() => {})
    return
  }

  if (imageInfo.value.uploadingCount > 0) {
    ElMessage.warning('图片正在上传中，请稍候...')
    return
  }

  await doSubmit()
}

const hasFaultTemplateContent = () => {
  const t = faultTemplate.value
  return t && (t.deviceModel || t.accessoryModel || t.connectionType || t.symptoms || t.triedActions)
}

const generateFaultTemplateContent = () => {
  const t = faultTemplate.value
  if (!t) return ''
  const lines = []
  if (t.deviceModel) lines.push(`📱 **设备型号**：${t.deviceModel}`)
  if (t.accessoryModel) lines.push(`🔧 **配件型号**：${t.accessoryModel}`)
  if (t.connectionType) lines.push(`🔌 **连接方式**：${t.connectionType}`)
  if (t.symptoms) {
    lines.push('')
    lines.push('❓ **出现症状**')
    lines.push('')
    lines.push(t.symptoms)
  }
  if (t.triedActions) {
    lines.push('')
    lines.push('🔄 **已尝试动作**')
    lines.push('')
    lines.push(t.triedActions)
  }
  return lines.join('\n')
}

const contentHasTemplateMarkers = (content) => {
  if (!content) return false
  const markers = [
    '📱 **设备型号**',
    '🔧 **配件型号**',
    '🔌 **连接方式**',
    '❓ **出现症状**',
    '🔄 **已尝试动作**'
  ]
  return markers.some(m => content.includes(m))
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
    let finalContent = form.content
    if (form.type === 2 && hasFaultTemplateContent() && !contentHasTemplateMarkers(finalContent)) {
      const templateContent = generateFaultTemplateContent()
      if (templateContent) {
        if (finalContent.trim()) {
          finalContent = templateContent + '\n\n---\n\n' + finalContent
        } else {
          finalContent = templateContent
        }
      }
    }

    const postData = {
      ...form,
      content: finalContent,
      images: images.value,
      accessoryCards: accessoryCards.value
    }
    if (form.type === 1 && imageGroups.value && imageGroups.value.length > 0) {
      postData.imageGroups = imageGroups.value
    }
    await createPost(postData)
    ElMessage.success('发布成功')
    router.push('/')
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

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
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

  .form-item-tip {
    margin-top: 12px;
    padding: 10px 14px;
    background: #fffbe6;
    border: 1px solid #ffe58f;
    border-radius: 8px;
    font-size: 13px;
    color: #d48806;
    display: flex;
    align-items: center;
    gap: 8px;
    line-height: 1.6;

    .el-icon {
      flex-shrink: 0;
      font-size: 16px;
    }

    strong {
      color: #ad6800;
      font-weight: 600;
      margin: 0 2px;
    }

    &.success {
      background: #f0f9eb;
      border-color: #c2e7b0;
      color: #52c41a;

      strong {
        color: #389e0d;
      }
    }
  }
}
</style>
