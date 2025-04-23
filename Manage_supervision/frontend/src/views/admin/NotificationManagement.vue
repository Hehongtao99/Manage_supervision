<template>
  <div class="notification-management-container">
    <div class="page-header">
      <h2>通知管理</h2>
    </div>

    <el-card class="notification-list">
      <template #header>
        <div class="card-header">
          <span>全部通知</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索通知"
              clearable
              prefix-icon="Search"
              style="width: 220px"
            />
            <el-select v-model="filterType" placeholder="类型" style="width: 120px; margin-left: 10px;">
              <el-option label="全部" value="" />
              <el-option label="全体通知" value="ALL" />
              <el-option label="班级通知" value="CLASS" />
            </el-select>
          </div>
        </div>
      </template>

      <el-empty v-if="filteredNotifications.length === 0" description="暂无通知记录" />

      <div v-else>
        <el-table :data="filteredNotifications" style="width: 100%">
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <el-tooltip :content="row.content" placement="top" :show-after="500">
                <div class="notification-title">{{ row.title }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="senderName" label="发送者" width="120">
            <template #default="{ row }">
              {{ row.senderName }}
            </template>
          </el-table-column>
          
          <el-table-column prop="recipientType" label="接收者" width="120">
            <template #default="{ row }">
              <el-tag :type="row.recipientType === 'ALL' ? 'danger' : 'primary'">
                {{ row.recipientType === 'ALL' ? '全体' : row.className }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="发送时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="viewNotificationDetail(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="通知详情"
      width="500px"
    >
      <div v-if="selectedNotification" class="notification-detail">
        <h3 class="detail-title">{{ selectedNotification.title }}</h3>
        
        <div class="detail-meta">
          <div class="sender-info">
            <el-avatar :size="32" :src="selectedNotification.senderAvatar" class="sender-avatar">
              {{ selectedNotification.senderName?.charAt(0) }}
            </el-avatar>
            <span class="sender-name">{{ selectedNotification.senderName }}</span>
          </div>
          
          <div class="notification-info">
            <span class="recipient">
              <el-tag :type="selectedNotification.recipientType === 'ALL' ? 'danger' : 'primary'" size="small">
                {{ selectedNotification.recipientType === 'ALL' ? '全体通知' : '班级通知: ' + selectedNotification.className }}
              </el-tag>
            </span>
            <span class="time">发送时间: {{ formatTime(selectedNotification.createTime) }}</span>
          </div>
        </div>
        
        <div class="detail-content">
          {{ selectedNotification.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getAllNotifications, NotificationResponse } from '@/api/notification';

// 数据列表
const notifications = ref<NotificationResponse[]>([]);
const selectedNotification = ref<NotificationResponse | null>(null);

// 过滤和搜索
const searchKeyword = ref('');
const filterType = ref('');

// 对话框控制
const detailDialogVisible = ref(false);

// 过滤通知列表
const filteredNotifications = computed(() => {
  let result = notifications.value;
  
  // 按类型筛选
  if (filterType.value) {
    result = result.filter(item => item.recipientType === filterType.value);
  }
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(item => 
      item.title.toLowerCase().includes(keyword) || 
      item.content.toLowerCase().includes(keyword) ||
      item.senderName.toLowerCase().includes(keyword)
    );
  }
  
  return result;
});

// 初始化数据
onMounted(async () => {
  await loadAllNotifications();
});

// 加载所有通知
const loadAllNotifications = async () => {
  try {
    const res = await getAllNotifications();
    if (res.data.success) {
      notifications.value = res.data.data;
    }
  } catch (error) {
    console.error('获取通知列表失败', error);
    ElMessage.error('获取通知列表失败');
  }
};

// 查看通知详情
const viewNotificationDetail = (notification: NotificationResponse) => {
  selectedNotification.value = notification;
  detailDialogVisible.value = true;
};

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.notification-management-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-weight: 500;
  color: #303133;
}

.notification-list {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.notification-title {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-detail {
  padding: 0 20px;
}

.detail-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #303133;
}

.detail-meta {
  margin-bottom: 20px;
}

.sender-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.sender-avatar {
  margin-right: 8px;
}

.sender-name {
  font-weight: 500;
}

.notification-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #303133;
  padding: 16px;
  background-color: #f8f8f8;
  border-radius: 4px;
  min-height: 100px;
}
</style> 