import { defineStore } from 'pinia';
import chatService from '../services/chat';
import type { ChatMessage, Conversation } from '../services/chat';
import { useUserStore } from './user';

// 创建一个事件总线用于消息通知
export const chatEvents = {
  listeners: new Map<string, Function[]>(),
  lastNotificationId: 0, // 添加最后通知ID记录
  lastNotificationTime: 0, // 添加最后通知时间记录
  
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
    // 对notification事件做特殊处理，避免短时间内重复通知
    if (event === 'notification' && data) {
      const now = Date.now();
      // 如果是相同消息且间隔小于3秒，则跳过
      if (data.messageId === this.lastNotificationId && now - this.lastNotificationTime < 3000) {
        console.log('跳过短时间内的重复通知');
        return;
      }
      
      // 更新最后通知信息
      this.lastNotificationId = data.messageId || now;
      this.lastNotificationTime = now;
    }
    
    const callbacks = this.listeners.get(event) || [];
    callbacks.forEach(callback => callback(data));
  }
};

interface ChatState {
  conversations: Conversation[];
  activeConversationId: number | null;
  activeConversation: Conversation | null;
  messages: Record<number, ChatMessage[]>; // 以会话ID为键的消息映射
  unreadCount: number;
  loadingConversations: boolean;
  loadingMessages: boolean;
  error: string | null;
}

export const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    conversations: [],
    activeConversationId: null,
    activeConversation: null,
    messages: {},
    unreadCount: 0,
    loadingConversations: false,
    loadingMessages: false,
    error: null
  }),

  getters: {
    // 获取按最后消息时间排序的会话列表
    sortedConversations: (state) => {
      return [...state.conversations].sort((a, b) => {
        const timeA = new Date(a.lastMessageTime).getTime();
        const timeB = new Date(b.lastMessageTime).getTime();
        return timeB - timeA; // 降序，最新的在最前面
      });
    },
    
    // 获取指定会话的消息
    getMessagesForConversation: (state) => (conversationId: number) => {
      return state.messages[conversationId] || [];
    },
    
    // 获取总未读消息数
    totalUnreadCount: (state) => {
      return state.unreadCount;
    },
    
    // 聊天是否有错误
    hasError: (state) => !!state.error
  },

  actions: {
    // 初始化聊天服务
    async initChat() {
      try {
        await chatService.init();
        // 注册消息回调
        chatService.onMessage(this.handleNewMessage.bind(this));
        
        // 自动加载会话列表
        await this.loadConversations();
        
        // 获取未读消息总数
        this.fetchUnreadCount();
      } catch (error) {
        console.error('初始化聊天服务失败:', error);
        this.error = '初始化聊天服务失败';
      }
    },
    
    // 加载用户的会话列表
    async loadConversations() {
      if (this.loadingConversations) return;
      
      this.loadingConversations = true;
      this.error = null;
      
      try {
        const conversations = await chatService.getConversations();
        this.conversations = conversations;
        this.loadingConversations = false;
      } catch (error) {
        console.error('加载会话列表失败:', error);
        this.error = '加载会话列表失败';
        this.loadingConversations = false;
      }
    },
    
    // 激活指定的会话
    async setActiveConversation(conversationId: number) {
      this.activeConversationId = conversationId;
      
      // 查找会话
      const conversation = this.conversations.find(c => c.id === conversationId);
      if (conversation) {
        this.activeConversation = conversation;
      } else {
        // 如果在列表中找不到，可能需要重新获取
        await this.loadConversations();
        const refetchedConversation = this.conversations.find(c => c.id === conversationId);
        this.activeConversation = refetchedConversation || null;
      }
      
      // 加载会话消息
      if (this.activeConversation) {
        await this.loadMessagesForConversation(conversationId);
        
        // 标记会话为已读
        if (this.activeConversation.unreadCount > 0) {
          await this.markConversationAsRead(conversationId);
        }
      }
    },
    
    // 获取与特定用户的会话
    async getOrCreateConversationWithUser(userId: number) {
      try {
        const conversation = await chatService.getConversationWithUser(userId);
        
        // 将新会话添加到列表中（如果还不存在）
        const exists = this.conversations.some(c => c.id === conversation.id);
        if (!exists) {
          this.conversations.push(conversation);
        } else {
          // 更新现有会话
          const index = this.conversations.findIndex(c => c.id === conversation.id);
          if (index !== -1) {
            this.conversations[index] = conversation;
          }
        }
        
        // 如果服务器返回了最近消息，添加到消息列表中
        // @ts-ignore - 服务器可能返回recentMessages，但我们的类型定义中没有
        if (conversation.recentMessages && conversation.recentMessages.length > 0) {
          // @ts-ignore
          this.messages[conversation.id] = conversation.recentMessages;
        }
        
        return conversation;
      } catch (error) {
        console.error('获取用户会话失败:', error);
        this.error = '获取用户会话失败';
        throw error;
      }
    },
    
    // 加载会话的消息
    async loadMessagesForConversation(conversationId: number, page: number = 0, size: number = 20) {
      if (this.loadingMessages) return;
      
      this.loadingMessages = true;
      this.error = null;
      
      try {
        const messages = await chatService.getMessagesForConversation(conversationId, page, size);
        
        // 初始化或替换消息列表
        if (page === 0) {
          this.messages[conversationId] = messages;
        } else {
          // 添加新消息，避免重复
          const existingIds = new Set(this.messages[conversationId]?.map(m => m.id) || []);
          const newMessages = messages.filter(m => !existingIds.has(m.id));
          
          if (!this.messages[conversationId]) {
            this.messages[conversationId] = [];
          }
          
          this.messages[conversationId] = [...this.messages[conversationId], ...newMessages];
          
          // 按发送时间排序
          this.messages[conversationId].sort((a, b) => {
            return new Date(a.sentTime).getTime() - new Date(b.sentTime).getTime();
          });
        }
        
        this.loadingMessages = false;
      } catch (error) {
        console.error('加载会话消息失败:', error);
        this.error = '加载会话消息失败';
        this.loadingMessages = false;
      }
    },
    
    // 加载更早的消息
    async loadOlderMessages(conversationId: number, messageId: number, size: number = 20) {
      if (this.loadingMessages) return;
      
      this.loadingMessages = true;
      this.error = null;
      
      try {
        const olderMessages = await chatService.getMessagesBeforeId(conversationId, messageId, size);
        
        if (!this.messages[conversationId]) {
          this.messages[conversationId] = [];
        }
        
        // 添加新消息，避免重复
        const existingIds = new Set(this.messages[conversationId].map(m => m.id));
        const newMessages = olderMessages.filter(m => !existingIds.has(m.id));
        
        this.messages[conversationId] = [...newMessages, ...this.messages[conversationId]];
        
        // 按发送时间排序
        this.messages[conversationId].sort((a, b) => {
          return new Date(a.sentTime).getTime() - new Date(b.sentTime).getTime();
        });
        
        this.loadingMessages = false;
        return olderMessages.length;
      } catch (error) {
        console.error('加载更早消息失败:', error);
        this.error = '加载更早消息失败';
        this.loadingMessages = false;
        return 0;
      }
    },
    
    // 发送消息
    async sendMessage(recipientId: number, content: string) {
      this.error = null;
      
      try {
        // 发送消息前检查用户状态
        const userStore = useUserStore();
        if (!userStore.isLoggedIn || !userStore.userId) {
          throw new Error('用户未登录，无法发送消息');
        }
        
        // 发送消息
        const tempMessage = await chatService.sendMessage(recipientId, content) as ChatMessage;
        
        // 如果有临时消息返回，立即添加到UI中
        if (tempMessage) {
          // 找到相应的会话
          const conversation = this.conversations.find(c => 
            (c.user1.id === recipientId || c.user2.id === recipientId)
          );
          
          if (conversation) {
            // 更新会话的最后消息和时间
            conversation.lastMessage = tempMessage;
            conversation.lastMessageTime = tempMessage.sentTime;
            
            // 将临时消息添加到现有会话
            if (!this.messages[conversation.id]) {
              this.messages[conversation.id] = [];
            }
            
            // 添加临时消息到消息列表中
            this.messages[conversation.id].push(tempMessage);
            
            // 发布临时消息添加事件
            chatEvents.emit('messageAdded', {
              conversationId: conversation.id,
              message: tempMessage
            });
          } else {
            // 如果会话不存在，重新加载会话列表
            await this.loadConversations();
          }
        }
      } catch (error: any) {
        console.error('发送消息失败:', error);
        
        // 更新错误信息，提供更详细的反馈
        if (error.response?.data?.message) {
          this.error = error.response.data.message;
        } else if (error.message) {
          this.error = `发送失败: ${error.message}`;
        } else {
          this.error = '发送消息失败，请稍后重试';
        }
        
        // 如果是用户不存在的特定错误，刷新会话列表
        if (error.message && (
          error.message.includes('发送者不存在') || 
          error.message.includes('接收者不存在')
        )) {
          // 重新加载会话列表，清除可能已失效的用户
          await this.loadConversations();
        }
        
        throw error;
      }
    },
    
    // 处理新收到的消息
    handleNewMessage(message: ChatMessage) {
      console.log('收到新消息:', message);
      
      // 检查是否是发给当前用户的未读消息
      const userStore = useUserStore();
      const isForCurrentUser = message.recipientId === userStore.userId && !message.isRead;
      
      // 检查消息是否已存在
      let messageExists = false;
      if (message.conversationId && this.messages[message.conversationId]) {
        messageExists = this.messages[message.conversationId].some(m => m.id === message.id);
        if (messageExists) {
          console.log(`消息(ID: ${message.id})已存在于会话 ${message.conversationId}`);
        }
      }
      
      // 如果是发给当前用户的未读消息，无论是否已存在都显示通知
      if (isForCurrentUser) {
        // 显示桌面通知
        this.showNotification(message);
        
        // 发布新消息事件
        chatEvents.emit('new-message', message);
        
        // 如果消息不存在，则更新未读计数
        if (!messageExists && message.conversationId) {
          this.updateUnreadCount(message.conversationId);
        }
      }
      
      // 如果消息不存在，添加到相应会话
      if (!messageExists) {
        this.addMessageToConversation(message);
      }
    },
    
    // 标记会话为已读
    async markConversationAsRead(conversationId: number) {
      try {
        await chatService.markConversationAsRead(conversationId);
        
        // 更新本地会话未读数
        const conversationIndex = this.conversations.findIndex(c => c.id === conversationId);
        if (conversationIndex !== -1) {
          this.conversations[conversationIndex].unreadCount = 0;
          
          // 更新总未读数
          this.fetchUnreadCount();
        }
      } catch (error) {
        console.error('标记会话为已读失败:', error);
      }
    },
    
    // 标记单个消息为已读
    async markMessageAsRead(messageId: number) {
      // 首先检查消息ID是否有效
      if (!messageId || messageId < 0) {
        console.log('无效的消息ID，跳过标记已读请求');
        return;
      }

      try {
        console.log(`尝试标记消息 ${messageId} 为已读`);
        await chatService.markMessageAsRead(messageId);
        
        // 遍历所有会话的消息，找到并标记为已读
        for (const conversationId in this.messages) {
          const messageIndex = this.messages[conversationId].findIndex(m => m.id === messageId);
          if (messageIndex !== -1) {
            this.messages[conversationId][messageIndex].isRead = true;
            console.log(`消息 ${messageId} 已在本地标记为已读`);
            break;
          }
        }
      } catch (error) {
        console.error('标记消息为已读失败:', error);
        
        // 即使API调用失败，也在本地标记为已读以改善用户体验
        for (const conversationId in this.messages) {
          const messageIndex = this.messages[conversationId].findIndex(m => m.id === messageId);
          if (messageIndex !== -1) {
            this.messages[conversationId][messageIndex].isRead = true;
            console.log(`API调用失败，但消息 ${messageId} 已在本地标记为已读`);
            break;
          }
        }
      }
    },
    
    // 获取未读消息数量
    async fetchUnreadCount() {
      try {
        const count = await chatService.getUnreadMessageCount();
        this.unreadCount = count;
        return count;
      } catch (error) {
        console.error('获取未读消息数量失败:', error);
        return 0;
      }
    },
    
    // 清除聊天错误
    clearError() {
      this.error = null;
    },
    
    // 清除聊天数据（例如用户登出时）
    clearChatData() {
      this.conversations = [];
      this.activeConversationId = null;
      this.activeConversation = null;
      this.messages = {};
      this.unreadCount = 0;
      this.error = null;
      
      // 断开WebSocket连接
      chatService.disconnect();
    },
    
    // 显示消息通知
    showNotification(message: ChatMessage) {
      console.log('触发消息通知事件:', message.content);
      
      // 构建通知数据
      const notificationData = {
        title: '新消息',
        message: `${message.senderName}: ${message.content}`,
        messageId: message.id,
        sender: {
          id: message.senderId,
          name: message.senderName,
          avatar: message.senderAvatar || ''
        }
      };
      
      // 使用事件总线发布通知事件，让组件可以订阅
      chatEvents.emit('notification', notificationData);
      console.log('通知事件已发出');
    },
    
    // 将消息添加到会话
    addMessageToConversation(message: ChatMessage) {
      // 处理conversationId为null的情况
      if (!message.conversationId) {
        console.log("消息缺少conversationId，尝试重新加载会话列表");
        this.loadConversations();
        return;
      }
      
      // 查找相应的会话
      const conversationId = message.conversationId;
      const conversation = this.conversations.find(c => c.id === conversationId);
      
      if (conversation) {
        // 更新会话的最后消息和时间
        conversation.lastMessage = message;
        conversation.lastMessageTime = message.sentTime;
        
        // 将消息添加到现有会话
        if (!this.messages[conversationId]) {
          this.messages[conversationId] = [];
        }
        
        // 检查是否有对应的临时消息需要替换
        const tempMessageIndex = this.messages[conversationId].findIndex(m => 
          m.id < 0 && m.senderId === message.senderId && m.content === message.content
        );

        if (tempMessageIndex >= 0) {
          // 找到了临时消息，替换它
          console.log(`替换临时消息(${this.messages[conversationId][tempMessageIndex].id})为实际消息(${message.id})`);
          this.messages[conversationId][tempMessageIndex] = message;
          
          // 发布消息更新事件
          chatEvents.emit('messageUpdated', { 
            conversationId, 
            messageId: message.id 
          });
        } else {
          // 添加新消息
          console.log(`添加新消息到会话 ${conversationId}, 消息ID: ${message.id}`);
          
          // 设置消息为已读状态（如果是当前活跃会话）
          message.isRead = this.activeConversationId === conversationId;
          this.messages[conversationId].push(message);
          
          // 发布新消息事件
          chatEvents.emit('messageAdded', { 
            conversationId, 
            message 
          });
        }
      } else {
        console.log("收到消息的会话不存在，重新加载会话列表");
        // 如果会话不存在，重新加载会话列表
        this.loadConversations();
      }
    },
    
    // 更新未读消息计数
    updateUnreadCount(conversationId: number) {
      // 找到对应的会话
      const conversation = this.conversations.find(c => c.id === conversationId);
      
      if (conversation) {
        // 如果不是当前活跃会话，增加未读消息计数
        if (this.activeConversationId !== conversationId) {
          conversation.unreadCount = (conversation.unreadCount || 0) + 1;
          this.unreadCount += 1;
        } else {
          // 如果是当前活跃会话，自动标记为已读
          this.markConversationAsRead(conversationId).catch(error => {
            console.error('自动标记会话为已读失败:', error);
          });
        }
      }
    },

    // 注册消息回调
    onMessage(callback: (message: ChatMessage) => void) {
      return chatEvents.on('new-message', callback);
    }
  }
});