import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { getAuthHeader } from '@/utils/auth'

// 基础API路径
const API_URL = '/api/admin/social'

/**
 * 获取所有朋友圈帖子分页列表
 * @param page 页码
 * @param size 每页大小
 * @param keyword 搜索关键词
 */
export const getAllPosts = async (page: number, size: number, keyword?: string): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/posts`, {
    params: { page, size, keyword },
    headers: getAuthHeader()
  })
}

/**
 * 获取指定用户的朋友圈帖子
 * @param userId 用户ID
 * @param page 页码
 * @param size 每页大小
 */
export const getUserPosts = async (userId: number, page: number, size: number): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/posts/user/${userId}`, {
    params: { page, size },
    headers: getAuthHeader()
  })
}

/**
 * 获取帖子详情
 * @param postId 帖子ID
 */
export const getPostDetail = async (postId: number): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/post/${postId}`, {
    headers: getAuthHeader()
  })
}

/**
 * 删除帖子
 * @param postId 帖子ID
 */
export const deletePost = async (postId: number): Promise<AxiosResponse> => {
  return axios.delete(`${API_URL}/post/${postId}`, {
    headers: getAuthHeader()
  })
}

/**
 * 获取帖子的评论列表
 * @param postId 帖子ID
 */
export const getPostComments = async (postId: number): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/post/${postId}/comments`, {
    headers: getAuthHeader()
  })
}

/**
 * 获取所有评论分页列表
 * @param page 页码
 * @param size 每页大小
 * @param keyword 搜索关键词
 */
export const getAllComments = async (page: number, size: number, keyword?: string): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/comments`, {
    params: { page, size, keyword },
    headers: getAuthHeader()
  })
}

/**
 * 删除评论
 * @param commentId 评论ID
 */
export const deleteComment = async (commentId: number): Promise<AxiosResponse> => {
  return axios.delete(`${API_URL}/comment/${commentId}`, {
    headers: getAuthHeader()
  })
} 