/**
 * 此文件用于兼容性导出，重新导出 date.ts 中的函数
 */

// 从 date.ts 中导入函数
import { formatDateTime, formatDate, getRelativeTime } from './date';

// 导出文件大小格式化函数
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B';
  
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  
  return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i];
}

// 重新导出日期相关的函数
export { formatDateTime, formatDate, getRelativeTime }; 