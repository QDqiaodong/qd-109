import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register } from '@/api'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)

  const init = () => {
    const saved = localStorage.getItem('userInfo')
    if (saved) {
      userInfo.value = JSON.parse(saved)
    }
  }

  const doLogin = async (username, password) => {
    const data = await login({ username, password })
    userInfo.value = data
    localStorage.setItem('userInfo', JSON.stringify(data))
    return data
  }

  const doRegister = async (username, password, nickname) => {
    const data = await register({ username, password, nickname })
    userInfo.value = data
    localStorage.setItem('userInfo', JSON.stringify(data))
    return data
  }

  const logout = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  return { userInfo, init, doLogin, doRegister, logout }
})
