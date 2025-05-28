import instance from '../utils/axios'
import { getAuthHeader } from '../utils/auth'

// Runner类型定义
export interface Runner {
  id: number;
  username: string;
  runnerId?: string;
  name: string;
  email: string;
  phone: string;
  status: string;
  createTime: string;
}

// RunnerDetail类型定义
export interface RunnerDetail extends Runner {
  userNumber?: string;
  realName?: string;
}

/**
 * 获取所有跑步爱好者列表
 */
export const getAllRunners = async (): Promise<Runner[]> => {
  const response = await instance.get('/api/user/runners', { headers: getAuthHeader() })
  return response.data.data || []
}

/**
 * 获取跑步爱好者详细信息
 * @param id 跑步爱好者ID
 */
export const getUserDetail = async (id: number): Promise<RunnerDetail> => {
  const response = await instance.get(`/api/user/runners/${id}`, { headers: getAuthHeader() })
  return response.data.data
}

/**
 * 获取指定用户的详细信息
 * @param userId 用户ID
 */
export const getUserInfo = async (userId: number) => {
  return instance.get(`/api/user/${userId}/info`, { headers: getAuthHeader() })
}

/**
 * 获取用户的统计信息（发帖数，跑步记录数等）
 * @param userId 用户ID
 */
export const getUserStats = async (userId: number) => {
  return instance.get(`/api/user/${userId}/stats`, { headers: getAuthHeader() })
} 