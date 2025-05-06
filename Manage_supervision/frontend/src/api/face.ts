import axios from '../utils/axios'

/**
 * 注册用户人脸（文件上传方式）
 * @param faceImage 人脸图像文件
 */
export const registerFace = async (faceImage: File): Promise<boolean> => {
  try {
    const formData = new FormData()
    formData.append('faceImage', faceImage)
    
    const response = await axios.post('/api/face/register', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return response.status === 200
  } catch (error) {
    console.error('注册人脸失败:', error)
    throw error
  }
}

/**
 * 注册用户人脸（Base64方式）
 * @param base64Image Base64编码的图像
 */
export const registerFaceWithBase64 = async (base64Image: string): Promise<boolean> => {
  try {
    const response = await axios.post('/api/face/register/base64', {
      faceImage: base64Image
    })
    
    return response.status === 200
  } catch (error) {
    console.error('注册人脸失败:', error)
    throw error
  }
}

/**
 * 人脸登录（文件上传方式）
 * @param faceImage 人脸图像文件
 */
export const loginWithFace = async (faceImage: File): Promise<any> => {
  try {
    const formData = new FormData()
    formData.append('faceImage', faceImage)
    
    const response = await axios.post('/api/face/login', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return response.data
  } catch (error) {
    console.error('人脸登录失败:', error)
    throw error
  }
}

/**
 * 人脸登录（Base64方式）
 * @param base64Image Base64编码的图像
 */
export const loginWithFaceBase64 = async (base64Image: string): Promise<any> => {
  try {
    const response = await axios.post('/api/face/login/base64', {
      faceImage: base64Image
    })
    
    return response.data
  } catch (error) {
    console.error('人脸登录失败:', error)
    throw error
  }
}

/**
 * 选择特定用户登录（人脸识别匹配多个用户时使用）
 * @param userId 选择的用户ID
 */
export const loginSelectedUser = async (userId: number): Promise<any> => {
  try {
    const response = await axios.post('/api/face/login/select-user', {
      userId: userId
    })
    
    return response.data
  } catch (error) {
    console.error('选择用户登录失败:', error)
    throw error
  }
}

/**
 * 删除用户人脸信息
 */
export const deleteFace = async (): Promise<boolean> => {
  try {
    const response = await axios.delete('/api/face/delete')
    
    return response.status === 200
  } catch (error) {
    console.error('删除人脸失败:', error)
    throw error
  }
}

/**
 * 检查用户是否已注册人脸
 */
export const checkFaceStatus = async (): Promise<boolean> => {
  try {
    const response = await axios.get('/api/face/status')
    
    return response.data.hasFaceRegistered
  } catch (error) {
    console.error('检查人脸状态失败:', error)
    throw error
  }
} 