<template>
  <div>
    <Navbar />
    
    <div class="portfolio-detail-container">
      <div v-if="portfolioStore.loading && !portfolioStore.currentPortfolio" class="loading">
        Loading portfolio...
      </div>

      <div v-else-if="portfolioStore.currentPortfolio" class="portfolio-content">
        <div class="portfolio-header">
          <div class="portfolio-title">
            <button @click="$router.push('/portfolios')" class="btn-back">← Back</button>
            <span class="portfolio-icon">{{ portfolioStore.currentPortfolio.icon || '💼' }}</span>
            <h1>{{ portfolioStore.currentPortfolio.name }}</h1>
          </div>
          <div class="header-actions">
            <a v-if="portfolioStore.currentPortfolio.link" :href="portfolioStore.currentPortfolio.link" target="_blank" class="btn-link">
              🔗 View Link
            </a>
            <button @click="showEditModal = true" class="btn-secondary">
              ✏️ Edit Portfolio
            </button>
            <button @click="showAddStockModal = true" class="btn-primary">
              + Add Stock
            </button>
          </div>
        </div>

        <p v-if="portfolioStore.currentPortfolio.description" class="portfolio-description">
          {{ portfolioStore.currentPortfolio.description }}
        </p>
        
         <div class="portfolio-stats">
           <div class="stat-card">
             <div class="stat-label">Total Invested</div>
             <div class="stat-value">{{ preferencesStore.formatPrice(performanceStats?.totalInvested || 0) }}</div>
           </div>
           <div class="stat-card">
             <div class="stat-label">Current Value</div>
             <div class="stat-value">{{ preferencesStore.formatPrice(performanceStats?.currentValue || 0) }}</div>
           </div>
           <div class="stat-card">
             <div class="stat-label">Total Gain/Loss</div>
             <div class="stat-value" :class="(performanceStats?.totalGainLoss || 0) >= 0 ? 'positive' : 'negative'">
               {{ preferencesStore.formatPrice(performanceStats?.totalGainLoss || 0) }}
               ({{ formatPercent(performanceStats?.totalGainLossPercent || 0) }}%)
             </div>
           </div>
         </div>

        <div v-if="showChart" class="chart-section">
          <div class="chart-header">
            <h3>Portfolio Performance</h3>
            <div class="time-selector">
              <button
                v-for="range in timeRanges"
                :key="range"
                @click="selectedRange = range; fetchChartData()"
                :class="{ active: selectedRange === range }"
                class="time-btn"
              >
                {{ range }}
              </button>
            </div>
            <button @click="showChart = false" class="btn-icon-modern">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
          </div>
          <div v-if="loadingChart" class="loading-chart">Loading chart...</div>
          <PerformanceChart
            v-else-if="chartData.length"
            :data="chartData"
            label="Portfolio Value"
            :selectedRange="selectedRange === 'ALL' ? '5Y' : selectedRange"
          />
          <div v-else class="empty-chart">No chart data available</div>
        </div>

        <div v-else class="show-chart-section">
          <button @click="showChart = true; fetchChartData()" class="btn-secondary">
            📈 Show Performance Chart
          </button>
        </div>

        <div v-if="stocks.length" class="stocks-table">
          <table>
            <thead>
              <tr>
                <th>Symbol</th>
                <th>Name</th>
                <th>Type</th>
                <th>Shares</th>
                <th>Avg. Buy Price</th>
                <th>Current Price</th>
                <th>Total Value</th>
                <th>Gain/Loss</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stock in stocks" :key="stock.id" @click="openPurchaseHistory(stock)" class="clickable-row">
                 <td class="symbol">
                   <router-link :to="`/stocks/${stock.stock.symbol}`" class="stock-link" @click.stop>
                     {{ stock.stock.symbol }}
                   </router-link>
                 </td>
                 <td>{{ stock.stock.name }}</td>
                 <td>
                   <span class="badge">{{ stock.stock.type }}</span>
                 </td>
                 <td>{{ formatNumber(stock.quantity) }}</td>
                 <td>{{ preferencesStore.formatPrice(stock.purchasePrice) }}</td>
                 <td>{{ preferencesStore.formatPrice(stock.currentPrice) }}</td>
                 <td>{{ preferencesStore.formatPrice(stock.currentValue) }}</td>
                 <td :class="(stock.gainLoss || 0) >= 0 ? 'positive' : 'negative'">
                   {{ preferencesStore.formatPrice(stock.gainLoss) }}
                   ({{ formatPercent(stock.gainLossPercentage) }}%)
                 </td>
                <td>
                  <div class="action-buttons">
                    <button @click.stop="openActionModal(stock)" class="btn-icon-modern delete" title="Remove/Sell">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state">
          <p>No stocks in this portfolio yet.</p>
          <button @click="showAddStockModal = true" class="btn-primary">Add Your First Stock</button>
        </div>

        <Modal :show="showAddStockModal" title="Add Stock to Portfolio" @close="closeAddStockModal">
          <form @submit.prevent="addStock">
            <div class="form-group">
              <label for="searchStock">Search Stock</label>
              <input
                id="searchStock"
                v-model="searchQuery"
                type="text"
                placeholder="Search by symbol or name..."
                @input="searchStocks"
              />
            </div>

            <div v-if="searchResults.length > 0" class="search-results">
              <div 
                v-for="stock in searchResults" 
                :key="stock.symbol"
                class="search-result-item"
                :class="{ selected: selectedStock?.symbol === stock.symbol }"
                @click="selectStock(stock)"
              >
                <div class="stock-symbol-icon">{{ stock.symbol }}</div>
                <div class="stock-name">{{ stock.name }}</div>
              </div>
            </div>

             <div v-if="selectedStock" class="selected-stock-info">
               <h4>Selected: {{ selectedStock.symbol }} - {{ selectedStock.name }}</h4>
               <p>Current Price: {{ preferencesStore.formatPrice(selectedStock.currentPrice || 0) }}</p>
             </div>

            <div class="form-group">
              <label for="quantity">Quantity</label>
              <input
                id="quantity"
                v-model.number="stockForm.quantity"
                type="number"
                step="0.000001"
                min="0"
                required
                placeholder="0"
              />
            </div>

            <div class="form-group">
              <label>Price Option</label>
              <div class="radio-group">
                <label class="radio-option">
                  <input
                    type="radio"
                    v-model="priceOption"
                    value="manual"
                    @change="onPriceOptionChange"
                  />
                  <span>Enter price manually</span>
                </label>
                <label class="radio-option">
                  <input
                    type="radio"
                    v-model="priceOption"
                    value="date"
                    @change="onPriceOptionChange"
                  />
                  <span>Get price by purchase date</span>
                </label>
              </div>
            </div>

            <div class="form-group">
              <label for="purchaseDate">Purchase Date</label>
              <input
                id="purchaseDate"
                v-model="stockForm.purchaseDate"
                type="datetime-local"
                required
                @change="onPurchaseDateChange"
              />
            </div>

            <div class="form-group">
              <label for="purchasePrice">Purchase Price</label>
              <input
                id="purchasePrice"
                v-model.number="stockForm.purchasePrice"
                type="number"
                step="0.01"
                min="0"
                required
                placeholder="0.00"
                :disabled="priceOption === 'date'"
                :class="{ 'loading-price': loadingHistoricalPrice }"
              />
              <small v-if="priceOption === 'date'" class="helper-text">
                Price will be fetched automatically based on purchase date
              </small>
              <small v-if="loadingHistoricalPrice" class="helper-text loading">
                Loading historical price...
              </small>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeAddStockModal" class="btn-secondary">Cancel</button>
              <button type="submit" class="btn-primary" :disabled="portfolioStore.loading || !selectedStock">
                Add Stock
              </button>
            </div>
          </form>
        </Modal>

        <Modal :show="showActionModal" title="Remove or Sell Stock?" @close="closeActionModal">
             <div v-if="selectedStockForAction" class="action-modal-content">
               <div class="selected-stock-info">
                 <h4>{{ selectedStockForAction.stock.symbol }} - {{ selectedStockForAction.stock.name }}</h4>
                 <p>Quantity: {{ formatNumber(selectedStockForAction.quantity) }}</p>
                 <p>Purchase Price: {{ preferencesStore.formatPrice(selectedStockForAction.purchasePrice) }}</p>
                 <p>Current Price: {{ preferencesStore.formatPrice(selectedStockForAction.currentPrice) }}</p>
               </div>

            <div class="action-choices">
              <button @click="confirmDelete" class="btn-delete">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                Delete
                <span class="action-description">Remove without tracking sale</span>
              </button>
              <button @click="openSellModal" class="btn-sell">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><polyline points="17 18 12 23 7 18"></polyline></svg>
                Sell
                <span class="action-description">Record sale with price and date</span>
              </button>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeActionModal" class="btn-secondary">Cancel</button>
            </div>
          </div>
        </Modal>

        <Modal :show="showSellStockModal" title="Sell Stock" @close="closeSellStockModal">
           <form @submit.prevent="sellStock">
             <div v-if="selectedStockToSell" class="selected-stock-info">
               <h4>{{ selectedStockToSell.stock.symbol }} - {{ selectedStockToSell.stock.name }}</h4>
               <p>Quantity: {{ formatNumber(selectedStockToSell.quantity) }}</p>
               <p>Purchase Price: {{ preferencesStore.formatPrice(selectedStockToSell.purchasePrice) }}</p>
               <p>Current Price: {{ preferencesStore.formatPrice(selectedStockToSell.currentPrice) }}</p>
             </div>

            <div class="form-group">
              <label for="salePrice">Sale Price</label>
              <input
                id="salePrice"
                v-model.number="sellForm.salePrice"
                type="number"
                step="0.01"
                min="0"
                required
                placeholder="0.00"
              />
            </div>

            <div class="form-group">
              <label for="saleDate">Sale Date</label>
              <input
                id="saleDate"
                v-model="sellForm.saleDate"
                type="datetime-local"
                required
              />
            </div>

            <div class="form-actions">
              <button type="button" @click="closeSellStockModal" class="btn-secondary">Cancel</button>
              <button type="submit" class="btn-primary" :disabled="portfolioStore.loading">
                Sell Stock
              </button>
            </div>
          </form>
        </Modal>

        <Modal :show="showEditModal" title="Edit Portfolio" @close="showEditModal = false">
          <form @submit.prevent="updatePortfolio">
            <div class="form-group">
              <label for="portfolioName">Name</label>
              <input
                id="portfolioName"
                v-model="editForm.name"
                type="text"
                required
                placeholder="Portfolio name"
              />
            </div>

            <div class="form-group">
              <label for="portfolioDescription">Description</label>
              <textarea
                id="portfolioDescription"
                v-model="editForm.description"
                placeholder="Portfolio description"
                rows="3"
              />
            </div>

            <div class="form-group">
              <label>Icon</label>
              <IconPicker v-model="editForm.icon" />
            </div>

            <div class="form-group">
              <label for="portfolioLink">Link</label>
              <input
                id="portfolioLink"
                v-model="editForm.link"
                type="url"
                placeholder="https://example.com"
              />
            </div>

            <div class="form-actions">
              <button type="button" @click="showEditModal = false" class="btn-secondary">Cancel</button>
              <button type="submit" class="btn-primary" :disabled="portfolioStore.loading">
                Update Portfolio
              </button>
            </div>
          </form>
        </Modal>

        <Modal :show="showPurchaseHistoryModal" title="Purchase History" size="large" @close="closePurchaseHistoryModal">
          <div v-if="selectedStockForHistory" class="purchase-history-content">
            <div class="stock-summary">
              <h4>{{ selectedStockForHistory.stock.symbol }} - {{ selectedStockForHistory.stock.name }}</h4>
               <div class="summary-stats">
                 <div class="summary-item">
                   <span class="summary-label">Total Shares:</span>
                   <span class="summary-value">{{ formatNumber(selectedStockForHistory.quantity) }}</span>
                 </div>
                 <div class="summary-item">
                   <span class="summary-label">Avg. Purchase Price:</span>
                   <span class="summary-value">{{ preferencesStore.formatPrice(selectedStockForHistory.purchasePrice) }}</span>
                 </div>
                 <div class="summary-item">
                   <span class="summary-label">Current Price:</span>
                   <span class="summary-value">{{ preferencesStore.formatPrice(selectedStockForHistory.currentPrice) }}</span>
                 </div>
                 <div class="summary-item">
                   <span class="summary-label">Total Gain/Loss:</span>
                   <span class="summary-value" :class="(selectedStockForHistory.gainLoss || 0) >= 0 ? 'positive' : 'negative'">
                     {{ preferencesStore.formatPrice(selectedStockForHistory.gainLoss) }} ({{ formatPercent(selectedStockForHistory.gainLossPercentage) }}%)
                   </span>
                 </div>
               </div>
            </div>

            <div v-if="purchaseHistory.length" class="purchases-table-container">
              <h5>Individual Purchases</h5>
              <table class="purchases-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Quantity</th>
                    <th>Purchase Price</th>
                    <th>Purchase Value</th>
                    <th>Current Value</th>
                    <th>Gain/Loss</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                   <tr v-for="purchase in purchaseHistory" :key="purchase.id">
                     <td>{{ formatDateTime(purchase.purchaseDate) }}</td>
                     <td>{{ formatNumber(purchase.quantity) }}</td>
                     <td>{{ preferencesStore.formatPrice(purchase.purchasePrice) }}</td>
                     <td>{{ preferencesStore.formatPrice(purchase.purchasePrice * purchase.quantity) }}</td>
                     <td>{{ preferencesStore.formatPrice(purchase.currentValue) }}</td>
                     <td :class="(purchase.gainLoss || 0) >= 0 ? 'positive' : 'negative'">
                       {{ preferencesStore.formatPrice(purchase.gainLoss) }}<br>
                       <small>({{ formatPercent(purchase.gainLossPercentage) }}%)</small>
                     </td>
                    <td>
                      <button @click="deletePurchase(purchase.id)" class="btn-delete-purchase-mini" title="Delete purchase">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-else class="no-purchases">
              <p>No individual purchase records found.</p>
            </div>
          </div>
        </Modal>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import Modal from '../components/Modal.vue'
import PerformanceChart from '../components/PerformanceChart.vue'
import IconPicker from '../components/IconPicker.vue'
import { usePortfolioStore } from '../stores/portfolio'
import { usePreferencesStore } from '../stores/preferences'
import api from '../services/api'

const route = useRoute()
const portfolioStore = usePortfolioStore()
const preferencesStore = usePreferencesStore()

const showAddStockModal = ref(false)
const showSellStockModal = ref(false)
const showEditModal = ref(false)
const showActionModal = ref(false)
const showPurchaseHistoryModal = ref(false)
const showChart = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const selectedStock = ref(null)
const selectedStockToSell = ref(null)
const selectedStockForAction = ref(null)
const selectedStockForHistory = ref(null)
const purchaseHistory = ref([])
const performanceStats = ref(null)
const chartData = ref([])
const loadingChart = ref(false)
const selectedRange = ref('1M')
const timeRanges = ['1D', '1W', '1M', '3M', '6M', '1Y', 'ALL']
const priceOption = ref('manual')
const loadingHistoricalPrice = ref(false)
const stockForm = ref({
  symbol: '',
  quantity: 0,
  purchasePrice: 0,
  purchaseDate: new Date().toISOString().slice(0, 16),
  useMarketPrice: false
})
const sellForm = ref({
  salePrice: 0,
  saleDate: new Date().toISOString().slice(0, 16)
})
const editForm = ref({
  name: '',
  description: '',
  icon: '',
  link: ''
})

const stocks = computed(() => portfolioStore.currentPortfolio?.stocks || [])

onMounted(() => {
  portfolioStore.fetchPortfolioById(route.params.id).then(() => {
    if (portfolioStore.currentPortfolio) {
      editForm.value = {
        name: portfolioStore.currentPortfolio.name || '',
        description: portfolioStore.currentPortfolio.description || '',
        icon: portfolioStore.currentPortfolio.icon || '',
        link: portfolioStore.currentPortfolio.link || ''
      }
    }
  })
  fetchPerformance()
})

async function updatePortfolio() {
  try {
    await portfolioStore.updatePortfolio(route.params.id, editForm.value)
    showEditModal.value = false
  } catch (error) {
    console.error('Failed to update portfolio:', error)
  }
}

async function fetchPerformance() {
  try {
    const response = await api.get(`/portfolios/${route.params.id}/performance`, {
      params: { range: selectedRange.value }
    })
    performanceStats.value = response.data
  } catch (error) {
    console.error('Failed to fetch performance:', error)
  }
}

async function fetchChartData() {
  loadingChart.value = true
  try {
    const period = selectedRange.value === 'ALL' ? '5Y' : selectedRange.value
    const response = await api.get(`/stats/portfolio-chart?period=${period}&portfolioIds=${route.params.id}`)
    chartData.value = response.data
  } catch (error) {
    console.error('Failed to fetch chart data:', error)
    chartData.value = []
  } finally {
    loadingChart.value = false
  }
}

let searchTimeout = null
async function searchStocks() {
  if (searchTimeout) clearTimeout(searchTimeout)
  
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  
  searchTimeout = setTimeout(async () => {
    try {
      const response = await api.get('/stocks/search', {
        params: { query: searchQuery.value }
      })
      searchResults.value = response.data
    } catch (error) {
      console.error('Failed to search stocks:', error)
    }
  }, 300)
}

function selectStock(stock) {
  selectedStock.value = stock
  stockForm.value.symbol = stock.symbol
  if (priceOption.value === 'manual') {
    stockForm.value.purchasePrice = stock.currentPrice || 0
  }
  searchQuery.value = `${stock.symbol} - ${stock.name}`
  searchResults.value = []
}

function onPriceOptionChange() {
  if (priceOption.value === 'date' && selectedStock.value) {
    fetchHistoricalPrice()
  } else if (priceOption.value === 'manual' && selectedStock.value) {
    stockForm.value.purchasePrice = selectedStock.value.currentPrice || 0
  }
}

async function onPurchaseDateChange() {
  if (priceOption.value === 'date' && selectedStock.value) {
    await fetchHistoricalPrice()
  }
}

async function fetchHistoricalPrice() {
  if (!selectedStock.value || !stockForm.value.purchaseDate) return
  
  loadingHistoricalPrice.value = true
  try {
    // Convert datetime-local to ISO string for API
    const date = new Date(stockForm.value.purchaseDate).toISOString()
    const response = await api.get(`/stocks/info/${selectedStock.value.symbol}/historical-price`, {
      params: { date }
    })
    stockForm.value.purchasePrice = response.data.price || 0
    console.log(`Fetched historical price for ${stockForm.value.purchaseDate}: $${response.data.price}`)
  } catch (error) {
    console.error('Failed to fetch historical price:', error)
    // Fallback to current price if historical price fails
    stockForm.value.purchasePrice = selectedStock.value.currentPrice || 0
  } finally {
    loadingHistoricalPrice.value = false
  }
}

async function addStock() {
  if (!selectedStock.value) return
  
  try {
    await api.post(`/purchases/portfolio/${route.params.id}`, {
      symbol: selectedStock.value.symbol,
      quantity: stockForm.value.quantity,
      purchasePrice: priceOption.value === 'manual' ? stockForm.value.purchasePrice : null,
      purchaseDate: new Date(stockForm.value.purchaseDate).toISOString(),
      useMarketPrice: priceOption.value === 'date'
    })
    closeAddStockModal()
    await portfolioStore.fetchPortfolioById(route.params.id)
    await fetchPerformance()
  } catch (error) {
    console.error('Failed to add stock:', error)
  }
}

function openActionModal(stock) {
  selectedStockForAction.value = stock
  showActionModal.value = true
}

function closeActionModal() {
  showActionModal.value = false
  selectedStockForAction.value = null
}

async function confirmDelete() {
  if (!selectedStockForAction.value) return
  
  if (confirm('Are you sure you want to delete this stock? This action cannot be undone.')) {
    try {
      await api.delete(`/portfolios/stocks/${selectedStockForAction.value.id}`)
      await portfolioStore.fetchPortfolioById(route.params.id)
      await fetchPerformance()
      closeActionModal()
    } catch (error) {
      console.error('Failed to delete stock:', error)
    }
  }
}

function openSellModal() {
  selectedStockToSell.value = selectedStockForAction.value
  sellForm.value.salePrice = selectedStockForAction.value.currentPrice || 0
  closeActionModal()
  showSellStockModal.value = true
}

async function removeStockConfirm(stock) {
  selectedStockToSell.value = stock
  sellForm.value.salePrice = stock.currentPrice || 0
  showSellStockModal.value = true
}

async function sellStock() {
  if (!selectedStockToSell.value) return
  
  try {
    await api.put(`/portfolios/stocks/${selectedStockToSell.value.id}/sell`, {
      salePrice: sellForm.value.salePrice,
      saleDate: new Date(sellForm.value.saleDate).toISOString()
    })
    await portfolioStore.fetchPortfolioById(route.params.id)
    await fetchPerformance()
    closeSellStockModal()
  } catch (error) {
    console.error('Failed to sell stock:', error)
  }
}

function closeSellStockModal() {
  showSellStockModal.value = false
  selectedStockToSell.value = null
  sellForm.value = {
    salePrice: 0,
    saleDate: new Date().toISOString().slice(0, 16)
  }
}

function closeAddStockModal() {
  showAddStockModal.value = false
  searchQuery.value = ''
  searchResults.value = []
  selectedStock.value = null
  stockForm.value = {
    symbol: '',
    quantity: 0,
    purchasePrice: 0,
    purchaseDate: new Date().toISOString().slice(0, 16)
  }
}

function formatNumber(num) {
  return num?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

function formatPercent(num) {
  if (num === null || num === undefined || isNaN(num)) return '0.00'
  return Number(num).toFixed(2)
}

function formatDateTime(dateString) {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function openPurchaseHistory(portfolioStock) {
  selectedStockForHistory.value = portfolioStock
  showPurchaseHistoryModal.value = true
  await fetchPurchaseHistory(portfolioStock.id)
}

function closePurchaseHistoryModal() {
  showPurchaseHistoryModal.value = false
  selectedStockForHistory.value = null
  purchaseHistory.value = []
}

async function fetchPurchaseHistory(portfolioStockId) {
  try {
    const response = await api.get(`/purchases/portfolio-stock/${portfolioStockId}`)
    purchaseHistory.value = response.data
  } catch (error) {
    console.error('Failed to fetch purchase history:', error)
  }
}

async function deletePurchase(purchaseId) {
  if (!confirm('Are you sure you want to delete this purchase? This action cannot be undone.')) return
  
  try {
    await api.delete(`/purchases/${purchaseId}`)
    // Refresh purchase history
    await fetchPurchaseHistory(selectedStockForHistory.value.id)
    // Refresh portfolio data
    await portfolioStore.fetchPortfolioById(route.params.id)
    await fetchPerformance()
    
    // Close modal if no more purchases
    if (purchaseHistory.value.length === 0) {
      closePurchaseHistoryModal()
    }
  } catch (error) {
    console.error('Failed to delete purchase:', error)
    alert('Failed to delete purchase. Please try again.')
  }
}
</script>

<style scoped>
.portfolio-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.portfolio-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.portfolio-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-back {
  background: none;
  border: none;
  color: #667eea;
  font-size: 1rem;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: background 0.3s;
}

.btn-back:hover {
  background: #f0f3ff;
}

.portfolio-icon {
  font-size: 2.5rem;
}

h1 {
  margin: 0;
  color: #333;
}

.portfolio-description {
  color: #666;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.portfolio-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #333;
}

.chart-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.loading-chart,
.empty-chart {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.chart-header h3 {
  margin: 0;
  color: #333;
}

.time-selector {
  display: flex;
  gap: 0.5rem;
}

.time-btn {
  padding: 0.5rem 1rem;
  background: #f0f0f0;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.time-btn:hover {
  background: #e8eaf6;
}

.time-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.show-chart-section {
  text-align: center;
  margin-bottom: 2rem;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stocks-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #667eea;
  color: white;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background: #f9f9f9;
}

.symbol {
  font-weight: 700;
}

.stock-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s;
}

.stock-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #e0e0e0;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.positive {
  color: #10b981;
  font-weight: 600;
}

.negative {
  color: #ef4444;
  font-weight: 600;
}

.btn-icon-modern {
  background: #f0f0f0;
  border: 1px solid #e0e0e0;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.btn-icon-modern:hover {
  background: #667eea;
  border-color: #667eea;
  color: white;
  transform: translateY(-2px);
}

.btn-icon-modern.delete:hover {
  background: #ef4444;
  border-color: #ef4444;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem;
  transition: transform 0.2s;
}

.btn-icon:hover {
  transform: scale(1.2);
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-state p {
  color: #666;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

input,
select,
textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: border-color 0.3s;
  box-sizing: border-box;
  resize: vertical;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: #e0e0e0;
  color: #333;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-secondary:hover {
  background: #d0d0d0;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  cursor: pointer;
  transition: background 0.2s;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background: #f9f9f9;
}

.search-result-item.selected {
  background: #e8eaf6;
  border-left: 3px solid #667eea;
}

.stock-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stock-symbol {
  font-weight: 700;
  color: #667eea;
  font-size: 1rem;
}

.stock-name {
  color: #666;
  font-size: 0.85rem;
}

.stock-price {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
}

.selected-stock-info {
  background: #e8f5e9;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  border-left: 3px solid #10b981;
}

.selected-stock-info h4 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1rem;
}

.selected-stock-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
}

.radio-option:hover {
  border-color: #667eea;
  background: #f7fafc;
}

.radio-option input[type="radio"] {
  cursor: pointer;
  width: 18px;
  height: 18px;
}

.radio-option input[type="radio"]:checked + span {
  font-weight: 600;
  color: #667eea;
}

.radio-option span {
  flex: 1;
  color: #333;
}

input:disabled {
  background: #f7fafc;
  cursor: not-allowed;
  opacity: 0.6;
}

.helper-text {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.helper-text.loading {
  color: #667eea;
  font-style: italic;
}

.loading-price {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.action-modal-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.action-choices {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.btn-delete,
.btn-sell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1.5rem;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  font-size: 1rem;
}

.btn-delete {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
  border-color: #fca5a5;
}

.btn-delete:hover {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  border-color: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.btn-sell {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
  border-color: #93c5fd;
}

.btn-sell:hover {
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.btn-delete svg,
.btn-sell svg {
  flex-shrink: 0;
}

.action-description {
  font-size: 0.75rem;
  font-weight: 400;
  opacity: 0.8;
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.clickable-row {
  cursor: pointer;
}

.clickable-row:hover {
  background: #f7fafc !important;
}

.purchase-history-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stock-summary {
  background: #f0f9ff;
  padding: 1.5rem;
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.stock-summary h4 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.25rem;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.summary-label {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

.summary-value {
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.purchases-table-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.purchases-table-container h5 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.purchases-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.purchases-table thead {
  background: #667eea;
  color: white;
}

.purchases-table th {
  padding: 0.875rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.9rem;
  background: #667eea;
  color: white;
}

.purchases-table td {
  padding: 0.875rem;
  border-bottom: 1px solid #e2e8f0;
  font-size: 0.9rem;
}

.purchases-table tbody tr:hover {
  background: #f7fafc;
}

.purchases-table tbody tr:last-child td {
  border-bottom: none;
}

.purchases-table td small {
  font-size: 0.8rem;
  opacity: 0.8;
}

.btn-delete-purchase-mini {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fca5a5;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-delete-purchase-mini:hover {
  background: #fecaca;
  border-color: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

.purchases-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.purchases-list h5 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.1rem;
}

.purchase-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.25rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  transition: all 0.3s;
}

.purchase-item:hover {
  border-color: #cbd5e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.purchase-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.purchase-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.purchase-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
  min-width: 120px;
}

.purchase-value {
  font-size: 0.95rem;
  color: #333;
  font-weight: 600;
  text-align: right;
  flex: 1;
}

.btn-delete-purchase {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
  border: 2px solid #fca5a5;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  font-size: 0.9rem;
  white-space: nowrap;
}

.btn-delete-purchase:hover {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  border-color: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.no-purchases {
  text-align: center;
  padding: 2rem;
  color: #666;
  background: #f9fafb;
  border-radius: 8px;
}

.no-purchases p {
  margin: 0;
}

</style>
