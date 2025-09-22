import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  
  const isLoggedIn = computed(() => !!token.value)
  
  const login = async (username, password) => {
    try {
      // 这里应该调用实际的登录API
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      })
      
      if (response.ok) {
        const data = await response.json()
        token.value = data.token
        user.value = data.user
        localStorage.setItem('token', data.token)
        return true
      }
      return false
    } catch (error) {
      console.error('Login error:', error)
      return false
    }
  }
  
  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
  }
  
  const initUser = () => {
    if (token.value) {
      // 验证token有效性并获取用户信息
      // 这里可以调用API验证token
    }
  }
  
  return {
    user,
    token,
    isLoggedIn,
    login,
    logout,
    initUser
  }
})
