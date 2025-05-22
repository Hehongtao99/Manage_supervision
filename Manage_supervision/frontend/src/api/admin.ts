import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { getAuthHeader } from '../utils/auth'

// 基础API路径
const ADMIN_API_URL = '/api/admin'

// 社交管理API
const SOCIAL_API_URL = `${ADMIN_API_URL}/social`

// 运动管理API
const RUNNING_API_URL = `${ADMIN_API_URL}/running`

/**
 * 获取所有朋友圈帖子列表
 * @param page 页码
 * @param size 每页大小
 */
export const getAllPosts = async (page = 1, size = 10): Promise<AxiosResponse> => {
  return axios.get(`${SOCIAL_API_URL}/posts`, {
    params: { page, size },
    headers: getAuthHeader()
  })
}

/**
 * 获取所有评论列表
 * @param page 页码
 * @param size 每页大小
 */
export const getAllComments = async (page = 1, size = 10): Promise<AxiosResponse> => {
  return axios.get(`${SOCIAL_API_URL}/comments`, {
    params: { page, size },
    headers: getAuthHeader()
  })
}

/**
 * 下架朋友圈帖子
 * @param id 帖子ID
 * @param reason 下架原因
 */
export const takedownPost = async (id: number, reason?: string): Promise<AxiosResponse> => {
  return axios.post(`${SOCIAL_API_URL}/post/${id}/takedown`, null, {
    params: { reason },
    headers: getAuthHeader()
  })
}

/**
 * 下架评论
 * @param id 评论ID
 * @param reason 下架原因
 */
export const takedownComment = async (id: number, reason?: string): Promise<AxiosResponse> => {
  return axios.post(`${SOCIAL_API_URL}/comment/${id}/takedown`, null, {
    params: { reason },
    headers: getAuthHeader()
  })
}

/**
 * 恢复朋友圈帖子
 * @param id 帖子ID
 */
export const restorePost = async (id: number): Promise<AxiosResponse> => {
  return axios.post(`${SOCIAL_API_URL}/post/${id}/restore`, null, {
    headers: getAuthHeader()
  })
}

/**
 * 恢复评论
 * @param id 评论ID
 */
export const restoreComment = async (id: number): Promise<AxiosResponse> => {
  return axios.post(`${SOCIAL_API_URL}/comment/${id}/restore`, null, {
    headers: getAuthHeader()
  })
}

/**
 * 搜索朋友圈帖子
 * @param keyword 关键词
 * @param page 页码
 * @param size 每页大小
 */
export const searchPosts = async (keyword: string, page = 1, size = 10): Promise<AxiosResponse> => {
  return axios.get(`${SOCIAL_API_URL}/posts/search`, {
    params: { keyword, page, size },
    headers: getAuthHeader()
  })
}

/**
 * 搜索评论
 * @param keyword 关键词
 * @param page 页码
 * @param size 每页大小
 */
export const searchComments = async (keyword: string, page = 1, size = 10): Promise<AxiosResponse> => {
  return axios.get(`${SOCIAL_API_URL}/comments/search`, {
    params: { keyword, page, size },
    headers: getAuthHeader()
  })
}

/**
 * 获取所有用户跑步记录
 * @param page 页码
 * @param size 每页大小
 * @param username 用户名筛选（可选）
 * @param startDate 开始日期筛选，格式yyyy-MM-dd（可选）
 * @param endDate 结束日期筛选，格式yyyy-MM-dd（可选）
 */
export const getAllUserRunningRecords = async (page = 1, size = 10, username?: string, startDate?: string, endDate?: string): Promise<AxiosResponse> => {
  return axios.get(`${RUNNING_API_URL}/records`, {
    params: { page, size, username, startDate, endDate },
    headers: getAuthHeader()
  })
}

/**
 * 获取跑步统计数据
 */
export const getRunningStatistics = async (): Promise<AxiosResponse> => {
  return axios.get(`${RUNNING_API_URL}/statistics`, {
    headers: getAuthHeader()
  })
}

/**
 * 删除跑步记录
 * @param id 记录ID
 */
export const deleteRunningRecord = async (id: number): Promise<AxiosResponse> => {
  return axios.delete(`${RUNNING_API_URL}/record/${id}`, {
    headers: getAuthHeader()
  })
} 