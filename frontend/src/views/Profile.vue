<template>
  <div>
    <Navbar />
    
    <div class="profile-container">
      <h1>Profil et Préférences</h1>
      
      <div class="preferences-section">
         <h2>💱 Devise</h2>
         <p class="section-description">
           Sélectionnez votre devise de référence pour tous les prix et valeurs
         </p>
         
         <div class="currency-selector">
           <select 
             v-model="preferences.currency"
             @change="savePreferences"
             class="currency-dropdown"
           >
             <option value="USD">USD - Dollar américain</option>
             <option value="EUR">EUR - Euro</option>
             <option value="GBP">GBP - Livre sterling</option>
             <option value="JPY">JPY - Yen japonais</option>
             <option value="CHF">CHF - Franc suisse</option>
             <option value="CAD">CAD - Dollar canadien</option>
             <option value="AUD">AUD - Dollar australien</option>
           </select>
         </div>
         
         <div v-if="saveMessage" class="save-message" :class="{ error: saveError }">
           {{ saveMessage }}
         </div>
       </div>
       
       <div class="preferences-section">
         <h2>📊 Préférences d'affichage des graphiques</h2>
        <p class="section-description">
          Personnalisez les métriques et informations affichées sur les graphiques d'actions
        </p>
        
        <div class="preferences-grid">
          <div class="preference-card">
            <div class="preference-header">
              <div class="preference-icon">📈</div>
              <div class="preference-info">
                <h3>Rendement annualisé</h3>
                <p>Affiche le rendement annualisé de l'action sur la période sélectionnée</p>
              </div>
            </div>
            <label class="toggle-switch">
              <input 
                type="checkbox" 
                v-model="preferences.showAnnualizedReturn"
                @change="savePreferences"
              />
              <span class="slider"></span>
            </label>
          </div>
          
          <div class="preference-card">
            <div class="preference-header">
              <div class="preference-icon">📊</div>
              <div class="preference-info">
                <h3>Comparaison avec indice de référence</h3>
                <p>Compare la performance avec un indice (par défaut S&P 500)</p>
              </div>
            </div>
            <label class="toggle-switch">
              <input 
                type="checkbox" 
                v-model="preferences.showBenchmarkComparison"
                @change="savePreferences"
              />
              <span class="slider"></span>
            </label>
          </div>
          
          <div class="preference-card" :class="{ disabled: !preferences.showBenchmarkComparison }">
            <div class="preference-header">
              <div class="preference-icon">🎯</div>
              <div class="preference-info">
                <h3>Indice de référence</h3>
                <p>Symbole de l'indice de comparaison</p>
              </div>
            </div>
            <input 
              type="text" 
              v-model="preferences.benchmarkSymbol"
              @blur="savePreferences"
              :disabled="!preferences.showBenchmarkComparison"
              class="benchmark-input"
              placeholder="SPY"
            />
          </div>
          
          <div class="preference-card">
            <div class="preference-header">
              <div class="preference-icon">⬆️⬇️</div>
              <div class="preference-info">
                <h3>Meilleur / Pire jour</h3>
                <p>Affiche le meilleur et le pire jour de performance sur la période</p>
              </div>
            </div>
            <label class="toggle-switch">
              <input 
                type="checkbox" 
                v-model="preferences.showBestWorstDay"
                @change="savePreferences"
              />
              <span class="slider"></span>
            </label>
          </div>
          
          <div class="preference-card">
            <div class="preference-header">
              <div class="preference-icon">📍</div>
              <div class="preference-info">
                <h3>Ligne de prix de départ</h3>
                <p>Affiche une ligne horizontale au prix de départ de la période</p>
              </div>
            </div>
            <label class="toggle-switch">
              <input 
                type="checkbox" 
                v-model="preferences.showStartPriceLine"
                @change="savePreferences"
              />
              <span class="slider"></span>
            </label>
          </div>
          
          <div class="preference-card">
            <div class="preference-header">
              <div class="preference-icon">🏷️</div>
              <div class="preference-info">
                <h3>Badge de performance</h3>
                <p>Affiche le pourcentage de gain/perte sur le bouton de période actif</p>
              </div>
            </div>
            <label class="toggle-switch">
              <input 
                type="checkbox" 
                v-model="preferences.showPerformanceBadge"
                @change="savePreferences"
              />
              <span class="slider"></span>
            </label>
          </div>
          
          <div class="preference-card">
            <div class="preference-header">
              <div class="preference-icon">📋</div>
              <div class="preference-info">
                <h3>Métriques détaillées</h3>
                <p>Affiche toutes les métriques au-dessus du graphique</p>
              </div>
            </div>
            <label class="toggle-switch">
              <input 
                type="checkbox" 
                v-model="preferences.showDetailedMetrics"
                @change="savePreferences"
              />
              <span class="slider"></span>
            </label>
          </div>
        </div>
        
        <div v-if="saveMessage" class="save-message" :class="{ error: saveError }">
          {{ saveMessage }}
        </div>
      </div>

      <div class="preferences-section export-section">
        <h2>📥 Export des données</h2>
        <p class="section-description">
          Téléchargez vos portfolios au format CSV. Choisissez les colonnes à inclure.
        </p>

        <div class="export-columns-grid">
          <label
            v-for="col in exportColumns"
            :key="col.key"
            class="export-col-toggle"
            :class="{ active: col.selected }"
          >
            <input type="checkbox" v-model="col.selected" />
            <span class="col-check">
              <svg v-if="col.selected" xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            </span>
            <span class="col-label">{{ col.label }}</span>
          </label>
        </div>

        <div class="export-footer">
          <span class="export-count">{{ exportColumns.filter(c => c.selected).length }} colonne(s) sélectionnée(s)</span>
          <button @click="exportCSV" :disabled="exporting || exportColumns.every(c => !c.selected)" class="btn-export">
            <svg v-if="!exporting" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            <svg v-else class="spinner" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"/><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"/><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"/>
            </svg>
            {{ exporting ? 'Export en cours...' : 'Exporter en CSV' }}
          </button>
        </div>
        <div v-if="exportError" class="save-message error">{{ exportError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../components/Navbar.vue'
import { usePreferencesStore } from '../stores/preferences'
import { usePortfolioStore } from '../stores/portfolio'
import api from '../services/api'

const preferencesStore = usePreferencesStore()
const portfolioStore = usePortfolioStore()

const exporting = ref(false)
const exportError = ref('')

const exportColumns = ref([
  { key: 'portfolio',          label: 'Portfolio',         selected: true },
  { key: 'symbol',             label: 'Symbole',           selected: true },
  { key: 'name',               label: 'Nom',               selected: true },
  { key: 'type',               label: 'Type',              selected: false },
  { key: 'quantity',           label: 'Quantité',          selected: true },
  { key: 'purchasePrice',      label: 'Prix moyen achat',  selected: true },
  { key: 'purchaseDate',       label: "Date d'achat",      selected: false },
  { key: 'currentPrice',       label: 'Prix actuel',       selected: false },
  { key: 'currentValue',       label: 'Valeur totale',     selected: true },
  { key: 'gainLoss',           label: 'Gain/Perte',        selected: true },
  { key: 'gainLossPercentage', label: 'Gain/Perte (%)',    selected: true },
  { key: 'currency',           label: 'Devise',            selected: false },
])

function getStockValue(key, s, portfolioName, currency) {
  switch (key) {
    case 'portfolio':          return escapeCsv(portfolioName)
    case 'symbol':             return escapeCsv(s.stock?.symbol ?? '')
    case 'name':               return escapeCsv(s.stock?.name ?? '')
    case 'type':               return escapeCsv(s.stock?.type ?? '')
    case 'quantity':           return formatNum(s.quantity)
    case 'purchasePrice':      return formatNum(s.purchasePrice)
    case 'purchaseDate':       return s.purchaseDate ? new Date(s.purchaseDate).toLocaleDateString('fr-FR') : ''
    case 'currentPrice':       return formatNum(s.currentPrice)
    case 'currentValue':       return formatNum(s.currentValue)
    case 'gainLoss':           return formatNum(s.gainLoss)
    case 'gainLossPercentage': return formatNum(s.gainLossPercentage)
    case 'currency':           return currency
    default:                   return ''
  }
}

async function exportCSV() {
  exporting.value = true
  exportError.value = ''
  try {
    const portfolios = await portfolioStore.fetchPortfolios()
    const detailed = await Promise.all(
      portfolios.map(p => api.get(`/portfolios/${p.id}`).then(r => r.data))
    )

    const currency = preferencesStore.preferences?.currency || 'USD'
    const now = new Date().toLocaleString('fr-FR')
    const active = exportColumns.value.filter(c => c.selected)
    const headers = active.map(c => c.label)

    const rows = []
    for (const portfolio of detailed) {
      const stocks = portfolio.stocks || []
      if (stocks.length === 0) {
        rows.push(active.map(col => col.key === 'portfolio' ? escapeCsv(portfolio.name) : ''))
      } else {
        for (const s of stocks) {
          rows.push(active.map(col => getStockValue(col.key, s, portfolio.name, currency)))
        }
      }
    }

    const csvContent = [
      `# Export Capitalot — ${now}`,
      headers.join(';'),
      ...rows.map(r => r.join(';'))
    ].join('\n')

    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `capitalot_export_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Export failed:', e)
    exportError.value = 'Erreur lors de l\'export. Veuillez réessayer.'
  } finally {
    exporting.value = false
  }
}

function escapeCsv(val) {
  if (val == null) return ''
  const str = String(val)
  return str.includes(';') || str.includes('"') || str.includes('\n')
    ? `"${str.replace(/"/g, '""')}"`
    : str
}

function formatNum(val) {
  if (val == null) return ''
  return Number(val).toFixed(2).replace('.', ',')
}
const preferences = ref({
  showAnnualizedReturn: false,
  showBenchmarkComparison: false,
  showBestWorstDay: false,
  showStartPriceLine: false,
  benchmarkSymbol: 'SPY',
  currency: 'USD',
  showPerformanceBadge: true,
  showDetailedMetrics: true
})

const saveMessage = ref('')
const saveError = ref(false)
let saveTimeout = null

onMounted(async () => {
  await preferencesStore.loadPreferences()
  preferences.value = { ...preferencesStore.preferences }
})

async function savePreferences() {
  const success = await preferencesStore.updatePreferences(preferences.value)
  if (success) {
    showSaveMessage('Préférences sauvegardées ✓', false)
  } else {
    showSaveMessage('Erreur lors de la sauvegarde', true)
  }
}

function showSaveMessage(message, isError) {
  saveMessage.value = message
  saveError.value = isError
  
  if (saveTimeout) {
    clearTimeout(saveTimeout)
  }
  
  saveTimeout = setTimeout(() => {
    saveMessage.value = ''
    saveError.value = false
  }, 3000)
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin: 0 0 2rem 0;
  color: #333;
  font-size: 2.5rem;
}

.preferences-section {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.preferences-section h2 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.5rem;
}

.section-description {
  color: #666;
  margin: 0 0 2rem 0;
  font-size: 0.95rem;
}

.preferences-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.preference-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px solid #e0e0e0;
  transition: all 0.3s;
}

.preference-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.preference-card.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.preference-header {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  flex: 1;
}

.preference-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.preference-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  color: #333;
}

.preference-info p {
  margin: 0;
  font-size: 0.875rem;
  color: #666;
  line-height: 1.4;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 34px;
  flex-shrink: 0;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 34px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.benchmark-input {
  width: 100px;
  padding: 0.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.9rem;
  text-align: center;
  font-weight: 600;
  transition: border-color 0.3s;
}

.benchmark-input:focus {
  outline: none;
  border-color: #667eea;
}

.benchmark-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.currency-selector {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px solid #e0e0e0;
}

.currency-dropdown {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s;
}

.currency-dropdown:focus {
  outline: none;
  border-color: #667eea;
}

.currency-dropdown:hover {
  border-color: #667eea;
}

.save-message {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #d1fae5;
  border-left: 4px solid #10b981;
  border-radius: 8px;
  color: #065f46;
  font-weight: 600;
  text-align: center;
}

.save-message.error {
  background: #fee2e2;
  border-left-color: #ef4444;
  color: #991b1b;
}

.export-section {
  margin-top: 2rem;
}

.export-columns-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-bottom: 1.5rem;
}

.export-col-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.875rem;
  border-radius: 20px;
  border: 2px solid #e0e0e0;
  background: #f8f9fa;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  color: #666;
  transition: all 0.15s;
  user-select: none;
}

.export-col-toggle input {
  display: none;
}

.export-col-toggle:hover {
  border-color: #667eea;
  color: #667eea;
}

.export-col-toggle.active {
  background: #eef0fc;
  border-color: #667eea;
  color: #667eea;
}

.col-check {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 2px solid #d0d0d0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s;
}

.export-col-toggle.active .col-check {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.export-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.export-count {
  font-size: 0.875rem;
  color: #888;
}

.btn-export {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.85rem 1.75rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  white-space: nowrap;
  transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
  flex-shrink: 0;
}

.btn-export:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.4);
}

.btn-export:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .preferences-grid {
    grid-template-columns: 1fr;
  }

  .export-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .btn-export {
    width: 100%;
    justify-content: center;
  }
}
</style>
