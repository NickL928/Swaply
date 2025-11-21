<template>
  <div class="detail-wrapper">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <button @click="goBack" class="hero-back-btn">← Back to Browse</button>
        <h1 class="hero-title">🔍 Product Details</h1>
      </div>
    </section>

    <!-- Main Content -->
    <main class="main-content">
      <div v-if="listing" class="content-wrap">
        <div class="detail-grid">
          <!-- Image Gallery Section -->
          <div class="image-section">
            <div class="image-gallery">
              <div class="main-image-container">
                <img :src="resolveImage(listing.imageUrl)" :alt="listing?.title || 'Listing image'" class="main-image" />
              </div>
            </div>
          </div>

          <!-- Product Info Section -->
          <div class="info-section">
            <div class="product-header">
              <h1 class="product-title">{{ listing.title }}</h1>
              <div class="badges-row">
                <span class="badge category-badge">
                  {{ getCategoryIcon(listing.category) }} {{ prettyEnum(listing.category) }}
                </span>
                <span class="badge condition-badge" :class="getConditionClass(listing.condition)">
                  {{ prettyEnum(listing.condition) }}
                </span>
              </div>
            </div>

            <!-- Seller Info Card -->
            <div class="seller-card" v-if="listing.sellerUsername">
              <div class="seller-header">
                <img class="seller-avatar" :src="resolveImage(listing.sellerProfileImageUrl)" alt="Seller avatar" />
                <div class="seller-details">
                  <span class="seller-label">Seller</span>
                  <span class="seller-name">{{ listing.sellerUsername }}</span>
                </div>
              </div>
              <button v-if="isOwnListing" class="edit-btn" @click="goEdit">
                ✏️ Edit Listing
              </button>
            </div>

            <!-- Price Card -->
            <div class="price-card">
              <div class="price-label">Price</div>
              <div class="price-value">{{ formatPrice(listing.price) }}</div>
            </div>

            <!-- Description -->
            <div class="description-card">
              <h3 class="description-title">Description</h3>
              <p class="description-text">{{ listing.description }}</p>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons">
              <button class="add-to-cart-btn" @click="add" :disabled="adding || isOwnListing">
                <span class="btn-icon">🛒</span>
                {{ isOwnListing ? 'Your Listing' : (adding ? 'Adding...' : 'Add to Cart') }}
              </button>
              <button class="view-cart-btn" @click="goCart">
                View Cart
              </button>
            </div>

            <!-- Messages -->
            <div v-if="isOwnListing" class="info-message">
              ℹ️ This is your listing. You cannot add it to your cart.
            </div>
            <div v-else-if="msg" class="success-message">
              ✓ {{ msg }}
            </div>
          </div>
        </div>
      </div>
      <div v-else class="loading-state">
        <div class="loader"></div>
        <p>Loading product details...</p>
      </div>
    </main>
  </div>
</template>

<script setup>
// ...existing code...
import { ref, onMounted, computed } from 'vue'
import listingApi from './services/listingApi.js'
import cartApi from './services/cartApi.js'
import { fmtFt } from './services/currency.js'
const props = defineProps({ listingId: { type: Number, required: true } })
const emit = defineEmits(['navigate'])
const listing = ref(null)
const adding = ref(false)
const msg = ref('')

const me = (() => { try { return JSON.parse(localStorage.getItem('user')) } catch { return null } })()
const myId = me?.userId || me?.id
const myName = me?.userName || me?.username

const isOwnListing = computed(() => {
  const l = listing.value
  if (!l) return false
  if (myId && (l.sellerId === myId || l.userId === myId || l.ownerId === myId)) return true
  if (myName && l.sellerUsername && l.sellerUsername === myName) return true
  return false
})

const load = async () => {
  const { data } = await listingApi.getListing(props.listingId)
  listing.value = data
}

onMounted(load)

const add = async () => {
  if (isOwnListing.value) return
  try {
    adding.value = true
    await cartApi.addToCart(listing.value.listingId, 1)
    window.dispatchEvent(new Event('cart-updated'))
    msg.value = 'Added to cart'
    setTimeout(()=> msg.value='', 2000)
  } catch(e) {
    if (e.response?.status === 401) {
      msg.value = 'Please sign in to add items to your cart'
    } else if (e.response?.status === 403) {
      msg.value = e.response?.data?.message || 'Forbidden: You may be adding your own item or lack permission.'
    } else {
      msg.value = 'Failed: ' + (e.response?.data?.message || e.message)
    }
  } finally {
    adding.value = false
  }
}
const goBack = () => emit('navigate','home')
const goCart = () => emit('navigate','cart')
const formatPrice = (p)=> fmtFt(p)
const prettyEnum = (val) => {
  if (!val) return ''
  return String(val)
    .toLowerCase()
    .split('_')
    .map(w => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}
const resolveImage = (path)=>{
  if(!path) return './assets/logo.png'
  if(path.startsWith('http')) return path
  if(path.startsWith('/uploads/')) return path
  if(path.startsWith('uploads/')) return '/' + path
  return path
}
const goEdit = () => {
  if (!listing.value) return
  emit('navigate','post-item', { listingId: listing.value.listingId })
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
.detail-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 2rem 3rem 2rem;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-back-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  margin-bottom: 1rem;
  display: inline-block;
}

.hero-back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateX(-4px);
}

.hero-title {
  font-size: 2rem;
  font-weight: 800;
  color: white;
  margin: 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* Main Content */
.main-content {
  padding: 0 2rem 4rem 2rem;
  background: #f8fafc;
  border-radius: 32px 32px 0 0;
  margin-top: -2rem;
  position: relative;
  z-index: 2;
  min-height: 60vh;
}

.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 3rem;
}

/* Detail Grid */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
}

/* Image Section */
.image-section {
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.image-gallery {
  background: white;
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.main-image-container {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.main-image:hover {
  transform: scale(1.05);
}

/* Info Section */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.product-header {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.product-title {
  font-size: 2rem;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 1rem 0;
  line-height: 1.2;
}

.badges-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.badge {
  padding: 0.5rem 1rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.category-badge {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  color: #667eea;
}

.condition-badge {
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.condition-new {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46;
}

.condition-like-new {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
}

.condition-good {
  background: linear-gradient(135deg, #e9d5ff 0%, #d8b4fe 100%);
  color: #6b21a8;
}

.condition-fair {
  background: linear-gradient(135deg, #fed7aa 0%, #fdba74 100%);
  color: #92400e;
}

.condition-poor {
  background: linear-gradient(135deg, #e5e7eb 0%, #d1d5db 100%);
  color: #374151;
}

/* Seller Card */
.seller-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seller-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.seller-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.seller-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.seller-label {
  font-size: 0.75rem;
  color: #94a3b8;
  text-transform: uppercase;
  font-weight: 600;
}

.seller-name {
  font-size: 1rem;
  color: #1e293b;
  font-weight: 700;
}

.edit-btn {
  padding: 0.5rem 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #475569;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.edit-btn:hover {
  background: white;
  border-color: #667eea;
  color: #667eea;
}

/* Price Card */
.price-card {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid #fbbf24;
}

.price-label {
  font-size: 0.9rem;
  color: #92400e;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.price-value {
  font-size: 3rem;
  font-weight: 900;
  color: #78350f;
  line-height: 1;
}

/* Description Card */
.description-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.description-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1rem 0;
}

.description-text {
  color: #475569;
  line-height: 1.7;
  font-size: 1rem;
  margin: 0;
  white-space: pre-wrap;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.add-to-cart-btn {
  flex: 1;
  padding: 1.25rem 2rem;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.add-to-cart-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.4);
}

.add-to-cart-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.view-cart-btn {
  padding: 1.25rem 2rem;
  background: white;
  color: #475569;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-cart-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.btn-icon {
  font-size: 1.3rem;
}

/* Messages */
.info-message {
  background: #eff6ff;
  color: #1e40af;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  border-left: 4px solid #3b82f6;
  font-weight: 600;
}

.success-message {
  background: #d1fae5;
  color: #065f46;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  border-left: 4px solid #10b981;
  font-weight: 600;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
}

.loader {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: #64748b;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .image-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 1.5rem 1rem 2rem 1rem;
  }

  .main-content {
    padding: 0 1rem 3rem 1rem;
  }

  .content-wrap {
    padding-top: 2rem;
  }

  .product-title {
    font-size: 1.5rem;
  }

  .price-value {
    font-size: 2.5rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .add-to-cart-btn,
  .view-cart-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .product-header,
  .seller-card,
  .price-card,
  .description-card {
    padding: 1.5rem;
  }

  .seller-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .edit-btn {
    width: 100%;
  }
}
</style>
