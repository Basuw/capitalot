<template>
  <div>
    <Navbar />
    
    <div class="stock-search-container">
      <h1>Actions</h1>
      
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher des actions par symbole ou nom..."
          @input="handleSearch"
        />
        <div v-if="searching" class="search-status">Searching...</div>
      </div>

      <div class="filters-section">
        <div class="filter-group">
          <label>Type</label>
          <select v-model="filters.type">
            <option value="">Tous</option>
            <option value="STOCK">Stock</option>
            <option value="ETF">ETF</option>
            <option value="CRYPTO">Crypto</option>
            <option value="COMMODITY">Commodity</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Score minimum</label>
          <select v-model.number="filters.minScore">
            <option :value="0">Tous</option>
            <option :value="7">7+</option>
            <option :value="8">8+</option>
            <option :value="9">9+</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Trier par</label>
          <select v-model="filters.sortBy">
            <option value="symbol">Symbole</option>
            <option value="name">Nom</option>
            <option value="score">Score</option>
            <option value="price">Prix</option>
          </select>
        </div>
      </div>

      <div v-if="filteredResults.length" class="results-section">
        <h2>Résultats ({{ filteredResults.length }})</h2>
        <div class="results-grid">
          <div v-for="stock in filteredResults" :key="stock.symbol" class="stock-card">
            <div class="stock-header">
              <div>
                <div class="stock-symbol">{{ stock.symbol }}</div>
                <div class="stock-name">{{ stock.name }}</div>
              </div>
              <span class="stock-type">{{ stock.type }}</span>
            </div>
            
            <div class="stock-details">
              <div v-if="stock.currentPrice" class="stock-price">
                ${{ formatNumber(stock.currentPrice) }}
              </div>
              <div v-if="stock.marketScore" class="stock-score">
                Score: {{ stock.marketScore }}/10
              </div>
            </div>

            <div class="stock-actions">
              <button @click="addToPortfolio(stock)" class="btn-action">
                Ajouter au Wallet
              </button>
              <button @click="addToWatchlist(stock)" class="btn-action secondary">
                Ajouter à la Watchlist
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="searchQuery && !searching" class="empty-state">
        <p>Aucune action trouvée pour "{{ searchQuery }}"</p>
      </div>

      <div v-else class="empty-state">
        <p>Entrez un symbole ou nom d'action pour rechercher</p>
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

const filters = ref({
  type: '',
  minScore: 0,
  sortBy: 'symbol'
})

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
  let results = [...searchResults.value]
  
  if (filters.value.type) {
    results = results.filter(stock => stock.type === filters.value.type)
  }
  
  if (filters.value.minScore > 0) {
    results = results.filter(stock => stock.marketScore >= filters.value.minScore)
  }
  
  results.sort((a, b) => {
    switch (filters.value.sortBy) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'score':
        return (b.marketScore || 0) - (a.marketScore || 0)
      case 'price':
        return (b.currentPrice || 0) - (a.currentPrice || 0)
      default:
        return a.symbol.localeCompare(b.symbol)
    }
  })
  
  return results
})

onMounted(() => {
  portfolioStore.fetchPortfolios()
})

function handleSearch() {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  if (!searchQuery.value.trim()) {
    searchResults.value = []
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin: 0 0 2rem 0;
  color: #333;
}

h2 {
  color: #333;
  margin: 0 0 1.5rem 0;
}

h2 {
  color: #333;
  margin: 0 0 1.5rem 0;
}

.filters-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  min-width: 150px;
}

.filter-group label {
  font-size: 0.875rem;
  color: #555;
  font-weight: 500;
}

.filter-group select {
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
}

.search-box {
  margin-bottom: 2rem;
  position: relative;
}

input[type="text"] {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1.1rem;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

input[type="text"]:focus {
  outline: none;
  border-color: #667eea;
}

.search-status {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  font-size: 0.9rem;
}

.results-section {
  margin-top: 2rem;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.stock-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stock-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.stock-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.stock-symbol {
  font-size: 1.5rem;
  font-weight: 700;
  color: #667eea;
}

.stock-name {
  color: #666;
  font-size: 0.9rem;
  margin-top: 0.25rem;
}

.stock-type {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #e0e0e0;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.stock-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
}

.stock-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.stock-score {
  font-size: 1rem;
  font-weight: 600;
  color: #667eea;
  background: #f0f4ff;
  padding: 0.5rem 1rem;
  border-radius: 8px;
}

.stock-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn-action {
  width: 100%;
  padding: 0.75rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}

.btn-action:hover {
  opacity: 0.9;
}

.btn-action.secondary {
  background: #e0e0e0;
  color: #333;
}

.btn-action.secondary:hover {
  background: #d0d0d0;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: #666;
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
