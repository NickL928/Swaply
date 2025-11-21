<template>
  <div class="bidding-wrapper">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">🔨 Live Auctions</h1>
        <p class="hero-subtitle">Bid on amazing items and win great deals</p>
        <div class="hero-actions">
          <button class="hero-btn secondary" @click="goHome">← Back to Home</button>
          <button class="hero-btn primary" @click="goCreate">
            <span class="btn-icon">✨</span> Create Auction
          </button>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <main class="main-content">
      <div class="content-wrap">
        <div v-if="loading" class="state-card loading">
          <div class="loader"></div>
          <p>Loading auctions...</p>
        </div>
        <div v-else-if="error" class="state-card error">
          <span class="error-icon">⚠️</span>
          <p>{{ error }}</p>
        </div>
        <div v-else>
          <div v-if="!auctions.length" class="state-card empty">
            <span class="empty-icon">📭</span>
            <h3>No Active Auctions</h3>
            <p>Be the first to create an auction!</p>
            <button class="create-btn" @click="goCreate">Create Auction</button>
          </div>
          <div v-else class="auctions-grid">
            <div class="auction-card" v-for="a in auctions" :key="a.auctionId" @click="open(a)">
              <div class="auction-image-wrapper">
                <img class="auction-image" :src="resolveImage(a.imageUrl)" :alt="a.title" />
                <div class="live-badge">🔴 LIVE</div>
                <div class="countdown-badge" :class="getCountdownClass(a.endTime)">
                  <span class="countdown-icon">⏱️</span>
                  <span class="countdown-text">{{ timeLeft(a.endTime) }}</span>
                </div>
              </div>
              <div class="auction-content">
                <h3 class="auction-title">{{ a.title }}</h3>

                <div class="seller-info" v-if="a.sellerUsername">
                  <img class="seller-avatar" :src="resolveImage(a.sellerProfileImageUrl)" :alt="a.sellerUsername" />
                  <div class="seller-details">
                    <span class="seller-label">Seller</span>
                    <span class="seller-name">{{ a.sellerUsername }}</span>
                  </div>
                </div>

                <div class="price-section">
                  <div class="current-price">
                    <span class="price-label">Current Bid</span>
                    <span class="price-value">{{ money(a.currentPrice) }}</span>
                  </div>
                  <div class="min-increment">
                    <span class="increment-label">Min +</span>
                    <span class="increment-value">{{ money(a.minIncrement) }}</span>
                  </div>
                </div>

                <div class="bidder-info">
                  <span class="bidder-label">Top Bidder:</span>
                  <span class="bidder-name">{{ a.highestBidderUsername || 'No bids yet' }}</span>
                </div>

                <button class="bid-button" @click.stop="open(a)">
                  <span class="bid-icon">🔨</span> Place Bid
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
// ...existing code...
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import auctionApi from './services/auctionApi.js'
import { fmtFt } from './services/currency.js'

const emit = defineEmits(['navigate'])

const auctions = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try { const { data } = await auctionApi.getActive(); auctions.value = data || [] }
  catch(e){ error.value = 'Failed to load auctions' }
  finally { loading.value = false }
}

const refreshInterval = ref(null)

const startAutoRefresh = () => {
  if (refreshInterval.value) return
  refreshInterval.value = setInterval(async () => {
    try {
      const { data } = await auctionApi.getActive()
      auctions.value = data || []
    } catch (e) { /* ignore transient */ }
  }, 15000)
}

const ticker = ref(null)
const tick = () => {
  _now.value = Date.now()
}
const _now = ref(Date.now())

onMounted(() => {
  load();
  startAutoRefresh();
  ticker.value = setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (refreshInterval.value) { clearInterval(refreshInterval.value); refreshInterval.value = null }
  if (ticker.value) { clearInterval(ticker.value); ticker.value = null }
})

const goHome = () => emit('navigate','home')
const goCreate = () => emit('navigate','create-auction')
const open = (a) => emit('navigate','auction-detail', { auctionId: a.auctionId })

const money = (v)=> fmtFt(v)
const resolveImage = (p)=>{ if(!p) return './assets/logo.png'; if(p.startsWith('http')) return p; if(p.startsWith('/uploads/')) return p; if(p.startsWith('uploads/')) return '/' + p; return p }

const timeLeft = (end) => {
  void _now.value
  if (!end) return '—'
  const endTs = new Date(end).getTime()
  const now = Date.now()
  const diff = Math.max(0, endTs - now)
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m ${s}s`
}

const getCountdownClass = (end) => {
  if (!end) return ''
  const endTs = new Date(end).getTime()
  const now = Date.now()
  const diff = Math.max(0, endTs - now)
  const minutes = diff / 60000
  if (minutes < 5) return 'urgent'
  if (minutes < 30) return 'soon'
  return 'normal'
}
</script>

<style scoped>
.bidding-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 3rem 2rem;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  color: white;
  margin: 0 0 1rem 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 2rem 0;
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
}

/* Main Content */
.main-content {
  padding: 2rem 2rem 4rem 2rem;
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
}

/* State Cards */
.state-card {
  background: white;
  border-radius: 16px;
  padding: 3rem 2rem;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.state-card.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
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

.state-card.error {
  color: #dc2626;
}

.error-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 1rem;
}

.state-card.empty {
  color: #64748b;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 1rem;
}

.state-card h3 {
  font-size: 1.5rem;
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.create-btn {
  margin-top: 1.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* Auctions Grid */
.auctions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.auction-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
}

.auction-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(239, 68, 68, 0.2);
  border-color: #ef4444;
}

.auction-image-wrapper {
  position: relative;
  height: 220px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
}

.auction-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.auction-card:hover .auction-image {
  transform: scale(1.1);
}

.live-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(239, 68, 68, 0.95);
  color: white;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
  z-index: 2;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.countdown-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 700;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.countdown-badge.urgent {
  background: rgba(239, 68, 68, 0.95);
  color: white;
  animation: blink 1s ease-in-out infinite;
}

.countdown-badge.soon {
  background: rgba(251, 146, 60, 0.95);
  color: white;
}

.countdown-badge.normal {
  background: rgba(255, 255, 255, 0.95);
  color: #1e293b;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.auction-content {
  padding: 1.25rem;
}

.auction-title {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0 0 1rem 0;
  color: #0f172a;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f5f9;
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

.price-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
}

.current-price {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.price-label {
  font-size: 0.75rem;
  color: #92400e;
  text-transform: uppercase;
  font-weight: 600;
}

.price-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #78350f;
}

.min-increment {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
}

.increment-label {
  font-size: 0.7rem;
  color: #92400e;
  font-weight: 600;
}

.increment-value {
  font-size: 0.9rem;
  font-weight: 700;
  color: #78350f;
}

.bidder-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.85rem;
}

.bidder-label {
  color: #64748b;
  font-weight: 600;
}

.bidder-name {
  color: #1e293b;
  font-weight: 700;
}

.bid-button {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.bid-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
}

.bid-icon {
  font-size: 1.2rem;
}
</style>
