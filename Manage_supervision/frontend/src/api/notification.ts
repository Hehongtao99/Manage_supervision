import request from '@/utils/request';
import { getCurrentUserId } from '@/utils/auth';

export interface NotificationRequest {
  title: string;
  content: string;
  recipientType: 'ALL' | 'CLASS' | 'PARENT' | 'STUDENT' | 'TEACHER';
  classId?: number;
}

export interface NotificationResponse {
  id: number;
  title: string;
  content: string;
  senderId: number;
  senderName: string;
  senderAvatar: string;
  recipientType: string;
  classId?: number;
  className?: string;
  createTime: string;
  isGlobal: boolean;
}

/**
 * 创建通知
 */
export function createNotification(data: NotificationRequest) {
  const userId = getCurrentUserId();
  console.log('发送通知，当前用户ID:', userId);
  console.log('通知数据:', data);
  
  if (!userId) {
    console.error('发送通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法发送通知'));
  }
  
  return request({
    url: '/api/notifications/create',
    method: 'post',
    data,
    headers: {
      'userId': userId
    }
  });
}

/**
 * 获取当前用户收到的通知
 */
export function getReceivedNotifications() {
  const userId = getCurrentUserId();
  if (!userId) {
    console.error('获取通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法获取通知'));
  }
  
  return request({
    url: '/api/notifications/received',
    method: 'get',
    headers: {
      'userId': userId
    }
  });
}

/**
 * 获取当前用户发送的通知
 */
export function getSentNotifications() {
  const userId = getCurrentUserId();
  if (!userId) {
    console.error('获取已发送通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法获取已发送通知'));
  }
  
  return request({
    url: '/api/notifications/sent',
    method: 'get',
    headers: {
      'userId': userId
    }
  });
}

/**
 * 获取所有通知（管理员）
 */
export function getAllNotifications() {
  const userId = getCurrentUserId();
  if (!userId) {
    console.error('获取所有通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法获取所有通知'));
  }
  
  return request({
    url: '/api/notifications/all',
    method: 'get',
    headers: {
      'userId': userId
    }
  });
}

/**
 * 管理员给所有家长发送通知
 */
export function sendToAllParents(data: NotificationRequest) {
  const userId = getCurrentUserId();
  console.log('发送给所有家长的通知，当前用户ID:', userId);
  console.log('通知数据:', data);
  
  if (!userId) {
    console.error('发送通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法发送通知'));
  }
  
  return request({
    url: '/api/notifications/send-to-all-parents',
    method: 'post',
    data,
    headers: {
      'userId': userId
    }
  });
}

/**
 * 管理员给所有学生发送通知
 */
export function sendToAllStudents(data: NotificationRequest) {
  const userId = getCurrentUserId();
  console.log('发送给所有学生的通知，当前用户ID:', userId);
  console.log('通知数据:', data);
  
  if (!userId) {
    console.error('发送通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法发送通知'));
  }
  
  return request({
    url: '/api/notifications/send-to-all-students',
    method: 'post',
    data,
    headers: {
      'userId': userId
    }
  });
}

/**
 * 管理员给所有教师发送通知
 */
export function sendToAllTeachers(data: NotificationRequest) {
  const userId = getCurrentUserId();
  console.log('发送给所有教师的通知，当前用户ID:', userId);
  console.log('通知数据:', data);
  
  if (!userId) {
    console.error('发送通知失败: 未获取到用户ID');
    return Promise.reject(new Error('未获取到用户ID，无法发送通知'));
  }
  
  return request({
    url: '/api/notifications/send-to-all-teachers',
    method: 'post',
    data,
    headers: {
      'userId': userId
    }
  });
} 