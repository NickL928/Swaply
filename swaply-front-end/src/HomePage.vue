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
          <a href="#" class="nav-link">Community</a>
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
            <img alt="Profile" class="profile-picture" src="./assets/logo.svg" width="40" height="40" />
          </div>
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

      <!-- Filter Buttons -->
      <div class="filter-section">
        <button
          class="filter-button"
          :class="{ active: activeFilter === 'all' }"
          @click="setFilter('all')"
        >
          For Sale
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
          :class="{ active: activeFilter === 'request' }"
          @click="setFilter('request')"
        >
          Buy Request
        </button>
      </div>

      <!-- Items Grid -->
      <div class="items-grid">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="item-card"
          @click="selectItem(item)"
        >
          <div class="item-image">
            <img :src="item.image" :alt="item.title" />
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <p class="item-price">{{ item.price }}</p>
            <p class="item-description">{{ item.description }}</p>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination">
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

const emit = defineEmits(['navigate'])

const searchQuery = ref('')
const activeFilter = ref('all')
const currentPage = ref(1)
const itemsPerPage = 6

// Mock data for items
const items = ref([
  {
    id: 1,
    title: 'Vintage Camera',
    price: '$120',
    description: 'Classic film camera in excellent condition',
    image: './assets/logo.svg',
    category: 'electronics'
  },
  {
    id: 2,
    title: 'Designer Handbag',
    price: '$85',
    description: 'Authentic leather handbag, barely used',
    image: './assets/logo.svg',
    category: 'fashion'
  },
  {
    id: 3,
    title: 'Gaming Console',
    price: '$200',
    description: 'PlayStation 4 with controllers and games',
    image: './assets/logo.svg',
    category: 'electronics'
  },
  {
    id: 4,
    title: 'Bicycle',
    price: '$150',
    description: 'Mountain bike in great condition',
    image: './assets/logo.svg',
    category: 'sports'
  },
  {
    id: 5,
    title: 'Textbooks',
    price: '$45',
    description: 'Computer Science textbooks bundle',
    image: './assets/logo.svg',
    category: 'books'
  },
  {
    id: 6,
    title: 'Coffee Maker',
    price: '$60',
    description: 'Espresso machine with milk frother',
    image: './assets/logo.svg',
    category: 'appliances'
  },
  {
    id: 7,
    title: 'Smartphone',
    price: '$300',
    description: 'iPhone in excellent condition with case',
    image: './assets/logo.svg',
    category: 'electronics'
  },
  {
    id: 8,
    title: 'Guitar',
    price: '$180',
    description: 'Acoustic guitar perfect for beginners',
    image: './assets/logo.svg',
    category: 'music'
  }
])

const filteredItems = computed(() => {
  let filtered = items.value

  // Apply search filter
  if (searchQuery.value) {
    filtered = filtered.filter(item =>
      item.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // Apply pagination
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filtered.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(items.value.length / itemsPerPage)
})

const setFilter = (filter) => {
  if (filter === 'sell') {
    // Navigate to post item page instead of filtering
    emit('navigate', 'post-item')
    return
  }

  activeFilter.value = filter
  currentPage.value = 1
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const selectItem = (item) => {
  console.log('Selected item:', item)
  // Add item selection logic here
}

onMounted(() => {
  // Initialize component
})
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

/* Main Content */
.main-content {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 2.5rem 2rem;
  box-sizing: border-box;
}

/* Search Section */
.search-section {
  margin-bottom: 2.5rem;
}

.search-container {
  max-width: 700px;
  margin: 0 auto;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 1.5rem;
  color: #6b7280;
  font-size: 1.3rem;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 1.5rem 1.5rem 1.5rem 3.5rem;
  border: 3px solid #e5e7eb;
  border-radius: 20px;
  font-size: 1.2rem;
  background: white;
  transition: all 0.3s;
  box-sizing: border-box;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.search-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 5px rgba(79, 70, 229, 0.1), 0 8px 20px rgba(0, 0, 0, 0.1);
}

/* Filter Section */
.filter-section {
  display: flex;
  gap: 1.25rem;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
  justify-content: center;
}

.filter-button {
  padding: 1rem 2rem;
  border: 2px solid #e5e7eb;
  background: white;
  border-radius: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  color: #6b7280;
  font-size: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-button:hover,
.filter-button.active {
  border-color: #4f46e5;
  color: white;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.3);
}

/* Items Grid */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2.5rem;
  margin-bottom: 3rem;
}

.item-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s;
  border: 1px solid #f1f5f9;
}

.item-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: #4f46e5;
}

.item-image {
  width: 100%;
  height: 220px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.item-image::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 49%, rgba(79, 70, 229, 0.05) 50%, transparent 51%);
}

.item-image img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  opacity: 0.4;
  z-index: 1;
  position: relative;
}

.item-content {
  padding: 2rem;
}

.item-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.75rem;
  line-height: 1.3;
}

.item-price {
  font-size: 1.75rem;
  font-weight: 800;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 1rem;
}

.item-description {
  color: #6b7280;
  font-size: 1rem;
  line-height: 1.6;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2.5rem;
  margin-top: 3rem;
}

.pagination-button {
  padding: 1rem 2rem;
  border: 2px solid #e5e7eb;
  background: white;
  border-radius: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  color: #6b7280;
  font-size: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.pagination-button:hover:not(:disabled) {
  border-color: #4f46e5;
  color: white;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.3);
}

.pagination-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  font-weight: 700;
  color: #374151;
  font-size: 1.1rem;
  padding: 1rem 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Mobile Responsiveness */
@media (max-width: 1200px) {
  .header-content {
    padding: 1rem 1.5rem;
  }

  .main-content {
    padding: 2rem 1.5rem;
  }

  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 2rem;
  }
}

@media (max-width: 1024px) {
  .nav-menu {
    display: none;
  }

  .header-content {
    justify-content: space-between;
  }

  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    gap: 1rem;
    padding: 1rem;
  }

  .logo-text {
    font-size: 1.5rem;
  }

  .filter-section {
    justify-content: center;
    gap: 1rem;
  }

  .filter-button {
    padding: 0.75rem 1.5rem;
    font-size: 0.9rem;
  }

  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 1.5rem;
  }

  .pagination {
    flex-direction: column;
    gap: 1rem;
  }

  .main-content {
    padding: 1.5rem 1rem;
  }
}

@media (max-width: 480px) {
  .items-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .search-input {
    padding: 1.25rem 1.25rem 1.25rem 3rem;
    font-size: 1rem;
    border-radius: 16px;
  }

  .search-icon {
    left: 1.25rem;
    font-size: 1.1rem;
  }

  .filter-button {
    padding: 0.6rem 1.25rem;
    font-size: 0.85rem;
  }

  .item-content {
    padding: 1.5rem;
  }

  .item-title {
    font-size: 1.2rem;
  }

  .item-price {
    font-size: 1.5rem;
  }
}
</style>
