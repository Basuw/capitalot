<template>
  <div>
    <Navbar />
    
    <div class="watchlist-container">
      <div class="header">
        <h1>Watchlists</h1>
        <button @click="showCreateWatchlistModal = true" class="btn-primary">
          + Create Watchlist
        </button>
      </div>

      <div v-if="watchlistStore.loading" class="loading">Loading watchlists...</div>

      <div v-else-if="watchlistStore.watchlists.length" class="watchlists-section">
        <div
          v-for="watchlist in watchlistStore.watchlists"
          :key="watchlist.id"
          class="watchlist-card"
        >
          <div class="watchlist-header" @click="toggleWatchlist(watchlist.id)">
            <div class="watchlist-title">
              <span class="watchlist-icon">{{ watchlist.icon || '📋' }}</span>
              <div class="watchlist-info">
                <h3>{{ watchlist.name }}</h3>
                <p v-if="watchlist.description" class="watchlist-description">{{ watchlist.description }}</p>
              </div>
            </div>
            <div class="watchlist-meta">
              <span class="item-count">{{ getWatchlistItemCount(watchlist.id) }} items</span>
              <button @click.stop="deleteWatchlistConfirm(watchlist.id)" class="btn-icon-small delete" title="Delete Watchlist">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
              </button>
              <button @click.stop="showAddStockModal(watchlist.id)" class="btn-add-stock" title="Add Stock">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
              </button>
              <span class="expand-icon">{{ expandedWatchlists[watchlist.id] ? '▼' : '▶' }}</span>
            </div>
          </div>

          <div v-if="expandedWatchlists[watchlist.id]" class="watchlist-content">
            <div v-if="loadingItems[watchlist.id]" class="loading-items">Loading items...</div>
            
            <div v-else-if="watchlistItems[watchlist.id]?.length" class="watchlist-items">
              <div class="filter-controls">
                <label>Filter by tag:</label>
                <select v-model="filterTags[watchlist.id]" class="filter-select">
                  <option value="">All Tags</option>
                  <option v-for="tag in getAvailableTags(watchlist.id)" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
                </select>
              </div>

              <div class="watchlist-table">
                <table>
                  <thead>
                    <tr>
                      <th @click="handleSort('symbol', watchlist.id)" class="sortable">
                        Symbol
                        <span class="sort-indicator" v-if="sortConfig[watchlist.id]?.by === 'symbol'">{{ sortConfig[watchlist.id]?.order === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th>Name</th>
                      <th>Type</th>
                      <th @click="handleSort('price', watchlist.id)" class="sortable">
                        Current Price
                        <span class="sort-indicator" v-if="sortConfig[watchlist.id]?.by === 'price'">{{ sortConfig[watchlist.id]?.order === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th @click="handleSort('targetPrice', watchlist.id)" class="sortable">
                        Target Price
                        <span class="sort-indicator" v-if="sortConfig[watchlist.id]?.by === 'targetPrice'">{{ sortConfig[watchlist.id]?.order === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th @click="handleSort('priority', watchlist.id)" class="sortable">
                        Priority
                        <span class="sort-indicator" v-if="sortConfig[watchlist.id]?.by === 'priority'">{{ sortConfig[watchlist.id]?.order === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th>Tags</th>
                      <th>Notes</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr 
                      v-for="item in getFilteredAndSortedItems(watchlist.id)" 
                      :key="item.id"
                      @click="goToStock(item.stock.symbol)"
                      class="clickable-row"
                    >
                      <td class="symbol">
                        <span class="stock-link">{{ item.stock.symbol }}</span>
                      </td>
                      <td>{{ item.stock.name }}</td>
                      <td>
                        <span v-if="item.stock?.type" :class="['type-badge', item.stock.type.toLowerCase()]">{{ item.stock.type }}</span>
                        <span v-else>-</span>
                      </td>
                      <td>
                        <div class="price-column">
                          <div class="price">${{ formatNumber(item.stock.currentPrice) }}</div>
                          <div 
                            v-if="item.stock.dailyChange !== undefined" 
                            :class="['daily-change', item.stock.dailyChange >= 0 ? 'positive' : 'negative']"
                          >
                            {{ item.stock.dailyChange >= 0 ? '+' : '' }}${{ formatNumber(Math.abs(item.stock.dailyChange)) }}
                            ({{ item.stock.dailyChange >= 0 ? '+' : '' }}{{ formatPercent(item.stock.dailyChangePercentage) }}%)
                          </div>
                        </div>
                      </td>
                      <td @click.stop="startEdit(item.id, 'targetPrice')" class="editable">
                        <span v-if="editingCell?.id !== item.id || editingCell?.field !== 'targetPrice'">
                          ${{ formatNumber(item.targetPrice) }}
                        </span>
                        <input
                          v-else
                          v-model.number="editValue"
                          @blur="saveEdit(item)"
                          @keyup.enter="saveEdit(item)"
                          @keyup.esc="cancelEdit"
                          type="number"
                          step="0.01"
                          class="inline-edit"
                          ref="editInput"
                        />
                      </td>
                      <td @click.stop="startEdit(item.id, 'priority')" class="editable">
                        <span v-if="editingCell?.id !== item.id || editingCell?.field !== 'priority'" :class="['priority-badge', item.priority?.toLowerCase() || 'medium']">
                          {{ item.priority || 'MEDIUM' }}
                        </span>
                        <select
                          v-else
                          v-model="editValue"
                          @change="saveEdit(item)"
                          @blur="saveEdit(item)"
                          @keyup.esc="cancelEdit"
                          class="inline-edit-select"
                          ref="editInput"
                        >
                          <option value="HIGH">High</option>
                          <option value="MEDIUM">Medium</option>
                          <option value="LOW">Low</option>
                        </select>
                      </td>
                      <td @click.stop="startEdit(item.id, 'tags')" class="editable tags-cell">
                        <div v-if="editingCell?.id !== item.id || editingCell?.field !== 'tags'" class="tags-display">
                          <span v-for="tag in item.tags" :key="tag.id" class="tag-badge">
                            {{ tag.name }}
                          </span>
                          <span v-if="!item.tags || item.tags.length === 0" class="empty-tags">Click to add tags</span>
                        </div>
                        <div v-else class="tags-edit-container">
                          <input
                            v-model="tagInput"
                            @keyup.enter="addTag(item)"
                            @blur="saveTagsEdit(item)"
                            @keyup.esc="cancelEdit"
                            placeholder="Type and press Enter"
                            class="inline-edit tags-input"
                            ref="editInput"
                          />
                          <div class="current-tags-edit">
                            <span v-for="tag in editTags" :key="tag.id || tag.name" class="tag-badge editable">
                              {{ tag.name }}
                              <button @click.stop="removeTag(tag)" class="remove-tag">×</button>
                            </span>
                          </div>
                          <div v-if="filteredTagSuggestions.length" class="tag-suggestions">
                            <div
                              v-for="tag in filteredTagSuggestions"
                              :key="tag.id"
                              @mousedown.prevent="selectTagSuggestion(tag, item)"
                              class="tag-suggestion"
                            >
                              {{ tag.name }}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td @click.stop="startEdit(item.id, 'notes')" class="editable notes">
                        <span v-if="editingCell?.id !== item.id || editingCell?.field !== 'notes'">
                          {{ item.notes || '-' }}
                        </span>
                        <input
                          v-else
                          v-model="editValue"
                          @blur="saveEdit(item)"
                          @keyup.enter="saveEdit(item)"
                          @keyup.esc="cancelEdit"
                          class="inline-edit"
                          ref="editInput"
                        />
                      </td>
                      <td @click.stop>
                        <button @click="removeItemConfirm(item.id, watchlist.id)" class="btn-icon-modern delete" title="Remove">
                          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div v-else class="empty-items">
              <p>No items in this watchlist yet.</p>
              <button @click="showAddStockModal(watchlist.id)" class="btn-secondary">Add Stock</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>You don't have any watchlists yet.</p>
        <button @click="showCreateWatchlistModal = true" class="btn-primary">Create Your First Watchlist</button>
      </div>

      <Modal :show="showCreateWatchlistModal" title="Create Watchlist" @close="closeCreateWatchlistModal">
        <form @submit.prevent="createWatchlist">
          <div class="form-group">
            <label for="watchlistName">Watchlist Name</label>
            <input
              id="watchlistName"
              v-model="watchlistForm.name"
              type="text"
              placeholder="e.g., Tech Stocks"
              required
            />
          </div>

          <div class="form-group">
            <label for="watchlistIcon">Icon</label>
            <input
              id="watchlistIcon"
              v-model="watchlistForm.icon"
              type="text"
              placeholder="📋"
              maxlength="2"
            />
          </div>

          <div class="form-group">
            <label for="watchlistDescription">Description (optional)</label>
            <textarea
              id="watchlistDescription"
              v-model="watchlistForm.description"
              rows="3"
              placeholder="Describe what this watchlist is for..."
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeCreateWatchlistModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="watchlistStore.loading || !watchlistForm.name.trim()">
              Create Watchlist
            </button>
          </div>
        </form>
      </Modal>

      <Modal :show="showAddModal" :title="editingItem ? 'Edit Watchlist Item' : 'Add to Watchlist'" @close="closeAddStockModal">
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label for="search">Search Stock</label>
            <input
              id="search"
              v-model="searchQuery"
              @input="searchStocks"
              type="text"
              placeholder="Search by symbol or name..."
              autocomplete="off"
              :disabled="!!editingItem"
            />
            <div v-if="searchResults.length && !selectedStock && !editingItem" class="search-results">
              <div 
                v-for="stock in searchResults" 
                :key="stock.id"
                @click="selectStock(stock)"
                class="search-result-item"
              >
                <div class="result-main">
                  <strong>{{ stock.symbol }}</strong> - {{ stock.name }}
                </div>
                <div class="result-meta">
                  <span class="result-type">{{ stock.type }}</span>
                  <span class="result-price">${{ formatNumber(stock.currentPrice) }}</span>
                </div>
              </div>
            </div>
            <div v-if="selectedStock || editingItem" class="selected-stock">
              <strong>{{ form.symbol }}</strong> - {{ form.name }}
              <button v-if="!editingItem" type="button" @click="clearSelection" class="btn-clear">✕</button>
            </div>
          </div>

          <div class="form-group">
            <label for="targetPrice">Target Price</label>
            <input
              id="targetPrice"
              v-model.number="form.targetPrice"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
            />
          </div>

          <div class="form-group">
            <label for="priority">Priority</label>
            <select id="priority" v-model="form.priority">
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          <div class="form-group">
            <label for="notes">Notes (optional)</label>
            <textarea
              id="notes"
              v-model="form.notes"
              rows="3"
              placeholder="Add any notes about this stock..."
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeAddStockModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="watchlistStore.loading || (!selectedStock && !editingItem)">
              {{ editingItem ? 'Update' : 'Add to Watchlist' }}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import Modal from '../components/Modal.vue'
import { useWatchlistStore } from '../stores/watchlist'
import api from '../services/api'

const router = useRouter()
const watchlistStore = useWatchlistStore()

const showCreateWatchlistModal = ref(false)
const showAddModal = ref(false)
const currentWatchlistId = ref(null)
const editingItem = ref(null)
const searchQuery = ref('')
const searchResults = ref([])
const selectedStock = ref(null)
const expandedWatchlists = ref({})
const watchlistItems = ref({})
const loadingItems = ref({})
const filterTags = ref({})
const sortConfig = ref({})

const watchlistForm = ref({
  name: '',
  icon: '📋',
  description: ''
})

const form = ref({
  symbol: '',
  name: '',
  type: 'STOCK',
  targetPrice: null,
  priority: 'MEDIUM',
  notes: ''
})

const editingCell = ref(null)
const editValue = ref(null)
const editTags = ref([])
const tagInput = ref('')
const allTags = ref([])
const editInput = ref(null)

onMounted(async () => {
  await watchlistStore.fetchWatchlists()
  await fetchAllTags()
})

async function fetchAllTags() {
  try {
    const response = await api.get('/tags')
    allTags.value = response.data
  } catch (error) {
    console.error('Failed to fetch tags:', error)
  }
}

async function toggleWatchlist(watchlistId) {
  expandedWatchlists.value[watchlistId] = !expandedWatchlists.value[watchlistId]
  
  if (expandedWatchlists.value[watchlistId] && !watchlistItems.value[watchlistId]) {
    loadingItems.value[watchlistId] = true
    try {
      await watchlistStore.fetchWatchlistItems(watchlistId)
      watchlistItems.value[watchlistId] = watchlistStore.watchlist
    } catch (error) {
      console.error('Failed to load watchlist items:', error)
    } finally {
      loadingItems.value[watchlistId] = false
    }
  }
}

function getWatchlistItemCount(watchlistId) {
  return watchlistItems.value[watchlistId]?.length || 0
}

function getAvailableTags(watchlistId) {
  const items = watchlistItems.value[watchlistId] || []
  const tagsSet = new Set()
  items.forEach(item => {
    if (item.tags) {
      item.tags.forEach(tag => {
        tagsSet.add(JSON.stringify(tag))
      })
    }
  })
  return Array.from(tagsSet).map(t => JSON.parse(t))
}

function getFilteredAndSortedItems(watchlistId) {
  let items = [...(watchlistItems.value[watchlistId] || [])]
  
  if (filterTags.value[watchlistId]) {
    items = items.filter(item => 
      item.tags && item.tags.some(tag => tag.id === parseInt(filterTags.value[watchlistId]))
    )
  }
  
  const config = sortConfig.value[watchlistId]
  if (config?.by && config?.order) {
    items.sort((a, b) => {
      let aVal, bVal
      
      switch (config.by) {
        case 'symbol':
          aVal = a.stock.symbol.toLowerCase()
          bVal = b.stock.symbol.toLowerCase()
          break
        case 'price':
          aVal = a.stock.currentPrice || 0
          bVal = b.stock.currentPrice || 0
          break
        case 'priority':
          const priorityOrder = { HIGH: 3, MEDIUM: 2, LOW: 1 }
          aVal = priorityOrder[a.priority] || 0
          bVal = priorityOrder[b.priority] || 0
          break
        case 'targetPrice':
          aVal = a.targetPrice || 0
          bVal = b.targetPrice || 0
          break
        default:
          return 0
      }
      
      if (typeof aVal === 'string') {
        return config.order === 'asc' 
          ? aVal.localeCompare(bVal) 
          : bVal.localeCompare(aVal)
      } else {
        return config.order === 'asc' 
          ? aVal - bVal 
          : bVal - aVal
      }
    })
  }
  
  return items
}

function handleSort(field, watchlistId) {
  if (!sortConfig.value[watchlistId]) {
    sortConfig.value[watchlistId] = { by: null, order: null }
  }
  
  const config = sortConfig.value[watchlistId]
  if (config.by === field) {
    if (config.order === 'asc') {
      config.order = 'desc'
    } else if (config.order === 'desc') {
      config.by = null
      config.order = null
    }
  } else {
    config.by = field
    config.order = 'asc'
  }
}

async function createWatchlist() {
  try {
    await watchlistStore.createWatchlist(watchlistForm.value)
    await watchlistStore.fetchWatchlists()
    closeCreateWatchlistModal()
  } catch (error) {
    console.error('Failed to create watchlist:', error)
  }
}

function closeCreateWatchlistModal() {
  showCreateWatchlistModal.value = false
  watchlistForm.value = {
    name: '',
    icon: '📋',
    description: ''
  }
}

function showAddStockModal(watchlistId) {
  currentWatchlistId.value = watchlistId
  showAddModal.value = true
}

async function deleteWatchlistConfirm(watchlistId) {
  if (confirm('Are you sure you want to delete this watchlist? All items will be removed.')) {
    try {
      await watchlistStore.deleteWatchlist(watchlistId)
      await watchlistStore.fetchWatchlists()
      delete watchlistItems.value[watchlistId]
      delete expandedWatchlists.value[watchlistId]
    } catch (error) {
      console.error('Failed to delete watchlist:', error)
    }
  }
}

const filteredTagSuggestions = computed(() => {
  if (!tagInput.value.trim()) return []
  const input = tagInput.value.toLowerCase()
  return allTags.value.filter(tag => 
    tag.name.toLowerCase().includes(input) && 
    !editTags.value.some(t => t.id === tag.id)
  ).slice(0, 5)
})

async function startEdit(id, field) {
  let item = null
  for (const items of Object.values(watchlistItems.value)) {
    item = items.find(i => i.id === id)
    if (item) break
  }
  if (!item) return
  
  editingCell.value = { id, field }
  
  if (field === 'tags') {
    editTags.value = item.tags ? [...item.tags] : []
    tagInput.value = ''
  } else {
    editValue.value = item[field]
  }
  
  await nextTick()
  if (editInput.value) {
    if (Array.isArray(editInput.value)) {
      editInput.value[0]?.focus()
    } else {
      editInput.value.focus()
    }
  }
}

function cancelEdit() {
  editingCell.value = null
  editValue.value = null
  editTags.value = []
  tagInput.value = ''
}

async function saveEdit(item) {
  if (!editingCell.value) return
  
  const field = editingCell.value.field
  const updateData = {}
  
  if (field === 'targetPrice') {
    updateData.targetPrice = editValue.value
  } else if (field === 'priority') {
    updateData.priority = editValue.value
  } else if (field === 'notes') {
    updateData.notes = editValue.value
  }
  
  try {
    await watchlistStore.updateWatchlistItem(item.id, updateData)
    
    for (const [watchlistId, items] of Object.entries(watchlistItems.value)) {
      const itemIndex = items.findIndex(i => i.id === item.id)
      if (itemIndex !== -1) {
        await watchlistStore.fetchWatchlistItems(parseInt(watchlistId))
        watchlistItems.value[watchlistId] = watchlistStore.watchlist
        break
      }
    }
    
    cancelEdit()
  } catch (error) {
    console.error('Failed to update item:', error)
  }
}

async function saveTagsEdit(item) {
  if (!editingCell.value || editingCell.value.field !== 'tags') return
  
  await new Promise(resolve => setTimeout(resolve, 200))
  
  try {
    const tags = editTags.value.map(t => t.name)
    await watchlistStore.updateWatchlistItem(item.id, { tags })
    
    for (const [watchlistId, items] of Object.entries(watchlistItems.value)) {
      const itemIndex = items.findIndex(i => i.id === item.id)
      if (itemIndex !== -1) {
        await watchlistStore.fetchWatchlistItems(parseInt(watchlistId))
        watchlistItems.value[watchlistId] = watchlistStore.watchlist
        break
      }
    }
    
    cancelEdit()
  } catch (error) {
    console.error('Failed to update tags:', error)
  }
}

function addTag(item) {
  const input = tagInput.value.trim()
  if (!input) return
  
  const existingTag = allTags.value.find(t => t.name.toLowerCase() === input.toLowerCase())
  
  if (existingTag && !editTags.value.some(t => t.id === existingTag.id)) {
    editTags.value.push(existingTag)
  } else if (!existingTag) {
    editTags.value.push({ name: input, id: null })
  }
  
  tagInput.value = ''
}

function selectTagSuggestion(tag, item) {
  if (!editTags.value.some(t => t.id === tag.id)) {
    editTags.value.push(tag)
  }
  tagInput.value = ''
}

function removeTag(tag) {
  editTags.value = editTags.value.filter(t => t !== tag)
}

async function saveItem() {
  try {
    if (editingItem.value) {
      await watchlistStore.updateWatchlistItem(editingItem.value.id, {
        targetPrice: form.value.targetPrice,
        priority: form.value.priority,
        notes: form.value.notes
      })
    } else {
      await watchlistStore.addToWatchlist(currentWatchlistId.value, {
        symbol: selectedStock.value.symbol,
        targetPrice: form.value.targetPrice,
        priority: form.value.priority,
        notes: form.value.notes
      })
      
      if (expandedWatchlists.value[currentWatchlistId.value]) {
        await watchlistStore.fetchWatchlistItems(currentWatchlistId.value)
        watchlistItems.value[currentWatchlistId.value] = watchlistStore.watchlist
      }
    }
    closeAddStockModal()
  } catch (error) {
    console.error('Failed to save watchlist item:', error)
  }
}

let searchTimeout = null
async function searchStocks() {
  if (searchTimeout) clearTimeout(searchTimeout)
  
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  
  searchTimeout = setTimeout(async () => {
    try {
      const response = await api.get('/stocks/search', {
        params: { query: searchQuery.value }
      })
      searchResults.value = response.data
    } catch (error) {
      console.error('Failed to search stocks:', error)
    }
  }, 300)
}

function selectStock(stock) {
  selectedStock.value = stock
  form.value.symbol = stock.symbol
  form.value.name = stock.name
  form.value.type = stock.type
  searchResults.value = []
  searchQuery.value = ''
}

function clearSelection() {
  selectedStock.value = null
  form.value.symbol = ''
  form.value.name = ''
  form.value.type = 'STOCK'
  searchQuery.value = ''
  searchResults.value = []
}

async function removeItemConfirm(id, watchlistId) {
  if (confirm('Are you sure you want to remove this stock from your watchlist?')) {
    try {
      await watchlistStore.removeFromWatchlist(id)
      
      if (expandedWatchlists.value[watchlistId]) {
        await watchlistStore.fetchWatchlistItems(watchlistId)
        watchlistItems.value[watchlistId] = watchlistStore.watchlist
      }
    } catch (error) {
      console.error('Failed to remove from watchlist:', error)
    }
  }
}

function closeAddStockModal() {
  showAddModal.value = false
  currentWatchlistId.value = null
  editingItem.value = null
  selectedStock.value = null
  searchQuery.value = ''
  searchResults.value = []
  form.value = {
    symbol: '',
    name: '',
    type: 'STOCK',
    targetPrice: null,
    priority: 'MEDIUM',
    notes: ''
  }
}

function formatNumber(num) {
  return num?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

function formatPercent(num) {
  if (num === null || num === undefined || isNaN(num)) return '0.00'
  return Number(num).toFixed(2)
}

function goToStock(symbol) {
  router.push(`/stocks/${symbol}`)
}
</script>

<style scoped>
.watchlist-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 2rem;
  flex-wrap: wrap;
}

h1 {
  margin: 0;
  color: #555;
  font-size: 1.5rem;
  font-weight: 500;
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

.watchlists-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.watchlist-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.watchlist-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.watchlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transition: background 0.3s;
}

.watchlist-header:hover {
  background: linear-gradient(135deg, #5568d3 0%, #653a8b 100%);
}

.watchlist-title {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.watchlist-icon {
  font-size: 2rem;
}

.watchlist-info h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.watchlist-description {
  margin: 0.25rem 0 0 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.watchlist-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.item-count {
  font-size: 0.9rem;
  opacity: 0.9;
  padding: 0.25rem 0.75rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

.btn-icon-small {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
}

.btn-icon-small:hover {
  background: rgba(255, 255, 255, 0.3);
}

.btn-icon-small.delete:hover {
  background: #ef4444;
}

.btn-add-stock {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
}

.btn-add-stock:hover {
  background: rgba(255, 255, 255, 0.3);
}

.expand-icon {
  font-size: 0.9rem;
  opacity: 0.9;
}

.watchlist-content {
  padding: 1.5rem;
  background: #f9f9f9;
}

.loading-items {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.empty-items {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.empty-items p {
  margin-bottom: 1rem;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.filter-controls label {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.filter-select {
  padding: 0.5rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  background: white;
  transition: border-color 0.3s;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
}

.watchlist-items {
  margin-top: 1rem;
}

.watchlist-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #667eea;
  color: white;
  padding: 0.875rem 1rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e0e0e0;
}

th.sortable {
  cursor: pointer;
  user-select: none;
  transition: background 0.2s;
}

th.sortable:hover {
  background: #5568d3;
}

.sort-indicator {
  margin-left: 0.5rem;
  font-size: 0.75rem;
  opacity: 0.9;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background: #f9f9f9;
}

.clickable-row {
  cursor: pointer;
  transition: background 0.2s;
}

.clickable-row:hover {
  background: #f0f3ff;
}

.symbol {
  font-weight: 700;
}

.stock-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s;
  cursor: pointer;
}

.stock-link:hover {
  color: #764ba2;
}

.price-column {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.price {
  font-weight: 600;
  font-size: 1rem;
}

.daily-change {
  font-size: 0.85rem;
  font-weight: 600;
}

.daily-change.positive {
  color: #10b981;
}

.daily-change.negative {
  color: #ef4444;
}

.type-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.type-badge.stock {
  background: #e3f2fd;
  color: #1976d2;
}

.type-badge.etf {
  background: #f3e5f5;
  color: #7b1fa2;
}

.type-badge.crypto {
  background: #fff3e0;
  color: #e65100;
}

.type-badge.commodity {
  background: #e8f5e9;
  color: #2e7d32;
}

.priority-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.priority-badge.high {
  background: #fee;
  color: #c33;
}

.priority-badge.medium {
  background: #fef3cd;
  color: #856404;
}

.priority-badge.low {
  background: #d4edda;
  color: #155724;
}

.notes {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #666;
  font-size: 0.9rem;
}

.editable {
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
}

.editable:hover {
  background: #f8f9fa;
}

.inline-edit {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  font-family: inherit;
  box-sizing: border-box;
}

.inline-edit:focus {
  outline: none;
  border-color: #667eea;
}

.inline-edit-select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  font-family: inherit;
  cursor: pointer;
  background: white;
}

.inline-edit-select:focus {
  outline: none;
  border-color: #667eea;
}

.tags-cell {
  min-width: 200px;
  max-width: 300px;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.tag-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  background: #e8eaf6;
  color: #3f51b5;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.tag-badge.editable {
  padding-right: 0.5rem;
}

.remove-tag {
  background: none;
  border: none;
  color: #3f51b5;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  margin-left: 0.25rem;
  line-height: 1;
  font-weight: bold;
  opacity: 0.7;
}

.remove-tag:hover {
  opacity: 1;
}

.empty-tags {
  color: #999;
  font-size: 0.85rem;
  font-style: italic;
}

.tags-edit-container {
  position: relative;
}

.tags-input {
  margin-bottom: 0.5rem;
}

.current-tags-edit {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.tag-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ccc;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 150px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tag-suggestion {
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  transition: background 0.2s;
}

.tag-suggestion:hover {
  background: #f0f0f0;
}

.btn-icon-modern {
  background: white;
  border: 1px solid #e0e0e0;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.btn-icon-modern:hover {
  background: #f5f5f5;
  border-color: #ccc;
  color: #333;
}

.btn-icon-modern.delete:hover {
  background: #fee;
  border-color: #fcc;
  color: #c33;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: #666;
}

.empty-state p {
  margin-bottom: 1.5rem;
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

input:disabled,
select:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
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

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 2px solid #667eea;
  border-top: none;
  border-radius: 0 0 8px 8px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  margin-top: -8px;
}

.search-result-item {
  padding: 0.875rem 1rem;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background: #f8f9ff;
}

.result-main {
  margin-bottom: 0.25rem;
  color: #333;
}

.result-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.85rem;
}

.result-type {
  color: #667eea;
  font-weight: 600;
}

.result-price {
  color: #666;
}

.selected-stock {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem;
  background: #f8f9ff;
  border: 2px solid #667eea;
  border-radius: 8px;
  margin-top: 0.5rem;
}

.btn-clear {
  background: none;
  border: none;
  color: #999;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  transition: color 0.2s;
}

.btn-clear:hover {
  color: #333;
}

.form-group {
  position: relative;
}
</style>
