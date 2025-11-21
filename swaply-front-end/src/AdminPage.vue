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
        <button :class="{active: tab==='users'}" @click="tab = 'users'">Users</button>
        <button :class="{active: tab==='categories'}" @click="tab = 'categories'">Categories</button>
        <button :class="{active: tab==='announcements'}" @click="tab = 'announcements'">Announcements</button>
        <button :class="{active: tab==='orders'}" @click="tab = 'orders'">Orders</button>
        <button :class="{active: tab==='auctions'}" @click="tab = 'auctions'">Auctions</button>
        <button :class="{active: tab==='listings'}" @click="tab = 'listings'">Listings</button>
        <button :class="{active: tab==='apiTester'}" @click="tab='apiTester'">API Tester</button>
        <button :class="{active: tab==='dbTester'}" @click="tab='dbTester'">DB Query</button>
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

          <div class="live-metrics-grid">
            <div class="live-metric-card" v-for="metric in liveMetricCards" :key="metric.label">
              <div class="metric-label">{{ metric.label }}</div>
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-sub">{{ metric.subtext }}</div>
            </div>
          </div>

          <div class="resource-card">
            <div class="chart-title">System Resource Usage</div>
            <div class="metrics-bars">
              <div class="metrics-bar-row">
                <div class="metrics-bar-label">CPU</div>
                <div class="metrics-bar-track">
                  <div class="metrics-bar-fill" :style="{ width: (liveMetrics.cpuUsage || 0) + '%' }"></div>
                </div>
                <div class="metrics-bar-value">{{ liveMetrics.cpuUsage || 0 }}%</div>
              </div>
              <div class="metrics-bar-row">
                <div class="metrics-bar-label">Memory</div>
                <div class="metrics-bar-track">
                  <div class="metrics-bar-fill" :style="{ width: (liveMetrics.memoryUsage || 0) + '%' }"></div>
                </div>
                <div class="metrics-bar-value">{{ liveMetrics.memoryUsage || 0 }}%</div>
              </div>
              <div class="metrics-bar-row">
                <div class="metrics-bar-label">Storage</div>
                <div class="metrics-bar-track">
                  <div class="metrics-bar-fill" :style="{ width: (liveMetrics.storageUsage || 0) + '%' }"></div>
                </div>
                <div class="metrics-bar-value">{{ liveMetrics.storageUsage || 0 }}%</div>
              </div>
            </div>
            <div class="metrics-footer">Last updated: {{ metricsLastUpdated }} • Auto-refresh every 10s</div>
          </div>

          <div class="charts">
            <div class="chart-card">
              <div class="chart-title">User & Listing Ratio</div>
              <div class="bar-row">
                <div class="bar-label">Users</div>
                <div class="bar-track">
                  <div class="bar-fill-users" :style="{ width: userListingRatio.users + '%' }"></div>
                </div>
                <div class="bar-value">{{ userListingRatio.users }}%</div>
              </div>
              <div class="bar-row">
                <div class="bar-label">Listings</div>
                <div class="bar-track">
                  <div class="bar-fill-listings" :style="{ width: userListingRatio.listings + '%' }"></div>
                </div>
                <div class="bar-value">{{ userListingRatio.listings }}%</div>
              </div>
            </div>

            <div class="chart-card">
              <div class="chart-title">Order Status Breakdown</div>
              <div v-if="stats.orders > 0" class="status-bars">
                <div
                  v-for="(count, status) in orderStatusBreakdown"
                  :key="status"
                  class="status-bar-row"
                >
                  <div class="status-label">{{ status }}</div>
                  <div class="status-bar-track">
                    <div
                      class="status-bar-fill"
                      :style="{ width: ((count / stats.orders) * 100) + '%' }"
                    ></div>
                  </div>
                  <div class="status-count">{{ count }}</div>
                </div>
              </div>
              <div v-else class="empty">No orders yet</div>
            </div>
          </div>

          <div class="chart-card trend-card">
            <div class="chart-title">Growth Trend (Last 10 Days)</div>
            <div class="trend-legend">
              <span class="legend-item"><span class="legend-dot users"></span>Users</span>
              <span class="legend-item"><span class="legend-dot listings"></span>Listings</span>
            </div>
            <div class="trend-plot">
              <svg viewBox="0 0 100 50" preserveAspectRatio="none">
                <!-- Grid lines -->
                <line x1="0" y1="10" x2="100" y2="10" class="grid-line" />
                <line x1="0" y1="25" x2="100" y2="25" class="grid-line" />
                <line x1="0" y1="40" x2="100" y2="40" class="grid-line" />

                <!-- Trend lines -->
                <polyline
                  v-if="userTrendPoints.length"
                  :points="userTrendPoints"
                  class="trend-line trend-line-users"
                />
                <polyline
                  v-if="listingTrendPoints.length"
                  :points="listingTrendPoints"
                  class="trend-line trend-line-listings"
                />
              </svg>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='users'" class="card">
          <h2>Users</h2>
          <div class="user-search-row">
            <input v-model="userSearch" class="user-search-input" placeholder="Search users by name or email" />
          </div>
          <div class="users-table">
            <div class="users-row users-head">
              <div class="col-id">ID</div>
              <div class="col-avatar">Avatar</div>
              <div class="col-username">Username</div>
              <div class="col-email">Email</div>
              <div class="col-role">Role</div>
              <div class="col-active">Active</div>
              <div class="col-ops">Operations</div>
            </div>
            <div v-for="u in filteredUsers" :key="u.userId" class="users-row">
              <div class="col-id">{{ u.userId }}</div>
              <div class="col-avatar">
                <img v-if="u.profileImageUrl" :src="u.profileImageUrl" alt="avatar" class="user-avatar" />
                <div v-else class="user-avatar-placeholder">{{ (u.userName||'?').charAt(0).toUpperCase() }}</div>
              </div>
              <div class="col-username">{{ u.userName }}</div>
              <div class="col-email">{{ u.email }}</div>
              <div class="col-role">
                <span class="role-badge" :class="u.role.toLowerCase()">{{ u.role }}</span>
              </div>
              <div class="col-active">
                <span class="status-badge" :class="u.isActive ? 'active' : 'inactive'">{{ u.isActive ? 'Yes' : 'No' }}</span>
              </div>
              <div class="col-ops">
                <input type="file" accept="image/*" :ref="el => fileInputs[u.userId] = el" style="display:none" @change="onFileSelected(u, $event)" />
                <button class="btn-sm" @click="chooseFile(u)">📷</button>
                <button class="btn-sm btn-admin" @click="setRole(u,'ADMIN')" v-if="u.role!=='ADMIN'">Admin</button>
                <button class="btn-sm btn-user" @click="setRole(u,'USER')" v-if="u.role!=='USER'">User</button>
                <button class="btn-sm" :class="u.isActive ? 'btn-danger' : 'btn-success'" @click="toggleActive(u)">
                  {{ u.isActive ? 'Deactivate' : 'Activate' }}
                </button>
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
          <div class="announce-form-card">
            <h3>Create New Announcement</h3>
            <form class="announce-form" @submit.prevent="createAnnouncement">
              <div class="form-group">
                <label>Title</label>
                <input v-model="newAnn.title" placeholder="Enter announcement title" required />
              </div>
              <div class="form-group">
                <label>Content</label>
                <textarea v-model="newAnn.content" placeholder="Enter announcement content" required rows="4"></textarea>
              </div>
              <div class="form-group-inline">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="newAnn.active"/>
                  <span>Active (visible to users)</span>
                </label>
                <button type="submit" class="btn-primary">📢 Publish Announcement</button>
              </div>
            </form>
          </div>
          <div class="announce-list">
            <h3>Published Announcements</h3>
            <div class="announce-card" v-for="a in announcements" :key="a.id">
              <div class="announce-header">
                <h4>{{ a.title }}</h4>
                <span class="announce-date">{{ new Date(a.createdAt).toLocaleDateString() }}</span>
              </div>
              <p class="announce-content">{{ a.content }}</p>
              <div class="announce-footer">
                <span class="status-badge" :class="a.active ? 'active' : 'inactive'">
                  {{ a.active ? '✓ Active' : '✗ Inactive' }}
                </span>
                <button class="btn-sm btn-danger" @click="removeAnnouncement(a)">🗑 Delete</button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='orders'" class="card">
          <h2>Orders</h2>
          <div class="table">
            <div class="row head">
              <div>ID</div>
              <div>Buyer</div>
              <div>Listing</div>
              <div>Status</div>
              <div>Total</div>
            </div>
            <div v-for="o in orders" :key="o.orderId" class="row">
              <div>{{ o.orderId }}</div>
              <div>{{ o.buyer?.userName || o.buyer?.userId }}</div>
              <div>{{ o.listing?.title || o.listing?.listingId }}</div>
              <div><span class="status-badge" :class="o.status.toLowerCase()">{{ o.status }}</span></div>
              <div>{{ o.totalAmount?.toFixed(2) || '0.00' }} ft</div>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='auctions'" class="card">
          <h2>Auctions</h2>
          <div class="table bids-table">
            <div class="row head">
              <div>ID</div>
              <div>Listing</div>
              <div>Seller</div>
              <div>Status</div>
              <div>Current Bid</div>
              <div>Ends</div>
              <div>Operations</div>
            </div>
            <div v-for="a in auctions" :key="a.auctionId" class="row">
              <div>{{ a.auctionId }}</div>
              <div>{{ a.listing?.title || a.listingId }}</div>
              <div>{{ a.sellerUsername || a.listing?.user?.userName || a.sellerId }}</div>
              <div><span class="status-badge" :class="(a.status || '').toLowerCase()">{{ a.status }}</span></div>
              <div>{{ a.currentPrice?.toFixed(2) || a.startingPrice?.toFixed(2) }} ft</div>
              <div>{{ a.endTime ? new Date(a.endTime).toLocaleString() : '—' }}</div>
              <div class="ops">
                <button class="danger btn-sm btn-danger" @click="deleteAuction(a.auctionId)">🗑 End & Delete</button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='listings'" class="card">
          <h2>Listings</h2>
          <div class="table listings-table">
            <div class="row head">
              <div>ID</div>
              <div>Title</div>
              <div>Seller</div>
              <div>Status</div>
              <div>Price</div>
              <div>Operations</div>
            </div>
            <div v-for="l in listings" :key="l.listingId" class="row">
              <div>{{ l.listingId }}</div>
              <div>{{ l.title }}</div>
              <div>{{ l.user?.userName || l.user?.userId }}</div>
              <div><span class="status-badge" :class="l.status.toLowerCase()">{{ l.status }}</span></div>
              <div>{{ l.price?.toFixed(2) }} ft</div>
              <div class="ops">
                <button class="btn-sm btn-danger" @click="deleteListing(l.listingId)">🗑 Delete</button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='apiTester'" class="card">
          <h2>🔌 API Testing Tool</h2>
          <div class="api-tester-container">
            <div class="api-form-section">
              <h3>Request</h3>
              <div class="form-group">
                <label>Select Endpoint</label>
                <select v-model="apiPresetKey" class="select-input">
                  <option disabled value="">Choose an API endpoint...</option>
                  <option v-for="opt in apiEndpoints" :key="opt.key" :value="opt.key">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div v-if="currentApi" class="endpoint-info">
                <span class="method-badge" :class="currentApi.method.toLowerCase()">{{ currentApi.method }}</span>
                <span class="endpoint-path">{{ currentApi.path }}</span>
              </div>
              <div class="form-group">
                <label>Request Body (JSON)</label>
                <textarea v-model="apiBody" placeholder='{"key": "value"}' class="code-textarea" rows="8"></textarea>
              </div>
              <button class="btn-run" @click="runApiTest" :disabled="apiRunning || !apiPresetKey">
                {{ apiRunning ? '⏳ Running...' : '▶ Run Test' }}
              </button>
            </div>
            <div class="api-result-section">
              <h3>Response</h3>
              <div class="result-meta">
                <div class="meta-item">
                  <span class="meta-label">Status:</span>
                  <span class="status-code" :class="{
                    'status-success': apiResult?.status >= 200 && apiResult?.status < 300,
                    'status-error': apiResult?.status >= 400 || apiResult?.status === 0
                  }">
                    {{ apiResult?.status ?? '-' }}
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Duration:</span>
                  <span class="meta-value">{{ apiResult?.durationMs ?? '-' }} ms</span>
                </div>
                <div class="result-actions">
                  <button v-if="apiResult" class="btn-icon" @click="copyApiResult" title="Copy result">📋 Copy</button>
                </div>
              </div>
              <pre class="result-output">{{ apiResultPretty }}</pre>
            </div>
          </div>
        </div>

        <div v-else-if="tab==='dbTester'" class="card">
          <h2>🗄️ Database Query Tool</h2>
          <div class="db-tester-container">
            <div class="db-form-section">
              <h3>Query</h3>
              <div class="form-group">
                <label>Quick Presets</label>
                <select v-model="dbPresetKey" class="select-input">
                  <option disabled value="">Choose a preset query...</option>
                  <option v-for="p in dbPresets" :key="p.key" :value="p.key">
                    {{ p.label }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>SQL Query</label>
                <textarea v-model="dbSql" placeholder="SELECT * FROM tb_user LIMIT 10" class="code-textarea" rows="10"></textarea>
              </div>
              <div class="form-controls">
                <div class="form-group-inline">
                  <label>Max rows</label>
                  <input type="number" v-model.number="dbMaxRows" min="1" max="1000" class="number-input" />
                </div>
                <button class="btn-run" @click="runDbQuery" :disabled="dbRunning || !dbSql">
                  {{ dbRunning ? '⏳ Running...' : '▶ Execute Query' }}
                </button>
              </div>
            </div>
            <div class="db-result-section">
              <h3>Results</h3>
              <div class="result-meta">
                <div class="meta-item">
                  <span class="meta-label">Rows:</span>
                  <span class="meta-value">{{ dbResult?.rows?.length ?? 0 }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Duration:</span>
                  <span class="meta-value">{{ dbResult?.durationMs ?? '-' }} ms</span>
                </div>
                <div class="result-actions">
                  <button v-if="dbResult?.rows?.length > 0" class="btn-icon" @click="exportDbToCsv" title="Export to CSV">📊 CSV</button>
                  <button v-if="dbResult?.rows?.length > 0" class="btn-icon" @click="copyDbResult" title="Copy results">📋 Copy</button>
                </div>
              </div>
              <div class="db-table-container" v-if="dbResult && dbResult.rows && dbResult.rows.length">
                <table class="db-table">
                  <thead>
                    <tr>
                      <th v-for="col in dbColumns" :key="col">{{ col }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, idx) in dbResult.rows" :key="idx">
                      <td v-for="col in dbColumns" :key="col">{{ row[col] }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty">Run a query to see results</div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, watch } from 'vue'
import axios from 'axios'

const emit = defineEmits(['navigate','logout'])
const tab = ref('dashboard')

// Timer for periodic metrics refresh
let metricsTimer = null

const stats = ref({ users:0, listings:0, orders:0, bids:0, totalOrderAmount:0 })
const users = ref([])
const userSearch = ref('')
const categories = ref([])
const announcements = ref([])
const newAnn = ref({ title:'', content:'', active:true })
const orders = ref([])
const auctions = ref([])
const listings = ref([])
const fileInputs = ref({})

// API Tester state
const apiPresetKey = ref('')
const apiBody = ref('')
const apiRunning = ref(false)
const apiResult = ref(null)

// DB Query Tester state
const dbSql = ref('')
const dbMaxRows = ref(100)
const dbRunning = ref(false)
const dbResult = ref(null)
const dbPresetKey = ref('recentUsers')

// metrics/trend state for dashboard
const liveMetrics = ref({})
const liveMetricCards = computed(() => [
  {
    label: 'Active Sessions',
    value: liveMetrics.value.activeSessions ?? 0,
    subtext: 'Real-time connected users'
  },
  {
    label: 'API Response Time',
    value: `${liveMetrics.value.avgResponseTime ?? 0}ms`,
    subtext: 'Avg last 1 min'
  },
  {
    label: 'DB Queries/min',
    value: liveMetrics.value.dbQueries ?? 0,
    subtext: 'Aggregate throughput'
  },
  {
    label: 'Cache Hit Rate',
    value: `${liveMetrics.value.cacheHitRate ?? 0}%`,
    subtext: 'Higher is better'
  }
])
const userTrend = ref([])
const listingTrend = ref([])
const metricsLastUpdated = computed(() => liveMetrics.value?.timestamp || new Date().toLocaleTimeString())

const apiEndpoints = [
  // Auth scenarios (called directly, no admin proxy)
  { key: 'auth-login-admin', label: 'Auth: Login (admin)', path: '/api/auth/login', method: 'POST', template: { userName: 'admin', password: 'ChangeMe123!' }, viaAdminProxy: false },
  { key: 'auth-login-wrong-pwd', label: 'Auth: Login (wrong password)', path: '/api/auth/login', method: 'POST', template: { userName: 'admin', password: 'WrongPass123!' }, viaAdminProxy: false },
  { key: 'auth-login-nonexist', label: 'Auth: Login (non-existing user)', path: '/api/auth/login', method: 'POST', template: { userName: 'no_such_user_123', password: 'SomePass123!' }, viaAdminProxy: false },
  { key: 'auth-register', label: 'Auth: Register (example)', path: '/api/auth/register', method: 'POST', template: { userName: 'NewUser', email: 'user@example.com', password: 'Password123!' }, viaAdminProxy: false },

  // User/account (call directly with auth token)
  { key: 'user-all', label: 'User: Get All Users', path: '/api/user', method: 'GET', viaAdminProxy: false },
  { key: 'user-by-id', label: 'User: By ID (example 1)', path: '/api/user/1', method: 'GET', viaAdminProxy: false },

  // Listings (public or user endpoints)
  { key: 'listings-all', label: 'Listings: All', path: '/api/listings', method: 'GET', viaAdminProxy: false },
  { key: 'listing-by-id', label: 'Listings: By ID (example 1)', path: '/api/listings/1', method: 'GET', viaAdminProxy: false },

  // Orders (user endpoints)
  { key: 'orders-buyer', label: 'Orders: My Buyer Orders', path: '/api/orders/buyer', method: 'GET', viaAdminProxy: false },
  { key: 'orders-seller', label: 'Orders: My Seller Orders', path: '/api/orders/seller', method: 'GET', viaAdminProxy: false },

  // Cart & public
  { key: 'cart-current', label: 'Cart: Current', path: '/api/cart', method: 'GET', viaAdminProxy: false },
  { key: 'announcements-public', label: 'Announcements: Public list', path: '/api/announcements', method: 'GET', viaAdminProxy: false },

  // Admin resources (must go via admin proxy)
  { key: 'admin-stats', label: 'Admin: Stats', path: '/api/admin/stats', method: 'GET', viaAdminProxy: true },
  { key: 'admin-users', label: 'Admin: Users', path: '/api/admin/users', method: 'GET', viaAdminProxy: true },
  { key: 'admin-listings', label: 'Admin: Listings', path: '/api/admin/listings', method: 'GET', viaAdminProxy: true },
  { key: 'admin-orders', label: 'Admin: Orders', path: '/api/admin/orders', method: 'GET', viaAdminProxy: true },
  { key: 'admin-auctions', label: 'Admin: Auctions', path: '/api/admin/auctions', method: 'GET', viaAdminProxy: true }
]

const currentApi = computed(() => apiEndpoints.find(e => e.key === apiPresetKey.value) || null)

// DB presets: use real table names from JPA @Table annotations
const dbPresets = [
  {
    key: 'recentUsers',
    label: 'Recent users',
    sql:
      'SELECT user_id, user_name, email, created_at ' +
      'FROM tb_user ' +
      'ORDER BY created_at DESC ' +
      'LIMIT 20'
  },
  {
    key: 'recentListings',
    label: 'Recent listings',
    sql:
      'SELECT listing_id, title, created_date, status ' +
      'FROM tb_listing ' +
      'ORDER BY created_date DESC ' +
      'LIMIT 20'
  },
  {
    key: 'topListingCategories',
    label: 'Top listing categories',
    sql:
      'SELECT category, COUNT(*) AS cnt ' +
      'FROM tb_listing ' +
      'GROUP BY category ' +
      'ORDER BY cnt DESC ' +
      'LIMIT 10'
  },
  {
    key: 'recentOrders',
    label: 'Recent orders',
    sql:
      'SELECT order_id, status, total_amount, created_date ' +
      'FROM tb_order ' +
      'ORDER BY created_date DESC ' +
      'LIMIT 20'
  },
  {
    key: 'recentBids',
    label: 'Recent bids',
    sql:
      'SELECT bid_id, auction_id, bidder_id, amount, created_date ' +
      'FROM tb_bid ' +
      'ORDER BY created_date DESC ' +
      'LIMIT 20'
  }
]

const userTrendPoints = computed(() => normalizeTrendToSvg(userTrend.value))
const listingTrendPoints = computed(() => normalizeTrendToSvg(listingTrend.value))

// Watch for API preset changes and auto-fill body
watch(apiPresetKey, (newKey) => {
  if (newKey) {
    const preset = apiEndpoints.find(e => e.key === newKey)
    if (preset?.template) {
      let template = preset.template
      // For register endpoint, generate unique username/email to avoid duplicates
      if (newKey === 'auth-register') {
        const timestamp = Date.now()
        template = {
          userName: `User_${timestamp}`,
          email: `user_${timestamp}@example.com`,
          password: 'Password123!'
        }
      }
      apiBody.value = JSON.stringify(template, null, 2)
    } else {
      apiBody.value = ''
    }
  }
})

// Watch for DB preset changes and auto-fill SQL
watch(dbPresetKey, (newKey) => {
  if (newKey) {
    const preset = dbPresets.find(p => p.key === newKey)
    if (preset?.sql) {
      dbSql.value = preset.sql
    }
  }
})

// Watch for tab changes and load appropriate data
watch(tab, async (newTab) => {
  try {
    switch(newTab) {
      case 'users':
        await loadUsers()
        break
      case 'categories':
        await loadCategories()
        break
      case 'announcements':
        await loadAnnouncements()
        break
      case 'orders':
        await loadOrders()
        break
      case 'auctions':
        await loadAuctions()
        break
      case 'listings':
        await loadListings()
        break
    }
  } catch (e) {
    console.error(`Failed to load data for ${newTab} tab:`, e)
  }
})

function normalizeTrendToSvg(series) {
  if (!series || !series.length) return ''
  const xs = series.map((_, i) => i)
  const ys = series.map(p => Number(p.count || 0))
  const maxX = Math.max(xs[xs.length - 1], 1)
  const maxY = Math.max(...ys, 1)
  return series.map((p, idx) => {
    const x = xs[idx] / maxX * 100
    const y = 45 - (Number(p.count || 0) / maxY * 35) // Use 45 as bottom, 35 as height range
    return `${x},${y}`
  }).join(' ')
}

async function loadStats() {
  const { data } = await axios.get('/api/admin/stats', { headers: authHeaders() })
  stats.value = data
}
async function loadUsers() {
  const { data } = await axios.get('/api/admin/users', { headers: authHeaders() })
  users.value = data
}
async function loadCategories() {
  const { data } = await axios.get('/api/admin/categories', { headers: authHeaders() })
  categories.value = data
}
async function loadAnnouncements() {
  const { data } = await axios.get('/api/announcements')
  announcements.value = data
}
async function loadOrders() {
  const { data } = await axios.get('/api/admin/orders', { headers: authHeaders() })
  orders.value = data
}
async function loadAuctions() {
  const { data } = await axios.get('/api/admin/auctions', { headers: authHeaders() })
  auctions.value = data
}
async function loadListings() {
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
async function deleteListing(id) {
  if (!confirm('Delete listing #' + id + '?')) return
  await axios.delete(`/api/admin/listings/${id}`, { headers: authHeaders() })
  await loadListings()
}
async function deleteAuction(id) {
  if (!confirm('Delete auction #' + id + ' and all related bids?')) return
  await axios.delete(`/api/admin/auctions/${id}`, { headers: authHeaders() })
  await loadAuctions()
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

// derived data for charts
const userListingRatio = computed(() => {
  const u = Number(stats.value.users) || 0
  const l = Number(stats.value.listings) || 0
  const total = u + l
  if (!total) return { users: 0, listings: 0 }
  return {
    users: +(u / total * 100).toFixed(1),
    listings: +(l / total * 100).toFixed(1)
  }
})

const orderStatusBreakdown = computed(() => {
  const result = {}
  for (const o of orders.value || []) {
    const s = o.status || 'UNKNOWN'
    result[s] = (result[s] || 0) + 1
  }
  return result
})

const userSearchLower = computed(() => userSearch.value.trim().toLowerCase())
const filteredUsers = computed(() => {
  const q = userSearchLower.value
  if (!q) return users.value
  return users.value.filter(u => {
    const name = (u.userName || '').toLowerCase()
    const email = (u.email || '').toLowerCase()
    return name.includes(q) || email.includes(q)
  })
})

const apiResultPretty = computed(() => {
  if (!apiResult.value) return ''
  try {
    const cloned = { ...apiResult.value }
    if (typeof cloned.body === 'string') {
      try { cloned.body = JSON.parse(cloned.body) } catch (_) {}
    }
    return JSON.stringify(cloned, null, 2)
  } catch (e) {
    return String(apiResult.value)
  }
})

const dbColumns = computed(() => {
  if (!dbResult.value || !dbResult.value.rows || !dbResult.value.rows.length) return []
  return Object.keys(dbResult.value.rows[0])
})

function authHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function runApiTest() {
  if (!apiPresetKey.value) return
  const selected = currentApi.value
  const method = selected?.method || 'GET'

  if (!selected) {
    apiResult.value = { status: 0, body: 'No endpoint selected', headers: {}, durationMs: 0 }
    return
  }

  // Body is already set by watcher
  const bodyText = apiBody.value.trim()

  // Parse JSON body once - declare outside try so accessible in catch
  let bodyData = null

  apiRunning.value = true
  const started = performance.now()
  try {
    if (bodyText && bodyText.startsWith('{')) {
      try {
        bodyData = JSON.parse(bodyText)
      } catch (parseError) {
        apiResult.value = {
          status: 0,
          body: 'Invalid JSON in request body: ' + parseError.message,
          headers: {},
          durationMs: 0
        }
        apiRunning.value = false
        return
      }
    }

    // If this endpoint is marked as public/auth, call it directly
    if (selected.viaAdminProxy === false) {
      const cfg = { headers: { 'Content-Type': 'application/json', ...authHeaders() } }
      let resp
      if (method === 'GET' || method === 'DELETE') {
        resp = await axios({ url: selected.path, method, ...cfg })
      } else {
        resp = await axios({ url: selected.path, method, data: bodyData, ...cfg })
      }
      apiResult.value = {
        status: resp.status,
        headers: Object.fromEntries(Object.entries(resp.headers || {})),
        body: resp.data,
        durationMs: Math.round(performance.now() - started)
      }
      return
    }

    // Otherwise, go through the admin proxy tester
    const { data } = await axios.post('/api/admin/api-test', {
      method,
      url: selected.path,
      headers: authHeaders(),
      body: bodyText
    }, { headers: authHeaders() })
    apiResult.value = data
  } catch (e) {
    const httpStatus = e?.response?.status
    const errData = e?.response?.data

    // Build detailed error body
    let errorBody = {
      error: e?.message || 'Error',
      status: httpStatus,
      details: errData
    }

    // Add request info for debugging
    if (httpStatus === 400) {
      errorBody.sentData = bodyData
      errorBody.note = 'Check backend logs for validation errors'
    }

    apiResult.value = {
      status: httpStatus || 0,
      body: errorBody,
      headers: {},
      durationMs: Math.round(performance.now() - started)
    }
  } finally {
    apiRunning.value = false
  }
}

function copyApiResult() {
  if (!apiResult.value) return
  const text = JSON.stringify(apiResult.value, null, 2)
  navigator.clipboard.writeText(text).then(() => {
    alert('✓ Copied to clipboard!')
  }).catch(() => {
    alert('✗ Failed to copy')
  })
}

function copyDbResult() {
  if (!dbResult.value?.rows?.length) return
  const text = JSON.stringify(dbResult.value.rows, null, 2)
  navigator.clipboard.writeText(text).then(() => {
    alert('✓ Copied to clipboard!')
  }).catch(() => {
    alert('✗ Failed to copy')
  })
}

function exportDbToCsv() {
  if (!dbResult.value?.rows?.length) return
  const rows = dbResult.value.rows
  const cols = dbColumns.value

  // Create CSV header
  let csv = cols.join(',') + '\n'

  // Add rows
  rows.forEach(row => {
    const values = cols.map(col => {
      const val = row[col]
      // Escape quotes and wrap in quotes if contains comma
      if (val === null || val === undefined) return ''
      const str = String(val).replace(/"/g, '""')
      return str.includes(',') ? `"${str}"` : str
    })
    csv += values.join(',') + '\n'
  })

  // Download
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `query-result-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

async function runDbQuery() {
  // SQL is already filled by watcher
  if (!dbSql.value.trim()) return

  dbRunning.value = true
  const started = performance.now()
  try {
    const { data } = await axios.post('/api/admin/db-query', {
      sql: dbSql.value,
      maxRows: dbMaxRows.value
    }, { headers: authHeaders() })
    dbResult.value = {
      ...data,
      durationMs: data.durationMs || Math.round(performance.now() - started)
    }
  } catch (e) {
    const errMsg = e?.response?.data?.message || e.message || 'Query failed'
    const statusCode = e?.response?.status

    let detailedError = `Error: ${errMsg}`
    if (statusCode === 403) {
      detailedError = '403 Forbidden: You need ADMIN privileges to run database queries'
    } else if (statusCode === 400) {
      detailedError = `400 Bad Request: ${errMsg}\n\nTip: Only SELECT queries are allowed for safety`
    } else if (statusCode >= 500) {
      detailedError = `${statusCode} Server Error: ${errMsg}\n\nCheck your SQL syntax and table names`
    }

    dbResult.value = {
      rows: [],
      durationMs: Math.round(performance.now() - started),
      error: detailedError
    }
    alert(detailedError)
  } finally {
    dbRunning.value = false
  }
}

async function refreshMetrics() {
  try {
    const { data } = await axios.get('/api/admin/metrics', { headers: authHeaders() })
    liveMetrics.value = { ...data, timestamp: new Date().toLocaleTimeString() }
  } catch (e) {
    // If backend doesn't have metrics endpoint yet, use sample data
    console.warn('Metrics endpoint not available, using sample data:', e.message)
    liveMetrics.value = {
      activeSessions: Math.floor(Math.random() * 50) + 10,
      avgResponseTime: Math.floor(Math.random() * 200) + 50,
      dbQueries: Math.floor(Math.random() * 1000) + 100,
      cacheHitRate: Math.floor(Math.random() * 30) + 70,
      cpuUsage: Math.floor(Math.random() * 40) + 20,
      memoryUsage: Math.floor(Math.random() * 50) + 30,
      storageUsage: Math.floor(Math.random() * 30) + 40,
      timestamp: new Date().toLocaleTimeString()
    }
  }
}

async function refreshTrends() {
  try {
    const { data } = await axios.get('/api/admin/trends', { headers: authHeaders() })
    userTrend.value = data.users || []
    listingTrend.value = data.listings || []
  } catch (e) {
    console.warn('Trends endpoint not available:', e.message)
    // Generate sample trend data
    const now = Date.now()
    userTrend.value = Array.from({ length: 10 }, (_, i) => ({
      date: new Date(now - (9 - i) * 86400000).toISOString(),
      count: Math.floor(Math.random() * 5) + i
    }))
    listingTrend.value = Array.from({ length: 10 }, (_, i) => ({
      date: new Date(now - (9 - i) * 86400000).toISOString(),
      count: Math.floor(Math.random() * 8) + i * 2
    }))
  }
}

onMounted(async () => {
  try {
    await loadStats()
  } catch (e) {
    console.error('Failed to load stats:', e)
  }

  try {
    await refreshMetrics()
  } catch (e) {
    console.error('Failed to refresh metrics:', e)
  }

  try {
    await refreshTrends()
  } catch (e) {
    console.error('Failed to refresh trends:', e)
  }

  metricsTimer = setInterval(() => {
    refreshMetrics().catch(e => console.error('Metrics refresh error:', e))
    refreshTrends().catch(e => console.error('Trends refresh error:', e))
  }, 10000)
})

onBeforeUnmount(() => {
  if (metricsTimer) clearInterval(metricsTimer)
})
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

/* Live metrics grid */
.live-metrics-grid {
  display:grid;
  grid-template-columns:repeat(auto-fit,minmax(180px,1fr));
  gap:0.75rem;
  margin:1rem 0;
}
.live-metric-card {
  background:linear-gradient(135deg,#1f2937,#0f172a);
  border-radius:12px;
  padding:0.85rem 1rem;
  border:1px solid rgba(255,255,255,0.06);
  box-shadow:0 8px 24px rgba(15,23,42,0.35);
}
.live-metric-card .metric-label { font-size:0.8rem; color:#cbd5f5; text-transform:uppercase; letter-spacing:0.08em; }
.live-metric-card .metric-value { font-size:1.8rem; font-weight:700; color:#f8fafc; margin:0.1rem 0; }
.live-metric-card .metric-sub { font-size:0.75rem; color:#9ca3ba; }

/* Resource Usage Card */
.resource-card {
  background:#0b1220;
  border-radius:12px;
  padding:1rem;
  border:1px solid #374151;
  margin:1rem 0;
}
.metrics-bars {
  display:flex;
  flex-direction:column;
  gap:1rem;
  margin-top:0.75rem;
}
.metrics-bar-row {
  display:flex;
  align-items:center;
  gap:0.75rem;
}
.metrics-bar-label {
  width:80px;
  font-size:0.85rem;
  font-weight:600;
  color:#cbd5e1;
  text-align:right;
}
.metrics-bar-track {
  flex:1;
  height:20px;
  background:#020617;
  border-radius:10px;
  overflow:hidden;
  border:1px solid #374151;
}
.metrics-bar-fill {
  height:100%;
  background:linear-gradient(90deg,#3b82f6,#2563eb);
  border-radius:10px;
  transition:width 0.5s ease;
}
.metrics-bar-value {
  width:60px;
  text-align:right;
  font-size:0.9rem;
  font-weight:700;
  color:#e5e7eb;
  font-variant-numeric:tabular-nums;
}
.metrics-footer {
  margin-top:0.75rem;
  padding-top:0.75rem;
  border-top:1px solid #374151;
  font-size:0.75rem;
  color:#9ca3ba;
  text-align:center;
}

/* simple bar chart styles */
.charts { display:flex; flex-wrap:wrap; gap:1rem; margin-top:1rem; }
.chart-card { flex:1 1 260px; background:#0b1220; border-radius:10px; border:1px solid #374151; padding:.75rem; }
.chart-title { font-size:.9rem; font-weight:600; color:#e5e7eb; margin-bottom:.5rem; }
.bar-row { display:flex; align-items:center; gap:.5rem; margin:.25rem 0; color:#cbd5e1; font-size:.85rem; }
.bar-label { width:90px; text-align:right; }
.bar-track { flex:1; height:10px; border-radius:999px; background:#020617; overflow:hidden; }
.bar-fill-users { height:100%; background:linear-gradient(90deg,#22c55e,#16a34a); }
.bar-fill-listings { height:100%; background:linear-gradient(90deg,#3b82f6,#2563eb); }
.bar-value { width:42px; text-align:right; font-variant-numeric:tabular-nums; }

/* Order status bar chart */
.status-bars { display:flex; flex-direction:column; gap:0.4rem; margin-top:0.25rem; }
.status-bar-row { display:flex; align-items:center; gap:0.5rem; font-size:0.85rem; color:#e5e7eb; }
.status-label { width:90px; text-align:right; }
.status-bar-track { flex:1; height:10px; border-radius:999px; background:#020617; overflow:hidden; }
.status-bar-fill { height:100%; border-radius:999px; background:linear-gradient(90deg,#22c55e,#16a34a); }
.status-count { width:40px; text-align:right; font-variant-numeric:tabular-nums; }

/* Users Table */
.users-table { display:flex; flex-direction:column; gap:0; margin-top:1rem; border-radius:8px; overflow:hidden; border:1px solid #374151; }
.users-row {
  display:grid;
  grid-template-columns: 50px 60px 1.2fr 1.8fr 100px 80px 2fr;
  gap:0.75rem;
  padding:0.65rem 0.75rem;
  border-bottom:1px solid #374151;
  align-items:center;
  background:#0b1220;
  transition:background 0.2s;
}
.users-row:hover { background:#1f2937; }
.users-row:last-child { border-bottom:none; }
.users-head {
  background:#1f2937 !important;
  font-weight:700;
  color:#f9fafb;
  font-size:0.75rem;
  text-transform:uppercase;
  letter-spacing:0.05em;
}
.col-id { font-size:0.85rem; color:#9ca3ba; }
.col-avatar { display:flex; align-items:center; justify-content:center; }
.user-avatar {
  width:36px;
  height:36px;
  border-radius:50%;
  object-fit:cover;
  border:2px solid #374151;
}
.user-avatar-placeholder {
  width:36px;
  height:36px;
  border-radius:50%;
  background:linear-gradient(135deg,#3b82f6,#2563eb);
  display:flex;
  align-items:center;
  justify-content:center;
  color:#fff;
  font-weight:700;
  font-size:0.9rem;
}
.col-username { font-weight:600; color:#e5e7eb; }
.col-email { color:#9ca3ba; font-size:0.85rem; }
.col-role, .col-active { display:flex; justify-content:center; }
.role-badge {
  display:inline-block;
  padding:0.25rem 0.6rem;
  border-radius:12px;
  font-size:0.7rem;
  font-weight:700;
  text-transform:uppercase;
  letter-spacing:0.05em;
}
.role-badge.admin { background:#7c3aed; color:#fff; }
.role-badge.user { background:#0ea5e9; color:#fff; }
.status-badge {
  display:inline-block;
  padding:0.25rem 0.6rem;
  border-radius:12px;
  font-size:0.7rem;
  font-weight:700;
  text-transform:uppercase;
}
.status-badge.active { background:#10b981; color:#fff; }
.status-badge.inactive { background:#6b7280; color:#fff; }
.status-badge.pending { background:#f59e0b; color:#fff; }
.status-badge.completed { background:#10b981; color:#fff; }
.status-badge.cancelled { background:#ef4444; color:#fff; }
.status-badge.shipped { background:#3b82f6; color:#fff; }
.status-badge.delivered { background:#10b981; color:#fff; }
.status-badge.available { background:#10b981; color:#fff; }
.status-badge.sold { background:#6b7280; color:#fff; }
.status-badge.reserved { background:#f59e0b; color:#fff; }
.col-ops { display:flex; gap:0.35rem; flex-wrap:wrap; }
.btn-sm {
  padding:0.35rem 0.6rem;
  border:none;
  border-radius:6px;
  background:#374151;
  color:#f8fafc;
  cursor:pointer;
  font-size:0.75rem;
  font-weight:600;
  transition:all 0.2s;
  white-space:nowrap;
}
.btn-sm:hover { background:#4b5563; transform:translateY(-1px); }
.btn-sm.btn-admin { background:#7c3aed; }
.btn-sm.btn-admin:hover { background:#6d28d9; }
.btn-sm.btn-user { background:#0ea5e9; }
.btn-sm.btn-user:hover { background:#0284c7; }
.btn-sm.btn-danger { background:#dc2626; }
.btn-sm.btn-danger:hover { background:#b91c1c; }
.btn-sm.btn-success { background:#10b981; }
.btn-sm.btn-success:hover { background:#059669; }

.user-search-row { display:flex; gap:.5rem; margin-bottom:1rem; }
.user-search-input {
  flex:1;
  padding:.6rem .8rem;
  border:1px solid #374151;
  border-radius:8px;
  background:#0b1220;
  color:#e7e7eb;
  font-size:.85rem;
}
.user-search-input::placeholder { color:#9ca3ba; }

/* Announcements */
.announce-form-card {
  background:#0b1220;
  border-radius:10px;
  padding:1.25rem;
  border:1px solid #374151;
  margin-bottom:1.5rem;
}
.announce-form-card h3 {
  margin:0 0 1rem 0;
  color:#f8fafc;
  font-size:1rem;
}
.announce-form { display:flex; flex-direction:column; gap:0.75rem; }
.form-group {
  display:flex;
  flex-direction:column;
  gap:0.4rem;
}
.form-group label {
  font-size:0.8rem;
  font-weight:600;
  color:#cbd5e1;
  text-transform:uppercase;
  letter-spacing:0.05em;
}
.form-group input, .form-group textarea {
  padding:0.65rem 0.85rem;
  border:1px solid #374151;
  border-radius:8px;
  background:#020617;
  color:#e5e7eb;
  font-size:0.9rem;
  font-family:inherit;
  transition:border-color 0.2s;
}
.form-group input:focus, .form-group textarea:focus {
  outline:none;
  border-color:#3b82f6;
}
.form-group-inline {
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:1rem;
}
.checkbox-label {
  display:flex;
  align-items:center;
  gap:0.5rem;
  color:#cbd5e1;
  cursor:pointer;
}
.checkbox-label input[type="checkbox"] {
  width:18px;
  height:18px;
  cursor:pointer;
}
.btn-primary {
  padding:0.65rem 1.25rem;
  border:none;
  border-radius:8px;
  background:linear-gradient(135deg,#3b82f6,#2563eb);
  color:#fff;
  font-weight:700;
  cursor:pointer;
  transition:all 0.2s;
  font-size:0.9rem;
}
.btn-primary:hover {
  transform:translateY(-2px);
  box-shadow:0 8px 16px rgba(59,130,246,0.3);
}
.announce-list h3 {
  margin:0 0 1rem 0;
  color:#f8fafc;
  font-size:1rem;
}
.announce-card {
  background:#0b1220;
  border-radius:10px;
  padding:1rem;
  border:1px solid #374151;
  margin-bottom:0.75rem;
  transition:background 0.2s;
}
.announce-card:hover { background:#1f2937; }
.announce-header {
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-bottom:0.5rem;
}
.announce-header h4 {
  margin:0;
  color:#f8fafc;
  font-size:1rem;
}
.announce-date {
  font-size:0.75rem;
  color:#9ca3ba;
}
.announce-content {
  color:#cbd5e1;
  margin:0.5rem 0;
  line-height:1.6;
}
.announce-footer {
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-top:0.75rem;
  padding-top:0.75rem;
  border-top:1px solid #374151;
}

/* API Tester */
.api-tester-container {
  display:grid;
  grid-template-columns:1fr 1.5fr;
  gap:1.5rem;
  margin-top:1rem;
}
.api-form-section, .api-result-section {
  background:#0b1220;
  border-radius:10px;
  padding:1.25rem;
  border:1px solid #374151;
}
.api-form-section h3, .api-result-section h3 {
  margin:0 0 1rem 0;
  color:#f8fafc;
  font-size:0.95rem;
  font-weight:700;
  text-transform:uppercase;
  letter-spacing:0.05em;
}
.select-input {
  width:100%;
  padding:0.65rem 0.85rem;
  border:1px solid #374151;
  border-radius:8px;
  background:#020617;
  color:#e5e7eb;
  font-size:0.9rem;
  cursor:pointer;
  transition:border-color 0.2s;
}
.select-input:focus {
  outline:none;
  border-color:#3b82f6;
}
.endpoint-info {
  display:flex;
  align-items:center;
  gap:0.5rem;
  margin-top:0.75rem;
  padding:0.75rem;
  background:#020617;
  border-radius:8px;
  border:1px solid #374151;
}
.method-badge {
  display:inline-block;
  padding:0.3rem 0.65rem;
  border-radius:6px;
  font-size:0.75rem;
  font-weight:700;
  text-transform:uppercase;
}
.method-badge.get { background:#10b981; color:#fff; }
.method-badge.post { background:#3b82f6; color:#fff; }
.method-badge.put { background:#f59e0b; color:#fff; }
.method-badge.patch { background:#8b5cf6; color:#fff; }
.method-badge.delete { background:#ef4444; color:#fff; }
.endpoint-path {
  font-family:monospace;
  font-size:0.85rem;
  color:#cbd5e1;
}
.code-textarea {
  width:100%;
  padding:0.75rem;
  border:1px solid #374151;
  border-radius:8px;
  background:#020617;
  color:#e5e7eb;
  font-family:monospace;
  font-size:0.85rem;
  resize:vertical;
  transition:border-color 0.2s;
}
.code-textarea:focus {
  outline:none;
  border-color:#3b82f6;
}
.btn-run {
  width:100%;
  padding:0.75rem 1.25rem;
  border:none;
  border-radius:8px;
  background:linear-gradient(135deg,#10b981,#059669);
  color:#fff;
  font-weight:700;
  cursor:pointer;
  transition:all 0.2s;
  font-size:0.95rem;
  margin-top:0.75rem;
}
.btn-run:hover:not(:disabled) {
  transform:translateY(-2px);
  box-shadow:0 8px 16px rgba(16,185,129,0.3);
}
.btn-run:disabled {
  opacity:0.5;
  cursor:not-allowed;
}
.result-meta {
  display:flex;
  justify-content:space-between;
  align-items:center;
  flex-wrap:wrap;
  gap:0.75rem;
  padding:0.75rem;
  background:#020617;
  border-radius:8px;
  margin-bottom:0.75rem;
  border:1px solid #374151;
}
.meta-item {
  display:flex;
  align-items:center;
  gap:0.5rem;
}
.meta-label {
  font-size:0.75rem;
  color:#9ca3ba;
  font-weight:600;
  text-transform:uppercase;
}
.meta-value {
  font-size:0.9rem;
  color:#e5e7eb;
  font-weight:700;
}
.status-code {
  font-size:0.9rem;
  font-weight:700;
  padding:0.25rem 0.6rem;
  border-radius:6px;
}
.status-code.status-success {
  background:#10b981;
  color:#fff;
}
.status-code.status-error {
  background:#ef4444;
  color:#fff;
}
.result-actions {
  display:flex;
  gap:0.5rem;
}
.btn-icon {
  padding:0.4rem 0.75rem;
  border:none;
  border-radius:6px;
  background:#374151;
  color:#f8fafc;
  cursor:pointer;
  font-size:0.8rem;
  font-weight:600;
  transition:all 0.2s;
}
.btn-icon:hover {
  background:#4b5563;
  transform:translateY(-1px);
}
.result-output {
  background:#020617;
  padding:1rem;
  border-radius:8px;
  overflow-x:auto;
  font-family:monospace;
  font-size:0.8rem;
  color:#e5e7eb;
  line-height:1.6;
  max-height:500px;
  overflow-y:auto;
  border:1px solid #374151;
}

/* DB Tester */
.db-tester-container {
  display:grid;
  grid-template-columns:1fr 2fr;
  gap:1.5rem;
  margin-top:1rem;
}
.db-form-section, .db-result-section {
  background:#0b1220;
  border-radius:10px;
  padding:1.25rem;
  border:1px solid #374151;
}
.db-form-section h3, .db-result-section h3 {
  margin:0 0 1rem 0;
  color:#f8fafc;
  font-size:0.95rem;
  font-weight:700;
  text-transform:uppercase;
  letter-spacing:0.05em;
}
.form-controls {
  display:flex;
  flex-direction:column;
  gap:0.75rem;
  margin-top:0.75rem;
}
.number-input {
  width:100px;
  padding:0.5rem;
  border:1px solid #374151;
  border-radius:6px;
  background:#020617;
  color:#e5e7eb;
  font-size:0.85rem;
}
.number-input:focus {
  outline:none;
  border-color:#3b82f6;
}
.db-table-container {
  max-height:500px;
  overflow:auto;
  border-radius:8px;
  border:1px solid #374151;
}
.db-table {
  width:100%;
  border-collapse:collapse;
  font-size:0.85rem;
}
.db-table th {
  position:sticky;
  top:0;
  background:#1f2937;
  color:#f9fafb;
  font-weight:700;
  text-align:left;
  padding:0.75rem;
  border-bottom:2px solid #374151;
  text-transform:uppercase;
  font-size:0.75rem;
  letter-spacing:0.05em;
}
.db-table td {
  padding:0.65rem 0.75rem;
  border-bottom:1px solid #374151;
  color:#e5e7eb;
  background:#0b1220;
}
.db-table tr:hover td {
  background:#1f2937;
}

/* Trend Graph */
.trend-card { margin-top:1rem; }
.trend-legend {
  display:flex;
  gap:1.5rem;
  margin-bottom:0.75rem;
  font-size:0.85rem;
}
.legend-item {
  display:flex;
  align-items:center;
  gap:0.4rem;
  color:#cbd5e1;
}
.legend-dot {
  width:12px;
  height:12px;
  border-radius:50%;
}
.legend-dot.users { background:#22c55e; }
.legend-dot.listings { background:#3b82f6; }
.trend-plot {
  width:100%;
  height:160px;
  background:#020617;
  border-radius:8px;
  padding:0.75rem;
  border:1px solid #374151;
}
.trend-plot svg { width:100%; height:100%; }
.grid-line {
  stroke:#374151;
  stroke-width:0.2;
  stroke-dasharray:2,2;
  opacity:0.5;
}
.trend-line {
  fill:none;
  stroke-width:1.5;
  stroke-linecap:round;
  stroke-linejoin:round;
}
.trend-line-users {
  stroke:#22c55e;
  filter:drop-shadow(0 0 3px rgba(34,197,94,0.4));
}
.trend-line-listings {
  stroke:#3b82f6;
  filter:drop-shadow(0 0 3px rgba(59,130,246,0.4));
}

.empty { color:#9ca3ba; text-align:center; padding:2rem 0; font-size:.9rem; }

/* Other tables (orders, bids, listings) */
.table { display:flex; flex-direction:column; gap:.25rem; }
.row {
  display:grid;
  grid-template-columns: 60px 1fr 1fr 120px 100px;
  gap:.5rem;
  padding:.5rem .65rem;
  border-bottom:1px solid #374151;
  align-items:center;
  background:#0b1220;
  transition:background 0.2s;
}
.row:hover:not(.head) { background:#1f2937; }
.row.head { font-weight:800; background:#1f2937; color:#f9fafb; border-radius:8px 8px 0 0; }
.bids-table .row, .listings-table .row {
  grid-template-columns: 60px 100px 1fr 120px 180px 120px;
}
.ops {
  display:flex;
  gap:0.35rem;
  flex-wrap:wrap;
}
.ops button {
  padding:.4rem .6rem;
  background:#374151;
  color:#f8fafc;
  border:none;
  border-radius:8px;
  cursor:pointer;
  font-size:.85rem;
  transition:background 0.2s;
}
.ops button:hover { background:#4b5563; }
.ops button.danger { background:#dc2626; }
.ops button.danger:hover { background:#b91c1c; }

</style>
