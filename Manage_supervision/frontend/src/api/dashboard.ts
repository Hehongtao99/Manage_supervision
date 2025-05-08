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

// 管理控制台数据接口
export interface DashboardStats {
  totalUsers: number
  activeUsers?: number
  totalRoles?: number
  roleDistribution?: Record<string, number>
  systemInfo?: {
    cpuUsage?: number
    memoryUsage?: number
    diskUsage?: number
    status: string
  }
  userInfo?: Array<{
    id: number
    username: string
    role: string
    lastLoginTime?: string
  }>
  userList?: Array<{
    id: number
    username: string
    createTime: string
    roles?: string[]
  }>
}

// 获取仪表盘统计数据
export async function getDashboardStats(): Promise<DashboardStats> {
  try {
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
  } catch (error) {
    console.error('获取管理控制台统计数据失败:', error)
    // 返回默认数据，确保前端不会因API失败而崩溃
    
    // 生成一些模拟用户数据作为备用
    const mockUserList = []
    const now = new Date()
    
    // 生成90天的用户数据，每天1-3个新用户
    for (let i = 90; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      
      // 每天添加1-3个用户
      const usersToAdd = Math.floor(Math.random() * 3) + 1
      
      for (let j = 0; j < usersToAdd; j++) {
        mockUserList.push({
          id: mockUserList.length + 1,
          username: `user_${mockUserList.length + 1}`,
          createTime: date.toISOString(),
          roles: ['USER']
        })
      }
    }
    
    return {
      totalUsers: 100,
      activeUsers: 70,
      roleDistribution: {
        'ADMIN': 3,
        'SUPERVISOR': 15,
        'USER': 82
      },
      systemInfo: {
        cpuUsage: 25,
        memoryUsage: 40,
        diskUsage: 35,
        status: '正常'
      },
      userList: mockUserList
    }
  }
}

// 获取用户列表数据用于生成增长趋势
export async function getUserList() {
  try {
    const response = await axios.get('/api/users/list')
    
    if (!response.data || !Array.isArray(response.data)) {
      throw new Error('获取用户列表失败: 返回数据格式错误')
    }
    
    return response.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
    throw error // 抛出错误，不再提供模拟数据
  }
}

// 获取用户增长趋势数据
export async function getUserGrowthTrend(days = 30) {
  try {
    const response = await axios.get('/api/dashboard/user-trend', {
      params: { days }
    })
    return response.data
  } catch (error) {
    console.error('获取用户增长趋势失败:', error)
    
    // 返回默认数据
    const dates = []
    const counts = []
    
    // 生成最近30天的数据
    const now = new Date()
    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      dates.push(`${date.getMonth() + 1}-${date.getDate()}`)
      
      // 生成一些随机增长的数据
      counts.push(100 + Math.floor(Math.random() * 5) + (days - i) * 3)
    }
    
    return {
      dates,
      counts
    }
  }
} 