/**
 * 工具函数集合
 */

/**
 * 格式化数字，如果是个位数，前面补0
 * @param {number} n 要格式化的数字
 * @return {string} 格式化后的字符串
 */
const formatNumber = (n) => {
  n = n.toString();
  return n[1] ? n : '0' + n;
};

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {Date} date 日期对象
 * @return {string} 格式化后的日期字符串
 */
const formatDate = (date) => {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  
  return [year, month, day].map(formatNumber).join('-');
};

/**
 * 格式化时间为 HH:MM:SS 格式
 * @param {Date} date 日期对象
 * @return {string} 格式化后的时间字符串
 */
const formatTime = (date) => {
  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();
  
  return [hour, minute, second].map(formatNumber).join(':');
};

/**
 * 格式化日期和时间为 YYYY-MM-DD HH:MM:SS 格式
 * @param {Date} date 日期对象
 * @return {string} 格式化后的日期和时间字符串
 */
const formatDateTime = (date) => {
  return formatDate(date) + ' ' + formatTime(date);
};

/**
 * 计算两个日期之间的天数
 * @param {Date|string} date1 日期1
 * @param {Date|string} date2 日期2
 * @return {number} 天数
 */
const daysBetween = (date1, date2) => {
  const d1 = new Date(date1);
  const d2 = new Date(date2);
  const timeDiff = Math.abs(d2.getTime() - d1.getTime());
  return Math.ceil(timeDiff / (1000 * 3600 * 24));
};

/**
 * 获取当前月份的第一天和最后一天
 * @param {Date} date 日期对象，默认为当前日期
 * @return {Object} 包含第一天和最后一天的对象
 */
const getMonthRange = (date = new Date()) => {
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  
  return {
    firstDay: formatDate(firstDay),
    lastDay: formatDate(lastDay)
  };
};

/**
 * 检查是否为空值（null, undefined, '', NaN）
 * @param {*} value 需要检查的值
 * @return {boolean} 是否为空
 */
const isEmpty = (value) => {
  return value === null || value === undefined || value === '' || (typeof value === 'number' && isNaN(value));
};

module.exports = {
  formatNumber,
  formatDate,
  formatTime,
  formatDateTime,
  daysBetween,
  getMonthRange,
  isEmpty
}; 