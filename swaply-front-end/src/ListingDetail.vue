<template>
  <div class="detail-container" v-if="listing">
    <button @click="goBack" class="back-btn">← Back</button>
    <div class="info">
      <img :src="resolveImage(listing.imageUrl)" class="pic" />
      <div class="meta">
        <h1>{{ listing.title }}</h1>
        <p class="price">{{ formatPrice(listing.price) }}</p>
        <p>Category: {{ listing.category }} | Condition: {{ listing.condition }}</p>
        <p>Seller: {{ listing.sellerUsername }}</p>
        <p class="desc">{{ listing.description }}</p>
        <div class="actions">
          <input type="number" v-model.number="qty" min="1" />
          <button @click="add" :disabled="adding">Add to Cart</button>
          <button class="secondary" @click="goCart">Go to Cart</button>
          <span v-if="msg" class="msg">{{ msg }}</span>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading">Loading...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import listingApi from './services/listingApi.js'
import cartApi from './services/cartApi.js'
const props = defineProps({ listingId: { type: Number, required: true } })
const emit = defineEmits(['navigate'])
const listing = ref(null)
const qty = ref(1)
const adding = ref(false)
const msg = ref('')
const load = async () => {
  const { data } = await listingApi.getListing(props.listingId)
  listing.value = data
}
onMounted(load)
const add = async () => {
  try {
    adding.value = true
    await cartApi.addToCart(listing.value.listingId, qty.value)
    window.dispatchEvent(new Event('cart-updated'))
    msg.value = 'Added to cart'
    setTimeout(()=> msg.value='', 2000)
  } catch(e) {
    msg.value = 'Failed: ' + (e.response?.data?.message || e.message)
  } finally {
    adding.value = false
  }
}
const goBack = () => emit('navigate','home')
const goCart = () => emit('navigate','cart')
const formatPrice = (p)=> (p==null? '—' : Number(p).toLocaleString(undefined,{style:'currency',currency:'USD'}))
const resolveImage = (path)=>{
  if(!path) return './assets/logo.svg'
  if(path.startsWith('http')) return path
  if(path.startsWith('/uploads/')) return listingApi.BASE_URL + path
  return path
}
</script>

<style scoped>
.detail-container { padding:2rem; }
.info { display:flex; gap:2rem; flex-wrap:wrap; }
.pic { width:320px; height:320px; object-fit:cover; border-radius:12px; background:#f1f5f9; }
.meta { flex:1; min-width:260px; }
.checkout:disabled { opacity:.5; cursor:not-allowed; }
.success { margin-top:1rem; color:#16a34a; font-weight:600; }
.empty { margin-top:2rem; color:#555; }
.back { margin-top:2rem; background:none; border:none; color:#4f46e5; cursor:pointer; }
.actions { display:flex; gap:.75rem; align-items:center; flex-wrap:wrap; }
.actions .secondary { background:#e2e8f0; color:#1e293b; }
.actions .secondary:hover { background:#cbd5e1; }
.msg { color:#16a34a; font-weight:600; }
</style>
