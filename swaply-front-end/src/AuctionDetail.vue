<template>
  <div class="auction-detail-wrapper">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <button @click="back" class="hero-back-btn">← Back to Auctions</button>
        <h1 class="hero-title">🔨 Live Auction</h1>
      </div>
    </section>

    <!-- Main Content -->
    <main class="main-content">
      <div v-if="auction" class="content-wrap">
        <div class="auction-grid">
          <!-- Image Section -->
          <div class="image-section">
            <div class="image-gallery">
              <div class="live-indicator">
                <span class="live-dot"></span>
                <span class="live-text">LIVE</span>
              </div>
              <div class="countdown-overlay" :class="getUrgencyClass()">
                <span class="countdown-icon">⏱️</span>
                <span class="countdown-text">{{ timeLeft(auction.endTime) }}</span>
              </div>
              <div class="main-image-container">
                <img :src="resolveImage(auction.imageUrl)" :alt="auction.title" class="main-image" />
              </div>
            </div>
          </div>

          <!-- Auction Info Section -->
          <div class="info-section">
            <!-- Product Header -->
            <div class="product-header">
              <h1 class="product-title">{{ auction.title }}</h1>
              <div class="auction-status-badge">
                🔥 Active Auction
              </div>
            </div>

            <!-- Seller Card -->
            <div class="seller-card" v-if="auction.sellerUsername">
              <div class="seller-header">
                <img class="seller-avatar" :src="resolveImage(auction.sellerProfileImageUrl)" alt="seller" />
                <div class="seller-details">
                  <span class="seller-label">Auctioneer</span>
                  <span class="seller-name">{{ auction.sellerUsername }}</span>
                </div>
              </div>
            </div>

            <!-- Current Bid Card -->
            <div class="current-bid-card">
              <div class="bid-info-row">
                <div class="bid-info-item main">
                  <span class="bid-label">Current Bid</span>
                  <span class="bid-value">{{ money(auction.currentPrice) }}</span>
                </div>
                <div class="bid-info-item">
                  <span class="bid-label">Min Increment</span>
                  <span class="bid-increment">+{{ money(auction.minIncrement) }}</span>
                </div>
              </div>
              <div class="top-bidder-row">
                <span class="top-bidder-label">👑 Top Bidder:</span>
                <span class="top-bidder-name">{{ auction.highestBidderUsername || 'No bids yet' }}</span>
              </div>
            </div>

            <!-- Bid Placement Card -->
            <div class="bid-placement-card">
              <h3 class="bid-card-title">Place Your Bid</h3>
              <div class="bid-input-group">
                <label class="bid-input-label">Your Bid Amount</label>
                <div class="bid-input-wrapper">
                  <input
                    type="number"
                    step="0.01"
                    v-model.number="amount"
                    class="bid-input"
                    placeholder="Enter amount..."
                  />
                  <button class="quick-bid-btn" @click="setMin" :disabled="busy">
                    + Min
                  </button>
                </div>
              </div>

              <div class="bid-actions">
                <button class="place-bid-btn" @click="place" :disabled="busy">
                  <span class="btn-icon">🔨</span>
                  {{ busy ? 'Placing Bid...' : 'Place Bid' }}
                </button>
              </div>

              <p v-if="msg" :class="{'success-msg': ok, 'error-msg': !ok}" class="bid-message">
                {{ ok ? '✓' : '⚠️' }} {{ msg }}
              </p>
            </div>

            <!-- Auction Details -->
            <div class="auction-details-card">
              <h3 class="details-title">Auction Information</h3>
              <div class="details-list">
                <div class="detail-item">
                  <span class="detail-label">Starting Price</span>
                  <span class="detail-value">{{ money(auction.startingPrice || auction.currentPrice) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Time Remaining</span>
                  <span class="detail-value countdown-value">{{ timeLeft(auction.endTime) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Status</span>
                  <span class="detail-value status-active">Active</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="loading-state">
        <div class="loader"></div>
        <p>Loading auction details...</p>
      </div>
    </main>
  </div>
</template>

<script setup>
// ...existing code...
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import auctionApi from './services/auctionApi.js'
import { fmtFt } from './services/currency.js'

const props = defineProps({ auctionId: { type: Number, required: true } })
const emit = defineEmits(['navigate'])

const auction = ref(null)
const amount = ref(null)
const busy = ref(false)
const msg = ref('')
const ok = ref(false)

const me = (()=>{ try { return JSON.parse(localStorage.getItem('user')) } catch { return null } })()
const myId = me?.userId || me?.id

const load = async () => {
  const { data } = await auctionApi.getAuction(props.auctionId)
  auction.value = data
  setMin()
}

const ended = () => auction.value && new Date(auction.value.endTime).getTime() <= Date.now()

const refreshTimer = ref(null)
const startDetailAutoRefresh = () => {
  if (refreshTimer.value) return
  refreshTimer.value = setInterval(async () => {
    try {
      const { data } = await auctionApi.getAuction(props.auctionId)
      auction.value = data
    } catch (e) { /* ignore transient */ }
  }, 15000)
}

onMounted(() => { load(); startDetailAutoRefresh() })
watch(()=>props.auctionId, load)

onBeforeUnmount(() => { if (refreshTimer.value) { clearInterval(refreshTimer.value); refreshTimer.value = null } })

const setMin = () => {
  if (!auction.value) return
  const cur = Number(auction.value.currentPrice||0)
  const step = Number(auction.value.minIncrement||0)
  amount.value = +(cur + step).toFixed(2)
}

const place = async () => {
  if (!myId) { msg.value = 'Please sign in'; ok.value = false; return }
  try {
    busy.value = true; msg.value = ''
    const { data } = await auctionApi.placeBid(auction.value.auctionId, myId, amount.value)
    auction.value = data
    setMin()
    ok.value = true; msg.value = 'Bid placed successfully!'
  } catch(e){
    ok.value = false
    msg.value = e.response?.data || e.message
  } finally { busy.value = false }
}

const back = () => emit('navigate','bidding')

const money = (v)=> fmtFt(v)
const resolveImage = (p)=>{ if(!p) return './assets/logo.png'; if(p.startsWith('http')) return p; if(p.startsWith('/uploads/')) return p; if(p.startsWith('uploads/')) return '/' + p; return p }

const timeLeft = (end) => {
  if (!end) return '—'
  const endTs = new Date(end).getTime()
  const now = Date.now()
  const diff = Math.max(0, endTs - now)
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  if (h > 0) return `${h}h ${m}m ${s}s`
  return `${m}m ${s}s`
}

const getUrgencyClass = () => {
  if (!auction.value || !auction.value.endTime) return 'normal'
  const endTs = new Date(auction.value.endTime).getTime()
  const now = Date.now()
  const diff = Math.max(0, endTs - now)
  const minutes = diff / 60000
  if (minutes < 5) return 'urgent'
  if (minutes < 30) return 'soon'
  return 'normal'
}
</script>

<style scoped>
.auction-detail-wrapper {
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

/* Auction Grid */
.auction-grid {
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
  position: relative;
}

.live-indicator {
  position: absolute;
  top: 2rem;
  left: 2rem;
  background: rgba(239, 68, 68, 0.95);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  animation: pulse 2s ease-in-out infinite;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: blink 1.5s ease-in-out infinite;
}

.live-text {
  letter-spacing: 0.1em;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.countdown-overlay {
  position: absolute;
  top: 2rem;
  right: 2rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 700;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.countdown-overlay.urgent {
  background: rgba(239, 68, 68, 0.95);
  color: white;
  animation: urgentBlink 1s ease-in-out infinite;
}

.countdown-overlay.soon {
  background: rgba(251, 146, 60, 0.95);
  color: white;
}

.countdown-overlay.normal {
  background: rgba(255, 255, 255, 0.95);
  color: #1e293b;
}

@keyframes urgentBlink {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.05); }
}

.main-image-container {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
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

/* Product Header */
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

.auction-status-badge {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 700;
  display: inline-block;
}

/* Seller Card */
.seller-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
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

/* Current Bid Card */
.current-bid-card {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid #fbbf24;
}

.bid-info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid rgba(180, 83, 9, 0.2);
}

.bid-info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.bid-info-item.main {
  flex: 1;
}

.bid-label {
  font-size: 0.85rem;
  color: #92400e;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.bid-value {
  font-size: 2.5rem;
  font-weight: 900;
  color: #78350f;
  line-height: 1;
}

.bid-increment {
  font-size: 1.25rem;
  font-weight: 800;
  color: #92400e;
}

.top-bidder-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bidder-label {
  font-size: 0.9rem;
  color: #92400e;
  font-weight: 600;
}

.top-bidder-name {
  font-size: 1rem;
  color: #78350f;
  font-weight: 800;
}

/* Bid Placement Card */
.bid-placement-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.bid-card-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.5rem 0;
}

.bid-input-group {
  margin-bottom: 1.5rem;
}

.bid-input-label {
  display: block;
  font-size: 0.9rem;
  color: #475569;
  font-weight: 600;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.bid-input-wrapper {
  display: flex;
  gap: 0.75rem;
}

.bid-input {
  flex: 1;
  padding: 1rem 1.25rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1.25rem;
  font-weight: 700;
  transition: all 0.3s ease;
  background: white;
}

.bid-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.quick-bid-btn {
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.quick-bid-btn:hover:not(:disabled) {
  background: white;
  border-color: #667eea;
  color: #667eea;
}

.quick-bid-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bid-actions {
  margin-bottom: 1rem;
}

.place-bid-btn {
  width: 100%;
  padding: 1.25rem 2rem;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.place-bid-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.4);
}

.place-bid-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  font-size: 1.3rem;
}

.bid-message {
  margin: 0;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  font-weight: 600;
  animation: slideIn 0.3s ease;
}

.success-msg {
  background: #d1fae5;
  color: #065f46;
  border-left: 4px solid #10b981;
}

.error-msg {
  background: #fee2e2;
  color: #dc2626;
  border-left: 4px solid #ef4444;
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

/* Auction Details Card */
.auction-details-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.details-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.5rem 0;
}

.details-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 12px;
}

.detail-label {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 600;
}

.detail-value {
  font-size: 1rem;
  color: #1e293b;
  font-weight: 700;
}

.countdown-value {
  color: #ef4444;
  font-variant-numeric: tabular-nums;
}

.status-active {
  color: #10b981;
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
  .auction-grid {
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

  .bid-value {
    font-size: 2rem;
  }

  .bid-input-wrapper {
    flex-direction: column;
  }

  .quick-bid-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .product-header,
  .seller-card,
  .current-bid-card,
  .bid-placement-card,
  .auction-details-card {
    padding: 1.5rem;
  }

  .bid-info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>
