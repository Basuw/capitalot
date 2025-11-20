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

      <div v-if="filteredResults.length" class="results-section">
        <h2>{{ searchQuery ? `Résultats (${filteredResults.length})` : 'Actions populaires' }}</h2>
        <div class="results-grid">
          <div v-for="stock in filteredResults" :key="stock.symbol" class="stock-card">
            <div class="stock-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
            </div>
            <div class="stock-header">
              <div>
                <router-link :to="`/stocks/${stock.symbol}`" class="stock-symbol">{{ stock.symbol }}</router-link>
                <div class="stock-name">{{ stock.name }}</div>
              </div>
              <span v-if="stock.type" class="stock-type">{{ stock.type }}</span>
            </div>
            
            <div class="stock-price-section">
              <div class="stock-price">
                ${{ stock.currentPrice ? formatNumber(stock.currentPrice) : '--.--' }}
              </div>
              <div v-if="stock.marketScore" class="stock-score">
                ⭐ {{ stock.marketScore }}/10
              </div>
            </div>

            <div class="stock-actions">
              <button @click="addToPortfolio(stock)" class="btn-action primary">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                Wallet
              </button>
              <button @click="addToWatchlist(stock)" class="btn-action secondary">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>
                Watchlist
              </button>
            </div>
          </div>
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

      <Modal :show="showPortfolioModal" title="Add to Portfolio" @close="showPortfolioModal = false">
        <div v-if="portfolioStore.portfolios.length">
          <p>Select a portfolio to add {{ selectedStock?.symbol }}:</p>
          <div class="portfolio-list">
            <div
              v-for="portfolio in portfolioStore.portfolios"
              :key="portfolio.id"
              class="portfolio-option"
              @click="addStockToPortfolio(portfolio.id)"
            >
              <span class="portfolio-icon">{{ portfolio.icon || '💼' }}</span>
              <span class="portfolio-name">{{ portfolio.name }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>You don't have any portfolios yet.</p>
          <router-link to="/portfolios" class="btn-primary">Create a Portfolio</router-link>
        </div>
      </Modal>

      <Modal :show="showStockModal" title="Add Stock Details" @close="closeStockModal">
        <form @submit.prevent="submitStockToPortfolio">
          <div class="stock-info">
            <strong>{{ selectedStock?.symbol }}</strong> - {{ selectedStock?.name }}
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
            <button type="button" @click="closeStockModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="portfolioStore.loading">
              Add Stock
            </button>
          </div>
        </form>
      </Modal>

      <Modal :show="showWatchlistModal" title="Add to Watchlist" @close="closeWatchlistModal">
        <form @submit.prevent="submitToWatchlist">
          <div class="stock-info">
            <strong>{{ selectedStock?.symbol }}</strong> - {{ selectedStock?.name }}
          </div>

          <div class="form-group">
            <label for="targetPrice">Target Price (optional)</label>
            <input
              id="targetPrice"
              v-model.number="watchlistForm.targetPrice"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
            />
          </div>

          <div class="form-group">
            <label for="priority">Priority</label>
            <select id="priority" v-model="watchlistForm.priority">
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          <div class="form-group">
            <label for="notes">Notes (optional)</label>
            <textarea
              id="notes"
              v-model="watchlistForm.notes"
              rows="3"
              placeholder="Add any notes about this stock..."
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeWatchlistModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="watchlistStore.loading">
              Add to Watchlist
            </button>
          </div>
        </form>
      </Modal>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import Navbar from '../components/Navbar.vue'
import Modal from '../components/Modal.vue'
import { usePortfolioStore } from '../stores/portfolio'
import { useWatchlistStore } from '../stores/watchlist'
import api from '../services/api'

const portfolioStore = usePortfolioStore()
const watchlistStore = useWatchlistStore()

const searchQuery = ref('')
const searchResults = ref([])
const searching = ref(false)
const selectedStock = ref(null)
const selectedPortfolioId = ref(null)

const showPortfolioModal = ref(false)
const showStockModal = ref(false)
const showWatchlistModal = ref(false)

const stockForm = ref({
  quantity: 0,
  purchasePrice: 0,
  purchaseDate: new Date().toISOString().slice(0, 16)
})

const watchlistForm = ref({
  targetPrice: null,
  priority: 'MEDIUM',
  notes: ''
})

let searchTimeout = null

const filteredResults = computed(() => {
  return searchResults.value
})

onMounted(async () => {
  portfolioStore.fetchPortfolios()
  await loadPopularStocks()
})

async function loadPopularStocks() {
  searching.value = true
  try {
    const response = await api.get('/stocks/popular')
    searchResults.value = response.data
  } catch (error) {
    console.error('Failed to load popular stocks:', error)
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
    loadPopularStocks()
    return
  }

  searchTimeout = setTimeout(async () => {
    searching.value = true
    try {
      const response = await api.get(`/stocks/search?query=${encodeURIComponent(searchQuery.value)}`)
      searchResults.value = response.data
    } catch (error) {
      console.error('Search failed:', error)
      searchResults.value = []
    } finally {
      searching.value = false
    }
  }, 500)
}

function addToPortfolio(stock) {
  selectedStock.value = stock
  showPortfolioModal.value = true
}

function addStockToPortfolio(portfolioId) {
  selectedPortfolioId.value = portfolioId
  showPortfolioModal.value = false
  showStockModal.value = true
}

async function submitStockToPortfolio() {
  try {
    await portfolioStore.addStock(selectedPortfolioId.value, {
      symbol: selectedStock.value.symbol,
      quantity: stockForm.value.quantity,
      purchasePrice: stockForm.value.purchasePrice,
      purchaseDate: new Date(stockForm.value.purchaseDate).toISOString()
    })
    closeStockModal()
  } catch (error) {
    console.error('Failed to add stock to portfolio:', error)
  }
}

function closeStockModal() {
  showStockModal.value = false
  selectedStock.value = null
  selectedPortfolioId.value = null
  stockForm.value = {
    quantity: 0,
    purchasePrice: 0,
    purchaseDate: new Date().toISOString().slice(0, 16)
  }
}

function addToWatchlist(stock) {
  selectedStock.value = stock
  showWatchlistModal.value = true
}

async function submitToWatchlist() {
  try {
    await watchlistStore.addToWatchlist({
      symbol: selectedStock.value.symbol,
      name: selectedStock.value.name,
      type: selectedStock.value.type,
      targetPrice: watchlistForm.value.targetPrice,
      priority: watchlistForm.value.priority,
      notes: watchlistForm.value.notes
    })
    closeWatchlistModal()
  } catch (error) {
    console.error('Failed to add to watchlist:', error)
  }
}

function closeWatchlistModal() {
  showWatchlistModal.value = false
  selectedStock.value = null
  watchlistForm.value = {
    targetPrice: null,
    priority: 'MEDIUM',
    notes: ''
  }
}

function formatNumber(num) {
  return num?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}
</script>

<style scoped>
.stock-search-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

.header-section {
  margin-bottom: 3rem;
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

.results-section {
  margin-top: 2rem;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
}

.stock-card {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.stock-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.stock-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  color: white;
}

.stock-header {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stock-symbol {
  font-size: 1.75rem;
  font-weight: 700;
  color: #333;
  text-decoration: none;
  transition: color 0.3s;
}

.stock-symbol:hover {
  color: #667eea;
}

.stock-name {
  color: #666;
  font-size: 0.95rem;
  margin-top: 0.5rem;
  line-height: 1.4;
}

.stock-type {
  display: inline-block;
  padding: 0.4rem 0.9rem;
  background: #f0f4ff;
  color: #667eea;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.stock-price-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
}

.stock-price {
  font-size: 1.75rem;
  font-weight: 700;
  color: #333;
}

.stock-score {
  font-size: 1rem;
  font-weight: 600;
  color: #667eea;
}

.stock-actions {
  display: flex;
  gap: 0.75rem;
}

.btn-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-action.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-action.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-action.secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-action.secondary:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
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

.portfolio-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
}

.portfolio-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.portfolio-option:hover {
  background: #e0e0e0;
}

.portfolio-icon {
  font-size: 1.5rem;
}

.portfolio-name {
  font-weight: 600;
  color: #333;
}

.stock-info {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  color: #333;
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

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
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
</style>
