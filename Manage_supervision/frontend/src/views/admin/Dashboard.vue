<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from '../../utils/axios'

const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalRoles: 0,
  systemHealth: '正常',
  totalStudents: 0,
  totalTeachers: 0,
  totalClasses: 0,
  totalNotifications: 0
})

const loading = ref(true)

const fetchDashboardData = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/dashboard/admin/stats')
    console.log('管理员仪表盘数据:', response.data)
    
    if (response.data) {
      statistics.value = {
        totalUsers: response.data.totalUsers || 0,
        activeUsers: response.data.activeUsers || 0,
        totalRoles: response.data.totalRoles || 0,
        systemHealth: response.data.systemStatus || '正常',
        totalStudents: response.data.totalStudents || 0,
        totalTeachers: response.data.totalTeachers || 0,
        totalClasses: response.data.totalClasses || 0,
        totalNotifications: response.data.totalNotifications || 0
      }
    }
  } catch (error) {
    console.error('获取管理员仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchDashboardData()
})
</script>

<template>
  <div class="admin-dashboard">
    <h1 class="dashboard-title">管理控制台</h1>
    
    <el-row v-loading="loading" :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalUsers }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.activeUsers }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>角色数量</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalRoles }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.systemHealth }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增的统计信息行 -->
    <el-row :gutter="20" class="second-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>学生总数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalStudents }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>教师总数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalTeachers }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>班级总数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalClasses }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>通知总数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalNotifications }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.admin-dashboard {
  padding: 24px;
}

.dashboard-title {
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
}

.stat-card {
  margin-bottom: 20px;
  height: 120px;
  overflow: visible;
}

.stat-value {
  font-size: 28px;
  font-weight: 500;
  color: #333;
  text-align: center;
  margin-top: 10px;
  white-space: nowrap;
  overflow: visible;
  height: 48px;
  line-height: 48px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.second-row {
  margin-top: 10px;
}
</style> 