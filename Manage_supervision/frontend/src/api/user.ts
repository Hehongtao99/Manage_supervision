// 该文件是为了兼容性而创建的，实际上重新导出runner.ts中的内容
// 注意：这只是一个临时措施，请尽快更新所有导入到直接使用runner.ts
import { getRunners as _getRunners } from './runner';
import instance from '../utils/axios'
import { getAuthHeader } from '../utils/auth'

// 为兼容性重新导出
export { getRunnerDetail as getUserDetail } from './runner';
// 将getRunners作为getAllRunners导出，以便兼容旧代码
export const getAllRunners = _getRunners;

// 直接在此处定义Runner类型，而不是导入RunnerDTO
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