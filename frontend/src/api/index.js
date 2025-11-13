import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 300000, // 5分钟超时（适用于大规模合同分解）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    // 处理超时错误
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage({
        type: 'warning',
        message: '后台服务超时未响应，处理可能仍在进行中。请稍后刷新页面查看结果。',
        duration: 8000,
        showClose: true
      })
    } 
    // 处理网络错误
    else if (error.message === 'Network Error') {
      ElMessage({
        type: 'error',
        message: '网络连接失败，请检查网络后重试',
        duration: 5000,
        showClose: true
      })
    }
    // 处理401未授权
    else if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.error('登录已过期，请重新登录')
      // 跳转到登录页
      window.location.href = '/login'
    } 
    // 处理500服务器错误
    else if (error.response?.status >= 500) {
      ElMessage({
        type: 'error',
        message: '服务器错误，请稍后重试',
        duration: 5000,
        showClose: true
      })
    } 
    // 处理其他错误
    else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } 
    else {
      ElMessage.error('请求失败')
    }
    return Promise.reject(error)
  }
)

export default api
