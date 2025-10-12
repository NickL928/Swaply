import axios from 'axios';

// Use Vite dev proxy: all API calls go through /api on the same origin
const API_BASE = '/api';
const LISTING_BASE = API_BASE + '/listings';

const client = axios.create({
  baseURL: LISTING_BASE
  // Do not set a fixed Content-Type so multipart can work when needed
});

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

function authHeader() {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export default {
  API_BASE,
  getActiveListings() {
    return client.get('/active');
  },
  getListing(id) {
    return client.get('/' + id);
  },
  createListing(userId, payload) {
    return client.post(`/user/${userId}`, payload, { headers: { 'Content-Type': 'application/json', ...authHeader() } });
  },
  uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    // Post to same-origin /api; Vite proxies to backend to avoid CORS
    return axios.post(API_BASE + '/files/upload', formData, {
      headers: { ...authHeader() }
    });
  },
  getListingsByUser(userId) {
    return client.get(`/user/${userId}`);
  }
};
