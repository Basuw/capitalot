import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'

export const useStatsStore = defineStore('stats', () => {
  const stats = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchStats() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/stats')
      stats.value = response.data
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch stats'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    stats,
    loading,
    error,
    fetchStats
  }
})
