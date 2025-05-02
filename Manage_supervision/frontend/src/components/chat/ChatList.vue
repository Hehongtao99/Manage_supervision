<template>
  <div class="chat-list">
    <el-skeleton v-if="loading" :rows="3" animated />
    
    <div v-else-if="conversations.length === 0" class="empty-state">
      <el-empty description="暂无会话" />
    </div>
    
    <div v-else class="conversation-list">
      <div
        v-for="conversation in conversations"
        :key="conversation.id"
        :class="['conversation-item', { active: activeConversationId === conversation.id }]"
        @click="selectConversation(conversation.id)"
      >
        <div class="avatar">
          <el-badge v-if="conversation.unreadCount > 0" :value="conversation.unreadCount" class="unread-badge">
            <el-avatar 
              :size="50" 
              :src="getOtherUser(conversation).avatar || defaultAvatar"
            />
          </el-badge>
          <el-avatar 
            v-else
            :size="50" 
            :src="getOtherUser(conversation).avatar || defaultAvatar"
          />
        </div>
        
        <div class="conversation-info">
          <div class="conversation-header">
            <span class="conversation-name">{{ getOtherUser(conversation).name }}</span>
            <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
          </div>
          
          <div class="conversation-message">
            <span class="message-preview" v-if="conversation.lastMessage">
              {{ conversation.lastMessage.content }}
            </span>
            <span class="no-message" v-else>暂无消息</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useChatStore } from '../../stores/chat';
import { useUserStore } from '../../stores/user';
import { format, isToday, isYesterday } from 'date-fns';
import type { Conversation } from '../../services/chat';

const userStore = useUserStore();
const chatStore = useChatStore();

defineProps<{
  loading?: boolean;
}>();

// 定义事件
const emit = defineEmits<{
  (e: 'select', conversationId: number): void;
}>();

// 默认头像
const defaultAvatar = '/images/default-avatar.png';

// 活跃的会话ID
const activeConversationId = computed(() => chatStore.activeConversationId);

// 排序后的会话列表
const conversations = computed(() => chatStore.sortedConversations);

// 根据当前用户获取会话中的另一个用户
const getOtherUser = (conversation: Conversation) => {
  const currentUserId = userStore.userId;
  return conversation.user1.id === currentUserId
    ? conversation.user2
    : conversation.user1;
};

// 选择会话
const selectConversation = (conversationId: number) => {
  emit('select', conversationId);
};

// 格式化时间
const formatTime = (timeString: string) => {
  if (!timeString) return '';
  
  const date = new Date(timeString);
  
  if (isToday(date)) {
    return format(date, 'HH:mm');
  } else if (isYesterday(date)) {
    return '昨天';
  } else {
    return format(date, 'MM-dd');
  }
};
</script>

<style scoped>
.chat-list {
  height: 100%;
  overflow-y: auto;
  border-right: 1px solid #e0e0e0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.conversation-list {
  padding: 0;
}

.conversation-item {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f1f1;
  cursor: pointer;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f8f8f8;
}

.conversation-item.active {
  background-color: #f0f7ff;
}

.avatar {
  margin-right: 12px;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.conversation-name {
  font-weight: 500;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.conversation-message {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-message {
  font-style: italic;
  color: #999;
}

.unread-badge :deep(.el-badge__content) {
  background-color: #ff4949;
}
</style> 