import axios from 'axios'

/**
 * 主机信息接口
 */
export interface Host {
  id: number
  hostname: string
  ip: string
  os: string
  cpuModel: string
  cpuCores: number
  memoryTotal: number
  diskTotal: number
  status: string
  lastUpdateTime: string
  description?: string
  userId?: number
  cpuUsage?: number[]
  memoryUsage?: number[]
  networkTraffic?: {
    input: number[]
    output: number[]
  }
}

/**
 * 分页请求接口
 */
export interface PageRequest {
  page?: number
  pageSize?: number
  query?: string
  sortBy?: string
  sortOrder?: string
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * 主机管理API服务
 */
export default {
  /**
   * 获取主机列表
   */
  getHosts(params: PageRequest): Promise<PageResponse<Host>> {
    return axios.get('/api/hosts', { params })
      .then(response => response.data);
  },

  /**
   * 获取主机详情
   */
  getHostById(id: number): Promise<Host> {
    return axios.get(`/api/hosts/${id}`)
      .then(response => response.data);
  },
  
  /**
   * 获取当前主机真实信息
   */
  getCurrentHostInfo(): Promise<Host> {
    return axios.get('/api/hosts/current')
      .then(response => response.data);
  },

  /**
   * 添加主机
   */
  addHost(data: Partial<Host>): Promise<Host> {
    return axios.post('/api/hosts', data)
      .then(response => response.data);
  },

  /**
   * 更新主机信息
   */
  updateHost(id: number, data: Partial<Host>): Promise<Host> {
    return axios.put(`/api/hosts/${id}`, data)
      .then(response => response.data);
  },

  /**
   * 删除主机
   */
  deleteHost(id: number): Promise<void> {
    return axios.delete(`/api/hosts/${id}`)
      .then(() => {});
  },

  /**
   * 根据用户ID获取主机列表
   */
  getHostsByUserId(userId: number): Promise<Host[]> {
    return axios.get(`/api/hosts/user/${userId}`)
      .then(response => response.data);
  },

  /**
   * 根据状态获取主机列表
   */
  getHostsByStatus(status: string): Promise<Host[]> {
    return axios.get(`/api/hosts/status/${status}`)
      .then(response => response.data);
  },

  /**
   * 检查主机名是否存在
   */
  isHostnameExists(hostname: string): Promise<boolean> {
    return axios.get(`/api/hosts/check/hostname/${hostname}`)
      .then(response => response.data);
  },

  /**
   * 检查IP是否存在
   */
  isIpExists(ip: string): Promise<boolean> {
    return axios.get(`/api/hosts/check/ip/${ip}`)
      .then(response => response.data);
  }
} 