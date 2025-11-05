import './assets/main.css'

// Polyfills for libraries expecting Node globals in browser
if (typeof window !== 'undefined') {
  if (typeof window.global === 'undefined') window.global = window
  if (typeof window.process === 'undefined') window.process = { env: {} }
}

import { createApp } from 'vue'
import App from './App.vue'

createApp(App).mount('#app')
