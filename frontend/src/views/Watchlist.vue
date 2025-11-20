<template>
  <div>
    <Navbar />
    
    <div class="watchlist-container">
      <div class="header">
        <h1>Watchlist</h1>
        <button @click="showAddModal = true" class="btn-primary">
          + Add Stock
        </button>
      </div>

      <div v-if="watchlistStore.loading" class="loading">Loading watchlist...</div>

      <div v-else-if="watchlistStore.watchlist.length" class="watchlist-table">
        <table>
          <thead>
            <tr>
              <th>Symbol</th>
              <th>Name</th>
              <th>Type</th>
              <th>Current Price</th>
              <th>Target Price</th>
              <th>Priority</th>
              <th>Tags</th>
              <th>Notes</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in watchlistStore.watchlist" :key="item.id">
              <td class="symbol">
                <router-link :to="`/stocks/${item.stock.symbol}`" class="stock-link">
                  {{ item.stock.symbol }}
                </router-link>
              </td>
              <td>{{ item.stock.name }}</td>
              <td>
                <span v-if="item.stock?.type" :class="['type-badge', item.stock.type.toLowerCase()]">{{ item.stock.type }}</span>
                <span v-else>-</span>
              </td>
              <td>${{ formatNumber(item.currentPrice) }}</td>
              <td @click="startEdit(item.id, 'targetPrice')" class="editable">
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
              <td @click="startEdit(item.id, 'priority')" class="editable">
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
              <td @click="startEdit(item.id, 'tags')" class="editable tags-cell">
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
              <td @click="startEdit(item.id, 'notes')" class="editable notes">
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
              <td>
                <button @click="removeItemConfirm(item.id)" class="btn-icon-modern delete" title="Remove">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty-state">
        <p>Your watchlist is empty. Add stocks you're interested in tracking!</p>
      </div>

      <Modal :show="showAddModal" :title="editingItem ? 'Edit Watchlist Item' : 'Add to Watchlist'" @close="closeModal">
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label for="symbol">Stock Symbol</label>
            <input
              id="symbol"
              v-model="form.symbol"
              type="text"
              required
              placeholder="e.g., AAPL"
              :disabled="!!editingItem"
            />
          </div>

          <div class="form-group">
            <label for="name">Stock Name</label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              required
              placeholder="e.g., Apple Inc."
              :disabled="!!editingItem"
            />
          </div>

          <div class="form-group">
            <label for="type">Type</label>
            <select id="type" v-model="form.type" required :disabled="!!editingItem">
              <option value="STOCK">Stock</option>
              <option value="ETF">ETF</option>
              <option value="CRYPTO">Crypto</option>
              <option value="COMMODITY">Commodity</option>
            </select>
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
            <button type="button" @click="closeModal" class="btn-secondary">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="watchlistStore.loading">
              {{ editingItem ? 'Update' : 'Add' }}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import Navbar from '../components/Navbar.vue'
import Modal from '../components/Modal.vue'
import { useWatchlistStore } from '../stores/watchlist'
import api from '../services/api'

const watchlistStore = useWatchlistStore()

const showAddModal = ref(false)
const editingItem = ref(null)
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

onMounted(() => {
  watchlistStore.fetchWatchlist()
  fetchAllTags()
})

async function fetchAllTags() {
  try {
    const response = await api.get('/tags')
    allTags.value = response.data
  } catch (error) {
    console.error('Failed to fetch tags:', error)
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
  const item = watchlistStore.watchlist.find(i => i.id === id)
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

function editItem(item) {
  editingItem.value = item
  form.value = {
    symbol: item.stock.symbol,
    name: item.stock.name,
    type: item.stock.type,
    targetPrice: item.targetPrice,
    priority: item.priority,
    notes: item.notes || ''
  }
  showAddModal.value = true
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
      await watchlistStore.addToWatchlist(form.value)
    }
    closeModal()
  } catch (error) {
    console.error('Failed to save watchlist item:', error)
  }
}

async function removeItemConfirm(id) {
  if (confirm('Are you sure you want to remove this stock from your watchlist?')) {
    try {
      await watchlistStore.removeFromWatchlist(id)
    } catch (error) {
      console.error('Failed to remove from watchlist:', error)
    }
  }
}

function closeModal() {
  showAddModal.value = false
  editingItem.value = null
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
  background: #f8f9fa;
  color: #555;
  padding: 0.875rem 1rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e0e0e0;
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

.symbol {
  font-weight: 700;
}

.stock-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s;
}

.stock-link:hover {
  color: #764ba2;
  text-decoration: underline;
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

.btn-icon {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem;
  margin-right: 0.5rem;
  transition: transform 0.2s;
}

.btn-icon:hover {
  transform: scale(1.2);
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
</style>
