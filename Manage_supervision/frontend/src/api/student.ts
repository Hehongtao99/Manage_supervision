import axios from '../utils/axios';

export interface StudentDTO {
  id: number;
  name: string;
  studentId: string;
  className: string;
  email: string;
  phone: string;
  status: string;
  lastLogin: string;
  progress: number;
}

export interface StudentDetailDTO extends StudentDTO {
  courses: CourseDTO[];
  activities: ActivityDTO[];
}

export interface CourseDTO {
  name: string;
  status: string;
  description: string;
  completion: number;
}

export interface ActivityDTO {
  type: string;
  title: string;
  content: string;
  time: string;
}

// 获取所有学生
export const getStudents = async () => {
  const response = await axios.get('/api/supervisor/students');
  return response.data;
};

// 获取学生详情
export const getStudentDetail = async (studentId: number) => {
  const response = await axios.get(`/api/supervisor/students/${studentId}`);
  return response.data;
};

// 更新学生状态
export const updateStudentStatus = async (studentId: number, status: string) => {
  const response = await axios.put(`/api/supervisor/students/${studentId}/status`, { status });
  return response.data;
};

// 删除学生
export const deleteStudent = async (studentId: number) => {
  const response = await axios.delete(`/api/supervisor/students/${studentId}`);
  return response.status === 200;
};

// 分页获取学生列表
export const getStudentsByPage = async (page: number, size: number) => {
  const response = await axios.get(`/api/supervisor/students/page`, {
    params: { page, size }
  });
  return response.data;
};

// 根据条件筛选学生
export const getStudentsByFilter = async (filter: Record<string, any>) => {
  const response = await axios.get(`/api/supervisor/students/filter`, {
    params: filter
  });
  return response.data;
};

// 添加学生（分配到督导员）
export const assignStudentToSupervisor = async (studentId: number, supervisorId: number) => {
  const response = await axios.put(`/api/supervisor/students/${studentId}/supervisor/${supervisorId}`);
  return response.data;
};

// 获取督导员的学生列表
export const getSupervisorStudents = async (supervisorId: number) => {
  const response = await axios.get(`/api/supervisor/students/supervisor/${supervisorId}`);
  return response.data;
}; 