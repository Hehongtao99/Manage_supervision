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
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    let url = '/api/admin/students'; // 默认管理员API
    
    // 如果是督导员/教师角色，使用督导员API
    if (userRoles.includes('SUPERVISOR')) {
      url = '/api/supervisor/students/assigned';
    }
    
    const params: any = { page, size };
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await axios.get(url, { params });
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
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    console.log('当前用户角色:', userRoles);
    
    // 默认使用督导员API，因为这是StudentManagement.vue是督导员视图
    const url = `/api/supervisor/students/${studentId}`;
    console.log('使用API路径:', url);
    
    // 保存原始错误以便在处理失败时显示
    let originalError = null;
    
    try {
      const response = await axios.get(url);
      console.log('获取学生详情成功:', response.data);
      return response.data;
    } catch (error) {
      console.warn('通过督导员API获取学生详情失败，尝试使用学生列表数据:', error);
      originalError = error;
      
      // 尝试从已获取的学生列表中找到该学生
      const assignedResponse = await axios.get('/api/supervisor/students/assigned');
      if (Array.isArray(assignedResponse.data)) {
        const student = assignedResponse.data.find(s => s.id === studentId);
        if (student) {
          console.log('从已获取的学生列表中找到学生:', student);
          return student;
        }
      }
      
      // 如果仍未找到，抛出原始错误
      throw originalError;
    }
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

// 添加获取当前督导员分配的学生列表的API函数
export const getAssignedStudents = async () => {
  try {
    const response = await axios.get('/api/supervisor/students/assigned');
    return response.data;
  } catch (error) {
    console.error('获取分配的学生列表失败:', error);
    throw error;
  }
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