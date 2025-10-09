<script setup>
const props = defineProps({
  thread: { type: Object, required: true }
})

const formatDate = (iso) => {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

const bodySnippet = (text, len = 140) => {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}
</script>

<template>
  <article class="thread-card">
    <header class="thread-header">
      <h3 class="thread-title">{{ thread.title }}</h3>
      <div class="thread-meta">
        <span class="author">{{ thread.author?.name || 'Unknown' }}</span>
        <span>•</span>
        <span class="time">{{ formatDate(thread.createdAt) }}</span>
        <span v-if="thread.category" class="badge">{{ thread.category }}</span>
      </div>
    </header>
    <p class="thread-snippet">{{ bodySnippet(thread.body) }}</p>
    <footer class="thread-footer">
      <span class="stat">💬 {{ thread.stats?.replies ?? 0 }}</span>
      <span class="stat">❤️ {{ thread.stats?.likes ?? 0 }}</span>
      <span v-if="thread.stats?.views != null" class="stat">👁️ {{ thread.stats.views }}</span>
    </footer>
  </article>
</template>

<style scoped>
.thread-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1rem;
  background: #fff;
  transition: box-shadow .2s, transform .2s;
}
.thread-card:hover {
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}
.thread-title {
  margin: 0 0 .25rem 0;
}
.thread-meta {
  display: flex;
  gap: .5rem;
  color: #64748b;
  font-size: .9rem;
}
.badge {
  background: #eef2ff;
  color: #4f46e5;
  padding: .15rem .5rem;
  border-radius: .5rem;
  font-size: .75rem;
}
.thread-snippet {
  color: #334155;
  margin: .75rem 0 0 0;
}
.thread-footer {
  margin-top: .75rem;
  display: flex;
  gap: 1rem;
  color: #475569;
  font-size: .9rem;
}
.stat { display: inline-flex; align-items: center; gap: .25rem; }
</style>

