import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'

export const usePortfolioStore = defineStore('portfolio', () => {
  const portfolios = ref([])
  const currentPortfolio = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchPortfolios() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/portfolios')
      portfolios.value = response.data
      return response.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Failed to fetch portfolios'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchPortfolioById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/portfolios/${id}`)
      currentPortfolio.value = response.data
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
      if (currentPortfolio.value?.id === portfolioId) {
        await fetchPortfolioById(portfolioId)
      }
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
      await api.delete(`/portfolios/${portfolioId}/stocks/${stockId}`)
      if (currentPortfolio.value?.id === portfolioId) {
        await fetchPortfolioById(portfolioId)
      }
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
    removeStock
  }
})
