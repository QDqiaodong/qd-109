<template>
  <div class="post-card card">
    <div class="post-header">
      <img :src="post.avatar" alt="" class="avatar" />
      <div class="post-meta">
        <span class="nickname">{{ post.nickname }}</span>
        <span class="post-time">{{ formatTime(post.createTime) }}</span>
      </div>
      <span :class="['tag', post.type === 1 ? 'tag-experience' : 'tag-question']">
        {{ post.type === 1 ? '体验分享' : '问题求助' }}
      </span>
    </div>

    <router-link :to="`/post/${post.id}`" class="post-title">{{ post.title }}</router-link>

    <p class="post-excerpt">{{ post.content }}</p>

    <div class="post-images" v-if="postImages && postImages.length">
      <img
        v-for="(img, idx) in postImages.slice(0, 3)"
        :key="idx"
        :src="img"
        class="post-image"
      />
    </div>

    <div class="post-footer">
      <span class="category-tag">{{ post.categoryName }}</span>
      <div class="post-stats">
        <span>👁️ {{ post.viewCount }}</span>
        <span>💬 {{ post.commentCount }}</span>
        <span>👍 {{ post.likeCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const postImages = computed(() => {
  if (!props.post.images) return []
  return Array.isArray(props.post.images) ? props.post.images : props.post.images.split(',')
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}
</script>

<style lang="scss" scoped>
.post-card {
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  .post-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .post-meta {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;

      .nickname {
        font-size: 14px;
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
    display: block;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
    transition: color 0.2s;

    &:hover {
      color: #1890ff;
    }
  }

  .post-excerpt {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .post-images {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    .post-image {
      width: 160px;
      height: 120px;
      object-fit: cover;
      border-radius: 6px;
    }
  }

  .post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    .category-tag {
      font-size: 12px;
      color: #1890ff;
      background: #e6f7ff;
      padding: 2px 8px;
      border-radius: 4px;
    }

    .post-stats {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: #999;
    }
  }
}
</style>
