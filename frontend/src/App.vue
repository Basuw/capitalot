<template>
  <div class="app-container">
    <Navbar v-if="showNavbar" />
    <div :class="{ 'main-content': showNavbar, 'main-content-full': !showNavbar }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import Navbar from './components/Navbar.vue'

const route = useRoute()
const authStore = useAuthStore()

const showNavbar = computed(() => {
  const publicRoutes = ['/login', '/register']
  return !publicRoutes.includes(route.path) && authStore.token
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.main-content {
  margin-left: 260px;
  min-height: 100vh;
}

.main-content-full {
  margin-left: 0;
  min-height: 100vh;
}
</style>
