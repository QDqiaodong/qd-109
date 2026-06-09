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

            <el-form-item label="上传图片">
              <ImageUpload v-model="images" :limit="9" @change="handleImageChange" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, createPost } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'

const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const images = ref([])
const imageInfo = ref({ successCount: 0, failedCount: 0, all: [] })
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

onMounted(() => {
  if (!userStore.userInfo) {
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
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
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

const doSubmit = async () => {
  submitting.value = true
  try {
    await createPost({
      ...form,
      images: images.value
    })
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
}
</style>
