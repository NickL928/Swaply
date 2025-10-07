import axios from 'axios';

// Base configuration for auth endpoints
const authClient = axios.create({
  baseURL: 'http://localhost:8080/api/auth',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Attach token automatically if present
authClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default {
  login(payload) {
    // payload: { userName, password }
    return authClient.post('/login', payload);
  },
  register(payload) {
    // payload: { userName, email, password }
    return authClient.post('/register', payload);
  }
};

