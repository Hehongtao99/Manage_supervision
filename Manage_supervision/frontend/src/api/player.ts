import axios from '../utils/axios';

export interface PlayerDTO {
  id: number;
  name: string;
  playerId: string;
  className: string;
  email: string;
  phone: string;
  status: string;
  lastLogin: string;
  progress: number;
}

export interface PlayerDetailDTO extends PlayerDTO {
  games: GameDTO[];
  activities: ActivityDTO[];
}

export interface GameDTO {
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

// 获取玩家列表（分页）
export const getPlayers = async (page = 1, size = 10, keyword?: string) => {
  try {
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    let url = '/api/admin/players'; // 默认管理员API
    
    // 如果是陪玩角色，使用陪玩API
    if (userRoles.includes('SUPERVISOR')) {
      url = '/api/companion/players/assigned';
    }
    
    const params: any = { page, size };
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await axios.get(url, { params });
    return response.data;
  } catch (error) {
    console.error('获取玩家列表失败:', error);
    throw error;
  }
};

// 获取未分配的玩家列表
export const getUnassignedPlayers = async () => {
  try {
    const response = await axios.get('/api/admin/players/unassigned');
    return response.data;
  } catch (error) {
    console.error('获取未分配玩家列表失败:', error);
    throw error;
  }
};

// 获取玩家详情
export const getPlayerDetail = async (playerId: number) => {
  try {
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    console.log('当前用户角色:', userRoles);
    
    // 默认使用陪玩API，因为这是PlayerManagement.vue是陪玩视图
    const url = `/api/companion/players/${playerId}`;
    console.log('使用API路径:', url);
    
    // 保存原始错误以便在处理失败时显示
    let originalError = null;
    
    try {
      const response = await axios.get(url);
      console.log('获取玩家详情成功:', response.data);
      return response.data;
    } catch (error) {
      console.warn('通过陪玩API获取玩家详情失败，尝试使用玩家列表数据:', error);
      originalError = error;
      
      // 尝试从已获取的玩家列表中找到该玩家
      const assignedResponse = await axios.get('/api/companion/players/assigned');
      if (Array.isArray(assignedResponse.data)) {
        const player = assignedResponse.data.find(s => s.id === playerId);
        if (player) {
          console.log('从已获取的玩家列表中找到玩家:', player);
          return player;
        }
      }
      
      // 如果仍未找到，抛出原始错误
      throw originalError;
    }
  } catch (error) {
    console.error('获取玩家详情失败:', error);
    throw error;
  }
};

// 更新玩家状态
export const updatePlayerStatus = async (playerId: number, status: string) => {
  try {
    const response = await axios.post(`/api/admin/users/${playerId}/toggle-status`);
    return true;
  } catch (error) {
    console.error('更新玩家状态失败:', error);
    throw error;
  }
};

// 删除玩家
export const deletePlayer = async (playerId: number) => {
  const response = await axios.delete(`/api/companion/players/${playerId}`);
  return response.status === 200;
};

// 分页获取玩家列表
export const getPlayersByPage = async (page: number, size: number) => {
  const response = await axios.get(`/api/companion/players/page`, {
    params: { page, size }
  });
  return response.data;
};

// 根据条件筛选玩家
export const getPlayersByFilter = async (filter: Record<string, any>) => {
  const response = await axios.get(`/api/companion/players/filter`, {
    params: filter
  });
  return response.data;
};

// 添加玩家（分配到陪玩）
export const assignPlayerToCompanion = async (playerId: number, companionId: number) => {
  const response = await axios.put(`/api/companion/players/${playerId}/companion/${companionId}`);
  return response.data;
};

// 获取陪玩的玩家列表
export const getCompanionPlayers = async (companionId: number) => {
  const response = await axios.get(`/api/companion/players/companion/${companionId}`);
  return response.data;
};

// 添加获取当前陪玩分配的玩家列表的API函数
export const getAssignedPlayers = async () => {
  try {
    const response = await axios.get('/api/companion/players/assigned');
    return response.data;
  } catch (error) {
    console.error('获取分配的玩家列表失败:', error);
    throw error;
  }
};

// 接口为用户类型声明
export interface Player {
  id: number;
  username: string;
  name: string;
  realName: string;
  playerId: string;
  userNumber: string;
  email: string;
  phone: string;
  status: string;
  createTime: string;
  roles: string[];
} 