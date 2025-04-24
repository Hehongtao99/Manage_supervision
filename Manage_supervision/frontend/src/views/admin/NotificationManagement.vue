<template>
  <div class="notification-management-container">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="showSendNotificationDialog">发送通知</el-button>
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
              <el-option label="家长通知" value="PARENT" />
              <el-option label="学生通知" value="STUDENT" />
              <el-option label="教师通知" value="TEACHER" />
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
              <el-tag :type="getTagType(row.recipientType)">
                {{ getRecipientTypeText(row.recipientType) }}
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
              <el-tag :type="getTagType(selectedNotification.recipientType)" size="small">
                {{ getRecipientTypeText(selectedNotification.recipientType) }}
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

    <!-- 发送通知对话框 -->
    <el-dialog
      v-model="sendDialogVisible"
      title="发送通知"
      width="600px"
    >
      <el-form :model="notificationForm" label-width="80px" label-position="top">
        <el-form-item label="接收对象" required>
          <el-radio-group v-model="notificationForm.recipientGroup">
            <el-radio label="ALL">全体用户</el-radio>
            <el-radio label="PARENT">所有家长</el-radio>
            <el-radio label="STUDENT">所有学生</el-radio>
            <el-radio label="TEACHER">所有教师</el-radio>
            <el-radio label="CLASS">指定班级</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="班级" v-if="notificationForm.recipientGroup === 'CLASS'" required>
          <el-select v-model="notificationForm.classId" placeholder="请选择班级">
            <el-option 
              v-for="item in classList" 
              :key="item.id" 
              :label="item.className" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" required>
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>

        <el-form-item label="内容" required>
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入通知内容"
          ></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="sendDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="sendNotification" :loading="sendingNotification">
            发送通知
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { 
  getAllNotifications, 
  NotificationResponse, 
  NotificationRequest, 
  createNotification,
  sendToAllParents,
  sendToAllStudents,
  sendToAllTeachers
} from '@/api/notification';
import { getClassesByTeacher } from '@/api/class';

// 数据列表
const notifications = ref<NotificationResponse[]>([]);
const selectedNotification = ref<NotificationResponse | null>(null);
const classList = ref<any[]>([]);

// 过滤和搜索
const searchKeyword = ref('');
const filterType = ref('');

// 对话框控制
const detailDialogVisible = ref(false);
const sendDialogVisible = ref(false);
const sendingNotification = ref(false);

// 通知表单
const notificationForm = ref({
  recipientGroup: 'ALL',
  classId: undefined as number | undefined,
  title: '',
  content: ''
});

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
  await loadClasses();
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

// 加载班级列表
const loadClasses = async () => {
  try {
    const res = await getClassesByTeacher();
    if (res.data.success) {
      classList.value = res.data.data;
    }
  } catch (error) {
    console.error('获取班级列表失败', error);
  }
};

// 显示发送通知对话框
const showSendNotificationDialog = () => {
  notificationForm.value = {
    recipientGroup: 'ALL',
    classId: undefined,
    title: '',
    content: ''
  };
  sendDialogVisible.value = true;
};

// 发送通知
const sendNotification = async () => {
  // 表单验证
  if (!notificationForm.value.title.trim()) {
    ElMessage.warning('请输入通知标题');
    return;
  }
  if (!notificationForm.value.content.trim()) {
    ElMessage.warning('请输入通知内容');
    return;
  }
  if (notificationForm.value.recipientGroup === 'CLASS' && !notificationForm.value.classId) {
    ElMessage.warning('请选择班级');
    return;
  }

  try {
    sendingNotification.value = true;
    
    const request: NotificationRequest = {
      title: notificationForm.value.title,
      content: notificationForm.value.content,
      recipientType: notificationForm.value.recipientGroup === 'CLASS' ? 'CLASS' : 'ALL'
    };
    
    if (notificationForm.value.recipientGroup === 'CLASS') {
      request.classId = notificationForm.value.classId;
    }
    
    let response;
    switch (notificationForm.value.recipientGroup) {
      case 'ALL':
        response = await createNotification(request);
        break;
      case 'PARENT':
        response = await sendToAllParents(request);
        break;
      case 'STUDENT':
        response = await sendToAllStudents(request);
        break;
      case 'TEACHER':
        response = await sendToAllTeachers(request);
        break;
      case 'CLASS':
        response = await createNotification(request);
        break;
    }
    
    if (response.data.success) {
      ElMessage.success('通知发送成功');
      sendDialogVisible.value = false;
      loadAllNotifications();
    } else {
      ElMessage.error(response.data.message || '通知发送失败');
    }
  } catch (error: any) {
    console.error('发送通知失败', error);
    ElMessage.error(error.message || '发送通知失败');
  } finally {
    sendingNotification.value = false;
  }
};

// 查看通知详情
const viewNotificationDetail = (notification: NotificationResponse) => {
  selectedNotification.value = notification;
  detailDialogVisible.value = true;
};

// 获取接收者类型对应的文字
const getRecipientTypeText = (type: string) => {
  switch (type) {
    case 'ALL': return '全体用户';
    case 'CLASS': return '班级';
    case 'PARENT': return '所有家长';
    case 'STUDENT': return '所有学生';
    case 'TEACHER': return '所有教师';
    default: return type;
  }
};

// 获取接收者类型对应的标签类型
const getTagType = (type: string) => {
  switch (type) {
    case 'ALL': return 'danger';
    case 'CLASS': return 'primary';
    case 'PARENT': return 'success';
    case 'STUDENT': return 'warning';
    case 'TEACHER': return 'info';
    default: return '';
  }
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 