<template>
  <div>
    <Navbar />
    
    <div class="stock-search-container">
      <div class="header-section">
        <h1>Actions</h1>
        <div class="search-box">
          <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher des actions par symbole ou nom..."
            @input="handleSearch"
          />
          <div v-if="searching" class="search-status">
            <svg class="spinner" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
          </div>
        </div>
      </div>

      <div class="filters-section">
        <div class="filter-group">
          <label>Secteur</label>
          <select v-model="selectedSector">
            <option value="">Tous les secteurs</option>
            <option v-for="sector in uniqueSectors" :key="sector" :value="sector">{{ sector }}</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label>Industrie</label>
          <select v-model="selectedIndustry">
            <option value="">Toutes les industries</option>
            <option v-for="industry in uniqueIndustries" :key="industry" :value="industry">{{ industry }}</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label>Risque</label>
          <select v-model="selectedRisk">
            <option value="">Tous</option>
            <option value="low">Faible (0-3)</option>
            <option value="medium">Moyen (4-6)</option>
            <option value="high">Élevé (7-10)</option>
          </select>
        </div>
      </div>

      <div v-if="paginatedResults.length" class="results-section">
        <h2>{{ searchQuery ? `Résultats (${filteredResults.length})` : `Actions (${filteredResults.length})` }}</h2>
        <div class="table-container">
          <table class="stocks-table">
            <thead>
              <tr>
                <th @click="sortBy('symbol')" class="sortable">
                  Symbole
                  <span v-if="sortField === 'symbol'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
                <th @click="sortBy('name')" class="sortable">
                  Nom
                  <span v-if="sortField === 'name'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
                <th @click="sortBy('currentPrice')" class="sortable right-align">
                  Prix
                  <span v-if="sortField === 'currentPrice'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
                <th @click="sortBy('dailyChangePercentage')" class="sortable right-align">
                  Changement
                  <span v-if="sortField === 'dailyChangePercentage'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
                <th @click="sortBy('sector')" class="sortable">
                  Secteur
                  <span v-if="sortField === 'sector'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
                <th @click="sortBy('risk')" class="sortable right-align">
                  Risque
                  <span v-if="sortField === 'risk'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
                <th @click="sortBy('marketScore')" class="sortable right-align">
                  Score
                  <span v-if="sortField === 'marketScore'">{{ sortDirection === 'asc' ? '↑' : '↓' }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="stock in paginatedResults" 
                :key="stock.symbol" 
                class="stock-row"
                @click="navigateToStock(stock.symbol)"
              >
                <td class="symbol-cell">
                  <div class="stock-icon-small">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
                  </div>
                  <strong>{{ stock.symbol }}</strong>
                </td>
                <td>{{ stock.name }}</td>
                <td class="right-align">${{ formatNumber(stock.currentPrice) }}</td>
                <td class="right-align">
                  <span :class="getDailyChangeClass(stock.dailyChangePercentage)">
                    {{ formatChange(stock.dailyChange) }}
                    ({{ formatPercent(stock.dailyChangePercentage) }}%)
                  </span>
                </td>
                <td>{{ stock.sector || '-' }}</td>
                <td class="right-align">
                  <span :class="getRiskClass(stock.risk)">{{ stock.risk?.toFixed(1) || '-' }}</span>
                </td>
                <td class="right-align">{{ stock.marketScore || '-' }}/10</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button 
            @click="currentPage--" 
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            Précédent
          </button>
          <span class="page-info">Page {{ currentPage }} sur {{ totalPages }}</span>
          <button 
            @click="currentPage++" 
            :disabled="currentPage === totalPages"
            class="pagination-btn"
          >
            Suivant
          </button>
        </div>
      </div>

      <div v-else-if="searchQuery && !searching" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
        <p>Aucune action trouvée pour "{{ searchQuery }}"</p>
      </div>

      <div v-else-if="!searching && !filteredResults.length" class="loading-state">
        <svg class="spinner" xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
        <p>Chargement...</p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import api from '../services/api'

const router = useRouter()

const searchQuery = ref('')
const searchResults = ref([])
const searching = ref(false)
const selectedSector = ref('')
const selectedIndustry = ref('')
const selectedRisk = ref('')
const sortField = ref('symbol')
const sortDirection = ref('asc')
const currentPage = ref(1)
const itemsPerPage = 20

let searchTimeout = null

const uniqueSectors = computed(() => {
  const sectors = searchResults.value.map(s => s.sector).filter(Boolean)
  return [...new Set(sectors)].sort()
})

const uniqueIndustries = computed(() => {
  const industries = searchResults.value.map(s => s.industry).filter(Boolean)
  return [...new Set(industries)].sort()
})

const filteredResults = computed(() => {
  let results = searchResults.value

  if (selectedSector.value) {
    results = results.filter(s => s.sector === selectedSector.value)
  }

  if (selectedIndustry.value) {
    results = results.filter(s => s.industry === selectedIndustry.value)
  }

  if (selectedRisk.value) {
    results = results.filter(s => {
      const risk = s.risk || 0
      if (selectedRisk.value === 'low') return risk <= 3
      if (selectedRisk.value === 'medium') return risk > 3 && risk <= 6
      if (selectedRisk.value === 'high') return risk > 6
      return true
    })
  }

  results = [...results].sort((a, b) => {
    let aVal = a[sortField.value]
    let bVal = b[sortField.value]

    if (aVal == null) aVal = sortDirection.value === 'asc' ? Infinity : -Infinity
    if (bVal == null) bVal = sortDirection.value === 'asc' ? Infinity : -Infinity

    if (typeof aVal === 'string') {
      return sortDirection.value === 'asc' 
        ? aVal.localeCompare(bVal) 
        : bVal.localeCompare(aVal)
    }

    return sortDirection.value === 'asc' ? aVal - bVal : bVal - aVal
  })

  return results
})

const totalPages = computed(() => {
  return Math.ceil(filteredResults.value.length / itemsPerPage)
})

const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredResults.value.slice(start, end)
})

watch([selectedSector, selectedIndustry, selectedRisk], () => {
  currentPage.value = 1
})

onMounted(async () => {
  await loadAllStocks()
})

async function loadAllStocks() {
  searching.value = true
  try {
    const response = await api.get('/stocks/search')
    searchResults.value = response.data
  } catch (error) {
    console.error('Failed to load stocks:', error)
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

function handleSearch() {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  if (!searchQuery.value.trim()) {
    loadAllStocks()
    return
  }

  searchTimeout = setTimeout(async () => {
    searching.value = true
    try {
      const response = await api.get(`/stocks/search?query=${encodeURIComponent(searchQuery.value)}`)
      searchResults.value = response.data
      currentPage.value = 1
    } catch (error) {
      console.error('Search failed:', error)
      searchResults.value = []
    } finally {
      searching.value = false
    }
  }, 500)
}

function sortBy(field) {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'asc'
  }
}

function navigateToStock(symbol) {
  router.push(`/stocks/${symbol}`)
}

function formatNumber(num) {
  return num?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

function formatChange(num) {
  if (!num) return '$0.00'
  const formatted = Math.abs(num).toFixed(2)
  return num >= 0 ? `+$${formatted}` : `-$${formatted}`
}

function formatPercent(num) {
  if (!num && num !== 0) return '0.00'
  return num >= 0 ? `+${num.toFixed(2)}` : num.toFixed(2)
}

function getDailyChangeClass(changePercent) {
  if (!changePercent) return ''
  return changePercent >= 0 ? 'positive-change' : 'negative-change'
}

function getRiskClass(risk) {
  if (!risk) return ''
  if (risk <= 3) return 'risk-low'
  if (risk <= 6) return 'risk-medium'
  return 'risk-high'
}
</script>

<style scoped>
.stock-search-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

.header-section {
  margin-bottom: 2rem;
}

h1 {
  margin: 0 0 2rem 0;
  color: #333;
  font-size: 2.5rem;
}

h2 {
  color: #333;
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
}

.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

input[type="text"] {
  width: 100%;
  padding: 1.25rem 1.25rem 1.25rem 3.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 16px;
  font-size: 1.1rem;
  transition: all 0.3s;
  box-sizing: border-box;
  background: white;
}

input[type="text"]:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.search-status {
  position: absolute;
  right: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
}

.spinner {
  animation: spin 1s linear infinite;
  color: #667eea;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.filters-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-group {
  flex: 1;
  min-width: 200px;
}

.filter-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 600;
  font-size: 0.9rem;
}

.filter-group select {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s;
}

.filter-group select:focus {
  outline: none;
  border-color: #667eea;
}

.results-section {
  margin-top: 2rem;
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.stocks-table {
  width: 100%;
  border-collapse: collapse;
}

.stocks-table thead {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stocks-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
  transition: background 0.3s;
}

.stocks-table th.sortable:hover {
  background: rgba(255, 255, 255, 0.1);
}

.stocks-table th.right-align {
  text-align: right;
}

.stocks-table tbody tr {
  border-bottom: 1px solid #e0e0e0;
  cursor: pointer;
  transition: background 0.2s;
}

.stocks-table tbody tr:hover {
  background: #f8f9fa;
}

.stocks-table td {
  padding: 1rem;
}

.stocks-table td.right-align {
  text-align: right;
}

.symbol-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
  color: #333;
}

.stock-icon-small {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.positive-change {
  color: #10b981;
  font-weight: 600;
}

.negative-change {
  color: #ef4444;
  font-weight: 600;
}

.risk-low {
  color: #10b981;
  font-weight: 600;
}

.risk-medium {
  color: #f59e0b;
  font-weight: 600;
}

.risk-high {
  color: #ef4444;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
  margin-top: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-btn {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  font-weight: 600;
  color: #333;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  color: #666;
}

.empty-state svg {
  color: #ccc;
  margin-bottom: 1rem;
}

.empty-state p {
  font-size: 1.1rem;
  margin: 0;
}

.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-state svg {
  margin-bottom: 1rem;
}

.loading-state p {
  color: #666;
  font-size: 1.1rem;
}
</style>
