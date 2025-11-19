import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'

export const useWatchlistStore = defineStore('watchlist', () => {
  const watchlist = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchWatchlist() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/watchlists/items')
      watchlist.value = response.data
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch watchlist'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function addToWatchlist(stockData) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/watchlists/items', stockData)
      watchlist.value.push(response.data)
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to add to watchlist'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function removeFromWatchlist(id) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/watchlists/items/${id}`)
      watchlist.value = watchlist.value.filter(item => item.id !== id)
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to remove from watchlist'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateWatchlistItem(id, data) {
    loading.value = true
    error.value = null
    try {
      const response = await api.put(`/watchlists/items/${id}`, data)
      const index = watchlist.value.findIndex(item => item.id === id)
      if (index !== -1) {
        watchlist.value[index] = response.data
      }
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to update watchlist item'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    watchlist,
    loading,
    error,
    fetchWatchlist,
    addToWatchlist,
    removeFromWatchlist,
    updateWatchlistItem
  }
})
