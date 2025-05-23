import axios from '../utils/axios';

export interface RunnerDTO {
  id: number;
  name: string;
  runnerId: string;
  email: string;
  phone: string;
  status: string;
  lastLogin: string;
  progress: number;
}

export interface RunnerDetailDTO extends RunnerDTO {
  runningRecords?: RunningRecordDTO[];
  achievements?: AchievementDTO[];
}

export interface RunningRecordDTO {
  id: number;
  distance: number;
  duration: number;
  pace: number;
  calories: number;
  createTime: string;
}

export interface AchievementDTO {
  type: string;
  title: string;
  description: string;
  time: string;
}

// 获取跑步爱好者列表（分页）
export const getRunners = async (page = 1, size = 10, keyword?: string) => {
  try {
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    let url = '/api/admin/runners'; // 默认管理员API
    
    // 如果是督导员角色，使用督导员API
    if (userRoles.includes('SUPERVISOR')) {
      url = '/api/supervisor/runners/assigned';
    }
    
    const params: any = { page, size };
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await axios.get(url, { params });
    return response.data;
  } catch (error) {
    console.error('获取跑步爱好者列表失败:', error);
    throw error;
  }
};

// 获取未分配的跑步爱好者列表
export const getUnassignedRunners = async () => {
  try {
    const response = await axios.get('/api/admin/runners/unassigned');
    return response.data;
  } catch (error) {
    console.error('获取未分配跑步爱好者列表失败:', error);
    throw error;
  }
};

// 获取跑步爱好者详情
export const getRunnerDetail = async (runnerId: number) => {
  try {
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    console.log('当前用户角色:', userRoles);
    
    const url = `/api/supervisor/runners/${runnerId}`;
    console.log('使用API路径:', url);
    
    let originalError = null;
    
    try {
      const response = await axios.get(url);
      console.log('获取跑步爱好者详情成功:', response.data);
      return response.data;
    } catch (error) {
      console.warn('通过督导员API获取跑步爱好者详情失败，尝试使用列表数据:', error);
      originalError = error;
      
      // 尝试从已获取的列表中找到该跑步爱好者
      const assignedResponse = await axios.get('/api/supervisor/runners/assigned');
      if (Array.isArray(assignedResponse.data)) {
        const runner = assignedResponse.data.find(r => r.id === runnerId);
        if (runner) {
          console.log('从已获取的列表中找到跑步爱好者:', runner);
          return runner;
        }
      }
      
      throw originalError;
    }
  } catch (error) {
    console.error('获取跑步爱好者详情失败:', error);
    throw error;
  }
};

// 更新跑步爱好者状态
export const updateRunnerStatus = async (runnerId: number, status: string) => {
  try {
    const response = await axios.post(`/api/admin/users/${runnerId}/toggle-status`);
    return true;
  } catch (error) {
    console.error('更新跑步爱好者状态失败:', error);
    throw error;
  }
};

// 删除跑步爱好者
export const deleteRunner = async (runnerId: number) => {
  const response = await axios.delete(`/api/supervisor/runners/${runnerId}`);
  return response.status === 200;
};

// 分页获取跑步爱好者列表
export const getRunnersByPage = async (page: number, size: number) => {
  const response = await axios.get(`/api/supervisor/runners/page`, {
    params: { page, size }
  });
  return response.data;
};

// 根据条件筛选跑步爱好者
export const getRunnersByFilter = async (filter: Record<string, any>) => {
  const response = await axios.get(`/api/supervisor/runners/filter`, {
    params: filter
  });
  return response.data;
};

// 分配跑步爱好者到督导员
export const assignRunnerToSupervisor = async (runnerId: number, supervisorId: number) => {
  const response = await axios.put(`/api/supervisor/runners/${runnerId}/supervisor/${supervisorId}`);
  return response.data;
};

// 获取督导员的跑步爱好者列表
export const getSupervisorRunners = async (supervisorId: number) => {
  const response = await axios.get(`/api/supervisor/runners/supervisor/${supervisorId}`);
  return response.data;
};

// 获取当前督导员分配的跑步爱好者列表
export const getAssignedRunners = async () => {
  try {
    const response = await axios.get('/api/supervisor/runners/assigned');
    return response.data;
  } catch (error) {
    console.error('获取分配的跑步爱好者列表失败:', error);
    throw error;
  }
};

// 跑步爱好者类型声明
export interface Runner {
  id: number;
  username: string;
  name: string;
  realName: string;
  runnerId: string;
  userNumber: string;
  email: string;
  phone: string;
  status: string;
  createTime: string;
  roles: string[];
} 