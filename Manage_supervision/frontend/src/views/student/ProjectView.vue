<template>
  <div class="project-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>我的课题</h3>
          <div class="actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索课题"
              prefix-icon="el-icon-search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
            <el-button @click="navigateToChat" type="success">
              <el-icon><ChatDotRound /></el-icon>
              <span>联系督导员</span>
            </el-button>
            <el-button type="primary" @click="openApplyProjectDialog">申请课题</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredProjects"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="课题标题" min-width="200" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewProjectDetails(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
        
        <template #empty>
          <div class="empty-data">
            <el-empty description="暂无课题分配给您" :image-size="120">
              <template #description>
                <p>暂时没有课题分配给您</p>
                <p class="sub-text">您可以点击"申请课题"按钮申请课题，或等待督导员分配</p>
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
          :total="totalProjects"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 课题详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="课题详情"
      width="70%"
      destroy-on-close
      @closed="handleProjectDialogClosed"
    >
      <el-descriptions
        v-if="currentProject"
        :column="2"
        border
      >
        <el-descriptions-item label="课题标题" :span="2">{{ currentProject.title }}</el-descriptions-item>
        <el-descriptions-item label="课题状态">
          <el-tag :type="getStatusType(currentProject.status)">
            {{ currentProject.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="类别">{{ currentProject.category || '未分类' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentProject.startTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentProject.endTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="课题描述" :span="2">
          <div class="project-description">{{ currentProject.description || '无描述' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="课题要求" :span="2">
          <div class="project-requirements">{{ currentProject.requirements || '无特殊要求' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="参考资源" :span="2">
          <div class="project-resources">{{ currentProject.resources || '无参考资源' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 课题评价部分 -->
      <div v-if="currentProject && currentProject.status === '已完成'" class="project-evaluation-section">
        <div class="section-header">
          <h3>督导评价</h3>
        </div>
        <div v-loading="evaluationLoading">
          <el-empty v-if="!currentEvaluation" description="暂无评价" :image-size="80">
            <template #description>
              <p>督导尚未对此课题进行评价</p>
            </template>
          </el-empty>
          
          <div v-else class="evaluation-container">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="score-container" :class="getScoreClass(currentEvaluation.score)">
                  <div class="score-value">{{ currentEvaluation.score }}</div>
                  <div class="score-label">评分</div>
                  <el-progress 
                    :percentage="currentEvaluation.score" 
                    :color="getScoreColor(currentEvaluation.score)"
                    :format="() => currentEvaluation.score + '分'"
                    class="score-progress"
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
                    {{ currentEvaluation.comment || '督导未留下评语' }}
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
      
      <!-- 关联任务列表 -->
      <div v-if="currentProject" class="related-tasks">
        <h3>关联任务
          <el-tooltip content="刷新任务列表" placement="top">
            <el-button type="primary" size="small" icon="Refresh" circle plain @click="refreshProjectTasks" :loading="tasksLoading"></el-button>
          </el-tooltip>
        </h3>
        
        <!-- 任务加载状态 -->
        <div v-if="tasksLoading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        
        <!-- 任务加载错误 -->
        <div v-else-if="tasksLoadError" class="error-container">
          <el-empty description="加载任务时出错">
            <template #description>
              <p>{{ tasksLoadError }}</p>
              <el-button type="primary" @click="refreshProjectTasks">重试</el-button>
            </template>
          </el-empty>
        </div>
        
        <!-- 任务为空 -->
        <div v-else-if="projectTasks.length === 0" class="empty-task">
          <el-empty description="暂无关联任务" />
        </div>
        
        <!-- 任务列表 -->
        <el-table v-else :data="projectTasks" border style="width: 100%">
          <el-table-column prop="title" label="任务标题" min-width="200" />
          <el-table-column prop="status" label="任务状态" width="120">
            <template #default="scope">
              <el-tag :type="getTaskProgressType(scope.row)">
                {{ getTaskProgressStatus(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="截止时间" width="160" />
          <el-table-column label="进度" width="160">
            <template #default="scope">
              <el-progress 
                :percentage="calculateTaskProgress(scope.row)" 
                :status="getTaskProgressStatus(scope.row) === '已完成' ? 'success' : ''"
              />
            </template>
          </el-table-column>
          <el-table-column label="分配状态" width="120">
            <template #default="scope">
              <el-tag v-if="isTaskAssignedToMe(scope.row)" type="success" size="small">
                分配给我
              </el-tag>
              <el-tag v-else type="info" size="small">
                其他学生
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewTaskDetails(scope.row)">
                详情
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="uploadTaskFile(scope.row)"
                :disabled="!isTaskAssignedToMe(scope.row)"
                :title="!isTaskAssignedToMe(scope.row) ? '只能上传分配给您的任务' : ''"
              >
                上传
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

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
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
        <el-descriptions-item label="进度状态">
          <el-tag :type="getTaskProgressType(currentTask)">
            {{ getTaskProgressStatus(currentTask) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="进度">
          <el-progress 
            :percentage="calculateTaskProgress(currentTask)" 
            :status="getTaskProgressStatus(currentTask) === '已完成' ? 'success' : ''"
          />
        </el-descriptions-item>
        <el-descriptions-item label="督导">{{ currentTask.supervisorName }}</el-descriptions-item>
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
      
      <!-- 文件上传区域 -->
      <div class="file-upload-section">
        <div class="section-header">
          <h3>提交文件</h3>
          <div v-if="isTaskAssignedToMe(currentTask)" class="task-completion-controls">
            <el-switch
              v-model="currentTask.completed"
              active-text="标记为已完成"
              inactive-text="进行中"
              @change="handleTaskCompletionChange"
              :disabled="getTaskProgressStatus(currentTask) === '未开始'"
            />
          </div>
        </div>
        <el-upload
          class="upload-demo"
          :action="`/api/user/tasks/${currentTask?.id}/submit`"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :disabled="!isTaskAssignedToMe(currentTask)"
        >
          <el-button type="primary" :disabled="!isTaskAssignedToMe(currentTask)">点击上传</el-button>
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
              <el-button type="danger" size="small" @click="handleDeleteSubmission(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="taskDialogVisible = false">关闭</el-button>
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
          <strong>进度:</strong> 
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
              <el-button type="danger" size="small" @click="handleDeleteSubmission(scope.row)">
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

    <!-- 申请课题对话框 -->
    <el-dialog
      v-model="applyProjectDialogVisible"
      title="申请课题"
      width="60%"
      destroy-on-close
    >
      <el-form
        ref="projectFormRef"
        :model="projectForm"
        :rules="projectRules"
        label-width="100px"
      >
        <el-form-item label="课题标题" prop="title">
          <el-input v-model="projectForm.title" placeholder="请输入课题标题" />
        </el-form-item>
        <el-form-item label="课题类别" prop="category">
          <el-input v-model="projectForm.category" placeholder="请输入课题类别" />
        </el-form-item>
        <el-form-item label="时间范围" prop="timeRange">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-date-picker
              v-model="projectForm.startTime"
              type="datetime"
              placeholder="开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 9, 0, 0)"
              style="width: 100%;"
            />
            <span>至</span>
            <el-date-picker
              v-model="projectForm.endTime"
              type="datetime"
              placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 18, 0, 0)"
              style="width: 100%;"
            />
          </div>
        </el-form-item>
        <el-form-item label="课题描述" prop="description">
          <el-input 
            v-model="projectForm.description" 
            placeholder="请输入课题描述"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="课题需求" prop="requirements">
          <el-input 
            v-model="projectForm.requirements" 
            placeholder="请输入课题需求"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="预期成果" prop="resources">
          <el-input 
            v-model="projectForm.resources" 
            placeholder="请输入预期成果"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="督导员" prop="supervisorId" required>
          <el-select v-model="projectForm.supervisorId" placeholder="请选择督导员" filterable>
            <el-option
              v-for="supervisor in supervisors"
              :key="supervisor.id"
              :label="supervisor.name || supervisor.username"
              :value="supervisor.id"
            />
          </el-select>
        </el-form-item>
        
        <!-- 修改关联任务部分，使其与督导员创建任务对话框风格一致 -->
        <div class="task-create-section">
          <div class="section-header">
            <h3>关联任务</h3>
            <div class="button-group">
              <el-button type="primary" size="small" @click="openNewTaskDialog">
                <el-icon><Plus /></el-icon>添加任务
              </el-button>
            </div>
          </div>
          
          <el-table
            :data="projectForm.tasks"
            border
            style="width: 100%; margin-top: 15px;"
          >
            <el-table-column prop="title" label="任务标题" min-width="150" />
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="scope">
                <el-tag :type="getPriorityType(scope.row.priority)">
                  {{ scope.row.priority || '中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="截止时间" width="160" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="warning" size="small" @click="editTask(scope.$index)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="danger" size="small" @click="removeTask(scope.$index)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="projectForm.tasks.length === 0" class="empty-tasks-tip">
            <el-empty description='暂无关联任务，请点击"添加任务"按钮创建' />
          </div>
        </div>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applyProjectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProjectApplication" :loading="submitting">提交申请</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加任务对话框 -->
    <el-dialog
      v-model="taskFormDialogVisible"
      :title="editingTaskIndex >= 0 ? '编辑任务' : '添加任务'"
      width="50%"
      destroy-on-close
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title" required>
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
              v-model="taskForm.startTime"
              type="datetime"
              placeholder="开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 9, 0, 0)"
              style="width: 100%;"
            />
            <span>至</span>
            <el-date-picker
              v-model="taskForm.endTime"
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
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="taskFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTaskForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { View, Edit, UploadFilled, Delete, Plus, ChatDotRound } from '@element-plus/icons-vue';
import { Project, getStudentProjects, applyForProject, getMyProjectEvaluation, ProjectEvaluation } from '../../api/project';
import { Task, getProjectTasks, getMyTaskDetail, updateTaskCompletionStatus } from '../../api/task';
import { TaskSubmission, getTaskSubmissions, deleteSubmission as apiDeleteSubmission, submitTaskFile } from '../../api/task';
import { getSupervisors } from '../../api/supervisor';
import { useUserStore } from '../../stores/user';
import type { FormInstance, FormRules } from 'element-plus';
import { useRouter } from 'vue-router';

// 添加用户store以获取当前用户ID
const userStore = useUserStore();

// 路由实例
const router = useRouter();

// 数据状态
const projects = ref<Project[]>([]);
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');

// 分页设置
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalProjects = ref<number>(0);

// 课题详情
const detailsDialogVisible = ref<boolean>(false);
const currentProject = ref<Project | null>(null);
const projectTasks = ref<Task[]>([]);
const tasksLoading = ref<boolean>(false);
const tasksLoadError = ref<string | null>(null);

// 任务详情
const taskDialogVisible = ref<boolean>(false);
const currentTask = ref<Task | null>(null);
const submissions = ref<TaskSubmission[]>([]);

// 上传文件对话框
const uploadDialogVisible = ref<boolean>(false);

// 上传文件相关
const uploadHeaders = reactive({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});

// 申请课题相关
const applyProjectDialogVisible = ref<boolean>(false);
const projectFormRef = ref<FormInstance>();
const submitting = ref<boolean>(false);
const supervisors = ref<any[]>([]);

// 新增：表单相关
const taskFormDialogVisible = ref<boolean>(false);
const taskFormRef = ref<FormInstance>();
const editingTaskIndex = ref<number>(-1);

// 任务表单
const taskForm = ref({
  title: '',
  description: '',
  priority: '中',
  status: '未开始',
  startTime: '',
  endTime: '',
});

// 任务表单验证规则
const taskRules = reactive<FormRules>({
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
});

// 申请课题表单
const projectForm = ref({
  title: '',
  description: '',
  category: '',
  requirements: '',
  resources: '',
  startTime: '',
  endTime: '',
  supervisorId: null as number | null,
  tasks: [] as any[]
});

// 申请课题表单验证规则
const projectRules = reactive<FormRules>({
  title: [{ required: true, message: '请输入课题标题', trigger: 'blur' }],
  supervisorId: [{ required: true, message: '请选择督导员', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
});

// 添加评价相关状态
const currentEvaluation = ref<ProjectEvaluation | null>(null);
const evaluationLoading = ref<boolean>(false);

// 筛选课题
const filteredProjects = computed(() => {
  let result = projects.value;
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(project => 
      project.title.toLowerCase().includes(query) || 
      (project.description && project.description.toLowerCase().includes(query))
    );
  }
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  
  totalProjects.value = result.length;
  return result.slice(start, end);
});

// 初始化数据
onMounted(() => {
  fetchProjects();
  fetchSupervisors();
});

// 获取课题
const fetchProjects = async () => {
  loading.value = true;
  try {
    const result = await getStudentProjects();
    projects.value = result;
    
    // 如果有项目，预加载第一个项目的任务数据
    if (result.length > 0) {
      preloadProjectTasks(result[0].id);
    }
  } catch (error) {
    console.error('获取课题失败:', error);
    ElMessage.error('获取课题失败');
  } finally {
    loading.value = false;
  }
};

// 预加载项目任务，提高用户体验
const preloadProjectTasks = async (projectId) => {
  if (!projectId) return;
  
  try {
    console.log(`预加载项目任务数据，项目ID: ${projectId}`);
    // 不显示加载状态，后台静默加载
    await getProjectTasks(projectId);
  } catch (error) {
    // 预加载失败不显示错误，只记录日志
    console.warn('预加载项目任务失败:', error);
  }
};

// 获取督导员列表
const fetchSupervisors = async () => {
  try {
    const result = await getSupervisors();
    supervisors.value = result;
  } catch (error) {
    console.error('获取督导员列表失败:', error);
    ElMessage.error('获取督导员列表失败');
  }
};

// 查看课题详情
const viewProjectDetails = async (project: Project) => {
  currentProject.value = project;
  detailsDialogVisible.value = true;
  
  // 清空之前的任务数据
  projectTasks.value = [];
  tasksLoadError.value = null;
  
  // 开始加载关联任务
  refreshProjectTasks();
  
  // 如果课题已完成，获取评价信息
  if (project.status === '已完成') {
    await loadProjectEvaluation(project.id);
  }
};

// 获取课题评价
const loadProjectEvaluation = async (projectId: number) => {
  try {
    evaluationLoading.value = true;
    const evaluation = await getMyProjectEvaluation(projectId);
    currentEvaluation.value = evaluation;
  } catch (error: any) {
    // 忽略404或无数据错误，因为可能课题尚未评价
    if (!error.response || error.response.status !== 404) {
      console.error('获取课题评价失败:', error);
    }
    currentEvaluation.value = null;
  } finally {
    evaluationLoading.value = false;
  }
};

// 获取分数对应的样式类
const getScoreClass = (score: number): string => {
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 70) return 'score-satisfactory';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
};

// 获取分数对应的颜色
const getScoreColor = (score: number): string => {
  if (score >= 90) return '#67C23A';  // 绿色
  if (score >= 80) return '#85ce61';  // 浅绿色
  if (score >= 70) return '#E6A23C';  // 黄色
  if (score >= 60) return '#F56C6C';  // 红色
  return '#909399';  // 灰色
};

// 查看任务详情
const viewTaskDetails = async (task: Task) => {
  // 首先判断是否分配给当前用户，如果不是则给予提示
  if (!isTaskAssignedToMe(task)) {
    ElMessage({
      message: '这个任务不是分配给您的，您可以查看基本信息，但无法提交文件',
      type: 'warning',
      duration: 3000,
      showClose: true
    });
  }

  // 重置文件列表，避免显示上一个任务的文件
  submissions.value = [];

  try {
    // 获取完整任务详情
    const taskDetail = await getMyTaskDetail(task.id);
    currentTask.value = taskDetail;
    taskDialogVisible.value = true;
    
    // 获取任务提交记录
    if (isTaskAssignedToMe(task)) {
      try {
        const taskSubmissions = await getTaskSubmissions(task.id);
        submissions.value = taskSubmissions;
      } catch (error) {
        console.error('获取任务提交记录失败:', error);
        submissions.value = [];
      }
    } else {
      submissions.value = []; // 不是分配给当前用户的任务不显示提交记录
    }
  } catch (error: any) {
    console.error('获取任务详情失败:', error);
    taskDialogVisible.value = false; // 确保对话框关闭
    currentTask.value = null; // 清理状态
    
    // 优化错误提示，针对无权查看的情况提供友好提示
    if (error.data && error.data.message === '无权查看该任务') {
      ElMessage({
        message: '您没有权限查看此任务的详细信息，这个任务可能不是分配给您的',
        type: 'warning',
        duration: 3000,
        showClose: true
      });
    } else {
      ElMessage({
        message: '获取任务详情失败，请稍后重试',
        type: 'error',
        duration: 3000
      });
    }
  }
};

// 上传任务文件
const uploadTaskFile = async (task: Task) => {
  // 首先判断是否分配给当前用户，如果不是则给予提示并中断
  if (!isTaskAssignedToMe(task)) {
    ElMessage({
      message: '您只能为分配给自己的任务上传文件',
      type: 'warning',
      duration: 3000,
      showClose: true
    });
    return;
  }

  // 重置文件列表，避免显示上一个任务的文件
  submissions.value = [];

  try {
    // 获取完整任务详情
    const taskDetail = await getMyTaskDetail(task.id);
    currentTask.value = taskDetail;
    uploadDialogVisible.value = true;
    
    // 获取任务提交记录
    const submissionResult = await getTaskSubmissions(task.id);
    submissions.value = submissionResult;
  } catch (error: any) {
    console.error('获取任务详情失败:', error);
    uploadDialogVisible.value = false; // 确保对话框关闭
    currentTask.value = null; // 清理状态
    
    if (error.data && error.data.message === '无权查看该任务') {
      ElMessage({
        message: '您没有权限管理此任务，可能该任务不是分配给您的',
        type: 'warning',
        duration: 3000,
        showClose: true
      });
    } else {
      ElMessage({
        message: '获取任务详情失败，请稍后重试',
        type: 'error',
        duration: 3000
      });
    }
  }
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

// 辅助函数
const getStatusType = (status: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case '未开始': return 'primary';
    case '进行中': return 'warning';
    case '已完成': return 'success';
    case '已结项': return 'info';
    case '未审核': return 'warning';
    case '审核未通过': return 'danger';
    default: return 'primary';
  }
};

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

// 判断任务是否分配给当前用户
const isTaskAssignedToMe = (task) => {
  return task.assigneeId && task.assigneeId === userStore.user.id;
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

// 打开申请课题对话框
const openApplyProjectDialog = () => {
  // 重置表单
  projectForm.value = {
    title: '',
    description: '',
    category: '',
    requirements: '',
    resources: '',
    startTime: '',
    endTime: '',
    supervisorId: null,
    tasks: []
  };
  
  // 加载督导员列表
  fetchSupervisors();
  
  // 显示对话框
  applyProjectDialogVisible.value = true;
};

// 打开添加任务对话框
const openNewTaskDialog = () => {
  // 重置任务表单
  taskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始',
    startTime: '',
    endTime: '',
  };
  editingTaskIndex.value = -1;
  taskFormDialogVisible.value = true;
};

// 编辑任务
const editTask = (index: number) => {
  const task = projectForm.value.tasks[index];
  taskForm.value = { ...task };
  editingTaskIndex.value = index;
  taskFormDialogVisible.value = true;
};

// 提交任务表单
const submitTaskForm = () => {
  taskFormRef.value?.validate((valid) => {
    if (valid) {
      if (editingTaskIndex.value >= 0) {
        // 更新现有任务
        projectForm.value.tasks[editingTaskIndex.value] = { ...taskForm.value };
      } else {
        // 添加新任务
        projectForm.value.tasks.push({ ...taskForm.value });
      }
      taskFormDialogVisible.value = false;
    }
  });
};

// 删除任务
const removeTask = (index: number) => {
  projectForm.value.tasks.splice(index, 1);
};

// 提交课题申请
const submitProjectApplication = async () => {
  projectFormRef.value?.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 确保任务关联到督导员
        const projectData = { ...projectForm.value };
        
        // 确保任务有必要的关联信息
        if (projectData.tasks) {
          projectData.tasks = projectData.tasks.map(task => ({
            ...task,
            supervisorId: projectData.supervisorId,
            status: task.status || '未开始',
            // 默认开始时间如果未设置
            startTime: task.startTime || new Date().toISOString()
          }));
        }
        
        const result = await applyForProject(projectData);
        ElMessage.success('课题申请已提交，请等待督导员审核');
        applyProjectDialogVisible.value = false;
        
        // 重置表单
        projectForm.value = {
          title: '',
          description: '',
          category: '',
          requirements: '',
          resources: '',
          supervisorId: null,
          tasks: []
        };
        
        // 刷新项目列表
        fetchProjects();
      } catch (error: any) {
        console.error('申请课题失败:', error);
        ElMessage.error(error.data?.message || '申请课题失败，请重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 辅助函数：计算任务进度状态
const getTaskProgressStatus = (task: Task) => {
  const now = new Date();
  const endTime = task.endTime ? new Date(task.endTime) : null;
  const hasSubmissions = submissions.value && submissions.value.length > 0;
  
  // 检查任务是否已经被标记为完成
  if (task.completed) {
    return '已完成';
  }
  
  // 根据时间和提交情况判断状态
  if (!hasSubmissions) {
    return '未开始';
  } else if (endTime && now > endTime && !task.completed) {
    return '待完成';
  } else {
    return '进行中';
  }
};

// 辅助函数：获取任务进度状态对应的样式类型
const getTaskProgressType = (task: Task): 'success' | 'warning' | 'info' | 'danger' => {
  const status = getTaskProgressStatus(task);
  switch (status) {
    case '已完成': return 'success';
    case '进行中': return 'primary';
    case '待完成': return 'warning';
    case '未开始': return 'info';
    default: return 'info';
  }
};

// 辅助函数：计算任务完成百分比
const calculateTaskProgress = (task: Task) => {
  // 如果任务已完成，返回100%
  if (task.completed) {
    return 100;
  }
  
  // 如果任务未完成且当前正在查看任务详情，根据提交记录判断进度
  if (submissions.value && submissions.value.length > 0 && 
      currentTask.value && currentTask.value.id === task.id) {
    return 100;  // 有提交则显示100%
  }
  
  // 检查任务是否有submissionCount属性（从API获取的任务可能包含此属性）
  if (task.submissionCount && task.submissionCount > 0) {
    return 100;  // 有提交则显示100%
  }
  
  // 如果没有上传文件，则返回0%
  return 0;
};

// 处理任务完成状态变更
const handleTaskCompletionChange = async () => {
  if (!currentTask.value) return;
  
  try {
    // 调用API更新任务完成状态
    const updatedTask = await updateTaskCompletionStatus(
      currentTask.value.id, 
      !!currentTask.value.completed
    );
    
    // 更新本地数据
    if (currentTask.value) {
      currentTask.value.completed = updatedTask.completed;
    }
    
    // 更新任务列表
    const index = projectTasks.value.findIndex(t => t.id === updatedTask.id);
    if (index !== -1) {
      projectTasks.value[index] = {
        ...projectTasks.value[index],
        completed: updatedTask.completed
      };
    }
    
    ElMessage.success(
      updatedTask.completed 
        ? '任务已标记为完成' 
        : '任务已恢复为进行中状态'
    );
  } catch (error) {
    console.error('更新任务完成状态失败:', error);
    
    // 恢复UI状态
    if (currentTask.value) {
      currentTask.value.completed = !currentTask.value.completed;
    }
    
    ElMessage.error('更新任务状态失败');
  }
};

// 刷新课题任务
const refreshProjectTasks = async () => {
  if (!currentProject.value) return;
  
  tasksLoading.value = true;
  tasksLoadError.value = null;
  
  try {
    const tasks = await getProjectTasks(currentProject.value.id);
    projectTasks.value = tasks;
    
    if (tasks.length === 0) {
      console.log(`未找到课题ID=${currentProject.value.id}的关联任务`);
    } else {
      console.log(`找到课题ID=${currentProject.value.id}的关联任务${tasks.length}个`);
    }
  } catch (error: any) {
    console.error('刷新课题任务失败:', error);
    
    // 优化错误信息展示
    if (error instanceof Error && error.message) {
      tasksLoadError.value = error.message;
    } else if (error.response?.data?.message) {
      tasksLoadError.value = error.response.data.message;
    } else {
      tasksLoadError.value = '获取任务失败，请稍后重试';
    }
    
    // 对403错误特殊处理
    if (error.response?.status === 403) {
      tasksLoadError.value = '无权查看该课题的任务，该课题可能未分配给您';
      ElMessage({
        message: tasksLoadError.value,
        type: 'warning',
        duration: 5000,
        showClose: true
      });
    }
  } finally {
    tasksLoading.value = false;
  }
};

// 处理课题详情对话框关闭前的数据清理和加载状态重置
const handleProjectDialogClosed = () => {
  // 清空之前的任务数据
  projectTasks.value = [];
  tasksLoadError.value = null;
};

// 删除提交记录
const handleDeleteSubmission = async (submission: TaskSubmission) => {
  // 添加参数检查
  if (!submission || !submission.id) {
    console.error('删除提交记录失败: 无效的提交记录');
    ElMessage({
      message: '删除提交记录失败: 无效的提交记录',
      type: 'error',
      duration: 3000
    });
    return;
  }

  try {
    // 明确使用从API导入的deleteSubmission函数
    await apiDeleteSubmission(submission.id);
    
    // 更新提交列表
    submissions.value = submissions.value.filter(s => s.id !== submission.id);
    
    ElMessage({
      message: '提交记录已删除',
      type: 'success',
      duration: 3000
    });
  } catch (error: any) {
    console.error('删除提交记录失败:', error);
    
    if (error.data && error.data.message) {
      ElMessage({
        message: `删除提交记录失败: ${error.data.message}`,
        type: 'error',
        duration: 3000,
        showClose: true
      });
    } else {
      ElMessage({
        message: '删除提交记录失败，请稍后重试',
        type: 'error',
        duration: 3000
      });
    }
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
const handleUploadSuccess = (response: any, file: any) => {
  ElMessage.success('文件上传成功，任务将自动标记为已完成');
  
  // 添加到提交列表
  submissions.value.push(response);
  
  // 检查是否需要更新任务状态
  if (currentTask.value) {
    updateTaskProgressAfterSubmission(currentTask.value);
  }
};

// 上传后更新任务进度
const updateTaskProgressAfterSubmission = async (task: Task) => {
  try {
    // 获取最新的任务提交记录
    const taskSubmissions = await getTaskSubmissions(task.id);
    submissions.value = taskSubmissions;
    
    // 任务有新提交，自动将任务标记为"已完成"
    if (taskSubmissions.length > 0 && !task.completed) {
      // 更新任务状态为已完成
      try {
        // 调用完成任务的API
        const updatedTask = await updateTaskCompletionStatus(task.id, true);
        
        // 更新本地数据
        if (currentTask.value && currentTask.value.id === updatedTask.id) {
          currentTask.value = updatedTask;
        }
        
        // 更新任务列表中的数据
        const index = projectTasks.value.findIndex(t => t.id === updatedTask.id);
        if (index !== -1) {
          projectTasks.value[index] = updatedTask;
        }
        
        ElMessage.success('任务已自动标记为已完成');
      } catch (error) {
        console.error('自动更新任务状态失败:', error);
        // 不显示错误提示，避免干扰用户
      }
    }
  } catch (error) {
    console.error('更新任务进度失败:', error);
  }
};

// 处理上传失败
const handleUploadError = (error: any) => {
  console.error('上传失败:', error);
  ElMessage.error('文件上传失败');
};

// 导航到聊天功能
const navigateToChat = () => {
  // 实现导航到聊天功能的逻辑
  router.push('/chat');
};
</script>

<style scoped>
.project-view {
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

.project-description,
.project-requirements,
.project-resources,
.task-description {
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}

.related-tasks,
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
  font-size: 0.9em;
  color: #909399;
  margin-top: 5px;
}

.empty-data {
  padding: 30px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.task-item {
  margin-bottom: 15px;
  padding: 15px;
  border: 1px dashed #e0e0e0;
  border-radius: 4px;
  background-color: rgba(64, 158, 255, 0.05);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.task-header h4 {
  margin: 0;
  color: #409EFF;
}

.add-task-button {
  margin-top: 20px;
  text-align: center;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.task-completion-controls {
  margin-right: 15px;
}

.loading-container {
  padding: 20px;
  text-align: center;
}

.error-container {
  padding: 20px;
  text-align: center;
}

.empty-task {
  padding: 20px;
  text-align: center;
}

/* 任务部分样式 */
.task-create-section {
  margin-top: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.button-group {
  display: flex;
  gap: 10px;
}

.empty-tasks-tip {
  margin: 20px 0;
  text-align: center;
}

/* 评价相关样式 */
.project-evaluation-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.score-container {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 5px;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.score-progress {
  margin-top: 10px;
}

.score-excellent {
  background-color: #f0f9eb;
  color: #67C23A;
}

.score-good {
  background-color: #f0f9eb;
  color: #85ce61;
}

.score-satisfactory {
  background-color: #fdf6ec;
  color: #E6A23C;
}

.score-pass {
  background-color: #fef0f0;
  color: #F56C6C;
}

.score-fail {
  background-color: #f4f4f5;
  color: #909399;
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
  white-space: pre-line;
}

.evaluator-info {
  margin-top: 15px;
  text-align: right;
}

.evaluation-container {
  margin-top: 15px;
}
</style> 