import axios from 'axios';

const BASE_URL = 'http://localhost:8080';
const CART_BASE = BASE_URL + '/api/cart';

const client = axios.create({ baseURL: CART_BASE });
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

export default {
  BASE_URL,
  async getCart() { return client.get(''); },
  async addToCart(listingId, quantity = 1) { return client.post('', { listingId, quantity }); },
  async removeItem(cartItemId) { return client.delete('/' + cartItemId); },
  async clearCart() { return client.post('/clear'); },
  async checkout() { return client.post('/checkout'); }
};
