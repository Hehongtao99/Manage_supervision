import axios from '../utils/axios'
import { AxiosError } from 'axios'

// 课程DTO接口
export interface CourseDTO {
  id: number
  teacherId: number
  teacherName: string
  title: string
  subject: string
  hourlyPrice: number
  description: string
  imagePaths: string[]
  workTimeStart: number
  workTimeEnd: number
  status: string
  createTime: string
  updateTime: string
}

// 添加分页相关接口
export interface PageParams {
  page: number;
  size: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 学生获取所有已审核通过的课程（支持分页）
 */
export const getApprovedCoursesPaged = async (params: PageParams) => {
  try {
    const response = await axios.get('/api/student/courses/approved/page', { params });
    return response.data;
  } catch (error) {
    console.error('获取已审核通过的课程列表失败:', error);
    throw error;
  }
}

/**
 * 学生获取单个课程详情
 */
export const getCourseDetail = async (courseId: number) => {
  try {
    const response = await axios.get(`/api/student/courses/${courseId}`)
    return response.data
  } catch (error) {
    console.error('获取课程详情失败:', error)
    throw error
  }
}

/**
 * 开始与教师的聊天
 */
export const startChatWithTeacher = async (teacherId: number) => {
  try {
    console.log('API调用: 开始与教师聊天，教师ID:', teacherId)
    
    // 首先检查teacherId是否有效
    if (!teacherId || teacherId <= 0) {
      throw new Error('无效的教师ID')
    }
    
    // 发起API请求创建/获取会话
    const response = await axios.post('/api/chat/conversations', {
      recipientId: teacherId
    })
    
    console.log('聊天会话API原始返回:', response)
    
    // 处理响应数据，有些API会直接返回数据，有些会封装在data字段
    const conversationData = response.data
    console.log('聊天会话API处理后数据:', conversationData)
    
    // 检查返回的数据是否有效 - 会话对象必须有id
    if (!conversationData || !conversationData.id) {
      console.error('API返回的会话数据无效:', conversationData)
      throw new Error('服务器返回的会话数据无效')
    }
    
    return conversationData
  } catch (error: any) {
    console.error('开始聊天失败:', error)
    if (error instanceof AxiosError && error.response) {
      console.error('API错误状态码:', error.response.status)
      console.error('API错误详情:', error.response.data)
      
      // 根据不同的错误状态码给出不同的错误消息
      if (error.response.status === 401) {
        throw new Error('未登录或会话已过期，请重新登录')
      } else if (error.response.status === 404) {
        throw new Error('找不到指定教师')
      } else if (error.response.status === 500) {
        throw new Error('服务器内部错误，请稍后再试')
      }
    }
    throw error
  }
}

/**
 * 开始与学生的聊天
 */
export const startChatWithStudent = async (studentId: number) => {
  try {
    console.log('API调用: 开始与学生聊天，学生ID:', studentId)
    
    // 首先检查studentId是否有效
    if (!studentId || studentId <= 0) {
      throw new Error('无效的学生ID')
    }
    
    // 发起API请求创建/获取会话
    const response = await axios.post('/api/chat/conversations', {
      recipientId: studentId
    })
    
    console.log('聊天会话API原始返回:', response)
    
    // 处理响应数据，有些API会直接返回数据，有些会封装在data字段
    const conversationData = response.data
    console.log('聊天会话API处理后数据:', conversationData)
    
    // 检查返回的数据是否有效 - 会话对象必须有id
    if (!conversationData || !conversationData.id) {
      console.error('API返回的会话数据无效:', conversationData)
      throw new Error('服务器返回的会话数据无效')
    }
    
    return conversationData
  } catch (error: any) {
    console.error('开始聊天失败:', error)
    if (error instanceof AxiosError && error.response) {
      console.error('API错误状态码:', error.response.status)
      console.error('API错误详情:', error.response.data)
      
      // 根据不同的错误状态码给出不同的错误消息
      if (error.response.status === 401) {
        throw new Error('未登录或会话已过期，请重新登录')
      } else if (error.response.status === 404) {
        throw new Error('找不到指定学生')
      } else if (error.response.status === 500) {
        throw new Error('服务器内部错误，请稍后再试')
      }
    }
    throw error
  }
} 