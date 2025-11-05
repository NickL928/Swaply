import axios from 'axios'

const client = axios.create({ baseURL: '/api/chat' })
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

export default {
  getThread(a, b) { return client.get('/thread', { params: { a, b } }) },
  getConversations() { return client.get('/conversations') },
  markRead(userId, peerId) { return client.post('/mark-read', null, { params: { userId, peerId } }) },
  send(dto) { return client.post('/send', dto) },
}
