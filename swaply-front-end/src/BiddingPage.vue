<template>
  <div class="bidding-wrapper">
    <header class="top">
      <h1 class="title">Live Bidding</h1>
      <div class="actions">
        <button class="btn back" @click="goHome">← Home</button>
        <button class="btn primary" @click="goCreate">Create Auction</button>
      </div>
    </header>

    <div class="card">
      <div v-if="loading" class="state">Loading...</div>
      <div v-else-if="error" class="state error">{{ error }}</div>
      <div v-else>
        <div v-if="!auctions.length" class="state empty">No active auctions right now</div>
        <div v-else class="grid">
          <div class="auction-card" v-for="a in auctions" :key="a.auctionId">
            <div class="thumb">
              <img :src="resolveImage(a.imageUrl)" :alt="a.title" />
            </div>
            <div class="info">
              <h3 class="name">{{ a.title }}</h3>
              <div class="seller-row" v-if="a.sellerUsername">
                <img class="seller-avatar" :src="resolveImage(a.sellerProfileImageUrl)" alt="seller" />
                <span class="seller-name">{{ a.sellerUsername }}</span>
              </div>
              <div class="row"><span class="label">Current:</span><span class="val">{{ money(a.currentPrice) }}</span></div>
              <div class="row"><span class="label">Min step:</span><span class="val">{{ money(a.minIncrement) }}</span></div>
              <div class="row"><span class="label">Top bidder:</span><span class="val">{{ a.highestBidderUsername || '—' }}</span></div>
              <div class="row time"><span class="label">Ends in:</span><span class="val">{{ timeLeft(a.endTime) }}</span></div>
            </div>
            <div class="actions-row">
              <button class="btn secondary" @click="open(a)">Bid</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import auctionApi from './services/auctionApi.js'

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

onMounted(load)

const goHome = () => emit('navigate','home')
const goCreate = () => emit('navigate','create-auction')
const open = (a) => emit('navigate','auction-detail', { auctionId: a.auctionId })

const money = (v)=> v==null? '$—' : Number(v).toLocaleString(undefined,{style:'currency',currency:'USD'})
const resolveImage = (p)=>{ if(!p) return './assets/logo.svg'; if(p.startsWith('http')) return p; if(p.startsWith('/uploads/')) return p; if(p.startsWith('uploads/')) return '/' + p; return p }
const timeLeft = (end) => {
  if (!end) return '—'
  const endTs = new Date(end).getTime()
  const now = Date.now()
  const diff = Math.max(0, endTs - now)
  const m = Math.floor(diff / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  return `${m}m ${s}s`
}
</script>

<style scoped>
.bidding-wrapper{ min-height:100vh; background:#f8fafc; padding:2rem 1rem 3rem; display:flex; flex-direction:column; align-items:center }
.top{ width:100%; max-width:1100px; display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem }
.title{ margin:0; font-size:1.9rem; font-weight:800; background:linear-gradient(135deg,#4f46e5 0%, #7c3aed 100%); -webkit-background-clip:text; background-clip:text; color:transparent }
.actions{ display:flex; gap:.75rem }

.card{ width:100%; max-width:1100px; background:#fff; border:1px solid #e2e8f0; border-radius:16px; box-shadow:0 10px 24px rgba(15,23,42,.06); padding:1rem }
.state{ text-align:center; padding:2rem 0; font-weight:700; color:#475569 }
.state.error{ color:#b91c1c }
.state.empty{ color:#64748b }

.grid{ display:grid; grid-template-columns:repeat(auto-fill, minmax(260px, 1fr)); gap:1rem }
.auction-card{ background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; overflow:hidden; display:flex; flex-direction:column }
.thumb{ height:160px; background:#eef2f7; display:flex; align-items:center; justify-content:center; overflow:hidden }
.thumb img{ width:100%; height:100%; object-fit:cover }
.info{ padding:.75rem .9rem; display:flex; flex-direction:column; gap:.35rem }
.name{ margin:0 0 .25rem; font-size:1.05rem; font-weight:800; color:#111827 }
.row{ display:flex; justify-content:space-between; font-size:.92rem; color:#334155 }
.row .label{ color:#64748b }
.time .val{ font-weight:800; color:#0f172a }
.actions-row{ padding:.75rem .9rem; display:flex; justify-content:flex-end }

.btn{ cursor:pointer; border:none; border-radius:12px; font-weight:700; padding:.6rem .95rem }
.primary{ background:linear-gradient(135deg,#4f46e5 0%, #7c3aed 100%); color:#fff }
.secondary{ background:#e2e8f0; color:#0f172a }
.back{ background:#e2e8f0; color:#0f172a }
.seller-row{ display:flex; align-items:center; gap:.45rem; margin:.1rem 0 .35rem }
.seller-avatar{ width:20px; height:20px; border-radius:50%; object-fit:cover; background:#f1f5f9; border:1px solid #e2e8f0 }
.seller-name{ font-weight:700; color:#0f172a; font-size:.88rem }
</style>
