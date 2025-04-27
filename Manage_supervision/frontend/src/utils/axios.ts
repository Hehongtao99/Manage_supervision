import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

// 创建axios实例
const instance = axios.create({
  baseURL: '', // 根据环境配置
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 添加请求拦截器
instance.interceptors.request.use(
  config => {
    console.log('发送请求:', config.method?.toUpperCase(), config.url, config.params || config.data);
    
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 添加调试日志
      console.log('发送请求携带token:', token.substring(0, 10) + '...')
      config.headers.Authorization = `Bearer ${token}`
    } else {
      console.log('请求未携带token，用户可能未登录')
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 添加响应拦截器
instance.interceptors.response.use(
  response => {
    console.log('接收响应:', response.config.url, response.status, response.data);
    
    // 处理成功响应
    return response;
  },
  error => {
    console.error('响应错误:', error.config?.url, error.response?.status, error.response?.data);
    
    // 获取具体错误信息
    const errorMessage = error.response?.data?.message || '请求失败，请稍后重试'
    const statusCode = error.response?.status
    const requestUrl = error.config?.url || '未知URL'
    
    // 检查是否为课题或任务评价相关的404错误
    const isEvaluationRequest = requestUrl && (
      requestUrl.includes('/evaluation') || 
      requestUrl.includes('/evaluate')
    )
    
    // 根据状态码处理不同的错误情况
    switch (statusCode) {
      case 400:
        console.log('请求参数错误:', errorMessage)
        break
      case 401:
        console.log(`授权失败 (${requestUrl}):`, errorMessage)
        console.log('当前token:', localStorage.getItem('token'))
        
        // 忽略用户信息API和登录API的401错误，防止循环重定向
        if (!requestUrl.includes('/api/auth/info') && !requestUrl.includes('/api/auth/login')) {
          // 如果不是刷新或获取用户信息，重置登录状态
          const userStore = useUserStore()
          
          // 判断是否需要重定向到登录页
          if (!window.location.pathname.includes('/login')) {
            ElMessage.error('登录已过期，请重新登录')
            userStore.handleAuthError()
          }
        } else {
          console.log('忽略认证相关API的401错误，防止循环重定向')
        }
        break
      case 403:
        console.log(`权限不足 (${requestUrl}):`, errorMessage)
        ElMessage.error('权限不足，无法执行此操作')
        break
      case 404:
        console.log(`请求的资源不存在 (${requestUrl}):`, errorMessage)
        // 对于评价相关的404错误，不显示全局错误消息
        if (!isEvaluationRequest) {
          ElMessage.error('请求的资源不存在')
        } else {
          console.log('评价相关404错误，正常情况，不显示错误消息')
        }
        break
      case 500:
        console.log(`服务器错误 (${requestUrl}):`, errorMessage)
        ElMessage.error('服务器错误，请稍后重试')
        break
      default:
        if (error.message === 'Network Error') {
          console.log('网络连接错误，请检查网络连接或服务器状态')
          ElMessage.error('网络连接错误，请检查网络连接或服务器状态')
        } else {
          console.log(`未知错误 (${requestUrl}):`, error)
        }
    }

    if (error.response?.data) {
      error.data = error.response.data
    }
    
    return Promise.reject(error)
  }
)

export default instance