import request from '@/utils/request';
import { getCurrentUserId } from '@/utils/auth';

export interface NotificationRequest {
  title: string;
  content: string;
  recipientType: 'ALL' | 'CLASS';
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
  return request({
    url: '/api/notifications/create',
    method: 'post',
    data,
    headers: {
      'userId': getCurrentUserId()
    }
  });
}

/**
 * 获取当前用户收到的通知
 */
export function getReceivedNotifications() {
  return request({
    url: '/api/notifications/received',
    method: 'get',
    headers: {
      'userId': getCurrentUserId()
    }
  });
}

/**
 * 获取当前用户发送的通知
 */
export function getSentNotifications() {
  return request({
    url: '/api/notifications/sent',
    method: 'get',
    headers: {
      'userId': getCurrentUserId()
    }
  });
}

/**
 * 获取所有通知（管理员）
 */
export function getAllNotifications() {
  return request({
    url: '/api/notifications/all',
    method: 'get',
    headers: {
      'userId': getCurrentUserId()
    }
  });
}

/**
 * 管理员给所有家长发送通知
 */
export function sendToAllParents(data: NotificationRequest) {
  return request({
    url: '/api/notifications/send-to-all-parents',
    method: 'post',
    data,
    headers: {
      'userId': getCurrentUserId()
    }
  });
}

/**
 * 管理员给所有学生发送通知
 */
export function sendToAllStudents(data: NotificationRequest) {
  return request({
    url: '/api/notifications/send-to-all-students',
    method: 'post',
    data,
    headers: {
      'userId': getCurrentUserId()
    }
  });
}

/**
 * 管理员给所有教师发送通知
 */
export function sendToAllTeachers(data: NotificationRequest) {
  return request({
    url: '/api/notifications/send-to-all-teachers',
    method: 'post',
    data,
    headers: {
      'userId': getCurrentUserId()
    }
  });
} 