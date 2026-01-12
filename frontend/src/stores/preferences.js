import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { useAuthStore } from './auth'

export const usePreferencesStore = defineStore('preferences', () => {
  const preferences = ref({
    showAnnualizedReturn: false,
    showBenchmarkComparison: false,
    showBestWorstDay: false,
    showStartPriceLine: false,
    showPerformanceBadge: true,
    showDetailedMetrics: true,
    benchmarkSymbol: 'SPY'
  })

  const loading = ref(false)
  const error = ref(null)

  async function loadPreferences() {
    const authStore = useAuthStore()
    if (!authStore.token) return

    loading.value = true
    error.value = null

    try {
      const response = await axios.get('/api/user/preferences', {
        headers: {
          'Authorization': `Bearer ${authStore.token}`
        }
      })
      preferences.value = response.data
    } catch (err) {
      console.error('Failed to load preferences:', err)
      error.value = err.response?.data?.message || 'Failed to load preferences'
      // Keep default values on error
    } finally {
      loading.value = false
    }
  }

  async function updatePreferences(updates) {
    const authStore = useAuthStore()
    if (!authStore.token) return

    loading.value = true
    error.value = null

    try {
      const response = await axios.put('/api/user/preferences', updates, {
        headers: {
          'Authorization': `Bearer ${authStore.token}`
        }
      })
      preferences.value = response.data
      return true
    } catch (err) {
      console.error('Failed to update preferences:', err)
      error.value = err.response?.data?.message || 'Failed to update preferences'
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    preferences,
    loading,
    error,
    loadPreferences,
    updatePreferences
  }
})
