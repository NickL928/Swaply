<template>
  <div class="profile-container">
    <header class="profile-header">
      <div class="logo-block" @click="$emit('navigate','home')">
        <img src="./assets/logo.png" alt="Logo" class="logo" />
        <h1>Swaply</h1>
      </div>
      <div class="header-actions">
        <button class="home-btn" @click="$emit('navigate','home')">Home</button>
        <button class="logout-btn" @click="$emit('logout')">Logout</button>
      </div>
    </header>

    <main class="content">
      <!-- Left: Profile + forms -->
      <section class="profile-panel">
        <div class="card user-summary">
          <div class="avatar-wrapper">
            <img :src="avatarPreview || avatarUrl" class="avatar" alt="Avatar" />
            <input ref="fileInput" type="file" accept="image/*" class="hidden-file" @change="onFileChosen" />
            <button class="change-avatar-btn" :disabled="uploadingAvatar" @click="triggerFileSelect">
              {{ uploadingAvatar ? 'Uploading...' : 'Change' }}
            </button>
          </div>
          <div class="user-meta">
            <h2>{{ form.userName || 'User' }}</h2>
            <p class="email">{{ form.email }}</p>
            <p class="role" v-if="user?.role">Role: {{ user.role }}</p>
            <p class="meta-dates" v-if="user?.createdAt">Joined: {{ formatDate(user.createdAt) }}</p>
          </div>
        </div>

        <div class="card edit-form">
          <h3>Update Profile</h3>
          <form @submit.prevent="submitProfile">
            <label>
              Username
              <input v-model="form.userName" type="text" required />
            </label>
            <label>
              Email
              <input v-model="form.email" type="email" required />
            </label>
            <div class="password-toggle" @click="showChangePassword = !showChangePassword">
              <span>{{ showChangePassword ? '▼' : '▶' }} Change Password</span>
            </div>
            <transition name="fade">
              <div v-if="showChangePassword" class="password-fields">
                <label>Current Password
                  <input v-model="passwords.current" type="password" />
                </label>
                <label>New Password
                  <input v-model="passwords.newPass" type="password" />
                </label>
                <label>Confirm New Password
                  <input v-model="passwords.confirm" type="password" />
                </label>
              </div>
            </transition>
            <div class="actions">
              <button type="submit" :disabled="saving">{{ saving ? 'Saving...' : 'Save Changes' }}</button>
              <button type="button" class="danger" @click="confirmDelete" :disabled="deleting">
                {{ deleting ? 'Deleting...' : 'Delete Account' }}
              </button>
            </div>
            <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
            <p v-if="successMessage" class="success">{{ successMessage }}</p>
          </form>
        </div>
      </section>

      <!-- Right: User Listings -->
      <section class="listings-panel">
        <div class="card listings-card">
          <div class="listings-header">
            <h3>My Listings</h3>
            <button class="new-btn" @click="$emit('navigate','post-item')">+ New Listing</button>
          </div>
          <div v-if="loadingListings" class="status">Loading listings...</div>
          <div v-else-if="listingError" class="status error">{{ listingError }}</div>
          <div v-else-if="!userListings.length" class="status empty">You haven't posted any listings yet.</div>
          <div class="user-listings" v-else>
            <div class="listing-row" v-for="l in userListings" :key="l.listingId">
              <div class="thumb">
                <img :src="resolveImage(l.imageUrl)" alt="img" />
              </div>
              <div class="info">
                <h4>{{ l.title }}</h4>
                <p class="small">{{ truncate(l.description, 100) }}</p>
                <div class="tags">
                  <span class="tag">{{ prettyEnum(l.category) }}</span>
                  <span class="tag muted">{{ prettyEnum(l.condition) }}</span>
                  <span class="tag status" :class="l.status?.toLowerCase()">{{ l.status }}</span>
                </div>
              </div>
              <div class="price">{{ formatPrice(l.price) }}</div>
              <div class="ops">
                <button @click="editListing(l)">Edit</button>
                <button class="danger" @click="deleteListing(l)">Delete</button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import userApi from './services/userApi.js'
import listingApi from './services/listingApi.js'
import { fmtFt } from './services/currency.js'

const props = defineProps({ user: { type: Object, required: true } })
const emit = defineEmits(['user-updated','navigate','logout'])

const form = reactive({ userName: '', email: '' })
const showChangePassword = ref(false)
const passwords = reactive({ current: '', newPass: '', confirm: '' })
const saving = ref(false)
const deleting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const avatarUrl = ref('./assets/logo.png')
const avatarPreview = ref('')
const uploadingAvatar = ref(false)
const fileInput = ref(null)

const userListings = ref([])
const loadingListings = ref(false)
const listingError = ref('')

watch(() => props.user, (u) => {
  if (u) {
    form.userName = u.userName || ''
    form.email = u.email || ''
    avatarUrl.value = u.profileImageUrl ? resolveImage(u.profileImageUrl) : './assets/logo.png'
  }
}, { immediate: true })

function resetMessages() {
  errorMessage.value = ''
  successMessage.value = ''
}

async function loadListings() {
  if (!props.user?.userId) return
  loadingListings.value = true
  listingError.value = ''
  try {
    const { data } = await listingApi.getListingsByUser(props.user.userId)
    userListings.value = data || []
  } catch (e) {
    listingError.value = 'Failed to load listings'
  } finally {
    loadingListings.value = false
  }
}

onMounted(() => {
  loadListings()
})

function validatePasswords() {
  if (!showChangePassword.value) return true
  if (!passwords.current || !passwords.newPass || !passwords.confirm) {
    errorMessage.value = 'Fill all password fields'
    return false
  }
  if (passwords.newPass !== passwords.confirm) {
    errorMessage.value = 'New passwords do not match'
    return false
  }
  return true
}

async function submitProfile() {
  resetMessages()
  if (!validatePasswords()) return
  saving.value = true
  try {
    const payload = { userName: form.userName, email: form.email }
    if (showChangePassword.value && passwords.newPass) {
      payload.password = passwords.newPass
    }
    await userApi.updateUser(props.user.userId, payload)
    // After update, fetch latest user data from backend for consistency
    try {
      const refreshed = await userApi.getUser(props.user.userId)
      const updatedUser = refreshed.data
      localStorage.setItem('user', JSON.stringify(updatedUser))
      emit('user-updated', updatedUser)
      // Update avatarUrl in case backend changed it elsewhere
      avatarUrl.value = updatedUser.profileImageUrl ? resolveImage(updatedUser.profileImageUrl) : './assets/logo.png'
    } catch {/* ignore secondary error */}
    successMessage.value = 'Profile updated successfully'
    showChangePassword.value = false
    passwords.current = passwords.newPass = passwords.confirm = ''
  } catch (e) {
    errorMessage.value = e.response?.data || 'Update failed'
  } finally {
    saving.value = false
  }
}

function confirmDelete() {
  if (!confirm('Are you sure you want to delete your account? This cannot be undone.')) return
  deleteAccount()
}

async function deleteAccount() {
  deleting.value = true
  try {
    await userApi.deleteUser(props.user.userId)
    alert('Account deleted')
    emit('logout')
  } catch (e) {
    errorMessage.value = 'Delete failed'
  } finally {
    deleting.value = false
  }
}

function editListing(listing) {
  alert('Edit listing not implemented yet. Listing ID: ' + listing.listingId)
}

async function deleteListing(listing) {
  // Could call DELETE /api/listings/{listingId}/user/{userId}
  if (!confirm('Delete this listing?')) return
  try {
    await fetch(`/api/listings/${listing.listingId}/user/${props.user.userId}`, {
      method: 'DELETE',
      headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
    })
    userListings.value = userListings.value.filter(l => l.listingId !== listing.listingId)
  } catch (e) {
    alert('Failed to delete')
  }
}

function triggerFileSelect() {
  resetMessages()
  if (fileInput.value) fileInput.value.click()
}

function clearAvatarPreview() {
  if (avatarPreview.value) {
    try { URL.revokeObjectURL(avatarPreview.value) } catch {}
    avatarPreview.value = ''
  }
}

function onFileChosen(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  // basic validation: max 5MB, image/*
  if (!file.type.startsWith('image/')) {
    errorMessage.value = 'Please select an image file'
    return
  }
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    errorMessage.value = 'Image must be under 5MB'
    return
  }
  // Preview
  clearAvatarPreview()
  avatarPreview.value = URL.createObjectURL(file)
  uploadAvatar(file)
}

async function uploadAvatar(file) {
  uploadingAvatar.value = true
  try {
    const resp = await listingApi.uploadImage(file)
    const imageUrl = resp.data?.url
    if (!imageUrl) throw new Error('No URL returned from upload')

    // Update user profile with new image URL, keep other fields
    await userApi.updateUser(props.user.userId, {
      userName: form.userName,
      email: form.email,
      profileImageUrl: imageUrl
    })

    // Refresh user data
    try {
      const refreshed = await userApi.getUser(props.user.userId)
      const updatedUser = refreshed.data
      localStorage.setItem('user', JSON.stringify(updatedUser))
      emit('user-updated', updatedUser)
      avatarUrl.value = updatedUser.profileImageUrl ? resolveImage(updatedUser.profileImageUrl) : './assets/logo.png'
    } catch {/* ignore */}

    successMessage.value = 'Profile picture updated'
  } catch (e) {
    errorMessage.value = e.response?.data || e.message || 'Upload failed'
  } finally {
    // Always clear preview and input so the final server image shows and no leaks
    clearAvatarPreview()
    uploadingAvatar.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function truncate(text, max) {
  if (!text) return ''
  return text.length > max ? text.slice(0, max - 1) + '…' : text
}

function prettyEnum(val) {
  if (!val) return ''
  return val.toLowerCase().split('_').map(w => w[0].toUpperCase() + w.slice(1)).join(' ')
}

function formatPrice(price) {
  return fmtFt(price)
}

function resolveImage(path) {
  if (!path) return './assets/logo.png'
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/uploads/')) return path
  if (path.startsWith('uploads/')) return '/' + path
  return path
}

function formatDate(dt) {
  try {
    return new Date(dt).toLocaleDateString()
  } catch { return dt }
}
</script>

<style scoped>
.profile-container { min-height:100vh; background:#f1f5f9; display:flex; flex-direction:column; }
.profile-header { display:flex; justify-content:space-between; align-items:center; padding:1rem 2rem; background:linear-gradient(135deg,#4f46e5,#7c3aed); color:#fff; }
.logo-block { display:flex; align-items:center; gap:.75rem; cursor:pointer; }
.logo { width:42px; height:42px; filter:brightness(0) invert(1); }
.header-actions button { margin-left:.75rem; }
.home-btn, .logout-btn { background:rgba(255,255,255,.2); border:1px solid rgba(255,255,255,.3); color:#fff; padding:.6rem 1.1rem; border-radius:10px; cursor:pointer; font-weight:600; }
.home-btn:hover, .logout-btn:hover { background:rgba(255,255,255,.3); }
.content { display:flex; gap:2rem; padding:2rem; flex:1; align-items:flex-start; }
.profile-panel { width:380px; display:flex; flex-direction:column; gap:1.5rem; }
.listings-panel { flex:1; }
.card { background:#fff; border-radius:18px; padding:1.5rem 1.6rem; box-shadow:0 4px 16px rgba(0,0,0,.07); border:1px solid #e2e8f0; }
.user-summary { display:flex; gap:1rem; }
.avatar-wrapper { position:relative; display:flex; flex-direction:column; align-items:center; }
.avatar { width:90px; height:90px; border-radius:50%; background:#eef2ff; object-fit:cover; border:4px solid #fff; box-shadow:0 2px 6px rgba(0,0,0,.15); }
.change-avatar-btn { margin-top:.5rem; font-size:.7rem; padding:.35rem .6rem; border-radius:6px; border:1px solid #cbd5e1; background:#f8fafc; cursor:pointer; }
.change-avatar-btn:disabled { opacity:.7; cursor:not-allowed; }
.hidden-file { display:none; }
.user-meta h2 { margin:0 0 .4rem; font-size:1.4rem; }
.user-meta .email { color:#64748b; margin:0 0 .3rem; font-size:.9rem; }
.user-meta .role, .meta-dates { font-size:.75rem; text-transform:uppercase; letter-spacing:.5px; color:#475569; margin:.2rem 0; font-weight:600; }
.edit-form h3 { margin:0 0 1rem; font-size:1.15rem; }
.edit-form form { display:flex; flex-direction:column; gap:.9rem; }
.edit-form label { display:flex; flex-direction:column; font-size:.8rem; font-weight:600; color:#334155; gap:.35rem; }
.edit-form input { padding:.65rem .75rem; border:2px solid #e2e8f0; border-radius:10px; font-size:.85rem; background:#fff; }
.edit-form input:focus { outline:none; border-color:#6366f1; box-shadow:0 0 0 3px rgba(99,102,241,.25); }
.password-toggle { font-size:.8rem; font-weight:600; cursor:pointer; color:#4f46e5; margin-top:.25rem; }
.password-fields { display:flex; flex-direction:column; gap:.75rem; }
.actions { display:flex; gap:.75rem; margin-top:.25rem; }
.actions button { flex:1; padding:.7rem 1rem; background:#4f46e5; color:#fff; border:none; font-weight:600; border-radius:10px; cursor:pointer; font-size:.85rem; }
.actions button:hover { background:#4338ca; }
.actions button.danger { background:#ef4444; }
.actions button.danger:hover { background:#dc2626; }
.error { color:#b91c1c; font-size:.75rem; margin-top:.1rem; font-weight:600; }
.success { color:#059669; font-size:.75rem; margin-top:.1rem; font-weight:600; }
.listings-card { display:flex; flex-direction:column; gap:1rem; }
.listings-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:.25rem; }
.new-btn { background:#10b981; color:#fff; border:none; padding:.55rem 1rem; border-radius:10px; cursor:pointer; font-weight:600; }
.new-btn:hover { background:#059669; }
.status { text-align:center; padding:1rem; font-size:.85rem; font-weight:600; color:#475569; }
.status.error { color:#b91c1c; }
.status.empty { color:#64748b; font-weight:500; }
.user-listings { display:flex; flex-direction:column; gap:.8rem; }
.listing-row { display:grid; grid-template-columns:70px 1fr 100px 120px; align-items:center; gap:1rem; padding:.9rem 1rem; border:1px solid #e2e8f0; border-radius:14px; background:#f8fafc; transition:.25s; }
.listing-row:hover { background:#fff; box-shadow:0 4px 14px rgba(0,0,0,.07); }
.thumb img { width:70px; height:70px; object-fit:cover; border-radius:12px; background:#fff; border:1px solid #e2e8f0; }
.info h4 { margin:0 0 .35rem; font-size:.95rem; }
.info .small { margin:0; font-size:.7rem; color:#64748b; }
.tags { display:flex; flex-wrap:wrap; gap:.35rem; margin-top:.4rem; }
.tag { background:#eef2ff; color:#4338ca; padding:.25rem .55rem; font-size:.55rem; border-radius:999px; font-weight:700; letter-spacing:.5px; text-transform:uppercase; }
.tag.muted { background:#f1f5f9; color:#475569; }
.tag.status { background:#d1fae5; color:#065f46; }
.tag.status.sold { background:#fee2e2; color:#b91c1c; }
.price { font-weight:700; font-size:.9rem; color:#111827; }
.ops { display:flex; gap:.5rem; }
.ops button { background:#6366f1; color:#fff; border:none; padding:.45rem .7rem; font-size:.65rem; border-radius:8px; cursor:pointer; font-weight:600; }
.ops button:hover { background:#4f46e5; }
.ops .danger { background:#ef4444; }
.ops .danger:hover { background:#dc2626; }

@media (max-width: 1100px) {
  .content { flex-direction:column; }
  .profile-panel { width:100%; flex-direction:row; align-items:stretch; }
  .profile-panel .card { flex:1; }
  .listings-panel { width:100%; }
}
@media (max-width: 800px) {
  .profile-panel { flex-direction:column; }
  .listing-row { grid-template-columns:60px 1fr 80px 90px; }
}
@media (max-width: 560px) {
  .listing-row { grid-template-columns:60px 1fr 70px; }
  .ops { display:none; }
}
.fade-enter-active, .fade-leave-active { transition: opacity .25s; }
.fade-enter-from, .fade-leave-to { opacity:0; }
</style>
