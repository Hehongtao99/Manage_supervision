<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, UserFilled, Avatar } from '@element-plus/icons-vue'

const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalAnalysts: 0,
  systemHealth: '正常'
})

onMounted(async () => {
  // TODO: 从后端获取实际统计数据
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
        <router-link to="/admin/students" class="quick-link-card">
          <div class="icon-container">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="link-text">用户管理</div>
        </router-link>
        
        <router-link to="/admin/teachers" class="quick-link-card">
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
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.stat-value {
  font-size: 24px;
  font-weight: 500;
  color: #333;
}

.dashboard-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 32px;
}

.dashboard-section h2 {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.quick-link-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
}

.quick-link-card:hover {
  background-color: #ecf5ff;
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.icon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  background-color: #409EFF;
  border-radius: 50%;
  margin-bottom: 12px;
}

.icon-container .el-icon {
  font-size: 30px;
  color: white;
}

.link-text {
  font-size: 16px;
  font-weight: 500;
}
</style> 