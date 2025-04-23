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
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // 获取用户ID
    const userId = localStorage.getItem('userId');
    if (userId) {
      config.headers['userId'] = userId;
    }
    
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
    return response;
  },
  error => {
    console.error('响应错误:', error);
    
    // 处理网络错误或服务器错误
    if (error.response) {
      // 服务器响应错误
      const status = error.response.status;
      let message = '服务器错误';
      
      switch (status) {
        case 400:
          message = '请求参数错误';
          break;
        case 401:
          message = '未授权，请重新登录';
          handleTokenExpiration();
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = `请求错误(${status})`;
      }
      
      ElMessage.error(error.response.data?.message || message);
    } else if (error.request) {
      // 请求已发送但未收到响应
      ElMessage.error('服务器未响应，请检查网络连接');
    } else {
      // 请求设置有问题
      ElMessage.error('请求配置错误');
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