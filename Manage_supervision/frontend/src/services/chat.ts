import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from '../utils/axios';
import { useUserStore } from '../stores/user';
import { chatEvents } from '../stores/chat';

export interface ChatMessage {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  senderAvatar: string;
  recipientId: number;
  recipientName: string;
  recipientAvatar: string;
  content: string;
  sentTime: string;
  isRead: boolean;
  // 文件相关字段
  fileUrl?: string;
  fileName?: string;
  fileType?: string;
  fileSize?: number;
}

export interface Conversation {
  id: number;
  user1: {
    id: number;
    name: string;
    avatar: string;
  };
  user2: {
    id: number;
    name: string;
    avatar: string;
  };
  lastMessage?: ChatMessage;
  lastMessageTime: string;
  unreadCount: number;
}

class ChatService {
  private client: Client | null = null;
  private connected: boolean = false;
  private connecting: boolean = false;
  private messageCallbacks: Map<string, Function[]> = new Map();
  private connectionTimeout: ReturnType<typeof setTimeout> | undefined = undefined;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;
  private reconnectDelay: number = 5000; // 5秒
  private pollingInterval: ReturnType<typeof setInterval> | undefined = undefined;
  private messageSubscription: any;
  private notificationSubscription: any;

  /**
   * 初始化聊天服务
   * @returns 连接成功的Promise
   */
  async init() {
    if (this.connecting || this.connected) return;
    
    try {
      this.connecting = true;
      console.log('初始化聊天服务...');
      
      // 获取token
      const userStore = useUserStore();
      const token = userStore.token;
      
      if (!token) {
        throw new Error('用户未登录，无法连接聊天服务');
      }
      
      // 设置连接超时
      this.connectionTimeout = setTimeout(() => {
        if (!this.connected) {
          console.error('WebSocket连接超时，将尝试轮询模式');
          this.connecting = false;
          this.initPolling();
        }
      }, 10000); // 10秒超时
      
      this.client = new Client({
        webSocketFactory: () => {
          // 使用绝对路径，确保SockJS能正确处理代理
          console.log('创建SockJS连接到: /ws');
          
          // 使用完整的SockJS选项
          const socket = new SockJS('/ws', null, {
            transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
            timeout: 5000,
          });
          
          // 添加调试事件
          socket.onopen = () => console.log('SockJS连接已打开');
          socket.onclose = (event) => console.log('SockJS连接已关闭', event);
          socket.onerror = (error) => console.error('SockJS连接错误', error);
          
          return socket;
        },
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        onConnect: this.onConnect.bind(this),
        onDisconnect: this.onDisconnect.bind(this),
        onStompError: this.onStompError.bind(this),
        // 添加更多调试选项
        debug: (msg) => {
          console.debug('STOMP调试:', msg);
        }
      });
      
      console.log('正在激活STOMP客户端...');
      this.client.activate();
    } catch (error) {
      console.error('初始化聊天服务失败:', error);
      this.connecting = false;
      if (this.connectionTimeout) {
        clearTimeout(this.connectionTimeout);
      }
    }
  }
  
  /**
   * 初始化HTTP轮询模式
   */
  private initPolling() {
    console.log('使用HTTP轮询模式');
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
    
    // 每30秒检查一次新消息
    this.pollingInterval = setInterval(async () => {
      try {
        const userStore = useUserStore();
        if (!userStore.isLoggedIn) {
          this.stopPolling();
          return;
        }
        
        // 获取活跃的会话ID
        const response = await axios.get('/api/chat/conversations');
        const conversations = response.data;
        
        if (conversations && conversations.length > 0) {
          // 对每个会话，检查新消息
          conversations.forEach(async (conversation: Conversation) => {
            const messagesResponse = await axios.get(`/api/chat/conversations/${conversation.id}/messages`, {
              params: {
                page: 0,
                size: 20
              }
            });
            
            const messages = messagesResponse.data;
            if (messages && messages.length > 0) {
              const callbacks = this.messageCallbacks.get('message') || [];
              const conversationCallbacks = this.messageCallbacks.get(`conversation:${conversation.id}`) || [];
              
              // 调用消息回调，注意可能会有重复通知的问题
              messages.forEach((message: ChatMessage) => {
                callbacks.forEach(callback => callback(message));
                conversationCallbacks.forEach(callback => callback(message));
              });
            }
          });
        }
      } catch (error) {
        console.error('轮询消息失败:', error);
      }
    }, 30000); // 30秒
  }
  
  /**
   * 停止轮询
   */
  private stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = undefined;
    }
  }
  
  // 连接成功回调
  private onConnect() {
    console.log('WebSocket连接成功');
    this.connected = true;
    this.connecting = false;
    this.reconnectAttempts = 0;
    
    // 清除连接超时
    if (this.connectionTimeout) {
      clearTimeout(this.connectionTimeout);
      this.connectionTimeout = undefined;
    }
    
    // 停止轮询（如果正在进行）
    this.stopPolling();
    
    // 订阅用户特定的消息通道
    this.subscribeToUserChannels();
  }
  
  // 订阅用户特定的消息通道
  private subscribeToUserChannels() {
    const userStore = useUserStore();
    const userId = userStore.userId;
    
    if (this.client && userId) {
      try {
        console.log(`订阅个人消息通道: /user/${userId}/queue/messages`);
        // 首先取消所有现有订阅
        if (this.messageSubscription) {
          this.messageSubscription.unsubscribe();
        }
        if (this.notificationSubscription) {
          this.notificationSubscription.unsubscribe();
        }
        
        // 订阅个人消息
        this.messageSubscription = this.client.subscribe(
          `/user/${userId}/queue/messages`, 
          this.onMessageReceived.bind(this),
          { id: `messages-${userId}` }
        );
        
        // 订阅通知
        this.notificationSubscription = this.client.subscribe(
          `/user/${userId}/queue/notifications`, 
          this.onErrorReceived.bind(this),
          { id: `notifications-${userId}` }
        );
        
        // 发送连接消息
        console.log('发送连接消息到: /app/chat.connect');
        this.client.publish({
          destination: '/app/chat.connect'
        });
      } catch (error) {
        console.error('订阅消息通道失败:', error);
      }
    }
  }
  
  // 断开连接回调
  private onDisconnect() {
    console.log('WebSocket断开连接');
    this.connected = false;
    this.connecting = false;
    
    // 尝试重新连接
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      setTimeout(() => {
        this.init();
      }, this.reconnectDelay);
    } else {
      console.warn(`WebSocket重连失败，超过最大尝试次数(${this.maxReconnectAttempts})，切换到HTTP轮询模式`);
      this.initPolling();
    }
  }
  
  // STOMP错误回调
  private onStompError(frame: any) {
    console.error('STOMP错误:', frame.headers['message']);
    console.error('STOMP错误详情:', frame.body);
  }
  
  // 接收消息回调
  private onMessageReceived(message: IMessage) {
    try {
      const chatMessage = JSON.parse(message.body) as ChatMessage;
      console.log('收到新消息:', chatMessage);
      console.log(`消息详情 - ID: ${chatMessage.id}, 发送者: ${chatMessage.senderId}, 会话ID: ${chatMessage.conversationId}`);
      
      // 立即触发消息接收事件，优先处理UI更新
      chatEvents.emit('messageReceived', chatMessage);
      
      // 调用所有的消息回调
      const callbacks = this.messageCallbacks.get('message') || [];
      console.log(`触发${callbacks.length}个全局消息回调`);
      callbacks.forEach(callback => callback(chatMessage));
      
      // 调用特定会话的回调
      if (chatMessage.conversationId) {
        const conversationCallbacks = this.messageCallbacks.get(`conversation:${chatMessage.conversationId}`) || [];
        console.log(`触发${conversationCallbacks.length}个会话(ID: ${chatMessage.conversationId})特定回调`);
        conversationCallbacks.forEach(callback => callback(chatMessage));
      }
    } catch (error) {
      console.error('处理收到的消息失败:', error);
    }
  }
  
  // 接收错误通知回调
  private onErrorReceived(message: IMessage) {
    try {
      const errorInfo = JSON.parse(message.body);
      console.error('收到错误通知:', errorInfo);
      
      // 这里可以添加错误处理逻辑，比如显示通知给用户
    } catch (error) {
      console.error('处理错误通知失败:', error);
    }
  }
  
  // 添加消息回调
  onMessage(callback: Function, conversationId?: number) {
    const key = conversationId ? `conversation:${conversationId}` : 'message';
    
    if (!this.messageCallbacks.has(key)) {
      this.messageCallbacks.set(key, []);
    }
    
    this.messageCallbacks.get(key)!.push(callback);
    return () => this.removeMessageCallback(callback, conversationId);
  }
  
  // 移除消息回调
  private removeMessageCallback(callback: Function, conversationId?: number) {
    const key = conversationId ? `conversation:${conversationId}` : 'message';
    
    if (this.messageCallbacks.has(key)) {
      const callbacks = this.messageCallbacks.get(key)!;
      const index = callbacks.indexOf(callback);
      if (index !== -1) {
        callbacks.splice(index, 1);
      }
    }
  }
  
  // 发送消息
  async sendMessage(recipientId: number, content: string): Promise<ChatMessage | void> {
    const userStore = useUserStore();
    if (!userStore.isLoggedIn || !userStore.userId) {
        console.error('发送消息失败: 用户未登录或ID无效');
        return Promise.reject(new Error('发送消息失败: 用户未登录'));
    }

    if (!recipientId) {
        console.error('发送消息失败: 接收者ID无效');
        return Promise.reject(new Error('接收者ID无效'));
    }

    // 验证内容
    if (!content || content.trim() === '') {
        console.error('发送消息失败: 消息内容为空');
        return Promise.reject(new Error('消息内容不能为空'));
    }

    const senderId = Number(userStore.userId);
    if (isNaN(senderId)) {
        throw new Error('用户ID无效');
    }

    if (!this.connected || !this.client) {
        console.warn('WebSocket未连接，使用HTTP API发送消息');
        return this.sendMessageHttp(recipientId, content);
    }

    try {
        console.log(`通过WebSocket发送消息 - 发送者ID: ${senderId}, 接收者ID: ${recipientId}`);
        
        // 发送消息
        this.client.publish({
            destination: '/app/chat.sendMessage',
            body: JSON.stringify({
                senderId,
                recipientId,
                content
            })
        });

        // 创建本地临时消息对象以立即显示在发送方UI
        const tempMessage: ChatMessage = {
            id: -new Date().getTime(), // 使用负数时间戳作为临时ID
            conversationId: -1, // 使用临时负数ID
            senderId: senderId,
            senderName: userStore.user?.nickname || userStore.user?.realName || userStore.user?.username || '',
            senderAvatar: userStore.user?.avatar || '',
            recipientId: recipientId,
            recipientName: '', // 接收方信息可能不完整
            recipientAvatar: '',
            content: content,
            sentTime: new Date().toISOString(),
            isRead: false
        };

        // 返回临时消息对象以供调用者立即显示
        return tempMessage;
    } catch (error) {
        console.error('发送消息失败:', error);
        // 降级到HTTP API
        return this.sendMessageHttp(recipientId, content);
    }
  }
  
  // 通过HTTP API发送消息
  private async sendMessageHttp(recipientId: number, content: string): Promise<ChatMessage | void> {
    try {
      console.log(`通过HTTP API发送消息 - 接收者ID: ${recipientId}`);
      
      // 添加重试机制
      let retries = 0;
      const maxRetries = 2;
      
      while (retries <= maxRetries) {
        try {
          const response = await axios.post('/api/chat/send', {
            recipientId,
            content
          });
          
          console.log('HTTP消息发送成功');
          return response.data;
        } catch (error: any) {
          if (error.response && error.response.status === 401) {
            // 认证问题，尝试刷新用户状态
            const userStore = useUserStore();
            console.warn('消息发送失败，可能是认证问题，尝试刷新用户状态');
            
            if (retries < maxRetries) {
              retries++;
              await userStore.fetchUserInfo(true);
              continue;
            }
          }
          
          // 其他错误或重试失败，抛出异常
          throw error;
        }
      }
    } catch (error) {
      console.error('通过HTTP API发送消息失败:', error);
      throw error;
    }
  }
  
  // 发送文件消息
  async sendFileMessage(recipientId: number, file: File, content: string = ''): Promise<ChatMessage | void> {
    try {
      const userStore = useUserStore();
      const senderId = Number(userStore.userId);
      
      if (isNaN(senderId)) {
        throw new Error('用户ID无效');
      }
      
      // 先上传文件
      const fileInfo = await this.uploadChatFile(file);
      
      // 创建本地临时消息对象以立即显示在发送方UI
      const tempMessage: ChatMessage = {
        id: -new Date().getTime(), // 使用负数时间戳作为临时ID
        conversationId: -1, // 使用临时负数ID
        senderId: senderId,
        senderName: userStore.user?.nickname || userStore.user?.realName || userStore.user?.username || '',
        senderAvatar: userStore.user?.avatar || '',
        recipientId: recipientId,
        recipientName: '', // 接收方信息可能不完整
        recipientAvatar: '',
        content: content,
        sentTime: new Date().toISOString(),
        isRead: false,
        fileUrl: fileInfo.fileUrl,
        fileName: fileInfo.fileName,
        fileType: fileInfo.fileType,
        fileSize: fileInfo.fileSize
      };
      
      // 然后发送包含文件信息的消息
      if (!this.connected || !this.client) {
        // 降级到HTTP API
        await this.sendFileMessageHttp(recipientId, fileInfo, content);
        return tempMessage;
      }
      
      this.client.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({
          senderId,
          recipientId,
          content,
          fileUrl: fileInfo.fileUrl,
          fileName: fileInfo.fileName,
          fileType: fileInfo.fileType,
          fileSize: fileInfo.fileSize
        })
      });
      
      return tempMessage;
    } catch (error) {
      console.error('发送文件消息失败:', error);
      throw error;
    }
  }
  
  // 通过HTTP API发送文件消息
  private async sendFileMessageHttp(recipientId: number, fileInfo: any, content: string = '') {
    try {
      const response = await axios.post('/api/chat/send', {
        recipientId,
        content,
        fileUrl: fileInfo.fileUrl,
        fileName: fileInfo.fileName,
        fileType: fileInfo.fileType,
        fileSize: fileInfo.fileSize
      });
      
      return response.data;
    } catch (error) {
      console.error('通过HTTP API发送文件消息失败:', error);
      throw error;
    }
  }
  
  // 上传聊天文件
  async uploadChatFile(file: File) {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await axios.post('/api/chat/upload-file', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      return response.data;
    } catch (error) {
      console.error('上传聊天文件失败:', error);
      throw error;
    }
  }
  
  // 获取与指定用户的会话
  async getConversationWithUser(userId: number) {
    try {
      const response = await axios.get(`/api/chat/conversations/${userId}`);
      return response.data as Conversation;
    } catch (error) {
      console.error('获取与用户的会话失败:', error);
      throw error;
    }
  }
  
  // 获取当前用户的所有会话
  async getConversations() {
    try {
      const response = await axios.get('/api/chat/conversations');
      return response.data as Conversation[];
    } catch (error) {
      console.error('获取会话列表失败:', error);
      throw error;
    }
  }
  
  // 获取未读消息数量
  async getUnreadMessageCount() {
    try {
      const response = await axios.get('/api/chat/unread/count');
      return response.data.count as number;
    } catch (error) {
      console.error('获取未读消息数量失败:', error);
      return 0;
    }
  }
  
  // 获取特定消息之前的消息
  async getMessagesBeforeId(conversationId: number, messageId: number, size: number = 20) {
    try {
      const response = await axios.get(
        `/api/chat/conversations/${conversationId}/messages/before/${messageId}`,
        { params: { size } }
      );
      return response.data as ChatMessage[];
    } catch (error) {
      console.error('获取历史消息失败:', error);
      throw error;
    }
  }
  
  // 标记会话为已读
  async markConversationAsRead(conversationId: number) {
    try {
      // 发送请求标记会话为已读
      const response = await axios.post(`/api/chat/conversations/${conversationId}/read`);
      return response.data;
    } catch (error) {
      console.error('标记会话为已读失败:', error);
      throw error;
    }
  }
  
  // 标记单个消息为已读
  async markMessageAsRead(messageId: number) {
    try {
      // 发送请求标记消息为已读
      const response = await axios.post(`/api/chat/messages/${messageId}/read`);
      return response.data;
    } catch (error) {
      console.error('标记消息为已读失败:', error);
      throw error;
    }
  }
  
  // 获取会话的消息
  async getMessagesForConversation(conversationId: number, page: number = 0, size: number = 20) {
    try {
      const response = await axios.get(
        `/api/chat/conversations/${conversationId}/messages`,
        { params: { page, size } }
      );
      return response.data as ChatMessage[];
    } catch (error) {
      console.error('获取会话消息失败:', error);
      throw error;
    }
  }
  
  // 断开WebSocket连接
  disconnect() {
    if (this.client && (this.connected || this.connecting)) {
      console.log('正在断开WebSocket连接...');
      try {
        this.client.deactivate();
        this.connected = false;
        this.connecting = false;
        console.log('WebSocket连接已断开');
      } catch (error) {
        console.error('断开WebSocket连接时发生错误:', error);
      }
    }
    
    // 停止轮询
    this.stopPolling();
  }
}

// 导出单例
export default new ChatService(); 