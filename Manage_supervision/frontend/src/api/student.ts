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

// 获取学生列表（分页）
export const getStudents = async (page = 1, size = 10, keyword?: string) => {
  try {
    const params: any = { page, size };
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await axios.get('/api/admin/students', { params });
    return response.data;
  } catch (error) {
    console.error('获取学生列表失败:', error);
    throw error;
  }
};

// 获取未分配的学生列表
export const getUnassignedStudents = async () => {
  try {
    const response = await axios.get('/api/admin/students/unassigned');
    return response.data;
  } catch (error) {
    console.error('获取未分配学生列表失败:', error);
    throw error;
  }
};

// 获取学生详情
export const getStudentDetail = async (studentId: number) => {
  try {
    const response = await axios.get(`/api/admin/users/${studentId}`);
    return response.data;
  } catch (error) {
    console.error('获取学生详情失败:', error);
    throw error;
  }
};

// 更新学生状态
export const updateStudentStatus = async (studentId: number, status: string) => {
  try {
    const response = await axios.post(`/api/admin/users/${studentId}/toggle-status`);
    return true;
  } catch (error) {
    console.error('更新学生状态失败:', error);
    throw error;
  }
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

// 接口为用户类型声明
export interface Student {
  id: number;
  username: string;
  name: string;
  realName: string;
  studentId: string;
  userNumber: string;
  email: string;
  phone: string;
  status: string;
  createTime: string;
  roles: string[];
} 