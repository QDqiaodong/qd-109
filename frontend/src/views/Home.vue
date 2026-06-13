<template>
  <div class="home container" :class="{ 'has-compare-bar': !compareStore.isEmpty }">
    <div class="home-layout">
      <div class="main-content">
        <CategoryQuickBar sortType="hot" />

        <div class="section-tabs">
          <el-radio-group v-model="activeTab" size="large" @change="handleTabChange">
            <el-radio-button label="latest">最新发布</el-radio-button>
            <el-radio-button label="hot">热门讨论</el-radio-button>
          </el-radio-group>
        </div>

        <div class="post-list">
          <template v-if="infinite.initialLoading.value">
            <PostSkeleton v-for="i in 5" :key="i" />
          </template>
          <template v-else>
            <PostCard v-for="post in infinite.list.value" :key="post.id" :post="post" />
            <el-empty v-if="!infinite.loading.value && infinite.list.value.length === 0" description="暂无帖子" />
          </template>

          <div class="load-more-wrap">
            <div v-if="infinite.loading.value && !infinite.initialLoading.value">
              <PostSkeleton v-for="i in 3" :key="'load-' + i" />
            </div>
            <InfiniteLoadMore
              v-if="!infinite.initialLoading.value"
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
          <h3 class="sidebar-title">🔥 热门帖子</h3>
          <div class="hot-list" v-loading="hotLoading">
            <div v-for="(post, index) in hotPosts" :key="post.id" class="hot-item">
              <span class="hot-rank" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
              <router-link :to="`/post/${post.id}`" class="hot-title">{{ post.title }}</router-link>
            </div>
          </div>
        </div>

        <div class="card community-tip">
          <h3>💡 社区公约</h3>
          <ul>
            <li>分享真实使用体验，互帮互助</li>
            <li>友善交流，理性讨论</li>
            <li>不发布广告、交易信息</li>
            <li>尊重他人，维护社区氛围</li>
          </ul>
        </div>
      </aside>
    </div>

    <CompareBar />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getPostList, getHotPosts } from '@/api'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import PostCard from '@/components/PostCard.vue'
import PostSkeleton from '@/components/PostSkeleton.vue'
import InfiniteLoadMore from '@/components/InfiniteLoadMore.vue'
import CompareBar from '@/components/CompareBar.vue'
import CategoryQuickBar from '@/components/CategoryQuickBar.vue'
import { useCompareStore } from '@/store/compare'

const compareStore = useCompareStore()
const activeTab = ref('latest')
const hotPosts = ref([])
const hotLoading = ref(false)

const fetchPosts = (params) => {
  return getPostList({
    ...params,
    sort: activeTab.value
  })
}

const infinite = useInfiniteScroll(fetchPosts, {
  pageSize: 10,
  immediate: false,
  useWindowScroll: true,
  threshold: 200
})

const handleTabChange = () => {
  infinite.reload()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  compareStore.init()
  compareStore.syncFromStorage()
  infinite.loadMore()
  loadHotPosts()
})

const loadHotPosts = async () => {
  hotLoading.value = true
  try {
    hotPosts.value = await getHotPosts()
  } finally {
    hotLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.home {
  .home-layout {
    display: flex;
    gap: 24px;
  }

  .main-content {
    flex: 1;
    min-width: 0;
  }

  .sidebar {
    width: 320px;
    flex-shrink: 0;
  }

  .section-tabs {
    margin-bottom: 16px;
  }

  .post-list {
    margin-bottom: 20px;
  }

  .load-more-wrap {
    margin-top: -16px;
  }

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }

  .hot-list {
    .hot-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    .hot-rank {
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 600;
      border-radius: 4px;
      background: #f0f0f0;
      color: #999;

      &.top-three {
        background: #ff7a45;
        color: #fff;
      }
    }

    .hot-title {
      flex: 1;
      font-size: 14px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      &:hover {
        color: #1890ff;
      }
    }
  }

  .community-tip {
    h3 {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 12px;
    }

    ul {
      list-style: none;
      font-size: 13px;
      color: #666;

      li {
        padding: 6px 0;
        padding-left: 16px;
        position: relative;

        &::before {
          content: '•';
          position: absolute;
          left: 0;
          color: #1890ff;
        }
      }
    }
  }
}
</style>
