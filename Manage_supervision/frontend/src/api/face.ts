import apiService from './apiService.js'

/**
 * 检测人脸
 * @param data 包含图像数据的对象
 */
export function detectFace(data: { image: string }) {
  return apiService.face.detectFace(data.image)
}

/**
 * 注册当前用户的人脸
 * @param data 包含人脸数据的对象
 */
export function registerFace(data: { faceData: string }) {
  return apiService.face.registerFace(data.faceData)
}

/**
 * 验证人脸
 * @param userId 用户ID
 * @param faceData 人脸数据
 */
export function verifyFace(userId: number, faceData: string) {
  return apiService.face.verifyFace(userId, faceData)
}

/**
 * 获取当前用户的人脸注册状态
 */
export function getFaceStatus() {
  return apiService.face.getFaceStatus()
} 