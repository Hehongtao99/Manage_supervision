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

// 获取教师的班级列表
export const getTeacherClasses = async () => {
  try {
    const response = await axios.get('/api/supervisor/classes');
    console.log('从/api/supervisor/classes获取到的班级数据:', response.data);
    
    // 如果是数组，直接返回
    if (Array.isArray(response.data)) {
      return response.data;
    }
    
    // 如果返回的是包含classes字段的对象
    if (response.data && typeof response.data === 'object' && response.data.classes && Array.isArray(response.data.classes)) {
      return response.data.classes;
    }
    
    // 如果是包含data属性的对象，且data是数组
    if (response.data && typeof response.data === 'object' && response.data.data && Array.isArray(response.data.data)) {
      return response.data.data;
    }
    
    // 其他情况，确保返回一个空数组
    console.warn('班级数据格式不是预期的数组:', response.data);
    return [];
  } catch (error) {
    console.error('获取教师的班级列表失败:', error);
    return [];
  }
};

// 获取班级的学生列表
export const getClassStudents = async (classId: number) => {
  try {
    const response = await axios.get('/api/supervisor/classes/' + classId + '/students');
    console.log('从/api/supervisor/classes/'+classId+'/students获取到的学生数据:', response.data);
    
    // 如果是数组，直接返回
    if (Array.isArray(response.data)) {
      return response.data;
    }
    
    // 如果是包含data属性的对象，且data是数组
    if (response.data && typeof response.data === 'object' && response.data.data && Array.isArray(response.data.data)) {
      return response.data.data;
    }
    
    // 其他情况，确保返回一个空数组
    console.warn('学生数据格式不是预期的数组:', response.data);
    return [];
  } catch (error) {
    console.error('获取班级的学生列表失败:', error);
    return [];
  }
}; 