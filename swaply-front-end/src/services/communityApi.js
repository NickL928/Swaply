// Use real backend API for community threads via Vite proxy
import axios from 'axios'

const API_BASE = '/api/threads'

const client = axios.create({ baseURL: API_BASE })
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

export default {
  async listThreads({ page = 1, size = 10, q, category } = {}) {
    const params = { page, size }
    if (q) params.q = q
    if (category) params.category = category
    const { data } = await client.get('', { params })
    return data // { items, page, size, total }
  },

  async getThread(id, { incrementView = true } = {}) {
    const { data } = await client.get(`/${id}`, { params: { inc: incrementView } })
    return data // ThreadDto
  },

  async postThread({ title, body, category }) {
    const { data } = await client.post('', { title, body, category })
    return data // ThreadDto
  },

  async likeThread(id) {
    const { data } = await client.post(`/${id}/like`)
    return data
  },

  async unlikeThread(id) {
    const { data } = await client.post(`/${id}/unlike`)
    return data
  },

  // Replies
  async listReplies(threadId, { page = 1, size = 20 } = {}) {
    const { data } = await client.get(`/${threadId}/replies`, { params: { page, size } })
    return data // { items, page, size, total }
  },

  async addReply(threadId, { body }) {
    const { data } = await client.post(`/${threadId}/replies`, { body })
    return data // ThreadReplyDto
  }

}
