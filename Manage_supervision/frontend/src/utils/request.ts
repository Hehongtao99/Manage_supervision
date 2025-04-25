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
  (response) => {
    // 在这里可以对响应数据做些什么
    console.log('API响应:', response.config.url, response.status);
    
    // 直接返回响应数据，不做嵌套处理
    return response.data;
  },
  (error) => {
    console.error('API请求错误:', error);
    
    // 错误处理
    if (error.response) {
      // 请求已发出，服务器返回状态码不在 2xx 范围内
      console.error('错误状态码:', error.response.status);
      console.error('错误数据:', error.response.data);
      
      // 处理401错误（未授权）
      if (error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录');
        // 清除token并跳转到登录页
        localStorage.removeItem('token');
        // 使用window.location跳转到登录页
        window.location.href = '/login';
      } else {
        // 显示服务器返回的错误信息
        ElMessage.error(
          error.response.data.message || '请求失败，请稍后重试'
        );
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      console.error('未收到响应:', error.request);
      ElMessage.error('网络连接异常，请检查网络');
    } else {
      // 请求配置发生错误
      console.error('请求错误:', error.message);
      ElMessage.error('请求配置错误：' + error.message);
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