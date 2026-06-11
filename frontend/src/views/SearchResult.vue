<template>
  <div class="search-page container" :class="{ 'has-compare-bar': !compareStore.isEmpty }">
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
          <div class="filter-group">
            <span class="filter-label">视图：</span>
            <el-radio-group v-model="viewMode" size="default">
              <el-radio-button label="list">📋 列表视图</el-radio-button>
              <el-radio-button label="group">📁 分组视图</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-group" v-if="viewMode === 'group'">
            <span class="filter-label">分组依据：</span>
            <el-radio-group v-model="groupBy" size="default">
              <el-radio-button label="category">📂 设备大类</el-radio-button>
              <el-radio-button label="interface">🔌 接口类型</el-radio-button>
              <el-radio-button label="brand">🏷️ 品牌</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="post-list">
          <template v-if="infinite.initialLoading.value">
            <PostSkeleton v-for="i in 5" :key="i" />
          </template>
          <template v-else>
            <template v-if="viewMode === 'list'">
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
                  <div class="result-footer-actions">
                    <button
                      :class="['compare-btn', { active: isInCompare(post.id) }]"
                      @click.stop="toggleCompare(post)"
                      :title="isInCompare(post.id) ? '移出对照' : '加入对照'"
                    >
                      <span class="compare-icon">⚖️</span>
                      <span class="compare-text">{{ isInCompare(post.id) ? '已对照' : '对照' }}</span>
                    </button>
                    <div class="post-stats">
                      <span>👁️ {{ post.viewCount }}</span>
                      <span>💬 {{ post.commentCount }}</span>
                      <span>👍 {{ post.likeCount }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <template v-else>
              <div v-for="group in groupedPosts" :key="group.key" class="group-section">
                <div
                  class="group-header card"
                  @click="toggleGroup(group.key)"
                >
                  <div class="group-header-left">
                    <span class="expand-icon" :class="{ expanded: expandedGroups.has(group.key) }">
                      ▶
                    </span>
                    <span class="group-icon">{{ group.icon }}</span>
                    <span class="group-name">{{ group.name }}</span>
                    <span class="group-count-badge">{{ group.posts.length }} 条</span>
                  </div>
                  <div class="group-header-right">
                    <span class="group-summary">
                      {{ expandedGroups.has(group.key) ? '点击收起' : '点击展开查看' }}
                    </span>
                  </div>
                </div>

                <transition name="expand">
                  <div v-show="expandedGroups.has(group.key)" class="group-content">
                    <div
                      v-for="post in group.posts"
                      :key="post.id"
                      class="search-result-item card group-item"
                    >
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
                        <div class="result-footer-actions">
                          <button
                            :class="['compare-btn', { active: isInCompare(post.id) }]"
                            @click.stop="toggleCompare(post)"
                            :title="isInCompare(post.id) ? '移出对照' : '加入对照'"
                          >
                            <span class="compare-icon">⚖️</span>
                            <span class="compare-text">{{ isInCompare(post.id) ? '已对照' : '对照' }}</span>
                          </button>
                          <div class="post-stats">
                            <span>👁️ {{ post.viewCount }}</span>
                            <span>💬 {{ post.commentCount }}</span>
                            <span>👍 {{ post.likeCount }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>

              <div v-if="ungroupedPosts.length > 0" class="group-section">
                <div
                  class="group-header card ungrouped"
                  @click="toggleGroup('__ungrouped__')"
                >
                  <div class="group-header-left">
                    <span class="expand-icon" :class="{ expanded: expandedGroups.has('__ungrouped__') }">
                      ▶
                    </span>
                    <span class="group-icon">📦</span>
                    <span class="group-name">其他（未分类）</span>
                    <span class="group-count-badge">{{ ungroupedPosts.length }} 条</span>
                  </div>
                  <div class="group-header-right">
                    <span class="group-summary">
                      {{ expandedGroups.has('__ungrouped__') ? '点击收起' : '点击展开查看' }}
                    </span>
                  </div>
                </div>

                <transition name="expand">
                  <div v-show="expandedGroups.has('__ungrouped__')" class="group-content">
                    <div
                      v-for="post in ungroupedPosts"
                      :key="post.id"
                      class="search-result-item card group-item"
                    >
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
                        <div class="result-footer-actions">
                          <button
                            :class="['compare-btn', { active: isInCompare(post.id) }]"
                            @click.stop="toggleCompare(post)"
                            :title="isInCompare(post.id) ? '移出对照' : '加入对照'"
                          >
                            <span class="compare-icon">⚖️</span>
                            <span class="compare-text">{{ isInCompare(post.id) ? '已对照' : '对照' }}</span>
                          </button>
                          <div class="post-stats">
                            <span>👁️ {{ post.viewCount }}</span>
                            <span>💬 {{ post.commentCount }}</span>
                            <span>👍 {{ post.likeCount }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>
            </template>

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

    <CompareBar />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { searchPosts, getCategories } from '@/api'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import { highlightKeyword, getContentSnippet } from '@/utils/highlight'
import { useCompareStore } from '@/store/compare'
import { ElMessage } from 'element-plus'
import PostSkeleton from '@/components/PostSkeleton.vue'
import InfiniteLoadMore from '@/components/InfiniteLoadMore.vue'
import CompareBar from '@/components/CompareBar.vue'

const compareStore = useCompareStore()

const comparedIdSet = computed(() => {
  const set = new Set()
  compareStore.compareList.forEach(item => set.add(item.id))
  return set
})

const isInCompare = (postId) => comparedIdSet.value.has(postId)

const toggleCompare = (post) => {
  if (isInCompare(post.id)) {
    compareStore.removeFromCompare(post.id)
    ElMessage.info('已移出对照清单')
  } else {
    if (compareStore.isMax) {
      ElMessage.warning(`对照清单最多添加 ${compareStore.MAX_COMPARE_ITEMS} 篇帖子`)
      return
    }
    const added = compareStore.addToCompare(post)
    if (added) {
      ElMessage.success('已加入对照清单')
    }
  }
}

const route = useRoute()
const keyword = computed(() => route.query.q || '')
const categories = ref([])
const selectedCategory = ref(null)
const selectedType = ref(null)
const viewMode = ref('list')
const groupBy = ref('category')
const expandedGroups = reactive(new Set())

const CATEGORY_ICONS = {
  '手机配件': '📱',
  '电脑配件': '💻',
  '影音设备': '🎧',
  '智能穿戴': '⌚',
  '摄影器材': '📷',
  '游戏外设': '🎮',
  '网络设备': '📶',
  '存储设备': '💾'
}

const BRAND_PATTERNS = [
  { brand: 'Apple', patterns: ['Apple', 'iPhone', 'iPad', 'MacBook', 'iMac', 'AirPods', 'Apple Watch', 'Mac', 'iOS'] },
  { brand: '华为', patterns: ['华为', 'HUAWEI', 'Mate', 'Pura', 'P系列', 'Nova', '荣耀', 'HONOR'] },
  { brand: '小米', patterns: ['小米', 'Xiaomi', 'Redmi', '红米', 'POCO'] },
  { brand: '三星', patterns: ['三星', 'Samsung', 'Galaxy'] },
  { brand: '索尼', patterns: ['索尼', 'Sony', 'WH-', 'WF-', 'WI-'] },
  { brand: '樱桃', patterns: ['樱桃', 'Cherry', 'MX'] },
  { brand: '佳达隆', patterns: ['佳达隆', 'Gateron'] },
  { brand: '罗技', patterns: ['罗技', 'Logitech', 'G系列'] },
  { brand: '雷蛇', patterns: ['雷蛇', 'Razer'] },
  { brand: '绿联', patterns: ['绿联', 'UGREEN'] },
  { brand: '倍思', patterns: ['倍思', 'Baseus'] },
  { brand: 'Anker', patterns: ['安克', 'Anker'] },
  { brand: '贝尔金', patterns: ['贝尔金', 'Belkin'] },
  { brand: '戴尔', patterns: ['戴尔', 'Dell', 'XPS', 'Latitude'] },
  { brand: '联想', patterns: ['联想', 'Lenovo', 'ThinkPad', '小新', '拯救者'] },
  { brand: '惠普', patterns: ['惠普', 'HP', '暗影精灵', '战66'] },
  { brand: '华硕', patterns: ['华硕', 'ASUS', 'ROG', '天选'] }
]

const extractBrand = (post) => {
  const textParts = []
  if (post.title) textParts.push(post.title)
  if (post.accessoryCards && post.accessoryCards.length > 0) {
    post.accessoryCards.forEach(card => {
      if (card.model) textParts.push(card.model)
    })
  }
  const text = textParts.join(' ')
  for (const { brand, patterns } of BRAND_PATTERNS) {
    for (const pattern of patterns) {
      if (text.includes(pattern)) {
        return brand
      }
    }
  }
  return null
}

const extractInterfaceType = (post) => {
  if (post.accessoryCards && post.accessoryCards.length > 0) {
    const types = new Set()
    post.accessoryCards.forEach(card => {
      if (card.interfaceType) {
        const normalized = card.interfaceType
          .replace(/\s*\/\s*/g, '/')
          .split('/')
          .map(t => t.trim())
          .filter(t => t)
        normalized.forEach(t => types.add(t))
      }
    })
    if (types.size > 0) {
      return Array.from(types).sort()
    }
  }
  const text = (post.title || '') + ' ' + (post.content || '')
  if (/Type[-\s]?C/i.test(text)) return ['Type-C']
  if (/Lightning/i.test(text)) return ['Lightning']
  if (/USB[-\s]?A/i.test(text) || /USB[\s-]?2\.?0?/i.test(text) || /USB[\s-]?3/i.test(text)) return ['USB-A']
  if (/蓝牙|Bluetooth/i.test(text)) return ['蓝牙']
  if (/HDMI/i.test(text)) return ['HDMI']
  if (/3\.5mm|耳机孔|音频线/i.test(text)) return ['3.5mm音频']
  if (/Wi-?Fi|无线/i.test(text)) return ['Wi-Fi']
  if (/雷电|Thunderbolt/i.test(text)) return ['雷电']
  return null
}

const toggleGroup = (key) => {
  if (expandedGroups.has(key)) {
    expandedGroups.delete(key)
  } else {
    expandedGroups.add(key)
  }
}

const groupedPosts = computed(() => {
  const list = infinite.list.value
  if (!list || list.length === 0) return []

  const groupMap = new Map()
  const ungrouped = []

  list.forEach(post => {
    let keys = []
    if (groupBy.value === 'category') {
      if (post.categoryName) {
        keys = [{ key: post.categoryName, name: post.categoryName, icon: CATEGORY_ICONS[post.categoryName] || '📦' }]
      }
    } else if (groupBy.value === 'interface') {
      const types = extractInterfaceType(post)
      if (types && types.length > 0) {
        keys = types.map(t => ({ key: t, name: t, icon: '🔌' }))
      }
    } else if (groupBy.value === 'brand') {
      const brand = extractBrand(post)
      if (brand) {
        keys = [{ key: brand, name: brand, icon: '🏷️' }]
      }
    }

    if (keys.length === 0) {
      ungrouped.push(post)
    } else {
      keys.forEach(({ key, name, icon }) => {
        if (!groupMap.has(key)) {
          groupMap.set(key, { key, name, icon, posts: [] })
        }
        if (!groupMap.get(key).posts.find(p => p.id === post.id)) {
          groupMap.get(key).posts.push(post)
        }
      })
    }
  })

  ungroupedPosts.value = ungrouped

  return Array.from(groupMap.values()).sort((a, b) => {
    if (a.posts.length !== b.posts.length) {
      return b.posts.length - a.posts.length
    }
    return a.name.localeCompare(b.name, 'zh-CN')
  })
})

const ungroupedPosts = ref([])

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
  expandedGroups.clear()
  infinite.reload()
  window.scrollTo({ top: 0, behavior: 'auto' })
})

watch(viewMode, () => {
  expandedGroups.clear()
})

watch(groupBy, () => {
  expandedGroups.clear()
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

  .group-section {
    margin-bottom: 12px;

    .group-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      cursor: pointer;
      transition: all 0.2s;
      user-select: none;
      background: linear-gradient(135deg, #f8f9ff 0%, #f0f5ff 100%);
      border: 1px solid #e8ecff;

      &:hover {
        box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
        border-color: #c5cfff;
      }

      &.ungrouped {
        background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
        border-color: #ffe58f;

        &:hover {
          box-shadow: 0 4px 16px rgba(250, 173, 20, 0.15);
          border-color: #ffd666;
        }
      }

      .group-header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .expand-icon {
          display: inline-block;
          font-size: 12px;
          color: #1890ff;
          transition: transform 0.25s ease;

          &.expanded {
            transform: rotate(90deg);
          }
        }

        .group-icon {
          font-size: 22px;
        }

        .group-name {
          font-size: 16px;
          font-weight: 600;
          color: #1a1a1a;
        }

        .group-count-badge {
          display: inline-block;
          padding: 2px 10px;
          font-size: 12px;
          font-weight: 500;
          color: #1890ff;
          background: #e6f7ff;
          border-radius: 10px;
        }
      }

      .group-header-right {
        .group-summary {
          font-size: 13px;
          color: #999;
        }
      }
    }

    .group-content {
      padding-top: 12px;
    }

    .group-item {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .expand-enter-active,
  .expand-leave-active {
    transition: all 0.3s ease;
    overflow: hidden;
  }

  .expand-enter-from,
  .expand-leave-to {
    opacity: 0;
    max-height: 0;
  }

  .expand-enter-to,
  .expand-leave-from {
    opacity: 1;
    max-height: 5000px;
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

      .result-footer-actions {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .compare-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        font-size: 12px;
        border: 1px solid #d9d9d9;
        border-radius: 16px;
        background: #fff;
        color: #666;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: #722ed1;
          color: #722ed1;
          background: #f9f0ff;
        }

        &.active {
          border-color: #722ed1;
          background: #722ed1;
          color: #fff;

          &:hover {
            background: #9254de;
            border-color: #9254de;
          }
        }

        .compare-icon {
          font-size: 12px;
        }

        .compare-text {
          font-weight: 500;
        }
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
