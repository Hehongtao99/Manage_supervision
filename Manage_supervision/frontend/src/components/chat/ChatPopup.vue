<template>
  <div 
    class="chat-popup" 
    :class="{ 
      'chat-popup-open': visible || embedded,
      'chat-popup-embedded': embedded
    }"
  >
    <div class="chat-popup-header">
      <div class="chat-title">
        <el-avatar :size="32" :src="recipientAvatar">{{ recipientName.charAt(0) }}</el-avatar>
        <span class="chat-name">{{ recipientName }}</span>
      </div>
      <div class="chat-actions" v-if="!embedded">
        <el-button type="text" @click="$emit('close')">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="chat-popup-body" ref="messagesContainer">
      <div v-if="loading" class="chat-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载消息中...
      </div>
      
      <div v-else-if="messages.length === 0" class="chat-empty">
        <el-empty description="暂无消息，开始聊天吧" :image-size="60" />
      </div>
      
      <template v-else>
        <div 
          v-for="message in messages" 
          :key="message.id"
          class="chat-message"
          :class="{'chat-message-self': message.senderId === currentUserId}"
        >
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.sentTime) }}</div>
          </div>
        </div>
      </template>
    </div>
    
    <div class="chat-popup-footer">
      <el-input
        v-model="messageText"
        placeholder="输入消息..."
        :disabled="sending"
        @keyup.enter.prevent="sendMessage"
        resize="none"
        maxlength="500"
        show-word-limit
      />
      <el-button 
        type="primary" 
        :loading="sending" 
        :disabled="!messageText.trim()" 
        @click="sendMessage"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue';
import { useChatStore } from '../../stores/chat';
import { useUserStore } from '../../stores/user';
import { Close, Loading } from '@element-plus/icons-vue';
import type { ChatMessage } from '../../services/chat';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  recipientId: {
    type: Number,
    required: true
  },
  recipientName: {
    type: String,
    required: true
  },
  recipientAvatar: {
    type: String,
    default: ''
  },
  embedded: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close', 'message-sent']);

const chatStore = useChatStore();
const userStore = useUserStore();
const currentUserId = computed(() => userStore.userId);

const messagesContainer = ref<HTMLElement | null>(null);
const messageText = ref('');
const loading = ref(false);
const sending = ref(false);
const conversationId = ref<number | null>(null);
const messages = ref<ChatMessage[]>([]);

// 监听窗口可见性变化，当窗口打开时加载消息
watch(() => props.visible, async (isVisible) => {
  if (isVisible && props.recipientId) {
    await loadMessages();
  }
});

// 监听embedded属性，当为嵌入式模式时加载消息
watch(() => props.embedded, async (isEmbedded) => {
  if (isEmbedded && props.recipientId) {
    await loadMessages();
  }
});

// 监听消息变化，自动滚动到底部
watch(() => messages.value.length, () => {
  scrollToBottom();
});

// 加载消息
async function loadMessages() {
  if (!props.recipientId) return;
  
  loading.value = true;
  messages.value = []; // 清空之前的消息
  
  try {
    console.log('开始加载与用户', props.recipientId, '的消息');
    // 获取或创建与用户的会话
    const conversation = await chatStore.getOrCreateConversationWithUser(props.recipientId);
    conversationId.value = conversation.id;
    console.log('获取到会话ID:', conversation.id);
    
    // 加载会话消息
    await chatStore.loadMessagesForConversation(conversation.id);
    
    // 更新本地消息列表
    messages.value = chatStore.getMessagesForConversation(conversation.id);
    console.log('加载到消息数量:', messages.value.length);
    
    // 如果有未读消息，标记为已读
    if (conversation.unreadCount > 0) {
      console.log('标记会话未读消息为已读');
      await chatStore.markConversationAsRead(conversation.id);
    }
  } catch (error) {
    console.error('加载消息失败:', error);
  } finally {
    loading.value = false;
    await nextTick();
    scrollToBottom();
  }
}

// 发送消息
async function sendMessage() {
  if (!messageText.value.trim() || sending.value) return;
  
  sending.value = true;
  try {
    await chatStore.sendMessage(props.recipientId, messageText.value);
    messageText.value = '';
    
    // 如果已经加载了会话，需要更新本地消息列表
    if (conversationId.value) {
      messages.value = chatStore.getMessagesForConversation(conversationId.value);
    }
    
    emit('message-sent');
  } catch (error) {
    console.error('发送消息失败:', error);
  } finally {
    sending.value = false;
    await nextTick();
    scrollToBottom();
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messagesContainer.value) {
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
      }
    });
  }
}

// 格式化时间
function formatTime(time: string): string {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // 如果是今天的消息，只显示时间
  if (diff < 24 * 60 * 60 * 1000 && 
      date.getDate() === now.getDate() && 
      date.getMonth() === now.getMonth() &&
      date.getFullYear() === now.getFullYear()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  
  // 如果是昨天的消息
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.getDate() === yesterday.getDate() && 
      date.getMonth() === yesterday.getMonth() &&
      date.getFullYear() === yesterday.getFullYear()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 如果是今年的消息
  if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + 
           ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  
  // 其他情况，显示完整日期
  return date.toLocaleDateString('zh-CN') + 
         ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

// 组件挂载时，如果是嵌入式模式，则加载消息
onMounted(async () => {
  // 如果是嵌入式模式且有接收者ID，自动加载消息
  if (props.embedded && props.recipientId) {
    console.log('嵌入式模式，自动加载消息');
    await loadMessages();
  }
  
  // 订阅新消息通知
  chatStore.onMessage((message: ChatMessage) => {
    if (conversationId.value && message.conversationId === conversationId.value) {
      // 自动更新消息列表
      messages.value = chatStore.getMessagesForConversation(conversationId.value);
      
      // 如果消息是对方发的且未读，修改本地状态为已读，但不调用API
      if (message.senderId === props.recipientId && !message.isRead) {
        // 查找消息并在本地标记为已读，不调用API避免错误
        const messageIndex = messages.value.findIndex(m => m.id === message.id);
        if (messageIndex !== -1) {
          messages.value[messageIndex].isRead = true;
        }
      }
    }
  });
});
</script>

<style scoped>
.chat-popup {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 360px;
  height: 480px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 2000;
  transform: translateY(100%);
  opacity: 0;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.chat-popup-open {
  transform: translateY(0);
  opacity: 1;
}

/* 当是嵌入式显示时，使用类选择器 */
.chat-popup-embedded {
  position: static;
  width: 100%;
  height: 100%;
  transform: none !important;
  opacity: 1 !important;
  border-radius: 0;
  box-shadow: none;
}

.chat-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  background-color: #fff;
}

.chat-title {
  display: flex;
  align-items: center;
}

.chat-name {
  margin-left: 10px;
  font-weight: 500;
}

.chat-popup-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background-color: #f5f7fa;
  scrollbar-width: thin;
  display: flex;
  flex-direction: column;
}

.chat-popup-footer {
  padding: 12px 16px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  background-color: #fff;
}

.chat-popup-footer .el-input {
  flex: 1;
  margin-right: 8px;
}

.chat-message {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.chat-message-self {
  align-items: flex-end;
}

.message-content {
  max-width: 70%;
  padding: 10px 12px;
  border-radius: 12px;
  background-color: #e0e0e0;
  position: relative;
  word-break: break-word;
}

.chat-message-self .message-content {
  background-color: #409eff;
  color: #fff;
}

.message-text {
  word-break: break-word;
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.chat-message-self .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.chat-loading, .chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.chat-loading .el-icon {
  margin-bottom: 8px;
  font-size: 24px;
}

/* 滚动条美化 */
.chat-popup-body::-webkit-scrollbar {
  width: 6px;
}

.chat-popup-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 6px;
}

.chat-popup-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 6px;
}

.chat-popup-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 