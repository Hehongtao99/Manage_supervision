/**
 * 格式化日期时间
 * @param dateStr 日期字符串
 * @param format 格式化模板，默认为 'YYYY-MM-DD HH:mm'
 * @returns 格式化后的日期字符串
 */
export const formatDate = (dateStr?: string, format: string = 'YYYY-MM-DD HH:mm'): string => {
  if (!dateStr) return '';
  
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return dateStr;
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}; 