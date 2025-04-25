import axios from '../utils/axios'
import { useUserStore } from '../stores/user'
import type { SupervisorDashboardDTO } from '@/types/dashboard'

export interface UserInfo {
  id: number
  username: string
  roles: string[]
  createTime: string
}

export interface SystemInfo {
  javaVersion: string
  osName: string
  osVersion: string
  totalMemory: number
  freeMemory: number
  availableProcessors: number
}

export interface DashboardStats {
  totalUsers: number
  roleDistribution: Record<string, number>
  systemInfo: SystemInfo
  userList: UserInfo[]
}

export const getDashboardStats = async (): Promise<DashboardStats> => {
  const userStore = useUserStore()
  
  // 根据用户角色选择合适的API端点
  let endpoint = '/stats'  // 默认使用普通用户端点
  
  if (userStore.isAdmin) {
    endpoint = '/admin/stats'  // 管理员使用管理员端点
  }
  
  // 添加调试日志
  console.log('调用仪表盘API，用户角色:', userStore.user.roles, '使用端点:', endpoint)
  
  const response = await axios.get<DashboardStats>(`/api/dashboard${endpoint}`)
  return response.data
}

/**
 * 获取管理员控制台统计数据
 */
export const getAdminDashboardStats = async () => {
  const response = await axios.get('/api/dashboard/admin/stats')
  return response.data
}

/**
 * 获取基础控制台统计数据
 */
export const getBasicDashboardStats = async () => {
  const response = await axios.get('/api/dashboard/stats')
  return response.data
}

/**
 * 获取教师/督导员控制台统计数据
 */
export const getSupervisorDashboardStats = async (): Promise<SupervisorDashboardDTO> => {
  const response = await axios.get('/api/dashboard/supervisor/stats')
  return response.data
} 