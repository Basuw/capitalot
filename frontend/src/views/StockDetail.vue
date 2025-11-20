<template>
  <div class="stock-detail">
    <div class="header-section">
      <button @click="goBack" class="back-btn">← Back</button>
      <h1>{{ stockInfo?.name || symbol }}</h1>
      <p v-if="stockInfo" class="stock-symbol-subtitle">{{ symbol }} • {{ stockInfo.exchange }}</p>
      
      <div v-if="stockInfo" class="main-info-grid">
        <div class="info-card price-card">
          <span class="label">Current Price</span>
          <span class="value price">${{ stockInfo.currentPrice?.toFixed(2) || '0.00' }}</span>
          <span 
            v-if="stockInfo.dailyChange !== undefined" 
            :class="['daily-change', stockInfo.dailyChange >= 0 ? 'positive' : 'negative']"
          >
            {{ stockInfo.dailyChange >= 0 ? '+' : '' }}${{ Math.abs(stockInfo.dailyChange).toFixed(2) }}
            ({{ stockInfo.dailyChange >= 0 ? '+' : '' }}{{ stockInfo.dailyChangePercentage?.toFixed(2) }}%)
          </span>
        </div>

        <div class="info-card">
          <span class="label">Annual Dividend</span>
          <span class="value">${{ stockInfo.annualDividend?.toFixed(2) || '0.00' }}</span>
        </div>

        <div class="info-card">
          <span class="label">Risk Score</span>
          <span class="value" :style="{ color: getRiskColor(stockInfo.risk) }">
            {{ stockInfo.risk?.toFixed(1) || 'N/A' }}/10
          </span>
        </div>

        <div class="info-card">
          <span class="label">Market Score</span>
          <span class="value">{{ stockInfo.marketScore?.toFixed(1) || 'N/A' }}</span>
        </div>
      </div>

      <div v-if="stockInfo" class="secondary-info-grid">
        <div class="info-item">
          <span class="label">Sector</span>
          <span class="value">{{ stockInfo.sector || 'N/A' }}</span>
        </div>
        <div class="info-item">
          <span class="label">Industry</span>
          <span class="value">{{ stockInfo.industry || 'N/A' }}</span>
        </div>
      </div>

      <div v-if="stockInfo && (stockInfo.longPercentage || stockInfo.shortPercentage)" class="long-short-section">
        <h3>Position Sentiment</h3>
        <div class="sentiment-bars">
          <div class="sentiment-bar">
            <div class="bar-label">
              <span>Long</span>
              <span class="percentage">{{ stockInfo.longPercentage?.toFixed(1) || 0 }}%</span>
            </div>
            <div class="bar-container">
              <div class="bar long" :style="{ width: `${stockInfo.longPercentage || 0}%` }"></div>
            </div>
          </div>
          <div class="sentiment-bar">
            <div class="bar-label">
              <span>Short</span>
              <span class="percentage">{{ stockInfo.shortPercentage?.toFixed(1) || 0 }}%</span>
            </div>
            <div class="bar-container">
              <div class="bar short" :style="{ width: `${stockInfo.shortPercentage || 0}%` }"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="stockInfo?.description" class="description-section">
        <h3>About {{ stockInfo.name }}</h3>
        <p>{{ stockInfo.description }}</p>
      </div>
    </div>

    <div v-if="loading" class="loading">Loading chart data...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="priceHistory.length > 0" class="chart-section">
      <h2>Price History</h2>
      <PerformanceChart 
        :data="priceHistory" 
        label="Price" 
        :color="priceChangeColor"
      />
    </div>
    <div v-else class="no-data">No price history available</div>

    <div class="news-section">
      <h2>Latest News</h2>
      <div class="news-placeholder">
        <p>News integration coming soon...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import PerformanceChart from '../components/PerformanceChart.vue'

const route = useRoute()
const router = useRouter()
const symbol = route.params.symbol

const stockInfo = ref(null)
const priceHistory = ref([])
const loading = ref(true)
const error = ref(null)

const currentPrice = computed(() => {
  if (priceHistory.value.length === 0) return null
  return priceHistory.value[priceHistory.value.length - 1].price
})

const priceChangeColor = computed(() => {
  if (priceHistory.value.length < 2) return '#667eea'
  const firstPrice = priceHistory.value[0].price
  const lastPrice = priceHistory.value[priceHistory.value.length - 1].price
  return lastPrice >= firstPrice ? '#10b981' : '#ef4444'
})

async function loadStockData() {
  try {
    loading.value = true
    error.value = null

    const [infoResponse, historyResponse] = await Promise.all([
      api.get(`/stocks/${symbol}`),
      api.get(`/stocks/${symbol}/price-history`)
    ])

    stockInfo.value = infoResponse.data
    priceHistory.value = historyResponse.data
  } catch (err) {
    console.error('Error loading stock data:', err)
    error.value = 'Failed to load stock data'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

function getRiskColor(risk) {
  if (!risk) return '#666'
  if (risk <= 3) return '#10b981'
  if (risk <= 6) return '#f59e0b'
  return '#ef4444'
}

onMounted(() => {
  loadStockData()
})
</script>

<style scoped>
.stock-detail {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  margin-bottom: 2rem;
}

.back-btn {
  background: white;
  border: 1px solid #e0e0e0;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.stock-detail h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: #1e1e2e;
}

.stock-symbol-subtitle {
  color: #6b7280;
  font-size: 1rem;
  margin-bottom: 1.5rem;
  font-weight: 500;
}

.main-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.info-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.price-card {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
}

.label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.value {
  font-size: 1.25rem;
  color: #1e1e2e;
  font-weight: 600;
}

.value.price {
  color: #667eea;
  font-size: 1.75rem;
}

.daily-change {
  font-size: 0.9rem;
  font-weight: 600;
}

.daily-change.positive {
  color: #10b981;
}

.daily-change.negative {
  color: #ef4444;
}

.secondary-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.long-short-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.long-short-section h3 {
  margin: 0 0 1rem 0;
  color: #1e1e2e;
  font-size: 1.1rem;
}

.sentiment-bars {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.sentiment-bar {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.bar-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
}

.percentage {
  color: #667eea;
}

.bar-container {
  width: 100%;
  height: 24px;
  background: #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
}

.bar {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 12px;
}

.bar.long {
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
}

.bar.short {
  background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
}

.description-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.description-section h3 {
  margin: 0 0 1rem 0;
  color: #1e1e2e;
  font-size: 1.1rem;
}

.description-section p {
  color: #555;
  line-height: 1.6;
  margin: 0;
}

.chart-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.chart-section h2 {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
  color: #1e1e2e;
}

.news-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.news-section h2 {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
  color: #1e1e2e;
}

.news-placeholder {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  font-style: italic;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  color: #6b7280;
}

.error {
  color: #ef4444;
}
</style>
