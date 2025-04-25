<template>
  <div class="notification-management-container">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="showCreateNotificationDialog">
        <el-icon><Plus /></el-icon>发送通知
      </el-button>
    </div>

    <el-card class="notification-list">
      <template #header>
        <div class="card-header">
          <span>已发送的通知</span>
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
        <el-table :data="filteredNotifications" style="width: 100%" :default-sort="{ prop: 'createTime', order: 'descending' }">
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <el-tooltip :content="row.content" placement="top" :show-after="500">
                <div class="notification-title">{{ row.title }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="recipientType" label="接收者" width="120">
            <template #default="{ row }">
              <el-tag :type="row.recipientType === 'ALL' ? 'danger' : 'primary'">
                {{ row.recipientType === 'ALL' ? '全体' : row.className }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="发送时间" width="180" sortable>
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
    
    <!-- 创建通知对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="发送通知"
      width="600px"
      destroy-on-close
    >
      <el-form ref="notificationFormRef" :model="notificationForm" :rules="notificationRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入通知内容"
          />
        </el-form-item>
        
        <el-form-item label="接收者" prop="recipientType">
          <el-radio-group v-model="notificationForm.recipientType">
            <el-radio label="ALL">全体通知</el-radio>
            <el-radio label="CLASS">班级通知</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="班级" prop="classId" v-if="notificationForm.recipientType === 'CLASS'">
          <el-select v-model="notificationForm.classId" placeholder="请选择班级">
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.className"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitNotification">发送</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="通知详情"
      width="500px"
    >
      <div v-if="selectedNotification" class="notification-detail">
        <h3 class="detail-title">{{ selectedNotification.title }}</h3>
        
        <div class="detail-meta">
          <span class="recipient">
            <el-tag :type="selectedNotification.recipientType === 'ALL' ? 'danger' : 'primary'" size="small">
              {{ selectedNotification.recipientType === 'ALL' ? '全体通知' : '班级通知: ' + selectedNotification.className }}
            </el-tag>
          </span>
          <span class="time">发送时间: {{ formatTime(selectedNotification.createTime) }}</span>
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
import { Plus, Search } from '@element-plus/icons-vue';
import { ElMessage, FormInstance } from 'element-plus';
import { createNotification, getSentNotifications, NotificationResponse } from '@/api/notification';
import { getClassesByTeacher } from '@/api/class';

// 数据列表
const notifications = ref<NotificationResponse[]>([]);
const classList = ref<any[]>([]);
const selectedNotification = ref<NotificationResponse | null>(null);

// 过滤和搜索
const searchKeyword = ref('');
const filterType = ref('');

// 对话框控制
const createDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const submitting = ref(false);

// 表单
const notificationFormRef = ref<FormInstance>();
const notificationForm = ref({
  title: '',
  content: '',
  recipientType: 'ALL',
  classId: undefined as number | undefined
});

// 表单验证规则
const notificationRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2到50个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度应在5到500个字符之间', trigger: 'blur' }
  ],
  recipientType: [
    { required: true, message: '请选择接收者类型', trigger: 'change' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ]
};

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
      item.content.toLowerCase().includes(keyword)
    );
  }
  
  return result;
});

// 初始化数据
onMounted(async () => {
  await Promise.all([
    loadNotifications(),
    loadClasses()
  ]);
});

// 加载通知列表 - 已发送通知
const loadNotifications = async () => {
  try {
    const res = await getSentNotifications();
    console.log('获取已发送通知响应:', res);
    
    if (res && res.data && res.data.success) {
      notifications.value = res.data.data;
    } else {
      console.error('获取通知列表失败:', res);
      ElMessage.error((res && res.data && res.data.message) || '获取通知列表失败');
    }
  } catch (error) {
    console.error('获取通知列表失败:', error);
    ElMessage.error('获取通知列表失败，请稍后重试');
  }
};

// 加载教师关联的班级列表
const loadClasses = async () => {
  try {
    const res = await getClassesByTeacher();
    if (res.data.success) {
      classList.value = res.data.data;
    }
  } catch (error) {
    console.error('获取班级列表失败', error);
    ElMessage.error('获取班级列表失败');
  }
};

// 显示创建通知对话框
const showCreateNotificationDialog = () => {
  notificationForm.value = {
    title: '',
    content: '',
    recipientType: 'ALL',
    classId: undefined
  };
  createDialogVisible.value = true;
};

// 提交创建通知
const submitNotification = async () => {
  if (!notificationFormRef.value) return;
  
  await notificationFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    // 如果是班级通知，但未选择班级
    if (notificationForm.value.recipientType === 'CLASS' && !notificationForm.value.classId) {
      ElMessage.warning('请选择班级');
      return;
    }
    
    submitting.value = true;
    
    try {
      console.log('准备发送通知:', notificationForm.value);
      const res = await createNotification(notificationForm.value);
      console.log('通知发送响应:', res);
      
      if (res && res.data && res.data.success) {
        ElMessage.success('通知发送成功');
        createDialogVisible.value = false;
        await loadNotifications();
      } else if (res && res.data) {
        ElMessage.error(res.data.message || '发送失败');
      } else {
        ElMessage.error('通知发送失败，未收到服务器响应');
      }
    } catch (error: any) {
      console.error('发送通知失败', error);
      ElMessage.error(error.message || '发送通知失败，请稍后重试');
    } finally {
      submitting.value = false;
    }
  });
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
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
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