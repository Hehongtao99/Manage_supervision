import { defineStore } from 'pinia';
import chatService from '../services/chat';
import type { ChatMessage, Conversation } from '../services/chat';
import { useUserStore } from './user';

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
        
        // 如果有最近消息，添加到消息列表中
        if (conversation.recentMessages && conversation.recentMessages.length > 0) {
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
        await chatService.sendMessage(recipientId, content);
        
        // 如果已经存在与该接收者的会话
        const conversation = this.conversations.find(c => 
          (c.user1.id === recipientId || c.user2.id === recipientId)
        );
        
        if (conversation) {
          // 注意：由于WebSocket发送消息不会返回消息内容，我们不在这里添加消息
          // 而是让WebSocket的onMessageReceived回调处理，这样就会触发handleNewMessage
          // 这样可以避免消息重复添加
        } else {
          // 如果会话不存在，重新加载会话列表
          await this.loadConversations();
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
      // 查找相应的会话
      const conversationId = message.conversationId;
      const conversation = this.conversations.find(c => c.id === conversationId);
      
      if (conversation) {
        // 更新会话的最后消息和时间
        conversation.lastMessage = message;
        conversation.lastMessageTime = message.sentTime;
        // 不再增加未读计数
        // conversation.unreadCount += 1;
        
        // 将消息添加到现有会话
        if (!this.messages[conversationId]) {
          this.messages[conversationId] = [];
        }
        
        // 检查消息是否已存在，避免重复添加
        const messageExists = this.messages[conversationId].some(m => m.id === message.id);
        if (!messageExists) {
          // 设置消息为已读状态
          message.read = true;
          this.messages[conversationId].push(message);
          
          // 不再增加未读消息总数
          // this.unreadCount += 1;
          
          // 自动将会话标记为已读
          this.markConversationAsRead(conversationId).catch(error => {
            console.error('自动标记会话为已读失败:', error);
          });
        } else {
          console.log(`消息(ID: ${message.id})已存在，跳过添加`);
        }
      } else {
        // 如果会话不存在，重新加载会话列表
        this.loadConversations();
      }
    },
    
    // 标记会话为已读
    async markConversationAsRead(conversationId: number) {
      try {
        await chatService.markConversationAsRead(conversationId);
        
        // 更新本地状态
        const conversation = this.conversations.find(c => c.id === conversationId);
        if (conversation) {
          // 减少总未读数
          this.unreadCount = Math.max(0, this.unreadCount - conversation.unreadCount);
          // 重置会话未读数
          conversation.unreadCount = 0;
        }
        
        // 标记消息为已读
        if (this.messages[conversationId]) {
          this.messages[conversationId] = this.messages[conversationId].map(m => ({
            ...m,
            read: true
          }));
        }
      } catch (error) {
        console.error('标记会话为已读失败:', error);
        this.error = '标记会话为已读失败';
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
    }
  }
}); 