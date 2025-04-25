/**
 * 日期格式化函数
 * @param date 日期对象或日期字符串
 * @param format 格式化模板，默认为'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | string | number, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  const d = date ? new Date(date) : new Date();
  
  if (isNaN(d.getTime())) {
    console.error('无效的日期:', date);
    return String(date); // 如果无效，则返回原始值的字符串表示
  }
  
  const year = d.getFullYear();
  const month = d.getMonth() + 1;
  const day = d.getDate();
  const hours = d.getHours();
  const minutes = d.getMinutes();
  const seconds = d.getSeconds();
  
  const o: Record<string, number | string> = {
    'YYYY': year,
    'MM': month < 10 ? '0' + month : month,
    'DD': day < 10 ? '0' + day : day,
    'HH': hours < 10 ? '0' + hours : hours,
    'mm': minutes < 10 ? '0' + minutes : minutes,
    'ss': seconds < 10 ? '0' + seconds : seconds
  };
  
  return format.replace(/(YYYY|MM|DD|HH|mm|ss)/g, (match) => {
    return String(o[match]);
  });
}

/**
 * 文件大小格式化函数
 * @param bytes 文件大小（字节）
 * @param decimals 小数位数，默认为2
 * @returns 格式化后的文件大小字符串
 */
export function formatFileSize(bytes: number | null | undefined, decimals: number = 2): string {
  if (bytes === null || bytes === undefined) {
    return '0 Bytes';
  }
  
  if (bytes === 0) {
    return '0 Bytes';
  }
  
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
} 