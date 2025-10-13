<script setup>
import { ref, onMounted } from 'vue'
import ThreadList from './components/community/ThreadList.vue'
import communityApi from './services/communityApi.js'

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

onMounted(load)
</script>

<template>
  <div class="community-page">
    <header class="community-header">
      <h1>Community</h1>
      <p class="subtitle">Discuss, ask questions, and share updates.</p>
    </header>

    <!-- Thread post form -->
    <div class="thread-post-form">
      <h2>Start a New Thread</h2>
      <form @submit.prevent="handlePostThread">
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
        <div class="form-group">
          <label>Content</label>
          <textarea v-model="newThread.body" rows="4" maxlength="1000" required placeholder="What do you want to discuss?"></textarea>
        </div>
        <div v-if="postError" class="alert error">{{ postError }}</div>
        <button type="submit" :disabled="posting">{{ posting ? 'Posting...' : 'Post Thread' }}</button>
      </form>
    </div>

    <div class="status" v-if="loading || error">
      <span v-if="loading">Loading threads…</span>
      <span v-else class="error">{{ error }}</span>
    </div>

    <ThreadList v-if="!loading && !error" :threads="threads" />
  </div>
</template>

<style scoped>
.community-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 1.5rem;
}
.community-header {
  margin-bottom: 1rem;
}
.subtitle {
  color: #64748b;
}
.status {
  margin: 1rem 0;
  color: #475569;
}
.error {
  color: #dc2626;
}
.thread-post-form {
  background: #f1f5f9;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.thread-post-form h2 {
  margin-bottom: 1rem;
  font-size: 1.3rem;
  color: #334155;
}
.thread-post-form .form-group {
  margin-bottom: 1rem;
}
.thread-post-form label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #475569;
}
.thread-post-form input,
.thread-post-form select,
.thread-post-form textarea {
  width: 100%;
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  font-size: 1rem;
  margin-bottom: 0.25rem;
  box-sizing: border-box;
}
.thread-post-form button {
  background: #4f46e5;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.thread-post-form button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>