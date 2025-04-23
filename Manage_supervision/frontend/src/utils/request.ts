import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 获取存储的token
    const token = localStorage.getItem('token');
    // 如果token存在，添加到请求头
    if (token) {
      // 使用方括号形式设置请求头，确保兼容性
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log('request.ts: 设置Authorization头:', token.substring(0, 10) + '...');
    } else {
      console.log('request.ts: 未找到token，不设置Authorization头');
    }
    
    // 获取用户ID
    const userId = localStorage.getItem('userId');
    if (userId) {
      config.headers['userId'] = userId;
    }
    
    // 添加请求日志
    console.log(`请求URL: ${config.url}, 方法: ${config.method}`, config);
    
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('API响应成功:', response.config.url, response.status);
    return response;
  },
  error => {
    const status = error.response?.status;
    const url = error.config?.url || '未知';
    const method = error.config?.method || '未知';
    console.error(`API请求失败: [${method.toUpperCase()}] ${url}, 状态码: ${status}`, error);
    
    // 提取具体的错误信息
    let errorMessage = '请求失败，服务器异常';
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    // 统一处理401未授权的情况
    if (status === 401) {
      console.warn('未授权访问，请重新登录');
      // 这里可以添加重定向到登录页面或清除token的逻辑
    }
    
    return Promise.reject(error);
  }
);

// 处理token过期的函数
const handleTokenExpiration = () => {
  // 提示用户重新登录
  ElMessage.error('登录已过期，请重新登录');
  
  // 清除本地存储的token和用户信息
  localStorage.removeItem('token');
  
  // 跳转到登录页面
  window.location.href = '/login';
};

// 封装请求方法
const request = (config: any) => {
  return service(config);
};

export default request; 