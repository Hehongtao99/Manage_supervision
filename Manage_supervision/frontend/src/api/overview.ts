import axios from 'axios';
import type { Host } from './host';
import type { SystemProcess } from './process';

/**
 * 获取资源监控概览数据
 * @returns Promise 包含资源监控概览数据
 */
export const getResourceOverview = async () => {
  const response = await axios.get('/api/resource/overview');
  return response.data;
};

/**
 * 获取资源使用趋势数据
 * @param timeRange 时间范围，'hour'表示最近1小时，'day'表示最近24小时
 * @returns Promise 包含资源使用趋势数据
 */
export const getResourceUsageTrend = async (timeRange: string = 'hour') => {
  const response = await axios.get('/api/resource/trend', {
    params: { timeRange }
  });
  return response.data;
}; 