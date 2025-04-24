import axios from 'axios';

// 定义AxiosResponse接口，因为新版本的axios可能不直接导出它
export interface AxiosResponse<T = any> {
  data: T;
  status: number;
  statusText: string;
  headers: Record<string, string>;
  config: any;
  request?: any;
}

// 扩展axios模块
declare module 'axios' {
  export interface AxiosResponse<T = any> {
    data: T;
    status: number;
    statusText: string;
    headers: Record<string, string>;
    config: any;
    request?: any;
  }
}

export default axios; 