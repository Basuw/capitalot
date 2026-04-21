import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useStockSearchStore = defineStore('stockSearch', () => {
  const searchQuery = ref('')
  const searchResults = ref([])
  const selectedSector = ref('')
  const selectedIndustry = ref('')
  const selectedRisk = ref('')
  const sortField = ref('symbol')
  const sortDirection = ref('asc')
  const currentPage = ref(1)
  const lastRefresh = ref('')
  const hasLoaded = ref(false)

  function reset() {
    searchQuery.value = ''
    searchResults.value = []
    selectedSector.value = ''
    selectedIndustry.value = ''
    selectedRisk.value = ''
    sortField.value = 'symbol'
    sortDirection.value = 'asc'
    currentPage.value = 1
    lastRefresh.value = ''
    hasLoaded.value = false
  }

  return {
    searchQuery,
    searchResults,
    selectedSector,
    selectedIndustry,
    selectedRisk,
    sortField,
    sortDirection,
    currentPage,
    lastRefresh,
    hasLoaded,
    reset
  }
})
