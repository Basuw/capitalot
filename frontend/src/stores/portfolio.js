import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'

const CACHE_TTL = 2 * 60 * 1000 // 2 minutes

export const usePortfolioStore = defineStore('portfolio', () => {
  const portfolios = ref([])
  const currentPortfolio = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // --- Cache timestamps ---
  const portfoliosListFetchedAt = ref(null)          // timestamp for the list
  const portfolioDetailFetchedAt = ref({})           // { [id]: timestamp } for each detail

  function isFresh(timestamp) {
    return timestamp != null && (Date.now() - timestamp) < CACHE_TTL
  }

  // Invalidate a specific portfolio detail + the list (totalValue may have changed)
  function invalidatePortfolio(id) {
    const numId = Number(id)
    delete portfolioDetailFetchedAt.value[numId]
    portfoliosListFetchedAt.value = null
  }

  // Invalidate only the list (e.g. after create/delete)
  function invalidatePortfoliosList() {
    portfoliosListFetchedAt.value = null
  }

  async function fetchPortfolios(force = false) {
    if (!force && isFresh(portfoliosListFetchedAt.value) && portfolios.value.length > 0) {
      return portfolios.value
    }
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/portfolios')
      portfolios.value = response.data
      portfoliosListFetchedAt.value = Date.now()
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch portfolios'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchPortfolioById(id, force = false) {
    const numId = Number(id)
    if (!force && isFresh(portfolioDetailFetchedAt.value[numId]) && currentPortfolio.value?.id === numId) {
      return currentPortfolio.value
    }
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/portfolios/${id}`)
      currentPortfolio.value = response.data
      portfolioDetailFetchedAt.value[numId] = Date.now()
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch portfolio'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createPortfolio(portfolioData) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/portfolios', portfolioData)
      portfolios.value.push(response.data)
      portfoliosListFetchedAt.value = null // list changed
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to create portfolio'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updatePortfolio(id, portfolioData) {
    loading.value = true
    error.value = null
    try {
      const response = await api.put(`/portfolios/${id}`, portfolioData)
      const index = portfolios.value.findIndex(p => p.id === id)
      if (index !== -1) {
        portfolios.value[index] = response.data
      }
      if (currentPortfolio.value?.id === id) {
        currentPortfolio.value = response.data
      }
      // Metadata changed (name/icon) — refresh cache timestamps
      portfolioDetailFetchedAt.value[Number(id)] = Date.now()
      portfoliosListFetchedAt.value = Date.now()
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to update portfolio'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deletePortfolio(id) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/portfolios/${id}`)
      portfolios.value = portfolios.value.filter(p => p.id !== id)
      if (currentPortfolio.value?.id === id) {
        currentPortfolio.value = null
      }
      delete portfolioDetailFetchedAt.value[Number(id)]
      portfoliosListFetchedAt.value = null
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to delete portfolio'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function addStock(portfolioId, stockData) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post(`/portfolios/${portfolioId}/stocks`, stockData)
      invalidatePortfolio(portfolioId) // force fresh detail + list on next fetch
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to add stock'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function removeStock(portfolioId, stockId) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/portfolios/stocks/${stockId}`)
      invalidatePortfolio(portfolioId) // force fresh detail + list on next fetch
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to remove stock'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    portfolios,
    currentPortfolio,
    loading,
    error,
    fetchPortfolios,
    fetchPortfolioById,
    createPortfolio,
    updatePortfolio,
    deletePortfolio,
    addStock,
    removeStock,
    invalidatePortfolio,
    invalidatePortfoliosList
  }
})
