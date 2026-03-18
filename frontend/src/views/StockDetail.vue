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
          <div class="stock-logo-container">
            <img
              :src="logoSrc"
              :alt="stockInfo?.name"
              @error="onLogoError"
              class="stock-logo-img"
              ref="logoImg"
            />
          </div>
          <div class="header-titles">
            <h1>{{ stockInfo?.name || symbol }}</h1>
            <p v-if="stockInfo" class="stock-symbol-subtitle">
              {{ symbol }}
              <span v-if="stockInfo.exchange"> • {{ stockInfo.exchange }}</span>
              <span v-if="stockInfo.currency"> • {{ stockInfo.currency }}</span>
            </p>
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
            <span class="label">Haut du jour</span>
            <span class="value high-value">{{ stockInfo.highPrice ? preferencesStore.formatPrice(stockInfo.highPrice) : 'N/A' }}</span>
          </div>

          <div class="info-card">
            <span class="label">Bas du jour</span>
            <span class="value low-value">{{ stockInfo.lowPrice ? preferencesStore.formatPrice(stockInfo.lowPrice) : 'N/A' }}</span>
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

          <div v-if="stockInfo.marketCapitalization" class="info-card">
            <span class="label">Capitalisation</span>
            <span class="value">{{ formatMarketCap(stockInfo.marketCapitalization) }}</span>
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
          <div v-if="stockInfo.shareOutstanding" class="info-item">
            <span class="label">Actions en circulation</span>
            <span class="value">{{ formatShares(stockInfo.shareOutstanding) }}</span>
          </div>
          <div v-if="stockInfo.weburl" class="info-item">
            <span class="label">Site web</span>
            <a :href="stockInfo.weburl" target="_blank" rel="noopener" class="weburl-link">{{ stockInfo.weburl }}</a>
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
        
        <!-- Métriques financières -->
        <div v-if="stockInfo?.fundamentals" class="fundamentals-section">
          <h3>Métriques financières</h3>
          <div class="fundamentals-groups">

            <div class="fund-group">
              <h4>Valorisation</h4>
              <div class="fund-grid">
                <div class="fund-item" v-if="stockInfo.fundamentals.peTTM != null">
                  <span class="fund-label">P/E (TTM)</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.peTTM.toFixed(2) }}</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.forwardPE != null">
                  <span class="fund-label">P/E Forward</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.forwardPE.toFixed(2) }}</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.pb != null">
                  <span class="fund-label">P/Book</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.pb.toFixed(2) }}</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.psTTM != null">
                  <span class="fund-label">P/Sales (TTM)</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.psTTM.toFixed(2) }}</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.evEbitdaTTM != null">
                  <span class="fund-label">EV/EBITDA</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.evEbitdaTTM.toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <div class="fund-group">
              <h4>Rentabilité</h4>
              <div class="fund-grid">
                <div class="fund-item" v-if="stockInfo.fundamentals.grossMarginTTM != null">
                  <span class="fund-label">Marge brute</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.grossMarginTTM >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.grossMarginTTM.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.operatingMarginTTM != null">
                  <span class="fund-label">Marge opérat.</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.operatingMarginTTM >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.operatingMarginTTM.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.netProfitMarginTTM != null">
                  <span class="fund-label">Marge nette</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.netProfitMarginTTM >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.netProfitMarginTTM.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.roeTTM != null">
                  <span class="fund-label">ROE</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.roeTTM >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.roeTTM.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.roaTTM != null">
                  <span class="fund-label">ROA</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.roaTTM >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.roaTTM.toFixed(1) }}%</span>
                </div>
              </div>
            </div>

            <div class="fund-group">
              <h4>Croissance</h4>
              <div class="fund-grid">
                <div class="fund-item" v-if="stockInfo.fundamentals.revenueGrowthTTMYoy != null">
                  <span class="fund-label">Revenus YoY</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.revenueGrowthTTMYoy >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.revenueGrowthTTMYoy >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.revenueGrowthTTMYoy.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.revenueGrowth5Y != null">
                  <span class="fund-label">Revenus 5 ans</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.revenueGrowth5Y >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.revenueGrowth5Y >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.revenueGrowth5Y.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.epsGrowthTTMYoy != null">
                  <span class="fund-label">BPA YoY</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.epsGrowthTTMYoy >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.epsGrowthTTMYoy >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.epsGrowthTTMYoy.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.epsGrowth5Y != null">
                  <span class="fund-label">BPA 5 ans</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.epsGrowth5Y >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.epsGrowth5Y >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.epsGrowth5Y.toFixed(1) }}%</span>
                </div>
              </div>
            </div>

            <div class="fund-group">
              <h4>Performance prix</h4>
              <div class="fund-grid">
                <div class="fund-item" v-if="stockInfo.fundamentals.return5D != null">
                  <span class="fund-label">5 jours</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.return5D >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.return5D >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.return5D.toFixed(2) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.return13W != null">
                  <span class="fund-label">13 semaines</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.return13W >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.return13W >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.return13W.toFixed(2) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.return26W != null">
                  <span class="fund-label">26 semaines</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.return26W >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.return26W >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.return26W.toFixed(2) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.return52W != null">
                  <span class="fund-label">52 semaines</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.return52W >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.return52W >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.return52W.toFixed(2) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.returnYTD != null">
                  <span class="fund-label">YTD</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.returnYTD >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.returnYTD >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.returnYTD.toFixed(2) }}%</span>
                </div>
              </div>
            </div>

            <div class="fund-group" v-if="stockInfo.fundamentals.dividendPerShareAnnual || stockInfo.fundamentals.beta != null">
              <h4>Dividende & Risque</h4>
              <div class="fund-grid">
                <div class="fund-item" v-if="stockInfo.fundamentals.dividendPerShareAnnual != null">
                  <span class="fund-label">Dividende/action</span>
                  <span class="fund-value">{{ preferencesStore.formatPrice(stockInfo.fundamentals.dividendPerShareAnnual) }}</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.dividendYieldAnnual != null">
                  <span class="fund-label">Rendement div.</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.dividendYieldAnnual.toFixed(2) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.dividendGrowthRate5Y != null">
                  <span class="fund-label">Croiss. div. 5A</span>
                  <span class="fund-value" :class="stockInfo.fundamentals.dividendGrowthRate5Y >= 0 ? 'pos' : 'neg'">{{ stockInfo.fundamentals.dividendGrowthRate5Y >= 0 ? '+' : '' }}{{ stockInfo.fundamentals.dividendGrowthRate5Y.toFixed(1) }}%</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.beta != null">
                  <span class="fund-label">Bêta</span>
                  <span class="fund-value">{{ stockInfo.fundamentals.beta.toFixed(2) }}</span>
                </div>
                <div class="fund-item" v-if="stockInfo.fundamentals.epsTTM != null">
                  <span class="fund-label">BPA (TTM)</span>
                  <span class="fund-value">{{ preferencesStore.formatPrice(stockInfo.fundamentals.epsTTM) }}</span>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- Données historiques — collapsible, fermé par défaut -->
        <div v-if="stockInfo?.historicalSnapshots?.length" class="collapsible-section">
          <button class="collapsible-header" @click="showHistory = !showHistory">
            <span>Données historiques</span>
            <svg :class="['chevron', { open: showHistory }]" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </button>
          <div v-show="showHistory" class="historical-table-wrapper">
            <table class="historical-table">
              <thead>
                <tr>
                  <th>Période</th>
                  <th>Date</th>
                  <th class="right">Ouverture</th>
                  <th class="right">Haut</th>
                  <th class="right">Bas</th>
                  <th class="right">Clôture</th>
                  <th class="right">Volume</th>
                  <th class="right">Variation</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="snap in stockInfo.historicalSnapshots" :key="snap.period">
                  <td class="period-label">{{ snap.period }}</td>
                  <td class="date-col">{{ snap.date }}</td>
                  <td class="right">{{ snap.open ? preferencesStore.formatPrice(snap.open) : '—' }}</td>
                  <td class="right high-value">{{ snap.high ? preferencesStore.formatPrice(snap.high) : '—' }}</td>
                  <td class="right low-value">{{ snap.low ? preferencesStore.formatPrice(snap.low) : '—' }}</td>
                  <td class="right"><strong>{{ snap.close ? preferencesStore.formatPrice(snap.close) : '—' }}</strong></td>
                  <td class="right">{{ snap.volume ? formatVolume(snap.volume) : '—' }}</td>
                  <td class="right">
                    <span v-if="snap.changePercent != null" :class="snap.changePercent >= 0 ? 'positive' : 'negative'">
                      {{ snap.changePercent >= 0 ? '+' : '' }}{{ snap.changePercent?.toFixed(2) }}%
                    </span>
                    <span v-else>—</span>
                  </td>
                </tr>
              </tbody>
            </table>
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
const loading = ref(true)
const refreshing = ref(false)
const error = ref(null)
const selectedPeriod = ref('1M')
const showHistory = ref(false)
const logoImg = ref(null)
const logoFallbackUsed = ref(false)

// Logo : CDN statique Finnhub en priorité, puis logoUrl du backend
const logoSrc = computed(() => {
  if (!logoFallbackUsed.value) {
    return `https://static2.finnhub.io/file/publicdatany/finnhubimage/stock_logo/${symbol}.png`
  }
  return stockInfo.value?.logoUrl || ''
})

function onLogoError() {
  if (!logoFallbackUsed.value && stockInfo.value?.logoUrl) {
    logoFallbackUsed.value = true // bascule sur logoUrl du backend
  } else {
    // Plus de fallback : cacher l'image et montrer le placeholder
    if (logoImg.value) logoImg.value.style.display = 'none'
  }
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
  if (volume >= 1000000000) return (volume / 1000000000).toFixed(2) + 'B'
  if (volume >= 1000000) return (volume / 1000000).toFixed(2) + 'M'
  if (volume >= 1000) return (volume / 1000).toFixed(2) + 'K'
  return volume.toLocaleString()
}

// marketCapitalization from Finnhub is in millions USD
function formatMarketCap(cap) {
  if (!cap) return 'N/A'
  const capM = cap // already in millions
  if (capM >= 1000000) return (capM / 1000000).toFixed(2) + 'T'
  if (capM >= 1000) return (capM / 1000).toFixed(2) + 'B'
  return capM.toFixed(0) + 'M'
}

// shareOutstanding from Finnhub is in millions
function formatShares(shares) {
  if (!shares) return 'N/A'
  if (shares >= 1000) return (shares / 1000).toFixed(2) + 'B'
  return shares.toFixed(2) + 'M'
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
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.10);
  border: 1px solid #eee;
  flex-shrink: 0;
}

.stock-logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 8px;
}

.stock-icon-placeholder {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  flex-shrink: 0;
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

.weburl-link {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
  word-break: break-all;
}

.weburl-link:hover {
  text-decoration: underline;
}

.historical-table-wrapper {
  overflow-x: auto;
}

.historical-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.historical-table th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-size: 0.78rem;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e5e7eb;
}

.historical-table th.right {
  text-align: right;
}

.historical-table td {
  padding: 0.875rem 1rem;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

.historical-table td.right {
  text-align: right;
}

.historical-table tbody tr:hover {
  background: #f9fafb;
}

.historical-table tbody tr:last-child td {
  border-bottom: none;
}

.period-label {
  font-weight: 600;
  color: #1e1e2e;
}

.date-col {
  color: #6b7280;
  font-size: 0.85rem;
}

.positive {
  color: #10b981;
  font-weight: 600;
}

.negative {
  color: #ef4444;
  font-weight: 600;
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

.fundamentals-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.fundamentals-section h3 {
  margin: 0 0 1.25rem 0;
  color: #1e1e2e;
  font-size: 1.1rem;
  font-weight: 600;
}

.fundamentals-groups {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.fund-group h4 {
  margin: 0 0 0.75rem 0;
  font-size: 0.8rem;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.6px;
}

.fund-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.75rem;
}

.fund-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  background: #f9fafb;
  padding: 0.75rem 1rem;
  border-radius: 8px;
}

.fund-label {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.fund-value {
  font-size: 1rem;
  font-weight: 700;
  color: #1e1e2e;
}

.fund-value.pos {
  color: #10b981;
}

.fund-value.neg {
  color: #ef4444;
}

.collapsible-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
  overflow: hidden;
}

.collapsible-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 1.25rem 1.5rem;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e1e2e;
  text-align: left;
  transition: background 0.2s;
}

.collapsible-header:hover {
  background: #f9fafb;
}

.chevron {
  transition: transform 0.25s ease;
  flex-shrink: 0;
  color: #6b7280;
}

.chevron.open {
  transform: rotate(180deg);
}
</style>