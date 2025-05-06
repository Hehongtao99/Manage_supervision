import { defineStore } from 'pinia';
import axios from '../utils/axios';
import { ElNotification } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from './user';
import chatEvents from '../services/eventBus';

// 创建一个事件总线用于好友请求通知
export const friendEvents = {
  listeners: new Map<string, Function[]>(),
  
  // 添加事件监听器
  on(event: string, callback: Function) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(callback);
    
    // 返回取消监听函数
    return () => {
      const callbacks = this.listeners.get(event);
      if (callbacks) {
        const index = callbacks.indexOf(callback);
        if (index !== -1) {
          callbacks.splice(index, 1);
        }
      }
    };
  },
  
  // 发布事件
  emit(event: string, data?: any) {
    const callbacks = this.listeners.get(event) || [];
    callbacks.forEach(callback => callback(data));
  }
};

export interface FriendRequest {
  id: number;
  fromUserId: number;
  toUserId: number;
  message: string;
  status: number; // 0-待处理，1-已接受，2-已拒绝
  createdTime: string;
  updatedTime: string;
  fromUser: {
    id: number;
    username: string;
    name: string;
    avatar: string;
  };
  toUser: {
    id: number;
    username: string;
    name: string;
    avatar: string;
  };
}

export interface Friend {
  id: number;
  username: string;
  name: string;
  avatar: string;
}

interface FriendState {
  friends: Friend[];
  receivedRequests: FriendRequest[];
  sentRequests: FriendRequest[];
  unreadRequestCount: number;
  loading: boolean;
  initialized: boolean;
  error: string | null;
}

export const useFriendStore = defineStore('friend', {
  state: (): FriendState => ({
    friends: [],
    receivedRequests: [],
    sentRequests: [],
    unreadRequestCount: 0,
    loading: false,
    initialized: false,
    error: null
  }),

  getters: {
    hasFriends: (state) => state.friends.length > 0,
    hasUnreadRequests: (state) => state.unreadRequestCount > 0,
    pendingReceivedRequests: (state) => 
      state.receivedRequests.filter(request => request.status === 0)
  },

  actions: {
    // 初始化好友系统
    async initFriendSystem() {
      if (this.initialized) return;
      
      try {
        // 订阅WebSocket通知
        this.subscribeToFriendNotifications();
        
        // 加载数据
        await this.loadFriends();
        await this.loadReceivedRequests();
        await this.loadSentRequests();
        
        this.initialized = true;
      } catch (error) {
        console.error('初始化好友系统失败:', error);
        this.error = '初始化好友系统失败';
      }
    },
    
    // 订阅好友请求通知
    subscribeToFriendNotifications() {
      // 使用新的eventBus监听通知事件
      chatEvents.on('friendRequest', (data) => {
        console.log('Friend store收到好友请求通知:', data);
        this.handleFriendRequestNotification(data);
      });
    },
    
    // 处理好友请求通知
    handleFriendRequestNotification(data: any) {
      if (!data) return;
      
      console.log('处理好友请求通知:', data);
      
      // 增加未读请求计数
      this.unreadRequestCount += 1;
      
      // 如果是新请求，添加到列表中
      if (data.type === 'NEW_REQUEST' && data.request) {
        this.receivedRequests.unshift(data.request);
        
        // 发送系统通知
        ElNotification({
          title: '新的好友请求',
          message: `${data.request.fromUser.name || data.request.fromUser.username} 请求添加您为好友`,
          type: 'info',
          duration: 5000,
          onClick: () => {
            const router = useRouter();
            router.push('/friends');
          }
        });
        
        // 发布事件
        friendEvents.emit('newFriendRequest', data.request);
      } 
      // 如果是请求状态更新
      else if (data.type === 'REQUEST_UPDATED' && data.request) {
        // 更新发送的请求状态
        const index = this.sentRequests.findIndex(req => req.id === data.request.id);
        if (index !== -1) {
          this.sentRequests[index] = data.request;
          
          // 如果请求被接受，添加到好友列表
          if (data.request.status === 1) {
            const userStore = useUserStore();
            const isSelf = userStore.userId === data.request.fromUserId;
            const friendInfo = isSelf ? data.request.toUser : data.request.fromUser;
            
            // 确保不重复添加
            const existingIndex = this.friends.findIndex(f => f.id === friendInfo.id);
            if (existingIndex === -1) {
              this.friends.push(friendInfo);
            }
            
            // 发送系统通知
            ElNotification({
              title: '好友请求已接受',
              message: `${friendInfo.name || friendInfo.username} 已接受您的好友请求`,
              type: 'success',
              duration: 5000
            });
          }
          
          // 发布事件
          friendEvents.emit('friendRequestUpdated', data.request);
        }
      }
    },
    
    // 加载好友列表
    async loadFriends() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await axios.get('/api/friends');
        this.friends = response.data;
      } catch (error) {
        console.error('加载好友列表失败:', error);
        this.error = '加载好友列表失败';
      } finally {
        this.loading = false;
      }
    },
    
    // 加载收到的好友请求
    async loadReceivedRequests() {
      try {
        const response = await axios.get('/api/friends/requests/received');
        this.receivedRequests = response.data;
        
        // 计算未读请求数量
        this.unreadRequestCount = this.receivedRequests.filter(req => req.status === 0).length;
      } catch (error) {
        console.error('加载收到的好友请求失败:', error);
        this.error = '加载收到的好友请求失败';
      }
    },
    
    // 加载发送的好友请求
    async loadSentRequests() {
      try {
        const response = await axios.get('/api/friends/requests/sent');
        this.sentRequests = response.data;
      } catch (error) {
        console.error('加载发送的好友请求失败:', error);
        this.error = '加载发送的好友请求失败';
      }
    },
    
    // 发送好友请求
    async sendFriendRequest(toUserId: number, message: string) {
      try {
        const response = await axios.post('/api/friends/requests', {
          toUserId,
          message
        });
        
        if (response.data.success) {
          // 重新加载发送请求列表
          await this.loadSentRequests();
          return { success: true };
        } else {
          return { success: false, message: response.data.message || '发送好友请求失败' };
        }
      } catch (error) {
        console.error('发送好友请求失败:', error);
        return { success: false, message: '发送好友请求失败' };
      }
    },
    
    // 处理好友请求
    async processFriendRequest(requestId: number, accept: boolean) {
      try {
        const response = await axios.put(`/api/friends/requests/${requestId}?accept=${accept}`);
        
        if (response.data.success) {
          // 更新请求状态
          const requestIndex = this.receivedRequests.findIndex(req => req.id === requestId);
          if (requestIndex !== -1) {
            this.receivedRequests[requestIndex].status = accept ? 1 : 2;
          }
          
          // 如果接受，添加到好友列表
          if (accept) {
            await this.loadFriends();
          }
          
          // 减少未读计数
          if (this.unreadRequestCount > 0) {
            this.unreadRequestCount -= 1;
          }
          
          return { success: true };
        } else {
          return { success: false, message: response.data.message || '处理好友请求失败' };
        }
      } catch (error) {
        console.error('处理好友请求失败:', error);
        return { success: false, message: '处理好友请求失败' };
      }
    },
    
    // 删除好友
    async deleteFriend(friendId: number) {
      try {
        const response = await axios.delete(`/api/friends/${friendId}`);
        
        if (response.data.success) {
          // 从列表移除这个好友
          const index = this.friends.findIndex(f => f.id === friendId);
          if (index !== -1) {
            this.friends.splice(index, 1);
          }
          return { success: true };
        } else {
          return { success: false, message: response.data.message || '删除好友失败' };
        }
      } catch (error) {
        console.error('删除好友失败:', error);
        return { success: false, message: '删除好友失败' };
      }
    },
    
    // 搜索用户
    async searchUsers(keyword: string) {
      try {
        // 获取当前用户角色信息
        const userStore = useUserStore();
        const isAdmin = userStore.isAdmin;
        const isSupervisor = userStore.isSupervisor;
        
        const response = await axios.get('/api/friends/search', {
          params: { 
            keyword,
            excludeAdmin: true,      // 排除管理员用户
            excludeSupervisor: false // 是否排除教师用户，可以根据需求调整
          }
        });
        
        return { success: true, data: response.data };
      } catch (error) {
        console.error('搜索用户失败:', error);
        return { success: false, message: '搜索用户失败' };
      }
    },
    
    // 检查是否有好友关系
    isFriend(userId: number) {
      return this.friends.some(f => f.id === userId);
    },
    
    // 清除所有未读好友请求计数
    clearUnreadRequestCount() {
      this.unreadRequestCount = 0;
    },
    
    // 重置状态
    resetState() {
      this.friends = [];
      this.receivedRequests = [];
      this.sentRequests = [];
      this.unreadRequestCount = 0;
      this.loading = false;
      this.initialized = false;
      this.error = null;
    }
  }
}); 