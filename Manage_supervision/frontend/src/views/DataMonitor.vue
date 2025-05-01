<template>
  <div class="data-monitor-container">
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <h2>数据监控分析</h2>
          <div class="header-actions">
            <el-button type="primary" @click="refreshData">
              刷新数据
            </el-button>
            <el-button type="success" @click="exportData">
              导出报告
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="monitor-content">
        <!-- 数据概览卡片 -->
        <div class="stat-cards">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ stats.totalRecords || 0 }}</div>
                <div class="stat-label">总数据量</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card warning">
                <div class="stat-value">{{ stats.anomalyCount || 0 }}</div>
                <div class="stat-label">异常数据</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card success">
                <div class="stat-value">{{ stats.resolvedCount || 0 }}</div>
                <div class="stat-label">已处理异常</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card info">
                <div class="stat-value">{{ stats.lastUpdated ? formatDate(stats.lastUpdated) : '无数据' }}</div>
                <div class="stat-label">最近更新</div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 筛选表单 -->
        <div class="filter-form">
          <el-form :model="queryParams" inline>
            <el-form-item label="开始日期">
              <el-date-picker 
                v-model="queryParams.startDate" 
                type="datetime" 
                placeholder="开始日期"
                format="YYYY-MM-DD HH:mm"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker 
                v-model="queryParams.endDate" 
                type="datetime" 
                placeholder="结束日期"
                format="YYYY-MM-DD HH:mm"
              />
            </el-form-item>
            <el-form-item label="异常阈值">
              <el-input-number v-model="queryParams.threshold" :min="0" :max="100" :step="5" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="queryParams.anomalyOnly">仅显示异常</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onFilterChange">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 数据表格 -->
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="dataType" label="数据类型" width="120" />
          <el-table-column prop="value" label="数值" width="100" />
          <el-table-column prop="expected" label="预期范围" width="120" />
          <el-table-column prop="deviation" label="偏差率" width="100">
            <template #default="scope">
              {{ scope.row.deviation ? scope.row.deviation.toFixed(2) + '%' : '0%' }}
            </template>
          </el-table-column>
          <el-table-column label="异常" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.isAnomaly ? 'danger' : 'success'">
                {{ scope.row.isAnomaly ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.resolved ? 'success' : 'warning'" v-if="scope.row.isAnomaly">
                {{ scope.row.resolved ? '已处理' : '未处理' }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="recordTime" label="记录时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.recordTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="source" label="来源" width="120" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button 
                v-if="scope.row.isAnomaly && !scope.row.resolved"
                type="primary" 
                size="small" 
                @click="markAsResolved(scope.row)"
              >
                标记已处理
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="showDetails(scope.row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItems"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserMonitorData, 
  getMonitorStats, 
  markAnomalyAsResolved,
  exportMonitorReport
} from '../api/dataMonitor'
import { format } from 'date-fns'

// 状态变量
const loading = ref(false)
const tableData = ref([])
const stats = ref({
  totalRecords: 0,
  anomalyCount: 0,
  resolvedCount: 0,
  lastUpdated: null,
  anomaliesByType: {}
})

// 分页变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

// 查询参数
const queryParams = reactive({
  startDate: null,
  endDate: null,
  threshold: 20,
  anomalyOnly: false
})

// 获取数据统计
const fetchStats = async () => {
  try {
    const res = await getMonitorStats()
    stats.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取表格数据
const fetchTableData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      page: currentPage.value - 1,
      size: pageSize.value,
      startDate: queryParams.startDate ? formatDateForBackend(queryParams.startDate) : null,
      endDate: queryParams.endDate ? formatDateForBackend(queryParams.endDate) : null
    }
    
    const res = await getUserMonitorData(params)
    tableData.value = res.data.records
    totalItems.value = res.data.totalItems
  } catch (error) {
    console.error('获取监控数据失败:', error)
    ElMessage.error('获取监控数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchStats()
  fetchTableData()
})

// 刷新数据
const refreshData = () => {
  fetchStats()
  fetchTableData()
}

// 导出数据
const exportData = async () => {
  try {
    const params = {
      ...queryParams,
      startDate: queryParams.startDate ? formatDateForBackend(queryParams.startDate) : null,
      endDate: queryParams.endDate ? formatDateForBackend(queryParams.endDate) : null
    }
    
    const res = await exportMonitorReport(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `monitor_report_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出报告失败:', error)
    ElMessage.error('导出报告失败')
  }
}

// 标记为已处理
const markAsResolved = async (row) => {
  try {
    await markAnomalyAsResolved(row.id)
    ElMessage.success('已标记为已处理')
    
    // 刷新数据
    row.resolved = true
    fetchStats()
  } catch (error) {
    console.error('标记失败:', error)
    ElMessage.error('标记失败')
  }
}

// 显示详情
const showDetails = (row) => {
  ElMessageBox.alert(
    `<div>
      <p><strong>ID:</strong> ${row.id}</p>
      <p><strong>数据类型:</strong> ${row.dataType}</p>
      <p><strong>数值:</strong> ${row.value}</p>
      <p><strong>预期范围:</strong> ${row.expected}</p>
      <p><strong>偏差率:</strong> ${row.deviation ? row.deviation.toFixed(2) + '%' : '0%'}</p>
      <p><strong>异常:</strong> ${row.isAnomaly ? '是' : '否'}</p>
      <p><strong>处理状态:</strong> ${row.isAnomaly ? (row.resolved ? '已处理' : '未处理') : '正常'}</p>
      <p><strong>记录时间:</strong> ${formatDate(row.recordTime)}</p>
      <p><strong>来源:</strong> ${row.source || '系统监控'}</p>
      <p><strong>描述:</strong> ${row.description || '无'}</p>
    </div>`,
    '数据详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

// 筛选条件变更
const onFilterChange = () => {
  currentPage.value = 1
  fetchTableData()
}

// 重置筛选条件
const resetFilter = () => {
  queryParams.startDate = null
  queryParams.endDate = null
  queryParams.threshold = 20
  queryParams.anomalyOnly = false
  currentPage.value = 1
  fetchTableData()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchTableData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchTableData()
}

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return format(date, 'yyyy-MM-dd HH:mm:ss')
}

const formatDateForBackend = (date) => {
  if (!date) return null
  return format(date, 'yyyy-MM-dd\'T\'HH:mm:ss')
}
</script>

<style scoped>
.data-monitor-container {
  padding: 20px;
}

.monitor-card {
  margin-bottom: 20px;
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

.monitor-content {
  margin-top: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 15px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-card.warning .stat-value {
  color: #e6a23c;
}

.stat-card.success .stat-value {
  color: #67c23a;
}

.stat-card.info .stat-value {
  color: #409eff;
}

.filter-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 