import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'compareList'
const EVENT_KEY = 'compareListUpdated'
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
  const compareList = ref(safeParseCompareList())
  const updateTick = ref(0)

  const count = computed(() => {
    updateTick.value
    return compareList.value.length
  })
  const isEmpty = computed(() => {
    updateTick.value
    return compareList.value.length === 0
  })
  const isMax = computed(() => {
    updateTick.value
    return compareList.value.length >= MAX_COMPARE_ITEMS
  })

  let storageListenerAttached = false
  const listeners = new Set()

  const notifyUpdated = () => {
    updateTick.value++
    try {
      window.dispatchEvent(new CustomEvent(EVENT_KEY, { detail: { list: compareList.value } }))
    } catch (e) {}
    listeners.forEach(fn => {
      try { fn(compareList.value) } catch (e) {}
    })
  }

  const subscribe = (fn) => {
    if (typeof fn === 'function') {
      listeners.add(fn)
    }
    return () => listeners.delete(fn)
  }

  const init = () => {
    compareList.value = safeParseCompareList()
    notifyUpdated()

    if (!storageListenerAttached) {
      try {
        window.addEventListener('storage', handleStorageChange)
        window.addEventListener(EVENT_KEY, handleCustomEvent)
        storageListenerAttached = true
      } catch (e) {}
    }
  }

  const handleStorageChange = (e) => {
    if (e.key === STORAGE_KEY) {
      if (e.newValue) {
        try {
          const data = JSON.parse(e.newValue)
          if (Array.isArray(data)) {
            compareList.value = data.filter(isValidPost)
            notifyUpdated()
          }
        } catch (err) {
          compareList.value = []
          notifyUpdated()
        }
      } else {
        compareList.value = []
        notifyUpdated()
      }
    }
  }

  const handleCustomEvent = (e) => {
    try {
      if (e.detail && Array.isArray(e.detail.list)) {
        compareList.value = e.detail.list.filter(isValidPost)
      }
    } catch (err) {}
  }

  const saveToStorage = () => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(compareList.value))
    } catch (e) {
      console.warn('Failed to save compare list to storage:', e)
    }
  }

  const isInCompare = (postId) => {
    updateTick.value
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
    notifyUpdated()
    return true
  }

  const removeFromCompare = (postId) => {
    const index = compareList.value.findIndex(item => item.id === postId)
    if (index > -1) {
      compareList.value.splice(index, 1)
      saveToStorage()
      notifyUpdated()
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
    notifyUpdated()
  }

  const syncFromStorage = () => {
    compareList.value = safeParseCompareList()
    notifyUpdated()
  }

  return {
    compareList,
    count,
    isEmpty,
    isMax,
    MAX_COMPARE_ITEMS,
    init,
    syncFromStorage,
    subscribe,
    isInCompare,
    addToCompare,
    removeFromCompare,
    toggleCompare,
    clearCompare
  }
})
