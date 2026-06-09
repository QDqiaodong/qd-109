<template>
  <div class="search-page container">
    <div class="search-header card">
      <h1 class="search-title">
        <span class="search-icon">🔍</span>
        搜索结果
      </h1>
      <p class="search-keyword">
        关键词：<span class="keyword-text">{{ keyword }}</span>
        <span class="result-count" v-if="!infinite.initialLoading.value">
          共找到 {{ infinite.total.value }} 条结果
        </span>
      </p>
    </div>

    <div class="search-layout">
      <div class="main-content">
        <div class="filter-bar">
          <div class="filter-group">
            <span class="filter-label">分类：</span>
            <el-radio-group v-model="selectedCategory" size="default" @change="handleFilterChange">
              <el-radio-button :label="null">全部分类</el-radio-button>
              <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">
                {{ cat.name }}
              </el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group">
            <span class="filter-label">类型：</span>
            <el-radio-group v-model="selectedType" size="default" @change="handleFilterChange">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="1">体验分享</el-radio-button>
              <el-radio-button :label="2">问题求助</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="post-list">
          <template v-if="infinite.initialLoading.value">
            <PostSkeleton v-for="i in 5" :key="i" />
          </template>
          <template v-else>
            <div v-for="post in infinite.list.value" :key="post.id" class="search-result-item card">
              <div class="result-header">
                <img :src="post.avatar" alt="" class="avatar" />
                <div class="result-meta">
                  <span class="nickname" v-html="highlightText(post.nickname, keyword)"></span>
                  <span class="post-time">{{ formatTime(post.createTime) }}</span>
                </div>
                <span :class="['tag', post.type === 1 ? 'tag-experience' : 'tag-question']">
                  {{ post.type === 1 ? '体验分享' : '问题求助' }}
                </span>
              </div>

              <router-link :to="`/post/${post.id}`" class="result-title" v-html="highlightText(post.title, keyword)"></router-link>

              <p class="result-excerpt" v-html="highlightText(getSnippet(post.content, keyword), keyword)"></p>

              <div class="result-footer">
                <span class="category-tag">{{ post.categoryName }}</span>
                <div class="post-stats">
                  <span>👁️ {{ post.viewCount }}</span>
                  <span>💬 {{ post.commentCount }}</span>
                  <span>👍 {{ post.likeCount }}</span>
                </div>
              </div>
            </div>

            <el-empty v-if="!infinite.loading.value && infinite.list.value.length === 0" description="没有找到相关内容" />
          </template>

          <div class="load-more-wrap">
            <div v-if="infinite.loading.value && !infinite.initialLoading.value">
              <PostSkeleton v-for="i in 3" :key="'load-' + i" />
            </div>
            <InfiniteLoadMore
              v-if="!infinite.initialLoading.value && infinite.list.value.length > 0"
              :loading="infinite.loading.value"
              :error="infinite.error.value"
              :finished="infinite.finished.value"
              @retry="infinite.retry"
            />
          </div>
        </div>
      </div>

      <aside class="sidebar">
        <div class="card">
          <h3 class="sidebar-title">📂 分类导航</h3>
          <div class="category-list">
            <router-link
              v-for="cat in categories"
              :key="cat.id"
              :to="`/category/${cat.id}`"
              class="category-item"
            >
              <span class="cat-icon">{{ cat.icon }}</span>
              <span class="cat-name">{{ cat.name }}</span>
            </router-link>
          </div>
        </div>

        <div class="card">
          <h3 class="sidebar-title">💡 搜索技巧</h3>
          <ul class="search-tips">
            <li>输入关键词可搜索帖子标题和内容</li>
            <li>支持按作者昵称搜索</li>
            <li>可结合分类和类型筛选缩小范围</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchPosts, getCategories } from '@/api'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import { highlightKeyword, getContentSnippet } from '@/utils/highlight'
import PostSkeleton from '@/components/PostSkeleton.vue'
import InfiniteLoadMore from '@/components/InfiniteLoadMore.vue'

const route = useRoute()
const keyword = computed(() => route.query.q || '')
const categories = ref([])
const selectedCategory = ref(null)
const selectedType = ref(null)

const highlightText = (text, kw) => {
  return highlightKeyword(text, kw)
}

const getSnippet = (content, kw) => {
  return getContentSnippet(content, kw, 150)
}

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

const fetchPosts = (params) => {
  return searchPosts({
    ...params,
    keyword: keyword.value,
    categoryId: selectedCategory.value,
    type: selectedType.value
  })
}

const infinite = useInfiniteScroll(fetchPosts, {
  pageSize: 10,
  immediate: false,
  useWindowScroll: true,
  threshold: 200
})

const handleFilterChange = () => {
  infinite.reload()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(keyword, () => {
  selectedCategory.value = null
  selectedType.value = null
  infinite.reload()
  window.scrollTo({ top: 0, behavior: 'auto' })
})

onMounted(() => {
  loadCategories()
  if (keyword.value) {
    infinite.loadMore()
  }
})

const loadCategories = async () => {
  categories.value = await getCategories()
}
</script>

<style lang="scss" scoped>
.search-page {
  .search-header {
    margin-bottom: 20px;
    padding: 24px 28px;

    .search-title {
      font-size: 24px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #333;
      display: flex;
      align-items: center;
      gap: 10px;

      .search-icon {
        font-size: 28px;
      }
    }

    .search-keyword {
      font-size: 14px;
      color: #666;
      margin: 0;

      .keyword-text {
        color: #1890ff;
        font-weight: 500;
        margin: 0 4px;
      }

      .result-count {
        margin-left: 16px;
        color: #999;
      }
    }
  }

  .search-layout {
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

  .filter-bar {
    background: #fff;
    border-radius: 8px;
    padding: 16px 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .filter-group {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-label {
        font-size: 14px;
        color: #666;
        white-space: nowrap;
      }
    }
  }

  .post-list {
    margin-bottom: 20px;
  }

  .search-result-item {
    margin-bottom: 16px;
    padding: 20px;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    .result-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;

      .avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        object-fit: cover;
      }

      .result-meta {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 2px;

        .nickname {
          font-size: 14px;
          font-weight: 500;
          color: #333;

          :deep(.highlight) {
            color: #1890ff;
            font-style: normal;
            background: #e6f7ff;
            padding: 0 2px;
            border-radius: 2px;
          }
        }

        .post-time {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .result-title {
      display: block;
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin-bottom: 10px;
      transition: color 0.2s;
      line-height: 1.5;

      &:hover {
        color: #1890ff;
      }

      :deep(.highlight) {
        color: #1890ff;
        font-style: normal;
        background: #e6f7ff;
        padding: 0 2px;
        border-radius: 2px;
        font-weight: 600;
      }
    }

    .result-excerpt {
      font-size: 14px;
      color: #666;
      line-height: 1.7;
      margin-bottom: 14px;

      :deep(.highlight) {
        color: #1890ff;
        font-style: normal;
        background: #e6f7ff;
        padding: 0 2px;
        border-radius: 2px;
      }
    }

    .result-footer {
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

  .load-more-wrap {
    margin-top: -8px;
  }

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }

  .category-list {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;

    .category-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px;
      border-radius: 8px;
      background: #f5f7fa;
      transition: all 0.2s;
      font-size: 13px;

      &:hover {
        background: #e6f7ff;
        color: #1890ff;
      }

      .cat-icon {
        font-size: 16px;
      }
    }
  }

  .search-tips {
    list-style: none;
    padding: 0;
    margin: 0;
    font-size: 13px;
    color: #666;

    li {
      padding: 8px 0;
      padding-left: 20px;
      position: relative;

      &::before {
        content: '•';
        position: absolute;
        left: 0;
        color: #1890ff;
        font-weight: bold;
      }
    }
  }
}
</style>
