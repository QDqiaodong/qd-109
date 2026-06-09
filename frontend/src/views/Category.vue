<template>
  <div class="category-page container">
    <div class="page-header card">
      <h1 class="category-title">
        <span class="cat-icon">{{ currentCategory?.icon }}</span>
        {{ currentCategory?.name }}
      </h1>
      <p class="category-desc">探索{{ currentCategory?.name }}相关的经验分享与讨论</p>
    </div>

    <div class="category-layout">
      <div class="main-content">
        <div class="filter-tabs">
          <el-radio-group v-model="postType" size="large">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="1">体验分享</el-radio-button>
            <el-radio-button :label="2">问题求助</el-radio-button>
          </el-radio-group>
        </div>

        <div class="post-list" v-loading="loading">
          <PostCard v-for="post in postList" :key="post.id" :post="post" />
          <el-empty v-if="!loading && postList.length === 0" description="该分类下暂无帖子" />
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
          <h3 class="sidebar-title">📂 全部分类</h3>
          <div class="category-nav">
            <router-link
              v-for="cat in categories"
              :key="cat.id"
              :to="`/category/${cat.id}`"
              :class="['category-nav-item', { active: cat.id == categoryId }]"
            >
              <span class="cat-icon">{{ cat.icon }}</span>
              <span class="cat-name">{{ cat.name }}</span>
            </router-link>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPostList, getCategories } from '@/api'
import PostCard from '@/components/PostCard.vue'

const route = useRoute()
const categoryId = computed(() => route.params.id)
const postList = ref([])
const categories = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const postType = ref(null)

const currentCategory = computed(() => {
  return categories.value.find(c => c.id == categoryId.value)
})

onMounted(() => {
  loadCategories()
  loadPosts()
})

watch(categoryId, () => {
  pageNum.value = 1
  loadPosts()
})

watch(postType, () => {
  pageNum.value = 1
  loadPosts()
})

const loadCategories = async () => {
  categories.value = await getCategories()
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getPostList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      categoryId: categoryId.value,
      type: postType.value
    })
    postList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.category-page {
  .page-header {
    margin-bottom: 20px;
    text-align: center;
    padding: 32px 20px;

    .category-title {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 8px;

      .cat-icon {
        margin-right: 12px;
      }
    }

    .category-desc {
      font-size: 14px;
      color: #999;
    }
  }

  .category-layout {
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

  .filter-tabs {
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
  }

  .category-nav {
    .category-nav-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px;
      border-radius: 8px;
      margin-bottom: 8px;
      transition: all 0.2s;

      &:hover, &.active {
        background: #e6f7ff;
      }

      &.active {
        color: #1890ff;
        font-weight: 500;
      }

      .cat-icon {
        font-size: 18px;
      }

      .cat-name {
        font-size: 14px;
      }
    }
  }
}
</style>
