<script setup lang="ts">
import { ref, onMounted } from 'vue'

const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalRoles: 0,
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
        <h3>角色数量</h3>
        <div class="stat-value">{{ statistics.totalRoles }}</div>
      </div>
      
      <div class="stat-card">
        <h3>系统状态</h3>
        <div class="stat-value">{{ statistics.systemHealth }}</div>
      </div>
    </div>

    <div class="dashboard-section">
      <h2>最近活动</h2>
      <p class="placeholder-text">暂无活动记录</p>
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
}

.dashboard-section h2 {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.placeholder-text {
  color: #999;
  text-align: center;
  padding: 32px 0;
}
</style> 