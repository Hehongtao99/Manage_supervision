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

// 获取所有订单（可按状态筛选）
export const getAllOrders = async (page = 1, size = 10, status?: string) => {
  try {
    const params: any = { page, size }
    if (status) {
      params.status = status
    }
    
    const response = await axios.get('/api/admin/orders', { params })
    return response.data
  } catch (error) {
    console.error('获取所有订单失败:', error)
    throw error
  }
}

// 获取订单详情
export const getOrderDetail = async (orderId: number) => {
  try {
    const response = await axios.get(`/api/admin/orders/${orderId}`)
    return response.data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    throw error
  }
}

// 取消订单
export const cancelOrder = async (orderId: number, reason?: string) => {
  try {
    const data: any = {}
    if (reason) {
      data.reason = reason
    }
    
    const response = await axios.post(`/api/admin/orders/${orderId}/cancel`, data)
    return response.data
  } catch (error) {
    console.error('取消订单失败:', error)
    throw error
  }
}

// 完成订单
export const completeOrder = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/admin/orders/${orderId}/complete`)
    return response.data
  } catch (error) {
    console.error('完成订单失败:', error)
    throw error
  }
}

// 获取待审核的评价列表
export const getPendingReviews = async (page = 1, size = 10) => {
  try {
    const response = await axios.get('/api/admin/reviews/pending', {
      params: { page, size }
    })
    return response.data
  } catch (error) {
    console.error('获取待审核评价失败:', error)
    throw error
  }
}

// 获取评价列表（可按状态筛选）
export const getReviewsByStatus = async (status?: string, page = 1, size = 10) => {
  try {
    const params: any = { 
      page, 
      size,
      _t: new Date().getTime() // 添加时间戳避免缓存
    }
    if (status) {
      params.status = status
    }
    
    console.log('发送获取评价列表请求，参数:', params);
    const response = await axios.get('/api/admin/reviews', { params })
    console.log('获取评价列表响应:', response);
    
    return response.data
  } catch (error) {
    console.error('获取评价列表失败，详细错误:', error)
    throw error
  }
}

// 获取评价详情
export const getReviewDetail = async (reviewId: number) => {
  try {
    const response = await axios.get(`/api/admin/reviews/${reviewId}`)
    return response.data
  } catch (error) {
    console.error('获取评价详情失败:', error)
    throw error
  }
}

// 审核评价
export const reviewReview = async (
  reviewId: number, 
  approved: boolean, 
  reason?: string
) => {
  try {
    const data: any = { approved }
    if (!approved && reason) {
      data.reason = reason
    }
    
    const response = await axios.post(`/api/admin/reviews/${reviewId}/review`, data)
    return response.data
  } catch (error) {
    console.error('审核评价失败:', error)
    throw error
  }
}

// 审核评价通过
export const approveReview = async (reviewId: number) => {
  try {
    const data = { approved: true }
    const response = await axios.post(`/api/admin/reviews/${reviewId}/review`, data)
    return response.data
  } catch (error) {
    console.error('审核通过评价失败:', error)
    throw error
  }
}

// 审核评价拒绝
export const rejectReview = async (reviewId: number, reason: string) => {
  try {
    const data = { 
      approved: false,
      reason
    }
    const response = await axios.post(`/api/admin/reviews/${reviewId}/review`, data)
    return response.data
  } catch (error) {
    console.error('审核拒绝评价失败:', error)
    throw error
  }
} 