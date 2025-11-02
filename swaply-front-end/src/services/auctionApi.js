import axios from 'axios'

const API_BASE = '/api'
const AUCTION_BASE = API_BASE + '/auctions'

const client = axios.create({ baseURL: AUCTION_BASE })

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

function authHeader() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export default {
  API_BASE,
  getActive() { return client.get('/active') },
  getAuction(id) { return client.get('/' + id) },
  createAuction(userId, payload) { return client.post(`/user/${userId}`, payload, { headers: { 'Content-Type': 'application/json', ...authHeader() } }) },
  placeBid(auctionId, userId, amount) { return client.post(`/${auctionId}/bid/user/${userId}`, { amount }, { headers: { 'Content-Type': 'application/json', ...authHeader() } }) }
}

