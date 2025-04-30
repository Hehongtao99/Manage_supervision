import axios from '../utils/axios';
import type { OperationLogEntry, PaginationResult, SystemLogEntry } from '../types/log';

/**
 * 日志API服务
 */
export default {
  /**
   * 获取系统日志
   * @param level 日志级别 (all, info, warning, error)
   * @param timeRange 时间范围 (1h, 24h, 7d, 30d)
   * @returns 日志条目列表
   */
  getSystemLogs(level: string = 'all', timeRange: string = '24h'): Promise<SystemLogEntry[]> {
    return axios.get('/api/logs/system', {
      params: { level, timeRange }
    }).then(response => response.data);
  },
  
  /**
   * 分页查询操作日志
   * @param params 查询参数
   * @returns 分页结果
   */
  getOperationLogs(params: {
    username?: string;
    operation?: string;
    startDate?: string;
    endDate?: string;
    module?: string;
    page?: number;
    size?: number;
  }): Promise<PaginationResult<OperationLogEntry>> {
    return axios.get('/api/logs/operations', {
      params
    }).then(response => response.data);
  },
  
  /**
   * 获取操作日志详情
   * @param id 日志ID
   * @returns 日志详情
   */
  getOperationLogDetail(id: number): Promise<OperationLogEntry> {
    return axios.get(`/api/logs/operations/${id}`).then(response => response.data);
  },
  
  /**
   * 删除指定日期之前的操作日志
   * @param date 删除该日期之前的日志 (YYYY-MM-DD 格式)
   * @returns 删除结果
   */
  deleteOperationLogsBefore(date: string): Promise<{ message: string; count: number }> {
    return axios.delete('/api/logs/operations', {
      params: { date }
    }).then(response => response.data);
  }
}; 