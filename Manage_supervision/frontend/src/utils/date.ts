/**
 * 将日期时间格式化为标准格式
 * @param dateTime 日期时间字符串或Date对象
 * @returns 格式化后的日期时间字符串，形如：YYYY-MM-DD HH:mm:ss
 */
export function formatDateTime(dateTime: string | Date | null | undefined): string {
  if (!dateTime) return '';
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime;
  
  if (isNaN(date.getTime())) {
    return '';
  }
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 将日期格式化为年月日
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串，形如：YYYY-MM-DD
 */
export function formatDate(date: string | Date | null | undefined): string {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  if (isNaN(dateObj.getTime())) {
    return '';
  }
  
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const day = String(dateObj.getDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
}

/**
 * 计算相对时间（几分钟前、几小时前、几天前等）
 * @param dateTime 日期时间字符串或Date对象
 * @returns 相对时间描述
 */
export function getRelativeTime(dateTime: string | Date | null | undefined): string {
  if (!dateTime) return '';
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime;
  
  if (isNaN(date.getTime())) {
    return '';
  }
  
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚';
  }
  
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`;
  }
  
  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`;
  }
  
  // 小于7天
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`;
  }
  
  // 其他情况返回完整日期
  return formatDate(date);
}