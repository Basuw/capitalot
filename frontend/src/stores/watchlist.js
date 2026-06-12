import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'

const CACHE_TTL = 3 * 60 * 1000 // 3 minutes

export const useWatchlistStore = defineStore('watchlist', () => {
  const watchlists = ref([])
  const watchlist = ref([])
  const loading = ref(false)
  const error = ref(null)

  // --- Cache timestamps ---
  const watchlistsListFetchedAt = ref(null)       // timestamp for the watchlists list
  const watchlistItemsFetchedAt = ref({})          // { [watchlistId]: timestamp }
  const watchlistItemsCache = ref({})              // { [watchlistId]: items[] }

  function isFresh(timestamp) {
    return timestamp != null && (Date.now() - timestamp) < CACHE_TTL
  }

  function invalidateWatchlist(watchlistId) {
    const id = Number(watchlistId)
    delete watchlistItemsFetchedAt.value[id]
    delete watchlistItemsCache.value[id]
  }

  function invalidateWatchlistsList() {
    watchlistsListFetchedAt.value = null
  }

  async function fetchWatchlists(force = false) {
    if (!force && isFresh(watchlistsListFetchedAt.value) && watchlists.value.length > 0) {
      return watchlists.value
    }
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/watchlists')
      watchlists.value = response.data
      watchlistsListFetchedAt.value = Date.now()
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

  async function fetchWatchlistItems(watchlistId, force = false) {
    const id = Number(watchlistId)
    if (!force && isFresh(watchlistItemsFetchedAt.value[id]) && watchlistItemsCache.value[id]) {
      watchlist.value = watchlistItemsCache.value[id] // keep watchlist ref in sync
      return watchlistItemsCache.value[id]
    }
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/watchlists/${watchlistId}/items`)
      watchlistItemsCache.value[id] = response.data
      watchlistItemsFetchedAt.value[id] = Date.now()
      watchlist.value = response.data // keep watchlist ref in sync for view compatibility
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
      watchlistsListFetchedAt.value = null // list changed
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
      invalidateWatchlist(watchlistId) // items changed
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
      const item = watchlist.value.find(i => i.id === id)
      if (item?.watchlistId) invalidateWatchlist(item.watchlistId)
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
      // Update in items cache too if present
      const wlId = response.data?.watchlistId
      if (wlId && watchlistItemsCache.value[wlId]) {
        const idx = watchlistItemsCache.value[wlId].findIndex(i => i.id === id)
        if (idx !== -1) watchlistItemsCache.value[wlId][idx] = response.data
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
      invalidateWatchlist(id)
      watchlistsListFetchedAt.value = null
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
    deleteWatchlist,
    invalidateWatchlist,
    invalidateWatchlistsList
  }
})
