import axios from '../utils/axios'

// 获取所有陪玩服务（可按状态筛选）
export const getAllCompanionServices = async (page = 1, size = 10, status?: string) => {
  try {
    const params: any = { page, size }
    if (status) {
      params.status = status
    }
    
    const response = await axios.get('/api/admin/companion-services', { params })
    return response.data
  } catch (error) {
    console.error('获取所有陪玩服务失败:', error)
    throw error
  }
}

// 获取待审核的陪玩服务
export const getPendingCompanionServices = async (page = 1, size = 10) => {
  try {
    const response = await axios.get('/api/admin/companion-services/pending', {
      params: { page, size }
    })
    return response.data
  } catch (error) {
    console.error('获取待审核陪玩服务失败:', error)
    throw error
  }
}

// 获取陪玩服务详情
export const getAdminCompanionServiceDetail = async (serviceId: number) => {
  try {
    const response = await axios.get(`/api/admin/companion-services/${serviceId}`)
    return response.data
  } catch (error) {
    console.error('获取陪玩服务详情失败:', error)
    throw error
  }
}

// 审核陪玩服务
export const reviewCompanionService = async (
  serviceId: number, 
  approved: boolean, 
  reason?: string
) => {
  try {
    const data: any = { approved }
    if (!approved && reason) {
      data.reason = reason
    }
    
    const response = await axios.post(`/api/admin/companion-services/${serviceId}/review`, data)
    return response.data
  } catch (error) {
    console.error('审核陪玩服务失败:', error)
    throw error
  }
}

// 获取管理员控制台统计数据
export const getAdminStatistics = async () => {
  try {
    const response = await axios.get('/api/admin/statistics')
    return response.data
  } catch (error) {
    console.error('获取管理员统计数据失败:', error)
    throw error
  }
}

// 获取角色分布统计
export const getRoleDistribution = async () => {
  try {
    const response = await axios.get('/api/admin/statistics/role-distribution')
    return response.data
  } catch (error) {
    console.error('获取角色分布统计失败:', error)
    throw error
  }
}

// 获取用户活跃度统计
export const getUserActivity = async () => {
  try {
    const response = await axios.get('/api/admin/statistics/user-activity')
    return response.data
  } catch (error) {
    console.error('获取用户活跃度统计失败:', error)
    throw error
  }
} 