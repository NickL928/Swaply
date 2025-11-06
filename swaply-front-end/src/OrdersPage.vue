<template>
  <div class="orders-root">
    <AppHeader active="orders" @navigate="(p)=>$emit('navigate',p)" />

    <main class="main">
      <div class="wrap">
        <div class="tabs">
          <button :class="{active: tab==='buyer'}" @click="tab='buyer'">My Orders</button>
          <button :class="{active: tab==='seller'}" @click="tab='seller'">Sales</button>
        </div>

        <section v-if="tab==='buyer'" class="panel">
          <h2>My Orders (Buyer)</h2>
          <div v-if="loadingBuyer" class="state">Loading...</div>
          <div v-else-if="buyerErr" class="state err">{{ buyerErr }}</div>
          <div v-else-if="!buyerOrders.length" class="state empty">No orders yet.</div>
          <div v-else class="list">
            <article class="card" v-for="o in buyerOrders" :key="o.orderId">
              <div class="row">
                <div class="title">{{ o.listingTitle }}</div>
                <div class="meta">Order #{{ o.orderId }}</div>
              </div>
              <div class="row">
                <div>Status: <strong>{{ o.status }}</strong></div>
                <div>Total: <strong>{{ money(o.totalAmount) }}</strong></div>
              </div>
              <div class="row small">Placed: {{ dt(o.createdAt) }}</div>
            </article>
          </div>
        </section>

        <section v-else class="panel">
          <h2>Sales (Seller)</h2>
          <div v-if="loadingSeller" class="state">Loading...</div>
          <div v-else-if="sellerErr" class="state err">{{ sellerErr }}</div>
          <div v-else-if="!sellerOrders.length" class="state empty">No sales yet.</div>
          <div v-else class="list">
            <article class="card" v-for="o in sellerOrders" :key="o.orderId">
              <div class="row">
                <div class="title">{{ o.listingTitle }}</div>
                <div class="meta">Order #{{ o.orderId }} • Buyer: {{ o.buyerName }}</div>
              </div>
              <div class="row">
                <div>Status:
                  <select v-model="statusMap[o.orderId]" @change="saveStatus(o.orderId)">
                    <option value="PENDING">Pending</option>
                    <option value="CONFIRMED">Confirmed</option>
                    <option value="SHIPPED">Shipped</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="CANCELLED">Cancelled</option>
                  </select>
                </div>
                <div>Total: <strong>{{ money(o.totalAmount) }}</strong></div>
              </div>
              <div class="row small">Updated: {{ dt(o.updatedAt) }}</div>
            </article>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import orderApi from './services/orderApi.js'
import { fmtFt } from './services/currency.js'

const emit = defineEmits(['navigate'])
const tab = ref('buyer')

const buyerOrders = ref([])
const loadingBuyer = ref(false)
const buyerErr = ref('')

const sellerOrders = ref([])
const loadingSeller = ref(false)
const sellerErr = ref('')

const statusMap = ref({})

function money(v){ return fmtFt(v) }
function dt(v){ try { return new Date(v).toLocaleString() } catch { return v } }

async function loadBuyer(){
  loadingBuyer.value = true; buyerErr.value=''
  try { buyerOrders.value = await orderApi.myBuyerOrders() } catch(e){ buyerErr.value='Failed to load' } finally { loadingBuyer.value=false }
}
async function loadSeller(){
  loadingSeller.value = true; sellerErr.value=''
  try { sellerOrders.value = await orderApi.mySellerOrders(); statusMap.value = Object.fromEntries(sellerOrders.value.map(o=>[o.orderId, o.status])) }
  catch(e){ sellerErr.value='Failed to load' } finally { loadingSeller.value=false }
}

async function saveStatus(orderId){
  try {
    const status = statusMap.value[orderId]
    await orderApi.update(orderId, { status, notes: '' })
    window.dispatchEvent(new Event('orders-updated'))
    await loadSeller()
  } catch(e){ alert('Update failed') }
}

onMounted(()=>{ loadBuyer(); loadSeller(); })
</script>

<style scoped>
.orders-root{ min-height:100vh; background:#f8fafc; }
.main{ padding:1.25rem 1.5rem; }
.wrap{ max-width:1100px; margin:0 auto; }
.tabs{ display:flex; gap:.5rem; margin-bottom:1rem; }
.tabs button{ background:#fff; border:1px solid #e2e8f0; padding:.5rem 1rem; border-radius:10px; font-weight:700; color:#334155; cursor:pointer; }
.tabs button.active{ background:#eef2ff; border-color:#818cf8; color:#312e81; }
.panel h2{ margin:.25rem 0 1rem; }
.state{ text-align:center; color:#475569; padding:1rem; font-weight:600; }
.state.err{ color:#b91c1c; }
.state.empty{ color:#64748b; }
.list{ display:grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap:.9rem; }
.card{ background:#fff; border:1px solid #e2e8f0; border-radius:12px; padding:.9rem 1rem; }
.row{ display:flex; justify-content:space-between; align-items:center; gap:.75rem; margin:.25rem 0; color:#0f172a; }
.row.small{ color:#64748b; font-size:.85rem; }
.title{ font-weight:800; color:#0f172a; }
.meta{ color:#475569; font-weight:600; font-size:.9rem; }
select{ padding:.35rem .5rem; border:1px solid #e2e8f0; border-radius:8px; }
</style>
