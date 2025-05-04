<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h2>消息</h2>
        </div>
        <ChatList 
          :loading="loadingConversations"
          @select="handleSelectConversation"
        />
      </div>
      <div class="chat-main">
        <ChatWindow 
          :conversation-id="activeConversationId"
          @message-sent="handleMessageSent"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '../../stores/user';
import { useChatStore } from '../../stores/chat';
import ChatList from '../../components/chat/ChatList.vue';
import ChatWindow from '../../components/chat/ChatWindow.vue';

const userStore = useUserStore();
const chatStore = useChatStore();

// 状态
const activeConversationId = ref<number | null>(null);
const loadingConversations = computed(() => chatStore.loadingConversations);

// 选择会话
const handleSelectConversation = (conversationId: number) => {
  activeConversationId.value = conversationId;
};

// 发送消息后处理
const handleMessageSent = () => {
  // 这里可以添加任何消息发送后的逻辑
};

// 初始化
onMounted(async () => {
  // 初始化聊天服务
  await chatStore.initChat();
  
  // 加载会话列表
  await chatStore.loadConversations();
  
  // 如果有会话，选择第一个
  if (chatStore.conversations.length > 0) {
    activeConversationId.value = chatStore.conversations[0].id;
  }
});
</script>

<style scoped>
.chat-page {
  height: 100vh;
  max-height: 100vh;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.chat-container {
  display: flex;
  height: 100%;
  max-height: 100vh;
  overflow: hidden !important;
  flex: 1;
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.chat-main {
  flex: 1;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}
</style> 