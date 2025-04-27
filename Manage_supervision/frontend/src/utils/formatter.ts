/**
 * 日期时间格式化工具函数
 */

/**
 * 格式化日期时间为易读的字符串
 * @param dateTime 日期时间字符串或Date对象
 * @param includeTime 是否包含时间部分，默认为true
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(dateTime: string | Date | null | undefined, includeTime: boolean = true): string {
  if (!dateTime) {
    return '-';
  }
  
  const date = new Date(dateTime);
  if (isNaN(date.getTime())) {
    return '-';
  }
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  if (!includeTime) {
    return `${year}-${month}-${day}`;
  }
  
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 格式化日期为易读的字符串 (不包含时间)
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: string | Date | null | undefined): string {
  return formatDateTime(date, false);
}

/**
 * 格式化剩余时间为易读的字符串
 * @param milliseconds 剩余的毫秒数
 * @returns 格式化后的剩余时间字符串
 */
export function formatRemainingTime(milliseconds: number): string {
  if (milliseconds <= 0) {
    return '00:00:00';
  }
  
  const totalSeconds = Math.floor(milliseconds / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
} 