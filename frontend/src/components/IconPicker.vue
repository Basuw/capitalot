<template>
  <div class="icon-picker">
    <div class="icon-categories">
      <button
        v-for="category in categories"
        :key="category"
        type="button"
        :class="['category-btn', { active: selectedCategory === category }]"
        @click="selectedCategory = category"
      >
        {{ category }}
      </button>
    </div>

    <div class="icon-grid">
      <button
        v-for="icon in filteredIcons"
        :key="icon"
        type="button"
        :class="['icon-btn', { selected: modelValue === icon }]"
        @click="selectIcon(icon)"
      >
        {{ icon }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: String
})

const emit = defineEmits(['update:modelValue'])

const selectedCategory = ref('Finance')

const icons = {
  Finance: ['💰', '💵', '💴', '💶', '💷', '💳', '💎', '📈', '📉', '💹', '🏦', '💸'],
  Business: ['💼', '📊', '📋', '📌', '📍', '🏢', '🏛️', '⚖️', '🔨', '⚙️'],
  Technology: ['💻', '⌨️', '🖥️', '📱', '⚡', '🔋', '💡', '🔌', '📡', '🛰️'],
  Travel: ['✈️', '🚀', '🚁', '🚂', '🚗', '🚕', '🚙', '🚌', '🚎', '🏎️', '🚓', '🚑'],
  Food: ['🍔', '🍕', '🍗', '🍟', '🌭', '🥪', '🌮', '🌯', '🥙', '🍝', '🍜', '☕'],
  Nature: ['🌲', '🌳', '🌴', '🌵', '🌾', '🌿', '☘️', '🍀', '🌍', '🌎', '🌏', '⭐'],
  Sports: ['⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '⛳', '🏆'],
  Objects: ['📦', '📫', '📪', '📬', '📭', '📮', '🗳️', '✏️', '✒️', '🖊️', '🖍️', '📝']
}

const categories = Object.keys(icons)

const filteredIcons = computed(() => {
  return icons[selectedCategory.value] || []
})

function selectIcon(icon) {
  emit('update:modelValue', icon)
}
</script>

<style scoped>
.icon-picker {
  width: 100%;
}

.icon-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e0e0e0;
}

.category-btn {
  padding: 0.5rem 1rem;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.category-btn:hover {
  background: #e0e0e0;
}

.category-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 0.5rem;
  max-height: 300px;
  overflow-y: auto;
}

.icon-btn {
  width: 60px;
  height: 60px;
  font-size: 2rem;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: #e0e0e0;
  transform: scale(1.1);
}

.icon-btn.selected {
  border-color: #667eea;
  background: #f0f3ff;
}
</style>
