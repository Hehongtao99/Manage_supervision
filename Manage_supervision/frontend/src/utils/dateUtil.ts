/**
 * 格式化日期时间
 * @param dateStr 日期字符串
 * @returns 格式化后的日期字符串
 */
export const formatDateTime = (dateStr: string): string => {
  if (!dateStr) return '';
  
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

/**
 * 格式化日期
 * @param dateStr 日期字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (dateStr: string): string => {
  if (!dateStr) return '';
  
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
};

/**
 * 获取当前日期时间字符串
 * @returns 当前日期时间字符串
 */
export const getNow = (): string => {
  return formatDateTime(new Date().toISOString());
};

/**
 * 格式化时间差为人类可读的格式
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 人类可读的时间差
 */
export const formatDateDiff = (startDate: string, endDate: string): string => {
  if (!startDate || !endDate) return '';
  
  const start = new Date(startDate).getTime();
  const end = new Date(endDate).getTime();
  
  if (isNaN(start) || isNaN(end)) return '';
  
  const diffInSeconds = Math.abs(end - start) / 1000;
  
  // 小于1分钟
  if (diffInSeconds < 60) {
    return '刚刚';
  }
  
  // 小于1小时
  if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60);
    return `${minutes}分钟`;
  }
  
  // 小于1天
  if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600);
    return `${hours}小时`;
  }
  
  // 小于1个月
  if (diffInSeconds < 2592000) {
    const days = Math.floor(diffInSeconds / 86400);
    return `${days}天`;
  }
  
  // 小于1年
  if (diffInSeconds < 31536000) {
    const months = Math.floor(diffInSeconds / 2592000);
    return `${months}个月`;
  }
  
  // 大于等于1年
  const years = Math.floor(diffInSeconds / 31536000);
  return `${years}年`;
}; 