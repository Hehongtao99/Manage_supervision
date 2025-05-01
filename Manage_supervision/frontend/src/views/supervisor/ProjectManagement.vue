<template>
  <div class="project-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课题管理</span>
          <div class="actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索课题..."
              class="search-input"
              clearable
              @input="handleSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="openCreateProjectDialog">创建课题</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="project-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="全部课题" name="all"></el-tab-pane>
        <el-tab-pane label="未分配" name="unassigned"></el-tab-pane>
        <el-tab-pane label="进行中" name="in_progress"></el-tab-pane>
        <el-tab-pane label="已完成" name="completed"></el-tab-pane>
        <el-tab-pane label="已结项" name="completed"></el-tab-pane>
        <el-tab-pane label="待审核" name="pending_review">
          <template #label>
            <span>待审核</span>
            <el-badge v-if="pendingReviewCount > 0" :value="pendingReviewCount" class="review-badge" />
          </template>
        </el-tab-pane>
      </el-tabs>
      
      <el-table
        :data="filteredProjects"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="课题标题" min-width="150" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="assigneeName" label="负责学生" width="120">
          <template #default="scope">
            <span v-if="scope.row.assigneeName">{{ scope.row.assigneeName }}</span>
            <el-tag v-else type="info">未分配</el-tag>
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewProjectDetails(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button 
              v-if="scope.row.status !== '未审核'"
              type="success" 
              size="small" 
              @click="openAssignDialog(scope.row)" 
              :disabled="!!scope.row.assigneeId"
            >
              <el-icon><CirclePlus /></el-icon>分配
            </el-button>
            <el-button 
              v-if="scope.row.status === '未审核'"
              type="success" 
              size="small" 
              @click="openReviewDialog(scope.row)"
            >
              <el-icon><Check /></el-icon>审核
            </el-button>
            <el-button type="warning" size="small" @click="editProject(scope.row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm
              title="确定要删除此课题吗？"
              @confirm="deleteProjectItem(scope.row.id)"
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
      width="60%"
      destroy-on-close
    >
      <el-descriptions
        v-if="currentProject"
        :column="2"
        border
      >
        <el-descriptions-item label="课题标题">{{ currentProject.title }}</el-descriptions-item>
        <el-descriptions-item label="课题状态">
          <el-tag :type="getStatusType(currentProject.status)">
            {{ currentProject.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="课题类别">{{ currentProject.category || '未分类' }}</el-descriptions-item>
        <el-descriptions-item label="负责学生">
          <span v-if="currentProject.assigneeName">{{ currentProject.assigneeName }}</span>
          <el-tag v-else type="info">未分配</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentProject.startTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentProject.endTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentProject.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentProject.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="课题描述" :span="2">
          <div class="project-description">{{ currentProject.description || '无描述' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="课题需求" :span="2">
          <div class="project-requirements">{{ currentProject.requirements || '无需求' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="相关资源" :span="2">
          <div class="project-resources">{{ currentProject.resources || '无资源' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 课题评价部分 -->
      <div v-if="currentProject && currentProject.status === '已完成'" class="project-evaluation-section">
        <div class="section-header">
          <h3>课题评价</h3>
        </div>
        <div v-loading="evaluationLoading">
          <el-form v-if="!currentEvaluation" :model="evaluationForm" label-width="80px">
            <el-form-item label="评分">
              <el-input-number
                v-model="evaluationForm.score"
                :min="0"
                :max="100"
                :step="1"
                controls-position="right"
              />
              <span style="margin-left: 10px;">分</span>
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
      
      <div class="task-section" v-if="currentProject && currentProject.id">
        <div class="task-header">
          <h3>关联任务列表</h3>
        </div>
        
        <el-table 
          :data="projectTasks" 
          border 
          style="width: 100%" 
          v-loading="tasksLoading"
        >
          <el-table-column prop="title" label="任务标题" min-width="120" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="scope">
              <el-tag :type="getTaskPriorityType(scope.row.priority)">
                {{ scope.row.priority || '中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="assigneeName" label="负责学生" width="120">
            <template #default="scope">
              <span v-if="scope.row.assigneeName">{{ scope.row.assigneeName }}</span>
              <el-tag v-else type="info">未分配</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="completed" label="文件提交" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.completed ? 'success' : 'info'">
                {{ scope.row.completed ? '已提交' : '未提交' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="150">
            <template #default="scope">
              <span>{{ scope.row.startTime || '未设置' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="截止时间" width="150">
            <template #default="scope">
              <span>{{ scope.row.endTime || '未设置' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="danger" size="small" @click="handleDeleteTask(scope.row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
              <el-button type="primary" size="small" @click="viewTaskSubmissions(scope.row)">
                <el-icon><Document /></el-icon>文件
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="empty-task" v-if="projectTasks.length === 0 && !tasksLoading">
          <el-empty description="暂无关联任务" />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 创建/编辑课题对话框 -->
    <el-dialog
      v-model="projectFormDialogVisible"
      :title="isEditing ? '编辑课题' : '创建课题'"
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
        <el-form-item label="相关资源" prop="resources">
          <el-input 
            v-model="projectForm.resources" 
            placeholder="请输入相关资源"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="projectForm.status" placeholder="请选择状态">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已结项" value="已结项" />
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
              placeholder="截止时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 18, 0, 0)"
              style="width: 100%;"
            />
          </div>
          <div style="margin-top: 8px;">
            <el-button 
              type="text" 
              size="small" 
              @click="useProjectTime"
              :disabled="!startTime || !endTime"
            >
              使用课题时间
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <!-- 创建课题时添加任务区域 -->
      <div class="task-create-section">
        <div class="section-header">
          <h3>课题任务</h3>
          <div class="button-group">
            <el-button type="primary" size="small" @click="openNewTaskDialog">
              <el-icon><Plus /></el-icon>添加任务
            </el-button>
            <el-button type="success" size="small" @click="openBatchSelectTasksDialog">
              <el-icon><Plus /></el-icon>批量选择任务
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="newProjectTasks"
          border
          style="width: 100%; margin-top: 15px;"
        >
          <el-table-column prop="title" label="任务标题" min-width="120" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="scope">
              <el-tag :type="getTaskPriorityType(scope.row.priority)">
                {{ scope.row.priority || '中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="150">
            <template #default="scope">
              <span>{{ scope.row.startTime || '未设置' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="截止时间" width="150">
            <template #default="scope">
              <span>{{ scope.row.endTime || '未设置' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="warning" size="small" @click="editNewTask(scope.$index)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="danger" size="small" @click="removeNewTask(scope.$index)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="newProjectTasks.length === 0" class="empty-tasks-tip">
          <el-empty description='暂无任务，请点击"添加任务"按钮创建' />
        </div>
      </div>
      
      <!-- 添加编辑课题时的任务加载提示 -->
      <div v-if="isEditing" class="task-loading-tip">
        <el-alert
          title="已自动加载与课题关联的任务"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>在这里可以管理课题的任务，包括添加新任务或编辑现有任务。</p>
            <p>所有更改将在点击确定后保存。</p>
            <p v-if="projectForm.assigneeId" class="auto-assign-tip">
              <strong>注意：</strong> 该课题已分配给学生，新增任务将自动分配给该学生。
            </p>
          </template>
        </el-alert>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="projectFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProjectForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 分配课题给学生对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配课题给学生"
      width="40%"
      destroy-on-close
    >
      <div v-loading="studentsLoading">
        <el-alert
          v-if="students.length === 0 && !studentsLoading"
          title="暂无可分配的学生"
          type="warning"
          :closable="false"
          show-icon
        />
        <div v-if="students.length === 0 && !studentsLoading" style="text-align: center; margin-top: 10px;">
          <el-button type="primary" size="small" @click="fetchStudents">
            <el-icon><Refresh /></el-icon>刷新学生列表
          </el-button>
        </div>
        <el-form v-else label-width="80px">
          <el-form-item label="选择学生">
            <div style="display: flex; gap: 10px;">
              <el-select
                v-model="selectedStudentId"
                placeholder="请选择学生"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="student in students"
                  :key="student.id"
                  :label="student.name ? `${student.name}${student.studentId ? ` (${student.studentId})` : ''}` : student.username"
                  :value="student.id"
                />
              </el-select>
              <el-button type="primary" @click="fetchStudents" :loading="studentsLoading">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssignProject" :disabled="!selectedStudentId">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加任务对话框 -->
    <el-dialog
      v-model="taskFormVisible"
      title="添加任务"
      width="50%"
      destroy-on-close
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="taskFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTaskForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 创建课题时添加任务对话框 -->
    <el-dialog
      v-model="newTaskFormVisible"
      :title="editingTaskIndex >= 0 ? '编辑任务' : '添加任务'"
      width="50%"
      destroy-on-close
    >
      <el-form
        ref="newTaskFormRef"
        :model="newTaskForm"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title" required>
          <el-input v-model="newTaskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="newTaskForm.description" 
            placeholder="请输入任务描述"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="newTaskForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="newTaskForm.status" placeholder="请选择状态">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" prop="timeRange">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-date-picker
              v-model="newTaskForm.startTime"
              type="datetime"
              placeholder="开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="new Date(2000, 0, 1, 9, 0, 0)"
              style="width: 100%;"
            />
            <span>至</span>
            <el-date-picker
              v-model="newTaskForm.endTime"
              type="datetime"
              placeholder="截止时间"
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
          <el-button @click="newTaskFormVisible = false">取消</el-button>
          <el-button type="primary" @click="addNewTask">确定</el-button>
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
          <el-table-column prop="title" label="任务标题" min-width="120" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="scope">
              <el-tag :type="getTaskPriorityType(scope.row.priority)">
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
          <el-button type="primary" @click="addSelectedTasks">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加任务提交记录对话框 -->
    <el-dialog
      v-model="submissionsDialogVisible"
      title="任务提交记录"
      width="70%"
      destroy-on-close
    >
      <div v-if="currentSubmissionTask" class="task-info">
        <h3>任务信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务标题">{{ currentSubmissionTask.title }}</el-descriptions-item>
          <el-descriptions-item label="任务进度">
            <el-tag :type="getTaskStatusType(currentSubmissionTask.status)">
              {{ currentSubmissionTask.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责学生">
            <span v-if="currentSubmissionTask.assigneeName">{{ currentSubmissionTask.assigneeName }}</span>
            <el-tag v-else type="info">未分配</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentSubmissionTask.startTime || '未设置' }}</el-descriptions-item>
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
    
    <!-- 审核课题申请对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核课题申请"
      width="50%"
      destroy-on-close
    >
      <div v-if="projectToReview" class="review-content">
        <el-descriptions
          title="课题申请信息"
          :column="1"
          border
        >
          <el-descriptions-item label="课题标题">{{ projectToReview.title }}</el-descriptions-item>
          <el-descriptions-item label="申请学生">{{ projectToReview.assigneeName }}</el-descriptions-item>
          <el-descriptions-item label="课题类别">{{ projectToReview.category || '未分类' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ projectToReview.createTime }}</el-descriptions-item>
          <el-descriptions-item label="课题描述">
            <div class="project-description">{{ projectToReview.description || '无描述' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="课题需求">
            <div class="project-requirements">{{ projectToReview.requirements || '无需求' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="rejectProjectApplication" :loading="reviewing">拒绝申请</el-button>
          <el-button type="success" @click="approveProjectApplication" :loading="reviewing">通过申请</el-button>
        </div>
      </template>
    </el-dialog>
    
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { useUserStore } from '../../stores/user';
import {
  Search,
  View,
  Edit,
  Delete,
  CirclePlus,
  Plus,
  Refresh,
  Document,
  Check
} from '@element-plus/icons-vue';
import {
  getSupervisorProjects,
  getSupervisorProjectsByStatus,
  createProject,
  updateProject,
  deleteProject,
  getProjectDetail,
  assignProjectToStudent,
  getPendingReviewProjects,
  reviewProjectApplication,
  getProjectEvaluation,
  evaluateProject,
  deleteProjectEvaluation,
  type Project,
  type ProjectEvaluation
} from '../../api/project';
import { 
  getProjectTasks, 
  createTask, 
  deleteTask,
  updateTask,
  getAvailableTasks,
  assignTaskToStudent,
  type Task,
  getTaskSubmissions,
  type TaskSubmission
} from '../../api/task';
import { getStudents } from '../../api/user';
import type { FormInstance, FormRules } from 'element-plus';
import type { Student } from '../../api/user';

const userStore = useUserStore();

// 状态变量
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalProjects = ref<number>(0);
const projects = ref<Project[]>([]);
const activeTab = ref<string>('all');
const detailsDialogVisible = ref<boolean>(false);
const projectFormDialogVisible = ref<boolean>(false);
const isEditing = ref<boolean>(false);
const currentProject = ref<Project | null>(null);
const projectForm = ref<Partial<Project>>({});
const startTime = ref<string | null>(null);
const endTime = ref<string | null>(null);
const assignDialogVisible = ref<boolean>(false);
const selectedStudentId = ref<number | null>(null);
const students = ref<Student[]>([]);
const projectToAssign = ref<Project | null>(null);
const projectFormRef = ref<FormInstance>();
const studentsLoading = ref(false);
const pendingReviewCount = ref<number>(0);

// 审核相关变量
const reviewDialogVisible = ref<boolean>(false);
const projectToReview = ref<Project | null>(null);
const reviewing = ref<boolean>(false);

// 任务相关变量
const projectTasks = ref<Task[]>([]);
const tasksLoading = ref<boolean>(false);
const taskFormVisible = ref<boolean>(false);
const taskForm = ref({
  title: '',
  description: '',
  priority: '中',
  status: '未开始',
  supervisorId: userStore.user.id,
  projectId: null as number | null,
  assigneeId: null,
  completed: false
});
const taskFormRef = ref<FormInstance>();

// 创建课题时的任务变量
const newProjectTasks = ref<any[]>([]);
const newTaskFormVisible = ref<boolean>(false);
const newTaskForm = ref({
  title: '',
  description: '',
  priority: '中',
  status: '未开始',
  startTime: null as string | null,
  endTime: null as string | null
});
const editingTaskIndex = ref<number>(-1);
const newTaskFormRef = ref<FormInstance>();

// 表单验证规则
const projectRules = ref<FormRules>({
  title: [
    { required: true, message: '请输入课题标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择课题状态', trigger: 'change' }
  ]
});

// 批量选择任务对话框
const batchSelectTasksVisible = ref<boolean>(false);
const availableTasksLoading = ref<boolean>(false);
const taskSearchQuery = ref<string>('');
const availableTasks = ref<Task[]>([]);
const selectedTasks = ref<Task[]>([]);

// 任务提交记录相关
const submissionsDialogVisible = ref<boolean>(false);
const submissions = ref<TaskSubmission[]>([]);
const currentSubmissionTask = ref<Task | null>(null);

// 添加评价相关变量
const evaluationLoading = ref(false);
const currentEvaluation = ref<ProjectEvaluation | null>(null);
const evaluationForm = ref({
  score: 80,
  comment: ''
});

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
  fetchProjects();
  fetchStudents();
  checkPendingReviews();
});

// 获取课题数据
const fetchProjects = async () => {
  loading.value = true;
  try {
    let data;
    if (activeTab.value === 'all') {
      data = await getSupervisorProjects(userStore.user.id);
    } else if (activeTab.value === 'pending_review') {
      data = await getPendingReviewProjects(userStore.user.id);
      pendingReviewCount.value = data?.length || 0;
    } else {
      data = await getSupervisorProjectsByStatus(userStore.user.id, activeTab.value);
    }
    projects.value = data || [];
    totalProjects.value = data?.length || 0;
    ElMessage.success('成功获取课题数据');
  } catch (error) {
    console.error('获取课题数据失败:', error);
    ElMessage.error('获取课题数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 根据搜索过滤课题
const filteredProjects = computed(() => {
  if (!searchQuery.value) return projects.value;
  
  const query = searchQuery.value.toLowerCase();
  return projects.value.filter(project => 
    project.title?.toLowerCase().includes(query) || 
    project.description?.toLowerCase().includes(query) || 
    project.category?.toLowerCase().includes(query) ||
    (project.assigneeName && project.assigneeName.toLowerCase().includes(query))
  );
});

// 处理函数 - 这些函数将在后续实现
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
  fetchProjects();
};

const openCreateProjectDialog = () => {
  isEditing.value = false;
  projectForm.value = {
    title: '',
    description: '',
    category: '',
    requirements: '',
    resources: '',
    status: '未开始',
    supervisorId: userStore.user.id
  };
  startTime.value = null;
  endTime.value = null;
  // 清空新任务列表
  newProjectTasks.value = [];
  projectFormDialogVisible.value = true;
};

const viewProjectDetails = async (project: Project) => {
  try {
    loading.value = true;
    currentProject.value = await getProjectDetail(project.id);
    detailsDialogVisible.value = true;
    
    // 加载课题关联的任务
    fetchProjectTasks(project.id);
    
    // 加载课题评价（如果课题状态为已完成）
    if (currentProject.value.status === '已完成') {
      loadProjectEvaluation(project.id);
    }
  } catch (error) {
    console.error('获取课题详情失败:', error);
    ElMessage.error('获取课题详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取课题关联的任务列表
const fetchProjectTasks = async (projectId: number) => {
  tasksLoading.value = true;
  try {
    const tasks = await getProjectTasks(projectId);
    projectTasks.value = tasks || [];
  } catch (error: any) {
    console.error('获取课题关联任务失败:', error);
    // 显示具体的错误信息，而不是通用消息
    let errorMessage = '获取课题关联任务失败';
    
    // 如果是我们处理过的错误，显示具体错误信息
    if (error instanceof Error && error.message) {
      errorMessage = error.message;
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 5000,
      showClose: true
    });
  } finally {
    tasksLoading.value = false;
  }
};

const editProject = async (project: Project) => {
  // 设置加载状态
  loading.value = true;
  isEditing.value = true;
  
  try {
    // 设置基本课题信息
    projectForm.value = {
      id: project.id,
      title: project.title,
      description: project.description || '',
      category: project.category || '',
      requirements: project.requirements || '',
      resources: project.resources || '',
      status: project.status,
      supervisorId: project.supervisorId,
      assigneeId: project.assigneeId // 确保加载assigneeId字段
    };
    
    // 记录课题分配状态
    if (project.assigneeId) {
      console.log(`课题已分配给学生(ID:${project.assigneeId}, 姓名:${project.assigneeName || '未知'})`);
    }
    
    if (project.startTime && project.endTime) {
      startTime.value = project.startTime;
      endTime.value = project.endTime;
    } else {
      startTime.value = null;
      endTime.value = null;
    }
    
    // 清空现有任务列表
    newProjectTasks.value = [];
    
    // 加载课题关联的任务
    const tasks = await getProjectTasks(project.id);
    console.log('获取到课题关联任务:', tasks);
    
    // 将任务添加到编辑表单中
    if (tasks && tasks.length > 0) {
      // 将任务转换为编辑所需格式
      newProjectTasks.value = tasks.map(task => ({
        id: task.id,
        title: task.title,
        description: task.description || '',
        priority: task.priority || '中',
        status: task.status || '未开始',
        startTime: task.startTime || null,
        endTime: task.endTime || null
      }));
    }
    
    // 显示编辑对话框
    projectFormDialogVisible.value = true;
  } catch (error) {
    console.error('加载课题任务失败:', error);
    ElMessage.error('加载课题任务失败，请重试');
  } finally {
    loading.value = false;
  }
};

const deleteProjectItem = async (id: number) => {
  loading.value = true;
  try {
    const success = await deleteProject(id);
    if (success) {
      ElMessage.success('课题删除成功');
      projects.value = projects.value.filter(project => project.id !== id);
      totalProjects.value = projects.value.length;
    } else {
      ElMessage.error('删除课题失败');
    }
  } catch (error) {
    console.error('删除课题失败:', error);
    ElMessage.error('删除课题失败');
  } finally {
    loading.value = false;
  }
};

const openAssignDialog = (project: Project) => {
  if (project.assigneeId) {
    ElMessage.warning('该课题已分配给学生');
    return;
  }
  
  projectToAssign.value = project;
  selectedStudentId.value = null;
  fetchStudents();
  assignDialogVisible.value = true;
};

const fetchStudents = async () => {
  studentsLoading.value = true;
  try {
    students.value = await getStudents();
    console.log('获取到的学生数据:', students.value);
    if (students.value.length === 0) {
      ElMessage.warning('没有找到可分配的学生');
    }
  } catch (error) {
    console.error('获取学生数据失败:', error);
    ElMessage.error('获取学生数据失败，请稍后重试');
    students.value = [];
  } finally {
    studentsLoading.value = false;
  }
};

// 辅助函数：将课题下的所有任务分配给指定学生
const assignProjectTasksToStudent = async (projectId: number, studentId: number) => {
  try {
    // 获取课题下的所有任务
    const tasks = await getProjectTasks(projectId);
    
    if (tasks.length === 0) {
      console.log('该课题下没有任务需要分配');
      return;
    }
    
    console.log(`尝试分配课题(ID:${projectId})下的${tasks.length}个任务给学生(ID:${studentId})`);
    
    // 创建一个任务分配Promise数组
    const assignPromises = tasks.map(task => 
      assignTaskToStudent(task.id, studentId)
        .catch(error => {
          console.error(`任务(ID:${task.id})分配失败:`, error);
          return null; // 返回null表示分配失败，但不中断其他任务的分配
        })
    );
    
    // 等待所有任务分配完成
    const results = await Promise.all(assignPromises);
    
    // 计算成功分配的任务数量
    const successCount = results.filter(result => result !== null).length;
    
    console.log(`成功分配${successCount}/${tasks.length}个任务给学生`);
    
    return {
      total: tasks.length,
      success: successCount,
      failed: tasks.length - successCount
    };
  } catch (error) {
    console.error('分配课题任务失败:', error);
    throw error;
  }
};

const handleAssignProject = async () => {
  if (!projectToAssign.value || !selectedStudentId.value) {
    ElMessage.warning('请选择要分配的学生');
    return;
  }
  
  loading.value = true;
  try {
    // 分配课题给学生
    const result = await assignProjectToStudent(projectToAssign.value.id, selectedStudentId.value);
    ElMessage.success('课题分配成功');
    
    // 更新本地数据
    const index = projects.value.findIndex(p => p.id === projectToAssign.value?.id);
    if (index !== -1) {
      projects.value[index] = result;
    }
    
    // 如果当前在详情页面，也更新当前课题
    if (currentProject.value && currentProject.value.id === projectToAssign.value.id) {
      currentProject.value = result;
    }
    
    // 分配课题下的所有任务给同一学生
    try {
      const assignTaskResult = await assignProjectTasksToStudent(projectToAssign.value.id, selectedStudentId.value);
      
      if (assignTaskResult && assignTaskResult.total > 0) {
        if (assignTaskResult.success === assignTaskResult.total) {
          ElMessage.success(`已成功将${assignTaskResult.success}个任务分配给学生`);
        } else {
          ElMessage.warning(`任务分配部分成功：${assignTaskResult.success}/${assignTaskResult.total}个任务已分配`);
        }
        
        // 如果当前在详情页面，刷新任务列表
        if (currentProject.value && currentProject.value.id === projectToAssign.value.id) {
          fetchProjectTasks(currentProject.value.id);
        }
      }
    } catch (taskError) {
      console.error('分配任务失败:', taskError);
      ElMessage.warning('课题分配成功，但任务分配失败，请手动分配任务');
    }
    
    assignDialogVisible.value = false;
    projectToAssign.value = null;
  } catch (error) {
    console.error('分配课题失败:', error);
    ElMessage.error('分配课题失败');
  } finally {
    loading.value = false;
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

// 获取任务状态样式
const getTaskStatusType = (status: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (status) {
    case '未开始': return 'primary';
    case '进行中': return 'warning';
    case '已完成': return 'success';
    case '初稿': return 'info';
    case '期中': return 'warning';
    case '终稿': return 'success';
    default: return 'primary';
  }
};

// 获取任务优先级样式
const getTaskPriorityType = (priority: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (priority) {
    case '高': return 'danger';
    case '中': return 'warning';
    case '低': return 'success';
    default: return 'warning';
  }
};

// 下面添加新函数 submitProjectForm
const submitProjectForm = async () => {
  if (!projectFormRef.value) return;
  
  await projectFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      
      try {
        const projectData = {
          ...projectForm.value
        };
        
        // 添加时间范围
        if (startTime.value && endTime.value) {
          projectData.startTime = startTime.value;
          projectData.endTime = endTime.value;
        }
        
        let result;
        if (isEditing.value && projectForm.value.id) {
          // 更新课题
          result = await updateProject(projectForm.value.id, projectData);
          ElMessage.success('课题更新成功');
          
          // 处理编辑模式下的任务
          if (newProjectTasks.value.length > 0) {
            const projectId = projectForm.value.id;
            console.log('编辑模式: 处理课题关联任务', newProjectTasks.value);
            
            // 检查课题是否已分配给学生
            const isAssignedToStudent = result.assigneeId !== null && result.assigneeId !== undefined;
            console.log('课题分配状态:', isAssignedToStudent ? `已分配给学生ID: ${result.assigneeId}` : '未分配');
            
            // 处理新增或修改的任务
            const promises = newProjectTasks.value.map(task => {
              const taskData = {
                ...task,
                supervisorId: userStore.user.id,
                projectId: projectId
              };
              
              // 如果课题已分配给学生，则任务也分配给该学生
              if (isAssignedToStudent) {
                taskData.assigneeId = result.assigneeId;
              }
              
              // 如果当前任务没有设置时间，但是课题有时间，可以使用课题时间
              if ((!taskData.startTime || !taskData.endTime) && startTime.value && endTime.value) {
                taskData.startTime = taskData.startTime || startTime.value;
                taskData.endTime = taskData.endTime || endTime.value;
              }
              
              // 如果任务有ID，则更新任务；否则创建新任务
              if (task.id) {
                return updateTask(task.id, taskData);
              } else {
                return createTask(taskData);
              }
            });
            
            // 等待所有任务操作完成
            await Promise.all(promises);
            
            // 根据课题分配状态给出不同的成功提示
            if (isAssignedToStudent) {
              ElMessage.success(`已成功更新 ${newProjectTasks.value.length} 个关联任务，并自动分配给负责学生`);
            } else {
              ElMessage.success(`已成功更新 ${newProjectTasks.value.length} 个关联任务`);
            }
          }
        } else {
          // 创建课题
          result = await createProject(projectData);
          ElMessage.success('课题创建成功');
          
          // 如果有关联任务，则创建任务
          if (newProjectTasks.value.length > 0 && result.id) {
            const projectId = result.id;
            
            // 检查新创建的课题是否已分配给学生（通常新创建的课题未分配）
            const isAssignedToStudent = result.assigneeId !== null && result.assigneeId !== undefined;
            console.log('新课题分配状态:', isAssignedToStudent ? `已分配给学生ID: ${result.assigneeId}` : '未分配');
            
            const promises = newProjectTasks.value.map(task => {
              // 创建任务数据
              const taskData = {
                ...task,
                supervisorId: userStore.user.id,
                projectId: projectId
              };
              
              // 如果课题已分配给学生，则任务也分配给该学生
              if (isAssignedToStudent) {
                taskData.assigneeId = result.assigneeId;
              }
              
              // 如果当前任务没有设置时间，但是课题有时间，可以使用课题时间
              if ((!taskData.startTime || !taskData.endTime) && startTime.value && endTime.value) {
                taskData.startTime = taskData.startTime || startTime.value;
                taskData.endTime = taskData.endTime || endTime.value;
              }
              
              // 创建任务
              return createTask(taskData);
            });
            
            // 等待所有任务创建完成
            await Promise.all(promises);
            
            // 根据课题分配状态给出不同的成功提示
            if (isAssignedToStudent) {
              ElMessage.success(`已成功创建 ${newProjectTasks.value.length} 个关联任务，并自动分配给负责学生`);
            } else {
              ElMessage.success(`已成功创建 ${newProjectTasks.value.length} 个关联任务`);
            }
          }
        }
        
        projectFormDialogVisible.value = false;
        fetchProjects(); // 刷新课题列表
      } catch (error) {
        console.error('提交课题失败:', error);
        ElMessage.error('提交课题失败');
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.error('请填写所有必填项');
    }
  });
};

// 打开添加任务对话框
const openAddTaskDialog = () => {
  // 初始化任务表单
  taskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始',
    supervisorId: userStore.user.id,
    projectId: currentProject.value?.id || null,
    assigneeId: null,
    completed: false
  };
  
  console.log('添加任务表单数据:', taskForm.value);
  taskFormVisible.value = true;
};

// 提交任务表单
const submitTaskForm = async () => {
  if (!taskForm.value.title) {
    ElMessage.warning('请输入任务标题');
    return;
  }
  
  if (!taskForm.value.projectId) {
    ElMessage.warning('无法关联到当前课题');
    return;
  }
  
  loading.value = true;
  try {
    // 确保projectId字段保留
    const taskData = {
      ...taskForm.value,
      projectId: currentProject.value?.id || taskForm.value.projectId
    };
    
    console.log('提交任务数据:', taskData);
    
    // 创建任务
    await createTask(taskData);
    ElMessage.success('任务创建成功');
    
    // 刷新任务列表
    if (currentProject.value?.id) {
      fetchProjectTasks(currentProject.value.id);
    }
    
    // 关闭对话框
    taskFormVisible.value = false;
  } catch (error) {
    console.error('创建任务失败:', error);
    ElMessage.error('创建任务失败');
  } finally {
    loading.value = false;
  }
};

// 删除任务
const handleDeleteTask = async (task: Task) => {
  ElMessageBox.confirm('确认删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    loading.value = true;
    try {
      const success = await deleteTask(task.id);
      if (success) {
        ElMessage.success('任务删除成功');
        
        // 刷新任务列表
        if (currentProject.value?.id) {
          fetchProjectTasks(currentProject.value.id);
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
  }).catch(() => {
    // 取消删除
  });
};

// 添加新任务
const addNewTask = () => {
  // 如果正在编辑任务，则更新该任务
  if (editingTaskIndex.value >= 0) {
    newProjectTasks.value[editingTaskIndex.value] = {...newTaskForm.value};
    editingTaskIndex.value = -1;
  } else {
    // 否则添加新任务
    newProjectTasks.value.push({...newTaskForm.value});
  }
  
  // 重置表单
  newTaskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始',
    startTime: null,
    endTime: null
  };
  
  // 关闭对话框
  newTaskFormVisible.value = false;
};

// 编辑创建课题时的任务
const editNewTask = (index: number) => {
  // 设置编辑的索引
  editingTaskIndex.value = index;
  
  // 设置表单数据
  const task = newProjectTasks.value[index];
  newTaskForm.value = {
    title: task.title,
    description: task.description || '',
    priority: task.priority || '中',
    status: task.status || '未开始',
    startTime: task.startTime || null,
    endTime: task.endTime || null
  };
  
  // 打开对话框
  newTaskFormVisible.value = true;
};

// 删除创建课题时的任务
const removeNewTask = (index: number) => {
  newProjectTasks.value.splice(index, 1);
};

// 添加新任务对话框
const openNewTaskDialog = () => {
  // 重置索引
  editingTaskIndex.value = -1;
  
  // 初始化任务表单
  newTaskForm.value = {
    title: '',
    description: '',
    priority: '中',
    status: '未开始',
    startTime: null,
    endTime: null
  };
  
  // 打开对话框
  newTaskFormVisible.value = true;
};

// 批量选择任务对话框
const openBatchSelectTasksDialog = async () => {
  // 清空已选任务
  selectedTasks.value = [];
  
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

const handleTaskSelectionChange = (selected: Task[]) => {
  selectedTasks.value = selected;
};

const addSelectedTasks = () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请至少选择一个任务');
    return;
  }
  
  // 将选中的任务添加到新项目任务列表
  selectedTasks.value.forEach(task => {
    // 检查是否已经添加过相同的任务
    const exists = newProjectTasks.value.some(t => 
      t.title === task.title && t.description === task.description
    );
    
    if (!exists) {
      newProjectTasks.value.push({
        title: task.title,
        description: task.description,
        priority: task.priority || '中',
        status: task.status || '未开始',
        startTime: task.startTime || null,
        endTime: task.endTime || null
      });
    }
  });
  
  ElMessage.success(`已添加 ${selectedTasks.value.length} 个任务`);
  batchSelectTasksVisible.value = false;
};

const viewTaskSubmissions = async (task: Task) => {
  currentSubmissionTask.value = task;
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
    
    // 显示下载中提示
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在准备下载文件...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
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
      loadingInstance.close();
      
      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('文件不存在或已被删除');
        } else if (response.status === 403) {
          throw new Error('您没有权限下载此文件');
        } else if (response.status === 401) {
          throw new Error('授权已过期，请重新登录');
        } else {
          throw new Error(`下载失败: ${response.status}`);
        }
      }
      return response.blob();
    })
    .then(blob => {
      // 检查是否是错误信息而不是文件
      if (blob.type === 'application/json') {
        // 如果是JSON，则可能是错误信息
        return blob.text().then(text => {
          try {
            const errorData = JSON.parse(text);
            throw new Error(errorData.message || '下载失败，请稍后重试');
          } catch (e) {
            throw new Error('下载失败，文件格式不正确');
          }
        });
      }
      
      // 确认是文件blob，创建下载链接
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
      loadingInstance.close();
      console.error('下载文件失败:', error);
      
      // 显示用户友好的错误信息
      ElMessageBox.alert(
        `下载文件"${submission.originalFilename}"失败: ${error.message}`, 
        '下载错误', 
        {
          confirmButtonText: '确定',
          type: 'error',
          callback: () => {
            // 可选：提供重试或其他解决方案建议
            ElMessage({
              type: 'info',
              message: '请联系管理员检查文件是否存在或尝试重新上传文件'
            });
          }
        }
      );
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

// 检查待审核课题数量
const checkPendingReviews = async () => {
  try {
    const pendingProjects = await getPendingReviewProjects(userStore.user.id);
    pendingReviewCount.value = pendingProjects?.length || 0;
  } catch (error) {
    console.error('获取待审核课题数量失败:', error);
  }
};

// 审核课题申请
const approveProjectApplication = async () => {
  if (!projectToReview.value) {
    ElMessage.warning('请选择要审核的课题');
    return;
  }
  
  reviewing.value = true;
  try {
    const result = await reviewProjectApplication(projectToReview.value.id, true);
    ElMessage.success('课题审核通过');
    
    // 更新本地数据
    const index = projects.value.findIndex(p => p.id === projectToReview.value.id);
    if (index !== -1) {
      projects.value[index] = result;
    }
    
    // 如果当前在详情页面，也更新当前课题
    if (currentProject.value && currentProject.value.id === projectToReview.value.id) {
      currentProject.value = result;
    }
    
    reviewDialogVisible.value = false;
    projectToReview.value = null;
  } catch (error) {
    console.error('审核课题失败:', error);
    ElMessage.error('审核课题失败');
  } finally {
    reviewing.value = false;
    // 更新待审核数量，使红点数字减少
    checkPendingReviews();
  }
};

const rejectProjectApplication = async () => {
  if (!projectToReview.value) {
    ElMessage.warning('请选择要审核的课题');
    return;
  }
  
  reviewing.value = true;
  try {
    const result = await reviewProjectApplication(projectToReview.value.id, false);
    ElMessage.success('课题审核拒绝');
    
    // 更新本地数据
    const index = projects.value.findIndex(p => p.id === projectToReview.value.id);
    if (index !== -1) {
      projects.value[index] = result;
    }
    
    // 如果当前在详情页面，也更新当前课题
    if (currentProject.value && currentProject.value.id === projectToReview.value.id) {
      currentProject.value = result;
    }
    
    reviewDialogVisible.value = false;
    projectToReview.value = null;
  } catch (error) {
    console.error('审核课题失败:', error);
    ElMessage.error('审核课题失败');
  } finally {
    reviewing.value = false;
    // 更新待审核数量，使红点数字减少
    checkPendingReviews();
  }
};

const openReviewDialog = (project: Project) => {
  projectToReview.value = project;
  reviewDialogVisible.value = true;
};

// 使用课题时间（快捷按钮）
const useProjectTime = () => {
  if (startTime.value && endTime.value) {
    newTaskForm.value.startTime = startTime.value;
    newTaskForm.value.endTime = endTime.value;
    ElMessage.success('已使用课题时间');
  } else {
    ElMessage.warning('课题时间未设置');
  }
};

// 加载课题评价
const loadProjectEvaluation = async (projectId: number) => {
  try {
    evaluationLoading.value = true;
    const evaluation = await getProjectEvaluation(projectId);
    currentEvaluation.value = evaluation;
  } catch (error: any) {
    // 忽略404或无数据错误，因为可能课题尚未评价
    if (error.response && error.response.status === 404) {
      console.log('课题尚未有评价，这是正常情况');
      currentEvaluation.value = null;
      
      // 重置评价表单为默认值
      evaluationForm.value.score = 80;
      evaluationForm.value.comment = '';
    } else if (!error.response || error.response.status !== 404) {
      console.error('获取课题评价失败:', error);
      // 不显示错误消息，而是静默处理
      
      currentEvaluation.value = null;
      
      // 重置评价表单为默认值
      evaluationForm.value.score = 80;
      evaluationForm.value.comment = '';
    }
  } finally {
    evaluationLoading.value = false;
  }
};

// 提交课题评价
const submitEvaluation = async () => {
  if (!currentProject.value) return;
  
  try {
    evaluationLoading.value = true;
    console.log('提交评价数据:', {
      projectId: currentProject.value.id,
      score: evaluationForm.value.score,
      comment: evaluationForm.value.comment
    });
    
    const response = await evaluateProject(
      currentProject.value.id,
      evaluationForm.value.score,
      evaluationForm.value.comment
    );
    
    console.log('评价提交响应:', response);
    
    // 验证返回数据的有效性
    if (response && response.id && response.score !== undefined) {
      currentEvaluation.value = response;
      ElMessage.success('评价提交成功');
    } else {
      // 数据不完整，保持表单状态
      console.error('API返回的评价数据不完整:', response);
      currentEvaluation.value = null; // 确保表单继续显示
      ElMessage.warning('评价提交成功，但无法显示评价详情');
    }
  } catch (error: any) {
    console.error('评价提交失败:', error);
    // 保持表单状态
    currentEvaluation.value = null;
    ElMessage.error(`评价提交失败: ${error.message || '未知错误'}`);
  } finally {
    evaluationLoading.value = false;
  }
};

// 编辑课题评价
const editEvaluation = () => {
  if (currentEvaluation.value) {
    evaluationForm.value.score = currentEvaluation.value.score;
    evaluationForm.value.comment = currentEvaluation.value.comment || '';
    currentEvaluation.value = null;
  }
};

// 删除课题评价
const deleteEvaluation = async () => {
  if (!currentProject.value) return;
  
  try {
    await ElMessageBox.confirm('确定要删除此评价吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    evaluationLoading.value = true;
    const success = await deleteProjectEvaluation(currentProject.value.id);
    if (success) {
      currentEvaluation.value = null;
      
      // 重置评价表单
      evaluationForm.value.score = 80;
      evaluationForm.value.comment = '';
      
      ElMessage.success('评价已删除');
    } else {
      ElMessage.error('删除评价失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error);
      ElMessage.error(`删除评价失败: ${error.message || '未知错误'}`);
    }
  } finally {
    evaluationLoading.value = false;
  }
};
</script>

<style scoped>
.project-management {
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

.project-tabs {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.project-description,
.project-requirements,
.project-resources {
  white-space: pre-wrap;
  word-break: break-word;
}

.project-resources {
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.task-section {
  margin-top: 20px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.empty-task {
  padding: 20px;
  text-align: center;
}

.submission-list {
  margin-top: 20px;
}

.task-info {
  margin-bottom: 20px;
}

.no-submissions {
  padding: 20px 0;
  text-align: center;
}

.task-create-section {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.empty-tasks-tip {
  margin: 20px 0;
  text-align: center;
}

.button-group {
  display: flex;
  gap: 10px;
}

.review-content {
  padding: 20px;
}

.review-badge {
  margin-left: 5px;
}

.project-evaluation-section {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.evaluation-details {
  margin-top: 20px;
}

.task-loading-tip {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.auto-assign-tip {
  margin-top: 10px;
  font-size: 0.8em;
  color: #909399;
}
</style>

 