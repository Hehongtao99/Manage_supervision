<template>
  <transition name="notification-fade">
    <div 
      v-if="visible" 
      class="chat-notification"
      @click="handleClick"
    >
      <div class="notification-avatar">
        <el-avatar :size="40" :src="notification.sender.avatar">
          {{ notification.sender.name?.charAt(0) }}
        </el-avatar>
      </div>
      <div class="notification-content">
        <div class="notification-title">{{ notification.title }}</div>
        <div class="notification-message">{{ notification.message }}</div>
      </div>
      <div class="notification-close" @click.stop="close">
        <el-icon><Close /></el-icon>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, defineProps, defineEmits } from 'vue';
import { Close } from '@element-plus/icons-vue';
import { useChatStore, chatEvents } from '../../stores/chat';

const props = defineProps({
  duration: {
    type: Number,
    default: 4000
  }
});

const emit = defineEmits(['click']);

const visible = ref(false);
const notification = ref({
  title: '',
  message: '',
  sender: {
    id: 0,
    name: '',
    avatar: ''
  }
});

// 显示通知
const showNotification = (data: {
  title: string;
  message: string;
  sender: {
    id: number;
    name: string;
    avatar: string;
  }
}) => {
  console.log('ChatNotification 收到通知:', data);
  notification.value = data;
  visible.value = true;
  
  // 自动关闭
  if (props.duration > 0) {
    setTimeout(() => {
      close();
    }, props.duration);
  }
};

// 关闭通知
const close = () => {
  visible.value = false;
};

// 点击通知处理
const handleClick = () => {
  emit('click', notification.value.sender.id);
  close();
};

let unsubscribe: Function | undefined;

// 组件挂载时监听通知事件
onMounted(() => {
  console.log('ChatNotification组件已挂载，开始监听notification事件');
  unsubscribe = chatEvents.on('notification', showNotification);
});

// 组件卸载时取消事件监听
onUnmounted(() => {
  console.log('ChatNotification组件卸载，取消监听');
  if (unsubscribe) {
    unsubscribe();
  }
});
</script>

<style scoped>
.chat-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 300px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  cursor: pointer;
  z-index: 9999;
}

.notification-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
  font-size: 15px;
}

.notification-message {
  font-size: 14px;
  color: #606266;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-close {
  padding: 4px;
  color: #909399;
  transition: color 0.3s;
  margin-left: 8px;
  flex-shrink: 0;
}

.notification-close:hover {
  color: #409EFF;
}

.chat-notification:hover {
  background-color: #f9f9f9;
}

/* 过渡动画 */
.notification-fade-enter-active,
.notification-fade-leave-active {
  transition: all 0.3s;
}

.notification-fade-enter-from,
.notification-fade-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style> 