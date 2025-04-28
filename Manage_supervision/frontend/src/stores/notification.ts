import { defineStore } from 'pinia';
import type { NotificationDTO } from '../types/notification';
import notificationService from '../services/notification';

interface NotificationState {
  notifications: NotificationDTO[];
  loading: boolean;
  currentNotification: NotificationDTO | null;
}

export const useNotificationStore = defineStore('notification', {
  state: (): NotificationState => ({
    notifications: [],
    loading: false,
    currentNotification: null
  }),

  getters: {
    // 获取按时间排序的通知
    sortedNotifications: (state) => {
      return [...state.notifications].sort((a, b) => {
        const timeA = new Date(a.createTime).getTime();
        const timeB = new Date(b.createTime).getTime();
        return timeB - timeA; // 降序，最新的在前
      });
    },
    
    // 获取通知数量
    notificationCount: (state) => state.notifications.length
  },

  actions: {
    // 加载收到的通知
    async loadReceivedNotifications() {
      this.loading = true;
      try {
        const notifications = await notificationService.getReceivedNotifications();
        this.notifications = notifications;
      } catch (error) {
        console.error('加载收到的通知失败:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 加载发送的通知
    async loadSentNotifications() {
      this.loading = true;
      try {
        const notifications = await notificationService.getSentNotifications();
        this.notifications = notifications;
      } catch (error) {
        console.error('加载发送的通知失败:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 添加新通知
    addNotification(notification: NotificationDTO) {
      // 检查通知是否已存在
      const exists = this.notifications.some(n => n.id === notification.id);
      if (!exists) {
        this.notifications.unshift(notification);
      }
    },
    
    // 发送通知
    async sendNotification(title: string, content: string, recipientIds: number[]) {
      try {
        const notification = await notificationService.sendNotification(title, content, recipientIds);
        this.addNotification(notification);
        return notification;
      } catch (error) {
        console.error('发送通知失败:', error);
        throw error;
      }
    },
    
    // 获取通知详情
    async getNotificationDetail(id: number) {
      try {
        const notification = await notificationService.getNotificationDetail(id);
        if (notification) {
          this.currentNotification = notification;
        }
        return notification;
      } catch (error) {
        console.error('获取通知详情失败:', error);
        return null;
      }
    },
    
    // 清空通知
    clearNotifications() {
      this.notifications = [];
      this.currentNotification = null;
    }
  }
}); 