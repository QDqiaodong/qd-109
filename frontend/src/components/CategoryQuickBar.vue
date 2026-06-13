<template>
  <div class="category-quick-bar card">
    <div class="quick-bar-header">
      <span class="header-title">🔥 热门分类</span>
      <span class="header-tip">横向滑动查看更多</span>
    </div>
    <div class="quick-bar-scroll" ref="scrollRef" @mousedown="handleMouseDown" @mousemove="handleMouseMove" @mouseup="handleMouseUp" @mouseleave="handleMouseUp">
      <router-link
        to="/"
        class="quick-item"
        :class="{ active: !currentCategoryId }"
      >
        <span class="quick-icon">🏠</span>
        <span class="quick-name">全部</span>
      </router-link>
      <router-link
        v-for="cat in displayCategories"
        :key="cat.id"
        :to="`/category/${cat.id}`"
        class="quick-item"
        :class="{ active: currentCategoryId === cat.id }"
      >
        <span class="quick-icon">{{ cat.icon }}</span>
        <span class="quick-name">{{ cat.name }}</span>
        <span v-if="sortType === 'hot' && cat.hotScore > 0" class="hot-badge">
          {{ formatHotScore(cat.hotScore) }}
        </span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getHotCategories } from '@/api'

const props = defineProps({
  categoryId: {
    type: Number,
    default: null
  },
  sortType: {
    type: String,
    default: 'sort'
  }
})

const route = useRoute()
const scrollRef = ref(null)
const categories = ref([])
const hotCategories = ref([])

const isDragging = ref(false)
const startX = ref(0)
const scrollLeft = ref(0)

const currentCategoryId = computed(() => {
  if (props.categoryId !== null) {
    return props.categoryId
  }
  if (route.name === 'category' && route.params.id) {
    return Number(route.params.id)
  }
  return null
})

const displayCategories = computed(() => {
  if (props.sortType === 'hot') {
    return hotCategories.value
  }
  return [...categories.value].sort((a, b) => (a.sort || 0) - (b.sort || 0))
})

const formatHotScore = (score) => {
  if (score >= 10000) {
    return (score / 10000).toFixed(1) + 'w'
  }
  if (score >= 1000) {
    return (score / 1000).toFixed(1) + 'k'
  }
  return score
}

const loadCategories = async () => {
  if (props.sortType === 'hot') {
    hotCategories.value = await getHotCategories()
  } else {
    categories.value = await getCategories()
  }
}

const scrollToActive = async () => {
  await nextTick()
  if (!scrollRef.value) return
  const activeEl = scrollRef.value.querySelector('.quick-item.active')
  if (activeEl) {
    const container = scrollRef.value
    const scrollLeftPos = activeEl.offsetLeft - container.offsetWidth / 2 + activeEl.offsetWidth / 2
    container.scrollTo({
      left: Math.max(0, scrollLeftPos),
      behavior: 'smooth'
    })
  }
}

const handleMouseDown = (e) => {
  isDragging.value = true
  startX.value = e.pageX - scrollRef.value.offsetLeft
  scrollLeft.value = scrollRef.value.scrollLeft
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault()
  const x = e.pageX - scrollRef.value.offsetLeft
  const walk = (x - startX.value) * 1.5
  scrollRef.value.scrollLeft = scrollLeft.value - walk
}

const handleMouseUp = () => {
  isDragging.value = false
}

watch(currentCategoryId, () => {
  scrollToActive()
})

onMounted(async () => {
  await loadCategories()
  scrollToActive()
})
</script>

<style lang="scss" scoped>
.category-quick-bar {
  margin-bottom: 20px;
  padding: 16px 20px;

  .quick-bar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;

    .header-title {
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }

    .header-tip {
      font-size: 12px;
      color: #999;
    }
  }

  .quick-bar-scroll {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    scroll-behavior: smooth;
    -webkit-overflow-scrolling: touch;
    cursor: grab;
    user-select: none;

    &:active {
      cursor: grabbing;
    }

    &::-webkit-scrollbar {
      display: none;
    }
  }

  .quick-item {
    position: relative;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 14px 18px;
    border-radius: 12px;
    background: linear-gradient(145deg, #f8f9fa 0%, #f0f2f5 100%);
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    min-width: 76px;
    text-decoration: none;
    color: inherit;
    border: 1px solid transparent;

    &:hover {
      background: linear-gradient(145deg, #e6f7ff 0%, #bae7ff 100%);
      transform: translateY(-3px);
      box-shadow: 0 6px 16px rgba(24, 144, 255, 0.15);
      border-color: #91d5ff;
    }

    &.active {
      background: linear-gradient(135deg, #1890ff 0%, #40a9ff 50%, #69c0ff 100%);
      color: #fff;
      box-shadow: 0 6px 20px rgba(24, 144, 255, 0.35);
      transform: translateY(-2px);
      border-color: transparent;

      .quick-icon {
        transform: scale(1.15);
      }

      .hot-badge {
        background: rgba(255, 255, 255, 0.25);
        color: #fff;
      }
    }

    .quick-icon {
      font-size: 26px;
      transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .quick-name {
      font-size: 13px;
      font-weight: 500;
      white-space: nowrap;
    }

    .hot-badge {
      position: absolute;
      top: 6px;
      right: 6px;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 10px;
      background: linear-gradient(135deg, #ff7a45 0%, #ff4d4f 100%);
      color: #fff;
      font-weight: 500;
      line-height: 1.2;
    }
  }
}
</style>
