<template>
  <div class="parent-teacher-message-container">
    <el-card class="message-card">
      <template #header>
        <div class="card-header">
          <h2>与教师留言</h2>
          <div class="header-actions">
            <el-button type="primary" @click="openNewMessageDialog">发送新留言</el-button>
            <el-button @click="loadMessages">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 教师列表信息展示 -->
      <div class="teacher-list-info">
        <h3>您的教师列表</h3>
        <div v-if="teachersList.length === 0" class="no-teachers">
          <el-empty description="暂无教师信息" />
        </div>
        <div v-else class="teacher-list">
          <el-tag
            v-for="teacher in teachersList"
            :key="teacher.id"
            type="success"
            effect="light"
            class="teacher-tag"
          >
            {{ teacher.name }}
          </el-tag>
        </div>
      </div>
      
      <!-- 留言历史记录 -->
      <div class="message-history">
        <h3>留言历史记录</h3>
        <el-table
          v-loading="loading"
          :data="messages"
          style="width: 100%"
          empty-text="暂无留言记录"
        >
          <el-table-column prop="teacherName" label="教师" width="120"></el-table-column>
          <el-table-column prop="studentName" label="相关学生" width="120"></el-table-column>
          <el-table-column prop="content" label="留言内容" show-overflow-tooltip></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发送时间" width="160">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button size="small" @click="viewMessage(scope.row)">查看</el-button>
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
      </div>
    </el-card>
    
    <!-- 发送新留言对话框 -->
    <el-dialog v-model="newMessageDialogVisible" title="发送留言给教师" width="50%">
      <el-form :model="newMessageForm" label-width="80px" :rules="rules" ref="newMessageFormRef">
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="newMessageForm.teacherId" placeholder="请选择教师" style="width: 100%">
            <el-option
              v-for="teacher in teachersList"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
            <template #empty>
              <div class="no-data">
                <p>暂无教师数据</p>
              </div>
            </template>
          </el-select>
        </el-form-item>
        
        <el-form-item label="留言内容" prop="content">
          <el-input
            v-model="newMessageForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入留言内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newMessageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitNewMessage" :loading="submitting">发送</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看留言详情对话框 -->
    <el-dialog
      v-model="messageDialogVisible"
      title="留言详情"
      width="50%"
    >
      <div v-if="currentMessage" class="message-detail">
        <div class="message-header">
          <div>
            <strong>接收教师：</strong> {{ currentMessage.teacherName }}
          </div>
          <div>
            <strong>发送时间：</strong> {{ formatDate(currentMessage.createTime) }}
          </div>
        </div>
        <div class="message-student" v-if="currentMessage.studentName">
          <strong>相关学生：</strong> {{ currentMessage.studentName }}
        </div>
        <div class="message-content">
          <strong>留言内容：</strong>
          <div class="content-box">{{ currentMessage.content }}</div>
        </div>
        
        <div v-if="currentMessage.replyContent" class="message-reply">
          <div class="reply-header">
            <strong>教师回复：</strong>
            <span>{{ formatDate(currentMessage.replyTime) }}</span>
          </div>
          <div class="content-box">{{ currentMessage.replyContent }}</div>
        </div>
        
        <div v-if="!currentMessage.replyContent" class="no-reply">
          <el-tag type="info">等待教师回复</el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, FormInstance } from 'element-plus';
import { sendMessage, getParentMessages, markAllRepliesAsReadByParent } from '@/api/parentTeacherMessage';
import { getUserInfo } from '@/api/user';
import { getChildrenList } from '@/api/parent';
import { getTeachersForStudent } from '@/api/student';

// 当前用户信息
const userInfo = ref<any>({});

// 教师列表
const teachersList = ref<any[]>([]);

// 留言列表
const loading = ref(false);
const messages = ref<any[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const messageDialogVisible = ref(false);
const currentMessage = ref<any>(null);

// 新留言对话框
const newMessageDialogVisible = ref(false);
const newMessageFormRef = ref<FormInstance>();
const newMessageForm = reactive({
  teacherId: null as number | null,
  content: '',
});
const submitting = ref(false);

// 表单验证规则
const rules = {
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }]
};

// 加载用户信息
const loadUserInfo = async () => {
  try {
    userInfo.value = await getUserInfo();
  } catch (error) {
    console.error('获取用户信息失败', error);
  }
};

// 加载教师列表
const loadTeachersList = async () => {
  try {
    // 获取子女列表
    const response = await getChildrenList();
    const relations = response.relations || [];
    
    console.log('获取到的子女关系数据:', relations);
    
    if (!relations.length) {
      console.log('未找到子女关系');
      teachersList.value = [];
      return;
    }
    
    // 合并所有子女的教师
    const allTeachers = new Map<number, {id: number, name: string}>();
    
    for (const relation of relations) {
      if (!relation.childId) continue;
      
      try {
        const childId = relation.childId;
        console.log(`获取子女 ${childId} 的教师列表`);
        
        const teachers = await getTeachersForStudent(childId);
        console.log(`子女 ${childId} 的教师列表:`, teachers);
        
        if (Array.isArray(teachers)) {
          for (const teacher of teachers) {
            const id = teacher.id;
            const name = teacher.realName || teacher.username;
            
            if (id && name) {
              console.log(`添加教师: ID=${id}, 名称=${name}`);
              allTeachers.set(id, { id, name });
            }
          }
        }
      } catch (error) {
        console.error(`获取子女 ${relation.childId} 的教师列表失败:`, error);
      }
    }
    
    teachersList.value = Array.from(allTeachers.values());
    console.log('合并后的教师列表:', teachersList.value);
    
    // 如果教师列表为空，添加一个测试教师用于调试
    if (teachersList.value.length === 0) {
      console.warn('教师列表为空，添加一个测试教师供调试使用');
      teachersList.value.push({ id: 3, name: '李白' });
    }
  } catch (error) {
    console.error('加载教师列表失败:', error);
    teachersList.value = [];
    
    // 添加测试教师用于调试
    console.warn('出错后添加测试教师供调试使用');
    teachersList.value.push({ id: 3, name: '李白(测试)' });
  }
};

// 加载留言列表
const loadMessages = async () => {
  loading.value = true;
  try {
    console.log('开始加载家长留言列表');
    const response = await getParentMessages(currentPage.value - 1, pageSize.value);
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

// 打开新留言对话框
const openNewMessageDialog = () => {
  newMessageDialogVisible.value = true;
  // 重置表单
  if (newMessageFormRef.value) {
    newMessageFormRef.value.resetFields();
  }
};

// 提交新留言
const submitNewMessage = async () => {
  if (!newMessageFormRef.value) return;
  
  await newMessageFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const { teacherId, content } = newMessageForm;
        
        if (teacherId) {
          // 这里我们直接传一个固定的studentId，因为在后端我们修改了不需要选择子女
          const studentId = 0; // 或者从用户的第一个子女中获取，如果有的话
          
          console.log('准备发送留言:', { teacherId, studentId, content });
          
          try {
            await sendMessage(teacherId, studentId, content);
            ElMessage.success('留言发送成功');
            newMessageDialogVisible.value = false;
            
            // 发送成功后重新加载留言列表
            loadMessages();
          } catch (error: any) {
            console.error('发送留言失败', error);
            let errorMsg = '发送留言失败';
            
            if (error.response) {
              console.error('服务器响应:', error.response.status, error.response.data);
              errorMsg += `，服务器响应：${error.response.status}`;
              if (error.response.data && error.response.data.message) {
                errorMsg += ` - ${error.response.data.message}`;
              }
            } else if (error.message) {
              errorMsg += `：${error.message}`;
            }
            
            ElMessage.error(errorMsg);
          }
        } else {
          ElMessage.error('请选择教师');
        }
      } catch (error) {
        console.error('提交表单时出错', error);
        ElMessage.error('提交表单时出错，请稍后重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 查看留言详情
const viewMessage = (message: any) => {
  currentMessage.value = message;
  messageDialogVisible.value = true;
  
  // 如果有未读回复，标记为已读
  if (message.replyContent && !message.replyRead) {
    // 标记回复为已读
    markAllRepliesAsReadByParent().then(() => {
      // 刷新列表
      loadMessages();
    }).catch(error => {
      console.error('标记回复已读失败', error);
    });
  }
};

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'unread': 'info',
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

// 加载初始数据
onMounted(async () => {
  await loadUserInfo();
  await loadTeachersList();
  await loadMessages();
});
</script>

<style scoped>
.parent-teacher-message-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.teacher-list-info {
  margin-top: 20px;
  margin-bottom: 30px;
}

.teacher-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.teacher-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.no-teachers {
  padding: 20px;
  text-align: center;
}

.no-data {
  text-align: center;
  padding: 20px 0;
  color: #909399;
}

.message-history {
  margin-top: 20px;
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
</style> 