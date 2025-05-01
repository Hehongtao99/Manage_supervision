<template>
  <div class="task-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>任务管理</h3>
          <div class="actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索任务"
              prefix-icon="el-icon-search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
            <el-button type="primary" @click="openCreateTaskDialog">
              <el-icon><CirclePlus /></el-icon>创建任务
            </el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="task-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="全部任务" name="all"></el-tab-pane>
        <el-tab-pane label="未开始" name="未开始"></el-tab-pane>
        <el-tab-pane label="进行中" name="进行中"></el-tab-pane>
        <el-tab-pane label="已完成" name="已完成"></el-tab-pane>
      </el-tabs>
      
      <el-table
        :data="filteredTasks"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="任务标题" min-width="150" />
        <el-table-column prop="assigneeName" label="负责学生" width="120">
          <template #default="scope">
            <span v-if="scope.row.assigneeName">{{ scope.row.assigneeName }}</span>
            <el-tag v-else type="info">未分配</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewTaskDetails(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button type="success" size="small" @click="openAssignDialog(scope.row)" :disabled="!!scope.row.assigneeId">
              <el-icon><CirclePlus /></el-icon>分配
            </el-button>
            <el-button type="warning" size="small" @click="editTask(scope.row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm
              title="确定要删除此任务吗？"
              @confirm="deleteTaskItem(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
            <el-button type="success" size="small" @click="viewTaskSubmissions(scope.row)">
              <el-icon><Document /></el-icon>文件
            </el-button>
          </template>
        </el-table-column>
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
          <el-tag :type="getStatusType(currentTask.status)">
            {{ currentTask.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责学生">
          <span v-if="currentTask.assigneeName">{{ currentTask.assigneeName }}</span>
          <el-tag v-else type="info">未分配</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentTask.priority)">
            {{ currentTask.priority || '中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentTask.startTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentTask.endTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">
          <div class="task-description">{{ currentTask.description || '无描述' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 任务评价部分 -->
      <div v-if="currentTask && currentTask.completed" class="task-evaluation-section">
        <div class="section-header">
          <h3>任务评价</h3>
        </div>
        <div v-loading="evaluationLoading">
          <el-form v-if="!currentEvaluation" :model="evaluationForm" label-width="80px">
            <el-form-item label="评分">
              <el-rate
                v-model="evaluationForm.score"
                :max="5"
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                :texts="['1分', '2分', '3分', '4分', '5分']"
                show-text
              />
            </el-form-item>
            <el-form-item label="评语">
              <el-input
                v-model="evaluationForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入对学生完成情况的评价"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitEvaluation">提交评价</el-button>
            </el-form-item>
          </el-form>
          
          <el-descriptions v-else border :column="1" class="evaluation-details">
            <el-descriptions-item label="评分">
              <el-rate
                v-model="currentEvaluation.score"
                disabled
                :max="5"
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              />
              {{ currentEvaluation.score }}分
            </el-descriptions-item>
            <el-descriptions-item label="评语">
              {{ currentEvaluation.comment || '无评语' }}
            </el-descriptions-item>
            <el-descriptions-item label="评价时间">
              {{ currentEvaluation.evaluationTime }}
            </el-descriptions-item>
            <el-descriptions-item>
              <el-button type="warning" @click="editEvaluation">修改评价</el-button>
              <el-button type="danger" @click="deleteEvaluation">删除评价</el-button>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <!-- 添加子任务列表 -->
      <div class="sub-tasks-section" v-if="currentTask">
        <div class="section-header">
          <h3>子任务列表</h3>
          <div class="button-group">
            <el-button type="primary" size="small" @click="addSubTaskForCurrentTask">
              <el-icon><Plus /></el-icon>添加子任务
            </el-button>
          </div>
        </div>
        <div v-loading="subTasksLoading">
          <el-table
            :data="subTasks"
            border
            style="width: 100%; margin-top: 15px;"
            v-if="subTasks.length > 0"
          >
            <el-table-column prop="title" label="子任务标题" min-width="150" />
            <el-table-column prop="assigneeName" label="负责学生" width="120">
              <template #default="scope">
                <span v-if="scope.row.assigneeName">{{ scope.row.assigneeName }}</span>
                <el-tag v-else type="info">未分配</el-tag>
              </template>
            </el-table-column>
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
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="warning" size="small" @click="editTask(scope.row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-popconfirm
                  title="确定要删除此子任务吗？"
                  @confirm="deleteTaskItem(scope.row.id)"
                >
                  <template #reference>
                    <el-button type="danger" size="small">
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <div v-else class="empty-subtasks">
            <el-empty description="暂无子任务" :image-size="80">
              <template #description>
                <p>该任务暂无子任务</p>
              </template>
            </el-empty>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
          <el-button 
            type="success" 
            @click="openAssignDialog(currentTask)" 
            :disabled="!!currentTask?.assigneeId"
          >
            分配给学生
          </el-button>
          <el-button type="primary" @click="editTask(currentTask)">编辑任务</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="taskFormDialogVisible"
      :title="isEditing ? '编辑任务' : '创建任务'"
      width="60%"
      destroy-on-close
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="taskForm.description" 
            placeholder="请输入任务描述"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="taskForm.status" placeholder="请选择状态">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" prop="timeRange">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-date-picker
              v-model="startTime"
              type="datetime"
              placeholder="开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 9, 0, 0)"
              style="width: 100%;"
            />
            <span>至</span>
            <el-date-picker
              v-model="endTime"
              type="datetime"
              placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 18, 0, 0)"
              style="width: 100%;"
            />
          </div>
        </el-form-item>
      </el-form>
      
      <!-- 添加子任务相关UI更新 -->
      <div class="task-create-section">
        <div class="section-header">
          <h3>子任务</h3>
          <div class="button-group">
            <el-button type="primary" size="small" @click="openNewSubTaskDialog">
              <el-icon><Plus /></el-icon>添加子任务
            </el-button>
            <el-button type="success" size="small" @click="openBatchSelectTasksDialog">
              <el-icon><Plus /></el-icon>批量选择子任务
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="newTaskList"
          border
          style="width: 100%; margin-top: 15px;"
        >
          <el-table-column prop="title" label="子任务标题" min-width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="scope">
              <el-tag :type="getPriorityType(scope.row.priority)">
                {{ scope.row.priority || '中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="warning" size="small" @click="editNewSubTask(scope.$index)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="danger" size="small" @click="removeNewSubTask(scope.$index)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="newTaskList.length === 0" class="empty-tasks-tip">
          <el-empty description='暂无子任务，请点击"添加子任务"按钮创建' />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="taskFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTaskForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 创建课题时添加子任务对话框 -->
    <el-dialog
      v-model="newSubTaskFormVisible"
      :title="editingSubTaskIndex >= 0 ? '编辑任务' : '添加任务'"
      width="50%"
      destroy-on-close
    >
      <el-form
        ref="newSubTaskFormRef"
        :model="newSubTaskForm"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title" required>
          <el-input v-model="newSubTaskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="newSubTaskForm.description" 
            placeholder="请输入任务描述"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="newSubTaskForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="newSubTaskForm.status" placeholder="请选择状态">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="newSubTaskFormVisible = false">取消</el-button>
          <el-button type="primary" @click="addNewSubTask">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 批量选择任务对话框 -->
    <el-dialog
      v-model="batchSelectTasksVisible"
      title="批量选择任务"
      width="70%"
      destroy-on-close
    >
      <div v-loading="availableTasksLoading">
        <el-input
          v-model="taskSearchQuery"
          placeholder="搜索任务..."
          prefix-icon="el-icon-search"
          clearable
          style="margin-bottom: 15px;"
        />
        
        <el-table
          :data="filteredAvailableTasks"
          border
          style="width: 100%"
          @selection-change="handleTaskSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="任务标题" min-width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="scope">
              <el-tag :type="getPriorityType(scope.row.priority)">
                {{ scope.row.priority || '中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchSelectTasksVisible = false">取消</el-button>
          <el-button type="primary" @click="addSelectedSubTasks">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 分配任务对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配任务给学生"
      width="40%"
      destroy-on-close
    >
      <el-select
        v-model="selectedStudentId"
        placeholder="请选择学生"
        style="width: 100%"
        v-loading="studentsLoading"
      >
        <el-option
          v-for="student in students"
          :key="student.id"
          :label="student.name"
          :value="student.id"
        />
      </el-select>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssignTask">确认分配</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 任务提交记录对话框 -->
    <el-dialog
      v-model="submissionsDialogVisible"
      title="任务提交记录"
      width="70%"
      destroy-on-close
    >
      <div v-if="currentTask" class="task-info">
        <h3>任务信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务标题">{{ currentTask.title }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(currentTask.status)">
              {{ currentTask.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学生">{{ currentTask.assigneeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentTask.startTime || '未设置' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="submissions.length > 0" class="submission-list">
        <h3>提交记录 ({{submissions.length}})</h3>
        <el-table :data="submissions" border style="width: 100%">
          <el-table-column prop="originalFilename" label="文件名" min-width="200" />
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="120" />
          <el-table-column prop="submitterName" label="提交人" width="120" />
          <el-table-column prop="submissionTime" label="提交时间" width="160" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="downloadSubmission(scope.row)">
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="no-submissions">
        <el-empty description="暂无提交记录" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="submissionsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '../../stores/user';
import type { FormInstance, FormRules } from 'element-plus';
import {
  Search,
  View,
  Edit,
  Delete,
  CirclePlus,
  Plus,
  Document
} from '@element-plus/icons-vue';
import {
  createTask,
  updateTask,
  getTaskDetail,
  deleteTask,
  getSupervisorTasks,
  getSupervisorTasksByStatus,
  assignTaskToStudent,
  updateTaskStatus,
  getAvailableTasks,
  type Task,
  type TaskDTO,
  createBatchTasks,
  getTaskSubmissions,
  type TaskSubmission,
  evaluateTask,
  getTaskEvaluation,
  deleteTaskEvaluation,
  type TaskEvaluation
} from '../../api/task';
import { getStudents } from '../../api/user';
import type { Student } from '../../api/user';

const userStore = useUserStore();

// 状态变量
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalTasks = ref<number>(0);
const tasks = ref<Task[]>([]);
const selectedTasks = ref<Task[]>([]);
const currentTask = ref<Task | null>(null);
const detailsDialogVisible = ref<boolean>(false);
const activeTab = ref<string>('all');

// 子任务相关变量
const subTasks = ref<Task[]>([]);
const subTasksLoading = ref<boolean>(false);

// 表单相关变量
const taskFormRef = ref<FormInstance>();
const taskFormDialogVisible = ref<boolean>(false);
const isEditing = ref<boolean>(false);
const taskForm = ref({
  id: null as number | null,
  title: '',
  description: '',
  priority: '中',
  status: '未开始',
  supervisorId: userStore.user.id,
  projectId: null,
  assigneeId: null,
  completed: false
});
const startTime = ref<string | null>(null);
const endTime = ref<string | null>(null);

// 表单验证规则
const taskRules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择任务状态', trigger: 'change' }
  ]
};

// 分配任务相关变量
const assignDialogVisible = ref<boolean>(false);
const selectedStudentId = ref<number | null>(null);
const students = ref<Student[]>([]);
const studentsLoading = ref<boolean>(false);
const taskToAssign = ref<Task | null>(null);

// 添加子任务相关变量
const newTaskList = ref<any[]>([]);
const newSubTaskFormVisible = ref<boolean>(false);
const newSubTaskForm = ref({
  title: '',
  description: '',
  priority: '中',
  status: '未开始'
});
const editingSubTaskIndex = ref<number>(-1);
const newSubTaskFormRef = ref<FormInstance>();

// 批量选择任务对话框相关变量
const batchSelectTasksVisible = ref<boolean>(false);
const availableTasksLoading = ref<boolean>(false);
const taskSearchQuery = ref<string>('');
const availableTasks = ref<Task[]>([]);
const selectedSubTasks = ref<Task[]>([]);

// 任务提交记录
const submissionsDialogVisible = ref<boolean>(false);
const submissions = ref<TaskSubmission[]>([]);

// 评价相关状态
const currentEvaluation = ref<TaskEvaluation | null>(null);
const evaluationForm = ref({
  score: 5,
  comment: ''
});
const evaluationLoading = ref<boolean>(false);

// 计算过滤后的可用任务
const filteredAvailableTasks = computed(() => {
  if (!taskSearchQuery.value) return availableTasks.value;
  
  const query = taskSearchQuery.value.toLowerCase();
  return availableTasks.value.filter(task => 
    task.title?.toLowerCase().includes(query) || 
    task.description?.toLowerCase().includes(query)
  );
});

// 初始化数据
onMounted(() => {
  fetchTasks();
});

// 获取任务数据
const fetchTasks = async () => {
  loading.value = true;
  try {
    let data;
    if (activeTab.value === 'all') {
      data = await getSupervisorTasks(userStore.user.id);
    } else {
      data = await getSupervisorTasksByStatus(userStore.user.id, activeTab.value);
    }
    // 过滤掉子任务（有parentTaskId的任务）
    tasks.value = data.filter(task => !task.parentTaskId);
    totalTasks.value = tasks.value.length;
    ElMessage.success('成功获取任务数据');
  } catch (error) {
    console.error('获取任务数据失败:', error);
    ElMessage.error('获取任务数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取学生列表
const fetchStudents = async () => {
  studentsLoading.value = true;
  try {
    const data = await getStudents();
    students.value = data;
  } catch (error) {
    console.error('获取学生列表失败:', error);
    ElMessage.error('获取学生列表失败');
  } finally {
    studentsLoading.value = false;
  }
};

// 根据搜索过滤任务
const filteredTasks = computed(() => {
  if (!searchQuery.value) return tasks.value;
  
  const query = searchQuery.value.toLowerCase();
  return tasks.value.filter(task => 
    task.title.toLowerCase().includes(query) || 
    (task.description && task.description.toLowerCase().includes(query)) ||
    (task.assigneeName && task.assigneeName.toLowerCase().includes(query))
  );
});

// 处理函数
const handleSearch = () => {
  currentPage.value = 1;
};

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  currentPage.value = 1;
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
};

const handleTabClick = () => {
  fetchTasks();
};

// 查看任务详情
const viewTaskDetails = async (task: Task) => {
  currentTask.value = task;
  detailsDialogVisible.value = true;
  
  // 加载子任务
  await loadSubTasks(task.id);
  
  // 如果任务已完成，加载评价信息
  if (task.completed) {
    await loadTaskEvaluation(task.id);
  }
};

// 创建任务
const openCreateTaskDialog = () => {
  isEditing.value = false;
  taskForm.value = {
    id: null,
    title: '',
    description: '',
    priority: '中',
    status: '未开始',
    supervisorId: userStore.user.id,
    projectId: null,
    assigneeId: null,
    completed: false
  };
  startTime.value = null;
  endTime.value = null;
  
  // 清空任务列表
  newTaskList.value = [];
  
  taskFormDialogVisible.value = true;
};

// 编辑任务
const editTask = (task: Task) => {
  isEditing.value = true;
  taskForm.value = {
    id: task.id,
    title: task.title,
    description: task.description || '',
    priority: task.priority || '中',
    status: task.status,
    supervisorId: task.supervisorId,
    projectId: task.projectId,
    assigneeId: task.assigneeId,
    completed: task.completed
  };
  
  if (task.startTime && task.endTime) {
    startTime.value = task.startTime;
    endTime.value = task.endTime;
  } else {
    startTime.value = null;
    endTime.value = null;
  }
  
  console.log('编辑任务表单数据:', taskForm.value);
  taskFormDialogVisible.value = true;
};

// 提交任务表单
const submitTaskForm = async () => {
  if (!taskFormRef.value) return;
  
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      
      try {
        const taskData = {
          ...taskForm.value
        };
        
        // 添加时间范围
        if (startTime.value && endTime.value) {
          taskData.startTime = startTime.value;
          taskData.endTime = endTime.value;
        }
        
        let result;
        if (isEditing.value && taskForm.value.id) {
          // 更新任务
          result = await updateTask(taskForm.value.id, taskData);
          ElMessage.success('任务更新成功');
          
          // 如果更新的是当前查看的任务，更新当前任务数据
          if (currentTask.value && currentTask.value.id === result.id) {
            currentTask.value = result;
          }
        } else {
          // 创建任务
          result = await createTask(taskData);
          ElMessage.success('任务创建成功');
          
          // 如果有子任务，创建子任务
          if (newTaskList.value.length > 0 && result.id) {
            const mainTaskId = result.id;
            const promises = newTaskList.value.map(task => {
              // 创建子任务数据
              const subTaskData = {
                ...task,
                supervisorId: userStore.user.id,
                parentTaskId: mainTaskId
              };
              
              return createTask(subTaskData);
            });
            
            // 等待所有子任务创建完成
            await Promise.all(promises);
            ElMessage.success(`已成功创建 ${newTaskList.value.length} 个关联任务`);
          }
        }
        
        taskFormDialogVisible.value = false;
        fetchTasks(); // 刷新任务列表
        
        // 如果正在查看任务详情，刷新子任务列表
        if (detailsDialogVisible.value && currentTask.value) {
          refreshSubTasks();
        }
      } catch (error) {
        console.error('提交任务失败:', error);
        ElMessage.error('提交任务失败');
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.error('请填写所有必填项');
    }
  });
};

// 删除任务
const deleteTaskItem = async (id: number) => {
  loading.value = true;
  try {
    const success = await deleteTask(id);
    if (success) {
      ElMessage.success('任务删除成功');
      tasks.value = tasks.value.filter(task => task.id !== id);
      totalTasks.value = tasks.value.length;
      
      // 如果正在查看任务详情，刷新子任务列表
      if (detailsDialogVisible.value && currentTask.value) {
        refreshSubTasks();
      }
    } else {
      ElMessage.error('删除任务失败');
    }
  } catch (error) {
    console.error('删除任务失败:', error);
    ElMessage.error('删除任务失败');
  } finally {
    loading.value = false;
  }
};

// 分配任务给学生
const openAssignDialog = (task: Task) => {
  if (task.assigneeId) {
    ElMessage.warning('该任务已分配给学生');
    return;
  }
  
  taskToAssign.value = task;
  selectedStudentId.value = null;
  fetchStudents();
  assignDialogVisible.value = true;
};

const handleAssignTask = async () => {
  if (!taskToAssign.value || !selectedStudentId.value) {
    ElMessage.warning('请选择要分配的学生');
    return;
  }
  
  loading.value = true;
  try {
    const result = await assignTaskToStudent(taskToAssign.value.id, selectedStudentId.value);
    ElMessage.success('任务分配成功');
    assignDialogVisible.value = false;
    
    // 更新本地数据
    const index = tasks.value.findIndex(t => t.id === taskToAssign.value?.id);
    if (index !== -1) {
      tasks.value[index] = result;
    }
    
    // 如果当前在详情页面，也更新当前任务
    if (currentTask.value && currentTask.value.id === taskToAssign.value.id) {
      currentTask.value = result;
    }
    
    taskToAssign.value = null;
  } catch (error) {
    console.error('分配任务失败:', error);
    ElMessage.error('分配任务失败');
  } finally {
    loading.value = false;
  }
};

// 辅助函数
const getStatusType = (status: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (status) {
    case '未开始': return 'primary';
    case '进行中': return 'warning';
    case '已完成': return 'success';
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

// 添加子任务对话框
const openNewSubTaskDialog = () => {
  // 重置索引
  editingSubTaskIndex.value = -1;
  
  // 初始化任务表单
  newSubTaskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始'
  };
  
  // 打开对话框
  newSubTaskFormVisible.value = true;
};

// 添加新子任务
const addNewSubTask = async () => {
  if (!newSubTaskForm.value.title) {
    ElMessage.warning('请输入任务标题');
    return;
  }
  
  // 如果是在任务详情中添加子任务
  if (detailsDialogVisible.value && currentTask.value) {
    loading.value = true;
    try {
      // 创建子任务数据
      const subTaskData = {
        ...newSubTaskForm.value,
        supervisorId: userStore.user.id,
        parentTaskId: currentTask.value.id
      };
      
      // 直接创建子任务
      await createTask(subTaskData);
      ElMessage.success('子任务创建成功');
      
      // 刷新子任务列表
      await refreshSubTasks();
      
      // 关闭对话框
      newSubTaskFormVisible.value = false;
    } catch (error) {
      console.error('创建子任务失败:', error);
      ElMessage.error('创建子任务失败');
    } finally {
      loading.value = false;
    }
    return;
  }
  
  // 如果正在编辑子任务，则更新该任务
  if (editingSubTaskIndex.value >= 0) {
    newTaskList.value[editingSubTaskIndex.value] = {...newSubTaskForm.value};
    editingSubTaskIndex.value = -1;
  } else {
    // 否则添加新子任务
    newTaskList.value.push({...newSubTaskForm.value});
  }
  
  // 重置表单
  newSubTaskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始'
  };
  
  // 关闭对话框
  newSubTaskFormVisible.value = false;
};

// 编辑创建任务时的子任务
const editNewSubTask = (index: number) => {
  // 设置编辑的索引
  editingSubTaskIndex.value = index;
  
  // 设置表单数据
  const task = newTaskList.value[index];
  newSubTaskForm.value = {
    title: task.title,
    description: task.description || '',
    priority: task.priority || '中',
    status: task.status || '未开始'
  };
  
  // 打开对话框
  newSubTaskFormVisible.value = true;
};

// 删除创建任务时的子任务
const removeNewSubTask = (index: number) => {
  newTaskList.value.splice(index, 1);
};

// 批量选择任务相关函数
// 打开批量选择任务对话框
const openBatchSelectTasksDialog = async () => {
  // 清空已选任务
  selectedSubTasks.value = [];
  
  // 加载可用任务
  availableTasksLoading.value = true;
  try {
    availableTasks.value = await getAvailableTasks(userStore.user.id);
  } catch (error) {
    console.error('获取可用任务失败:', error);
    ElMessage.error('获取可用任务失败');
  } finally {
    availableTasksLoading.value = false;
  }
  
  batchSelectTasksVisible.value = true;
};

// 处理任务选择变更
const handleTaskSelectionChange = (selected: Task[]) => {
  selectedSubTasks.value = selected;
};

// 添加选中的任务到任务列表
const addSelectedSubTasks = () => {
  if (selectedSubTasks.value.length === 0) {
    ElMessage.warning('请至少选择一个任务');
    return;
  }
  
  // 将选中的任务添加到新项目任务列表
  selectedSubTasks.value.forEach(task => {
    // 检查是否已经添加过相同的任务
    const exists = newTaskList.value.some(t => 
      t.title === task.title && t.description === task.description
    );
    
    if (!exists) {
      newTaskList.value.push({
        title: task.title,
        description: task.description,
        priority: task.priority || '中',
        status: task.status || '未开始'
      });
    }
  });
  
  ElMessage.success(`已添加 ${selectedSubTasks.value.length} 个任务`);
  batchSelectTasksVisible.value = false;
};

// 刷新子任务列表
const refreshSubTasks = async () => {
  if (!currentTask.value) return;
  
  subTasksLoading.value = true;
  try {
    const allTasks = await getSupervisorTasks(userStore.user.id);
    subTasks.value = allTasks.filter(t => t.parentTaskId === currentTask.value?.id);
  } catch (error) {
    console.error('刷新子任务失败:', error);
    ElMessage.warning('刷新子任务信息失败');
  } finally {
    subTasksLoading.value = false;
  }
};

// 添加子任务给当前任务
const addSubTaskForCurrentTask = () => {
  // 重置索引
  editingSubTaskIndex.value = -1;
  
  // 初始化任务表单
  newSubTaskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始'
  };
  
  // 打开对话框
  newSubTaskFormVisible.value = true;
};

// 在任务操作部分添加查看提交记录的操作
const viewTaskSubmissions = async (task: Task) => {
  currentTask.value = task;
  submissionsDialogVisible.value = true;
  submissions.value = []; // 清空之前的数据
  
  try {
    console.log('开始获取任务提交记录, 任务ID:', task.id);
    const result = await getTaskSubmissions(task.id);
    
    if (result && result.length > 0) {
      console.log('获取到提交记录数量:', result.length);
      console.log('提交记录详情:', JSON.stringify(result[0]));
      submissions.value = result;
    } else {
      console.log('该任务没有提交记录');
    }
  } catch (error) {
    console.error('获取任务提交记录失败:', error);
    ElMessage.error('获取任务提交记录失败');
  }
};

// 下载提交文件
const downloadSubmission = (submission: TaskSubmission) => {
  try {
    console.log('开始下载文件:', submission);
    
    // 获取授权token
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.error('授权已失效，请重新登录');
      return;
    }
    
    // 创建一个a标签来下载文件
    const link = document.createElement('a');
    link.href = `/api/supervisor/submissions/${submission.id}/download`;
    link.setAttribute('download', submission.originalFilename);
    
    // 为了确保请求中包含授权头，我们使用Fetch API
    fetch(link.href, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(response => {
      if (!response.ok) {
        throw new Error(`下载失败: ${response.status}`);
      }
      return response.blob();
    })
    .then(blob => {
      // 创建下载链接
      const url = window.URL.createObjectURL(blob);
      const downloadLink = document.createElement('a');
      downloadLink.href = url;
      downloadLink.download = submission.originalFilename;
      document.body.appendChild(downloadLink);
      downloadLink.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(downloadLink);
      
      ElMessage.success('文件下载成功');
    })
    .catch(error => {
      console.error('下载文件失败:', error);
      ElMessage.error(`下载文件失败: ${error.message}`);
    });
  } catch (error) {
    console.error('下载文件操作失败:', error);
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

// 获取任务评价
const loadTaskEvaluation = async (taskId: number) => {
  try {
    evaluationLoading.value = true;
    const evaluation = await getTaskEvaluation(taskId);
    currentEvaluation.value = evaluation;
  } catch (error: any) {
    // 忽略404或无数据错误，因为可能任务尚未评价
    if (error.response && error.response.status === 404) {
      console.log('任务尚未有评价，这是正常情况');
      currentEvaluation.value = null;
      
      // 重置评价表单为默认值
      evaluationForm.value.score = 5;
      evaluationForm.value.comment = '';
    } else if (!error.response || error.response.status !== 404) {
      console.error('获取任务评价失败:', error);
      // 不显示错误消息，而是静默处理
      currentEvaluation.value = null;
      
      // 重置评价表单为默认值
      evaluationForm.value.score = 5;
      evaluationForm.value.comment = '';
    }
  } finally {
    evaluationLoading.value = false;
  }
};

// 提交评价
const submitEvaluation = async () => {
  if (!currentTask.value) return;
  
  try {
    evaluationLoading.value = true;
    console.log('提交评价数据:', {
      taskId: currentTask.value.id,
      score: evaluationForm.value.score,
      comment: evaluationForm.value.comment
    });
    
    const response = await evaluateTask(
      currentTask.value.id,
      evaluationForm.value.score,
      evaluationForm.value.comment
    );
    currentEvaluation.value = response;
    ElMessage.success('评价提交成功');
  } catch (error: any) {
    console.error('评价提交失败:', error);
    ElMessage.error(`评价提交失败: ${error.message || '未知错误'}`);
  } finally {
    evaluationLoading.value = false;
  }
};

// 编辑评价
const editEvaluation = () => {
  if (currentEvaluation.value) {
    evaluationForm.value.score = currentEvaluation.value.score;
    evaluationForm.value.comment = currentEvaluation.value.comment || '';
    currentEvaluation.value = null;
  }
};

// 删除评价
const deleteEvaluation = async () => {
  if (!currentTask.value) return;
  
  try {
    await ElMessageBox.confirm('确定要删除此评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    evaluationLoading.value = true;
    await deleteTaskEvaluation(currentTask.value.id);
    currentEvaluation.value = null;
    
    // 重置评价表单为默认值
    evaluationForm.value.score = 5;
    evaluationForm.value.comment = '';
    
    ElMessage.success('评价已删除');
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error);
      ElMessage.error(`删除评价失败: ${error.message || '未知错误'}`);
    }
  } finally {
    evaluationLoading.value = false;
  }
};

// 加载子任务
const loadSubTasks = async (taskId: number) => {
  subTasksLoading.value = true;
  try {
    // 从所有任务中过滤出parentTaskId等于当前任务ID的任务
    const allTasks = await getSupervisorTasks(userStore.user.id);
    subTasks.value = allTasks.filter(t => t.parentTaskId === taskId);
  } catch (error) {
    console.error('获取子任务失败:', error);
    ElMessage.warning('获取子任务信息失败');
    subTasks.value = [];
  } finally {
    subTasksLoading.value = false;
  }
};
</script>

<style scoped>
.task-management {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.task-tabs {
  margin-bottom: 15px;
}

.task-create-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.empty-tasks-tip {
  text-align: center;
  padding: 10px;
  color: #909399;
}

.sub-tasks-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.sub-tasks-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.empty-subtasks {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.task-info,
.submission-list {
  margin-bottom: 20px;
}

h3 {
  margin-bottom: 15px;
  font-weight: 500;
  color: #303133;
}

.no-submissions {
  padding: 20px 0;
}

.task-evaluation-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.evaluation-details {
  margin-top: 15px;
}
</style> 