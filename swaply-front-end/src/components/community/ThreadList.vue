<script setup>
import ThreadCard from './ThreadCard.vue'

const props = defineProps({
  threads: { type: Array, required: true }
})
</script>

<template>
  <div class="thread-list">
    <div v-if="!threads || !threads.length" class="empty">No threads yet.</div>
    <div v-else class="grid">
      <ThreadCard v-for="t in threads" :key="t.id" :thread="t" />
    </div>
  </div>
</template>

<style scoped>
.thread-list { display: block; }
.grid {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr));
  gap: 1rem;
}
@media (min-width: 720px) {
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
.empty { color: #64748b; padding: 1rem; }
</style>

