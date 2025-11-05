<template>
  <div class="detail" v-if="auction">
    <button class="btn link" @click="back">← Back</button>
    <div class="card">
      <div class="media"><img :src="resolveImage(auction.imageUrl)" :alt="auction.title" /></div>
      <div class="meta">
        <h1 class="title">{{ auction.title }}</h1>
        <div class="seller-info" v-if="auction.sellerUsername">
          <img class="seller-avatar" :src="resolveImage(auction.sellerProfileImageUrl)" alt="seller" />
          <span class="seller-name">{{ auction.sellerUsername }}</span>
        </div>
        <div class="line"><span class="lb">Current</span><span class="val">{{ money(auction.currentPrice) }}</span></div>
        <div class="line"><span class="lb">Min increment</span><span class="val">{{ money(auction.minIncrement) }}</span></div>
        <div class="line"><span class="lb">Top bidder</span><span class="val">{{ auction.highestBidderUsername || '—' }}</span></div>
        <div class="line"><span class="lb">Ends in</span><span class="val">{{ timeLeft(auction.endTime) }}</span></div>

        <div class="bid-box">
          <label>Amount</label>
          <input type="number" step="0.01" v-model.number="amount" />
          <div class="btns">
            <button class="btn ghost" @click="setMin" :disabled="busy">+ Min</button>
            <button class="btn primary" @click="place" :disabled="busy">{{ busy ? 'Placing…' : 'Place bid' }}</button>
          </div>
          <p v-if="msg" :class="{'ok': ok, 'err': !ok}" class="msg">{{ msg }}</p>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading">Loading...</div>
</template>

<script setup>
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
    ok.value = true; msg.value = 'Bid placed!'
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
  const m = Math.floor(diff / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  return `${m}m ${s}s`
}
</script>

<style scoped>
.detail{ min-height:100vh; background:#f8fafc; padding:2rem 1rem 3rem }
.loading{ padding:2rem; text-align:center; font-weight:700; color:#64748b }

.card{ max-width:1100px; margin:0 auto; display:grid; grid-template-columns:1fr 1fr; gap:1.25rem; background:#fff; border:1px solid #e5e7eb; border-radius:20px; box-shadow:0 16px 28px rgba(0,0,0,.08); padding:1.25rem }
.media{ border-radius:16px; overflow:hidden; background:#eef2f7; display:flex; align-items:center; justify-content:center; height:520px }
.media img{ width:100%; height:100%; object-fit:cover }
.meta{ display:flex; flex-direction:column; gap:.6rem }
.title{ margin:.25rem 0 .5rem; font-size:1.8rem; font-weight:800; color:#0f172a }

.seller-info{ display:flex; align-items:center; gap:.5rem }
.seller-avatar{ width:28px; height:28px; border-radius:50%; object-fit:cover; background:#f1f5f9; border:1px solid #e2e8f0 }
.seller-name{ font-weight:800; color:#0f172a }

.line{ display:flex; justify-content:space-between; font-weight:700; color:#334155 }
.line .lb{ color:#64748b }

.bid-box{ margin-top:.75rem; background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:1rem }
.bid-box label{ display:block; font-weight:800; color:#0f172a; margin-bottom:.4rem }
.bid-box input{ width:100%; padding:.7rem .8rem; border-radius:12px; border:2px solid #e5e7eb; font-weight:700 }
.bid-box input:focus{ outline:none; border-color:#4f46e5; box-shadow:0 0 0 4px rgba(79,70,229,.12) }
.btns{ display:flex; gap:.6rem; margin-top:.7rem }
.btn{ cursor:pointer; border:none; border-radius:12px; font-weight:800; padding:.75rem 1.1rem }
.primary{ background:linear-gradient(135deg,#4f46e5 0%, #7c3aed 100%); color:#fff }
.ghost{ background:#fff; border:1px solid #e2e8f0; color:#0f172a }
.link{ background:none; color:#4f46e5; border:none; font-weight:800; margin:0 0 .75rem; cursor:pointer }
.msg{ margin:.5rem 0 0; font-weight:700 }
.msg.ok{ color:#16a34a }
.msg.err{ color:#b91c1c }

@media (max-width: 960px){ .card{ grid-template-columns: 1fr } .media{ height:360px } }
</style>
