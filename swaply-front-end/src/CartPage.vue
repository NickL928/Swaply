<template>
  <div class="cart-wrapper">
    <header class="cart-header">
      <h1 class="cart-title">My Cart</h1>
      <div class="actions-top">
        <button class="btn back" @click="goHome">← Home</button>
        <button class="btn danger" v-if="items.length" @click="clearCart" :disabled="loading">Clear Cart</button>
      </div>
    </header>

    <div class="card">
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
              <td>
                <button class="btn ghost danger-ghost" @click.stop="remove(it.cartItemId)" :disabled="loading">✕</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="summary">
          <div class="left">
            <div class="sum-line">
              <span class="label">Items:</span><span>{{ totalItems }}</span>
            </div>
            <div class="sum-line total">
              <span class="label">Grand Total:</span><span>{{ money(grandTotal) }}</span>
            </div>
          </div>
          <div class="right">
            <button class="btn primary" @click="checkout" :disabled="checking || !items.length">{{ checking ? 'Processing...' : 'Checkout' }}</button>
          </div>
        </div>

        <div v-if="checkoutInfo" class="checkout-result">
          <p>Created {{ checkoutInfo.ordersCreated }} order(s). Total: {{ money(checkoutInfo.grandTotal) }}</p>
          <p>Order IDs: {{ checkoutInfo.orderIds.join(', ') }}</p>
          <button class="btn secondary" @click="goHome">Back to Home</button>
        </div>
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
  const placeholder = './assets/logo.svg'
  if (!p) return placeholder
  if (p.startsWith('http')) return p
  if (p.startsWith('/uploads/')) return p
  if (p.startsWith('uploads/')) return '/' + p
  return p
}
</script>

<style scoped>
/* Page background and centering similar to Home */
.cart-wrapper {
  min-height: 100vh;
  width: 100%;
  background: #f8fafc;
  margin: 0;
  padding: 2rem 1rem 3rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}

/* Page header aligned with theme */
.cart-header {
  width: 100%;
  max-width: 1100px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 auto 1rem;
  padding: 0 .25rem;
}

.cart-title {
  margin: 0;
  font-size: 1.9rem;
  font-weight: 800;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.actions-top { display: flex; gap: .75rem; }

/* Card container matches Home style */
.card {
  width: 100%;
  max-width: 1100px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  padding: 1rem 1rem 1.25rem;
}

/* States */
.state { text-align: center; padding: 2rem 0; font-weight: 600; color: #0f172a; }
.state.error { color: #b91c1c; }
.state.empty { color: #64748b; }

/* Table visuals */
.table-container { width: 100%; overflow-x: auto; }
.cart-table { width: 100%; border-collapse: collapse; }
.cart-table thead th {
  position: sticky;
  top: 0;
  background: #f1f5f9;
  color: #0f172a;
  font-weight: 700;
  padding: .9rem .8rem;
  border-bottom: 1px solid #e2e8f0;
}
.cart-table td { padding: .85rem .8rem; border-bottom: 1px solid #eef2f7; font-size: .95rem; color: #334155; }
.item-cell { display: flex; align-items: center; gap: .85rem; cursor: pointer; }
.item-cell img { width: 56px; height: 56px; object-fit: cover; border-radius: 10px; background: #f1f5f9; border: 1px solid #e2e8f0; }
.title { font-weight: 700; color: #0f172a; }

/* Summary block */
.summary {
  display: flex; justify-content: space-between; flex-wrap: wrap; gap: 1.25rem; align-items: flex-start;
  background: #f8fafc; padding: 1rem 1.25rem; border: 1px solid #e2e8f0; border-radius: 12px; margin-top: 1rem;
}
.sum-line { display: flex; justify-content: space-between; gap: 1rem; font-size: .98rem; margin-bottom: .35rem; color: #0f172a; }
.sum-line .label { color: #475569; }
.sum-line.total { font-size: 1.15rem; font-weight: 800; }

/* Buttons aligned with theme */
.btn { cursor: pointer; border: none; border-radius: 12px; font-weight: 700; padding: .65rem 1rem; transition: transform .06s ease, box-shadow .2s ease, opacity .2s ease; }
.btn:disabled { opacity: .55; cursor: not-allowed; }
.btn:hover:not(:disabled) { transform: translateY(-1px); }

.primary { background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); color: #fff; box-shadow: 0 8px 18px rgba(79, 70, 229, .35); }
.secondary { background: #1d4ed8; color: #fff; }
.back { background: #e2e8f0; color: #0f172a; }
.danger { background: #ef4444; color: #fff; }
.ghost { background: #fff; border: 1px solid #e2e8f0; color: #0f172a; }
.danger-ghost { color: #ef4444; border-color: #fecaca; }

/* Checkout result */
.checkout-result { margin-top: 1rem; background: #ecfdf5; border: 1px solid #a7f3d0; padding: 1rem 1.25rem; border-radius: 12px; font-size: .95rem; color: #065f46; }

/* Responsive */
@media (max-width: 640px){
  .cart-title { font-size: 1.6rem; }
  .cart-table th:nth-child(2), .cart-table td:nth-child(2),
  .cart-table th:nth-child(3), .cart-table td:nth-child(3) { display:none; }
  .item-cell img { width: 52px; height: 52px; }
  .btn { padding: .6rem .85rem; }
}
</style>
