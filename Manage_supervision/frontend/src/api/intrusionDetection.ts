import axios from 'axios';

/**
 * 入侵检测配置接口
 */
interface DetectionConfig {
  /**
   * 检测类型
   */
  type: string;
  
  /**
   * 异常阈值（可选）
   */
  threshold?: number;
}

interface DetectionResult {
  id?: number;
  dataType: string;
  value: number;
  expectedMin: number;
  expectedMax: number;
  deviation: number;
  isAnomaly: boolean;
  recordTime: string;
  description?: string;
  source?: string;
}

/**
 * 执行入侵检测
 */
export const detectIntrusion = (config: DetectionConfig) => {
  const token = localStorage.getItem('token');
  return axios.post('/api/intrusion-detection/detect', config, {
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    }
  });
};

/**
 * 将检测结果添加到监控数据
 */
export const addDetectionToMonitor = (anomalyIds: number[]) => {
  const token = localStorage.getItem('token');
  return axios.post('/api/intrusion-detection/add-to-monitor', { anomalyIds }, {
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    }
  });
};

/**
 * 获取最近的入侵检测记录
 */
export const getRecentDetections = (limit: number = 10) => {
  const token = localStorage.getItem('token');
  return axios.get('/api/intrusion-detection/recent', {
    params: { limit },
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    }
  });
};

/**
 * 获取检测统计信息
 */
export const getDetectionStats = () => {
  return axios.get('/api/intrusion-detection/stats');
}; 