<template>
  <div class="message-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>收到的家长留言</span>
          <div>
            <el-button type="primary" size="small" @click="markAllAsRead">全部标为已读</el-button>
            <el-button size="small" @click="loadMessages">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 消息表格 -->
      <el-table
        v-loading="loading"
        :data="messages"
        stripe
        style="width: 100%"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="parentName" label="家长姓名" width="120"></el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="content" label="留言内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              size="small"
              @click="viewMessage(scope.row)"
            >
              查看
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="openReplyDialog(scope.row)"
            >
              回复
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 查看留言详情对话框 -->
    <el-dialog
      v-model="messageDialogVisible"
      title="留言详情"
      width="50%"
    >
      <div v-if="currentMessage" class="message-detail">
        <div class="message-header">
          <div>
            <strong>发送人：</strong> {{ currentMessage.parentName }}
          </div>
          <div>
            <strong>发送时间：</strong> {{ formatDate(currentMessage.createTime) }}
          </div>
        </div>
        <div class="message-student">
          <strong>相关学生：</strong> {{ currentMessage.studentName || '未指定' }}
        </div>
        <div class="message-content">
          <strong>留言内容：</strong>
          <div class="content-box">{{ currentMessage.content }}</div>
        </div>
        
        <div v-if="currentMessage.replyContent" class="message-reply">
          <div class="reply-header">
            <strong>回复内容：</strong>
            <span>{{ formatDate(currentMessage.replyTime) }}</span>
          </div>
          <div class="content-box">{{ currentMessage.replyContent }}</div>
        </div>
        
        <div v-if="!currentMessage.replyContent" class="no-reply">
          <el-button type="primary" @click="openReplyDialog(currentMessage)">回复</el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复留言"
      width="50%"
    >
      <el-form
        :model="replyForm"
        :rules="replyRules"
        ref="replyFormRef"
        label-width="80px"
      >
        <div class="message-info">
          <p><strong>家长：</strong> {{ currentMessage?.parentName }}</p>
          <p><strong>学生：</strong> {{ currentMessage?.studentName || '未指定' }}</p>
          <p><strong>留言内容：</strong> {{ currentMessage?.content }}</p>
        </div>
        
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :loading="submitting">
            提交回复
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus';
import { getTeacherMessages, replyMessage, markAllAsReadByTeacher } from '@/api/parentTeacherMessage';

// 状态变量
const loading = ref(false);
const messages = ref<any[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const messageDialogVisible = ref(false);
const replyDialogVisible = ref(false);
const currentMessage = ref<any>(null);
const submitting = ref(false);
const replyFormRef = ref<FormInstance>();

// 回复表单
const replyForm = reactive({
  replyContent: ''
});

// 回复表单验证规则
const replyRules = {
  replyContent: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ]
};

// 生命周期钩子
onMounted(() => {
  loadMessages();
});

// 加载留言列表
const loadMessages = async () => {
  loading.value = true;
  try {
    console.log('开始加载教师留言列表，页码:', currentPage.value - 1, '每页条数:', pageSize.value);
    const response = await getTeacherMessages(currentPage.value - 1, pageSize.value);
    console.log('留言响应数据:', response);
    
    // 检查不同的响应结构可能性
    if (response && response.content) {
      // 标准分页响应结构
      messages.value = response.content;
      total.value = response.totalElements;
      console.log('成功加载留言数量:', response.content.length, '总数:', response.totalElements);
    } else if (response && Array.isArray(response)) {
      // 数组结构响应
      messages.value = response;
      total.value = response.length;
      console.log('成功加载留言数量(数组):', response.length);
    } else if (response && response.data && response.data.content) {
      // 嵌套在data属性中的分页响应
      messages.value = response.data.content;
      total.value = response.data.totalElements;
      console.log('成功加载留言数量(data嵌套):', response.data.content.length, '总数:', response.data.totalElements);
    } else if (response && response.data && Array.isArray(response.data)) {
      // 嵌套在data属性中的数组响应
      messages.value = response.data;
      total.value = response.data.length;
      console.log('成功加载留言数量(data数组):', response.data.length);
    } else {
      console.warn('未能识别的留言数据格式:', response);
      messages.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取留言列表失败', error);
    if (error.response) {
      console.error('错误响应:', error.response.status, error.response.data);
    }
    ElMessage.error('获取留言列表失败，请检查网络或联系管理员');
    messages.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadMessages();
};

// 获取行样式
const getRowClassName = (row: any) => {
  if (row.row.status === 'unread') {
    return 'unread-row';
  }
  return '';
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'unread': 'danger',
    'read': 'info',
    'replied': 'success'
  };
  return types[status] || 'info';
};

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'unread': '未读',
    'read': '已读',
    'replied': '已回复'
  };
  return texts[status] || status;
};

// 查看留言详情
const viewMessage = (row: any) => {
  currentMessage.value = row;
  console.log('查看留言详情:', row);
  messageDialogVisible.value = true;
};

// 打开回复对话框
const openReplyDialog = (row: any) => {
  currentMessage.value = row;
  console.log('打开回复对话框，当前留言:', row);
  replyForm.replyContent = row.replyContent || '';
  replyDialogVisible.value = true;
};

// 提交回复
const submitReply = async () => {
  if (!replyFormRef.value) return;
  
  await replyFormRef.value.validate(async (valid) => {
    if (valid && currentMessage.value) {
      submitting.value = true;
      try {
        console.log('开始提交回复，留言ID:', currentMessage.value.id, '回复内容:', replyForm.replyContent);
        const result = await replyMessage(currentMessage.value.id, replyForm.replyContent);
        console.log('回复成功，服务器返回数据:', result);
        ElMessage.success('回复成功');
        replyDialogVisible.value = false;
        
        // 更新当前消息和列表中的消息
        if (result) {
          const index = messages.value.findIndex(msg => msg.id === currentMessage.value.id);
          if (index !== -1) {
            console.log('更新本地消息列表中的消息');
            // 保存原始的学生信息以防丢失
            const originalStudentName = messages.value[index].studentName;
            messages.value[index] = result;
            
            // 确保学生信息不丢失
            if (!messages.value[index].studentName && originalStudentName) {
              console.log('恢复原始学生信息:', originalStudentName);
              messages.value[index].studentName = originalStudentName;
            }
          }
          
          // 保存原始的学生信息以防丢失
          const originalStudentName = currentMessage.value.studentName;
          currentMessage.value = result;
          
          // 确保学生信息不丢失
          if (!currentMessage.value.studentName && originalStudentName) {
            console.log('恢复当前消息的原始学生信息:', originalStudentName);
            currentMessage.value.studentName = originalStudentName;
          }
        }
        
        // 重新加载消息列表
        loadMessages();
      } catch (error) {
        console.error('回复失败', error);
        ElMessage.error('回复失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 标记所有为已读
const markAllAsRead = async () => {
  try {
    const result = await markAllAsReadByTeacher();
    if (result) {
      ElMessage.success(`已将${result.count || 0}条留言标记为已读`);
      loadMessages();
    }
  } catch (error) {
    console.error('标记已读失败', error);
    ElMessage.error('标记已读失败');
  }
};
</script>

<style scoped>
.message-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.message-detail {
  padding: 10px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.message-student {
  margin-bottom: 10px;
}

.message-content {
  margin-bottom: 20px;
}

.content-box {
  margin-top: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
  white-space: pre-wrap;
}

.message-reply {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px dashed #ddd;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.no-reply {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px dashed #ddd;
  display: flex;
  justify-content: center;
}

.message-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.message-info p {
  margin: 5px 0;
}

:deep(.unread-row) {
  background-color: #fdf6ec !important;
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style> 