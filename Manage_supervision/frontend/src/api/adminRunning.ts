import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { getAuthHeader } from '@/utils/auth'

// 基础API路径
const API_URL = '/api/admin/running'

/**
 * 获取跑步距离排行榜
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 * @param limit 查询数量限制（可选，默认10）
 */
export const getDistanceRanking = async (
  startDate?: string, 
  endDate?: string, 
  limit?: number
): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/ranking/distance`, {
    params: { startDate, endDate, limit },
    headers: getAuthHeader()
  })
}

/**
 * 获取跑步次数排行榜
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 * @param limit 查询数量限制（可选，默认10）
 */
export const getFrequencyRanking = async (
  startDate?: string, 
  endDate?: string, 
  limit?: number
): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/ranking/frequency`, {
    params: { startDate, endDate, limit },
    headers: getAuthHeader()
  })
}

/**
 * 获取最佳配速排行榜
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 * @param limit 查询数量限制（可选，默认10）
 */
export const getBestPaceRanking = async (
  startDate?: string, 
  endDate?: string, 
  limit?: number
): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/ranking/pace`, {
    params: { startDate, endDate, limit },
    headers: getAuthHeader()
  })
}

/**
 * 获取跑步报表数据
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 */
export const getRunningReport = async (
  startDate?: string, 
  endDate?: string
): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/report`, {
    params: { startDate, endDate },
    headers: getAuthHeader()
  })
}

/**
 * 获取每日跑步人数统计
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 */
export const getDailyRunningCount = async (
  startDate?: string, 
  endDate?: string
): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/daily-count`, {
    params: { startDate, endDate },
    headers: getAuthHeader()
  })
}

/**
 * 获取距离分布统计
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 */
export const getDistanceDistribution = async (
  startDate?: string, 
  endDate?: string
): Promise<AxiosResponse> => {
  return axios.get(`${API_URL}/distance-distribution`, {
    params: { startDate, endDate },
    headers: getAuthHeader()
  })
} 