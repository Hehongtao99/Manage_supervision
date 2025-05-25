import axios from '../utils/axios'

// 课程接口类型定义
export interface Course {
  id?: number
  courseName: string
  courseDescription: string
  teacherId?: number
  teacherName: string
  courseDuration: number
  courseCategory: string
  status: string
  createTime?: string
  updateTime?: string
  createdBy?: number
  updatedBy?: number
}

// 分页响应类型
export interface PageResponse<T> {
  content: T[]
  total: number
  page: number
  size: number
}

// 获取课程列表（分页）
export const getCourseList = async (params: {
  page?: number
  size?: number
  courseName?: string
  teacherName?: string
  category?: string
  status?: string
}) => {
  try {
    const response = await axios.get('/api/courses', { params })
    return response.data
  } catch (error) {
    console.error('获取课程列表失败:', error)
    throw error
  }
}

// 根据ID获取课程详情
export const getCourseById = async (id: number) => {
  try {
    const response = await axios.get(`/api/courses/${id}`)
    return response.data
  } catch (error) {
    console.error('获取课程详情失败:', error)
    throw error
  }
}

// 创建课程
export const createCourse = async (courseData: Course) => {
  try {
    const response = await axios.post('/api/courses', courseData)
    return response.data
  } catch (error) {
    console.error('创建课程失败:', error)
    throw error
  }
}

// 更新课程
export const updateCourse = async (id: number, courseData: Course) => {
  try {
    const response = await axios.put(`/api/courses/${id}`, courseData)
    return response.data
  } catch (error) {
    console.error('更新课程失败:', error)
    throw error
  }
}

// 删除课程
export const deleteCourse = async (id: number) => {
  try {
    const response = await axios.delete(`/api/courses/${id}`)
    return response.data
  } catch (error) {
    console.error('删除课程失败:', error)
    throw error
  }
}

// 获取所有课程类别
export const getAllCategories = async () => {
  try {
    const response = await axios.get('/api/courses/categories')
    return response.data
  } catch (error) {
    console.error('获取课程类别失败:', error)
    throw error
  }
}

// 获取所有教师列表（用于下拉框）
export const getAllTeachers = async () => {
  try {
    const response = await axios.get('/api/courses/teachers')
    return response.data
  } catch (error) {
    console.error('获取教师列表失败:', error)
    throw error
  }
}

// 根据教师ID获取课程列表
export const getCoursesByTeacherId = async (teacherId: number) => {
  try {
    const response = await axios.get(`/api/courses/teacher/${teacherId}`)
    return response.data
  } catch (error) {
    console.error('获取教师课程列表失败:', error)
    throw error
  }
}

// 获取所有活跃课程
export const getActiveCourses = async () => {
  try {
    const response = await axios.get('/api/courses/active')
    return response.data
  } catch (error) {
    console.error('获取活跃课程列表失败:', error)
    throw error
  }
}

// 课程类别管理API
export interface CourseCategory {
  id?: number
  categoryName: string
  description?: string
  status: string
  createTime?: string
  updateTime?: string
}

// 获取课程类别列表（分页）
export const getCourseCategoryList = async (params: {
  page?: number
  size?: number
  categoryName?: string
  status?: string
}) => {
  try {
    const response = await axios.get('/api/courses/categories/list', { params })
    return response.data
  } catch (error) {
    console.error('获取课程类别列表失败:', error)
    throw error
  }
}

// 创建课程类别
export const createCourseCategory = async (categoryData: CourseCategory) => {
  try {
    const response = await axios.post('/api/courses/categories', categoryData)
    return response.data
  } catch (error) {
    console.error('创建课程类别失败:', error)
    throw error
  }
}

// 更新课程类别
export const updateCourseCategory = async (id: number, categoryData: CourseCategory) => {
  try {
    const response = await axios.put(`/api/courses/categories/${id}`, categoryData)
    return response.data
  } catch (error) {
    console.error('更新课程类别失败:', error)
    throw error
  }
}

// 删除课程类别
export const deleteCourseCategory = async (id: number) => {
  try {
    const response = await axios.delete(`/api/courses/categories/${id}`)
    return response.data
  } catch (error) {
    console.error('删除课程类别失败:', error)
    throw error
  }
} 