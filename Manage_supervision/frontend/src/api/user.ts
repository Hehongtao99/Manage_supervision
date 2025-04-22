// 该文件是为了兼容性而创建的，实际上重新导出student.ts中的内容

import { getStudents as _getStudents } from './student';
import axios from 'axios';
import type { UserDTO } from '@/types/user';

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

const API_URL = '/api/user';

// 获取当前用户信息
export const getCurrentUser = async (): Promise<UserDTO> => {
  const response = await axios.get(`${API_URL}/current`);
  return response.data;
};

// 获取所有教师
export const getAllTeachers = async (): Promise<UserDTO[]> => {
  const response = await axios.get(`${API_URL}/teachers`);
  return response.data;
};

// 更新用户信息
export const updateUserProfile = async (userInfo: UserDTO): Promise<UserDTO> => {
  const response = await axios.put(`${API_URL}/profile`, userInfo);
  return response.data;
};

// 更改密码
export const changePassword = async (oldPassword: string, newPassword: string): Promise<any> => {
  const response = await axios.post(`${API_URL}/change-password`, {
    oldPassword,
    newPassword
  });
  return response.data;
};

// 上传头像
export const uploadAvatar = async (file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('avatar', file);
  
  const response = await axios.post(`${API_URL}/avatar`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  
  return response.data.avatarUrl;
}; 