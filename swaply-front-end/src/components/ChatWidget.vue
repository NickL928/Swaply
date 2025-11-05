<template>
  <div class="chat-widget">
    <!-- Removed floating toggle button; DM opens from header button -->

    <!-- Modal popup teleported to body when open -->
    <teleport to="body">
      <div v-if="modelOpen" class="overlay" @click.self="emitClose">
        <div class="panel modal">
          <header class="header">
            <strong>Direct Messages</strong>
            <button class="close" @click="emitClose">×</button>
          </header>
          <div class="body">
            <div class="left">
              <div class="search">
                <input v-model="userQuery" placeholder="Search users by name" />
              </div>
              <div class="user-list">
                <div class="user-item" v-for="c in conversations" :key="c.peerId" :class="{ active: c.peerId === activeUserId }" @click="openConversation(c)">
                  <img :src="resolveImage(c.peerAvatarUrl)" class="avatar" alt="u" />
                  <div class="name" style="flex:1">
                    <div class="row1">
                      <span>{{ c.peerName }}</span>
                      <small v-if="c.lastTimestamp" class="ts">{{ formatTime(c.lastTimestamp) }}</small>
                    </div>
                    <small class="preview" v-if="c.lastContent">{{ c.lastContent }}</small>
                  </div>
                  <span v-if="c.unreadCount" class="badge" style="margin-left:.4rem">{{ c.unreadCount }}</span>
                </div>
                <div v-for="u in filteredUsers" :key="u.userId" class="user-item muted" @click="openChat(u)">
                  <img :src="resolveImage(u.profileImageUrl)" class="avatar" alt="u" />
                  <div class="name">{{ u.userName }}</div>
                </div>
              </div>
            </div>
            <div class="right">
              <div class="chat-header" v-if="activeUser">
                <img :src="resolveImage(activeUser.profileImageUrl)" class="avatar" alt="a" />
                <div class="meta">
                  <div class="name">{{ activeUser.userName }}</div>
                  <small>Private conversation</small>
                </div>
              </div>
              <div class="empty" v-else>
                Select a user to start chatting
              </div>

              <div class="messages" ref="messagesBox">
                <div v-for="(m, idx) in messagesForActive" :key="idx" :class="['msg', m.fromUserId===meId ? 'out' : 'in']">
                  <div class="bubble">{{ m.content }}</div>
                </div>
              </div>

              <form class="composer" v-if="activeUser" @submit.prevent="send">
                <input v-model="draft" placeholder="Type a message..." />
                <button :disabled="!draft.trim()">Send</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import SockJS from 'sockjs-client'
import { Client as StompClient } from '@stomp/stompjs'
import userApi from '../services/userApi.js'
import chatApi from '../services/chatApi.js'

const props = defineProps({ baseUrl: { type: String, default: '' }, open: { type: Boolean, default: false } })
const emit = defineEmits(['update:open'])

const modelOpen = ref(props.open)
watch(() => props.open, v => { modelOpen.value = v })
const emitToggle = () => emit('update:open', !modelOpen.value)
const emitClose = () => emit('update:open', false)

const meId = (()=>{ try { return JSON.parse(localStorage.getItem('user'))?.userId } catch { return null } })()
const users = ref([])
const conversations = ref([])
const userQuery = ref('')
const activeUserId = ref(null)
const draft = ref('')
const unread = ref({}) // userId -> count

const messages = ref({}) // userId -> array of ChatMessageDto
const messagesBox = ref(null)

const filteredUsers = computed(() => {
  const q = userQuery.value.trim().toLowerCase()
  if (!q) return []
  return users.value.filter(u => u.userId !== meId && (u.userName||'').toLowerCase().includes(q))
})

const activeUser = computed(() => {
  return conversations.value.find(c => c.peerId === activeUserId.value) || users.value.find(u => u.userId === activeUserId.value)
})
const messagesForActive = computed(() => messages.value[activeUserId.value] || [])
const unreadCount = computed(() => Object.values(unread.value).reduce((a,b)=>a+b,0))

let stomp = null
let sub = null

async function loadUsers(){
  try {
    const { data } = await userApi.list()
    users.value = (data || []).map(u => ({ userId: u.userId, userName: u.userName, profileImageUrl: u.profileImageUrl }))
  } catch (e) {
    // fall back silently; user can still open conversations if any
    console.error('load users failed', e)
  }
}

async function loadConversations(){
  if (!meId) return
  try {
    const { data } = await chatApi.getConversations()
    conversations.value = data || []
    const map = {}
    for (const c of conversations.value) map[c.peerId] = c.unreadCount || 0
    unread.value = map
  } catch (e) { console.error('load conversations failed', e) }
}

function connect(){
  // establish (or reuse) a single app-wide connection
  if (stomp && stomp.connected) return
  const token = localStorage.getItem('token')
  if (!token || !meId) return // don't attempt without auth context
  const url = (props.baseUrl || '') + '/ws-chat' + ('?token=' + encodeURIComponent(token))
  const socketFactory = () => new SockJS(url)
  try {
    stomp = new StompClient({
      connectHeaders: { Authorization: 'Bearer ' + token },
      webSocketFactory: socketFactory,
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        const destination = `/user/queue/messages`
        try { sub = stomp.subscribe(destination, onMessage) } catch (e) { console.error('subscribe failed', e) }
      },
      onStompError: (frame) => { console.error('STOMP error', frame.body) },
      onWebSocketClose: () => { /* auto-retry handled by reconnectDelay */ }
    })
    stomp.activate()
  } catch (e) { console.error('stomp init failed', e) }
}

function promotePeer(peerId, incoming){
  // ensure peer is in conversations; if not, add a basic shell using users list or payload
  let c = conversations.value.find(x => x.peerId === peerId)
  if (!c) {
    const u = users.value.find(x => x.userId === peerId) || { userId: peerId, userName: `User ${peerId}` }
    c = { peerId, peerName: u.userName, peerAvatarUrl: u.profileImageUrl, lastContent: '', lastTimestamp: null, unreadCount: 0 }
    conversations.value.unshift(c)
  } else {
    // move to top
    conversations.value = [c, ...conversations.value.filter(x => x.peerId !== peerId)]
  }
  if (incoming) c.unreadCount = (c.unreadCount||0) + 1
}

function persistState(){
  try {
    localStorage.setItem('chat_conversations', JSON.stringify(conversations.value))
    localStorage.setItem('chat_messages', JSON.stringify(messages.value))
  } catch {}
}

function restoreState(){
  try {
    const c = JSON.parse(localStorage.getItem('chat_conversations')||'[]')
    const m = JSON.parse(localStorage.getItem('chat_messages')||'{}')
    if (Array.isArray(c)) conversations.value = c
    if (m && typeof m === 'object') messages.value = m
    // rebuild unread map
    const map = {}
    for (const conv of conversations.value) map[conv.peerId] = conv.unreadCount || 0
    unread.value = map
  } catch {}
}

// persist when conversations or messages change
watch(conversations, persistState, { deep: true })
watch(messages, persistState, { deep: true })

function onMessage(msg){
  try {
    const m = JSON.parse(msg.body)
    const peerId = m.fromUserId === meId ? m.toUserId : m.fromUserId
    if (!messages.value[peerId]) messages.value[peerId] = []
    messages.value[peerId].push(m)

    // update conversation preview
    promotePeer(peerId, m.fromUserId !== meId)
    const c = conversations.value.find(x => x.peerId === peerId)
    if (c) { c.lastContent = m.content; c.lastTimestamp = m.timestamp }

    if (activeUserId.value !== peerId) unread.value[peerId] = (unread.value[peerId]||0) + 1
    nextTick(()=>{ if(messagesBox.value) messagesBox.value.scrollTop = messagesBox.value.scrollHeight })
  } catch (e) { console.error('ws message parse error', e) }
}

function disconnect(){
  try { if (sub) sub.unsubscribe() } catch{}
  try { if (stomp) stomp.deactivate() } catch{}
  sub = null; stomp = null
}

function openConversation(c){
  activeUserId.value = c.peerId
  unread.value[c.peerId] = 0
  c.unreadCount = 0
  chatApi.markRead(meId, c.peerId).catch(()=>{})
  ensureThreadLoaded(c.peerId)
  nextTick(()=>{ if(messagesBox.value) messagesBox.value.scrollTop = messagesBox.value.scrollHeight })
}

function openChat(u){
  // manual search selection
  activeUserId.value = u.userId
  unread.value[u.userId] = 0
  ensureThreadLoaded(u.userId)
  nextTick(()=>{ if(messagesBox.value) messagesBox.value.scrollTop = messagesBox.value.scrollHeight })
}

async function ensureThreadLoaded(peerId){
  if (messages.value[peerId] && messages.value[peerId].length) return
  try {
    const { data } = await chatApi.getThread(meId, peerId)
    messages.value[peerId] = data || []
  } catch (e) { console.error('load thread failed', e) }
}

function send(){
  const content = draft.value.trim()
  if (!content || !activeUserId.value) return
  const payload = { fromUserId: meId, toUserId: activeUserId.value, content }

  if (stomp && stomp.connected) {
    try { stomp.publish({ destination: '/app/chat.private', body: JSON.stringify(payload) }) } catch{}
    // Do NOT optimistic-push here to avoid duplicate when server echoes back
  } else {
    // Fallback to REST: push only the server response to avoid duplicates
    chatApi.send(payload).then(({ data }) => {
      const peerId = payload.toUserId
      if (!messages.value[peerId]) messages.value[peerId] = []
      messages.value[peerId].push(data)
      const c = conversations.value.find(x => x.peerId === peerId)
      if (c) { c.lastContent = data.content; c.lastTimestamp = data.timestamp; promotePeer(peerId, false) }
      nextTick(()=>{ if(messagesBox.value) messagesBox.value.scrollTop = messagesBox.value.scrollHeight })
    }).catch(e=>console.error('send rest failed', e))
  }

  draft.value = ''
  nextTick(()=>{ if(messagesBox.value) messagesBox.value.scrollTop = messagesBox.value.scrollHeight })
}

function resolveImage(path){
  if (!path) return '/favicon.ico'
  if (path.startsWith('http')) return path
  return (props.baseUrl || '') + path
}

function formatTime(ts){
  try { return new Date(ts).toLocaleString() } catch { return '' }
}

onMounted(()=>{
  restoreState()
  connect()
  loadUsers()
  loadConversations()
  window.addEventListener('auth-changed', onAuthChanged)
})

onBeforeUnmount(()=>{ disconnect(); window.removeEventListener('auth-changed', onAuthChanged) })

function onAuthChanged(e){
  // refresh meId, reconnect, and reload data
  try {
    const u = JSON.parse(localStorage.getItem('user'))
    if (u?.userId) {
      // eslint-disable-next-line no-global-assign
      // update reactive context
    }
  } catch {}
  disconnect()
  // re-evaluate id and token
  const u = (()=>{ try { return JSON.parse(localStorage.getItem('user')) } catch { return null } })()
  if (u?.userId) {
    // re-init
    connect()
    loadUsers()
    loadConversations()
  }
}
</script>

<style scoped>
.chat-widget{ position:fixed; right:16px; bottom:16px; z-index:10000 }
/* Remove .toggle styles if desired; keeping harmless styles is okay */
.badge{ background:#ef4444; color:#fff; border-radius:999px; padding:.12rem .4rem; margin-left:.5rem; font-size:.8rem; font-weight:800 }

/* Modal overlay */
.overlay{ position:fixed; inset:0; background:rgba(15,23,42,.55); backdrop-filter: blur(2px); display:flex; align-items:center; justify-content:center; z-index:10000 }
.panel.modal{ width:920px; height:600px; max-width:95vw; max-height:85vh; background:#fff; border:1px solid #e5e7eb; border-radius:16px; box-shadow:0 20px 32px rgba(0,0,0,.25); overflow:hidden }
.header{ display:flex; align-items:center; justify-content:space-between; padding:.6rem .9rem; border-bottom:1px solid #e5e7eb; font-weight:800; color:#111827 }
.close{ border:none; background:transparent; font-size:1.2rem; cursor:pointer }
.body{ display:flex; height:calc(100% - 48px) }
.left{ width:320px; border-right:1px solid #e5e7eb; display:flex; flex-direction:column }
.search{ padding:.5rem; border-bottom:1px solid #e5e7eb }
.search input{ width:100%; padding:.55rem .6rem; border-radius:10px; border:2px solid #e5e7eb }
.user-list{ flex:1; overflow:auto }
.user-item{ display:flex; align-items:center; gap:.5rem; padding:.5rem .6rem; cursor:pointer }
.user-item:hover{ background:#f8fafc }
.user-item.active{ background:#eef2ff }
.user-item.muted{ opacity:.8 }
.avatar{ width:36px; height:36px; border-radius:50%; object-fit:cover; border:1px solid #e2e8f0 }
.right{ flex:1; display:flex; flex-direction:column }
.chat-header{ display:flex; align-items:center; gap:.6rem; padding:.6rem .8rem; border-bottom:1px solid #e5e7eb }
.meta .name{ font-weight:800; color:#0f172a }
.empty{ padding:1rem; color:#475569; font-weight:700 }
.messages{ flex:1; overflow:auto; padding:.75rem .9rem; background:#f8fafc }
.msg{ display:flex; margin:.25rem 0 }
.msg .bubble{ max-width:72%; padding:.5rem .7rem; border-radius:14px; font-weight:600 }
.msg.in{ justify-content:flex-start }
.msg.in .bubble{ background:#e2e8f0; color:#0f172a }
.msg.out{ justify-content:flex-end }
.msg.out .bubble{ background:#4f46e5; color:#fff }
.composer{ display:flex; gap:.5rem; padding:.6rem .8rem; border-top:1px solid #e5e7eb }
.composer input{ flex:1; padding:.6rem .7rem; border-radius:12px; border:2px solid #e5e7eb }
.composer button{ background:#4f46e5; border:none; color:#fff; border-radius:10px; padding:.5rem .8rem; font-weight:800 }
.row1{ display:flex; justify-content:space-between; align-items:center }
.preview{ color:#475569 }
.ts{ color:#64748b; margin-left:.4rem }
@media (max-width: 860px){ .panel.modal{ width:95vw; height:70vh } }
</style>
