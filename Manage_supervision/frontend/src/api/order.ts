import axios from '../utils/axios'

// 订单数据接口
export interface OrderDTO {
  id: number
  studentId: number
  teacherId: number
  courseId: number
  orderNumber: string
  price: number
  hours: number
  totalAmount: number
  status: string // PENDING, REFUND_PENDING, REFUND_REJECTED, CANCELED, APPEALING, APPEAL_APPROVED, APPEAL_REJECTED
  message: string
  refundReason: string
  rejectReason: string
  appealReason: string
  teacherResponse: string
  adminDecision: string
  createTime: string
  updateTime: string
  appealTime: string
  
  // 关联信息
  studentName: string
  teacherName: string
  courseTitle: string
  courseSubject: string
  statusText?: string
}

// 订单状态中文映射
export const orderStatusMap = {
  'PENDING': '已支付',
  'ACCEPTED': '已接受',
  'REJECTED': '已拒绝',
  'REFUND_PENDING': '退款申请中',
  'REFUND_REJECTED': '退款已拒绝',
  'CANCELED': '已取消',
  'COMPLETED': '已完成',
  'APPEALING': '申诉中',
  'APPEAL_APPROVED': '申诉通过',
  'APPEAL_REJECTED': '申诉驳回'
}

/**
 * 学生创建订单
 */
export const createOrder = async (
  courseId: number, 
  hours: number, 
  price: number, 
  message?: string
) => {
  try {
    console.log('创建订单参数:', { courseId, hours, price, message });
    
    // 确保courseId是有效的
    if (!courseId || isNaN(Number(courseId))) {
      throw new Error('课程ID无效');
    }
    
    // 使用JSON格式发送数据到专用的JSON端点
    const response = await axios.post('/api/student/orders/json', {
      courseId: courseId,
      hours: hours,
      price: price,
      message: message || ''
    });
    
    return response.data;
  } catch (error) {
    console.error('创建订单失败:', error);
    throw error;
  }
}

/**
 * 学生获取订单列表
 */
export const getStudentOrders = async () => {
  try {
    const response = await axios.get('/api/student/orders')
    return response.data
  } catch (error) {
    console.error('获取学生订单列表失败:', error)
    throw error
  }
}

/**
 * 学生获取订单详情
 */
export const getStudentOrderDetail = async (orderId: number) => {
  try {
    const response = await axios.get(`/api/student/orders/${orderId}`)
    return response.data
  } catch (error) {
    console.error('获取学生订单详情失败:', error)
    throw error
  }
}

/**
 * 学生申请退款
 */
export const cancelOrder = async (orderId: number, refundReason: string) => {
  try {
    // 使用/refund接口发送退款理由
    const response = await axios.post(`/api/student/orders/${orderId}/refund`, {
      refundReason: refundReason
    })
    return response.data
  } catch (error) {
    console.error('申请退款失败:', error)
    throw error
  }
}

/**
 * 学生申诉退款
 */
export const appealRefund = async (orderId: number, appealReason: string) => {
  try {
    const response = await axios.post(`/api/student/orders/${orderId}/appeal`, {
      appealReason: appealReason
    })
    return response.data
  } catch (error) {
    console.error('申诉退款失败:', error)
    throw error
  }
}

/**
 * 教师获取订单列表
 */
export const getTeacherOrders = async () => {
  try {
    const response = await axios.get('/api/supervisor/orders')
    return response.data
  } catch (error) {
    console.error('获取教师订单列表失败:', error)
    throw error
  }
}

/**
 * 教师获取订单详情
 */
export const getTeacherOrderDetail = async (orderId: number) => {
  try {
    const response = await axios.get(`/api/supervisor/orders/${orderId}`)
    return response.data
  } catch (error) {
    console.error('获取教师订单详情失败:', error)
    throw error
  }
}

/**
 * 教师接受订单
 */
export const acceptOrder = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/supervisor/orders/${orderId}/accept`)
    return response.data
  } catch (error) {
    console.error('接受订单失败:', error)
    throw error
  }
}

/**
 * 教师拒绝订单
 */
export const rejectOrder = async (orderId: number, rejectReason: string) => {
  try {
    const formData = new FormData()
    formData.append('rejectReason', rejectReason)
    
    const response = await axios.post(`/api/supervisor/orders/${orderId}/reject`, formData)
    return response.data
  } catch (error) {
    console.error('拒绝订单失败:', error)
    throw error
  }
}

/**
 * 教师完成订单
 */
export const completeOrder = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/supervisor/orders/${orderId}/complete`)
    return response.data
  } catch (error) {
    console.error('完成订单失败:', error)
    throw error
  }
}

/**
 * 教师处理退款申请 - 同意
 */
export const approveRefund = async (orderId: number) => {
  try {
    const response = await axios.post(`/api/supervisor/orders/${orderId}/approve-refund`)
    return response.data
  } catch (error) {
    console.error('同意退款失败:', error)
    throw error
  }
}

/**
 * 教师处理退款申请 - 拒绝
 */
export const rejectRefund = async (orderId: number, rejectReason: string) => {
  try {
    // 使用JSON格式发送请求
    const response = await axios.post(`/api/supervisor/orders/${orderId}/reject-refund`, {
      rejectReason: rejectReason
    })
    return response.data
  } catch (error) {
    console.error('拒绝退款失败:', error)
    throw error
  }
}

/**
 * 教师回复申诉
 */
export const respondToAppeal = async (orderId: number, teacherResponse: string) => {
  try {
    const response = await axios.post(`/api/supervisor/orders/${orderId}/respond-appeal`, {
      teacherResponse: teacherResponse
    })
    return response.data
  } catch (error) {
    console.error('回复申诉失败:', error)
    throw error
  }
}

/**
 * 管理员获取申诉订单列表
 */
export const getAppealingOrders = async () => {
  try {
    const response = await axios.get('/api/admin/orders/appeals')
    return response.data
  } catch (error) {
    console.error('获取申诉订单列表失败:', error)
    throw error
  }
}

/**
 * 管理员获取申诉订单列表（分页）
 */
export const getAppealingOrdersPaged = async (page: number, size: number) => {
  try {
    const response = await axios.get('/api/admin/orders/appeals/page', {
      params: { page, size }
    })
    return response.data
  } catch (error) {
    console.error('获取申诉订单列表失败:', error)
    throw error
  }
}

/**
 * 管理员获取已处理的申诉订单列表（分页）
 */
export const getProcessedAppealsPaged = async (page: number, size: number) => {
  try {
    const response = await axios.get('/api/admin/orders/appeals/processed/page', {
      params: { page, size }
    })
    return response.data
  } catch (error) {
    console.error('获取已处理申诉记录失败:', error)
    throw error
  }
}

/**
 * 管理员批准申诉
 */
export const approveAppeal = async (orderId: number, adminDecision?: string) => {
  try {
    const response = await axios.post(`/api/admin/orders/${orderId}/approve-appeal`, {
      adminDecision: adminDecision || '管理员同意退款'
    })
    return response.data
  } catch (error) {
    console.error('批准申诉失败:', error)
    throw error
  }
}

/**
 * 管理员拒绝申诉
 */
export const rejectAppeal = async (orderId: number, adminDecision: string) => {
  try {
    const response = await axios.post(`/api/admin/orders/${orderId}/reject-appeal`, {
      adminDecision: adminDecision
    })
    return response.data
  } catch (error) {
    console.error('拒绝申诉失败:', error)
    throw error
  }
} 