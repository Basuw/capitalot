<template>
  <div class="sidebar">
    <div class="sidebar-brand">
      <div class="brand-icon">💼</div>
      <h1>Capitalot</h1>
    </div>
    
    <nav class="sidebar-nav">
      <router-link to="/" class="nav-item">
        <span class="nav-icon">📊</span>
        <span class="nav-text">Dashboard</span>
      </router-link>
      <router-link to="/portfolios" class="nav-item">
        <span class="nav-icon">💰</span>
        <span class="nav-text">Portfolios</span>
      </router-link>
      <router-link to="/watchlist" class="nav-item">
        <span class="nav-icon">👁️</span>
        <span class="nav-text">Watchlist</span>
      </router-link>
      <router-link to="/stock-search" class="nav-item">
        <span class="nav-icon">🔍</span>
        <span class="nav-text">Search</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="user-info">
        <div class="user-avatar">{{ userInitial }}</div>
        <span class="user-email">{{ userEmail }}</span>
      </div>
      <button @click="logout" class="logout-btn">
        <span class="nav-icon">🚪</span>
        <span>Logout</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const userEmail = computed(() => authStore.user?.email || 'Guest')
const userInitial = computed(() => (authStore.user?.email?.[0] || 'G').toUpperCase())

function logout() {
  authStore.clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  width: 260px;
  background: linear-gradient(180deg, #1e1e2e 0%, #27293d 100%);
  display: flex;
  flex-direction: column;
  padding: 1.5rem 0;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.sidebar-brand {
  padding: 0 1.5rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 2rem;
}

.brand-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.sidebar-brand h1 {
  color: white;
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0 1rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem 1rem;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: white;
}

.nav-item.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.nav-icon {
  font-size: 1.25rem;
  width: 24px;
  text-align: center;
}

.nav-text {
  font-size: 0.95rem;
}

.sidebar-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 0.9rem;
}

.user-email {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(255, 59, 48, 0.15);
  border: 1px solid rgba(255, 59, 48, 0.3);
  border-radius: 8px;
  color: #ff6b6b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.logout-btn:hover {
  background: rgba(255, 59, 48, 0.25);
  border-color: rgba(255, 59, 48, 0.5);
}
</style>
