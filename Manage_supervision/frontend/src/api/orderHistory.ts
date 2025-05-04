import axios from '../utils/axios'

/**
 * 订单历史记录
 */
export interface OrderStatusHistory {
  id?: number
  orderId?: number
  status?: string
  operatorId?: number
  operatorType?: string  // PLAYER, COMPANION, ADMIN, SYSTEM
  createTime?: string
  remark?: string
  // 拓展字段
  operatorName?: string
}

/**
 * 获取订单状态历史记录
 */
export const getOrderHistory = async (orderId: number) => {
  try {
    // 使用带有授权机制的axios实例
    const response = await axios.get(`/api/order-history/${orderId}`)
    return response.data
  } catch (error) {
    console.error('获取订单历史记录失败:', error)
    throw error
  }
} 