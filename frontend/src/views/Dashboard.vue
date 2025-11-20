<template>
  <div>
    <Navbar />
    
    <div class="dashboard-container">
      <h1>Dashboard</h1>

      <div v-if="loading" class="loading">Loading stats...</div>

      <div v-else class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">💰</div>
          <div class="stat-info">
            <div class="stat-label">Total Invested</div>
            <div class="stat-value">${{ formatNumber(allTimeStats?.totalInvested) }}</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📊</div>
          <div class="stat-info">
            <div class="stat-label">Current Value</div>
            <div class="stat-value">${{ formatNumber(allTimeStats?.currentValue) }}</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📈</div>
          <div class="stat-info">
            <div class="stat-label">Monthly Gain/Loss</div>
            <div :class="['stat-value', (monthlyStats?.totalGainLoss || 0) >= 0 ? 'positive' : 'negative']">
              ${{ formatNumber(monthlyStats?.totalGainLoss) }}
              <span class="stat-percent">({{ formatPercent(monthlyStats?.totalGainLossPercent) }}%)</span>
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🚀</div>
          <div class="stat-info">
            <div class="stat-label">Yearly Gain/Loss</div>
            <div :class="['stat-value', (yearlyStats?.totalGainLoss || 0) >= 0 ? 'positive' : 'negative']">
              ${{ formatNumber(yearlyStats?.totalGainLoss) }}
              <span class="stat-percent">({{ formatPercent(yearlyStats?.totalGainLossPercent) }}%)</span>
            </div>
          </div>
        </div>
      </div>

      <div class="performance-section">
        <div class="section-header">
          <h2>Portfolio Performance</h2>
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
        
        <div v-if="loadingChart" class="loading">Loading chart...</div>
        <div v-else-if="performanceData.length" class="chart-card">
          <PerformanceChart :data="performanceData" label="Total Value" :selectedRange="selectedPeriod" />
        </div>
        <div v-else class="empty-chart">No performance data available</div>
      </div>

      <div class="section">
        <div class="section-header">
          <h2>My Portfolios</h2>
          <router-link to="/portfolios" class="btn-view-all">View All</router-link>
        </div>

        <div v-if="portfolioStore.loading" class="loading">Loading portfolios...</div>

        <div v-else-if="portfolioStore.portfolios.length" class="portfolio-grid">
          <div 
            v-for="portfolio in portfolioStore.portfolios.slice(0, 4)" 
            :key="portfolio.id"
            class="portfolio-card"
            @click="$router.push(`/portfolios/${portfolio.id}`)"
          >
            <div class="portfolio-header">
              <span class="portfolio-icon">{{ portfolio.icon || '💼' }}</span>
              <h3>{{ portfolio.name }}</h3>
            </div>
            <div class="portfolio-stats">
              <div class="portfolio-stat">
                <span class="label">Stocks:</span>
                <span class="value">{{ portfolio.stocks?.length || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <p>No portfolios yet.</p>
          <router-link to="/portfolios" class="btn-primary">Create Your First Portfolio</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import Navbar from '../components/Navbar.vue'
import PerformanceChart from '../components/PerformanceChart.vue'
import { usePortfolioStore } from '../stores/portfolio'
import api from '../services/api'

const portfolioStore = usePortfolioStore()

const loading = ref(false)
const allTimeStats = ref(null)
const monthlyStats = ref(null)
const yearlyStats = ref(null)
const selectedPeriod = ref('1M')
const performanceData = ref([])
const loadingChart = ref(false)

const periods = [
  { label: '1D', value: '1D' },
  { label: '1W', value: '1W' },
  { label: '1M', value: '1M' },
  { label: '3M', value: '3M' },
  { label: '6M', value: '6M' },
  { label: '1Y', value: '1Y' },
  { label: 'ALL', value: '5Y' }
]

onMounted(async () => {
  await Promise.all([
    fetchAllStats(),
    portfolioStore.fetchPortfolios(),
    fetchPerformanceData()
  ])
})

watch(selectedPeriod, () => {
  fetchPerformanceData()
})

async function fetchAllStats() {
  loading.value = true
  try {
    const [allTime, monthly, yearly] = await Promise.all([
      api.get('/stats/all-time'),
      api.get('/stats/monthly'),
      api.get('/stats/yearly')
    ])
    allTimeStats.value = allTime.data
    monthlyStats.value = monthly.data
    yearlyStats.value = yearly.data
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  } finally {
    loading.value = false
  }
}

async function fetchPerformanceData() {
  loadingChart.value = true
  try {
    const response = await api.get(`/stats/performance-history?period=${selectedPeriod.value}`)
    performanceData.value = response.data
  } catch (error) {
    console.error('Failed to fetch performance data:', error)
    performanceData.value = []
  } finally {
    loadingChart.value = false
  }
}

function formatNumber(num) {
  return num?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

function formatPercent(num) {
  return num?.toFixed(2) || '0.00'
}
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin: 0 0 2rem 0;
  color: #333;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 3rem;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
}

.stat-percent {
  font-size: 0.9rem;
  margin-left: 0.25rem;
}

.stat-value.positive {
  color: #10b981;
}

.stat-value.negative {
  color: #ef4444;
}

.performance-section {
  margin-bottom: 3rem;
}

.section {
  margin-top: 3rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.time-selector {
  display: flex;
  gap: 0.5rem;
  background: white;
  padding: 0.5rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.period-btn {
  padding: 0.5rem 1rem;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  color: #666;
  transition: all 0.3s;
}

.period-btn:hover {
  background: #f5f5f5;
}

.period-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-chart {
  background: white;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
  color: #666;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-view-all {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s;
}

.btn-view-all:hover {
  color: #764ba2;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.portfolio-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.portfolio-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.portfolio-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.portfolio-icon {
  font-size: 2rem;
}

.portfolio-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.25rem;
}

.portfolio-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.portfolio-stat {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 0.9rem;
}

.portfolio-stat .value {
  font-weight: 600;
  color: #333;
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

.btn-primary {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}
</style>
