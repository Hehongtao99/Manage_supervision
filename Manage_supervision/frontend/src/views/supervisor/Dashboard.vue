<template>
  <div class="supervisor-dashboard">
    <el-card title="Supervisor Console" class="dashboard-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>Supervisor Console</span>
          <div class="header-actions">
            <el-button type="primary" class="refresh-button" @click="fetchDashboardData" :loading="loading">
              <el-icon><Refresh /></el-icon>
              Refresh Data
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs type="border-card" v-model="activeTab">
        <el-tab-pane label="Student List" name="students">
          <el-table :data="studentsWithProgress" style="width: 100%">
            <el-table-column prop="studentName" label="Name" />
            <el-table-column prop="projectTitle" label="Project" />
            <el-table-column label="Progress" width="180">
              <template #default="scope">
                <div class="progress-wrapper">
                  <el-progress 
                    :percentage="scope.row.progressPercentage" 
                    :status="getProgressStatus(scope.row.progressPercentage)"
                    :stroke-width="16"
                  />
                  <span class="progress-text">{{ scope.row.completedTasks }}/{{ scope.row.totalTasks }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="Status">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
                  {{ scope.row.status === 'active' ? 'Active' : 'Inactive' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions">
              <template #default="scope">
                <el-button size="small" @click="viewStudentDetails(scope.row)">View</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination 
              layout="prev, pager, next" 
              :total="studentsWithProgress.length"
              :page-size="pagination.pageSize" 
            />
          </div>
          <el-empty v-if="!studentsWithProgress || studentsWithProgress.length === 0"
                   description="No student project progress data available"></el-empty>
        </el-tab-pane>
        <el-tab-pane label="Recent Activities" name="recent">
          <div class="activity-list">
            <el-card v-for="(activity, index) in dashboardData.recentActivities" :key="index" 
                    class="activity-item" shadow="hover">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-description">{{ activity.description }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </el-card>
            <el-empty v-if="!dashboardData.recentActivities || dashboardData.recentActivities.length === 0"
                     description="No activity records"></el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElCard, ElButton, ElIcon, ElTabs, ElTabPane, 
         ElTable, ElTableColumn, ElTag, ElPagination, ElProgress, ElEmpty, ElMessage } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import axiosInstance from '../../utils/axios'; // 引入已配置的axios实例
import { useUserStore } from '../../stores/user'; // 引入用户store

// 路由实例
const router = useRouter();
// 用户store
const userStore = useUserStore();

// 当前激活的标签页
const activeTab = ref('students');

// 仪表盘数据
const dashboardData = ref({
  studentCount: 0,
  activeToday: 0,
  pendingTasks: 0,
  weeklyEvents: 0,
  studentProjectProgresses: [],
  recentActivities: []
});

// 加载状态
const loading = ref(false);

// 学生列表配置
const studentColumns = [
  { title: 'Name', key: 'studentName' },
  { title: 'Project', key: 'projectTitle' },
  { title: 'Progress', key: 'progress' },
  { title: 'Status', key: 'status' },
  { title: 'Actions', key: 'actions' }
];

// 学生进度列表计算属性
const studentsWithProgress = computed(() => {
  // 添加调试日志
  console.log('Student progress data:', dashboardData.value.studentProjectProgresses);
  // 确保每个学生都有姓名
  return (dashboardData.value.studentProjectProgresses || []).map(student => {
    if (!student.studentName) {
      console.warn('Found student without name:', student);
      student.studentName = 'Unknown Student';
    }
    return student;
  });
});

// 分页配置
const pagination = {
  pageSize: 5
};

// 根据进度百分比获取状态
function getProgressStatus(percentage) {
  if (percentage < 30) {
    return 'exception';
  } else if (percentage < 70) {
    return 'warning';
  } else {
    return 'success';
  }
}

// 查看学生详情
function viewStudentDetails(student) {
  console.log('Viewing student details:', student);
  router.push({
    name: 'StudentManagement',
    params: { id: student.studentId }
  });
}

// 获取仪表盘数据
async function fetchDashboardData() {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    ElMessage.error('You are not logged in, please login first');
    router.push('/login');
    return;
  }

  // 检查用户是否有督导员角色
  if (!userStore.isSupervisor) {
    ElMessage.error('You do not have supervisor privileges to access this page');
    router.push('/');
    return;
  }

  loading.value = true;
  try {
    console.log('Starting request for supervisor dashboard data');
    
    // 使用已配置的axios实例
    const response = await axiosInstance.get('/api/dashboard/supervisor/stats');
    
    console.log('Dashboard data retrieved successfully:', response.data);
    
    // 检查学生姓名
    if (response.data.studentProjectProgresses && response.data.studentProjectProgresses.length > 0) {
      response.data.studentProjectProgresses.forEach(student => {
        console.log(`Student ID: ${student.studentId}, Name: ${student.studentName}`);
      });
    }
    
    dashboardData.value = response.data;
    ElMessage.success('Data updated');
  } catch (error) {
    console.error('Failed to get dashboard data:', error);
    
    // 详细的错误处理
    if (error.response) {
      // 服务器响应了错误状态码
      const status = error.response.status;
      const errorMsg = error.response.data?.message || 'Unknown error';
      
      if (status === 401) {
        ElMessage.error('Authentication expired, please login again');
        userStore.logout(); // 登出并清除认证状态
        router.push('/login');
      } else if (status === 403) {
        ElMessage.error('Insufficient privileges, please contact administrator');
      } else {
        ElMessage.error(`Failed to get data: ${errorMsg}`);
      }
    } else if (error.request) {
      // 请求已发送但未收到响应
      ElMessage.error('No response from server, please check your network connection');
    } else {
      // 请求未能发送
      ElMessage.error(`Request error: ${error.message}`);
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  // 页面加载时获取仪表盘数据
  fetchDashboardData();
});
</script>

<style scoped>
.supervisor-dashboard {
  padding: 16px;
}

.dashboard-card {
  margin-bottom: 16px;
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

.refresh-button {
  margin-left: auto;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-item {
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-item:hover {
  transform: translateY(-2px);
}

.activity-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.activity-description {
  color: #606266;
  font-size: 14px;
}

.activity-time {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
  text-align: right;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.progress-wrapper {
  display: flex;
  flex-direction: column;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: center;
}
</style> 