import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const authChecked = ref(false) // 标记是否已经验证过token
  
  const isLoggedIn = computed(() => !!token.value)
  
  // 检查用户是否有指定权限
  const hasPermission = (permission) => {
    if (!user.value || !user.value.permissions) {
      return false
    }
    return user.value.permissions.includes(permission)
  }
  
  // 检查用户是否有任一指定权限
  const hasAnyPermission = (permissions) => {
    if (!user.value || !user.value.permissions) {
      return false
    }
    return permissions.some(permission => user.value.permissions.includes(permission))
  }
  
  const login = async (username, password) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      })
      
      const data = await response.json()
      
      if (response.ok && data.success) {
        token.value = data.token
        user.value = data.user
        authChecked.value = true
        localStorage.setItem('token', data.token)
        return true
      } else {
        return false
      }
    } catch (error) {
      console.error('Login error:', error)
      return false
    }
  }
  
  const logout = () => {
    user.value = null
    token.value = ''
    authChecked.value = false
    localStorage.removeItem('token')
  }
  
  const checkAuth = async () => {
    if (!token.value) {
      authChecked.value = true
      return false
    }
    
    // 如果已经验证过且用户信息存在，直接返回true
    if (authChecked.value && user.value) {
      return true
    }
    
    try {
      const response = await fetch('/api/auth/me', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.value}`
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        user.value = data
        authChecked.value = true
        return true
      } else {
        // Token无效，清除
        logout()
        return false
      }
    } catch (error) {
      console.error('Auth check error:', error)
      logout()
      return false
    }
  }
  
  const initUser = async () => {
    if (token.value) {
      // 验证token有效性并获取用户信息
      await checkAuth()
    } else {
      authChecked.value = true
    }
  }
  
  return {
    user,
    token,
    isLoggedIn,
    authChecked,
    hasPermission,
    hasAnyPermission,
    login,
    logout,
    checkAuth,
    initUser
  }
})
