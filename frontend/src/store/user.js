import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register } from '@/api'

const STORAGE_KEY = 'userInfo'

const isValidUserInfo = (data) => {
  if (!data || typeof data !== 'object') return false
  if (!data.userId || typeof data.userId !== 'number' || data.userId <= 0) return false
  if (!data.nickname) return false
  return true
}

const safeParseUserInfo = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (!saved) return null
    const data = JSON.parse(saved)
    if (isValidUserInfo(data)) {
      return data
    }
    localStorage.removeItem(STORAGE_KEY)
    return null
  } catch (e) {
    console.warn('Failed to parse user info from storage:', e)
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(safeParseUserInfo())

  const isLoggedIn = computed(() => {
    return userInfo.value && isValidUserInfo(userInfo.value)
  })

  let storageListenerAttached = false

  const init = () => {
    userInfo.value = safeParseUserInfo()

    if (!storageListenerAttached) {
      try {
        window.addEventListener('storage', handleStorageChange)
        storageListenerAttached = true
      } catch (e) {}
    }
  }

  const handleStorageChange = (e) => {
    if (e.key === STORAGE_KEY) {
      if (e.newValue) {
        try {
          const data = JSON.parse(e.newValue)
          if (isValidUserInfo(data)) {
            userInfo.value = data
          } else {
            userInfo.value = null
          }
        } catch (err) {
          userInfo.value = null
        }
      } else {
        userInfo.value = null
      }
    }
  }

  const doLogin = async (username, password) => {
    const data = await login({ username, password })
    if (!isValidUserInfo(data)) {
      throw new Error('登录信息无效')
    }
    userInfo.value = data
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    } catch (e) {
      console.warn('Failed to save user info to storage:', e)
    }
    return data
  }

  const doRegister = async (username, password, nickname) => {
    const data = await register({ username, password, nickname })
    if (!isValidUserInfo(data)) {
      throw new Error('注册信息无效')
    }
    userInfo.value = data
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    } catch (e) {
      console.warn('Failed to save user info to storage:', e)
    }
    return data
  }

  const logout = () => {
    userInfo.value = null
    try {
      localStorage.removeItem(STORAGE_KEY)
    } catch (e) {
      console.warn('Failed to remove user info from storage:', e)
    }
  }

  const checkLogin = () => {
    const stored = safeParseUserInfo()
    if (stored) {
      userInfo.value = stored
      return true
    }
    return false
  }

  return { userInfo, isLoggedIn, init, doLogin, doRegister, logout, checkLogin }
})
