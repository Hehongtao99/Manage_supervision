/**
 * 格式化日期时间
 * @param dateString 日期字符串
 * @returns 格式化后的日期时间字符串
 */
export const formatDateTime = (dateString: string | undefined | null): string => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error('Date formatting error:', error);
    return dateString;
  }
};

/**
 * 格式化日期（不包含时间）
 * @param dateString 日期字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (dateString: string | undefined | null): string => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (error) {
    console.error('Date formatting error:', error);
    return dateString;
  }
};

/**
 * 格式化时间差（相对时间）
 * @param dateString 日期字符串
 * @returns 格式化后的相对时间
 */
export const formatRelativeTime = (dateString: string | undefined | null): string => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    const now = new Date();
    const diffSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    
    if (diffSeconds < 60) {
      return '刚刚';
    } else if (diffSeconds < 3600) {
      return `${Math.floor(diffSeconds / 60)}分钟前`;
    } else if (diffSeconds < 86400) {
      return `${Math.floor(diffSeconds / 3600)}小时前`;
    } else if (diffSeconds < 2592000) {
      return `${Math.floor(diffSeconds / 86400)}天前`;
    } else if (diffSeconds < 31536000) {
      return `${Math.floor(diffSeconds / 2592000)}个月前`;
    } else {
      return `${Math.floor(diffSeconds / 31536000)}年前`;
    }
  } catch (error) {
    console.error('Relative time formatting error:', error);
    return dateString;
  }
}; 