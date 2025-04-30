/**
 * 系统日志条目接口
 */
export interface SystemLogEntry {
  id: number;
  timestamp: string;
  level: string;
  message: string;
  source: string;
  threadName?: string;
  logger?: string;
}

/**
 * 操作日志条目接口
 */
export interface OperationLogEntry {
  id: number;
  userId: number;
  username: string;
  operation: string;
  method: string;
  params: string;
  result: string;
  ip: string;
  userAgent: string;
  module: string;
  operationTime: string;
}

/**
 * 分页结果接口
 */
export interface PaginationResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
} 