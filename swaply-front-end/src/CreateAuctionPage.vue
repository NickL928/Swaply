<template>
  <div class="create-wrap">
    <header class="top">
      <h1 class="title">Create Auction</h1>
      <div class="actions">
        <button class="btn back" @click="back">← Back</button>
      </div>
    </header>

    <div class="card">
      <div class="state error" v-if="error">{{ error }}</div>
      <form class="form" @submit.prevent="submit">
        <div class="field">
          <label>Listing</label>
          <select v-model.number="listingId" required>
            <option disabled value="">Select listing</option>
            <option v-for="l in myListings" :key="l.listingId" :value="l.listingId">{{ l.title }}</option>
          </select>
        </div>
        <div class="grid2">
          <div class="field">
            <label>Starting price</label>
            <input type="number" step="0.01" v-model.number="startingPrice" required />
          </div>
          <div class="field">
            <label>Min increment</label>
            <input type="number" step="0.01" v-model.number="minIncrement" required />
          </div>
        </div>
        <div class="field">
          <label>End time</label>
          <input type="datetime-local" v-model="endTime" required />
        </div>

        <div class="actions-row">
          <button type="submit" class="btn primary" :disabled="busy">{{ busy ? 'Creating…' : 'Create Auction' }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import auctionApi from './services/auctionApi.js'
import listingApi from './services/listingApi.js'

const emit = defineEmits(['navigate'])

const myListings = ref([])
const listingId = ref('')
const startingPrice = ref(null)
const minIncrement = ref(null)
const endTime = ref('')
const busy = ref(false)
const error = ref('')

const me = (()=>{ try { return JSON.parse(localStorage.getItem('user')) } catch { return null } })()
const myId = me?.userId || me?.id

const load = async () => {
  if (!myId) { error.value = 'Please sign in'; return }
  try {
    const { data } = await listingApi.getListingsByUser(myId)
    myListings.value = (data || []).filter(l => l.status === 'ACTIVE')
  } catch(e){ error.value = 'Failed to load your listings' }
}

onMounted(load)

const submit = async () => {
  if (!myId) { error.value = 'Please sign in'; return }
  try {
    busy.value = true; error.value = ''
    const payload = { listingId: listingId.value, startingPrice: startingPrice.value, minIncrement: minIncrement.value, endTime: endTime.value }
    const { data } = await auctionApi.createAuction(myId, payload)
    emit('navigate','auction-detail',{ auctionId: data.auctionId })
  } catch(e){ error.value = e.response?.data || e.message }
  finally { busy.value = false }
}

const back = () => emit('navigate','bidding')
</script>

<style scoped>
.create-wrap{ min-height:100vh; background:#f8fafc; padding:2rem 1rem 3rem; display:flex; flex-direction:column; align-items:center }
.top{ width:100%; max-width:900px; display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem }
.title{ margin:0; font-size:1.8rem; font-weight:800; background:linear-gradient(135deg,#4f46e5 0%, #7c3aed 100%); -webkit-background-clip:text; background-clip:text; color:transparent }
.actions{ display:flex; gap:.75rem }

.card{ width:100%; max-width:900px; background:#fff; border:1px solid #e2e8f0; border-radius:16px; box-shadow:0 10px 24px rgba(15,23,42,.06); padding:1rem 1.25rem }
.state.error{ color:#b91c1c; font-weight:700; margin-bottom:.5rem }

.form{ display:flex; flex-direction:column; gap:1rem }
.field label{ font-weight:800; color:#0f172a; display:block; margin-bottom:.35rem }
.field input, .field select{ width:100%; padding:.7rem .8rem; border-radius:12px; border:2px solid #e5e7eb; font-weight:700 }
.field input:focus, .field select:focus{ outline:none; border-color:#4f46e5; box-shadow:0 0 0 4px rgba(79,70,229,.12) }
.grid2{ display:grid; grid-template-columns:1fr 1fr; gap:1rem }

.actions-row{ display:flex; justify-content:flex-end }
.btn{ cursor:pointer; border:none; border-radius:12px; font-weight:800; padding:.75rem 1.1rem }
.primary{ background:linear-gradient(135deg,#4f46e5 0%, #7c3aed 100%); color:#fff }
.back{ background:#e2e8f0; color:#0f172a }

@media(max-width:640px){ .grid2{ grid-template-columns:1fr } }
</style>
