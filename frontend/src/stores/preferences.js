import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'
import { useAuthStore } from './auth'
import { formatPrice, formatPriceShort, getCurrencySymbol } from '../services/currency'

export const usePreferencesStore = defineStore('preferences', () => {
  const preferences = ref({
    showAnnualizedReturn: false,
    showBenchmarkComparison: false,
    showBestWorstDay: false,
    showStartPriceLine: false,
    showPerformanceBadge: true,
    showDetailedMetrics: true,
    benchmarkSymbol: 'SPY',
    currency: 'USD'
  })

  const loading = ref(false)
  const error = ref(null)

  async function loadPreferences() {
    const authStore = useAuthStore()
    if (!authStore.token) return

    loading.value = true
    error.value = null

    try {
      const response = await api.get('/user/preferences')
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
      const response = await api.put('/user/preferences', updates)
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
     updatePreferences,
     formatPrice: (price) => formatPrice(price, preferences.value.currency),
     formatPriceShort: (price) => formatPriceShort(price, preferences.value.currency),
     getCurrencySymbol: () => getCurrencySymbol(preferences.value.currency)
   }
})
