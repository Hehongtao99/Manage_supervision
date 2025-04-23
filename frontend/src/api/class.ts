import axios from 'axios';
import { API_URL } from '@/config';
import { getCurrentUserId } from '@/utils/auth';

// 获取当前教师的班级（用于通知管理）
export const getClassesByTeacher = async () => {
  try {
    const userId = getCurrentUserId();
    console.log('当前用户ID:', userId);
    
    const response = await axios.get(`${API_URL}/teacher/classes`, {
      headers: {
        'userId': userId
      }
    });
    
    // 确保返回的数据格式正确
    if (response.data && Array.isArray(response.data)) {
      return {
        data: {
          success: true,
          data: response.data
        }
      };
    } else {
      // 如果后端返回了自定义的格式
      return response;
    }
  } catch (error) {
    console.error('获取教师班级列表失败:', error);
    throw error;
  }
}; 