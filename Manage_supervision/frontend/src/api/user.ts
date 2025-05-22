// 该文件是为了兼容性而创建的，实际上重新导出student.ts中的内容

import axios from '../utils/axios';

// 将getStudents作为getAllStudents导出，以便兼容旧代码
import { getStudents as _getStudents } from './student';

export interface UserProfile {
  id: number;
  username: string;
  realName: string;
  nickname: string;
  avatar: string;
  bio: string;
  email: string;
  phone: string;
  roles: string[];
  createTime: string;
  status: 'active' | 'inactive';
  userNumber: string;
}

export interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

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

// 更新用户头像
export const updateAvatar = async (formData: FormData) => {
  const response = await axios.post('/api/user/avatar', formData);
  return response.data;
};

// 上传人脸数据
export const uploadFaceData = async (faceData: string) => {
  const response = await axios.post('/api/user/face-data', { faceData });
  return response.data;
};

// 检查用户是否有人脸数据
export const hasFaceData = async () => {
  const response = await axios.get('/api/user/has-face-data');
  return response.data.hasFaceData;
};

// 获取用户人脸数据
export const getFaceData = async () => {
  const response = await axios.get('/api/user/face-data');
  return response.data.faceData;
}; 