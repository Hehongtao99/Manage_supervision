import axios from '../utils/axios'

// 获取教师列表（分页）
export const getTeachers = async (page = 1, size = 10, keyword?: string) => {
  try {
    const params: any = { page, size }
    if (keyword) {
      params.keyword = keyword
    }
    
    const response = await axios.get('/api/admin/teachers', { params })
    return response.data
  } catch (error) {
    console.error('获取教师列表失败:', error)
    throw error
  }
}

// 获取教师详情（包含学生列表）
export const getTeacherDetails = async (teacherId: number) => {
  try {
    const response = await axios.get(`/api/admin/teachers/${teacherId}/details`)
    return response.data
  } catch (error) {
    console.error('获取教师详情失败:', error)
    throw error
  }
}

// 获取教师的学生列表
export const getTeacherStudents = async (teacherId: number) => {
  try {
    const response = await axios.get(`/api/admin/teachers/${teacherId}/students`)
    return response.data
  } catch (error) {
    console.error('获取教师学生列表失败:', error)
    throw error
  }
}

// 分配学生给教师
export const assignStudentsToTeacher = async (teacherId: number, studentIds: number[]) => {
  try {
    const response = await axios.post('/api/admin/teachers/assign-students', {
      teacherId,
      studentIds
    })
    return response.data
  } catch (error) {
    console.error('分配学生失败:', error)
    throw error
  }
}

// 取消分配学生
export const unassignStudent = async (teacherId: number, studentId: number) => {
  try {
    const response = await axios.post(`/api/admin/teachers/${teacherId}/unassign/${studentId}`)
    return response.data
  } catch (error) {
    console.error('取消分配学生失败:', error)
    throw error
  }
} 