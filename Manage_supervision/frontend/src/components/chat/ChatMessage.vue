<template>
  <div :class="['message-container', isCurrentUser ? 'sent' : 'received']">
    <!-- 接收方消息布局 -->
    <template v-if="!isCurrentUser">
      <div class="avatar">
        <el-avatar :size="40" :src="message.senderAvatar || defaultAvatar" />
      </div>
      <div class="message-content">
        <div class="message-sender" v-if="showSender">{{ message.senderName }}</div>
        
        <!-- 文件消息 -->
        <div v-if="isFileMessage" class="message-bubble file-message">
          <div class="file-preview">
            <!-- 图片文件预览 -->
            <div v-if="isImageFile" class="image-preview">
              <img :src="message.fileUrl" @click="previewImage" alt="图片" />
            </div>
            
            <!-- 其他文件类型图标 -->
            <div v-else class="file-icon" :class="fileIconClass">
              <i :class="fileIconName"></i>
            </div>
          </div>
          
          <div class="file-info">
            <div class="file-name">{{ message.fileName }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(message.fileSize || 0) }}</span>
              <a :href="message.fileUrl" target="_blank" download class="download-link">
                <el-button size="small" type="primary">下载</el-button>
              </a>
            </div>
            <div v-if="message.content" class="file-caption">{{ message.content }}</div>
          </div>
          
          <span class="message-time">{{ formattedTime }}</span>
        </div>
        
        <!-- 普通文本消息 -->
        <div v-else class="message-bubble">
          <span class="message-text">{{ message.content }}</span>
          <span class="message-time">{{ formattedTime }}</span>
        </div>
      </div>
    </template>
    
    <!-- 发送方消息布局 -->
    <template v-else>
      <div class="message-content">
        <!-- 文件消息 -->
        <div v-if="isFileMessage" class="message-bubble file-message">
          <div class="file-preview">
            <!-- 图片文件预览 -->
            <div v-if="isImageFile" class="image-preview">
              <img :src="message.fileUrl" @click="previewImage" alt="图片" />
            </div>
            
            <!-- 其他文件类型图标 -->
            <div v-else class="file-icon" :class="fileIconClass">
              <i :class="fileIconName"></i>
            </div>
          </div>
          
          <div class="file-info">
            <div class="file-name">{{ message.fileName }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(message.fileSize || 0) }}</span>
              <a :href="message.fileUrl" target="_blank" download class="download-link">
                <el-button size="small" type="primary">下载</el-button>
              </a>
            </div>
            <div v-if="message.content" class="file-caption">{{ message.content }}</div>
          </div>
          
          <span class="message-time">{{ formattedTime }}</span>
        </div>
        
        <!-- 普通文本消息 -->
        <div v-else class="message-bubble">
          <span class="message-text">{{ message.content }}</span>
          <span class="message-time">{{ formattedTime }}</span>
        </div>
        
        <div class="message-status">
          <el-icon v-if="message.isRead" class="read-icon"><Check /></el-icon>
        </div>
      </div>
      <div class="avatar">
        <el-avatar :size="40" :src="message.senderAvatar || defaultAvatar" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useUserStore } from '../../stores/user';
import { Check } from '@element-plus/icons-vue';
import type { ChatMessage } from '../../services/chat';
import { format } from 'date-fns';
import { ElImageViewer } from 'element-plus';

const props = defineProps<{
  message: ChatMessage;
  showSender?: boolean;
}>();

const userStore = useUserStore();
const defaultAvatar = ref('/images/default-avatar.png');
const imageViewerVisible = ref(false);

// 判断消息是否是当前用户发送的
const isCurrentUser = computed(() => {
  return props.message.senderId === userStore.userId;
});

// 检查是否是文件消息
const isFileMessage = computed(() => {
  return !!props.message.fileUrl;
});

// 检查是否是图片文件
const isImageFile = computed(() => {
  if (!props.message.fileType) return false;
  
  return props.message.fileType.startsWith('image/');
});

// 获取文件图标的样式类
const fileIconClass = computed(() => {
  if (!props.message.fileType) return 'file-default';
  
  if (props.message.fileType.includes('pdf')) {
    return 'file-pdf';
  } else if (props.message.fileType.includes('word') || props.message.fileType.includes('document')) {
    return 'file-word';
  } else if (props.message.fileType.includes('excel') || props.message.fileType.includes('spreadsheet')) {
    return 'file-excel';
  } else if (props.message.fileType.includes('zip') || props.message.fileType.includes('rar') || props.message.fileType.includes('compressed')) {
    return 'file-zip';
  } else if (props.message.fileType.includes('audio')) {
    return 'file-audio';
  } else if (props.message.fileType.includes('video')) {
    return 'file-video';
  } else if (props.message.fileType.includes('text')) {
    return 'file-text';
  } else {
    return 'file-default';
  }
});

// 获取文件图标名称
const fileIconName = computed(() => {
  if (!props.message.fileType) return 'el-icon-document';
  
  if (props.message.fileType.includes('pdf')) {
    return 'el-icon-document-checked';
  } else if (props.message.fileType.includes('word') || props.message.fileType.includes('document')) {
    return 'el-icon-document';
  } else if (props.message.fileType.includes('excel') || props.message.fileType.includes('spreadsheet')) {
    return 'el-icon-document';
  } else if (props.message.fileType.includes('zip') || props.message.fileType.includes('rar') || props.message.fileType.includes('compressed')) {
    return 'el-icon-folder';
  } else if (props.message.fileType.includes('audio')) {
    return 'el-icon-headset';
  } else if (props.message.fileType.includes('video')) {
    return 'el-icon-video-camera';
  } else if (props.message.fileType.includes('text')) {
    return 'el-icon-document-copy';
  } else {
    return 'el-icon-document';
  }
});

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (!bytes) return '未知大小';
  
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

// 预览图片
const previewImage = () => {
  if (isImageFile.value && props.message.fileUrl) {
    // 这里可以实现图片预览逻辑，例如使用 element-plus 的 ImageViewer 组件
    // 或者打开新窗口显示图片
    window.open(props.message.fileUrl, '_blank');
  }
};

// 格式化消息时间
const formattedTime = computed(() => {
  const date = new Date(props.message.sentTime);
  const now = new Date();
  const messageDate = new Date(date);
  
  // 如果是今天发送的消息，只显示时间
  if (
    messageDate.getDate() === now.getDate() &&
    messageDate.getMonth() === now.getMonth() &&
    messageDate.getFullYear() === now.getFullYear()
  ) {
    return format(date, 'HH:mm');
  }
  
  // 如果是昨天发送的
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (
    messageDate.getDate() === yesterday.getDate() &&
    messageDate.getMonth() === yesterday.getMonth() &&
    messageDate.getFullYear() === yesterday.getFullYear()
  ) {
    return '昨天 ' + format(date, 'HH:mm');
  }
  
  // 如果是今年发送的
  if (messageDate.getFullYear() === now.getFullYear()) {
    return format(date, 'MM-dd HH:mm');
  }
  
  // 其他情况显示完整日期
  return format(date, 'yyyy-MM-dd HH:mm');
});
</script>

<style scoped>
.message-container {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-container.sent {
  justify-content: flex-end;
}

.avatar {
  margin: 0 8px;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  position: relative;
  word-break: break-word;
  display: inline-block;
}

.sent .message-bubble {
  background-color: #95ec69;
  margin-right: 8px;
  border-top-right-radius: 4px;
}

.received .message-bubble {
  background-color: #f0f0f0;
  margin-left: 8px;
  border-top-left-radius: 4px;
}

.message-text {
  font-size: 14px;
  line-height: 1.4;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-left: 8px;
  white-space: nowrap;
}

.message-status {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  text-align: right;
}

.read-icon {
  color: #409EFF;
  font-size: 14px;
}

/* 文件消息样式 */
.file-message {
  min-width: 220px;
  display: flex;
  flex-direction: column;
}

.file-preview {
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
}

.image-preview {
  max-width: 200px;
  max-height: 200px;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.file-icon i {
  font-size: 36px;
  color: #606266;
}

.file-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.file-name {
  margin-bottom: 4px;
  font-weight: 500;
  word-break: break-all;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.download-link {
  text-decoration: none;
}

.file-caption {
  margin-top: 8px;
  font-size: 14px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

/* 文件类型特定样式 */
.file-pdf i {
  color: #F56C6C;
}

.file-word i {
  color: #409EFF;
}

.file-excel i {
  color: #67C23A;
}

.file-zip i {
  color: #E6A23C;
}

.file-audio i {
  color: #409EFF;
}

.file-video i {
  color: #F56C6C;
}

.file-text i {
  color: #909399;
}
</style> 