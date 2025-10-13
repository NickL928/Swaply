<template>
  <div class="home-container">
    <!-- Header/Navigation -->
    <header class="header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo-section">
          <img alt="Swaply logo" class="logo" src="./assets/logo.svg" width="40" height="40" />
          <span class="logo-text">Swaply</span>
        </div>

        <!-- Navigation -->
        <nav class="nav-menu">
          <a href="#" class="nav-link active">Home</a>
          <a href="#" class="nav-link" @click.prevent="goCommunity">Community</a>
          <a href="#" class="nav-link">Buy Requests</a>
          <a href="#" class="nav-link">System Announcements</a>
          <a href="#" class="nav-link">Message & Feedback</a>
        </nav>

        <!-- User Profile & DM -->
        <div class="header-actions">
          <button class="dm-button">
            <span class="dm-icon">💬</span>
            DM
          </button>
          <button class="cart-button" @click="goCart" aria-label="Cart">
            <span class="cart-icon">🛒</span>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </button>
          <div class="profile-section">
            <img
              alt="Profile"
              class="profile-picture"
              src="./assets/logo.svg"
              width="40"
              height="40"
              @click="goProfile"
            />
          </div>
          <button class="logout-button" @click="logout">Logout</button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <div class="content-wrap">
        <!-- Search Section -->
        <div class="search-section">
          <div class="search-container">
            <div class="search-input-wrapper">
              <span class="search-icon">🔍</span>
              <input
                type="text"
                placeholder="Search for items..."
                class="search-input"
                v-model="searchQuery"
              />
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
            :class="{ active: activeFilter === 'sell' }"
            @click="setFilter('sell')"
          >
            Sell an Item
          </button>
          <button
            class="filter-button"
            :class="{ active: activeFilter === 'category' }"
            @click="cycleCategory()"
          >
            {{ categoryFilterLabel }}
          </button>
        </div>

        <!-- Items Grid -->
        <div class="items-grid">
          <div
            v-for="item in pagedItems"
            :key="item.listingId"
            class="item-card"
            @click="selectItem(item)"
          >
            <div class="item-image">
              <img :src="resolveImage(item.imageUrl)" :alt="item.title" />
            </div>
            <div class="item-content">
              <h3 class="item-title">{{ item.title }}</h3>
              <p class="item-price">{{ formatPrice(item.price) }}</p>
              <p class="item-description ellipsis">{{ item.description }}</p>
              <div class="meta-row">
                <span class="badge category">{{ prettyEnum(item.category) }}</span>
                <span class="badge condition">{{ prettyEnum(item.condition) }}</span>
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
import listingApi from './services/listingApi.js'
import cartApi from './services/cartApi.js'

const emit = defineEmits(['navigate','logout'])

const searchQuery = ref('')
const activeFilter = ref('all')
const currentPage = ref(1)
const itemsPerPage = 6
const loading = ref(false)
const errorMessage = ref('')
const listings = ref([])
const categoryCycle = ['ELECTRONICS','BOOKS','FURNITURE','CLOTHING','SPORTS','NECESSITIES','TOYS_GAMES','OTHER']
const categoryIndex = ref(0)

const placeholderImage = './assets/logo.svg'

const cartCount = ref(0)
async function refreshCartCount() {
  try {
    const { data } = await cartApi.getCart()
    cartCount.value = (data || []).reduce((sum, it) => sum + (it.quantity || 0), 0)
  } catch { /* silent */ }
}

const handleCartUpdated = () => refreshCartCount()

const fetchListings = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await listingApi.getActiveListings()
    listings.value = data || []
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
})

onBeforeUnmount(() => {
  window.removeEventListener('cart-updated', handleCartUpdated)
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
    items = items.filter(i =>
      i.title?.toLowerCase().includes(q) ||
      i.description?.toLowerCase().includes(q) ||
      i.category?.toLowerCase().includes(q) ||
      i.condition?.toLowerCase().includes(q)
    )
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

const formatPrice = (price) => {
  if (price == null) return '$—'
  const num = typeof price === 'number' ? price : parseFloat(price)
  if (isNaN(num)) return '$—'
  return num.toLocaleString(undefined, { style: 'currency', currency: 'USD' })
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
  const placeholderImage = './assets/logo.svg'
  if (!path) return placeholderImage
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/uploads/')) return path
  if (path.startsWith('uploads/')) return '/' + path
  return path
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  width: 100%;
  background: #f8fafc;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* Header Styles */
.header {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-bottom: none;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
}

.header-content {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 1.25rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  box-sizing: border-box;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.logo {
  border-radius: 8px;
  filter: brightness(0) invert(1);
}

.logo-text {
  font-size: 1.75rem;
  font-weight: 800;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.nav-menu {
  display: flex;
  gap: 0.5rem;
  flex-grow: 1;
  justify-content: center;
}

.nav-link {
  text-decoration: none;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  transition: all 0.3s;
  white-space: nowrap;
  font-size: 0.95rem;
}

.nav-link:hover,
.nav-link.active {
  color: white;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  transform: translateY(-1px);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.dm-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.dm-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.dm-icon {
  font-size: 1.1rem;
}

.cart-button {
  position: relative;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  padding: 0.6rem 0.8rem;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.cart-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.cart-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ef4444;
  color: white;
  border-radius: 9999px;
  padding: 0 6px;
  font-size: 12px;
}

.profile-section {
  display: flex;
  align-items: center;
}

.profile-picture {
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
}

.logout-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  padding: 0.6rem 0.8rem;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.logout-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

/* Main Content */
.main-content {
  padding: 1.5rem 2rem;
}

.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
}

.search-section {
  margin-bottom: 1rem;
}

.search-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-input-wrapper {
  position: relative;
  width: 100%;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.25rem;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  font-size: 1rem;
  outline: none;
  transition: border 0.2s ease;
}

.search-input:focus {
  border-color: #818cf8;
  box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.15);
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
  gap: 0.5rem;
  margin: 1rem 0 1.25rem 0;
  flex-wrap: wrap;
  justify-content: center;
}

.filter-button {
  padding: 0.5rem 1rem;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: white;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-button:hover {
  background: #f8fafc;
}

.filter-button.active {
  background: #eef2ff;
  border-color: #818cf8;
  color: #312e81;
}

/* Items Grid */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 320px));
  gap: 1rem;
  justify-content: center;
}

.item-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  width: 100%;
  max-width: 320px;
}

.item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.item-image img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  background: #f1f5f9;
}

.item-content {
  padding: 0.75rem 1rem;
}

.item-title {
  font-weight: 700;
  margin: 0 0 0.25rem 0;
  color: #0f172a;
}

.item-price {
  color: #16a34a;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
}

.item-description {
  color: #475569;
  font-size: 0.9rem;
  margin: 0 0 0.5rem 0;
}

.ellipsis {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.meta-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.badge {
  background: #f1f5f9;
  color: #334155;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* Pagination */
.pagination {
  display: flex;
  gap: 1rem;
  align-items: center;
  justify-content: center;
  margin: 1.25rem 0 0.5rem 0;
}

.page-info {
  color: #334155;
}

.pagination-button {
  padding: 0.5rem 1rem;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: white;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pagination-button:hover:not(:disabled) {
  background: #f8fafc;
}

.pagination-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
