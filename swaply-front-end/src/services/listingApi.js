import axios from 'axios';

const BASE_URL = 'http://localhost:8080';
const LISTING_BASE = BASE_URL + '/api/listings';

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
  BASE_URL,
  getActiveListings() {
    return client.get('/active');
  },
  createListing(userId, payload) {
    return client.post(`/user/${userId}`, payload, { headers: { 'Content-Type': 'application/json', ...authHeader() } });
  },
  uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post(BASE_URL + '/api/files/upload', formData, {
      headers: { ...authHeader(), 'Accept': 'application/json' }
    });
  },
  getListingsByUser(userId) {
    return client.get(`/user/${userId}`);
  }
};
