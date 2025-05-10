import axios from '../utils/axios'

// 科目DTO接口
export interface SubjectDTO {
  id: number
  name: string
  createTime: string
  updateTime: string
}

/**
 * 获取所有科目列表
 */
export const getAllSubjects = async () => {
  try {
    const response = await axios.get('/api/subjects')
    return response.data
  } catch (error) {
    console.error('获取科目列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取科目
 */
export const getSubjectById = async (id: number) => {
  try {
    const response = await axios.get(`/api/subjects/${id}`)
    return response.data
  } catch (error) {
    console.error('获取科目详情失败:', error)
    throw error
  }
}

/**
 * 创建科目（管理员）
 */
export const createSubject = async (name: string) => {
  try {
    const response = await axios.post('/api/admin/subjects', null, {
      params: { name }
    })
    return response.data
  } catch (error) {
    console.error('创建科目失败:', error)
    throw error
  }
}

/**
 * 更新科目（管理员）
 */
export const updateSubject = async (id: number, name: string) => {
  try {
    const response = await axios.put(`/api/admin/subjects/${id}`, null, {
      params: { name }
    })
    return response.data
  } catch (error) {
    console.error('更新科目失败:', error)
    throw error
  }
}

/**
 * 删除科目（管理员）
 */
export const deleteSubject = async (id: number) => {
  try {
    const response = await axios.delete(`/api/admin/subjects/${id}`)
    return response.data
  } catch (error) {
    console.error('删除科目失败:', error)
    throw error
  }
} 