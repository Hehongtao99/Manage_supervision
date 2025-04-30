<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

interface LogEntry {
  id: number
  timestamp: string
  level: string
  message: string
  source: string
  threadName?: string
  logger?: string
}

const logs = ref<LogEntry[]>([])
const loading = ref(false)

// 日志过滤参数
const filters = reactive({
  level: 'all',
  timeRange: '24h'
})

// 监听过滤条件变化重新加载日志
watch(filters, () => {
  fetchLogs()
})

// 从后端获取日志数据
const fetchLogs = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('您需要登录才能查看系统日志')
      return
    }

    const response = await axios.get('/api/logs/system', {
      headers: {
        Authorization: `Bearer ${token}`
      },
      params: {
        level: filters.level,
        timeRange: filters.timeRange
      }
    })

    // 转换日志格式
    logs.value = response.data.map((log: any) => ({
      id: log.id,
      timestamp: log.timestamp,
      level: log.level.toLowerCase(),
      message: log.message,
      source: log.source,
      threadName: log.threadName,
      logger: log.logger
    }))
  } catch (error: any) {
    console.error('获取日志失败:', error)
    ElMessage.error(error.response?.data?.message || '获取日志失败')
    logs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="system-logs">
    <h1 class="page-title">系统日志</h1>
    
    <div class="filters">
      <div class="filter-group">
        <label>日志级别：</label>
        <select v-model="filters.level">
          <option value="all">全部</option>
          <option value="info">信息</option>
          <option value="warning">警告</option>
          <option value="error">错误</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>时间范围：</label>
        <select v-model="filters.timeRange">
          <option value="1h">最近1小时</option>
          <option value="24h">最近24小时</option>
          <option value="7d">最近7天</option>
          <option value="30d">最近30天</option>
        </select>
      </div>
    </div>

    <div class="logs-container">
      <div v-if="loading" class="loading">
        加载中...
      </div>
      
      <template v-else>
        <div v-if="logs.length === 0" class="no-logs">
          暂无日志记录
        </div>
        
        <div v-else class="log-entries">
          <div v-for="log in logs" :key="log.id" class="log-entry" :class="log.level">
            <div class="log-timestamp">{{ new Date(log.timestamp).toLocaleString() }}</div>
            <div class="log-level">{{ log.level.toUpperCase() }}</div>
            <div class="log-source">{{ log.source }}</div>
            <div class="log-message">{{ log.message }}</div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.system-logs {
  padding: 24px;
}

.page-title {
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.logs-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-height: 400px;
  height: calc(100vh - 200px); /* 使容器高度固定，留出页面上方的空间 */
  display: flex;
  flex-direction: column;
}

.loading,
.no-logs {
  padding: 32px;
  text-align: center;
  color: #999;
}

.log-entries {
  padding: 16px;
  overflow-y: auto; /* 添加垂直滚动条 */
  flex: 1; /* 填充剩余空间 */
}

.log-entry {
  display: grid;
  grid-template-columns: 180px 80px 120px 1fr;
  gap: 16px;
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.log-entry:last-child {
  border-bottom: none;
}

.log-entry.info {
  border-left: 4px solid #4caf50;
}

.log-entry.warning, .log-entry.warn {
  border-left: 4px solid #ff9800;
}

.log-entry.error {
  border-left: 4px solid #f44336;
}

.log-timestamp {
  color: #666;
  font-size: 14px;
}

.log-level {
  font-weight: 500;
}

.log-source {
  color: #666;
}

.log-message {
  color: #333;
}
</style> 