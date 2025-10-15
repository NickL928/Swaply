<script setup>
import { ref, onMounted } from 'vue'
import ThreadList from './components/community/ThreadList.vue'
import communityApi from './services/communityApi.js'

const emit = defineEmits(['navigate'])

const loading = ref(false)
const error = ref('')
const threads = ref([])

const newThread = ref({ title: '', body: '', category: 'GENERAL' })
const posting = ref(false)
const postError = ref('')

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
    postError.value = 'Failed to post thread.'
  } finally {
    posting.value = false
  }
}

const goHome = () => emit('navigate', 'home')

onMounted(load)
</script>

<template>
  <div class="community-root">
    <!-- Themed Header -->
    <header class="header">
      <div class="header-content">
        <div class="logo-section" @click="goHome" style="cursor:pointer">
          <img alt="Swaply logo" class="logo" src="./assets/logo.svg" width="36" height="36" />
          <span class="logo-text">Swaply</span>
        </div>
        <nav class="nav-menu">
          <a href="#" class="nav-link" @click.prevent="goHome">Home</a>
          <a href="#" class="nav-link active">Community</a>
        </nav>
        <div class="header-actions">
          <button class="pill-btn" @click="goHome">Back to Home</button>
        </div>
      </div>
    </header>

    <main class="main">
      <div class="content-wrap">
        <header class="community-header">
          <h1>Community</h1>
          <p class="subtitle">Discuss, ask questions, and share updates.</p>
        </header>

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
            <div v-if="postError" class="alert error">{{ postError }}</div>
            <button type="submit" class="primary-btn" :disabled="posting">{{ posting ? 'Posting...' : 'Post Thread' }}</button>
          </form>
        </div>

        <div class="status" v-if="loading || error">
          <span v-if="loading">Loading threads…</span>
          <span v-else class="error">{{ error }}</span>
        </div>

        <ThreadList v-if="!loading && !error" :threads="threads" />
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Header (match Home) */
.header {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}
.logo-section { display: flex; align-items: center; gap: .6rem; }
.logo { filter: brightness(0) invert(1); border-radius: 8px; }
.logo-text { color: #fff; font-weight: 800; font-size: 1.4rem; }
.nav-menu { display: flex; gap: .5rem; justify-content: center; }
.nav-link {
  text-decoration: none;
  color: rgba(255,255,255,0.9);
  font-weight: 600;
  padding: 0.5rem 1rem;
  border-radius: 12px;
  transition: all .2s ease;
}
.nav-link:hover, .nav-link.active { color: #fff; background: rgba(255,255,255,0.2); }
.header-actions { display: flex; align-items: center; gap: .5rem; }
.pill-btn {
  background: rgba(255,255,255,0.2);
  color: white;
  border: 2px solid rgba(255,255,255,0.3);
  padding: 0.5rem 0.9rem;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}

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
</style>