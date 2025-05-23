import apiService from './apiService.js'

/**
 * 获取学生列表
 * @param params 查询参数
 */
export function getStudents(params: any) {
  return apiService.get('/api/admin/students', params)
}

/**
 * 更新用户的人脸数据
 * @param userId 用户ID
 * @param data 包含人脸数据的对象
 */
export function updateUserFaceData(userId: number, data: { faceData: string }) {
  return apiService.put(`/api/admin/user/${userId}/face`, data)
}

/**
 * 删除用户的人脸数据
 * @param userId 用户ID
 */
export function deleteUserFaceData(userId: number) {
  return apiService.delete(`/api/admin/user/${userId}/face`)
}

/**
 * 获取用户的人脸数据状态
 * @param userId 用户ID
 */
export function getUserFaceStatus(userId: number) {
  return apiService.get(`/api/admin/user/${userId}/face-status`)
} 