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

          <div class="post-images" v-if="post?.images?.length">
            <img v-for="(img, idx) in post.images" :key="idx" :src="img" class="post-image" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, getComments, createComment } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import AccessoryCard from '@/components/AccessoryCard.vue'

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const loading = ref(false)
const commentLoading = ref(false)
const commentContent = ref('')
const replyingTo = ref(null)

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
