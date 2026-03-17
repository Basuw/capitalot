<template>
  <div class="stock-detail">
    <div class="header-section">
      <div class="header-top">
        <button @click="goBack" class="back-btn">← Back</button>
        <button @click="refreshData" class="refresh-btn-detail" :disabled="refreshing">
          <svg v-if="!refreshing" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"></polyline><polyline points="1 20 1 14 7 14"></polyline><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path></svg>
          <svg v-else class="spinner" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
          Rafraîchir
        </button>
      </div>
      <h1>{{ stockInfo?.name || symbol }}</h1>
      <p v-if="stockInfo" class="stock-symbol-subtitle">{{ symbol }} • {{ stockInfo.exchange }}</p>
      
      <div v-if="stockInfo" class="main-info-grid">
         <div class="info-card price-card">
           <span class="label">Current Price</span>
           <span class="value price">{{ preferencesStore.formatPrice(stockInfo.currentPrice) }}</span>
           <span 
             v-if="stockInfo.dailyChange !== undefined" 
             :class="['daily-change', stockInfo.dailyChange >= 0 ? 'positive' : 'negative']"
           >
             {{ stockInfo.dailyChange >= 0 ? '+' : '' }}{{ preferencesStore.formatPrice(Math.abs(stockInfo.dailyChange)) }}
             ({{ stockInfo.dailyChange >= 0 ? '+' : '' }}{{ stockInfo.dailyChangePercentage?.toFixed(2) }}%)
           </span>
         </div>

         <div class="info-card">
           <span class="label">Open</span>
           <span class="value">{{ stockInfo.openPrice ? preferencesStore.formatPrice(stockInfo.openPrice) : 'N/A' }}</span>
         </div>

         <div class="info-card">
           <span class="label">High</span>
           <span class="value high-value">{{ stockInfo.highPrice ? preferencesStore.formatPrice(stockInfo.highPrice) : 'N/A' }}</span>
         </div>

         <div class="info-card">
           <span class="label">Low</span>
           <span class="value low-value">{{ stockInfo.lowPrice ? preferencesStore.formatPrice(stockInfo.lowPrice) : 'N/A' }}</span>
         </div>
         
         <div class="info-card">
           <span class="label">Previous Close</span>
           <span class="value">{{ stockInfo.previousClose ? preferencesStore.formatPrice(stockInfo.previousClose) : 'N/A' }}</span>
         </div>
        
        <div class="info-card">
          <span class="label">Volume</span>
          <span class="value">{{ formatVolume(stockInfo.volume) }}</span>
        </div>

         <div class="info-card">
           <span class="label">Annual Dividend</span>
           <span class="value">{{ preferencesStore.formatPrice(stockInfo.annualDividend || 0) }}</span>
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
      
      <!-- Advanced Metrics Section -->
      <div v-if="metrics && preferencesStore.preferences.showDetailedMetrics" class="metrics-section">
        <h3>Advanced Metrics</h3>
        <div class="metrics-grid">
          
          <!-- Annualized Return -->
          <div v-if="preferencesStore.preferences.showAnnualizedReturn && metrics.annualizedReturn !== null" class="metric-card">
            <div class="metric-header">
              <span class="metric-icon">📈</span>
              <span class="metric-label">Annualized Return</span>
            </div>
            <div :class="['metric-value', metrics.annualizedReturn >= 0 ? 'positive' : 'negative']">
              {{ metrics.annualizedReturn >= 0 ? '+' : '' }}{{ (metrics.annualizedReturn * 100).toFixed(2) }}%
            </div>
            <div class="metric-description">Projected annual return rate</div>
          </div>

          <!-- Benchmark Comparison -->
          <div v-if="preferencesStore.preferences.showBenchmarkComparison && metrics.benchmarkComparison" class="metric-card">
            <div class="metric-header">
              <span class="metric-icon">⚖️</span>
              <span class="metric-label">vs {{ metrics.benchmarkComparison.benchmarkSymbol }}</span>
            </div>
            <div class="benchmark-details">
              <div class="benchmark-row">
                <span class="benchmark-label">{{ symbol }}:</span>
                <span :class="['benchmark-value', metrics.percentChange >= 0 ? 'positive' : 'negative']">
                  {{ metrics.percentChange >= 0 ? '+' : '' }}{{ metrics.percentChange.toFixed(2) }}%
                </span>
              </div>
              <div class="benchmark-row">
                <span class="benchmark-label">{{ metrics.benchmarkComparison.benchmarkSymbol }}:</span>
                <span :class="['benchmark-value', metrics.benchmarkComparison.benchmarkPercentChange >= 0 ? 'positive' : 'negative']">
                  {{ metrics.benchmarkComparison.benchmarkPercentChange >= 0 ? '+' : '' }}{{ metrics.benchmarkComparison.benchmarkPercentChange.toFixed(2) }}%
                </span>
              </div>
              <div class="benchmark-row difference">
                <span class="benchmark-label">Difference:</span>
                <span :class="['benchmark-value', metrics.benchmarkComparison.outperformance >= 0 ? 'positive' : 'negative']">
                  {{ metrics.benchmarkComparison.outperformance >= 0 ? '+' : '' }}{{ metrics.benchmarkComparison.outperformance.toFixed(2) }}%
                </span>
              </div>
            </div>
          </div>

           <!-- Best Day -->
           <div v-if="preferencesStore.preferences.showBestWorstDay && metrics.bestDay" class="metric-card">
             <div class="metric-header">
               <span class="metric-icon">🎯</span>
               <span class="metric-label">Best Day</span>
             </div>
             <div class="metric-value positive">
               +{{ (metrics.bestDay.percentChange * 100).toFixed(2) }}%
             </div>
             <div class="metric-description">
               {{ new Date(metrics.bestDay.date).toLocaleDateString() }}
               <br/>
               {{ preferencesStore.formatPrice(metrics.bestDay.price) }} (+{{ preferencesStore.formatPrice(metrics.bestDay.priceChange) }})
             </div>
           </div>

           <!-- Worst Day -->
           <div v-if="preferencesStore.preferences.showBestWorstDay && metrics.worstDay" class="metric-card">
             <div class="metric-header">
               <span class="metric-icon">⚠️</span>
               <span class="metric-label">Worst Day</span>
             </div>
             <div class="metric-value negative">
               {{ (metrics.worstDay.percentChange * 100).toFixed(2) }}%
             </div>
             <div class="metric-description">
               {{ new Date(metrics.worstDay.date).toLocaleDateString() }}
               <br/>
               {{ preferencesStore.formatPrice(metrics.worstDay.price) }} ({{ preferencesStore.formatPrice(metrics.worstDay.priceChange) }})
             </div>
           </div>

        </div>
      </div>

      <div v-if="priceHistory.length > 0">
        <PerformanceChart 
          :data="priceHistory" 
          label="Price" 
          :color="priceChangeColor"
          :showStartPriceLine="preferencesStore.preferences.showStartPriceLine"
          :startPrice="priceHistory[0].price"
        />
      </div>
      <div v-else class="no-data">No price history available</div>
    </div>

    <div class="news-section">
      <h2>Latest News</h2>
      <div class="news-placeholder">
        <p>News integration coming soon...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import PerformanceChart from '../components/PerformanceChart.vue'
import { usePreferencesStore } from '../stores/preferences'

const route = useRoute()
const router = useRouter()
const symbol = route.params.symbol
const preferencesStore = usePreferencesStore()

const stockInfo = ref(null)
const priceHistory = ref([])
const metrics = ref(null)
const loading = ref(true)
const refreshing = ref(false)
const error = ref(null)
const selectedPeriod = ref('1M')

const periods = [
  { label: '1D', value: '1D' },
  { label: '1W', value: '1W' },
  { label: '1M', value: '1M' },
  { label: '3M', value: '3M' },
  { label: '6M', value: '6M' },
  { label: '1Y', value: '1Y' },
  { label: 'ALL', value: '5Y' }
]

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

const periodPerformance = computed(() => {
  if (priceHistory.value.length < 2) return null
  
  const firstPrice = priceHistory.value[0].price
  const lastPrice = priceHistory.value[priceHistory.value.length - 1].price
  const priceChange = lastPrice - firstPrice
  const percentChange = ((priceChange / firstPrice) * 100).toFixed(2)
  
  return {
    firstPrice,
    lastPrice,
    priceChange,
    percentChange,
    isPositive: priceChange >= 0
  }
})

async function loadStockData() {
  try {
    loading.value = true
    error.value = null

    const requests = [
      api.get(`/stocks/${symbol}`),
      api.get(`/stocks/${symbol}/history?period=${selectedPeriod.value}`)
    ]

    // Load metrics if any advanced feature is enabled
    const prefs = preferencesStore.preferences
    if (prefs.showAnnualizedReturn || prefs.showBenchmarkComparison || prefs.showBestWorstDay || prefs.showDetailedMetrics) {
      const benchmarkParam = prefs.benchmarkSymbol || 'SPY'
      requests.push(api.get(`/stocks/${symbol}/metrics?period=${selectedPeriod.value}&benchmark=${benchmarkParam}`))
    }

    const responses = await Promise.all(requests)
    stockInfo.value = responses[0].data
    priceHistory.value = responses[1].data
    if (responses.length > 2) {
      metrics.value = responses[2].data
    }
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

function formatVolume(volume) {
  if (!volume) return 'N/A'
  if (volume >= 1000000000) {
    return (volume / 1000000000).toFixed(2) + 'B'
  } else if (volume >= 1000000) {
    return (volume / 1000000).toFixed(2) + 'M'
  } else if (volume >= 1000) {
    return (volume / 1000).toFixed(2) + 'K'
  }
  return volume.toLocaleString()
}

function getPeriodLabel(period) {
  const labels = {
    '1D': '1 jour',
    '1W': '1 semaine',
    '1M': '1 mois',
    '3M': '3 mois',
    '6M': '6 mois',
    '1Y': '1 an',
    '5Y': '5 ans'
  }
  return labels[period] || period
}

async function refreshData() {
  refreshing.value = true
  try {
    await loadStockData()
  } finally {
    refreshing.value = false
  }
}

watch(selectedPeriod, () => {
  loadStockData()
})

onMounted(() => {
  preferencesStore.loadPreferences().then(() => {
    loadStockData()
  })
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

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.back-btn {
  background: white;
  border: 1px solid #e0e0e0;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.refresh-btn-detail {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn-detail:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.refresh-btn-detail:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn-detail svg {
  flex-shrink: 0;
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
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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

.high-value {
  color: #10b981;
}

.low-value {
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.chart-section h2 {
  font-size: 1.25rem;
  margin: 0;
  color: #1e1e2e;
}

.period-performance {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid transparent;
}

.period-performance .performance-label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.performance-value {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 700;
}

.performance-value svg {
  flex-shrink: 0;
}

.performance-value.positive {
  color: #10b981;
}

.performance-value.negative {
  color: #ef4444;
}

.performance-value.positive + .period-performance {
  border-left-color: #10b981;
}

.performance-detail {
  font-size: 0.875rem;
  font-weight: 500;
  opacity: 0.8;
}

.time-selector {
  display: flex;
  gap: 0.5rem;
  background: #f8f9fa;
  padding: 0.25rem;
  border-radius: 8px;
}

.period-btn {
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  min-width: 60px;
}

.period-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.period-btn.active {
  background: #667eea;
  color: white;
}

.period-badge {
  font-size: 0.7rem;
  font-weight: 700;
  margin-top: 2px;
}

.period-badge.positive {
  color: #d1fae5;
}

.period-badge.negative {
  color: #fecaca;
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

.metrics-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.metrics-section h3 {
  margin: 0 0 1rem 0;
  color: #1e1e2e;
  font-size: 1.1rem;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.metric-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 1.25rem;
  border-radius: 10px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.metric-icon {
  font-size: 1.25rem;
}

.metric-label {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.metric-value {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.metric-value.positive {
  color: #10b981;
}

.metric-value.negative {
  color: #ef4444;
}

.metric-description {
  font-size: 0.8rem;
  color: #6b7280;
  line-height: 1.4;
}

.benchmark-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.benchmark-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.benchmark-row:last-child {
  border-bottom: none;
}

.benchmark-row.difference {
  padding-top: 0.75rem;
  font-weight: 700;
  background: rgba(102, 126, 234, 0.05);
  padding: 0.75rem;
  border-radius: 6px;
  margin-top: 0.25rem;
}

.benchmark-label {
  font-size: 0.875rem;
  color: #555;
  font-weight: 500;
}

.benchmark-value {
  font-size: 0.95rem;
  font-weight: 600;
}

.benchmark-value.positive {
  color: #10b981;
}

.benchmark-value.negative {
  color: #ef4444;
}

</style>
