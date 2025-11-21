<script setup>
import { ref, onMounted } from 'vue'
import ThreadList from './components/community/ThreadList.vue'
import communityApi from './services/communityApi.js'
import AppHeader from './components/AppHeader.vue'

const emit = defineEmits(['navigate'])

const loading = ref(false)
const error = ref('')
const threads = ref([])

const newThread = ref({ title: '', body: '', category: 'GENERAL' })
const posting = ref(false)
const postError = ref('')

// Modal state
const isModalOpen = ref(false)
const selectedThread = ref(null)
const detailsLoading = ref(false)
const detailsError = ref('')

// Local like-state tracking (since backend doesn't persist per-user likes)
const likedMap = ref({})
function loadLikedMap() {
  try { likedMap.value = JSON.parse(localStorage.getItem('likedThreads') || '{}') } catch { likedMap.value = {} }
}
function saveLikedMap() {
  localStorage.setItem('likedThreads', JSON.stringify(likedMap.value || {}))
}

// Replies state
const replies = ref([])
const repliesLoading = ref(false)
const repliesError = ref('')
const replyText = ref('')
const replyPosting = ref(false)

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { items } = await communityApi.listThreads({ page: 1, size: 10 })
    threads.value = items
  } catch (e) {
    console.error(e)
    error.value = 'Failed to load threads'
  } finally {
    loading.value = false
  }
}

async function handlePostThread() {
  postError.value = ''
  if (!newThread.value.title.trim() || !newThread.value.body.trim()) {
    postError.value = 'Title and content are required.'
    return
  }
  posting.value = true
  try {
    await communityApi.postThread({
      title: newThread.value.title,
      body: newThread.value.body,
      category: newThread.value.category
    })
    newThread.value = { title: '', body: '', category: 'GENERAL' }
    await load()
  } catch (e) {
    postError.value = e?.response?.status === 401 ? 'Please log in to post.' : 'Failed to post thread.'
  } finally {
    posting.value = false
  }
}

const goHome = () => emit('navigate', 'home')

async function loadReplies(threadId) {
  repliesLoading.value = true
  repliesError.value = ''
  try {
    const { items } = await communityApi.listReplies(threadId, { page: 1, size: 100 })
    replies.value = items
  } catch (e) {
    console.error(e)
    repliesError.value = 'Failed to load replies.'
  } finally {
    repliesLoading.value = false
  }
}

async function postReply() {
  if (!selectedThread.value) return
  if (!replyText.value.trim()) return
  replyPosting.value = true
  try {
    const newReply = await communityApi.addReply(selectedThread.value.id, { body: replyText.value })
    replies.value = [...replies.value, newReply]
    replyText.value = ''
    // bump counts locally
    selectedThread.value = { ...selectedThread.value, stats: { ...selectedThread.value.stats, replies: (selectedThread.value.stats?.replies ?? 0) + 1 } }
    const idx = threads.value.findIndex(t => t.id === selectedThread.value.id)
    if (idx !== -1) {
      const t = threads.value[idx]
      threads.value[idx] = { ...t, stats: { ...t.stats, replies: (t.stats?.replies ?? 0) + 1 } }
    }
  } catch (e) {
    if (e?.response?.status === 401) alert('Please log in to reply.')
  } finally {
    replyPosting.value = false
  }
}

async function openThread(id) {
  isModalOpen.value = true
  detailsLoading.value = true
  detailsError.value = ''
  try {
    const data = await communityApi.getThread(id, { incrementView: true })
    selectedThread.value = { ...data, likedByMe: !!likedMap.value[id] }
    // reflect increased view in list as well
    const idx = threads.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      const t = threads.value[idx]
      const newViews = (t.stats?.views ?? 0) + 1
      threads.value[idx] = { ...t, stats: { ...t.stats, views: newViews } }
    }
    await loadReplies(id)
  } catch (e) {
    console.error(e)
    detailsError.value = 'Failed to load thread details.'
  } finally {
    detailsLoading.value = false
  }
}

function closeModal() {
  isModalOpen.value = false
  selectedThread.value = null
}

async function likeFromList(thread) {
  // Allow like without opening modal
  try {
    const id = thread.id
    const liked = !!likedMap.value[id]
    const res = liked ? await communityApi.unlikeThread(id) : await communityApi.likeThread(id)
    likedMap.value[id] = !liked
    saveLikedMap()
    // update list counts
    const idx = threads.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      threads.value[idx] = { ...threads.value[idx], stats: { ...threads.value[idx].stats, likes: res.stats?.likes ?? 0 } }
    }
  } catch (e) {
    if (e?.response?.status === 401) alert('Please log in to like threads.')
  }
}

async function toggleLikeInModal() {
  if (!selectedThread.value) return
  const id = selectedThread.value.id
  try {
    const liked = !!likedMap.value[id]
    const res = liked ? await communityApi.unlikeThread(id) : await communityApi.likeThread(id)
    likedMap.value[id] = !liked
    saveLikedMap()
    // update modal view and list view
    selectedThread.value = { ...selectedThread.value, likedByMe: !liked, stats: { ...selectedThread.value.stats, likes: res.stats?.likes ?? 0 } }
    const idx = threads.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      threads.value[idx] = { ...threads.value[idx], stats: { ...threads.value[idx].stats, likes: res.stats?.likes ?? 0 } }
    }
  } catch (e) {
    if (e?.response?.status === 401) alert('Please log in to like threads.')
  }
}

onMounted(() => { loadLikedMap(); load() })
</script>

<template>
  <div class="community-root">
    <AppHeader active="community" @navigate="(p)=>{ if(p==='home') goHome(); else $emit('navigate',p) }" />

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">💬 Community Forum</h1>
        <p class="hero-subtitle">Connect, discuss, and share with fellow traders</p>
      </div>
    </section>

    <main class="main">
      <div class="content-wrap">
        <!-- Thread post form -->
        <div class="thread-post-form">
          <h2>Start a New Thread</h2>
          <form @submit.prevent="handlePostThread">
            <div class="form-row">
              <div class="form-group">
                <label>Title</label>
                <input v-model="newThread.title" type="text" maxlength="100" required placeholder="Thread title" />
              </div>
              <div class="form-group">
                <label>Category</label>
                <select v-model="newThread.category">
                  <option value="GENERAL">General</option>
                  <option value="GUIDE">Guide</option>
                  <option value="REQUEST">Request</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>Content</label>
              <textarea v-model="newThread.body" rows="4" maxlength="1000" required placeholder="What do you want to discuss?"></textarea>
            </div>
            <div class="alert error" v-if="postError">{{ postError }}</div>
            <button type="submit" class="primary-btn" :disabled="posting">{{ posting ? 'Posting...' : 'Post Thread' }}</button>
          </form>
        </div>

        <div class="status" v-if="loading || error">
          <span v-if="loading">Loading threads…</span>
          <span v-else class="error">{{ error }}</span>
        </div>

        <ThreadList v-if="!loading && !error" :threads="threads" @open="openThread" @like="likeFromList" />
      </div>
    </main>

    <!-- Thread Details Modal -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <button class="modal-close" @click="closeModal">✕</button>
        <div v-if="detailsLoading" class="modal-status">Loading…</div>
        <div v-else-if="detailsError" class="modal-status error">{{ detailsError }}</div>
        <div v-else-if="selectedThread" class="modal-body">
          <header class="modal-header">
            <h2 class="modal-title">{{ selectedThread.title }}</h2>
            <div class="thread-meta">
              <span class="author">{{ selectedThread.author?.name || 'Unknown' }}</span>
              <span>•</span>
              <span class="time">{{ new Date(selectedThread.createdAt).toLocaleString() }}</span>
              <span v-if="selectedThread.category" class="badge">{{ selectedThread.category }}</span>
            </div>
          </header>
          <article class="modal-content">
            <p class="full-body">{{ selectedThread.body }}</p>
          </article>
          <footer class="modal-footer">
            <div class="stats">
              <span class="stat">👁️ {{ selectedThread.stats?.views ?? 0 }}</span>
              <span class="stat">❤️ {{ selectedThread.stats?.likes ?? 0 }}</span>
              <span class="stat">💬 {{ selectedThread.stats?.replies ?? 0 }}</span>
            </div>
            <button class="primary-btn" @click="toggleLikeInModal">{{ selectedThread.likedByMe ? 'Unlike' : 'Like' }}</button>
          </footer>

          <!-- Replies section -->
          <section class="replies-section">
            <h3 class="replies-title">Replies</h3>
            <div v-if="repliesLoading" class="replies-status">Loading replies…</div>
            <div v-else-if="repliesError" class="replies-status error">{{ repliesError }}</div>
            <ul v-else class="replies-list">
              <li v-for="r in replies" :key="r.id" class="reply-item">
                <div class="reply-meta">
                  <strong>{{ r.author?.name || 'Unknown' }}</strong>
                  <span>•</span>
                  <span>{{ new Date(r.createdAt).toLocaleString() }}</span>
                </div>
                <div class="reply-body">{{ r.body }}</div>
              </li>
              <li v-if="!replies.length" class="reply-empty">No replies yet.</li>
            </ul>

            <div class="reply-form">
              <textarea v-model="replyText" rows="3" placeholder="Write a reply…"></textarea>
              <button class="primary-btn" :disabled="replyPosting || !replyText.trim()" @click="postReply">
                {{ replyPosting ? 'Posting…' : 'Post Reply' }}
              </button>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-root {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 3rem 2rem;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
  margin: 0 0 0.5rem 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
}

/* Layout */
.main {
  padding: 3rem 2rem 4rem 2rem;
  background: #f8fafc;
  min-height: calc(100vh - 200px);
  border-radius: 32px 32px 0 0;
  margin-top: -2rem;
  position: relative;
  z-index: 2;
}

.content-wrap {
  max-width: 1000px;
  margin: 0 auto;
}

.status {
  margin: 1rem 0;
  color: #475569;
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 12px;
}

.error {
  color: #dc2626;
}

/* Post form */
.thread-post-form {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.thread-post-form:hover {
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
}

.thread-post-form h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 200px;
  gap: 1rem;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-group {
  margin-bottom: 1rem;
}

.thread-post-form label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #475569;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.thread-post-form input,
.thread-post-form select,
.thread-post-form textarea {
  width: 100%;
  padding: 0.875rem 1rem;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: white;
}

.thread-post-form input:focus,
.thread-post-form select:focus,
.thread-post-form textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.alert.error {
  background: #fee2e2;
  color: #dc2626;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  border-left: 4px solid #dc2626;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.875rem 1.5rem;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  font-size: 1rem;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 200;
}

.modal {
  width: min(860px, 96vw);
  max-height: 90vh;
  overflow-y: auto;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-close {
  position: absolute;
  right: 1rem;
  top: 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  font-size: 1.25rem;
  transition: all 0.2s ease;
  z-index: 1;
}

.modal-close:hover {
  background: #ef4444;
  color: white;
  border-color: #ef4444;
}

.modal-status {
  padding: 3rem;
  text-align: center;
  color: #64748b;
}

.modal-status.error {
  color: #dc2626;
}

.modal-body {
  padding: 2rem;
}

.modal-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f1f5f9;
}

.modal-title {
  margin: 0 0 0.75rem 0;
  font-size: 1.75rem;
  color: #1e293b;
}

.thread-meta {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  color: #64748b;
  font-size: 0.9rem;
  flex-wrap: wrap;
}

.author {
  font-weight: 600;
  color: #475569;
}

.time {
  color: #94a3b8;
}

.badge {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  color: #667eea;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
}

.modal-content {
  margin: 1.5rem 0;
}

.full-body {
  color: #334155;
  white-space: pre-wrap;
  line-height: 1.7;
  font-size: 1.05rem;
}

.modal-footer {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.stats {
  display: flex;
  gap: 1.5rem;
  color: #64748b;
  font-size: 1rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 600;
}

/* Replies section */
.replies-section {
  margin-top: 2rem;
  border-top: 2px solid #f1f5f9;
  padding-top: 2rem;
}

.replies-title {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  color: #1e293b;
  font-weight: 700;
}

.replies-status {
  color: #64748b;
  margin-bottom: 1rem;
  text-align: center;
  padding: 1rem;
}

.replies-status.error {
  color: #dc2626;
  background: #fee2e2;
  border-radius: 8px;
}

.replies-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1.5rem 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.reply-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1rem;
  transition: all 0.2s ease;
}

.reply-item:hover {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.reply-meta {
  color: #64748b;
  font-size: 0.85rem;
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.reply-meta strong {
  color: #475569;
}

.reply-body {
  color: #334155;
  white-space: pre-wrap;
  line-height: 1.6;
}

.reply-empty {
  color: #94a3b8;
  font-style: italic;
  text-align: center;
  padding: 2rem;
}

.reply-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
}

.reply-form textarea {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  resize: vertical;
  font-family: inherit;
  transition: all 0.3s ease;
}

.reply-form textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.reply-form button {
  align-self: flex-end;
}
</style>