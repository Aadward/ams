import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// Request interceptor: attach JWT token from localStorage
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: redirect to login on 401
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('role');
      // Use hash routing for HashRouter
      window.location.hash = '#/login';
    }
    return Promise.reject(error);
  }
);

export default http;
