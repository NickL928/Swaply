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
/* Remove local header styles; use shared AppHeader */

/* Layout */
.main { padding: 1.5rem 2rem; background: #f8fafc; min-height: calc(100vh - 64px); }
.content-wrap { max-width: 1000px; margin: 0 auto; }
.community-header { margin: .25rem 0 1rem 0; }
.subtitle { color: #64748b; }
.status { margin: 1rem 0; color: #475569; }
.error { color: #dc2626; }

/* Post form */
.thread-post-form {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1.25rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.thread-post-form h2 { margin: 0 0 .75rem 0; font-size: 1.15rem; color: #334155; }
.form-row { display: grid; grid-template-columns: 1fr 200px; gap: 1rem; }
@media (max-width: 640px) { .form-row { grid-template-columns: 1fr; } }
.form-group { margin-bottom: .75rem; }
.thread-post-form label { display: block; font-weight: 600; margin-bottom: .4rem; color: #475569; }
.thread-post-form input,
.thread-post-form select,
.thread-post-form textarea {
  width: 100%; padding: .7rem .8rem; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 1rem; box-sizing: border-box;
}
.primary-btn {
  background: #4f46e5; color: #fff; border: none; padding: .7rem 1.2rem; border-radius: 8px; font-weight: 700; cursor: pointer;
}
.primary-btn:disabled { opacity: .7; cursor: not-allowed; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 200;
}
.modal {
  width: min(860px, 96vw);
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 50px rgba(2,6,23,0.2);
  position: relative;
}
.modal-close {
  position: absolute;
  right: .75rem;
  top: .75rem;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: .35rem .6rem;
  cursor: pointer;
}
.modal-status { padding: 2rem; }
.modal-status.error { color: #dc2626; }
.modal-body { padding: 1.25rem 1.25rem 1rem; }
.modal-header { margin-bottom: .75rem; }
.modal-title { margin: 0; }
.full-body { color: #1f2937; white-space: pre-wrap; }
.modal-footer {
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stats { display: flex; gap: 1rem; color: #475569; }
.badge {
  background: #eef2ff;
  color: #4f46e5;
  padding: .15rem .5rem;
  border-radius: .5rem;
  font-size: .75rem;
}

/* Replies section */
.replies-section { margin-top: 1rem; border-top: 1px solid #e2e8f0; padding-top: 1rem; }
.replies-title { margin: 0 0 .5rem 0; font-size: 1.05rem; color: #334155; }
.replies-status { color: #475569; margin-bottom: .5rem; }
.replies-status.error { color: #dc2626; }
.replies-list { list-style: none; padding: 0; margin: 0 0 1rem 0; display: flex; flex-direction: column; gap: .75rem; }
.reply-item { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: .6rem .75rem; }
.reply-meta { color: #64748b; font-size: .85rem; display: flex; gap: .4rem; }
.reply-body { color: #1f2937; margin-top: .3rem; white-space: pre-wrap; }
.reply-empty { color: #64748b; font-style: italic; }
.reply-form { display: grid; grid-template-columns: 1fr auto; gap: .5rem; align-items: start; }
.reply-form textarea { width: 100%; padding: .6rem .7rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 1rem; resize: vertical; }
</style>