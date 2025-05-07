// 该文件是为了兼容性而创建的，实际上重新导出student.ts中的内容

import { getStudents as _getStudents } from './student';
import instance from '../utils/axios'
import { getAuthHeader } from '../utils/auth'

// 将getStudents作为getAllStudents导出，以便兼容旧代码
export const getAllStudents = _getStudents;

// 直接在此处定义Student类型，而不是导入StudentDTO
export interface Student {
  id: number;
  name: string;
  studentId?: string;
  userNumber: string; // 添加userNumber字段作为学号
  class?: string; // 注意：这里用class而不是className，与StudentManagement.vue中使用一致
  email: string;
  phone: string;
  status: string;
  lastLogin?: string;
  progress?: number;
  username?: string;
  realName?: string;
  roles?: string[];
  createTime?: string;
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