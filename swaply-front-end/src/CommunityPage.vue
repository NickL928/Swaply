<script setup>
import { ref, onMounted } from 'vue'
import ThreadList from './components/community/ThreadList.vue'
import communityApi from './services/communityApi.js'

const loading = ref(false)
const error = ref('')
const threads = ref([])

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

onMounted(load)
</script>

<template>
  <div class="community-page">
    <header class="community-header">
      <h1>Community</h1>
      <p class="subtitle">Discuss, ask questions, and share updates.</p>
    </header>

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
</style>