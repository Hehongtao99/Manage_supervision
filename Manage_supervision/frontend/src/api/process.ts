import axios from 'axios';

/**
 * 系统进程信息接口
 */
export interface SystemProcess {
  pid: number;           // 进程ID
  name: string;          // 进程名称
  cpuUsage: number;      // CPU占用率
  memoryUsage: number;   // 内存占用(KB)
  startTime: string;     // 启动时间
  command: string;       // 命令行
}

/**
 * 分页响应接口
 */
export interface PageResponse {
  processes: SystemProcess[]; // 进程列表
  currentPage: number;       // 当前页码
  totalPages: number;        // 总页数
  totalItems: number;        // 总项目数
}

/**
 * 获取所有进程列表
 * @returns Promise 包含进程列表
 */
export const getProcessList = async (): Promise<SystemProcess[]> => {
  const response = await axios.get('/api/system/process/list');
  return response.data;
};

/**
 * 分页获取进程列表
 * @param page 页码（从0开始）
 * @param size 每页大小
 * @returns Promise 包含分页响应
 */
export const getProcessListPaged = async (page: number = 0, size: number = 20): Promise<PageResponse> => {
  const response = await axios.get('/api/system/process/page', {
    params: { page, size }
  });
  return response.data;
};

/**
 * 获取CPU或内存占用最高的N个进程
 * @param limit 最大返回数量
 * @param sortBy 排序字段（'cpu'或'memory'）
 * @returns Promise 包含进程列表
 */
export const getTopProcesses = async (limit: number = 10, sortBy: 'cpu' | 'memory' = 'cpu'): Promise<SystemProcess[]> => {
  const response = await axios.get('/api/system/process/top', {
    params: { limit, sortBy }
  });
  return response.data;
};

/**
 * 获取指定进程详情
 * @param pid 进程ID
 * @returns Promise 包含进程详情
 */
export const getProcessById = async (pid: number): Promise<SystemProcess> => {
  const response = await axios.get(`/api/system/process/${pid}`);
  return response.data;
};

/**
 * 根据名称搜索进程
 * @param name 进程名称
 * @returns Promise 包含进程列表
 */
export const searchProcessByName = async (name: string): Promise<SystemProcess[]> => {
  const response = await axios.get('/api/system/process/search', {
    params: { name }
  });
  return response.data;
}; 