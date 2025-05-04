import axios from '../utils/axios'

export interface Review {
  id?: number
  orderId?: number
  orderNumber?: string
  reviewerId?: number
  reviewerName?: string
  reviewerAvatar?: string
  companionId?: number
  companionName?: string
  serviceId?: number
  serviceTitle?: string
  gameType?: string
  rating?: number
  content?: string
  anonymous?: boolean
  replied?: boolean
  reply?: string
  replyTime?: string
  createTime?: string
  updateTime?: string
}

export interface CreateReviewRequest {
  orderId: number
  rating: number
  content?: string
  anonymous?: boolean
}

export interface ReplyReviewRequest {
  reviewId: number
  reply: string
}

// 添加评分响应接口定义
export interface RatingResponse {
  rating: number;
  companionId?: number;
  totalReviews?: number;
}

// 创建评价
export const createReview = async (data: CreateReviewRequest) => {
  try {
    const response = await axios.post('/api/reviews', data)
    return response.data
  } catch (error) {
    console.error('创建评价失败:', error)
    throw error
  }
}

// 回复评价
export const replyReview = async (data: ReplyReviewRequest) => {
  try {
    const response = await axios.post('/api/reviews/reply', data)
    return response.data
  } catch (error) {
    console.error('回复评价失败:', error)
    throw error
  }
}

// 获取评价详情
export const getReviewById = async (reviewId: number) => {
  try {
    const response = await axios.get(`/api/reviews/${reviewId}`)
    return response.data
  } catch (error) {
    console.error('获取评价详情失败:', error)
    throw error
  }
}

// 获取陪玩的评价列表
export const getCompanionReviews = async (companionId: number, page = 1, size = 10) => {
  console.log(`开始调用API获取陪玩(${companionId})的评价列表, page=${page}, size=${size}`)
  try {
    const params = { page, size }
    const response = await axios.get(`/api/reviews/companion/${companionId}`, { params })
    console.log(`获取陪玩评价成功:`, response.data)
    
    // 标准化响应数据，确保返回的数据格式一致
    if (!response.data.records) {
      console.warn('API响应缺少records字段，返回空数组')
      return { records: [], total: 0 }
    }
    
    return response.data
  } catch (error) {
    console.error('获取陪玩评价列表失败:', error)
    // 返回空数据而不是抛出错误，避免UI卡住
    return { records: [], total: 0 }
  }
}

// 获取我的评价列表
export const getMyReviews = async (page = 1, size = 10) => {
  try {
    const params = { page, size }
    const response = await axios.get('/api/reviews/my-reviews', { params })
    return response.data
  } catch (error) {
    console.error('获取我的评价列表失败:', error)
    throw error
  }
}

// 获取陪玩的平均评分
export const getCompanionRating = async (companionId: number): Promise<RatingResponse> => {
  try {
    const response = await axios.get(`/api/reviews/rating/${companionId}`)
    // 规范化返回数据格式
    if (typeof response.data === 'object' && response.data !== null) {
      return response.data;
    } else {
      // 如果后端直接返回数值，转为标准格式
      return { rating: parseFloat(response.data) || 0, companionId };
    }
  } catch (error) {
    console.error('获取陪玩平均评分失败:', error)
    return { rating: 0, companionId };
  }
}

// 检查订单是否可以评价
export const canReviewOrder = async (orderId: number) => {
  try {
    const response = await axios.get(`/api/reviews/can-review/${orderId}`)
    return response.data
  } catch (error) {
    console.error('检查订单是否可以评价失败:', error)
    throw error
  }
} 