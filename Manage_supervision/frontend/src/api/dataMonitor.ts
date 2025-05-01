import axios from '../utils/axios';

interface MonitorDataQuery {
  startDate?: string;
  endDate?: string;
  userId?: number;
  threshold?: number;
  anomalyOnly?: boolean;
}

/**
 * 获取用户监控数据
 */
export const getUserMonitorData = (params: MonitorDataQuery) => {
  return axios.get('/api/data-monitor/user-data', { params });
};

/**
 * 获取异常数据
 */
export const getAnomalyData = (params: MonitorDataQuery) => {
  return axios.get('/api/data-monitor/anomaly', { params });
};

/**
 * 获取数据监控统计信息
 */
export const getMonitorStats = () => {
  return axios.get('/api/data-monitor/stats');
};

/**
 * 导出数据监控报告
 */
export const exportMonitorReport = (params: MonitorDataQuery) => {
  return axios.get('/api/data-monitor/export', { 
    params,
    responseType: 'blob'
  });
};

/**
 * 标记异常数据为已处理
 */
export const markAnomalyAsResolved = (id: number) => {
  return axios.post(`/api/data-monitor/anomaly/${id}/resolve`);
}; 