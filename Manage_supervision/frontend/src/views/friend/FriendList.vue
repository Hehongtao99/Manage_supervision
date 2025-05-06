<template>
  <div class="friend-page">
    <div class="friend-container">
      <div class="friend-sidebar">
        <div class="sidebar-header">
          <h2>好友列表</h2>
          <el-button type="primary" size="small" @click="openSearchDialog">
            添加好友
          </el-button>
        </div>
        <!-- 好友列表 -->
        <div class="friend-list">
          <el-skeleton v-if="loading" :rows="3" animated />
          
          <div v-else-if="friends.length === 0" class="empty-state">
            <el-empty description="暂无好友" />
          </div>
          
          <div v-else class="friend-items">
            <div
              v-for="friend in friends"
              :key="friend.id"
              :class="['friend-item', { active: selectedFriendId === friend.id }]"
              @click="selectFriend(friend)"
            >
              <div class="avatar">
                <el-avatar 
                  :size="50" 
                  :src="friend.avatar || defaultAvatar"
                />
              </div>
              
              <div class="friend-info">
                <div class="friend-name">{{ friend.name }}</div>
                <div class="friend-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click.stop="startChatWithFriend(friend)"
                  >
                    聊天
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click.stop="confirmDeleteFriend(friend)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 好友请求列表 -->
      <div class="friend-requests">
        <div class="request-header">
          <h3>
            好友请求
            <el-badge v-if="friendStore.unreadRequestCount > 0" :value="friendStore.unreadRequestCount" class="unread-badge" />
          </h3>
        </div>
        
        <el-tabs v-model="activeTab" class="request-tabs" @tab-click="handleTabClick">
          <el-tab-pane label="收到的请求" name="received">
            <el-skeleton v-if="loadingReceivedRequests" :rows="3" animated />
            
            <div v-else-if="receivedRequests.length === 0" class="empty-state">
              <el-empty description="暂无收到的请求" />
            </div>
            
            <div v-else class="request-items">
              <div
                v-for="request in receivedRequests"
                :key="request.id"
                class="request-item"
              >
                <div class="avatar">
                  <el-avatar 
                    :size="40" 
                    :src="request.fromUser?.avatar || defaultAvatar"
                  />
                </div>
                
                <div class="request-info">
                  <div class="request-name">{{ request.fromUser?.name || request.fromUser?.username }}</div>
                  <div class="request-message">{{ request.message || '请求添加您为好友' }}</div>
                  <div class="request-actions">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="handleFriendRequest(request.id, true)"
                      :disabled="request.status !== 0"
                    >
                      {{ request.status === 0 ? '接受' : (request.status === 1 ? '已接受' : '已拒绝') }}
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      @click="handleFriendRequest(request.id, false)"
                      :disabled="request.status !== 0"
                    >
                      {{ request.status === 0 ? '拒绝' : '已处理' }}
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="发送的请求" name="sent">
            <el-skeleton v-if="loadingSentRequests" :rows="3" animated />
            
            <div v-else-if="sentRequests.length === 0" class="empty-state">
              <el-empty description="暂无发送的请求" />
            </div>
            
            <div v-else class="request-items">
              <div
                v-for="request in sentRequests"
                :key="request.id"
                class="request-item"
              >
                <div class="avatar">
                  <el-avatar 
                    :size="40" 
                    :src="request.toUser?.avatar || defaultAvatar"
                  />
                </div>
                
                <div class="request-info">
                  <div class="request-name">{{ request.toUser?.name || request.toUser?.username }}</div>
                  <div class="request-message">{{ request.message || '请求添加对方为好友' }}</div>
                  <div class="request-status">
                    <el-tag 
                      :type="request.status === 0 ? 'info' : (request.status === 1 ? 'success' : 'danger')"
                    >
                      {{ request.status === 0 ? '等待处理' : (request.status === 1 ? '已接受' : '已拒绝') }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <!-- 搜索用户对话框 -->
    <el-dialog
      v-model="showSearchDialog"
      title="添加好友"
      width="500px"
    >
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户"
        prefix-icon="Search"
        clearable
        @input="handleSearch"
      />
      
      <el-table
        :data="searchResults"
        style="width: 100%; margin-top: 16px;"
        height="350px"
        v-loading="searching"
      >
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column fixed="right" label="操作" width="160">
          <template #default="scope">
            <el-button 
              v-if="!scope.row.isFriend && !scope.row.hasPendingRequest" 
              link 
              type="primary" 
              @click="sendFriendRequest(scope.row)"
            >
              添加好友
            </el-button>
            <el-tag v-else-if="scope.row.isFriend" type="success">已是好友</el-tag>
            <el-tag v-else-if="scope.row.hasPendingRequest" type="info">请求已发送</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 发送好友请求对话框 -->
    <el-dialog
      v-model="showRequestDialog"
      title="发送好友请求"
      width="400px"
    >
      <p class="request-to">发送给: {{ selectedUser?.name }}</p>
      
      <el-form>
        <el-form-item label="验证消息">
          <el-input
            v-model="requestMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入验证消息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRequestDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmSendRequest">发送</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 删除好友确认对话框 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="删除好友"
      width="400px"
    >
      <p>确定要删除好友 "{{ selectedFriend?.name }}" 吗？</p>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeleteDialog = false">取消</el-button>
          <el-button type="danger" @click="deleteFriend">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import axios from '../../utils/axios';
import { useChatStore } from '../../stores/chat';
import { useFriendStore, friendEvents } from '../../stores/friend';

const router = useRouter();
const chatStore = useChatStore();
const friendStore = useFriendStore();

// 状态
const loading = computed(() => friendStore.loading);
const friends = computed(() => friendStore.friends);
const selectedFriendId = ref<number | null>(null);
const selectedFriend = ref<any | null>(null);
const defaultAvatar = '/images/default-avatar.png';

// 好友请求状态
const activeTab = ref('received');
const receivedRequests = computed(() => friendStore.receivedRequests);
const sentRequests = computed(() => friendStore.sentRequests);
const loadingReceivedRequests = ref(false);
const loadingSentRequests = ref(false);

// 搜索用户状态
const showSearchDialog = ref(false);
const searchKeyword = ref('');
const searchResults = ref<any[]>([]);
const searching = ref(false);

// 好友请求对话框状态
const showRequestDialog = ref(false);
const selectedUser = ref<any | null>(null);
const requestMessage = ref('');

// 删除好友对话框状态
const showDeleteDialog = ref(false);

// 选择好友
const selectFriend = (friend: any) => {
  selectedFriendId.value = friend.id;
  selectedFriend.value = friend;
};

// 打开搜索对话框
const openSearchDialog = () => {
  showSearchDialog.value = true;
  searchKeyword.value = '';
  searchResults.value = [];
};

// 搜索用户
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = [];
    return;
  }
  
  searching.value = true;
  
  try {
    const result = await friendStore.searchUsers(searchKeyword.value);
    if (result.success) {
      searchResults.value = result.data;
    } else {
      ElMessage.error(result.message || '搜索用户失败');
    }
  } catch (error) {
    console.error('Failed to search users:', error);
    ElMessage.error('搜索用户失败');
  } finally {
    searching.value = false;
  }
};

// 发送好友请求
const sendFriendRequest = (user: any) => {
  selectedUser.value = user;
  requestMessage.value = '';
  showRequestDialog.value = true;
};

// 确认发送好友请求
const confirmSendRequest = async () => {
  if (!selectedUser.value) return;
  
  try {
    const result = await friendStore.sendFriendRequest(selectedUser.value.id, requestMessage.value);
    
    if (result.success) {
      ElMessage.success('好友请求已发送');
      showRequestDialog.value = false;
      
      // 刷新搜索结果
      handleSearch();
    } else {
      ElMessage.error(result.message || '发送好友请求失败');
    }
  } catch (error) {
    console.error('Failed to send friend request:', error);
    ElMessage.error('发送好友请求失败');
  }
};

// 处理好友请求
const handleFriendRequest = async (requestId: number, accept: boolean) => {
  try {
    const result = await friendStore.processFriendRequest(requestId, accept);
    
    if (result.success) {
      ElMessage.success(accept ? '已接受好友请求' : '已拒绝好友请求');
    } else {
      ElMessage.error(result.message || '处理好友请求失败');
    }
  } catch (error) {
    console.error('Failed to process friend request:', error);
    ElMessage.error('处理好友请求失败');
  }
};

// 确认删除好友
const confirmDeleteFriend = (friend: any) => {
  selectedFriend.value = friend;
  showDeleteDialog.value = true;
};

// 删除好友
const deleteFriend = async () => {
  if (!selectedFriend.value) return;
  
  try {
    const result = await friendStore.deleteFriend(selectedFriend.value.id);
    
    if (result.success) {
      ElMessage.success('已删除好友');
      showDeleteDialog.value = false;
      
      // 清除选择
      selectedFriendId.value = null;
      selectedFriend.value = null;
    } else {
      ElMessage.error(result.message || '删除好友失败');
    }
  } catch (error) {
    console.error('Failed to delete friend:', error);
    ElMessage.error('删除好友失败');
  }
};

// 开始与好友聊天
const startChatWithFriend = async (friend: any) => {
  try {
    const conversation = await chatStore.getOrCreateConversationWithUser(friend.id);
    router.push({ name: 'chat', params: { conversationId: conversation.id.toString() } });
  } catch (error) {
    console.error('Failed to start chat:', error);
    ElMessage.error('开始聊天失败');
  }
};

// 实时通知事件处理
const setupNotificationHandlers = () => {
  // 监听新的好友请求
  const newRequestHandler = friendEvents.on('newFriendRequest', (request) => {
    // 刷新列表并提示用户
    // 这部分已经在FriendStore中处理
  });
  
  // 监听好友请求状态更新
  const requestUpdateHandler = friendEvents.on('friendRequestUpdated', (request) => {
    // 刷新列表
    // 这部分已经在FriendStore中处理
  });
  
  // 返回清理函数
  return () => {
    newRequestHandler();
    requestUpdateHandler();
  };
};

// 清除未读计数
const clearUnreadCount = () => {
  if (activeTab.value === 'received' && friendStore.unreadRequestCount > 0) {
    friendStore.clearUnreadRequestCount();
  }
};

// 初始化
onMounted(async () => {
  // 初始化好友系统
  await friendStore.initFriendSystem();
  
  // 设置通知处理
  const cleanupNotifications = setupNotificationHandlers();
  
  // 设置标签页改变时的回调
  const handleTabChange = () => clearUnreadCount();
  
  // 清理函数
  onUnmounted(() => {
    cleanupNotifications();
  });
});

// 当切换到接收请求标签页时，清除未读计数
const handleTabClick = () => {
  clearUnreadCount();
};
</script>

<style scoped>
.friend-page {
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

.friend-container {
  display: flex;
  height: 100%;
  max-height: 100vh;
  overflow: hidden !important;
  flex: 1;
}

.friend-sidebar {
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

.friend-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.friend-items {
  padding: 0;
}

.friend-item {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f1f1;
  cursor: pointer;
  transition: background-color 0.2s;
}

.friend-item:hover {
  background-color: #f8f8f8;
}

.friend-item.active {
  background-color: #f0f7ff;
}

.avatar {
  margin-right: 12px;
}

.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.friend-name {
  font-weight: 500;
  font-size: 15px;
  margin-bottom: 8px;
}

.friend-actions {
  display: flex;
  gap: 8px;
}

.friend-requests {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e0e0e0;
  overflow: hidden;
}

.request-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.request-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.unread-badge :deep(.el-badge__content) {
  background-color: #ff4949;
  margin-left: 8px;
}

.request-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.request-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.request-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-item {
  display: flex;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.request-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.request-name {
  font-weight: 500;
  font-size: 15px;
  margin-bottom: 4px;
}

.request-message {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  word-break: break-word;
}

.request-actions {
  display: flex;
  gap: 8px;
}

.request-status {
  margin-top: 8px;
}

.request-to {
  margin-bottom: 16px;
  font-weight: 500;
}
</style> 