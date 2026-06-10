<template>
  <div class="post-detail container">
    <div class="detail-layout">
      <div class="main-content">
        <div class="post-card card" v-loading="loading">
          <div class="post-header">
            <img :src="post?.avatar" alt="" class="avatar" />
            <div class="post-meta">
              <span class="nickname">{{ post?.nickname }}</span>
              <span class="post-time">{{ post?.createTime }}</span>
            </div>
            <span :class="['tag', post?.type === 1 ? 'tag-experience' : 'tag-question']">
              {{ post?.type === 1 ? '体验分享' : '问题求助' }}
            </span>
          </div>

          <h1 class="post-title">{{ post?.title }}</h1>

          <div class="post-category">
            <span class="category-tag">{{ post?.categoryName }}</span>
          </div>

          <div class="accessory-cards" v-if="post?.accessoryCards?.length">
            <AccessoryCard
              v-for="(card, idx) in post.accessoryCards"
              :key="idx"
              :card="card"
            />
          </div>

          <div class="post-content">
            <p v-for="(para, idx) in contentParagraphs" :key="idx">{{ para }}</p>
          </div>

          <div v-if="hasImageGroups" class="grouped-images-section">
            <div class="section-intro">
              <el-icon><Reading /></el-icon>
              <span>作者按以下节奏分享使用体验</span>
            </div>
            <div class="grouped-images-flow">
              <div
                v-for="(group, gIdx) in sortedImageGroups"
                :key="group.key"
                class="grouped-image-block"
              >
                <div class="group-step-indicator">
                  <div class="step-dot" :style="{ background: getGroupColor(group.key) }">
                    {{ gIdx + 1 }}
                  </div>
                  <div v-if="gIdx < sortedImageGroups.length - 1" class="step-line"></div>
                </div>
                <div class="group-main-card">
                  <div class="group-title-bar" :style="{ borderLeftColor: getGroupColor(group.key) }">
                    <span class="group-title-icon">{{ getGroupIcon(group.key) }}</span>
                    <div class="group-title-text">
                      <h3 class="group-name" :style="{ color: getGroupColor(group.key) }">
                        {{ group.label }}
                      </h3>
                      <span class="group-desc">{{ getGroupDescription(group.key) }}</span>
                    </div>
                    <span class="group-counter">{{ group.images?.length || 0 }} 张图</span>
                  </div>
                  <div class="group-image-grid">
                    <div
                      v-for="(img, iIdx) in group.images"
                      :key="'gi' + gIdx + '-' + iIdx"
                      class="grouped-image-item"
                      @click="openImagePreview(gIdx, iIdx)"
                    >
                      <img :src="img" :alt="`${group.label}-${iIdx + 1}`" class="grouped-img" />
                      <span class="image-number">{{ iIdx + 1 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="post-images" v-else-if="post?.images?.length">
            <img
              v-for="(img, idx) in post.images"
              :key="idx"
              :src="img"
              class="post-image"
              @click="openFlatPreview(idx)"
            />
          </div>

          <div class="post-stats-bar">
            <span>👁️ 浏览 {{ post?.viewCount }}</span>
            <span>💬 评论 {{ post?.commentCount }}</span>
            <span>👍 点赞 {{ post?.likeCount }}</span>
          </div>
        </div>

        <div class="comment-section card">
          <h3 class="section-title">💬 评论区 ({{ comments.length }})</h3>

          <div class="comment-input-wrap">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
            />
            <div class="comment-actions">
              <el-button type="primary" @click="submitComment" :disabled="!commentContent">
                发表评论
              </el-button>
            </div>
          </div>

          <div class="comment-list" v-loading="commentLoading">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-main">
                <img :src="comment.avatar" alt="" class="avatar" />
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="nickname">{{ comment.nickname }}</span>
                    <span class="comment-time">{{ comment.createTime }}</span>
                  </div>
                  <p class="comment-text">
                    <span v-if="comment.replyNickname" class="reply-to">
                      回复 @{{ comment.replyNickname }}：
                    </span>
                    {{ comment.content }}
                  </p>
                  <div class="comment-actions-bar">
                    <span class="action-btn" @click="replyTo(comment)">回复</span>
                  </div>
                </div>
              </div>

              <div v-if="comment.children?.length" class="comment-children">
                <div v-for="child in comment.children" :key="child.id" class="comment-item child">
                  <img :src="child.avatar" alt="" class="avatar" />
                  <div class="comment-content">
                    <div class="comment-header">
                      <span class="nickname">{{ child.nickname }}</span>
                      <span class="comment-time">{{ child.createTime }}</span>
                    </div>
                    <p class="comment-text">
                      <span v-if="child.replyNickname" class="reply-to">
                        回复 @{{ child.replyNickname }}：
                      </span>
                      {{ child.content }}
                    </p>
                    <div class="comment-actions-bar">
                      <span class="action-btn" @click="replyTo(child)">回复</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <el-empty v-if="!commentLoading && comments.length === 0" description="暂无评论" />
          </div>
        </div>
      </div>

      <aside class="sidebar">
        <div class="card">
          <h3 class="sidebar-title">👤 作者</h3>
          <div class="author-info">
            <img :src="post?.avatar" alt="" class="author-avatar" />
            <span class="author-name">{{ post?.nickname }}</span>
          </div>
        </div>
      </aside>
    </div>

    <el-dialog
      v-model="imagePreviewVisible"
      width="auto"
      align-center
      :show-close="true"
      class="detail-preview-dialog"
      @keydown.left.prevent="prevPreviewImage"
      @keydown.right.prevent="nextPreviewImage"
      @keydown.esc.prevent="imagePreviewVisible = false"
    >
      <template #header>
        <div class="preview-dialog-header">
          <span class="preview-counter">{{ currentPreviewIdx + 1 }} / {{ flatPreviewList.length }}</span>
          <span class="preview-caption" v-if="currentPreviewGroup">
            {{ currentPreviewGroup.icon }} {{ currentPreviewGroup.label }} · 第 {{ currentPreviewInGroup + 1 }} 张
          </span>
        </div>
      </template>
      <div class="preview-dialog-body">
        <el-icon
          class="p-nav-btn p-prev"
          @click="prevPreviewImage"
          v-if="flatPreviewList.length > 1"
        ><ArrowLeft /></el-icon>
        <img
          :src="flatPreviewList[currentPreviewIdx]?.url"
          :alt="'预览图片'"
          class="preview-dialog-img"
          @wheel.stop.prevent="handlePreviewWheel"
          :style="previewTransform"
        />
        <el-icon
          class="p-nav-btn p-next"
          @click="nextPreviewImage"
          v-if="flatPreviewList.length > 1"
        ><ArrowRight /></el-icon>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, getComments, createComment } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Reading, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import AccessoryCard from '@/components/AccessoryCard.vue'

const GROUP_META = {
  appearance: { icon: '🎨', color: '#722ed1', desc: '产品整体外观、设计语言、做工细节' },
  interface:  { icon: '🔌', color: '#13c2c2', desc: '接口类型、线材、按键、连接方式' },
  usage:      { icon: '💻', color: '#1890ff', desc: '装在设备上的实际效果、使用场景' },
  desk:       { icon: '🖼️', color: '#fa8c16', desc: '整体桌面环境、氛围灯光、搭配组合' }
}

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const loading = ref(false)
const commentLoading = ref(false)
const commentContent = ref('')
const replyingTo = ref(null)

const imagePreviewVisible = ref(false)
const currentPreviewIdx = ref(0)
const currentPreviewState = reactive({ groupKey: null, indexInGroup: 0 })
const previewScale = ref(1)

const hasImageGroups = computed(() =>
  post.value?.imageGroups &&
  Array.isArray(post.value.imageGroups) &&
  post.value.imageGroups.some(g => g.images && g.images.length > 0)
)

const sortedImageGroups = computed(() => {
  if (!hasImageGroups.value) return []
  const sortOrder = ['appearance', 'interface', 'usage', 'desk']
  const result = post.value.imageGroups
    .filter(g => g.images && g.images.length > 0)
    .slice()
    .sort((a, b) => {
      const ai = typeof a.sort === 'number' ? a.sort : sortOrder.indexOf(a.key)
      const bi = typeof b.sort === 'number' ? b.sort : sortOrder.indexOf(b.key)
      if (ai === bi) return sortOrder.indexOf(a.key) - sortOrder.indexOf(b.key)
      return ai - bi
    })
  return result
})

const flatPreviewList = computed(() => {
  if (hasImageGroups.value) {
    const result = []
    sortedImageGroups.value.forEach(g => {
      (g.images || []).forEach(img => result.push({ url: img, key: g.key }))
    })
    return result
  }
  return (post.value?.images || []).map(url => ({ url, key: null }))
})

const currentPreviewGroup = computed(() => {
  const item = flatPreviewList.value[currentPreviewIdx.value]
  if (!item || !item.key) return null
  const group = sortedImageGroups.value.find(g => g.key === item.key)
  if (!group) return null
  const meta = GROUP_META[group.key] || { icon: '🖼️' }
  return {
    label: group.label,
    icon: meta.icon
  }
})

const currentPreviewInGroup = computed(() => {
  return currentPreviewState.indexInGroup
})

const previewTransform = computed(() => `scale(${previewScale.value})`)

const getGroupColor = (key) => GROUP_META[key]?.color || '#409eff'
const getGroupIcon = (key) => GROUP_META[key]?.icon || '🖼️'
const getGroupDescription = (key) => GROUP_META[key]?.desc || ''

const openImagePreview = (gIdx, iIdx) => {
  const group = sortedImageGroups.value[gIdx]
  if (!group) return
  currentPreviewState.groupKey = group.key
  currentPreviewState.indexInGroup = iIdx
  let flatIdx = 0
  for (let i = 0; i < gIdx; i++) {
    flatIdx += sortedImageGroups.value[i].images.length
  }
  flatIdx += iIdx
  currentPreviewIdx.value = flatIdx
  previewScale.value = 1
  imagePreviewVisible.value = true
}

const openFlatPreview = (idx) => {
  currentPreviewState.groupKey = null
  currentPreviewState.indexInGroup = idx
  currentPreviewIdx.value = idx
  previewScale.value = 1
  imagePreviewVisible.value = true
}

const prevPreviewImage = () => {
  if (flatPreviewList.value.length === 0) return
  currentPreviewIdx.value = (currentPreviewIdx.value - 1 + flatPreviewList.value.length) % flatPreviewList.value.length
  updatePreviewState()
  previewScale.value = 1
}

const nextPreviewImage = () => {
  if (flatPreviewList.value.length === 0) return
  currentPreviewIdx.value = (currentPreviewIdx.value + 1) % flatPreviewList.value.length
  updatePreviewState()
  previewScale.value = 1
}

const updatePreviewState = () => {
  const item = flatPreviewList.value[currentPreviewIdx.value]
  if (!item || !item.key) {
    currentPreviewState.groupKey = null
    currentPreviewState.indexInGroup = currentPreviewIdx.value
    return
  }
  currentPreviewState.groupKey = item.key
  const group = sortedImageGroups.value.find(g => g.key === item.key)
  if (group) {
    let idx = 0
    for (let i = 0; i < sortedImageGroups.value.length; i++) {
      if (sortedImageGroups.value[i].key === item.key) {
        break
      }
      idx += sortedImageGroups.value[i].images.length
    }
    currentPreviewState.indexInGroup = currentPreviewIdx.value - idx
  }
}

const handlePreviewWheel = (e) => {
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  previewScale.value = Math.max(0.5, Math.min(3, previewScale.value + delta))
}

const contentParagraphs = computed(() => {
  if (!post.value?.content) return []
  return post.value.content.split('\n').filter(p => p.trim())
})

onMounted(() => {
  loadPost()
  loadComments()
})

const loadPost = async () => {
  loading.value = true
  try {
    post.value = await getPostDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  commentLoading.value = true
  try {
    comments.value = await getComments(route.params.id)
  } finally {
    commentLoading.value = false
  }
}

const submitComment = async () => {
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    return
  }
  if (!commentContent.value.trim()) return

  try {
    const data = {
      postId: route.params.id,
      content: commentContent.value
    }
    if (replyingTo.value) {
      data.parentId = replyingTo.value.parentId || replyingTo.value.id
      data.replyUserId = replyingTo.value.userId
    }
    await createComment(data)
    ElMessage.success('评论成功')
    commentContent.value = ''
    replyingTo.value = null
    loadComments()
  } catch (e) {}
}

const replyTo = (comment) => {
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    return
  }
  replyingTo.value = comment
}
</script>

<style lang="scss" scoped>
.post-detail {
  .detail-layout {
    display: flex;
    gap: 24px;
  }

  .main-content {
    flex: 1;
    min-width: 0;
  }

  .sidebar {
    width: 280px;
    flex-shrink: 0;
  }

  .post-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;

    .post-meta {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;

      .nickname {
        font-size: 15px;
        font-weight: 500;
        color: #333;
      }

      .post-time {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .post-title {
    font-size: 24px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 12px;
    line-height: 1.4;
  }

  .post-category {
    margin-bottom: 20px;

    .category-tag {
      display: inline-block;
      font-size: 13px;
      color: #1890ff;
      background: #e6f7ff;
      padding: 4px 12px;
      border-radius: 4px;
    }
  }

  .post-content {
    font-size: 15px;
    line-height: 1.8;
    color: #333;
    margin-bottom: 20px;

    p {
      margin-bottom: 16px;
    }
  }

  .post-images {
    margin-bottom: 20px;

    .post-image {
      max-width: 100%;
      border-radius: 8px;
      margin-bottom: 12px;
      cursor: zoom-in;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.01);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
      }
    }
  }

  .grouped-images-section {
    margin-bottom: 28px;
    padding: 24px;
    background: linear-gradient(135deg, #fafbfc 0%, #f4f7fa 100%);
    border-radius: 16px;
    border: 1px solid #ebeef5;

    .section-intro {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      background: #fff;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
      color: #606266;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      margin-bottom: 24px;

      .el-icon {
        color: #409eff;
        font-size: 16px;
      }
    }
  }

  .grouped-images-flow {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .grouped-image-block {
    display: flex;
    gap: 16px;
    position: relative;
  }

  .group-step-indicator {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
    width: 44px;

    .step-dot {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      color: #fff;
      font-weight: 700;
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      flex-shrink: 0;
      z-index: 2;
    }

    .step-line {
      flex: 1;
      width: 3px;
      background: linear-gradient(180deg, #d9ecff 0%, #ecf5ff 100%);
      margin-top: 6px;
      min-height: 40px;
      border-radius: 2px;
    }
  }

  .group-main-card {
    flex: 1;
    min-width: 0;
    background: #fff;
    border-radius: 14px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    transition: box-shadow 0.3s;

    &:hover {
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    }
  }

  .group-title-bar {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #fafbfc 0%, #f6f9fc 100%);
    border-bottom: 1px solid #f0f0f0;
    border-left: 5px solid;

    .group-title-icon {
      font-size: 26px;
      flex-shrink: 0;
    }

    .group-title-text {
      flex: 1;
      min-width: 0;

      .group-name {
        font-size: 18px;
        font-weight: 700;
        margin: 0 0 3px 0;
        line-height: 1.2;
      }

      .group-desc {
        font-size: 12px;
        color: #909399;
        line-height: 1.4;
      }
    }

    .group-counter {
      flex-shrink: 0;
      padding: 5px 14px;
      background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
      color: #409eff;
      border-radius: 14px;
      font-size: 12px;
      font-weight: 600;
    }
  }

  .group-image-grid {
    padding: 20px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 14px;
  }

  .grouped-image-item {
    position: relative;
    width: 100%;
    aspect-ratio: 4 / 3;
    border-radius: 10px;
    overflow: hidden;
    cursor: zoom-in;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

      .grouped-img {
        transform: scale(1.05);
      }

      .image-number {
        opacity: 1;
        transform: scale(1);
      }
    }

    .grouped-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
      transition: transform 0.4s ease;
    }

    .image-number {
      position: absolute;
      bottom: 8px;
      right: 8px;
      background: rgba(0, 0, 0, 0.65);
      color: #fff;
      font-size: 11px;
      font-weight: 600;
      padding: 3px 10px;
      border-radius: 10px;
      backdrop-filter: blur(4px);
      opacity: 0.85;
      transform: scale(0.9);
      transition: all 0.25s ease;
    }
  }

  .detail-preview-dialog {
    :deep(.el-dialog__header) {
      padding: 14px 20px;
      border-bottom: 1px solid #f0f0f0;
      margin-right: 0;
    }

    :deep(.el-dialog__body) {
      padding: 0;
    }
  }

  .preview-dialog-header {
    display: flex;
    align-items: center;
    gap: 16px;

    .preview-counter {
      color: #909399;
      font-size: 13px;
      background: #f5f7fa;
      padding: 4px 12px;
      border-radius: 4px;
      font-weight: 500;
    }

    .preview-caption {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
  }

  .preview-dialog-body {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #1a1a1a;
    min-height: 300px;
    max-height: 75vh;
    overflow: hidden;
    padding: 40px 20px;
  }

  .preview-dialog-img {
    max-width: 88vw;
    max-height: 68vh;
    display: block;
    object-fit: contain;
    border-radius: 6px;
    transition: transform 0.2s ease;
    cursor: grab;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);

    &:active {
      cursor: grabbing;
    }
  }

  .p-nav-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    font-size: 32px;
    color: rgba(255, 255, 255, 0.75);
    cursor: pointer;
    padding: 18px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
    transition: all 0.25s;
    z-index: 10;
    backdrop-filter: blur(8px);

    &:hover {
      color: #fff;
      background: rgba(255, 255, 255, 0.18);
      transform: translateY(-50%) scale(1.1);
    }

    &.p-prev {
      left: 28px;
    }

    &.p-next {
      right: 28px;
    }
  }

  .post-stats-bar {
    display: flex;
    gap: 32px;
    padding: 16px 0;
    border-top: 1px solid #f0f0f0;
    font-size: 14px;
    color: #999;
  }

  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
  }

  .comment-input-wrap {
    margin-bottom: 24px;

    .comment-actions {
      margin-top: 12px;
      text-align: right;
    }
  }

  .comment-item {
    display: flex;
    gap: 12px;
    padding: 16px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    &.child {
      padding: 12px 0 0;
      border-bottom: none;
    }

    .comment-main {
      display: flex;
      gap: 12px;
      flex: 1;
    }

    .comment-content {
      flex: 1;
      min-width: 0;
    }

    .comment-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 6px;

      .nickname {
        font-size: 14px;
        font-weight: 500;
        color: #1890ff;
      }

      .comment-time {
        font-size: 12px;
        color: #999;
      }
    }

    .comment-text {
      font-size: 14px;
      line-height: 1.6;
      color: #333;
      margin-bottom: 8px;

      .reply-to {
        color: #1890ff;
      }
    }

    .comment-actions-bar {
      .action-btn {
        font-size: 12px;
        color: #999;
        cursor: pointer;

        &:hover {
          color: #1890ff;
        }
      }
    }

    .comment-children {
      margin-left: 52px;
      margin-top: 8px;
      background: #fafafa;
      border-radius: 6px;
      padding: 0 12px;
    }
  }

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  .author-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .author-avatar {
      width: 64px;
      height: 64px;
      border-radius: 50%;
    }

    .author-name {
      font-size: 16px;
      font-weight: 500;
    }
  }
}
</style>
