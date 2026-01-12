<template>
  <div>
    <Navbar />
    
    <div class="profile-container">
      <h1>Profil et Préférences</h1>
      
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../components/Navbar.vue'
import { usePreferencesStore } from '../stores/preferences'

const preferencesStore = usePreferencesStore()
const preferences = ref({
  showAnnualizedReturn: false,
  showBenchmarkComparison: false,
  showBestWorstDay: false,
  showStartPriceLine: false,
  benchmarkSymbol: 'SPY',
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

@media (max-width: 768px) {
  .preferences-grid {
    grid-template-columns: 1fr;
  }
}
</style>
