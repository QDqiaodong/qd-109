import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const getUserId = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (!userInfo) return null
    const user = JSON.parse(userInfo)
    if (user && user.userId && typeof user.userId === 'number' && user.userId > 0) {
      return user.userId
    }
    return null
  } catch (e) {
    console.warn('Failed to parse userInfo from localStorage:', e)
    return null
  }
}

request.interceptors.request.use(config => {
  const userId = getUserId()
  if (userId) {
    config.headers['X-User-Id'] = userId
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        try {
          localStorage.removeItem('userInfo')
        } catch (e) {}
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.response?.data?.error || error.message || '网络错误'

    if (status === 401) {
      try {
        localStorage.removeItem('userInfo')
      } catch (e) {}
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
