/**
 * 日期格式化工具
 */

/**
 * 格式化日期为本地字符串
 * @param dateStr 日期字符串或日期对象
 * @param format 可选的格式化选项
 * @returns 格式化后的日期字符串
 */
export function formatDate(dateStr: string | Date | null | undefined, format?: Intl.DateTimeFormatOptions): string {
  if (!dateStr) return '';
  
  try {
    const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr;
    
    // 默认格式化选项 
    const defaultOptions: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    };
    
    // 使用提供的选项或默认选项
    const options = format || defaultOptions;
    
    return date.toLocaleString('zh-CN', options);
  } catch (error) {
    console.error('日期格式化错误:', error);
    return String(dateStr);
  }
}

/**
 * 格式化日期为YYYY-MM-DD格式
 * @param dateStr 日期字符串或日期对象
 * @returns 格式化后的日期字符串
 */
export function formatDateOnly(dateStr: string | Date | null | undefined): string {
  if (!dateStr) return '';
  
  try {
    const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr;
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    console.error('日期格式化错误:', error);
    return String(dateStr);
  }
}

/**
 * 格式化日期为相对时间（例如：1小时前，2天前等）
 * @param dateStr 日期字符串或日期对象
 * @returns 相对时间字符串
 */
export function formatRelativeTime(dateStr: string | Date | null | undefined): string {
  if (!dateStr) return '';
  
  try {
    const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr;
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);
    const diffMonth = Math.floor(diffDay / 30);
    const diffYear = Math.floor(diffMonth / 12);
    
    if (diffYear > 0) {
      return `${diffYear}年前`;
    } else if (diffMonth > 0) {
      return `${diffMonth}个月前`;
    } else if (diffDay > 0) {
      return `${diffDay}天前`;
    } else if (diffHour > 0) {
      return `${diffHour}小时前`;
    } else if (diffMin > 0) {
      return `${diffMin}分钟前`;
    } else {
      return '刚刚';
    }
  } catch (error) {
    console.error('日期格式化错误:', error);
    return String(dateStr);
  }
} 