<template>
  <div class="home container">
    <div class="home-layout">
      <div class="main-content">
        <div class="section-tabs">
          <el-radio-group v-model="activeTab" size="large">
            <el-radio-button label="latest">最新发布</el-radio-button>
            <el-radio-button label="hot">热门讨论</el-radio-button>
          </el-radio-group>
        </div>

        <div class="post-list" v-loading="loading">
          <PostCard v-for="post in postList" :key="post.id" :post="post" />
          <el-empty v-if="!loading && postList.length === 0" description="暂无帖子" />
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadPosts"
          />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPostList, getHotPosts, getCategories } from '@/api'
import PostCard from '@/components/PostCard.vue'

const activeTab = ref('latest')
const postList = ref([])
const hotPosts = ref([])
const categories = ref([])
const loading = ref(false)
const hotLoading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadPosts()
  loadHotPosts()
  loadCategories()
})

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getPostList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    postList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadHotPosts = async () => {
  hotLoading.value = true
  try {
    hotPosts.value = await getHotPosts()
  } finally {
    hotLoading.value = false
  }
}

const loadCategories = async () => {
  categories.value = await getCategories()
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

  .pagination-wrap {
    display: flex;
    justify-content: center;
    padding: 20px 0;
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
  }

  .category-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px;
    border-radius: 8px;
    background: #f5f7fa;
    transition: all 0.2s;

    &:hover {
      background: #e6f7ff;
      color: #1890ff;
    }

    .cat-icon {
      font-size: 18px;
    }

    .cat-name {
      font-size: 13px;
    }
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
