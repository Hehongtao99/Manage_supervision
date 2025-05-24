import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8999',
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    // 如果存在token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    // 处理401错误 - 未认证
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

// API服务对象
const apiService = {
  // 基本请求方法
  get(url, params = {}) {
    return api.get(url, { params });
  },
  
  post(url, data = {}) {
    return api.post(url, data);
  },
  
  put(url, data = {}) {
    return api.put(url, data);
  },
  
  delete(url) {
    return api.delete(url);
  },
  
  // 认证相关API
  auth: {
    login(credentials) {
      return api.post('/api/auth/login', credentials);
    },
    
    register(userData) {
      // 注册功能已禁用
      return Promise.reject(new Error('注册功能已关闭，请联系管理员'));
    },
    
    getCurrentUser() {
      return api.get('/api/auth/me');
    }
  },
  
  // 用户相关API
  user: {
    uploadAvatar(formData) {
      return api.post('/api/user/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
    },
    
    updateProfile(profileData) {
      return api.put('/api/user/profile', profileData);
    }
  },
  
  // 人脸识别相关API
  face: {
    detectFace(imageData) {
      return api.post('/api/face/detect', { image: imageData });
    },
    
    registerFace(faceData) {
      return api.post('/api/face/register', { faceData: faceData });
    },
    
    verifyFace(userId, faceData) {
      return api.post('/api/face/verify', { userId, faceData });
    },
    
    getFaceStatus() {
      return api.get('/api/face/status');
    }
  },
  
  // 考勤相关API
  attendance: {
    getList() {
      return api.get('/api/attendance/user/list');
    },
    
    getRecords() {
      return api.get('/api/attendance/user/records');
    },
    
    faceCheckIn(attendanceId, data) {
      return api.post(`/api/attendance/${attendanceId}/face-check-in`, data);
    }
  }
};

export default apiService; 