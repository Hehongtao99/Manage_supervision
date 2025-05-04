<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, UserFilled, Avatar } from '@element-plus/icons-vue'
import { getDashboardStats } from '../../api/dashboard'
import { ElMessage } from 'element-plus'

const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalAnalysts: 0,
  systemHealth: '正常'
})

onMounted(async () => {
  try {
    const data = await getDashboardStats()
    
    // 更新统计数据
    statistics.value.totalUsers = data.totalUsers || 0
    statistics.value.activeUsers = data.activeUsers || 0
    statistics.value.totalAnalysts = data.totalAnalysts || 0
    statistics.value.systemHealth = data.systemInfo?.systemStatus || '正常'
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
    ElMessage.error('获取仪表盘数据失败')
  }
})
</script>

<template>
  <div class="admin-dashboard">
    <h1 class="dashboard-title">管理控制台</h1>
    
    <div class="statistics-grid">
      <div class="stat-card">
        <h3>总用户数</h3>
        <div class="stat-value">{{ statistics.totalUsers }}</div>
      </div>
      
      <div class="stat-card">
        <h3>活跃用户</h3>
        <div class="stat-value">{{ statistics.activeUsers }}</div>
      </div>
      
      <div class="stat-card">
        <h3>安全分析师数量</h3>
        <div class="stat-value">{{ statistics.totalAnalysts }}</div>
      </div>
      
      <div class="stat-card">
        <h3>系统状态</h3>
        <div class="stat-value">{{ statistics.systemHealth }}</div>
      </div>
    </div>

    <div class="dashboard-section">
      <h2>用户管理快捷入口</h2>
      <div class="quick-links">
        <router-link to="/admin/users" class="quick-link-card">
          <div class="icon-container">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="link-text">用户管理</div>
        </router-link>
        
        <router-link to="/admin/security-analysts" class="quick-link-card">
          <div class="icon-container">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="link-text">安全分析师管理</div>
        </router-link>
      </div>
    </div>
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

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h3 {
  font-size: 16px;
  color: #606266;
  margin-bottom: 16px;
  font-weight: normal;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.dashboard-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 32px;
}

.dashboard-section h2 {
  font-size: 18px;
  margin-bottom: 24px;
  font-weight: 500;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-link-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  padding: 24px;
  border-radius: 8px;
  background-color: #f5f7fa;
  transition: all 0.3s;
}

.quick-link-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.icon-container {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #ecf5ff;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 12px;
}

.icon-container .el-icon {
  font-size: 24px;
  color: #409EFF;
}

.link-text {
  font-size: 14px;
  font-size: 16px;
  font-weight: 500;
}
</style> 