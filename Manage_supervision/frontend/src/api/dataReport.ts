import axios from '../utils/axios';

interface DataReportQuery {
  reporterId?: number;
  handlerId?: number;
  status?: number;
  monitorDataId?: number;
  severity?: number;
  startDate?: string;
  endDate?: string;
  keyword?: string;
  page?: number;
  size?: number;
}

/**
 * 提交数据上报
 */
export const submitDataReport = (data: {
  monitorDataId?: number;
  title: string;
  description?: string;
  severity: number;
}) => {
  return axios.post('/api/data-report/submit', data);
};

/**
 * 获取数据上报列表
 */
export const getDataReports = (params: DataReportQuery) => {
  return axios.get('/api/data-report/list', { params });
};

/**
 * 获取数据上报详情
 */
export const getDataReportDetail = (id: number) => {
  return axios.get(`/api/data-report/${id}`);
};

/**
 * 处理数据上报
 */
export const handleDataReport = (id: number, status: number, resolution?: string) => {
  return axios.post(`/api/data-report/${id}/handle`, null, {
    params: { status, resolution }
  });
};

/**
 * 获取我提交的上报
 */
export const getMyReports = (page: number = 0, size: number = 10) => {
  return axios.get('/api/data-report/my-reports', {
    params: { page, size }
  });
};

/**
 * 获取待处理上报数量
 */
export const getPendingReportCount = () => {
  return axios.get('/api/data-report/pending-count');
};

/**
 * 获取上报状态统计
 */
export const getReportStats = () => {
  return axios.get('/api/data-report/stats');
};

/**
 * 获取最近上报列表
 */
export const getRecentReports = () => {
  return axios.get('/api/data-report/recent');
}; 