import axios from 'axios';

const BASE_URL = 'http://localhost:8080';
const USER_BASE = BASE_URL + '/api/user';

const client = axios.create({ baseURL: USER_BASE });

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default {
  getUser(id) { // Returns Promise<UserDto>
    return client.get(`/${id}`);
  },
  updateUser(id, payload) { // payload: { userName, email, password? }
    return client.put(`/${id}`, payload, { headers: { 'Content-Type': 'application/json' } });
  },
  deleteUser(id) { return client.delete(`/${id}`); },
  getByUsername(username) { return client.get(`/username/${encodeURIComponent(username)}`); },
  getByEmail(email) { return client.get(`/email/${encodeURIComponent(email)}`); }
};

