import axios from '../utils/axios'
import type { RegionData } from '../types/region'
import { ElMessage } from 'element-plus'

/**
 * 获取地区树结构 - 添加重试逻辑
 */
export const getRegionTree = async (retryCount = 3, retryDelay = 2000) => {
  let lastError = null;
  
  for (let attempt = 0; attempt < retryCount; attempt++) {
    try {
      const response = await axios.get('/api/admin/regions/tree')
      return response.data
    } catch (error: any) {
      lastError = error
      console.warn(`获取地区树结构失败，尝试重试 (${attempt + 1}/${retryCount})`, error.message)
      
      // 如果不是最后一次尝试，等待一段时间后重试
      if (attempt < retryCount - 1) {
        await new Promise(resolve => setTimeout(resolve, retryDelay))
      }
    }
  }
  
  // 所有重试都失败了
  console.error(`获取地区树结构失败，已重试${retryCount}次`, lastError)
  throw lastError
}

/**
 * 获取指定父级ID的子地区列表
 */
export const getChildRegions = async (parentId: number) => {
  const response = await axios.get(`/api/admin/regions/parent/${parentId}`)
  return response.data
}

/**
 * 获取指定级别的地区列表
 */
export const getRegionsByLevel = async (level: number) => {
  const response = await axios.get(`/api/admin/regions/level/${level}`)
  return response.data
}

/**
 * 获取地区详情
 */
export const getRegionDetail = async (id: number) => {
  const response = await axios.get(`/api/admin/regions/${id}`)
  return response.data
}

/**
 * 添加地区
 */
export const addRegion = async (data: RegionData) => {
  const response = await axios.post('/api/admin/regions', data)
  return response.data
}

/**
 * 更新地区
 */
export const updateRegion = async (id: number, data: RegionData) => {
  const response = await axios.put(`/api/admin/regions/${id}`, data)
  return response.data
}

/**
 * 删除地区
 */
export const deleteRegion = async (id: number) => {
  const response = await axios.delete(`/api/admin/regions/${id}`)
  return response.data
}

/**
 * 切换地区状态
 */
export const toggleRegionStatus = async (id: number) => {
  const response = await axios.post(`/api/admin/regions/${id}/toggle-status`)
  return response.data
}

/**
 * 获取地区路径（省市区完整路径）
 */
export const getRegionPath = async (regionId: number) => {
  const response = await axios.get(`/api/admin/regions/${regionId}/path`)
  return response.data
} 