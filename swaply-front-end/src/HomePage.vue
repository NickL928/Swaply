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
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import listingApi from './services/listingApi.js'

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

onMounted(fetchListings)

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
  console.log('Selected item:', item.listingId)
}

const logout = () => emit('logout')

const goProfile = () => emit('navigate', 'profile')

const goCommunity = () => emit('navigate', 'community')

const formatPrice = (price) => {
  if (price == null) return '$—'
  const num = typeof price === 'number' ? price : parseFloat(price)
  if (isNaN(num)) return '$—'
  return num.toLocaleString(undefined, { style: 'currency', currency: 'USD' })
}

const prettyEnum = (val) => {
  if (!val) return ''
  return val
    .toLowerCase()
    .split('_')
    .map(w => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}

const resolveImage = (path) => {
  if (!path) return placeholderImage
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/uploads/')) return listingApi.BASE_URL + path
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

.profile-section {
  position: relative;
}

.profile-picture {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
  filter: brightness(0) invert(1);
  background: rgba(255, 255, 255, 0.1);
  padding: 5px;
}

.profile-picture:hover {
  border-color: rgba(255, 255, 255, 0.6);
  transform: scale(1.05);
}

.logout-button {
  background: #ef4444;
  color: #fff;
  border: none;
  padding: 0.65rem 1rem;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: background .25s, transform .25s;
}

.logout-button:hover {
  background: #dc2626;
  transform: translateY(-2px);
}

/* Search Section */
.search-section {
  padding: 1.5rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin: 1.5rem 2rem 0;
}

.search-container {
  display: flex;
  justify-content: center;
}

.search-input-wrapper {
  position: relative;
  width: 100%;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  color: #a0aec0;
}

.search-input {
  width: 100%;
  padding: 0.75rem 2rem 0.75rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #4f46e5;
}

/* Status Bar */
.status-bar {
  text-align: center;
  padding: 0.75rem 0;
  font-weight: 500;
  color: #4a5568;
  margin: 0 2rem;
}

/* Filter Section */
.filter-section {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin: 1.5rem 0;
}

.filter-button {
  background: #edf2f7;
  color: #4a5568;
  border: 2px solid transparent;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  flex: 1;
  text-align: center;
}

.filter-button.active {
  background: #4f46e5;
  color: white;
  border-color: #4f46e5;
}

.filter-button:hover {
  background: #e2e8f0;
}

/* Items Grid */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
  padding: 0 2rem;
  margin-bottom: 2rem;
}

.item-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  position: relative;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.item-image {
  height: 150px;
  background: #f7fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image img {
  max-height: 100%;
  max-width: 100%;
  object-fit: contain;
}

.item-content {
  padding: 1rem;
}

.item-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0.5rem 0;
}

.item-price {
  font-size: 1rem;
  font-weight: 500;
  color: #4f46e5;
  margin: 0.5rem 0;
}

.item-description {
  font-size: 0.875rem;
  color: #718096;
  margin: 0.5rem 0;
}

/* Meta Row */
.meta-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.badge {
  background: #e2e8f0;
  color: #4a5568;
  padding: 0.375rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin: 2rem 0;
}

.pagination-button {
  background: #4f46e5;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s, transform 0.3s;
}

.pagination-button:disabled {
  background: #cbd5e0;
  cursor: not-allowed;
}

.page-info {
  font-size: 1rem;
  color: #4a5568;
}
</style>
