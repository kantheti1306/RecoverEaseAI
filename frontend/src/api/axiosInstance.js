import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const axiosInstance = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT automatically
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('recoverease_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Handle 401
axiosInstance.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('recoverease_token');
      localStorage.removeItem('recoverease_user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default axiosInstance;
