// services/parent.js
const { get, post, delete: del } = require('../utils/request');

/**
 * 获取家长的子女列表
 * @returns {Promise} 返回Promise对象
 */
const getChildren = () => {
  return get('/parent/children', {}, {}, 3);
};

/**
 * 获取子女详情
 * @param {number} childId 子女ID
 * @returns {Promise} 返回Promise对象
 */
const getChildDetails = (childId) => {
  return get(`/parent/children/${childId}/details`);
};

/**
 * 关联子女账号
 * @param {string} childIdentifier 子女标识符（用户名或学号）
 * @param {string} relationType 关系类型（father/mother/guardian）
 * @returns {Promise} 返回Promise对象
 */
const relateChild = (childIdentifier, relationType) => {
  return post('/parent/children/relate', {
    childIdentifier,
    relationType
  });
};

/**
 * 解除与子女的关联
 * @param {number} relationId 关联ID
 * @returns {Promise} 返回Promise对象
 */
const removeRelation = (relationId) => {
  return del(`/parent/children/${relationId}`);
};

/**
 * 获取子女的课程和任务
 * @param {number} childId 子女ID
 * @returns {Promise} 返回Promise对象
 */
const getChildCoursesAndTasks = (childId) => {
  return get(`/parent/children/${childId}/courses-tasks`);
};

/**
 * 获取家长通知
 * @returns {Promise} 返回Promise对象
 */
const getNotifications = () => {
  return get('/parent/notifications', {}, {}, 3);
};

/**
 * 获取通知详情
 * @param {number} notificationId 通知ID
 * @returns {Promise} 返回Promise对象
 */
const getNotificationDetail = (notificationId) => {
  return get(`/parent/notifications/${notificationId}`);
};

/**
 * 标记通知为已读
 * @param {number} notificationId 通知ID
 * @returns {Promise} 返回Promise对象
 */
const markNotificationAsRead = (notificationId) => {
  return post(`/parent/notifications/${notificationId}/read`);
};

/**
 * 获取子女的时间表
 * @param {number} childId 子女ID
 * @param {number} weekNumber 周数
 * @returns {Promise} 返回Promise对象
 */
const getChildTimetable = (childId, weekNumber) => {
  return get(`/parent/child/timetable/${childId}/${weekNumber}`);
};

/**
 * 获取家长仪表盘数据
 * @returns {Promise} 返回Promise对象
 */
const getDashboardData = () => {
  return get('/parent/dashboard', {}, {}, 3);
};

/**
 * 获取子女考勤记录
 * @param {number} childId 子女ID
 * @param {string} startDate 开始日期（可选，格式：YYYY-MM-DD）
 * @param {string} endDate 结束日期（可选，格式：YYYY-MM-DD）
 * @returns {Promise} 返回Promise对象
 */
const getChildAttendance = (childId, startDate, endDate) => {
  let url = `/parent/children/${childId}/attendance`;
  
  if (startDate && endDate) {
    url += `?startDate=${startDate}&endDate=${endDate}`;
  }
  
  return get(url);
};

/**
 * 获取子女作业列表
 * @param {number} childId 子女ID
 * @param {string} status 作业状态（可选，all/pending/completed）
 * @returns {Promise} 返回Promise对象
 */
const getChildHomework = (childId, status = 'all') => {
  return get(`/parent/children/${childId}/homework?status=${status}`);
};

/**
 * 获取作业详情
 * @param {number} homeworkId 作业ID
 * @returns {Promise} 返回Promise对象
 */
const getHomeworkDetails = (homeworkId) => {
  return get(`/parent/homework/${homeworkId}`);
};

/**
 * 确认已查看作业
 * @param {number} homeworkId 作业ID
 * @returns {Promise} 返回Promise对象
 */
const confirmHomeworkChecked = (homeworkId) => {
  return post(`/parent/homework/${homeworkId}/confirm`);
};

/**
 * 获取子女成绩列表
 * @param {number} childId 子女ID
 * @param {string} semester 学期（可选）
 * @returns {Promise} 返回Promise对象
 */
const getChildGrades = (childId, semester = '') => {
  let url = `/parent/children/${childId}/grades`;
  if (semester) {
    url += `?semester=${semester}`;
  }
  return get(url);
};

/**
 * 获取子女单次考试成绩详情
 * @param {number} examId 考试ID
 * @returns {Promise} 返回Promise对象
 */
const getExamDetails = (examId) => {
  return get(`/parent/exam/${examId}`);
};

/**
 * 获取子女统计信息
 * @param {number} childId 子女ID
 * @returns {Promise} 返回Promise对象
 */
const getChildStats = (childId) => {
  return get(`/parent/children/${childId}/stats`);
};

/**
 * 获取学生的教师列表
 * @param {number} childId 孩子ID
 * @returns {Promise} 返回Promise对象
 */
const getChildTeachers = (childId) => {
  return get(`/parent/students/${childId}/teachers`);
};

/**
 * 搜索学生
 * @param {string} keyword 搜索关键词
 * @returns {Promise} 返回Promise对象
 */
const searchStudents = (keyword) => {
  return get(`/parent/search-students?keyword=${encodeURIComponent(keyword)}`);
};

module.exports = {
  getChildren,
  getChildDetails,
  relateChild,
  removeRelation,
  getChildCoursesAndTasks,
  getNotifications,
  getNotificationDetail,
  markNotificationAsRead,
  getChildTimetable,
  getDashboardData,
  getChildAttendance,
  getChildHomework,
  getHomeworkDetails,
  confirmHomeworkChecked,
  getChildGrades,
  getExamDetails,
  getChildStats,
  getChildTeachers,
  searchStudents
}; 