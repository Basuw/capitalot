<template>
  <div class="stock-detail">
    <div class="header-section">
      <button @click="goBack" class="back-btn">← Back</button>
      <h1>{{ symbol }}</h1>
      <div v-if="stockInfo" class="stock-info">
        <div class="info-item">
          <span class="label">Current Price</span>
          <span class="value price">${{ currentPrice?.toFixed(2) }}</span>
        </div>
        <div class="info-item">
          <span class="label">Type</span>
          <span class="value">{{ stockInfo.type }}</span>
        </div>
        <div class="info-item">
          <span class="label">Market Score</span>
          <span class="value">{{ stockInfo.marketScore }}</span>
        </div>
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
      api.get(`/api/stocks/${symbol}`),
      api.get(`/api/stocks/${symbol}/price-history`)
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
  margin-bottom: 1.5rem;
  color: #1e1e2e;
}

.stock-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-item {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
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
  font-size: 1.5rem;
}

.chart-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chart-section h2 {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
  color: #1e1e2e;
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
