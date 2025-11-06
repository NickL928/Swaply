import axios from 'axios'

const client = axios.create({ baseURL: '/api/orders' })
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

export default {
  async myBuyerOrders() { return (await client.get('/buyer')).data },
  async mySellerOrders() { return (await client.get('/seller')).data },
  async get(orderId) { return (await client.get('/' + orderId)).data },
  async update(orderId, payload) {
    try {
      return (await client.patch('/' + orderId, payload)).data
    } catch (e) {
      return (await client.put('/' + orderId, payload)).data
    }
  },
  async remove(orderId) { await client.delete('/' + orderId) }
}
