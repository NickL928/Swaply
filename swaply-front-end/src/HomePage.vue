<template>
  <div class="home-container">
    <AppHeader active="home" @navigate="(p)=>emit('navigate',p)" @logout="logout" />

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
              <div class="seller-row" v-if="item.sellerUsername">
                <img class="seller-avatar" :src="resolveImage(item.sellerProfileImageUrl)" alt="seller" />
                <span class="seller-name">{{ item.sellerUsername }}</span>
              </div>
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
import AppHeader from './components/AppHeader.vue'
import listingApi from './services/listingApi.js'
import cartApi from './services/cartApi.js'
import { fmtFt } from './services/currency.js'

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
