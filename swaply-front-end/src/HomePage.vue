<template>
  <div class="home-container">
    <AppHeader active="home" @navigate="(p)=>emit('navigate',p)" @logout="logout" />

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">Welcome to Swaply</h1>
        <p class="hero-subtitle">Discover amazing deals and sell your items with ease</p>
        <div class="hero-actions">
          <button class="hero-btn primary" @click="emit('navigate', 'post-item')">
            <span class="btn-icon">📦</span> Sell an Item
          </button>
          <button class="hero-btn secondary" @click="emit('navigate', 'bidding')">
            <span class="btn-icon">🔨</span> Browse Auctions
          </button>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <main class="main-content">
      <div class="content-wrap">
        <!-- Search Section -->
        <div class="search-section">
          <h2 class="section-title">Find Your Next Treasure</h2>
          <div class="search-container">
            <div class="search-input-wrapper">
              <span class="search-icon">🔍</span>
              <input
                type="text"
                placeholder="Search for items, categories, or sellers..."
                class="search-input"
                v-model="searchQuery"
              />
              <span class="search-count" v-if="filteredItems.length">
                {{ filteredItems.length }} items found
              </span>
            </div>
          </div>
        </div>

        <div class="status-bar" v-if="loading || errorMessage">
          <span v-if="loading">Loading listings...</span>
          <span v-else class="error-text">{{ errorMessage }}</span>
        </div>

        <!-- Filter Buttons -->
        <div class="filter-section">
          <button
            class="filter-button"
            :class="{ active: activeFilter === 'all' }"
            @click="setFilter('all')"
          >
            All Active
          </button>
          <button
            class="filter-button"
            :class="{ active: activeFilter === 'latest' }"
            @click="setFilter('latest')"
          >
            Latest
          </button>
          <button
            class="filter-button"
            :class="{ active: activeFilter === 'popular' }"
            @click="setFilter('popular')"
          >
            Popular
          </button>
          <button
            class="filter-button"
            :class="{ active: activeFilter === 'price' }"
            @click="setFilter('price')"
          >
            Price Range
          </button>
          <button
            class="filter-button"
            :class="{ active: activeFilter === 'category' }"
            @click="cycleCategory()"
          >
            {{ categoryFilterLabel }}
          </button>
          <button
            class="filter-button accent"
            :class="{ active: activeFilter === 'sell' }"
            @click="setFilter('sell')"
          >
            Sell an Item
          </button>
        </div>
        <div class="price-range-panel" v-if="activeFilter === 'price'">
          <div class="price-field">
            <label>Min (ft)</label>
            <input type="number" min="0" step="1" v-model.number="priceRange.min" />
          </div>
          <div class="price-field">
            <label>Max (ft)</label>
            <input type="number" min="0" step="1" v-model.number="priceRange.max" />
          </div>
          <button class="apply-button" @click="fetchListings" :disabled="loading">Apply</button>
        </div>

        <!-- Items Grid -->
        <div class="items-grid">
          <div
            v-for="item in pagedItems"
            :key="item.listingId"
            class="item-card"
            @click="selectItem(item)"
          >
            <div class="item-image-wrapper">
              <img class="item-image" :src="resolveImage(item.imageUrl)" :alt="item.title" />
              <div class="item-badges">
                <span class="badge-pill category-badge">{{ getCategoryIcon(item.category) }} {{ prettyEnum(item.category) }}</span>
              </div>
            </div>
            <div class="item-content">
              <h3 class="item-title">{{ item.title }}</h3>
              <p class="item-description">{{ item.description }}</p>

              <div class="item-footer">
                <div class="price-section">
                  <span class="price-label">Price</span>
                  <p class="item-price">{{ formatPrice(item.price) }}</p>
                </div>

                <div class="seller-info" v-if="item.sellerUsername">
                  <img class="seller-avatar" :src="resolveImage(item.sellerProfileImageUrl)" :alt="item.sellerUsername" />
                  <div class="seller-details">
                    <span class="seller-label">Seller</span>
                    <span class="seller-name">{{ item.sellerUsername }}</span>
                  </div>
                </div>
              </div>

              <div class="condition-row">
                <span class="badge-condition" :class="getConditionClass(item.condition)">
                  {{ prettyEnum(item.condition) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination" v-if="!loading && pagedItems.length">
          <button
            class="pagination-button"
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            Previous
          </button>
          <span class="page-info">Page {{ currentPage }} of {{ totalPages }}</span>
          <button
            class="pagination-button"
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            Next
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import AppHeader from './components/AppHeader.vue'
import listingApi from './services/listingApi.js'
import cartApi from './services/cartApi.js'
import { fmtFt } from './services/currency.js'

const emit = defineEmits(['navigate','logout'])

const searchQuery = ref('')
const activeFilter = ref('latest')
const currentPage = ref(1)
const itemsPerPage = 6
const loading = ref(false)
const errorMessage = ref('')
const listings = ref([])
const categoryCycle = ['ELECTRONICS','BOOKS','FURNITURE','CLOTHING','SPORTS','NECESSITIES','TOYS_GAMES','OTHER']
const categoryIndex = ref(0)

const placeholderImage = './assets/logo.png'

const cartCount = ref(0)
async function refreshCartCount() {
  try {
    const { data } = await cartApi.getCart()
    cartCount.value = (data || []).reduce((sum, it) => sum + (it.quantity || 0), 0)
  } catch { /* silent */ }
}

const handleCartUpdated = () => refreshCartCount()
const handleOrdersUpdated = () => {
  // re-fetch active listings after checkout/order changes
  fetchListings()
}

const filters = {
  latest: () => listingApi.getLatestListings(30),
  popular: () => listingApi.getPopularListings(30),
  price: (range) => listingApi.getListingsByPriceRange(range.min, range.max, 30),
  all: () => listingApi.getActiveListings()
}

const priceRange = ref({ min: 0, max: 1000 })

const fetchListings = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    let response
    if (activeFilter.value === 'price') {
      response = await filters.price(priceRange.value)
    } else {
      response = await (filters[activeFilter.value] || filters.all)()
    }
    listings.value = response.data || []
  } catch (e) {
    errorMessage.value = 'Failed to load listings'
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchListings()
  refreshCartCount()
  window.addEventListener('cart-updated', handleCartUpdated)
  window.addEventListener('orders-updated', handleOrdersUpdated)
})

onBeforeUnmount(() => {
  window.removeEventListener('cart-updated', handleCartUpdated)
  window.removeEventListener('orders-updated', handleOrdersUpdated)
})

const categoryFilter = computed(() => activeFilter.value === 'category' ? categoryCycle[categoryIndex.value] : null)
const categoryFilterLabel = computed(() => {
  return activeFilter.value === 'category' ? 'Category: ' + prettyEnum(categoryCycle[categoryIndex.value]) : 'Filter Category'
})

const filteredItems = computed(() => {
  let items = listings.value
    .filter(l => l.status === 'ACTIVE')

  if (activeFilter.value === 'category' && categoryFilter.value) {
    items = items.filter(i => i.category === categoryFilter.value)
  }

  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    items = items.filter(i => (i.title||'').toLowerCase().includes(q) || (i.description||'').toLowerCase().includes(q))
  }
  return items
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / itemsPerPage)))

const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredItems.value.slice(start, start + itemsPerPage)
})

const setFilter = (filter) => {
  if (filter === 'sell') {
    emit('navigate', 'post-item')
    return
  }
  activeFilter.value = filter
  currentPage.value = 1
  fetchListings()
}

const cycleCategory = () => {
  if (activeFilter.value !== 'category') {
    activeFilter.value = 'category'
    categoryIndex.value = 0
  } else {
    categoryIndex.value = (categoryIndex.value + 1) % categoryCycle.length
  }
  currentPage.value = 1
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) currentPage.value = page
}

const selectItem = (item) => {
  emit('navigate', 'listing-detail', { listingId: item.listingId })
}

const logout = () => emit('logout')

const goProfile = () => emit('navigate', 'profile')
const goCart = () => emit('navigate', 'cart')
const goCommunity = () => emit('navigate', 'community')
const goBidding = () => emit('navigate', 'bidding')

const formatPrice = (price) => {
  return fmtFt(price)
}

const prettyEnum = (val) => {
  if (!val) return ''
  return String(val)
    .toLowerCase()
    .split('_')
    .map(w => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}

const resolveImage = (path) => {
  const placeholderImage = './assets/logo.png'
  if (!path) return placeholderImage
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/uploads/')) return path
  if (path.startsWith('uploads/')) return '/' + path
  return path
}

const avatarSrc = computed(() => {
  try {
    const u = JSON.parse(localStorage.getItem('user'))
    const p = u?.profileImageUrl
    if (!p) return './assets/logo.png'
    if (p.startsWith('http://') || p.startsWith('https://')) return p
    if (p.startsWith('/uploads/')) return p
    if (p.startsWith('uploads/')) return '/' + p
    return p
  } catch { return './assets/logo.png' }
})

function toggleChat(){
  // open the global chat widget
  window.dispatchEvent(new Event('open-chat'))
}

function getCategoryIcon(category) {
  const icons = {
    ELECTRONICS: '📱',
    BOOKS: '📚',
    FURNITURE: '🛋️',
    CLOTHING: '👕',
    SPORTS: '⚽',
    NECESSITIES: '🛒',
    TOYS_GAMES: '🎮',
    OTHER: '📦'
  }
  return icons[category] || '📦'
}

function getConditionClass(condition) {
  const classes = {
    NEW: 'condition-new',
    LIKE_NEW: 'condition-like-new',
    GOOD: 'condition-good',
    FAIR: 'condition-fair',
    POOR: 'condition-poor'
  }
  return classes[condition] || 'condition-good'
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 4rem 2rem;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><circle cx="50" cy="50" r="2" fill="white" opacity="0.1"/></svg>');
  opacity: 0.3;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: white;
  margin: 0 0 1rem 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 2rem 0;
  font-weight: 400;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.hero-btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.hero-btn.primary {
  background: white;
  color: #667eea;
}

.hero-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.hero-btn.secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.hero-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.btn-icon {
  font-size: 1.2rem;
}

/* Main Content */
.main-content {
  padding: 2rem 2rem 4rem 2rem;
  background: #f8fafc;
  border-radius: 32px 32px 0 0;
  margin-top: -2rem;
  position: relative;
  z-index: 2;
}

.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.5rem 0;
  letter-spacing: -0.01em;
}

.search-section {
  margin-bottom: 2rem;
}

.search-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-input-wrapper {
  position: relative;
  width: 100%;
  max-width: 700px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 1rem 1.25rem 1rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  font-size: 1rem;
  outline: none;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-input:focus {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.search-count {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: #667eea;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-bar {
  margin: 1rem 0;
  color: #334155;
}

.error-text {
  color: #dc2626;
}

/* Filters */
.filter-section {
  display: flex;
  gap: 0.75rem;
  margin: 2rem 0 1.5rem 0;
  flex-wrap: wrap;
  justify-content: center;
}

.filter-button {
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  background: white;
  color: #475569;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 0.95rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.filter-button:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.filter-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.filter-button.accent {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-color: transparent;
  color: white;
}

.filter-button.accent:hover {
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.4);
}

/* Price Range Panel */
.price-range-panel {
  display: flex;
  gap: 1rem;
  margin: 1rem 0;
  padding: 1rem;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.price-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.price-field label {
  font-size: 0.875rem;
  color: #334155;
}

.price-field input {
  padding: 0.5rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
  transition: border 0.2s ease;
}

.price-field input:focus {
  border-color: #818cf8;
  box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.15);
}

.apply-button {
  padding: 0.5rem 1rem;
  border-radius: 10px;
  border: none;
  background: #818cf8;
  color: white;
  cursor: pointer;
  transition: background 0.2s ease;
}

.apply-button:hover {
  background: #6f63d2;
}

/* Items Grid */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  justify-content: center;
  margin-top: 2rem;
}

.item-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
}

.item-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.item-image-wrapper {
  position: relative;
  overflow: hidden;
  height: 220px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.item-card:hover .item-image {
  transform: scale(1.1);
}

.item-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 1;
}

.badge-pill {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #1e293b;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.category-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.item-content {
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.item-title {
  font-weight: 700;
  font-size: 1.15rem;
  margin: 0;
  color: #0f172a;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.item-description {
  color: #64748b;
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f1f5f9;
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.price-label {
  font-size: 0.75rem;
  color: #94a3b8;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.item-price {
  color: #10b981;
  font-weight: 800;
  font-size: 1.5rem;
  margin: 0;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.seller-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f1f5f9;
}

.seller-details {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.seller-label {
  font-size: 0.7rem;
  color: #94a3b8;
  text-transform: uppercase;
  font-weight: 600;
}

.seller-name {
  font-size: 0.85rem;
  color: #475569;
  font-weight: 600;
}

.condition-row {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.badge-condition {
  padding: 0.35rem 0.75rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.condition-new {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.condition-like-new {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.condition-good {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
}

.condition-fair {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.condition-poor {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: white;
}

/* Pagination */
.pagination {
  display: flex;
  gap: 1.5rem;
  align-items: center;
  justify-content: center;
  margin: 3rem 0 1rem 0;
}

.page-info {
  color: #475569;
  font-weight: 600;
  font-size: 1rem;
}

.pagination-button {
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  background: white;
  color: #475569;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.pagination-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.pagination-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.seller-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0.15rem 0 0.35rem 0;
}

.seller-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.seller-name {
  font-weight: 700;
  color: #0f172a;
  font-size: 0.9rem;
}
</style>
