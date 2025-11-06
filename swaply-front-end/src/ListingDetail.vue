<template>
  <div class="detail-page" v-if="listing">
    <button @click="goBack" class="back-btn" aria-label="Back to Home">← Back to Home</button>

    <section class="detail-card">
      <div class="image-panel">
        <div class="image-wrap">
          <img :src="resolveImage(listing.imageUrl)" :alt="listing?.title || 'Listing image'" />
        </div>
      </div>

      <div class="meta-panel">
        <h1 class="title">{{ listing.title }}</h1>
        <div class="seller-info" v-if="listing.sellerUsername">
          <img class="seller-avatar" :src="resolveImage(listing.sellerProfileImageUrl)" alt="Seller avatar" />
          <span class="seller-name">{{ listing.sellerUsername }}</span>
        </div>
        <div class="price">{{ formatPrice(listing.price) }}</div>

        <div class="badges">
          <span class="badge category">{{ prettyEnum(listing.category) }}</span>
          <span class="badge condition">{{ prettyEnum(listing.condition) }}</span>
        </div>

        <div class="desc">
          {{ listing.description }}
        </div>

        <div class="actions">
          <button class="btn primary" @click="add" :disabled="adding || isOwnListing">
            {{ isOwnListing ? 'Your listing' : (adding ? 'Adding...' : 'Add to Cart') }}
          </button>
          <button class="btn secondary" @click="goCart">Go to Cart</button>
        </div>

        <p v-if="isOwnListing" class="hint">You can't add your own item</p>
        <p v-else-if="msg" class="hint success">{{ msg }}</p>
      </div>
    </section>
  </div>
  <div v-else class="loading">Loading...</div>
</template>

<script setup>
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
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  width: 100%;
  background: #f8fafc;
  padding: 2rem 2rem 3rem;
  box-sizing: border-box;
}

.back-btn {
  background: none;
  border: none;
  color: #4f46e5;
  font-weight: 700;
  cursor: pointer;
  margin-bottom: 1rem;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
}
.back-btn:hover { text-decoration: underline; color: #4338ca; }

.detail-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.08);
  padding: 2rem;
}

.image-panel { display: flex; align-items: center; justify-content: center; }
.image-wrap {
  width: 100%;
  max-width: 520px;
  aspect-ratio: 1 / 1;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border: 1px solid #e5e7eb;
}
.image-wrap img { width: 100%; height: 100%; object-fit: cover; display: block; }

.meta-panel { display: flex; flex-direction: column; gap: 0.75rem; }
.title { font-size: 2rem; font-weight: 800; color: #111827; margin: 0 0 .25rem; }

.seller-info { display: flex; align-items: center; gap: .6rem; }
.seller-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; background:#f1f5f9; border:1px solid #e2e8f0 }
.seller-name { font-weight: 800; color: #0f172a; }

.price {
  font-size: 2rem;
  font-weight: 900;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent;
}

.badges { display: flex; flex-wrap: wrap; gap: .5rem; margin: .25rem 0 .5rem; }
.badge { padding: .35rem .7rem; border-radius: 12px; font-size: .8rem; font-weight: 700; letter-spacing: .3px; }
.badge.category { background: #eef2ff; color: #4338ca; }
.badge.condition { background: #f1f5f9; color: #475569; }

.desc {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 1rem 1.25rem;
  color: #374151;
  line-height: 1.6;
  min-height: 120px;
}

.actions { display: flex; gap: 1rem; align-items: center; flex-wrap: wrap; margin-top: .5rem; }
.btn { cursor: pointer; border: none; border-radius: 14px; font-weight: 800; padding: .9rem 1.4rem; transition: all .25s; }
.btn.primary { background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); color: #fff; box-shadow: 0 8px 20px rgba(79,70,229,.25); }
.btn.primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(79,70,229,.35); }
.btn.primary:disabled { opacity: .6; cursor: not-allowed; }
.btn.secondary { background: #e2e8f0; color: #1e293b; }
.btn.secondary:hover { background: #cbd5e1; }

.hint { margin-top: .25rem; font-weight: 700; color: #6b7280; }
.hint.success { color: #16a34a; }

.loading { padding: 2rem; text-align: center; font-weight: 700; color: #6b7280; }

@media (max-width: 1024px) {
  .detail-card { grid-template-columns: 1fr; }
  .image-wrap { max-width: 100%; }
}

@media (max-width: 640px) {
  .detail-page { padding: 1.25rem 1rem 2rem; }
  .title { font-size: 1.6rem; }
  .price { font-size: 1.6rem; }
}
</style>
