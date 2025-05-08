import axios from '../utils/axios'
import { useUserStore } from '../stores/user'

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

export interface UserCreationTrend {
  dates: string[]
  counts: number[]
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
 * 获取近7天用户创建趋势数据
 * @returns 用户创建趋势数据，包含日期和对应的创建用户数量
 */
export const getUserCreationTrend = async (): Promise<UserCreationTrend> => {
  const response = await axios.get<UserCreationTrend>('/api/dashboard/admin/user-creation-trend')
  return response.data
} 