import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { fetchExchangeRates } from './services/currency'
import './style.css'

// Fetch exchange rates at startup
fetchExchangeRates()

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
