<template>
  <div class="ann-page">
    <header class="top">
      <div class="brand" @click="$emit('navigate','home')">
        <img src="./assets/logo.png" alt="logo" width="40" height="40" />
        <h1>System Announcements</h1>
      </div>
      <div class="spacer" />
      <button class="back" @click="$emit('navigate','home')">Back to Home</button>
    </header>

    <main class="main">
      <div class="wrap">
        <div class="card" v-for="a in anns" :key="a.id">
          <h2 class="title">{{ a.title }}</h2>
          <small class="time">{{ formatTime(a.createdAt) }}</small>
          <p class="content">{{ a.content }}</p>
        </div>
        <div v-if="!loading && !anns.length" class="empty">No announcements yet.</div>
        <div v-if="loading" class="loading">Loading...</div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import announcementApi from './services/announcementApi.js'

const anns = ref([])
const loading = ref(false)

function formatTime(ts){ try { return new Date(ts).toLocaleString() } catch { return ts } }

async function load(){
  loading.value = true
  try { const { data } = await announcementApi.list(); anns.value = data || [] }
  catch(e){ console.error(e) }
  finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.ann-page { min-height:100vh; background:#f8fafc; display:flex; flex-direction:column }
.top { display:flex; align-items:center; gap:1rem; padding:1rem 1.25rem; background:#111827; color:#fff }
.brand { display:flex; align-items:center; gap:.6rem; cursor:pointer }
.brand img { width:40px; height:40px; filter:brightness(0) invert(1) }
.brand h1 { margin:0; font-size:1.15rem }
.spacer { flex:1 }
.back { background:#4f46e5; color:#fff; border:none; padding:.5rem .8rem; border-radius:8px; font-weight:800 }
.main { padding:1.25rem }
.wrap { max-width:880px; margin:0 auto }
.card { background:#fff; border:1px solid #e2e8f0; border-radius:12px; padding:1rem; margin-bottom:.75rem }
.title { margin:0 0 .25rem 0; color:#0f172a }
.time { color:#64748b }
.content { color:#334155; white-space:pre-wrap }
.empty, .loading { text-align:center; color:#64748b; font-weight:700; padding:1rem }
</style>
