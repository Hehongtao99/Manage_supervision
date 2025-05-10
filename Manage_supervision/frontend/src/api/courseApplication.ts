import axios from '../utils/axios'

// 课程申请DTO接口
export interface CourseApplicationDTO {
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
  rejectionReason: string
  createTime: string
  updateTime: string
}

/**
 * 教师提交课程申请
 */
export const createCourseApplication = async (
  title: string,
  subject: string,
  hourlyPrice: string,
  description: string,
  workTimeStart: number,
  workTimeEnd: number,
  images: File[]
) => {
  const formData = new FormData()
  formData.append('title', title)
  formData.append('subject', subject)
  formData.append('hourlyPrice', hourlyPrice)
  formData.append('description', description)
  formData.append('workTimeStart', workTimeStart.toString())
  formData.append('workTimeEnd', workTimeEnd.toString())
  
  if (images && images.length > 0) {
    images.forEach(image => {
      formData.append('images', image)
    })
  }
  
  try {
    const response = await axios.post('/api/supervisor/course-applications', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  } catch (error) {
    console.error('提交课程申请失败:', error)
    throw error
  }
}

/**
 * 教师重新提交被拒绝的课程申请
 */
export const resubmitCourseApplication = async (
  applicationId: number,
  title: string,
  subject: string,
  hourlyPrice: string,
  description: string,
  workTimeStart: number,
  workTimeEnd: number,
  images: File[]
) => {
  const formData = new FormData()
  formData.append('title', title)
  formData.append('subject', subject)
  formData.append('hourlyPrice', hourlyPrice)
  formData.append('description', description)
  formData.append('workTimeStart', workTimeStart.toString())
  formData.append('workTimeEnd', workTimeEnd.toString())
  
  if (images && images.length > 0) {
    images.forEach(image => {
      formData.append('images', image)
    })
  }
  
  try {
    const response = await axios.put(`/api/supervisor/course-applications/${applicationId}/resubmit`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  } catch (error) {
    console.error('重新提交课程申请失败:', error)
    throw error
  }
}

/**
 * 教师获取自己的课程申请列表
 */
export const getTeacherCourseApplications = async () => {
  try {
    const response = await axios.get('/api/supervisor/course-applications')
    return response.data
  } catch (error) {
    console.error('获取课程申请列表失败:', error)
    throw error
  }
}

/**
 * 教师获取单个课程申请详情
 */
export const getTeacherCourseApplication = async (applicationId: number) => {
  try {
    const response = await axios.get(`/api/supervisor/course-applications/${applicationId}`)
    return response.data
  } catch (error) {
    console.error('获取课程申请详情失败:', error)
    throw error
  }
}

/**
 * 管理员获取所有课程申请列表
 */
export const getAllCourseApplications = async () => {
  try {
    const response = await axios.get('/api/admin/course-applications')
    return response.data
  } catch (error) {
    console.error('获取所有课程申请列表失败:', error)
    throw error
  }
}

/**
 * 管理员获取单个课程申请详情
 */
export const getAdminCourseApplication = async (applicationId: number) => {
  try {
    const response = await axios.get(`/api/admin/course-applications/${applicationId}`)
    return response.data
  } catch (error) {
    console.error('获取课程申请详情失败:', error)
    throw error
  }
}

/**
 * 管理员审核课程申请
 */
export const reviewCourseApplication = async (
  applicationId: number,
  approved: boolean,
  rejectionReason?: string
) => {
  try {
    let url = `/api/admin/course-applications/${applicationId}/review?approved=${approved}`
    
    if (rejectionReason) {
      url += `&rejectionReason=${encodeURIComponent(rejectionReason)}`
    }
    
    const response = await axios.post(url)
    return response.data
  } catch (error) {
    console.error('审核课程申请失败:', error)
    throw error
  }
} 