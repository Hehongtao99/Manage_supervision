import axios from '../utils/axios'
import type { ScenicSpotData, ScenicSpotQueryParams } from '../types/scenicSpot'

/**
 * 获取景区列表
 * @param params 查询参数
 */
export const getScenicSpotList = async (params: ScenicSpotQueryParams = {}) => {
  const response = await axios.get('/api/admin/scenic-spots', { params })
  return response.data
}

/**
 * 获取景区详情
 * @param id 景区ID
 */
export const getScenicSpotDetail = async (id: number) => {
  console.log('API调用: 获取景区详情, ID:', id);
  if (!id || isNaN(id)) {
    console.error('无效的景区ID:', id);
    throw new Error('无效的景区ID');
  }
  try {
    const response = await axios.get(`/api/admin/scenic-spots/${id}`);
    console.log('API原始返回结果:', response);
    return response.data;
  } catch (error) {
    console.error('获取景区详情API错误:', error);
    throw error;
  }
}

/**
 * 添加景区
 * @param data 景区数据
 */
export const addScenicSpot = async (data: ScenicSpotData) => {
  const response = await axios.post('/api/admin/scenic-spots', data)
  return response.data
}

/**
 * 更新景区
 * @param id 景区ID
 * @param data 景区数据
 */
export const updateScenicSpot = async (id: number, data: ScenicSpotData) => {
  const response = await axios.put(`/api/admin/scenic-spots/${id}`, data)
  return response.data
}

/**
 * 删除景区
 * @param id 景区ID
 */
export const deleteScenicSpot = async (id: number) => {
  const response = await axios.delete(`/api/admin/scenic-spots/${id}`)
  return response.data
}

/**
 * 切换景区状态
 * @param id 景区ID
 */
export const toggleScenicSpotStatus = async (id: number) => {
  const response = await axios.post(`/api/admin/scenic-spots/${id}/toggle-status`)
  return response.data
}

/**
 * 获取热门景区
 */
export const getHotScenicSpots = async (limit: number = 10) => {
  const response = await axios.get('/api/admin/scenic-spots/hot', { params: { limit } })
  return response.data
} 