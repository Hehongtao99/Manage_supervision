import axios from '../utils/axios'

export interface Order {
  id?: number
  orderNumber?: string
  playerId?: number
  playerName?: string
  companionId?: number
  companionName?: string
  serviceId?: number
  serviceTitle?: string
  gameType?: string
  hours?: number
  price?: number
  totalAmount?: number
  appointedTime?: string
  status?: string
  paymentStatus?: string
  paymentTime?: string
  createTime?: string
  updateTime?: string
  remark?: string
}

export interface CreateOrderRequest {
  serviceId: number
  hours: number
  appointedTime?: string
  remark?: string
}

// 创建订单
export const createOrder = async (orderData: CreateOrderRequest) => {
  try {
    const response = await axios.post('/api/orders', orderData)
    return response.data
  } catch (error) {
    console.error('创建订单失败:', error)
    throw error
  }
}

// 获取订单详情
export const getOrderById = async (orderId: number) => {
  try {
    const response = await axios.get(`/api/orders/${orderId}`)
    return response.data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    throw error
  }
}

// 获取玩家订单列表
export const getPlayerOrders = async (status?: string, page = 1, size = 10) => {
  try {
    const params: any = { page, size }
    if (status) {
      params.status = status
    }
    
    const response = await axios.get('/api/orders/my-player-orders', { params })
    return response.data
  } catch (error) {
    console.error('获取玩家订单列表失败:', error)
    throw error
  }
}

// 获取陪玩订单列表
export const getCompanionOrders = async (status?: string, page = 1, size = 10) => {
  try {
    const params: any = { page, size }
    if (status) {
      params.status = status
    }
    
    const response = await axios.get('/api/orders/my-companion-orders', { params })
    return response.data
  } catch (error) {
    console.error('获取陪玩订单列表失败:', error)
    throw error
  }
}

// 接受订单
export const acceptOrder = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/orders/${orderId}/accept`)
    return response.data
  } catch (error) {
    console.error('接受订单失败:', error)
    throw error
  }
}

// 完成订单
export const completeOrder = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/orders/${orderId}/complete`)
    return response.data
  } catch (error) {
    console.error('完成订单失败:', error)
    throw error
  }
}

// 取消订单
export const cancelOrder = async (orderId: number, reason?: string) => {
  try {
    const params: any = {}
    if (reason) {
      params.reason = reason
    }
    
    const response = await axios.post(`/api/orders/${orderId}/cancel`, null, { params })
    return response.data
  } catch (error) {
    console.error('取消订单失败:', error)
    throw error
  }
}

// 支付订单
export const payOrder = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/orders/${orderId}/pay`)
    return response.data
  } catch (error) {
    console.error('支付订单失败:', error)
    throw error
  }
}

// 申请退款
export const refundOrder = async (orderId: number, reason?: string) => {
  try {
    const params: any = {}
    if (reason) {
      params.reason = reason
    }
    
    const response = await axios.post(`/api/orders/${orderId}/refund`, null, { params })
    return response.data
  } catch (error) {
    console.error('申请退款失败:', error)
    throw error
  }
}

// 处理退款申请
export const handleRefundRequest = async (orderId: number, approved: boolean, response?: string) => {
  try {
    const params: any = { approved }
    if (response) {
      params.response = response
    }
    
    const res = await axios.post(`/api/orders/${orderId}/handle-refund`, null, { params })
    return res.data
  } catch (error) {
    console.error('处理退款申请失败:', error)
    throw error
  }
}

// 获取订单状态选项
export const getOrderStatusOptions = async () => {
  try {
    const response = await axios.get('/api/orders/status-options')
    return response.data
  } catch (error) {
    console.error('获取订单状态选项失败:', error)
    throw error
  }
} 