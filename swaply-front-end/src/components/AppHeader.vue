<template>
  <header class="header">
    <div class="header-content">
      <div class="logo-section" @click="go('home')" style="cursor:pointer">
        <img alt="Swaply logo" class="logo" src="../assets/logo.png" width="48" height="48" />
        <span class="logo-text">Swaply</span>
      </div>
      <nav class="nav-menu">
        <a href="#" class="nav-link" :class="{active: active==='home'}" @click.prevent="go('home')">Home</a>
        <a href="#" class="nav-link" :class="{active: active==='community'}" @click.prevent="go('community')">Community</a>
        <a href="#" class="nav-link" :class="{active: active==='bidding'}" @click.prevent="go('bidding')">Bidding</a>
        <a href="#" class="nav-link" :class="{active: active==='announcements'}" @click.prevent="go('announcements')">System Announcements</a>
      </nav>
      <div class="header-actions">
        <button class="pill-btn" @click="openChat">💬 DM</button>
        <img :src="avatar" alt="me" class="avatar" @click="go('profile')" />
        <button class="pill-btn" @click="$emit('logout')">Logout</button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ active: { type: String, default: '' } })
const emit = defineEmits(['navigate','logout'])
const go = (page) => emit('navigate', page)
const openChat = () => window.dispatchEvent(new Event('open-chat'))
const avatar = computed(() => {
  try {
    const u = JSON.parse(localStorage.getItem('user'))
    const p = u?.profileImageUrl
    if (!p) return '/favicon.ico'
    if (p.startsWith('http://') || p.startsWith('https://')) return p
    if (p.startsWith('/uploads/')) return p
    if (p.startsWith('uploads/')) return '/' + p
    return p
  } catch { return '/favicon.ico' }
})
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  position: sticky; top: 0; z-index: 100;
}
.header-content { padding: 1rem 2rem; display:flex; align-items:center; justify-content:space-between; gap:1rem; }
.logo-section { display:flex; align-items:center; gap:.6rem; }
.logo { filter: brightness(0) invert(1); border-radius: 8px; width: 48px; height: 48px; }
.logo-text { color:#fff; font-weight:800; font-size:1.4rem; }
.nav-menu { display:flex; gap:.5rem; justify-content:center; }
.nav-link { text-decoration:none; color:rgba(255,255,255,0.9); font-weight:600; padding:.5rem 1rem; border-radius:12px; transition:all .2s ease; }
.nav-link:hover, .nav-link.active { color:#fff; background:rgba(255,255,255,0.2); }
.header-actions { display:flex; align-items:center; gap:.5rem; }
.pill-btn { background:rgba(255,255,255,0.2); color:#fff; border:2px solid rgba(255,255,255,0.3); padding:.5rem .9rem; border-radius:12px; font-weight:700; cursor:pointer; }
.avatar { width:32px; height:32px; border-radius:50%; border:2px solid rgba(255,255,255,0.5); background:#fff; object-fit:cover; }
</style>
