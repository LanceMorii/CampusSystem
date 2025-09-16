import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isLoading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const userName = computed(() => userInfo.value?.username || '')
  const studentId = computed(() => userInfo.value?.studentId || '')

  // 登录
  const login = async (loginData) => {
    isLoading.value = true
    try {
      const response = await api.post('/auth/login', loginData)
      const { token: newToken, userInfo: newUserInfo } = response.data.data
      
      // 保存到状态和本地存储
      token.value = newToken
      userInfo.value = newUserInfo
      localStorage.setItem('token', newToken)
      localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
      
      // 设置API默认token
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      return response.data
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 注册
  const register = async (registerData) => {
    isLoading.value = true
    try {
      const response = await api.post('/auth/register', registerData)
      return response.data
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    delete api.defaults.headers.common['Authorization']
  }

  // 获取用户资料
  const getUserProfile = async () => {
    try {
      const response = await api.get('/auth/profile')
      userInfo.value = response.data.data
      localStorage.setItem('userInfo', JSON.stringify(response.data.data))
      return response.data
    } catch (error) {
      throw error
    }
  }

  // 更新用户资料
  const updateProfile = async (profileData) => {
    try {
      const response = await api.put('/auth/profile', profileData)
      userInfo.value = response.data.data
      localStorage.setItem('userInfo', JSON.stringify(response.data.data))
      return response.data
    } catch (error) {
      throw error
    }
  }

  // 初始化（设置token到API headers）
  const init = () => {
    if (token.value) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    }
  }

  return {
    // 状态
    token,
    userInfo,
    isLoading,
    
    // 计算属性
    isLoggedIn,
    isAdmin,
    userName,
    studentId,
    
    // 方法
    login,
    register,
    logout,
    getUserProfile,
    updateProfile,
    init
  }
})