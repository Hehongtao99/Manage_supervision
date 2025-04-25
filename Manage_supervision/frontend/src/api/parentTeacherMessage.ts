import request from '@/utils/request';

/**
 * 获取家长的所有留言列表（分页）
 * @param page 页码
 * @param size 每页条数
 * @returns 留言列表和分页信息
 */
export function getParentMessages(page: number, size: number) {
  return request({
    url: '/api/messages/parent-messages',
    method: 'get',
    params: {
      page,
      size
    },
    transformResponse: [(data: any) => {
      // 添加响应转换，确保正确解析
      try {
        const parsedData = JSON.parse(data);
        console.log('家长留言API响应数据解析结果:', parsedData);
        return parsedData;
      } catch (error) {
        console.error('家长留言API响应数据解析失败:', error);
        return data;
      }
    }]
  });
}

/**
 * 获取家长针对特定学生的留言列表（分页）
 * @param studentId 学生ID
 * @param page 页码
 * @param size 每页条数
 * @returns 留言列表和分页信息
 */
export function getParentMessagesByStudent(studentId: number, page: number, size: number) {
  return request({
    url: `/api/parent-messages/student/${studentId}`,
    method: 'get',
    params: {
      page,
      size
    }
  });
}

/**
 * 发送留言给教师
 * @param teacherId 教师ID
 * @param studentId 学生ID
 * @param content 留言内容
 * @returns 发送结果
 */
export function sendMessage(teacherId: number, studentId: number, content: string) {
  return request({
    url: '/api/messages',
    method: 'post',
    data: {
      teacherId,
      studentId,
      content
    }
  });
}

/**
 * 获取教师收到的所有留言列表（分页）
 * @param page 页码
 * @param size 每页条数
 * @returns 留言列表和分页信息
 */
export function getTeacherMessages(page: number, size: number) {
  return request({
    url: '/api/teacher-messages',
    method: 'get',
    params: {
      page,
      size
    },
    transformResponse: [(data: any) => {
      // 添加响应转换，确保正确解析
      try {
        const parsedData = JSON.parse(data);
        console.log('API响应数据解析结果:', parsedData);
        return parsedData;
      } catch (error) {
        console.error('API响应数据解析失败:', error);
        return data;
      }
    }]
  });
}

/**
 * 获取教师收到的关于特定学生的留言列表（分页）
 * @param studentId 学生ID
 * @param page 页码
 * @param size 每页条数
 * @returns 留言列表和分页信息
 */
export function getTeacherMessagesByStudent(studentId: number, page: number, size: number) {
  return request({
    url: `/api/teacher-messages/student/${studentId}`,
    method: 'get',
    params: {
      page,
      size
    }
  });
}

/**
 * 教师回复家长留言
 * @param messageId 留言ID
 * @param replyContent 回复内容
 * @returns 回复结果
 */
export function replyMessage(messageId: number, replyContent: string) {
  return request({
    url: `/api/teacher-messages/${messageId}/reply`,
    method: 'post',
    data: {
      replyContent
    }
  });
}

/**
 * 教师标记所有留言为已读
 * @returns 操作结果
 */
export function markAllAsReadByTeacher() {
  return request({
    url: '/api/teacher-messages/mark-all-read',
    method: 'put'
  });
}

/**
 * 家长标记所有回复为已读
 * @returns 操作结果
 */
export function markAllRepliesAsReadByParent() {
  return request({
    url: '/api/messages/parent-messages/mark-all-replies-read',
    method: 'put'
  });
}

/**
 * 统计教师未读留言数量
 * @returns 未读留言数量
 */
export function countUnreadMessagesByTeacher() {
  return request({
    url: '/api/teacher-messages/count-unread',
    method: 'get'
  });
}

/**
 * 统计家长未读回复数量
 * @returns 未读回复数量
 */
export function countUnreadRepliesByParent() {
  return request({
    url: '/api/parent-messages/count-unread-replies',
    method: 'get'
  });
} 