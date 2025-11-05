<template>
  <div class="chat-page">
    <header class="top">
      <div class="left">
        <button class="btn back" @click="goHome">← Back</button>
        <h1>Direct Messages</h1>
      </div>
      <div class="right">
        <span class="me" v-if="meName">Signed in as <strong>{{ meName }}</strong></span>
        <span class="state" :data-state="connState">{{ connState }}</span>
      </div>
    </header>

    <div v-if="connState === 'unauthorized'" class="alert">
      <span>Login required to use chat.</span>
      <button class="btn s" @click="goHome">Go to Login</button>
    </div>

    <div class="shell" v-else>
      <aside class="sidebar">
        <div class="search">
          <input v-model="query" placeholder="Search users by name" @keydown.enter.prevent="search" />
          <button class="btn s" @click="search">Search</button>
        </div>
        <div class="list">
          <div v-for="u in users" :key="u.userId" :class="['item', { active: u.userId === activeUserId }]" @click="select(u)">
            <img :src="resolveImage(u.profileImageUrl)" class="avatar" alt="avatar" />
            <div class="meta">
              <div class="name">{{ u.userName }}</div>
            </div>
          </div>
        </div>
      </aside>

      <main class="main">
        <div class="empty" v-if="!activeUser">Select a user to start chatting</div>
        <template v-else>
          <header class="thread-header">
            <div class="peer">
              <img :src="resolveImage(activeUser.profileImageUrl)" class="avatar" alt="avatar" />
              <div class="meta">
                <div class="name">{{ activeUser.userName }}</div>
                <small>Private conversation</small>
              </div>
            </div>
          </header>

          <section class="messages" ref="box">
            <div v-for="(m, idx) in thread" :key="idx" :class="['msg', m.fromUserId===meId ? 'out':'in']">
              <div class="bubble">{{ m.content }}</div>
            </div>
          </section>

          <footer class="composer">
            <input v-model="draft" placeholder="Type a message..." @keydown.enter.prevent="send" />
            <button class="btn send" :disabled="!draft.trim() || sending" @click="send">{{ sending ? 'Sending...' : 'Send' }}</button>
          </footer>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
// Remove eager imports of WS libs to avoid runtime issues; load them dynamically inside connect()
import userApi from './services/userApi.js'
import chatApi from './services/chatApi.js'
import axios from 'axios'

const emit = defineEmits(['navigate'])

const me = (()=>{ try { return JSON.parse(localStorage.getItem('user')) } catch { return null } })()
const meId = me?.userId || me?.id
const meName = me?.userName || me?.username

const query = ref('')
const users = ref([])
const activeUserId = ref(null)
const activeUser = computed(() => users.value.find(u => u.userId === activeUserId.value))
const messages = ref({}) // userId -> array
const thread = computed(() => messages.value[activeUserId.value] || [])
const draft = ref('')
const box = ref(null)
const connState = ref('disconnected')
const sending = ref(false)

function resolveImage(p){ if(!p) return './assets/logo.png'; if(p.startsWith('http')) return p; if(p.startsWith('/uploads/')) return p; if(p.startsWith('uploads/')) return '/' + p; return p }

async function search(){
  const name = query.value.trim()
  if (!name) return
  try {
    const { data } = await userApi.getByUsername(name)
    const arr = Array.isArray(data) ? data : (data ? [data] : [])
    users.value = arr
    if (arr.length) { activeUserId.value = arr[0].userId }
  } catch { /* ignore */ }
}

async function loadThread(){
  if (!activeUserId.value || !meId) return
  try {
    const { data } = await chatApi.getThread(meId, activeUserId.value)
    messages.value[activeUserId.value] = data || []
    nextTick(()=>{ if(box.value) box.value.scrollTop = box.value.scrollHeight })
  } catch (e) { console.error('load thread', e) }
}

function select(u){ activeUserId.value = u.userId; nextTick(()=>{ if(box.value) box.value.scrollTop = box.value.scrollHeight }); loadThread() }

let stomp = null
let sub = null

async function connect(){
  try {
    if (stomp && stomp.connected) return
    const token = localStorage.getItem('token')
    if (!token || !meId) { connState.value = 'unauthorized'; return }
    connState.value = 'connecting'
    const { default: SockJS } = await import('sockjs-client')
    const { Client: StompClient } = await import('@stomp/stompjs')
    const url = '/ws-chat?token=' + encodeURIComponent(token)
    const socketFactory = () => new SockJS(url)
    stomp = new StompClient({
      connectHeaders: { Authorization: 'Bearer ' + token, token },
      webSocketFactory: socketFactory,
      debug: () => {},
      onConnect: () => {
        connState.value = 'connected'
        // Subscribe to user destination without embedding userId
        sub = stomp.subscribe('/user/queue/messages', (msg) => {
          try {
            const m = JSON.parse(msg.body)
            const peerId = m.fromUserId === meId ? m.toUserId : m.fromUserId
            if (!messages.value[peerId]) messages.value[peerId] = []
            messages.value[peerId].push(m)
            nextTick(()=>{ if(box.value) box.value.scrollTop = box.value.scrollHeight })
          } catch (e) { console.error('WS parse', e) }
        })
      },
      onStompError: (frame) => { console.error('STOMP error', frame?.body); connState.value = 'error' },
      onWebSocketClose: () => { connState.value = 'disconnected' }
    })
    stomp.activate()
  } catch (e) {
    console.error('WS init failed', e)
    connState.value = 'error'
  }
}

function disconnect(){ try { if (sub) sub.unsubscribe(); if (stomp) stomp.deactivate() } catch {}
  sub = null; stomp = null; connState.value = 'disconnected' }

function canSend(){ return !!draft.value.trim() && !!activeUserId.value }

async function send(){
  if (!canSend()) return
  const payload = { fromUserId: meId, toUserId: activeUserId.value, content: draft.value.trim() }
  // optimistic UI
  if (!messages.value[activeUserId.value]) messages.value[activeUserId.value] = []
  messages.value[activeUserId.value].push({ ...payload, timestamp: new Date().toISOString() })
  nextTick(()=>{ if(box.value) box.value.scrollTop = box.value.scrollHeight })
  draft.value = ''

  sending.value = true
  try {
    if (stomp && stomp.connected) {
      stomp.publish({ destination: '/app/chat.private', body: JSON.stringify(payload) })
    } else {
      await chatApi.send(payload)
    }
  } catch (e) {
    console.error('send failed', e)
  } finally {
    sending.value = false
    setTimeout(loadThread, 200)
  }
}
function goHome(){ emit('navigate','home') }

onMounted(()=>{ connect() })

onBeforeUnmount(()=>{ disconnect() })
</script>

<style scoped>
.chat-page{ min-height:100vh; background:#f8fafc; padding:1rem }
.top{ display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem }
.top .left{ display:flex; align-items:center; gap:.75rem }
.top h1{ margin:0; font-size:1.6rem; font-weight:800; color:#0f172a }
.me{ color:#475569; font-weight:700 }
.btn{ cursor:pointer; border:none; border-radius:12px; font-weight:800; padding:.6rem .9rem }
.back{ background:#e2e8f0; color:#0f172a }

.shell{ display:grid; grid-template-columns:320px 1fr; gap:1rem; height:calc(100vh - 120px) }
.sidebar{ background:#fff; border:1px solid #e2e8f0; border-radius:16px; overflow:hidden; display:flex; flex-direction:column }
.search{ display:flex; gap:.5rem; padding:.6rem; border-bottom:1px solid #e2e8f0 }
.search input{ flex:1; padding:.55rem .7rem; border-radius:10px; border:2px solid #e2e8f0 }
.search .s{ background:#4f46e5; color:#fff }
.list{ flex:1; overflow:auto }
.item{ display:flex; align-items:center; gap:.6rem; padding:.6rem .75rem; cursor:pointer; border-bottom:1px solid #f1f5f9 }
.item:hover{ background:#f8fafc }
.item.active{ background:#eef2ff }
.avatar{ width:28px; height:28px; border-radius:50%; object-fit:cover; border:1px solid #e2e8f0 }

.main{ background:#fff; border:1px solid #e2e8f0; border-radius:16px; display:flex; flex-direction:column; overflow:hidden }
.thread-header{ display:flex; align-items:center; justify-content:space-between; padding:.6rem .9rem; border-bottom:1px solid #e2e8f0 }
.peer{ display:flex; align-items:center; gap:.6rem }
.meta .name{ font-weight:800; color:#0f172a }
.empty{ padding:2rem; text-align:center; color:#64748b; font-weight:800 }
.messages{ flex:1; overflow:auto; padding:.75rem .9rem; background:#f8fafc }
.msg{ display:flex; margin:.25rem 0 }
.msg .bubble{ max-width:70%; padding:.5rem .7rem; border-radius:14px; font-weight:600 }
.msg.in{ justify-content:flex-start }
.msg.in .bubble{ background:#e2e8f0; color:#0f172a }
.msg.out{ justify-content:flex-end }
.msg.out .bubble{ background:#4f46e5; color:#fff }
.composer{ display:flex; gap:.6rem; padding:.6rem .9rem; border-top:1px solid #e2e8f0 }
.composer input{ flex:1; padding:.6rem .7rem; border-radius:12px; border:2px solid #e2e8f0 }
.composer .send{ background:#4f46e5; color:#fff }
.state{ margin-left:1rem; padding:.2rem .5rem; border-radius:999px; font-weight:700; color:#0f172a; background:#e2e8f0; text-transform:capitalize }
.alert{ margin:0 0 1rem 0; padding:.6rem .9rem; background:#fef3c7; border:1px solid #fde68a; border-radius:12px; color:#92400e; font-weight:700; display:flex; align-items:center; gap:.75rem }

@media (max-width: 960px){
  .shell{ grid-template-columns: 1fr; height:auto }
}
</style>
