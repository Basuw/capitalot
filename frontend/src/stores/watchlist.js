import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'

export const useWatchlistStore = defineStore('watchlist', () => {
  const watchlists = ref([])
  const watchlist = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchWatchlists() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/watchlists')
      watchlists.value = response.data
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch watchlists'
      throw e
    } finally {
      loading.value = false
    }
  }

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

  async function fetchWatchlistItems(watchlistId) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/watchlists/${watchlistId}/items`)
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch watchlist items'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createWatchlist(data) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/watchlists', data)
      watchlists.value.push(response.data)
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to create watchlist'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function addToWatchlist(watchlistId, stockData) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post(`/watchlists/${watchlistId}/items`, stockData)
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

  async function deleteWatchlist(id) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/watchlists/${id}`)
      watchlists.value = watchlists.value.filter(w => w.id !== id)
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to delete watchlist'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    watchlists,
    watchlist,
    loading,
    error,
    fetchWatchlists,
    fetchWatchlist,
    fetchWatchlistItems,
    createWatchlist,
    addToWatchlist,
    removeFromWatchlist,
    updateWatchlistItem,
    deleteWatchlist
  }
})
