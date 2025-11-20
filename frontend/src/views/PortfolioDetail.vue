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
            <div class="stat-value">${{ formatNumber(performanceStats?.totalInvested || 0) }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Current Value</div>
            <div class="stat-value">${{ formatNumber(performanceStats?.currentValue || 0) }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Total Gain/Loss</div>
            <div class="stat-value" :class="(performanceStats?.totalGainLoss || 0) >= 0 ? 'positive' : 'negative'">
              ${{ formatNumber(performanceStats?.totalGainLoss || 0) }}
              ({{ formatPercent(performanceStats?.totalGainLossPercent || 0) }}%)
            </div>
          </div>
        </div>

        <div v-if="showChart && performanceStats?.performanceHistory" class="chart-section">
          <div class="chart-header">
            <h3>Portfolio Performance</h3>
            <div class="time-selector">
              <button 
                v-for="range in timeRanges" 
                :key="range"
                @click="selectedRange = range; fetchPerformance()"
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
          <PerformanceChart 
            :data="performanceStats.performanceHistory" 
            label="Portfolio Value"
            :selectedRange="selectedRange"
          />
        </div>
        
        <div v-else class="show-chart-section">
          <button @click="showChart = true; fetchPerformance()" class="btn-secondary">
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
              <tr v-for="stock in stocks" :key="stock.id">
                <td class="symbol">
                  <router-link :to="`/stocks/${stock.stock.symbol}`" class="stock-link">
                    {{ stock.stock.symbol }}
                  </router-link>
                </td>
                <td>{{ stock.stock.name }}</td>
                <td>
                  <span class="badge">{{ stock.stock.type }}</span>
                </td>
                <td>{{ formatNumber(stock.quantity) }}</td>
                <td>${{ formatNumber(stock.purchasePrice) }}</td>
                <td>${{ formatNumber(stock.currentPrice) }}</td>
                <td>${{ formatNumber(stock.currentValue) }}</td>
                <td :class="(stock.gainLoss || 0) >= 0 ? 'positive' : 'negative'">
                  ${{ formatNumber(stock.gainLoss) }}
                  ({{ formatPercent(stock.gainLossPercentage) }}%)
                </td>
                <td>
                  <button @click="removeStockConfirm(stock)" class="btn-icon-modern delete" title="Remove">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                  </button>
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
              <p>Current Price: ${{ formatNumber(selectedStock.currentPrice || 0) }}</p>
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
              <label for="purchasePrice">Purchase Price</label>
              <input
                id="purchasePrice"
                v-model.number="stockForm.purchasePrice"
                type="number"
                step="0.01"
                min="0"
                required
                placeholder="0.00"
              />
            </div>

            <div class="form-group">
              <label for="purchaseDate">Purchase Date</label>
              <input
                id="purchaseDate"
                v-model="stockForm.purchaseDate"
                type="datetime-local"
                required
              />
            </div>

            <div class="form-actions">
              <button type="button" @click="closeAddStockModal" class="btn-secondary">Cancel</button>
              <button type="submit" class="btn-primary" :disabled="portfolioStore.loading || !selectedStock">
                Add Stock
              </button>
            </div>
          </form>
        </Modal>

        <Modal :show="showSellStockModal" title="Sell Stock" @close="closeSellStockModal">
          <form @submit.prevent="sellStock">
            <div v-if="selectedStockToSell" class="selected-stock-info">
              <h4>{{ selectedStockToSell.stock.symbol }} - {{ selectedStockToSell.stock.name }}</h4>
              <p>Quantity: {{ formatNumber(selectedStockToSell.quantity) }}</p>
              <p>Purchase Price: ${{ formatNumber(selectedStockToSell.purchasePrice) }}</p>
              <p>Current Price: ${{ formatNumber(selectedStockToSell.currentPrice) }}</p>
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
              <label for="portfolioIcon">Icon</label>
              <input
                id="portfolioIcon"
                v-model="editForm.icon"
                type="text"
                placeholder="📊"
              />
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
import { usePortfolioStore } from '../stores/portfolio'
import api from '../services/api'

const route = useRoute()
const portfolioStore = usePortfolioStore()

const showAddStockModal = ref(false)
const showSellStockModal = ref(false)
const showEditModal = ref(false)
const showChart = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const selectedStock = ref(null)
const selectedStockToSell = ref(null)
const performanceStats = ref(null)
const selectedRange = ref('1M')
const timeRanges = ['1D', '1W', '1M', '3M', '6M', '1Y', 'ALL']
const stockForm = ref({
  symbol: '',
  quantity: 0,
  purchasePrice: 0,
  purchaseDate: new Date().toISOString().slice(0, 16)
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
  stockForm.value.purchasePrice = stock.currentPrice || 0
  searchQuery.value = `${stock.symbol} - ${stock.name}`
  searchResults.value = []
}

async function addStock() {
  if (!selectedStock.value) return
  
  try {
    await portfolioStore.addStock(route.params.id, {
      symbol: selectedStock.value.symbol,
      quantity: stockForm.value.quantity,
      purchasePrice: stockForm.value.purchasePrice,
      purchaseDate: new Date(stockForm.value.purchaseDate).toISOString()
    })
    closeAddStockModal()
  } catch (error) {
    console.error('Failed to add stock:', error)
  }
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
select {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

input:focus,
select:focus {
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
</style>
