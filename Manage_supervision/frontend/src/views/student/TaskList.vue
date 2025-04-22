<template>
  <div class="task-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>我的课题</h3>
          <div class="actions">
            <el-select v-model="priorityFilter" placeholder="优先级筛选" clearable style="width: 120px; margin-right: 10px;">
              <el-option label="高优先级" value="高" />
              <el-option label="中优先级" value="中" />
              <el-option label="低优先级" value="低" />
            </el-select>
            <el-input
              v-model="searchQuery"
              placeholder="搜索课题"
              prefix-icon="el-icon-search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部课题" name="all"></el-tab-pane>
        <el-tab-pane label="初稿" name="初稿"></el-tab-pane>
        <el-tab-pane label="期中" name="期中"></el-tab-pane>
        <el-tab-pane label="终稿" name="终稿"></el-tab-pane>
      </el-tabs>
      
      <el-table
        :data="filteredTasks"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="课题标题" min-width="150" />
        <el-table-column prop="supervisorName" label="督导" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ scope.row.priority || '中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="截止时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewTaskDetails(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
        
        <template #empty>
          <div class="empty-data">
            <el-empty description="暂无课题分配给您" :image-size="120">
              <template #description>
                <p>暂时没有课题分配给您</p>
                <p class="sub-text">课题将由督导员分配，请耐心等待</p>
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
      title="课题详情"
      width="60%"
      destroy-on-close
    >
      <el-descriptions
        v-if="currentTask"
        :column="2"
        border
      >
        <el-descriptions-item label="课题标题">{{ currentTask.title }}</el-descriptions-item>
        <el-descriptions-item label="课题状态">
          <el-tag :type="getStatusType(currentTask.status)">
            {{ currentTask.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="督导">{{ currentTask.supervisorName }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentTask.priority)">
            {{ currentTask.priority || '中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentTask.startTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentTask.endTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="课题描述" :span="2">
          <div class="task-description">{{ currentTask.description || '无描述' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 添加文件上传区域 -->
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
      
      <!-- 添加已提交文件列表 -->
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { View, Edit } from '@element-plus/icons-vue';
import { Task, getMyTasks, getMyTasksByStatus, getTaskSubmissions, deleteSubmission, getMyTaskDetail } from '../../api/task';

// 数据状态
const tasks = ref<Task[]>([]);
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const activeTab = ref<string>('all');
const priorityFilter = ref<string>(''); // 优先级筛选

// 分页设置
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalTasks = ref<number>(0);

// 任务详情
const detailsDialogVisible = ref<boolean>(false);
const currentTask = ref<Task | null>(null);
const submissions = ref<any[]>([]);

// 添加上传文件相关
const uploadHeaders = reactive({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});

// 获取当前用户ID
const currentUserId = JSON.parse(localStorage.getItem('user') || '{}').id;

// 筛选任务
const filteredTasks = computed(() => {
  // 首先确保只显示分配给当前用户的任务
  let result = tasks.value.filter(task => task.assigneeId === currentUserId);
  
  // 优先级筛选
  if (priorityFilter.value) {
    result = result.filter(task => task.priority === priorityFilter.value);
  }
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(task => 
      task.title.toLowerCase().includes(query) || 
      (task.description && task.description.toLowerCase().includes(query))
    );
  }
  
  // 根据优先级和截止时间排序
  result = [...result].sort((a, b) => {
    // 首先按优先级排序（高>中>低）
    const priorityOrder = { '高': 0, '中': 1, '低': 2 };
    const priorityA = priorityOrder[a.priority || '中'];
    const priorityB = priorityOrder[b.priority || '中'];
    
    if (priorityA !== priorityB) {
      return priorityA - priorityB;
    }
    
    // 优先级相同时，按截止时间排序（近的在前）
    if (a.endTime && b.endTime) {
      return new Date(a.endTime).getTime() - new Date(b.endTime).getTime();
    }
    
    // 如果没有截止时间，将其排在后面
    if (!a.endTime && b.endTime) return 1;
    if (a.endTime && !b.endTime) return -1;
    
    return 0;
  });
  
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
    if (activeTab.value === 'all') {
      const result = await getMyTasks();
      tasks.value = result;
    } else {
      const result = await getMyTasksByStatus(activeTab.value);
      tasks.value = result;
    }
  } catch (error) {
    console.error('获取任务失败:', error);
    ElMessage.error('获取任务失败');
  } finally {
    loading.value = false;
  }
};

// 查看任务详情
const viewTaskDetails = async (task: Task) => {
  // 重置文件列表，避免显示上一个任务的文件
  submissions.value = [];
  
  currentTask.value = task;
  detailsDialogVisible.value = true;
  
  // 获取任务提交记录
  try {
    const result = await getTaskSubmissions(task.id);
    submissions.value = result;
  } catch (error) {
    console.error('获取任务提交记录失败:', error);
    ElMessage.error('获取任务提交记录失败');
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 处理标签页切换
const handleTabClick = () => {
  currentPage.value = 1;
  fetchTasks();
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

// 辅助函数
const getStatusType = (status: string): 'primary' | 'success' | 'warning' | 'danger' => {
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

// 删除提交记录
const deleteSubmission = async (submission: any) => {
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
  const isLt50M = file.size / 1024 / 1024 < 50;
  
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB!');
    return false;
  }
  
  return true;
};

// 处理上传成功
const handleUploadSuccess = async (response: any, file: any) => {
  ElMessage.success('文件上传成功，任务已自动变为已完成状态');
  
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
  } catch (error) {
    console.error('获取更新后的任务详情失败:', error);
  }
};

// 处理上传失败
const handleUploadError = (error: any) => {
  console.error('上传失败:', error);
  ElMessage.error('文件上传失败');
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
</script>

<style scoped>
.task-list {
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

.search-input {
  width: 250px;
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

/* 添加的样式 */
.file-upload-section,
.submission-list {
  margin-top: 20px;
}

h3 {
  margin-bottom: 15px;
  font-weight: 500;
  color: #303133;
}
</style> 