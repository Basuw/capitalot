import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const userStr = localStorage.getItem('user')
  const user = ref(userStr && userStr !== 'undefined' && userStr !== 'null' ? JSON.parse(userStr) : null)

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(authToken, userData) {
    token.value = authToken
    user.value = userData
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function clearAuth() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    isAuthenticated,
    setAuth,
    clearAuth
  }
})
