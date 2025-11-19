<template>
  <div>
    <Navbar />
    
    <div class="portfolios-container">
      <div class="header">
        <h1>My Portfolios</h1>
        <button @click="showCreateModal = true" class="btn-primary">
          + New Portfolio
        </button>
      </div>

      <div v-if="portfolioStore.loading" class="loading">Loading portfolios...</div>

      <div v-else-if="portfolioStore.portfolios.length" class="portfolio-grid">
        <div 
          v-for="portfolio in portfolioStore.portfolios" 
          :key="portfolio.id"
          class="portfolio-card"
        >
          <div class="portfolio-header">
            <div class="portfolio-title">
              <span class="portfolio-icon">{{ portfolio.icon || '💼' }}</span>
              <h3>{{ portfolio.name }}</h3>
            </div>
            <div class="portfolio-actions">
              <button @click="editPortfolio(portfolio)" class="btn-icon-modern" title="Edit">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
              </button>
              <button @click="deletePortfolioConfirm(portfolio.id)" class="btn-icon-modern delete" title="Delete">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
              </button>
            </div>
          </div>
          
          <p v-if="portfolio.description" class="portfolio-description">
            {{ portfolio.description }}
          </p>

          <div class="portfolio-stats">
            <div class="stat">
              <span class="label">Stocks:</span>
              <span class="value">{{ portfolio.stocks?.length || 0 }}</span>
            </div>
          </div>

          <button @click="$router.push(`/portfolios/${portfolio.id}`)" class="btn-view">
            View Portfolio →
          </button>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>No portfolios yet. Create your first portfolio to get started!</p>
      </div>

      <Modal :show="showCreateModal" :title="editingPortfolio ? 'Edit Portfolio' : 'Create Portfolio'" @close="closeModal">
        <form @submit.prevent="savePortfolio">
          <div class="form-group">
            <label for="name">Portfolio Name</label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              required
              placeholder="e.g., Tech Stocks"
            />
          </div>

          <div class="form-group">
            <label for="description">Description (optional)</label>
            <textarea
              id="description"
              v-model="form.description"
              rows="3"
              placeholder="Describe this portfolio..."
            ></textarea>
          </div>

          <div class="form-group">
            <label>Icon</label>
            <IconPicker v-model="form.icon" />
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="portfolioStore.loading">
              {{ editingPortfolio ? 'Update' : 'Create' }}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../components/Navbar.vue'
import Modal from '../components/Modal.vue'
import IconPicker from '../components/IconPicker.vue'
import { usePortfolioStore } from '../stores/portfolio'

const portfolioStore = usePortfolioStore()

const showCreateModal = ref(false)
const editingPortfolio = ref(null)
const form = ref({
  name: '',
  description: '',
  icon: '💼'
})

onMounted(() => {
  portfolioStore.fetchPortfolios()
})

function editPortfolio(portfolio) {
  editingPortfolio.value = portfolio
  form.value = {
    name: portfolio.name,
    description: portfolio.description || '',
    icon: portfolio.icon || '💼'
  }
  showCreateModal.value = true
}

async function savePortfolio() {
  try {
    if (editingPortfolio.value) {
      await portfolioStore.updatePortfolio(editingPortfolio.value.id, form.value)
    } else {
      await portfolioStore.createPortfolio(form.value)
    }
    closeModal()
  } catch (error) {
    console.error('Failed to save portfolio:', error)
  }
}

async function deletePortfolioConfirm(id) {
  if (confirm('Are you sure you want to delete this portfolio?')) {
    try {
      await portfolioStore.deletePortfolio(id)
    } catch (error) {
      console.error('Failed to delete portfolio:', error)
    }
  }
}

function closeModal() {
  showCreateModal.value = false
  editingPortfolio.value = null
  form.value = {
    name: '',
    description: '',
    icon: '💼'
  }
}
</script>

<style scoped>
.portfolios-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

h1 {
  margin: 0;
  color: #333;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
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

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.portfolio-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.portfolio-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.portfolio-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.portfolio-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.portfolio-icon {
  font-size: 2rem;
}

.portfolio-title h3 {
  margin: 0;
  color: #333;
  font-size: 1.25rem;
}

.portfolio-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon-modern {
  background: #f0f0f0;
  border: 1px solid #e0e0e0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.btn-icon-modern:hover {
  background: #667eea;
  border-color: #667eea;
  color: white;
  transform: translateY(-2px);
}

.btn-icon-modern.delete:hover {
  background: #ef4444;
  border-color: #ef4444;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem;
  transition: transform 0.2s;
}

.btn-icon:hover {
  transform: scale(1.2);
}

.portfolio-description {
  color: #666;
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
}

.portfolio-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding: 1rem 0;
  border-top: 1px solid #e0e0e0;
  border-bottom: 1px solid #e0e0e0;
}

.stat {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 0.9rem;
}

.stat .value {
  font-weight: 600;
  color: #333;
}

.btn-view {
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

.btn-view:hover {
  opacity: 0.9;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: #666;
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
