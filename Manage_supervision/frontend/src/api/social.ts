import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { getAuthHeader } from '@/utils/auth'
import type { PostCreateRequest, CommentCreateRequest, PostForwardRequest } from '@/types/social'

// 基础API路径
const API_URL = '/api/social'

/**
 * 发布帖子
 * @param postData 帖子数据
 */
export const createPost = async (postData: PostCreateRequest): Promise<AxiosResponse> => {
  return axios.post(`${API_URL}/post`, postData, { headers: getAuthHeader() })
}

/**
 * 上传图片
 * @param file 图片文件
 */
export const uploadImage = async (file: File): Promise<AxiosResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post(`${API_URL}/upload/image`, formData, {
    headers: {
      ...getAuthHeader(),
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取朋友圈帖子列表
 * @param page 页码
 * @param size 每页大小
 */
export const getPostList = async (page = 1, size = 10): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/posts`, {
    params: { page, size },
    headers: getAuthHeader()
  })
}

/**
 * 获取用户自己的帖子列表
 * @param page 页码
 * @param size 每页大小
 */
export const getUserPostList = async (page = 1, size = 10): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/user/posts`, {
    params: { page, size },
    headers: getAuthHeader()
  })
}

/**
 * 获取帖子详情
 * @param id 帖子ID
 */
export const getPostDetail = async (id: number): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/post/${id}`, { headers: getAuthHeader() })
}

/**
 * 点赞或取消点赞
 * @param id 帖子ID
 */
export const toggleLike = async (id: number): Promise<AxiosResponse> => {
  return axios.post(`${API_URL}/post/${id}/like`, {}, { headers: getAuthHeader() })
}

/**
 * 发表评论
 * @param commentData 评论数据
 */
export const createComment = async (commentData: CommentCreateRequest): Promise<AxiosResponse> => {
  return axios.post(`${API_URL}/comment`, commentData, { headers: getAuthHeader() })
}

/**
 * 获取帖子的评论列表
 * @param postId 帖子ID
 */
export const getCommentList = async (postId: number): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/post/${postId}/comments`, { headers: getAuthHeader() })
}

/**
 * 删除帖子
 * @param id 帖子ID
 */
export const deletePost = async (id: number): Promise<AxiosResponse> => {
  return axios.delete(`${API_URL}/post/${id}`, { headers: getAuthHeader() })
}

/**
 * 删除评论
 * @param id 评论ID
 */
export const deleteComment = async (id: number): Promise<AxiosResponse> => {
  return axios.delete(`${API_URL}/comment/${id}`, { headers: getAuthHeader() })
}

/**
 * 编辑朋友圈帖子
 * @param postData 帖子数据
 */
export const updatePost = async (postData: {
  id: number
  content: string
  imageUrls: string[]
  runningRecordId?: number | null
}): Promise<AxiosResponse> => {
  return axios.put(`${API_URL}/post`, postData, { headers: getAuthHeader() })
}

/**
 * 获取用户跑步记录列表
 */
export const getRunningRecordsList = async (): Promise<AxiosResponse> => {
  return axios.get('/api/student/running/records', { headers: getAuthHeader() })
}

/**
 * 转发朋友圈帖子
 * @param forwardData 转发数据
 */
export const forwardPost = async (forwardData: PostForwardRequest): Promise<AxiosResponse> => {
  return axios.post(`${API_URL}/post/forward`, forwardData, { headers: getAuthHeader() })
}

/**
 * 获取原始帖子信息
 * @param originalId 原始帖子ID
 */
export const getOriginalPost = async (originalId: number): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/post/original/${originalId}`, { headers: getAuthHeader() })
} 