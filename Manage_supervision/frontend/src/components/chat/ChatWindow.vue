<template>
  <div class="chat-window">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div v-if="conversation" class="user-info">
        <span class="username">{{ otherUser.name }}</span>
      </div>
      <div v-else class="placeholder">
        <span>请选择一个聊天</span>
      </div>
    </div>
    
    <!-- 聊天内容区 -->
    <div 
      class="chat-content" 
      ref="chatContent"
      @scroll="handleScroll"
    >
      <el-empty v-if="!conversation" description="选择一个联系人开始聊天" />
      
      <template v-else>
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="messages.length === 0" class="empty-conversation">
          <el-empty description="暂无消息" />
        </div>
        
        <template v-else>
          <div v-if="hasMoreMessages" class="load-more">
            <el-button 
              :loading="loadingMore" 
              link 
              @click="loadMoreMessages"
            >
              加载更多
            </el-button>
          </div>
          
          <ChatMessage 
            v-for="(message, index) in messages" 
            :key="message.id"
            :message="message"
            :show-sender="shouldShowSender(message, index)"
          />
        </template>
      </template>
    </div>
    
    <!-- 聊天输入区 -->
    <div class="chat-input" v-if="conversation">
      <div class="input-area">
        <el-input
          v-model="messageContent"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 5 }"
          placeholder="输入消息..."
          @keydown.enter.exact.prevent="sendMessage"
        />
      </div>
      <div class="action-buttons">
        <!-- 文件上传按钮 -->
        <el-upload
          ref="upload"
          class="file-upload"
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileSelected"
        >
          <el-button type="primary" :icon="Upload" plain class="upload-btn">
            文件
          </el-button>
        </el-upload>
        
        <el-button 
          type="primary" 
          :disabled="!messageContent.trim() && !selectedFile" 
          @click="sendMessage"
          :loading="sending"
        >
          发送
        </el-button>
      </div>
      
      <!-- 显示已选择的文件 -->
      <div v-if="selectedFile" class="selected-file">
        <span class="file-name">{{ selectedFile.name }}</span>
        <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
        <el-button 
          type="danger" 
          :icon="Delete" 
          circle 
          plain 
          size="small"
          @click="clearSelectedFile"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue';
import { useChatStore } from '../../stores/chat';
import { useUserStore } from '../../stores/user';
import ChatMessage from './ChatMessage.vue';
import chatService from '../../services/chat';
import type { ChatMessage as ChatMessageType } from '../../services/chat';
import { Upload, Delete } from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';
import { chatEvents } from '../../stores/chat';

// 组件属性
const props = defineProps<{
  conversationId?: number;
}>();

// 组件事件
const emit = defineEmits<{
  (e: 'messageSent'): void;
}>();

// Store
const chatStore = useChatStore();
const userStore = useUserStore();

// 状态变量
const messageContent = ref('');
const chatContent = ref<HTMLElement | null>(null);
const loading = ref(false);
const sending = ref(false);
const loadingMore = ref(false);
const hasMoreMessages = ref(true);
const firstMessageId = ref<number | null>(null);
const selectedFile = ref<File | null>(null);
const upload = ref<any>(null);

// 计算当前会话
const conversation = computed(() => {
  if (!props.conversationId) return null;
  return chatStore.conversations.find(c => c.id === props.conversationId) || null;
});

// 获取另一个用户信息
const otherUser = computed(() => {
  if (!conversation.value) return { id: 0, name: '', avatar: '' };
  
  const currentUserId = userStore.userId;
  return conversation.value.user1.id === currentUserId
    ? conversation.value.user2
    : conversation.value.user1;
});

// 获取消息列表
const messages = computed(() => {
  if (!props.conversationId) return [];
  return chatStore.getMessagesForConversation(props.conversationId);
});

// 判断是否应该显示发送者名称（只在连续消息的第一条显示）
const shouldShowSender = (message: ChatMessageType, index: number) => {
  if (index === 0) return true;
  
  const prevMessage = messages.value[index - 1];
  return prevMessage.senderId !== message.senderId;
};

// 监听消息滚动
const handleScroll = () => {
  if (!chatContent.value) return;
  
  const { scrollTop } = chatContent.value;
  
  // 当滚动到顶部附近时，自动加载更多消息
  if (scrollTop < 50 && hasMoreMessages.value && !loadingMore.value) {
    loadMoreMessages();
  }
};

// 加载更多消息
const loadMoreMessages = async () => {
  if (!props.conversationId || !firstMessageId.value || loadingMore.value || !hasMoreMessages.value) return;
  
  loadingMore.value = true;
  
  try {
    // 记录当前滚动位置和高度
    const { scrollHeight } = chatContent.value!;
    
    // 加载更早的消息
    const loadedCount = await chatStore.loadOlderMessages(props.conversationId, firstMessageId.value);
    
    if (loadedCount === 0 || loadedCount < 20) {
      hasMoreMessages.value = false;
    }
    
    // 等待DOM更新
    await nextTick();
    
    // 保持滚动位置（考虑新增内容的高度）
    if (chatContent.value) {
      const newScrollHeight = chatContent.value.scrollHeight;
      chatContent.value.scrollTop = newScrollHeight - scrollHeight;
    }
    
    // 更新第一条消息ID
    updateFirstMessageId();
  } catch (error) {
    console.error('Failed to load more messages:', error);
  } finally {
    loadingMore.value = false;
  }
};

// 更新第一条消息ID
const updateFirstMessageId = () => {
  if (messages.value.length > 0) {
    firstMessageId.value = messages.value[0].id;
  }
};

// 发送消息
const sendMessage = async () => {
  if ((!messageContent.value.trim() && !selectedFile.value) || !props.conversationId || sending.value) return;
  
  sending.value = true;
  
  try {
    // 如果有文件，发送文件消息
    if (selectedFile.value) {
      const tempMessage = await chatService.sendFileMessage(
        otherUser.value.id, 
        selectedFile.value, 
        messageContent.value.trim()
      ) as ChatMessage;
      
      // 如果成功获取临时消息，立即添加到会话中
      if (tempMessage && props.conversationId) {
        // 通过 store 处理临时文件消息
        const conversation = chatStore.conversations.find(c => c.id === props.conversationId);
        if (conversation) {
          // 初始化消息列表（如果不存在）
          if (!chatStore.messages[conversation.id]) {
            chatStore.messages[conversation.id] = [];
          }
          
          // 添加临时消息到消息列表
          chatStore.messages[conversation.id].push(tempMessage);
          
          // 更新会话的最后消息和时间
          conversation.lastMessage = tempMessage;
          conversation.lastMessageTime = tempMessage.sentTime;
          
          // 发布消息添加事件
          chatEvents.emit('messageAdded', {
            conversationId: conversation.id,
            message: tempMessage
          });
        }
      }
      
      // 清除已选文件
      clearSelectedFile();
    } else {
      // 发送纯文本消息
      await chatStore.sendMessage(otherUser.value.id, messageContent.value.trim());
    }
    
    // 清空消息内容
    messageContent.value = '';
    emit('messageSent');
    
    // 滚动到底部
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('Failed to send message:', error);
    ElNotification({
      title: '发送失败',
      message: error instanceof Error ? error.message : '消息发送失败，请重试',
      type: 'error'
    });
  } finally {
    sending.value = false;
  }
};

// 处理文件选择
const handleFileSelected = (file: any) => {
  selectedFile.value = file.raw;
  
  // 如果文件太大，显示警告
  if (file.size > 20 * 1024 * 1024) { // 20MB
    ElNotification({
      title: '文件过大',
      message: '文件大小不能超过20MB',
      type: 'warning'
    });
  }
};

// 清除已选文件
const clearSelectedFile = () => {
  selectedFile.value = null;
  if (upload.value) {
    upload.value.clearFiles();
  }
};

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes < 1024) {
    return bytes + ' B';
  } else if (bytes < 1024 * 1024) {
    return (bytes / 1024).toFixed(1) + ' KB';
  } else if (bytes < 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  } else {
    return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (chatContent.value) {
    // 使用setTimeout确保在DOM完全更新后执行滚动
    setTimeout(() => {
      if (chatContent.value) {
        chatContent.value.scrollTop = chatContent.value.scrollHeight;
        console.log('滚动到底部完成');
      }
    }, 50);
  }
};

// 处理新消息回调
const handleNewMessage = (message: ChatMessageType) => {
  // 如果是当前会话的消息，滚动到底部
  if (message.conversationId === props.conversationId) {
    nextTick(() => {
      scrollToBottom();
    });
  }
};

// 监听消息列表变化，使用更快的响应时间
watch(() => messages.value.length, () => {
  console.log('消息数量变化，准备滚动到底部');
  nextTick(() => {
    scrollToBottom();
  });
}, { immediate: true });

// 加载会话消息
const loadConversationMessages = async () => {
  if (!props.conversationId) return;
  
  loading.value = true;
  
  try {
    // 设置活跃的会话
    await chatStore.setActiveConversation(props.conversationId);
    
    // 更新第一条消息ID
    updateFirstMessageId();
    
    // 滚动到底部
    scrollToBottom();
  } catch (error) {
    console.error('Failed to load conversation messages:', error);
  } finally {
    loading.value = false;
  }
};

// 监听conversationId变化
watch(() => props.conversationId, (newId) => {
  if (newId) {
    hasMoreMessages.value = true;
    loadConversationMessages();
  }
}, { immediate: true });

// 组件挂载时
onMounted(() => {
  // 注册消息监听
  const unsubscribe = chatService.onMessage(handleNewMessage);
  
  // 监听消息添加事件
  const unsubscribeMessageAdded = chatEvents.on('messageAdded', (data: any) => {
    if (data.conversationId === props.conversationId) {
      nextTick(() => {
        scrollToBottom();
      });
    }
  });
  
  // 监听消息更新事件
  const unsubscribeMessageUpdated = chatEvents.on('messageUpdated', (data: any) => {
    if (data.conversationId === props.conversationId) {
      nextTick(() => {
        scrollToBottom();
      });
    }
  });
  
  // 监听直接接收到的消息事件(WebSocket)
  const unsubscribeMessageReceived = chatEvents.on('messageReceived', (message: ChatMessageType) => {
    if (message.conversationId === props.conversationId) {
      console.log('直接接收到新消息，准备滚动到底部', message);
      nextTick(() => {
        scrollToBottom();
      });
    }
  });
  
  // 创建一个MutationObserver来监听内容区域的DOM变化
  if (chatContent.value) {
    const observer = new MutationObserver((mutations) => {
      // 当有子节点添加时，很可能是新消息
      if (mutations.some(mutation => mutation.type === 'childList' && mutation.addedNodes.length > 0)) {
        scrollToBottom();
      }
    });
    
    // 开始观察
    observer.observe(chatContent.value, {
      childList: true,    // 观察直接子节点变化
      subtree: true,      // 观察所有后代节点
      attributes: false,  // 不观察属性变化
      characterData: false // 不观察文本内容变化
    });
    
    // 组件卸载时停止观察
    onUnmounted(() => {
      observer.disconnect();
    });
  }
  
  // 组件卸载时取消监听
  onUnmounted(() => {
    if (unsubscribe) unsubscribe();
    unsubscribeMessageAdded();
    unsubscribeMessageUpdated();
    unsubscribeMessageReceived();
  });
  
  // 如果有会话ID，加载会话并滚动到底部
  if (props.conversationId) {
    loadConversationMessages().then(() => {
      nextTick(() => {
        scrollToBottom();
      });
    });
  }
});
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f5f5f5;
  overflow: hidden !important;
  position: relative;
}

.chat-header {
  padding: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  z-index: 10;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  font-weight: 500;
  font-size: 16px;
  margin-left: 10px;
}

.placeholder {
  color: #999;
  font-style: italic;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.chat-input {
  background-color: #fff;
  border-top: 1px solid #e0e0e0;
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.input-area {
  margin-bottom: 8px;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-upload {
  margin-right: auto;
}

.upload-btn {
  display: flex;
  align-items: center;
}

.selected-file {
  display: flex;
  align-items: center;
  margin-top: 8px;
  padding: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
}

.file-name {
  font-weight: 500;
  margin-right: 8px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: #999;
  font-size: 12px;
  margin-right: auto;
}

.load-more {
  text-align: center;
  margin-bottom: 16px;
}

.loading-container {
  padding: 16px;
}

.empty-conversation {
  display: flex;
  height: 100%;
  justify-content: center;
  align-items: center;
}

.read-icon {
  margin-left: 4px;
  font-size: 12px;
  color: #67c23a;
}
</style> 