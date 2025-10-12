import axios from 'axios';

// Use same-origin Vite proxy
const API_BASE = '/api';
const CART_BASE = API_BASE + '/cart';

const client = axios.create({ baseURL: CART_BASE });
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

export default {
  async getCart() { return client.get(''); },
  async addToCart(listingId, quantity = 1) { return client.post('', { listingId, quantity }); },
  async removeItem(cartItemId) { return client.delete('/' + cartItemId); },
  async clearCart() { return client.post('/clear'); },
  async checkout() { return client.post('/checkout'); }
};
