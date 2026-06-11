import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'compareList'
const MAX_COMPARE_ITEMS = 4

const safeParseCompareList = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (!saved) return []
    const data = JSON.parse(saved)
    if (Array.isArray(data)) {
      return data.filter(item => item && item.id && typeof item.id === 'number')
    }
    localStorage.removeItem(STORAGE_KEY)
    return []
  } catch (e) {
    console.warn('Failed to parse compare list from storage:', e)
    localStorage.removeItem(STORAGE_KEY)
    return []
  }
}

const isValidPost = (post) => {
  return post && post.id && typeof post.id === 'number'
}

export const useCompareStore = defineStore('compare', () => {
  const compareList = ref([])

  const count = computed(() => compareList.value.length)
  const isEmpty = computed(() => compareList.value.length === 0)
  const isMax = computed(() => compareList.value.length >= MAX_COMPARE_ITEMS)

  const init = () => {
    compareList.value = safeParseCompareList()

    try {
      window.addEventListener('storage', handleStorageChange)
    } catch (e) {}
  }

  const handleStorageChange = (e) => {
    if (e.key === STORAGE_KEY) {
      if (e.newValue) {
        try {
          const data = JSON.parse(e.newValue)
          if (Array.isArray(data)) {
            compareList.value = data.filter(isValidPost)
          }
        } catch (err) {
          compareList.value = []
        }
      } else {
        compareList.value = []
      }
    }
  }

  const saveToStorage = () => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(compareList.value))
    } catch (e) {
      console.warn('Failed to save compare list to storage:', e)
    }
  }

  const isInCompare = (postId) => {
    return compareList.value.some(item => item.id === postId)
  }

  const addToCompare = (post) => {
    if (!isValidPost(post)) return false
    if (isInCompare(post.id)) return false
    if (isMax.value) return false

    const minimalPost = {
      id: post.id,
      title: post.title,
      content: post.content,
      type: post.type,
      categoryName: post.categoryName,
      categoryId: post.categoryId,
      nickname: post.nickname,
      avatar: post.avatar,
      viewCount: post.viewCount,
      commentCount: post.commentCount,
      likeCount: post.likeCount,
      createTime: post.createTime,
      accessoryCards: post.accessoryCards || [],
      images: post.images ? (Array.isArray(post.images) ? post.images : post.images.split(',')) : []
    }

    compareList.value.push(minimalPost)
    saveToStorage()
    return true
  }

  const removeFromCompare = (postId) => {
    const index = compareList.value.findIndex(item => item.id === postId)
    if (index > -1) {
      compareList.value.splice(index, 1)
      saveToStorage()
      return true
    }
    return false
  }

  const toggleCompare = (post) => {
    if (isInCompare(post.id)) {
      removeFromCompare(post.id)
      return false
    } else {
      return addToCompare(post)
    }
  }

  const clearCompare = () => {
    compareList.value = []
    saveToStorage()
  }

  return {
    compareList,
    count,
    isEmpty,
    isMax,
    MAX_COMPARE_ITEMS,
    init,
    isInCompare,
    addToCompare,
    removeFromCompare,
    toggleCompare,
    clearCompare
  }
})
