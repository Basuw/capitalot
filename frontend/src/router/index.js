import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/portfolios',
      name: 'Portfolios',
      component: () => import('../views/Portfolios.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/portfolios/:id',
      name: 'PortfolioDetail',
      component: () => import('../views/PortfolioDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/watchlist',
      name: 'Watchlist',
      component: () => import('../views/Watchlist.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/actions',
      name: 'Actions',
      component: () => import('../views/StockSearch.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stocks/:symbol',
      name: 'StockDetail',
      component: () => import('../views/StockDetail.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if ((to.name === 'Login' || to.name === 'Register') && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
