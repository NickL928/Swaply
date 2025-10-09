<template>
  <div class="cart-wrapper">
    <header class="cart-header">
      <h1>My Cart</h1>
      <div class="actions-top">
        <button class="back" @click="goHome">← Home</button>
        <button class="clear" v-if="items.length" @click="clearCart" :disabled="loading">Clear Cart</button>
      </div>
    </header>

    <div v-if="loading" class="state">Loading...</div>
    <div v-else-if="errorMessage" class="state error">{{ errorMessage }}</div>
    <div v-else-if="!items.length" class="state empty">Cart is empty</div>

    <div v-else class="table-container">
      <table class="cart-table">
        <thead>
          <tr>
            <th>Item</th>
            <th>Price</th>
            <th>Qty</th>
            <th>Subtotal</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="it in items" :key="it.cartItemId">
            <td class="item-cell" @click="openListing(it.listingId)">
              <img :src="resolveImage(it.imageUrl)" alt="thumb" />
              <div class="title">{{ it.title }}</div>
            </td>
            <td>{{ money(it.price) }}</td>
            <td>{{ it.quantity }}</td>
            <td>{{ money(it.lineTotal) }}</td>
            <td><button class="remove" @click.stop="remove(it.cartItemId)" :disabled="loading">✕</button></td>
          </tr>
        </tbody>
      </table>

      <div class="summary">
        <div class="left">
          <div class="sum-line"><span class="label">Items:</span><span>{{ totalItems }}</span></div>
          <div class="sum-line total"><span class="label">Grand Total:</span><span>{{ money(grandTotal) }}</span></div>
        </div>
        <div class="right">
          <button class="checkout" @click="checkout" :disabled="checking || !items.length">{{ checking ? 'Processing...' : 'Checkout' }}</button>
        </div>
      </div>

      <div v-if="checkoutInfo" class="checkout-result">
        <p>Created {{ checkoutInfo.ordersCreated }} order(s). Total: {{ money(checkoutInfo.grandTotal) }}</p>
        <p>Order IDs: {{ checkoutInfo.orderIds.join(', ') }}</p>
        <button class="back-home" @click="goHome">Back to Home</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import cartApi from './services/cartApi.js'

const emit = defineEmits(['navigate'])

const items = ref([])
const loading = ref(false)
const checking = ref(false)
const errorMessage = ref('')
const checkoutInfo = ref(null)

const load = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await cartApi.getCart()
    items.value = data || []
  } catch (e) {
    errorMessage.value = 'Failed to load cart'
  } finally {
    loading.value = false
  }
}

onMounted(load)

const grandTotal = computed(() => items.value.reduce((s,i)=> s + parseFloat(i.lineTotal),0))
const totalItems = computed(() => items.value.reduce((s,i)=> s + i.quantity,0))

const money = v => {
  if (v == null) return '$—'
  const num = typeof v === 'number' ? v : parseFloat(v)
  if (isNaN(num)) return '$—'
  return num.toLocaleString(undefined,{ style:'currency', currency:'USD' })
}

const remove = async (id) => {
  try {
    loading.value = true
    await cartApi.removeItem(id)
    items.value = items.value.filter(i=> i.cartItemId !== id)
    window.dispatchEvent(new Event('cart-updated'))
  } catch (e) {
    errorMessage.value = 'Remove failed'
  } finally { loading.value = false }
}

const clearCart = async () => {
  if (!items.value.length) return
  if (!confirm('Clear entire cart?')) return
  try {
    loading.value = true
    await cartApi.clearCart()
    items.value = []
    window.dispatchEvent(new Event('cart-updated'))
  } catch (e) {
    errorMessage.value = 'Clear failed'
  } finally { loading.value = false }
}

const checkout = async () => {
  if (!items.value.length) return
  try {
    checking.value = true
    const { data } = await cartApi.checkout()
    checkoutInfo.value = data
    items.value = []
    window.dispatchEvent(new Event('cart-updated'))
  } catch (e) {
    errorMessage.value = e.response?.data?.message || 'Checkout failed'
  } finally { checking.value = false }
}

const goHome = () => emit('navigate','home')
const openListing = (id) => emit('navigate','listing-detail',{ listingId: id })

const resolveImage = (p) => {
  if (!p) return './assets/logo.svg'
  if (p.startsWith('http')) return p
  if (p.startsWith('/uploads/')) return cartApi.BASE_URL + p
  return p
}
</script>

<style scoped>
.cart-wrapper { max-width:1000px; margin:0 auto; padding:2rem 1rem 3rem; }
.cart-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem; }
.cart-header h1 { font-size:1.8rem; margin:0; }
.actions-top { display:flex; gap:.75rem; }
.back, .clear, .checkout, .remove, .back-home { cursor:pointer; border:none; border-radius:8px; font-weight:600; }
.back { background:#e2e8f0; padding:.55rem .9rem; }
.clear { background:#f87171; color:#fff; padding:.55rem .9rem; }
.clear:disabled { opacity:.5; cursor:not-allowed; }
.state { text-align:center; padding:2rem 0; font-weight:600; }
.state.error { color:#b91c1c; }
.state.empty { color:#64748b; }
.table-container { overflow-x:auto; }
.cart-table { width:100%; border-collapse:collapse; margin-bottom:1.5rem; }
.cart-table th, .cart-table td { padding:.75rem .6rem; border-bottom:1px solid #e2e8f0; font-size:.9rem; text-align:left; }
.cart-table th { background:#f1f5f9; font-weight:700; }
.item-cell { display:flex; align-items:center; gap:.75rem; cursor:pointer; }
.item-cell img { width:54px; height:54px; object-fit:cover; border-radius:8px; background:#f1f5f9; }
.remove { background:#ef4444; color:#fff; padding:.4rem .65rem; font-size:.8rem; }
.remove:disabled { opacity:.4; cursor:not-allowed; }
.summary { display:flex; justify-content:space-between; flex-wrap:wrap; gap:1.5rem; align-items:flex-start; background:#f8fafc; padding:1rem 1.25rem; border:1px solid #e2e8f0; border-radius:12px; }
.sum-line { display:flex; justify-content:space-between; gap:1rem; font-size:.95rem; margin-bottom:.4rem; }
.sum-line.total { font-size:1.15rem; font-weight:700; }
.checkout { background:#4f46e5; color:#fff; padding:.75rem 1.4rem; }
.checkout:disabled { opacity:.5; cursor:not-allowed; }
.checkout-result { margin-top:1.5rem; background:#ecfdf5; border:1px solid #a7f3d0; padding:1rem 1.25rem; border-radius:12px; font-size:.9rem; }
.back-home { margin-top:.75rem; background:#1d4ed8; color:#fff; padding:.55rem .9rem; }
@media (max-width:640px){
  .cart-table th:nth-child(2), .cart-table td:nth-child(2),
  .cart-table th:nth-child(3), .cart-table td:nth-child(3) { display:none; }
  .summary { flex-direction:column; }
  .item-cell img { width:48px; height:48px; }
}
</style>
