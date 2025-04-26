// services/message.js
const { get, post, put } = require('../utils/request');

/**
 * 获取家长的所有留言列表（分页）
 * @param {number} page 页码（从0开始）
 * @param {number} size 每页条数
 * @returns {Promise} 返回Promise对象
 */
const getParentMessages = (page = 0, size = 10) => {
  return get(`/messages/parent-messages?page=${page}&size=${size}`);
};

/**
 * 获取家长针对特定学生的留言列表（分页）
 * @param {number} studentId 学生ID
 * @param {number} page 页码（从0开始）
 * @param {number} size 每页条数
 * @returns {Promise} 返回Promise对象
 */
const getParentMessagesForStudent = (studentId, page = 0, size = 10) => {
  return get(`/messages/parent-messages/student/${studentId}?page=${page}&size=${size}`);
};

/**
 * 发送留言给教师
 * @param {number} teacherId 教师ID
 * @param {number} studentId 学生ID
 * @param {string} content 留言内容
 * @returns {Promise} 返回Promise对象
 */
const sendMessage = (teacherId, studentId, content) => {
  return post('/messages', {
    teacherId,
    studentId,
    content
  });
};

/**
 * 获取留言详情
 * @param {number} messageId 留言ID
 * @returns {Promise} 返回Promise对象
 */
const getMessageDetails = (messageId) => {
  // 由于后端没有提供单个留言详情的API，我们需要获取所有留言，然后在前端筛选
  return new Promise((resolve, reject) => {
    // 先尝试从本地缓存获取
    const cachedMessages = wx.getStorageSync('cached_messages');
    if (cachedMessages && Array.isArray(cachedMessages)) {
      const message = cachedMessages.find(msg => msg.id == messageId);
      if (message) {
        console.log('从缓存中获取留言详情:', message);
        return resolve(message);
      }
    }
    
    // 如果缓存中没有，则从服务器获取列表
    getParentMessages(0, 50)
      .then(res => {
        let messages = [];
        if (res.content && Array.isArray(res.content)) {
          messages = res.content;
        } else if (Array.isArray(res)) {
          messages = res;
        } else if (res.data) {
          if (res.data.content && Array.isArray(res.data.content)) {
            messages = res.data.content;
          } else if (Array.isArray(res.data)) {
            messages = res.data;
          }
        }
        
        // 保存到缓存
        wx.setStorageSync('cached_messages', messages);
        
        // 查找指定ID的留言
        const message = messages.find(msg => msg.id == messageId);
        if (message) {
          resolve(message);
        } else {
          reject({ message: '找不到指定ID的留言' });
        }
      })
      .catch(err => {
        reject(err);
      });
  });
};

/**
 * 标记所有回复为已读
 * @returns {Promise} 返回Promise对象
 */
const markAllRepliesAsRead = () => {
  return put('/messages/parent-messages/mark-all-replies-read');
};

module.exports = {
  getParentMessages,
  getParentMessagesForStudent,
  sendMessage,
  getMessageDetails,
  markAllRepliesAsRead
}; 