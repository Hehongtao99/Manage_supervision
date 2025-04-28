import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import { useUserStore } from '@/stores/user';
import { useNotificationStore } from '@/stores/notification';
import axios from '@/utils/axios';
import type { NotificationDTO } from '@/types/notification';
import { ElNotification } from 'element-plus';
import router from '@/router';

class NotificationService {
  private subscriptions: { [key: string]: any } = {};
  
  /**
   * 订阅用户通知
   */
  subscribeToNotifications(client: Client) {
    const userStore = useUserStore();
    const notificationStore = useNotificationStore();
    
    if (!client || !userStore.userId) {
      console.error('WebSocket客户端未初始化或用户未登录');
      return;
    }
    
    try {
      // 订阅用户通知队列
      const userId = userStore.userId;
      console.log(`订阅通知通道: /user/${userId}/queue/notifications`);
      
      this.subscriptions['notifications'] = client.subscribe(
        `/user/${userId}/queue/notifications`,
        (message: IMessage) => {
          try {
            const notification: NotificationDTO = JSON.parse(message.body);
            console.log('收到实时通知:', notification);
            
            // 将通知添加到通知存储中
            notificationStore.addNotification(notification);
            
            // 显示通知弹窗，并添加点击事件
            const notificationInstance = ElNotification({
              title: notification.title,
              message: notification.content.substring(0, 100) + (notification.content.length > 100 ? '...' : ''),
              type: 'info',
              duration: 5000,
              onClick: () => {
                // 根据用户角色跳转到对应的通知页面
                if (userStore.isSupervisor) {
                  router.push('/supervisor/notifications');
                } else if (userStore.isStudent) {
                  router.push('/student/notifications');
                }
                // 关闭通知弹窗 (ElNotification 会自动处理)
                // notificationInstance.close(); // 通常不需要手动调用
              }
            });

          } catch (error) {
            console.error('处理通知消息失败:', error);
          }
        }
      );
      
      console.log('通知订阅成功');
    } catch (error) {
      console.error('订阅通知失败:', error);
    }
  }
  
  /**
   * 取消订阅通知
   */
  unsubscribeFromNotifications() {
    if (this.subscriptions['notifications']) {
      try {
        this.subscriptions['notifications'].unsubscribe();
        delete this.subscriptions['notifications'];
        console.log('已取消订阅通知');
      } catch (error) {
        console.error('取消订阅通知失败:', error);
      }
    }
  }
  
  /**
   * 发送通知
   */
  async sendNotification(title: string, content: string, recipientIds: number[]): Promise<NotificationDTO> {
    try {
      const response = await axios.post('/api/notifications', {
        title,
        content,
        recipientIds
      });
      
      return response.data;
    } catch (error) {
      console.error('发送通知失败:', error);
      throw error;
    }
  }
  
  /**
   * 获取收到的通知
   */
  async getReceivedNotifications(): Promise<NotificationDTO[]> {
    try {
      const response = await axios.get('/api/notifications/received');
      return response.data;
    } catch (error) {
      console.error('获取收到的通知失败:', error);
      return [];
    }
  }
  
  /**
   * 获取发送的通知
   */
  async getSentNotifications(): Promise<NotificationDTO[]> {
    try {
      const response = await axios.get('/api/notifications/sent');
      return response.data;
    } catch (error) {
      console.error('获取发送的通知失败:', error);
      return [];
    }
  }
  
  /**
   * 获取学生列表
   */
  async getStudents() {
    try {
      const response = await axios.get('/api/notifications/students');
      return response.data;
    } catch (error) {
      console.error('获取学生列表失败:', error);
      return [];
    }
  }
  
  /**
   * 获取通知详情
   */
  async getNotificationDetail(id: number): Promise<NotificationDTO | null> {
    try {
      const response = await axios.get(`/api/notifications/${id}`);
      return response.data;
    } catch (error) {
      console.error('获取通知详情失败:', error);
      return null;
    }
  }
}

export default new NotificationService(); 