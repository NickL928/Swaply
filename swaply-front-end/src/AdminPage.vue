<template>
  <div class="admin-container">
    <header class="admin-header">
      <div class="logo" @click="$emit('navigate','home')">
        <img src="./assets/logo.png" alt="logo" width="40" height="40" />
        <h1>Admin Panel</h1>
      </div>
      <div class="spacer" />
      <button class="logout" @click="$emit('logout')">Logout</button>
    </header>

    <main class="admin-main">
      <aside class="sidebar">
        <button :class="{active: tab==='dashboard'}" @click="tab='dashboard'">Dashboard</button>
        <button :class="{active: tab==='users'}" @click="loadUsers()">Users</button>
        <button :class="{active: tab==='categories'}" @click="loadCategories()">Categories</button>
        <button :class="{active: tab==='announcements'}" @click="loadAnnouncements()">Announcements</button>
        <button :class="{active: tab==='orders'}" @click="loadOrders()">Orders</button>
        <button :class="{active: tab==='bids'}" @click="loadBids()">Bids</button>
        <button :class="{active: tab==='listings'}" @click="loadListings()">Listings</button>
      </aside>

      <section class="content">
        <div v-if="tab==='dashboard'" class="card">
          <h2>Statistics</h2>
          <div class="stats">
            <div class="stat">Users: {{ stats.users }}</div>
            <div class="stat">Listings: {{ stats.listings }}</div>
            <div class="stat">Orders: {{ stats.orders }}</div>
            <div class="stat">Bids: {{ stats.bids }}</div>
            <div class="stat">Total Order Amount: {{ stats.totalOrderAmount }}</div>
          </div>
        </div>

        <div v-else-if="tab==='users'" class="card">
          <h2>Users</h2>
          <div class="table">
            <div class="row head">
              <div>ID</div><div>Avatar</div><div>Username</div><div>Email</div><div>Role</div><div>Active</div><div>Ops</div>
            </div>
            <div v-for="u in users" :key="u.userId" class="row">
              <div>{{ u.userId }}</div>
              <div>
                <img v-if="u.profileImageUrl" :src="u.profileImageUrl" alt="avatar" class="avatar" />
                <div v-else class="avatar placeholder">{{ (u.userName||'?').charAt(0).toUpperCase() }}</div>
              </div>
              <div>{{ u.userName }}</div>
              <div>{{ u.email }}</div>
              <div>{{ u.role }}</div>
              <div>{{ u.isActive ? 'Yes' : 'No' }}</div>
              <div class="ops">
                <input type="file" accept="image/*" :ref="el => fileInputs[u.userId] = el" style="display:none" @change="onFileSelected(u, $event)" />
                <button @click="chooseFile(u)">Change Photo</button>
                <button @click="setRole(u,'ADMIN')" v-if="u.role!=='ADMIN'">Make Admin</button>
                <button @click="setRole(u,'USER')" v-if="u.role!=='USER'">Make User</button>
                <button @click="toggleActive(u)">{{ u.isActive ? 'Deactivate' : 'Activate' }}</button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='categories'" class="card">
          <h2>Categories</h2>
          <p>Categories are fixed in code (enum). Use below to view list.</p>
          <ul>
            <li v-for="c in categories" :key="c">{{ c }}</li>
          </ul>
        </div>

        <div v-else-if="tab==='announcements'" class="card">
          <h2>Announcements</h2>
          <form class="announce-form" @submit.prevent="createAnnouncement">
            <input v-model="newAnn.title" placeholder="Title" required />
            <textarea v-model="newAnn.content" placeholder="Content" required></textarea>
            <label><input type="checkbox" v-model="newAnn.active"/> Active</label>
            <button type="submit">Publish</button>
          </form>
          <div class="announce-list">
            <div class="announce" v-for="a in announcements" :key="a.id">
              <h4>{{ a.title }}</h4>
              <small>{{ a.createdAt }}</small>
              <p>{{ a.content }}</p>
              <div class="ops">
                <button @click="removeAnnouncement(a)">Delete</button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='orders'" class="card">
          <h2>Orders</h2>
          <div class="row head"><div>ID</div><div>Buyer</div><div>Listing</div><div>Status</div><div>Total</div></div>
          <div v-for="o in orders" :key="o.orderId" class="row">
            <div>{{ o.orderId }}</div>
            <div>{{ o.buyer?.userId }}</div>
            <div>{{ o.listing?.listingId }}</div>
            <div>{{ o.status }}</div>
            <div>{{ o.totalAmount }}</div>
          </div>
        </div>

        <div v-else-if="tab==='bids'" class="card">
          <h2>Bids</h2>
          <div class="row head"><div>ID</div><div>Auction</div><div>Bidder</div><div>Amount</div><div>Time</div></div>
          <div v-for="b in bids" :key="b.bidId" class="row">
            <div>{{ b.bidId }}</div>
            <div>{{ b.auction?.auctionId }}</div>
            <div>{{ b.bidder?.userId }}</div>
            <div>{{ b.amount }}</div>
            <div>{{ b.createdDate }}</div>
          </div>
        </div>

        <div v-else-if="tab==='listings'" class="card">
          <h2>Listings</h2>
          <div class="row head"><div>ID</div><div>Title</div><div>Seller</div><div>Status</div><div>Price</div></div>
          <div v-for="l in listings" :key="l.listingId" class="row">
            <div>{{ l.listingId }}</div>
            <div>{{ l.title }}</div>
            <div>{{ l.user?.userName }}</div>
            <div>{{ l.status }}</div>
            <div>{{ l.price }}</div>
          </div>
        </div>

      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const emit = defineEmits(['navigate','logout'])
const tab = ref('dashboard')

const stats = ref({ users:0, listings:0, orders:0, bids:0, totalOrderAmount:0 })
const users = ref([])
const categories = ref([])
const announcements = ref([])
const newAnn = ref({ title:'', content:'', active:true })
const orders = ref([])
const bids = ref([])
const listings = ref([])
const fileInputs = ref({})

function authHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function loadStats() {
  const { data } = await axios.get('/api/admin/stats', { headers: authHeaders() })
  stats.value = data
}
async function loadUsers() {
  tab.value = 'users'
  const { data } = await axios.get('/api/admin/users', { headers: authHeaders() })
  users.value = data
}
async function loadCategories() {
  tab.value = 'categories'
  const { data } = await axios.get('/api/admin/categories', { headers: authHeaders() })
  categories.value = data
}
async function loadAnnouncements() {
  tab.value = 'announcements'
  const { data } = await axios.get('/api/announcements')
  announcements.value = data
}
async function loadOrders() {
  tab.value = 'orders'
  const { data } = await axios.get('/api/admin/orders', { headers: authHeaders() })
  orders.value = data
}
async function loadBids() {
  tab.value = 'bids'
  const { data } = await axios.get('/api/admin/bids', { headers: authHeaders() })
  bids.value = data
}
async function loadListings() {
  tab.value = 'listings'
  const { data } = await axios.get('/api/admin/listings', { headers: authHeaders() })
  listings.value = data
}

async function setRole(u, role) {
  await axios.patch(`/api/admin/users/${u.userId}/role?role=${role}`, null, { headers: authHeaders() })
  await loadUsers()
}
async function toggleActive(u) {
  await axios.patch(`/api/admin/users/${u.userId}/activate?active=${!u.isActive}`, null, { headers: authHeaders() })
  await loadUsers()
}
async function createAnnouncement() {
  if (!newAnn.value.title || !newAnn.value.content) return
  await axios.post('/api/admin/announcements', newAnn.value, { headers: authHeaders() })
  newAnn.value = { title:'', content:'', active:true }
  await loadAnnouncements()
}
async function removeAnnouncement(a) {
  await axios.delete(`/api/admin/announcements/${a.id}`, { headers: authHeaders() })
  await loadAnnouncements()
}

function chooseFile(u) {
  const input = fileInputs.value[u.userId]
  if (input) input.click()
}

async function onFileSelected(u, evt) {
  const file = evt.target.files && evt.target.files[0]
  evt.target.value = '' // reset so same file can be selected again
  if (!file) return
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    alert('File too large. Max 5MB.')
    return
  }
  try {
    const fd = new FormData()
    fd.append('file', file)
    const { data } = await axios.post('/api/files/upload', fd, {
      headers: { ...authHeaders(), 'Content-Type': 'multipart/form-data' }
    })
    // Update user's profileImageUrl while preserving other fields
    await axios.put(`/api/user/${u.userId}`, {
      userName: u.userName,
      email: u.email,
      profileImageUrl: data.url
    }, { headers: authHeaders() })
    await loadUsers()
  } catch (e) {
    console.error(e)
    alert('Upload failed')
  }
}

onMounted(() => { loadStats() })
</script>

<style scoped>
.admin-container { min-height:100vh; background:#0b1220; display:flex; flex-direction:column; }
.admin-header { display:flex; align-items:center; padding:1rem 1.5rem; background:linear-gradient(135deg,#111827,#1f2937); color:#fff; }
.logo { display:flex; align-items:center; gap:.75rem; cursor:pointer; }
.logo img { width:40px; height:40px; filter:brightness(0) invert(1); }
.logo h1 { font-size:1.25rem; margin:0; }
.spacer { flex:1; }
.logout { background:#ef4444; border:none; color:#fff; padding:.6rem .9rem; border-radius:8px; cursor:pointer; font-weight:700; }
.admin-main { display:flex; flex:1; }
.sidebar { width:220px; background:#111827; color:#fff; display:flex; flex-direction:column; padding:1rem; gap:.5rem; }
.sidebar button { text-align:left; padding:.6rem .7rem; border:none; border-radius:8px; background:transparent; color:#cbd5e1; cursor:pointer; font-weight:700; }
.sidebar button.active, .sidebar button:hover { background:#1f2937; color:#fff; }
.content { flex:1; padding:1rem 1.5rem; display:flex; flex-direction:column; gap:1rem; background:#0f172a; color:#e5e7eb; }
.card { background:#111827; border-radius:14px; padding:1rem; border:1px solid #374151; color:#e5e7eb; }
.stats { display:grid; grid-template-columns:repeat(3,1fr); gap:1rem; }
.stats .stat { background:#0b1220; border:1px solid #374151; border-radius:10px; padding:.75rem; color:#e2e8f0; }
.table { display:flex; flex-direction:column; gap:.25rem; }
.row { display:grid; grid-template-columns: 60px 60px 1fr 1.5fr 100px 80px 260px; gap:.5rem; padding:.4rem .5rem; border-bottom:1px solid #374151; align-items:center; }
.row.head { font-weight:800; background:#1f2937; color:#f9fafb; }
.ops button { margin-right:.5rem; background:#374151; color:#f8fafc; border:none; padding:.4rem .6rem; border-radius:6px; }
.announce-form { display:flex; flex-direction:column; gap:.5rem; margin-bottom:1rem; }
.announce-form input, .announce-form textarea { padding:.5rem .6rem; border:1px solid #374151; border-radius:8px; background:#0b1220; color:#e5e7eb; }
.announce-form input::placeholder, .announce-form textarea::placeholder { color:#94a3b8; }
.announce { border:1px solid #374151; border-radius:10px; padding:.6rem .8rem; margin-bottom:.5rem; background:#0b1220; }
.avatar { width:40px; height:40px; border-radius:50%; object-fit:cover; border:1px solid #334155; }
.avatar.placeholder { display:flex; align-items:center; justify-content:center; background:#1f2937; color:#e2e8f0; font-weight:800; }

/* Headings contrast */
.card h2 { color:#f3f4f6; }
.card h4 { color:#e5e7eb; }
.card small { color:#94a3b8; }
</style>
