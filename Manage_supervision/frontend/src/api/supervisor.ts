import axios from '../utils/axios';

export interface SupervisorDTO {
  id: number;
  name?: string;
  username: string;
  email?: string;
  phone?: string;
  department?: string;
  title?: string;
  status?: string;
  lastLogin?: string;
}

// 获取所有督导员
export const getSupervisors = async () => {
  const response = await axios.get('/api/user/supervisors');
  return response.data;
};

// 获取督导员详情
export const getSupervisorDetail = async (supervisorId: number) => {
  const response = await axios.get(`/api/user/supervisors/${supervisorId}`);
  return response.data;
};

// 按部门获取督导员
export const getSupervisorsByDepartment = async (department: string) => {
  const response = await axios.get(`/api/user/supervisors/department/${department}`);
  return response.data;
}; 