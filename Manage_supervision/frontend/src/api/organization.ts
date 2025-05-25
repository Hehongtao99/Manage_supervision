import axios from '../utils/axios'

// 学院接口
export interface College {
  id?: number
  collegeName: string
  collegeCode: string
  description?: string
  status?: string
  createTime?: string
  updateTime?: string
}

// 专业接口
export interface Major {
  id?: number
  majorName: string
  majorCode: string
  collegeId: number
  description?: string
  status?: string
  createTime?: string
  updateTime?: string
  collegeName?: string
}

// 班级接口
export interface ClassEntity {
  id?: number
  className: string
  classCode: string
  collegeId: number
  majorId: number
  grade: string
  studentCount?: number
  description?: string
  status?: string
  createTime?: string
  updateTime?: string
  collegeName?: string
  majorName?: string
}

// 分页响应类型
export interface PageResponse<T> {
  content: T[]
  total: number
  page: number
  size: number
}

// ==================== 学院管理 API ====================

// 获取学院分页列表
export const getColleges = (params: {
  current?: number
  size?: number
  collegeName?: string
}) => {
  return axios.get<PageResponse<College>>('/api/admin/organization/colleges', { params })
}

// 创建学院
export const createCollege = (data: College) => {
  return axios.post('/api/admin/organization/colleges', data)
}

// 更新学院
export const updateCollege = (id: number, data: College) => {
  return axios.put(`/api/admin/organization/colleges/${id}`, data)
}

// 删除学院
export const deleteCollege = (id: number) => {
  return axios.delete(`/api/admin/organization/colleges/${id}`)
}

// 获取所有学院
export const getAllColleges = () => {
  return axios.get<College[]>('/api/admin/organization/colleges/all')
}

// ==================== 专业管理 API ====================

// 获取专业分页列表
export const getMajors = (params: {
  current?: number
  size?: number
  majorName?: string
  collegeId?: number
}) => {
  return axios.get<PageResponse<Major>>('/api/admin/organization/majors', { params })
}

// 创建专业
export const createMajor = (data: Major) => {
  return axios.post('/api/admin/organization/majors', data)
}

// 更新专业
export const updateMajor = (id: number, data: Major) => {
  return axios.put(`/api/admin/organization/majors/${id}`, data)
}

// 删除专业
export const deleteMajor = (id: number) => {
  return axios.delete(`/api/admin/organization/majors/${id}`)
}

// 根据学院获取专业
export const getMajorsByCollege = (collegeId: number) => {
  return axios.get<Major[]>(`/api/admin/organization/majors/by-college/${collegeId}`)
}

// 获取所有专业
export const getAllMajors = () => {
  return axios.get<Major[]>('/api/admin/organization/majors/all')
}

// ==================== 班级管理 API ====================

// 获取班级分页列表
export const getClasses = (params: {
  current?: number
  size?: number
  className?: string
  collegeId?: number
  majorId?: number
}) => {
  return axios.get<PageResponse<ClassEntity>>('/api/admin/organization/classes', { params })
}

// 创建班级
export const createClass = (data: ClassEntity) => {
  return axios.post('/api/admin/organization/classes', data)
}

// 更新班级
export const updateClass = (id: number, data: ClassEntity) => {
  return axios.put(`/api/admin/organization/classes/${id}`, data)
}

// 删除班级
export const deleteClass = (id: number) => {
  return axios.delete(`/api/admin/organization/classes/${id}`)
}

// 根据专业获取班级
export const getClassesByMajor = (majorId: number) => {
  return axios.get<ClassEntity[]>(`/api/admin/organization/classes/by-major/${majorId}`)
}

// 根据学院获取班级
export const getClassesByCollege = (collegeId: number) => {
  return axios.get<ClassEntity[]>(`/api/admin/organization/classes/by-college/${collegeId}`)
}

// 获取所有班级
export const getAllClasses = () => {
  return axios.get<ClassEntity[]>('/api/admin/organization/classes/all')
} 