import axios from '../utils/axios'

// 获取陪玩列表（分页）
export const getCompanions = async (page = 1, size = 10, keyword?: string) => {
  try {
    const params: any = { page, size }
    if (keyword) {
      params.keyword = keyword
    }
    
    const response = await axios.get('/api/admin/companions', { params })
    return response.data
  } catch (error) {
    console.error('获取陪玩列表失败:', error)
    throw error
  }
}

// 获取陪玩详情（包含玩家列表）
export const getCompanionDetails = async (companionId: number) => {
  try {
    const response = await axios.get(`/api/admin/companions/${companionId}/details`)
    return response.data
  } catch (error) {
    console.error('获取陪玩详情失败:', error)
    throw error
  }
}

// 获取陪玩的玩家列表
export const getCompanionPlayers = async (companionId: number) => {
  try {
    const response = await axios.get(`/api/admin/companions/${companionId}/players`)
    return response.data
  } catch (error) {
    console.error('获取陪玩玩家列表失败:', error)
    throw error
  }
}

// 分配玩家给陪玩
export const assignPlayersToCompanion = async (companionId: number, playerIds: number[]) => {
  try {
    const response = await axios.post('/api/admin/companions/assign-players', {
      companionId,
      playerIds
    })
    return response.data
  } catch (error) {
    console.error('分配玩家失败:', error)
    throw error
  }
}

// 取消分配玩家
export const unassignPlayer = async (companionId: number, playerId: number) => {
  try {
    const response = await axios.post(`/api/admin/companions/${companionId}/unassign/${playerId}`)
    return response.data
  } catch (error) {
    console.error('取消分配玩家失败:', error)
    throw error
  }
}

// 陪玩服务接口
export interface CompanionService {
  id?: number
  companionId?: number
  title: string
  description: string
  gameTypes: string
  price: number
  serviceStartTime: string | null
  serviceEndTime: string | null
  availability: string | null
  status?: string
  createdTime?: string
  updatedTime?: string
  companionName?: string
  companionAvatar?: string
  rating?: number  // 评分
}

// 创建陪玩服务
export const createCompanionService = async (service: CompanionService) => {
  try {
    const response = await axios.post('/api/companion/services', service)
    return response.data
  } catch (error) {
    console.error('创建陪玩服务失败:', error)
    throw error
  }
}

// 更新陪玩服务
export const updateCompanionService = async (serviceId: number, service: CompanionService) => {
  try {
    const response = await axios.put(`/api/companion/services/${serviceId}`, service)
    return response.data
  } catch (error) {
    console.error('更新陪玩服务失败:', error)
    throw error
  }
}

// 获取陪玩的所有服务
export const getMyCompanionServices = async () => {
  try {
    const response = await axios.get('/api/companion/services/my-services')
    return response.data
  } catch (error) {
    console.error('获取陪玩服务列表失败:', error)
    throw error
  }
}

// 获取服务详情
export const getCompanionServiceById = async (serviceId: number) => {
  try {
    const response = await axios.get(`/api/companion/services/${serviceId}`)
    return response.data
  } catch (error) {
    console.error('获取陪玩服务详情失败:', error)
    throw error
  }
}

// 获取所有激活的服务（分页）
export const getActiveCompanionServices = async (page = 1, size = 10) => {
  try {
    const response = await axios.get('/api/companion/services', {
      params: { page, size }
    })
    return response.data
  } catch (error) {
    console.error('获取激活的陪玩服务列表失败:', error)
    throw error
  }
}

// 获取公开的所有激活服务（分页）
export const getPublicCompanionServices = async (page = 1, size = 10) => {
  try {
    const response = await axios.get('/api/companion/services/public', {
      params: { page, size }
    })
    return response.data
  } catch (error) {
    console.error('获取公开陪玩服务列表失败:', error)
    throw error
  }
}

// 更新服务状态
export const updateCompanionServiceStatus = async (serviceId: number, status: string) => {
  try {
    const response = await axios.put(`/api/companion/services/${serviceId}/status?status=${status}`)
    return response.data
  } catch (error) {
    console.error('更新陪玩服务状态失败:', error)
    throw error
  }
}

// 删除服务
export const deleteCompanionService = async (serviceId: number) => {
  try {
    const response = await axios.delete(`/api/companion/services/${serviceId}`)
    return response.data
  } catch (error) {
    console.error('删除陪玩服务失败:', error)
    throw error
  }
}

// 获取陪玩的平均评分
export const getCompanionRating = async (companionId: number) => {
  try {
    const response = await axios.get(`/api/reviews/rating/${companionId}`)
    return response.data
  } catch (error) {
    console.error('获取陪玩平均评分失败:', error)
    throw error
  }
}

// 获取陪玩的评价列表
export const getCompanionReviews = async (companionId: number, page = 1, size = 3) => {
  try {
    const params = { page, size }
    const response = await axios.get(`/api/reviews/companion/${companionId}`, { params })
    return response.data
  } catch (error) {
    console.error('获取陪玩评价列表失败:', error)
    throw error
  }
} 