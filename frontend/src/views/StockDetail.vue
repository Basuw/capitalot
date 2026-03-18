<template>
  <div class="stock-detail">
    <div v-if="loading" class="loading-container">
      <div class="spinner-large"></div>
      <p>Chargement des données de l'action...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <h2>Erreur lors du chargement</h2>
      <p>{{ error }}</p>
      <button @click="loadStockData" class="btn-primary">Réessayer</button>
      <button @click="goBack" class="btn-secondary">Retour</button>
    </div>

    <div v-else-if="stockInfo" class="content-section">
      <div class="header-section">
        <div class="header-top">
          <button @click="goBack" class="back-btn">← Retour</button>
          <button @click="refreshData" class="refresh-btn-detail" :disabled="refreshing">
            <svg v-if="!refreshing" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"></polyline><polyline points="1 20 1 14 7 14"></polyline><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path></svg>
            <svg v-else class="spinner" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
            Rafraîchir
          </button>
        </div>
        <div class="header-main-title">
          <div v-if="stockInfo?.logoUrl" class="stock-logo-container">
            <img :src="stockInfo.logoUrl" :alt="stockInfo.name" @error="handleLogoError" class="stock-logo-img" />
          </div>
          <div v-else class="stock-icon-placeholder">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
          </div>
          <div class="header-titles">
            <h1>{{ stockInfo?.name || symbol }}</h1>
            <p v-if="stockInfo" class="stock-symbol-subtitle">{{ symbol }} • {{ stockInfo.exchange }}</p>
          </div>
        </div>
        
        <div v-if="stockInfo" class="main-info-grid">
           <div class="info-card price-card">
             <span class="label">Prix Actuel</span>
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
             <span class="label">Ouverture</span>
             <span class="value">{{ stockInfo.openPrice ? preferencesStore.formatPrice(stockInfo.openPrice) : 'N/A' }}</span>
           </div>

           <div class="info-card">
             <span class="label">Plus Haut</span>
             <span class="value high-value">{{ stockInfo.highPrice ? preferencesStore.formatPrice(stockInfo.highPrice) : 'N/A' }}</span>
           </div>

           <div class="info-card">
             <span class="label">Plus Bas</span>
             <span class="value low-value">{{ stockInfo.lowPrice ? preferencesStore.formatPrice(stockInfo.lowPrice) : 'N/A' }}</span>
           </div>
           
           <div class="info-card">
             <span class="label">Clôture Préc.</span>
             <span class="value">{{ stockInfo.previousClose ? preferencesStore.formatPrice(stockInfo.previousClose) : 'N/A' }}</span>
           </div>
          
          <div class="info-card">
            <span class="label">Volume</span>
            <span class="value">{{ formatVolume(stockInfo.volume) }}</span>
          </div>

           <div class="info-card">
             <span class="label">Dividende Ann.</span>
             <span class="value">{{ preferencesStore.formatPrice(stockInfo.annualDividend || 0) }}</span>
           </div>

          <div class="info-card">
            <span class="label">Score de Risque</span>
            <span class="value" :style="{ color: getRiskColor(stockInfo.risk) }">
              {{ stockInfo.risk?.toFixed(1) || 'N/A' }}/10
            </span>
          </div>
        </div>

        <div v-if="stockInfo" class="secondary-info-grid">
          <div class="info-item">
            <span class="label">Secteur</span>
            <span class="value">{{ stockInfo.sector || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Industrie</span>
            <span class="value">{{ stockInfo.industry || 'N/A' }}</span>
          </div>
        </div>

        <div v-if="stockInfo?.description" class="description-section">
          <h3>À propos</h3>
          <p>{{ stockInfo.description }}</p>
        </div>

        <div v-if="stockInfo && (stockInfo.longPercentage || stockInfo.shortPercentage)" class="long-short-section">
          <h3>Sentiment du marché</h3>
          <div class="sentiment-bars">
            <div class="sentiment-bar">
              <div class="bar-label">
                <span>Achat (Long)</span>
                <span class="percentage">{{ stockInfo.longPercentage?.toFixed(1) || 0 }}%</span>
              </div>
              <div class="bar-container">
                <div class="bar long" :style="{ width: `${stockInfo.longPercentage || 0}%` }"></div>
              </div>
            </div>
            <div class="sentiment-bar">
              <div class="bar-label">
                <span>Vente (Short)</span>
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
          <h3>Mesures Avancées</h3>
          <div class="metrics-grid">
            
            <!-- Annualized Return -->
            <div v-if="preferencesStore.preferences.showAnnualizedReturn && metrics.annualizedReturn !== null" class="metric-card">
              <div class="metric-header">
                <span class="metric-icon">📈</span>
                <span class="metric-label">Rendement Annualisé</span>
              </div>
              <div :class="['metric-value', metrics.annualizedReturn >= 0 ? 'positive' : 'negative']">
                {{ metrics.annualizedReturn >= 0 ? '+' : '' }}{{ (metrics.annualizedReturn * 100).toFixed(2) }}%
              </div>
              <div class="metric-description">Taux de rendement annuel projeté</div>
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
                  <span class="benchmark-label">Différence:</span>
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
                 <span class="metric-label">Meilleur Jour</span>
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
                 <span class="metric-label">Pire Jour</span>
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

        <div class="chart-section">
          <div class="section-header">
            <h2>Graphique des prix</h2>
            <div class="time-selector">
              <button 
                v-for="period in periods" 
                :key="period.value"
                @click="selectedPeriod = period.value"
                :class="['period-btn', { active: selectedPeriod === period.value }]"
              >
                {{ period.label }}
              </button>
            </div>
          </div>
          
          <div v-if="priceHistory.length > 0">
            <PerformanceChart 
              :data="priceHistory" 
              label="Prix" 
              :color="priceChangeColor"
              :showStartPriceLine="preferencesStore.preferences.showStartPriceLine"
              :startPrice="priceHistory[0].price"
              :selectedRange="selectedPeriod"
            />
          </div>
          <div v-else class="no-data">Aucun historique de prix disponible</div>
        </div>
      </div>

      <div v-if="stockInfo?.news && stockInfo.news.length" class="news-section">
        <h2>Actualités & Média</h2>
        <div class="news-list">
          <a v-for="item in stockInfo.news" :key="item.uuid" :href="item.link" target="_blank" class="news-item">
            <div v-if="item.thumbnail && item.thumbnail.resolutions && item.thumbnail.resolutions.length" class="news-thumbnail">
              <img :src="item.thumbnail.resolutions[0].url" :alt="item.title" />
            </div>
            <div class="news-content">
              <h3 class="news-title">{{ item.title }}</h3>
              <div class="news-meta">
                <span class="news-publisher">{{ item.publisher }}</span>
                <span class="news-dot">•</span>
                <span class="news-time">{{ formatNewsTime(item.providerPublishTime) }}</span>
              </div>
            </div>
          </a>
        </div>
      </div>
      <div v-else-if="stockInfo" class="news-placeholder">
        Aucune actualité récente pour cette action.
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

const handleLogoError = (e) => {
  e.target.style.display = 'none'
}

const formatNewsTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp * 1000)
  const now = new Date()
  const diffInHours = Math.floor((now - date) / (1000 * 60 * 60))
  
  if (diffInHours < 1) return 'À l\'instant'
  if (diffInHours < 24) return `Il y a ${diffInHours}h`
  return date.toLocaleDateString()
}

const periods = [
  { label: '1J', value: '1D' },
  { label: '1S', value: '1W' },
  { label: '1M', value: '1M' },
  { label: '3M', value: '3M' },
  { label: '6M', value: '6M' },
  { label: '1A', value: '1Y' },
  { label: 'Tout', value: '5Y' }
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

async function loadStockData() {
  try {
    loading.value = true
    error.value = null
    
    // S'assurer que le symbole est encodé correctement (important pour les points comme AIR.PA)
    const encodedSymbol = encodeURIComponent(symbol)

    const responses = await Promise.all([
      api.get(`/stocks/info/${encodedSymbol}`),
      api.get(`/stocks/info/${encodedSymbol}/history?period=${selectedPeriod.value}`)
    ])
    
    stockInfo.value = responses[0].data
    priceHistory.value = responses[1].data
    
    // Load metrics if any advanced feature is enabled
    const prefs = preferencesStore.preferences
    if (prefs.showAnnualizedReturn || prefs.showBenchmarkComparison || prefs.showBestWorstDay || prefs.showDetailedMetrics) {
      const benchmarkParam = prefs.benchmarkSymbol || 'SPY'
      try {
        const metricsResponse = await api.get(`/stocks/info/${encodedSymbol}/metrics?period=${selectedPeriod.value}&benchmark=${benchmarkParam}`)
        metrics.value = metricsResponse.data
      } catch (mErr) {
        console.warn('Failed to load metrics:', mErr)
      }
    }
  } catch (err) {
    console.error('Error loading stock data:', err)
    error.value = err.response?.data?.message || err.message || 'Impossible de charger les données'
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

onMounted(async () => {
  try {
    // S'assurer que les préférences sont chargées avant tout (important pour formatPrice)
    if (!preferencesStore.preferences.currency || preferencesStore.preferences.currency === 'USD') {
      await preferencesStore.loadPreferences()
    }
  } catch (err) {
    console.warn('Could not load preferences, using defaults:', err)
  }
  
  // Charger les données du stock
  await loadStockData()
})
</script>

<style scoped>
.stock-detail {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5rem 2rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
  text-align: center;
}

.spinner-large {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.error-container h2 {
  margin-bottom: 0.5rem;
  color: #ef4444;
}

.error-container p {
  margin-bottom: 2rem;
  color: #666;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  margin: 0.5rem;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  margin: 0.5rem;
}

.header-section {
  margin-bottom: 2rem;
}

.header-main-title {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.stock-logo-container {
  width: 64px;
  height: 64px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #eee;
}

.stock-logo-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  padding: 4px;
}

.stock-icon-placeholder {
  width: 64px;
  height: 64px;
  background: #f3f4f6;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.header-titles h1 {
  margin: 0;
  font-size: 2rem;
  color: #1e1e2e;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.news-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.news-item:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: #667eea;
}

.news-thumbnail {
  width: 120px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}

.news-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex-grow: 1;
}

.news-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  color: #1e1e2e;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #6b7280;
}

.news-dot {
  color: #d1d5db;
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

.chart-section h2 {
  font-size: 1.25rem;
  margin: 0;
  color: #1e1e2e;
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
}

.period-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.period-btn.active {
  background: #667eea;
  color: white;
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

.no-data {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  color: #6b7280;
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