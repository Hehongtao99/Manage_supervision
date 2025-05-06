import mitt from 'mitt';
import type { ChatMessage } from './chat';

// 定义事件类型
export type ChatEventType = {
  messageReceived: ChatMessage;
  messageAdded: any;
  messageUpdated: any;
  conversationCreated: number;
  conversationUpdated: number;
  contactUpdated: number;
  friendRequest: any; // 添加好友请求事件类型
};

// 创建事件总线
const chatEvents = mitt<ChatEventType>();

export default chatEvents; 