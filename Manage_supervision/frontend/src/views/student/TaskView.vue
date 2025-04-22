<template>
  <div class="task-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>我的任务</h3>
          <div class="actions">
            <el-select
              v-model="statusFilter"
              placeholder="任务状态"
              clearable
              class="filter-select"
              @change="handleFilterChange"
            >
              <el-option label="全部" value="" />
              <el-option label="初稿" value="初稿" />
              <el-option label="期中" value="期中" />
              <el-option label="终稿" value="终稿" />
            </el-select>
            
            <el-input
              v-model="searchQuery"
              placeholder="搜索任务"
              prefix-icon="el-icon-search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredTasks"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="supervisorName" label="教师" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getTaskStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="120">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ scope.row.priority || '中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="截止时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewTaskDetails(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button type="success" size="small" @click="uploadTaskFile(scope.row)">
              <el-icon><Upload /></el-icon>上传
            </el-button>
          </template>
        </el-table-column>
        
        <template #empty>
          <div class="empty-data">
            <el-empty description="暂无任务分配给您" :image-size="120">
              <template #description>
                <p>暂时没有任务分配给您</p>
                <p class="sub-text">任务将由教师分配，请耐心等待</p>
              </template>
            </el-empty>
          </div>
        </template>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalTasks"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="任务详情"
      width="60%"
      destroy-on-close
    >
      <el-descriptions
        v-if="currentTask"
        :column="2"
        border
      >
        <el-descriptions-item label="任务标题">{{ currentTask.title }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getTaskStatusType(currentTask.status)">
            {{ currentTask.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="教师">{{ currentTask.supervisorName }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentTask.priority)">
            {{ currentTask.priority || '中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentTask.startTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentTask.endTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">
          <div class="task-description">{{ currentTask.description || '无描述' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 任务评价部分 -->
      <div v-if="currentTask && currentTask.completed" class="task-evaluation-section">
        <div class="section-header">
          <h3>教师评价</h3>
        </div>
        <div v-loading="evaluationLoading">
          <el-empty v-if="!currentEvaluation" description="暂无评价" :image-size="80">
            <template #description>
              <p>教师尚未对此任务进行评价</p>
            </template>
          </el-empty>
          
          <div v-else class="evaluation-container">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="score-container" :class="getScoreClass(currentEvaluation.score)">
                  <div class="score-value">{{ currentEvaluation.score }}</div>
                  <div class="score-label">评分</div>
                  <el-rate
                    v-model="currentEvaluation.score"
                    disabled
                    :max="5"
                    :colors="['#F56C6C', '#E6A23C', '#67C23A']"
                    class="score-stars"
                    text-color="#ff9900"
                    show-score
                  />
                </div>
              </el-col>
              <el-col :span="16">
                <el-card shadow="hover" class="comment-card">
                  <template #header>
                    <div class="comment-header">
                      <span>评语</span>
                      <el-tag size="small" type="info">{{ currentEvaluation.evaluationTime }}</el-tag>
                    </div>
                  </template>
                  <div class="comment-content">
                    {{ currentEvaluation.comment || '教师未留下评语' }}
                  </div>
                  <div class="evaluator-info">
                    <el-tag size="small" type="success">评价人: {{ currentEvaluation.evaluatorName }}</el-tag>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
      
      <!-- 文件上传区域 -->
      <div class="file-upload-section">
        <h3>提交文件</h3>
        <el-upload
          class="upload-demo"
          :action="`/api/user/tasks/${currentTask?.id}/submit`"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
        >
          <el-button type="primary">点击上传</el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持任何类型的文件，大小不超过50MB
            </div>
          </template>
        </el-upload>
      </div>
      
      <!-- 已提交文件列表 -->
      <div v-if="submissions.length > 0" class="submission-list">
        <h3>已提交文件</h3>
        <el-table :data="submissions" border style="width: 100%">
          <el-table-column prop="originalFilename" label="文件名" min-width="200" />
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="submissionTime" label="提交时间" width="160" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="downloadSubmission(scope.row)">
                下载
              </el-button>
              <el-button type="danger" size="small" @click="deleteSubmission(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 上传文件对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文件"
      width="50%"
      destroy-on-close
    >
      <div v-if="currentTask" class="task-info-summary">
        <p><strong>任务标题:</strong> {{ currentTask.title }}</p>
        <p>
          <strong>状态:</strong> 
          <el-tag size="small" :type="getTaskStatusType(currentTask.status)">
            {{ currentTask.status }}
          </el-tag>
        </p>
        <p><strong>截止时间:</strong> {{ currentTask.endTime || '未设置' }}</p>
      </div>
      
      <div class="file-upload-section upload-centered">
        <el-upload
          class="upload-demo"
          :action="`/api/user/tasks/${currentTask?.id}/submit`"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持任何类型的文件，大小不超过50MB<br>
              <span class="error-tip" v-if="uploadError">上传错误: {{ uploadError }}</span>
            </div>
          </template>
        </el-upload>
      </div>
      
      <!-- 已提交文件列表 -->
      <div v-if="submissions.length > 0" class="submission-list">
        <h3>已提交文件</h3>
        <el-table :data="submissions" border style="width: 100%">
          <el-table-column prop="originalFilename" label="文件名" min-width="200" />
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="submissionTime" label="提交时间" width="160" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="downloadSubmission(scope.row)">
                下载
              </el-button>
              <el-button type="danger" size="small" @click="deleteSubmission(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { View, Upload, UploadFilled } from '@element-plus/icons-vue';
import { 
  Task, 
  getMyTasks, 
  getMyTasksByStatus, 
  getMyTaskDetail, 
  updateMyTaskStatus,
  getMyTaskEvaluation,
  type TaskEvaluation
} from '../../api/task';
import { 
  TaskSubmission, 
  getTaskSubmissions, 
  deleteSubmission, 
  submitTaskFile 
} from '../../api/task';

// 数据状态
const tasks = ref<Task[]>([]);
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const statusFilter = ref<string>('');
const uploadError = ref<string>('');

// 分页设置
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalTasks = ref<number>(0);

// 任务详情
const detailsDialogVisible = ref<boolean>(false);
const currentTask = ref<Task | null>(null);
const submissions = ref<TaskSubmission[]>([]);

// 上传文件
const uploadDialogVisible = ref<boolean>(false);

// 上传文件相关
const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  };
});

// 获取当前用户ID
const currentUserId = JSON.parse(localStorage.getItem('user') || '{}').id;

// 评价相关
const currentEvaluation = ref<TaskEvaluation | null>(null);
const evaluationLoading = ref<boolean>(false);

// 筛选任务
const filteredTasks = computed(() => {
  // 首先确保只显示分配给当前用户的任务
  let result = tasks.value.filter(task => task.assigneeId === currentUserId);
  
  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(task => task.status === statusFilter.value);
  }
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(task => 
      task.title.toLowerCase().includes(query) || 
      (task.description && task.description.toLowerCase().includes(query))
    );
  }
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  
  totalTasks.value = result.length;
  return result.slice(start, end);
});

// 初始化数据
onMounted(() => {
  fetchTasks();
});

// 获取任务
const fetchTasks = async () => {
  loading.value = true;
  try {
    const result = statusFilter.value 
      ? await getMyTasksByStatus(statusFilter.value)
      : await getMyTasks();
    
    tasks.value = result;
  } catch (error) {
    console.error('获取任务失败:', error);
    ElMessage.error('获取任务失败');
  } finally {
    loading.value = false;
  }
};

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1;
  fetchTasks();
};

// 获取任务评价
const loadTaskEvaluation = async (taskId: number) => {
  try {
    evaluationLoading.value = true;
    const evaluation = await getMyTaskEvaluation(taskId);
    currentEvaluation.value = evaluation;
  } catch (error: any) {
    // 忽略404或无数据错误，因为可能任务尚未评价
    if (!error.response || error.response.status !== 404) {
      console.error('获取任务评价失败:', error);
    }
    currentEvaluation.value = null;
  } finally {
    evaluationLoading.value = false;
  }
};

// 查看任务详情
const viewTaskDetails = async (task: Task) => {
  try {
    loading.value = true;
    
    // 获取详细任务信息
    const detailedTask = await getMyTaskDetail(task.id);
    currentTask.value = detailedTask;
    
    // 获取任务提交记录
    const taskSubmissions = await getTaskSubmissions(task.id);
    submissions.value = taskSubmissions;
    
    // 如果任务已完成，获取评价信息
    if (detailedTask.completed) {
      await loadTaskEvaluation(task.id);
    }
    
    detailsDialogVisible.value = true;
  } catch (error) {
    console.error('获取任务详情失败:', error);
    ElMessage.error('获取任务详情失败');
  } finally {
    loading.value = false;
  }
};

// 上传任务文件
const uploadTaskFile = async (task: Task) => {
  try {
    // 重置上传错误和文件列表
    uploadError.value = '';
    submissions.value = [];
    
    // 获取完整任务详情（强制刷新以获取最新数据）
    const taskDetail = await getMyTaskDetail(task.id);
    
    // 检查任务是否分配给当前用户
    if (taskDetail.assigneeId !== currentUserId) {
      uploadError.value = '此任务可能不是分配给您的，或者任务分配状态已变更。请联系教师确认。';
      ElMessage.warning(uploadError.value);
      return;
    }
    
    currentTask.value = taskDetail;
    uploadDialogVisible.value = true;
    
    // 获取任务提交记录
    const submissionResult = await getTaskSubmissions(task.id);
    submissions.value = submissionResult;
    
    // 如果任务已完成，显示提示信息但不阻止上传
    if (taskDetail.completed) {
      ElMessage.info('此任务已标记为完成状态，但您仍可以上传新的文件版本。');
    }
  } catch (error: any) {
    console.error('获取任务详情失败:', error);
    
    if (error.response && error.response.data && error.response.data.message) {
      uploadError.value = error.response.data.message;
      ElMessage.warning(error.response.data.message);
    } else if (error.data && error.data.message === '无权查看该任务') {
      uploadError.value = '您没有权限查看此任务，可能该任务不是分配给您的';
      ElMessage.warning('您没有权限查看此任务，可能该任务不是分配给您的');
    } else {
      uploadError.value = '获取任务详情失败';
      ElMessage.error('获取任务详情失败');
    }
  }
};

// 处理上传成功
const handleUploadSuccess = async (response: any, file: any) => {
  uploadError.value = '';
  
  // 添加到提交列表
  submissions.value.push(response);
  
  // 重新获取任务详情以获取最新状态
  try {
    if (currentTask.value) {
      const updatedTask = await getMyTaskDetail(currentTask.value.id);
      
      // 更新本地任务和当前任务
      currentTask.value = updatedTask;
      
      // 更新任务列表
      const index = tasks.value.findIndex(t => t.id === updatedTask.id);
      if (index !== -1) {
        tasks.value[index] = updatedTask;
      }
    }
    
    // 显示成功消息，明确告知状态变更
    ElMessage.success('文件上传成功，任务已自动变为已完成状态');
  } catch (error) {
    console.error('获取更新后的任务详情失败:', error);
    ElMessage.success('文件上传成功');
  }
};

// 处理上传失败
const handleUploadError = (error: any) => {
  console.error('上传失败:', error);
  
  let errorMessage = '文件上传失败';
  
  // 尝试解析详细的错误信息
  if (error.response) {
    console.error('错误响应详情:', error.response);
    
    // 检查响应中是否包含详细信息
    if (error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message;
      
      // 针对常见错误提供更友好的提示
      if (errorMessage.includes('无权提交该任务')) {
        errorMessage = '您没有权限提交此任务。可能是因为该任务不是分配给您的，或者教师对任务进行了修改。请联系您的教师确认任务分配状态。';
      } else if (errorMessage.includes('任务不存在')) {
        errorMessage = '任务不存在或已被删除。请刷新页面或联系教师确认任务状态。';
      }
    } else if (error.response.status === 400) {
      errorMessage = '请求错误 (400): 您可能没有权限上传文件、任务ID无效或任务状态已变更';
    } else if (error.response.status === 401) {
      errorMessage = '未授权 (401): 您的登录已过期，请重新登录后再试';
    } else if (error.response.status === 403) {
      errorMessage = '禁止访问 (403): 您没有权限执行此操作，请联系您的教师';
    } else if (error.response.status === 500) {
      errorMessage = '服务器错误 (500): 服务器处理请求时出错，请稍后再试或联系管理员';
    }
  } else if (error.message) {
    errorMessage = error.message;
  }
  
  uploadError.value = errorMessage;
  ElMessage.error(errorMessage);
  
  // 记录详细错误信息到控制台，便于调试
  console.error('文件上传错误详情:', {
    message: errorMessage,
    response: error.response,
    currentTask: currentTask.value ? {
      id: currentTask.value.id,
      title: currentTask.value.title,
      assigneeId: currentTask.value.assigneeId,
      supervisorId: currentTask.value.supervisorId,
      completed: currentTask.value.completed
    } : 'No task data',
    userInfo: {
      userId: currentUserId
    }
  });
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
};

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B';
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB';
  } else {
    return (size / 1024 / 1024).toFixed(2) + ' MB';
  }
};

// 下载提交文件
const downloadSubmission = (submission: any) => {
  try {
    // 创建a标签下载文件
    const link = document.createElement('a');
    link.href = `/api/user/submissions/${submission.id}/download`;
    link.setAttribute('download', submission.originalFilename);
    link.setAttribute('target', '_blank');
    
    // 添加授权信息
    link.setAttribute('data-token', localStorage.getItem('token') || '');
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('开始下载文件');
  } catch (error) {
    console.error('下载文件失败:', error);
    ElMessage.error('下载文件失败');
  }
};

// 删除提交记录
const deleteSubmission = async (submission: TaskSubmission) => {
  try {
    await deleteSubmission(submission.id);
    
    // 更新提交列表
    submissions.value = submissions.value.filter(s => s.id !== submission.id);
    
    ElMessage.success('提交记录已删除');
  } catch (error) {
    console.error('删除提交记录失败:', error);
    ElMessage.error('删除提交记录失败');
  }
};

// 处理文件上传前的验证
const beforeUpload = (file: File) => {
  uploadError.value = '';
  
  if (!currentTask.value || !currentTask.value.id) {
    uploadError.value = '任务信息不完整，请刷新页面后重试';
    ElMessage.error(uploadError.value);
    return false;
  }
  
  // 检查文件大小限制
  const isLt50M = file.size / 1024 / 1024 < 50;
  
  if (!isLt50M) {
    uploadError.value = '文件大小不能超过 50MB!';
    ElMessage.error(uploadError.value);
    return false;
  }
  
  // 额外确认任务分配状态
  if (currentTask.value.assigneeId !== currentUserId) {
    uploadError.value = '此任务可能不是分配给您的，请刷新页面或联系教师确认';
    ElMessage.error(uploadError.value);
    return false;
  }
  
  // 注意：不再检查任务完成状态，允许学生对已完成的任务继续上传文件
  
  return true;
};

// 辅助函数
const getTaskStatusType = (status: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (status) {
    case '初稿': return 'primary';
    case '期中': return 'warning';
    case '终稿': return 'success';
    default: return 'primary';
  }
};

const getPriorityType = (priority: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (priority) {
    case '高': return 'danger';
    case '中': return 'warning';
    case '低': return 'success';
    default: return 'warning';
  }
};

// 根据评分获取对应的样式类
const getScoreClass = (score: number): string => {
  if (score >= 4) return 'score-excellent';
  if (score >= 3) return 'score-good';
  if (score >= 2) return 'score-average';
  return 'score-poor';
};
</script>

<style scoped>
.task-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  gap: 10px;
}

.filter-select,
.search-input {
  width: 200px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.task-description {
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}

.file-upload-section,
.submission-list {
  margin-top: 20px;
}

.upload-centered {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.task-info-summary {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

h3 {
  margin-bottom: 15px;
  font-weight: 500;
  color: #303133;
}

.sub-text {
  color: #909399;
  font-size: 14px;
}

.empty-data {
  padding: 30px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.task-evaluation-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.section-header {
  margin-bottom: 15px;
  font-weight: 500;
  color: #303133;
}

.evaluation-container {
  margin-top: 15px;
}

.score-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  height: 100%;
  min-height: 180px;
  transition: all 0.3s;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 5px;
}

.score-label {
  font-size: 16px;
  color: #909399;
  margin-bottom: 10px;
}

.score-stars {
  margin-top: 10px;
}

.score-excellent {
  background-color: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.score-good {
  background-color: rgba(230, 162, 60, 0.1);
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.score-average {
  background-color: rgba(144, 147, 153, 0.1);
  border: 1px solid rgba(144, 147, 153, 0.2);
}

.score-poor {
  background-color: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
}

.comment-card {
  height: 100%;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-content {
  min-height: 80px;
  white-space: pre-wrap;
  line-height: 1.5;
  color: #606266;
}

.evaluator-info {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}
</style> 