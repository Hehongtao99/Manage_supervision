<template>
  <div class="feedback-management-container">
    <el-card class="feedback-card">
      <template #header>
        <div class="card-header">
          <h2>问题反馈管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="refreshData">
              刷新数据
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="feedback-content">
        <!-- 数据统计卡片 -->
        <div class="stat-cards">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ stats.total || 0 }}</div>
                <div class="stat-label">总反馈数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card warning">
                <div class="stat-value">{{ stats.pending || 0 }}</div>
                <div class="stat-label">待处理</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card info">
                <div class="stat-value">{{ stats.processing || 0 }}</div>
                <div class="stat-label">处理中</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card success">
                <div class="stat-value">{{ stats.resolved || 0 }}</div>
                <div class="stat-label">已解决</div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 筛选表单 -->
        <div class="filter-form">
          <el-form :model="queryParams" inline>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
                <el-option label="待处理" :value="0" />
                <el-option label="处理中" :value="1" />
                <el-option label="已解决" :value="2" />
                <el-option label="已关闭" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="严重程度">
              <el-select v-model="queryParams.severity" placeholder="全部级别" clearable>
                <el-option label="低" :value="1" />
                <el-option label="中" :value="2" />
                <el-option label="高" :value="3" />
              </el-select>
            </el-form-item>
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
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keyword" placeholder="标题/描述关键词" />
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
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" width="220" show-overflow-tooltip />
          <el-table-column prop="reporterName" label="上报人" width="120" />
          <el-table-column label="严重程度" width="100">
            <template #default="scope">
              <el-tag :type="getSeverityType(scope.row.severity)">
                {{ getSeverityLabel(scope.row.severity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reportTime" label="上报时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.reportTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="handlerName" label="处理人" width="120">
            <template #default="scope">
              {{ scope.row.handlerName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="showDetails(scope.row)"
              >
                详情
              </el-button>
              <el-button
                v-if="scope.row.status < 2"
                type="success"
                size="small"
                @click="handleFeedback(scope.row)"
              >
                处理
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

    <!-- 处理反馈对话框 -->
    <el-dialog
      v-model="handleDialogVisible"
      title="处理反馈"
      width="600px"
    >
      <el-form :model="handleForm" label-width="120px">
        <el-form-item label="反馈标题">
          <span>{{ currentReport?.title }}</span>
        </el-form-item>
        <el-form-item label="反馈描述">
          <div class="feedback-description">{{ currentReport?.description || '无详细描述' }}</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="handleForm.status">
            <el-option :value="0" label="待处理" />
            <el-option :value="1" label="处理中" />
            <el-option :value="2" label="已解决" />
            <el-option :value="3" label="已关闭" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input
            v-model="handleForm.resolution"
            type="textarea"
            rows="4"
            placeholder="请输入处理结果或备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHandleResult">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="反馈详情"
      width="700px"
    >
      <div v-if="currentReport" class="report-details">
        <div class="report-details-item">
          <div class="details-label">ID:</div>
          <div class="details-value">{{ currentReport.id }}</div>
        </div>
        <div class="report-details-item">
          <div class="details-label">标题:</div>
          <div class="details-value">{{ currentReport.title }}</div>
        </div>
        <div class="report-details-item">
          <div class="details-label">上报人:</div>
          <div class="details-value">{{ currentReport.reporterName }}</div>
        </div>
        <div class="report-details-item">
          <div class="details-label">严重程度:</div>
          <div class="details-value">
            <el-tag :type="getSeverityType(currentReport.severity)">
              {{ getSeverityLabel(currentReport.severity) }}
            </el-tag>
          </div>
        </div>
        <div class="report-details-item">
          <div class="details-label">状态:</div>
          <div class="details-value">
            <el-tag :type="getStatusType(currentReport.status)">
              {{ getStatusLabel(currentReport.status) }}
            </el-tag>
          </div>
        </div>
        <div class="report-details-item">
          <div class="details-label">上报时间:</div>
          <div class="details-value">{{ formatDate(currentReport.reportTime) }}</div>
        </div>
        <div class="report-details-item">
          <div class="details-label">处理人:</div>
          <div class="details-value">{{ currentReport.handlerName || '-' }}</div>
        </div>
        <div class="report-details-item">
          <div class="details-label">更新时间:</div>
          <div class="details-value">{{ currentReport.updatedAt ? formatDate(currentReport.updatedAt) : '-' }}</div>
        </div>
        <div class="report-details-item full-width">
          <div class="details-label">描述:</div>
          <div class="details-value description">{{ currentReport.description || '无详细描述' }}</div>
        </div>
        <div class="report-details-item full-width" v-if="currentReport.resolution">
          <div class="details-label">处理结果:</div>
          <div class="details-value resolution">{{ currentReport.resolution }}</div>
        </div>
        
        <!-- 如果有关联的监控数据，显示监控数据信息 -->
        <div v-if="currentReport.monitorData" class="related-data">
          <h3>关联的监控数据</h3>
          <div class="report-details-item">
            <div class="details-label">数据类型:</div>
            <div class="details-value">{{ currentReport.monitorData.dataType }}</div>
          </div>
          <div class="report-details-item">
            <div class="details-label">数值:</div>
            <div class="details-value">{{ currentReport.monitorData.value }}</div>
          </div>
          <div class="report-details-item">
            <div class="details-label">预期范围:</div>
            <div class="details-value">{{ currentReport.monitorData.expectedMin }} - {{ currentReport.monitorData.expectedMax }}</div>
          </div>
          <div class="report-details-item">
            <div class="details-label">偏差率:</div>
            <div class="details-value">{{ currentReport.monitorData.deviation ? currentReport.monitorData.deviation.toFixed(2) + '%' : '0%' }}</div>
          </div>
          <div class="report-details-item">
            <div class="details-label">记录时间:</div>
            <div class="details-value">{{ formatDate(currentReport.monitorData.recordTime) }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getDataReports, 
  getDataReportDetail, 
  handleDataReport, 
  getReportStats 
} from '../../api/dataReport'
import { format } from 'date-fns'

// 状态变量
const loading = ref(false)
const tableData = ref([])
const stats = reactive({
  total: 0,
  pending: 0,
  processing: 0,
  resolved: 0,
  closed: 0
})

// 分页变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

// 查询参数
const queryParams = reactive({
  status: null,
  severity: null,
  startDate: null,
  endDate: null,
  keyword: null
})

// 对话框控制
const handleDialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const currentReport = ref(null)

// 处理表单
const handleForm = reactive({
  status: 0,
  resolution: ''
})

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已解决',
    3: '已关闭'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'success',
    3: 'info'
  }
  return typeMap[status] || ''
}

// 获取严重程度标签
const getSeverityLabel = (severity) => {
  const severityMap = {
    1: '低',
    2: '中',
    3: '高'
  }
  return severityMap[severity] || '未知'
}

// 获取严重程度类型
const getSeverityType = (severity) => {
  const typeMap = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[severity] || ''
}

// 获取报告统计
const fetchStats = async () => {
  try {
    const res = await getReportStats()
    const statsData = res.data
    
    stats.pending = statsData[0] || 0
    stats.processing = statsData[1] || 0
    stats.resolved = statsData[2] || 0
    stats.closed = statsData[3] || 0
    stats.total = stats.pending + stats.processing + stats.resolved + stats.closed
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
    
    const res = await getDataReports(params)
    tableData.value = res.data.records
    totalItems.value = res.data.totalItems
  } catch (error) {
    console.error('获取反馈数据失败:', error)
    ElMessage.error('获取反馈数据失败')
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

// 筛选条件变更
const onFilterChange = () => {
  currentPage.value = 1
  fetchTableData()
}

// 重置筛选条件
const resetFilter = () => {
  Object.keys(queryParams).forEach(key => {
    queryParams[key] = null
  })
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

// 显示详情
const showDetails = async (row) => {
  try {
    const res = await getDataReportDetail(row.id)
    currentReport.value = res.data
    detailsDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 处理反馈
const handleFeedback = async (row) => {
  try {
    const res = await getDataReportDetail(row.id)
    currentReport.value = res.data
    
    // 初始化处理表单
    handleForm.status = currentReport.value.status
    handleForm.resolution = currentReport.value.resolution || ''
    
    handleDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 提交处理结果
const submitHandleResult = async () => {
  if (handleForm.status >= 2 && !handleForm.resolution) {
    ElMessage.warning('请输入处理结果')
    return
  }
  
  try {
    await handleDataReport(
      currentReport.value.id,
      handleForm.status,
      handleForm.resolution
    )
    
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    
    // 刷新数据
    fetchStats()
    fetchTableData()
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败')
  }
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
.feedback-management-container {
  padding: 20px;
}

.feedback-card {
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

.feedback-content {
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

.feedback-description {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  min-height: 60px;
  white-space: pre-wrap;
}

.report-details {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.report-details-item {
  display: flex;
  width: 45%;
  margin-bottom: 10px;
}

.report-details-item.full-width {
  width: 100%;
  flex-direction: column;
}

.details-label {
  font-weight: bold;
  width: 100px;
}

.details-value {
  flex: 1;
}

.details-value.description,
.details-value.resolution {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-top: 5px;
  white-space: pre-wrap;
}

.related-data {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 15px;
  width: 100%;
}

.related-data h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style> 