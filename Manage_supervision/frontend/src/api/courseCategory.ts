import axios from '../utils/axios'

// 获取课程类别列表（分页）
export const getCourseCategories = async (page = 1, size = 10, name?: string) => {
  try {
    const params: any = { page, size }
    if (name) {
      params.name = name
    }
    
    console.log('API调用: getCourseCategories, 参数:', params)
    const response = await axios.get('/api/course-categories', { params })
    console.log('API响应: getCourseCategories, 数据:', response.data)
    return response.data
  } catch (error) {
    console.error('获取课程类别列表失败:', error)
    throw error
  }
}

// 获取所有课程类别（不分页）
export const getAllCourseCategories = async () => {
  try {
    console.log('API调用: getAllCourseCategories')
    const response = await axios.get('/api/course-categories/all')
    console.log('API响应: getAllCourseCategories, 数据:', response.data)
    return response.data
  } catch (error) {
    console.error('获取所有课程类别失败:', error)
    throw error
  }
}

// 获取课程类别详情
export const getCourseCategoryById = async (id: number) => {
  try {
    console.log('API调用: getCourseCategoryById, ID:', id)
    const response = await axios.get(`/api/course-categories/${id}`)
    console.log('API响应: getCourseCategoryById, 数据:', response.data)
    return response.data
  } catch (error) {
    console.error('获取课程类别详情失败:', error)
    throw error
  }
}

// 创建课程类别
export const createCourseCategory = async (categoryData: any) => {
  try {
    console.log('API调用: createCourseCategory, 数据:', categoryData)
    const response = await axios.post('/api/course-categories', categoryData)
    console.log('API响应: createCourseCategory, 数据:', response.data)
    return response.data
  } catch (error) {
    console.error('创建课程类别失败:', error)
    throw error
  }
}

// 更新课程类别
export const updateCourseCategory = async (id: number, categoryData: any) => {
  try {
    console.log('API调用: updateCourseCategory, ID:', id, '数据:', categoryData)
    const response = await axios.put(`/api/course-categories/${id}`, categoryData)
    console.log('API响应: updateCourseCategory, 数据:', response.data)
    return response.data
  } catch (error) {
    console.error('更新课程类别失败:', error)
    throw error
  }
}

// 删除课程类别
export const deleteCourseCategory = async (id: number) => {
  try {
    console.log('API调用: deleteCourseCategory, ID:', id)
    const response = await axios.delete(`/api/course-categories/${id}`)
    console.log('API响应: deleteCourseCategory, 数据:', response.data)
    return response.data
  } catch (error) {
    console.error('删除课程类别失败:', error)
    throw error
  }
} 