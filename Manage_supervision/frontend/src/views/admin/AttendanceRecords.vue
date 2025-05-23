<template>
  <div class="attendance-records">
    <div class="page-header">
      <h2>考勤记录管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshRecords">
          <el-icon><Refresh /></el-icon>
          刷新记录
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" inline>
        <el-form-item label="记录类型">
          <el-select v-model="filterForm.recordType" placeholder="请选择记录类型" clearable>
            <el-option label="所有记录" value="all" />
            <el-option label="普通考勤记录" value="normal" />
            <el-option label="公共人脸识别记录" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input 
            v-model="filterForm.studentName" 
            placeholder="请输入学生姓名" 
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-number">{{ stats.totalRecords }}</div>
            <div class="stat-label">总记录数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-number">{{ stats.normalRecords }}</div>
            <div class="stat-label">普通考勤记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-number">{{ stats.specialRecords }}</div>
            <div class="stat-label">公共人脸记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-number">{{ stats.todayRecords }}</div>
            <div class="stat-label">今日记录</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考勤记录表格 -->
    <el-card class="table-card">
      <el-table 
        v-loading="loading" 
        :data="recordsList" 
        border 
        stripe
        style="width: 100%"
        :default-sort="{ prop: 'checkInTime', order: 'descending' }"
      >
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="realName" label="学生姓名" width="120">
          <template #default="{ row }">
            <div class="student-info">
              <div class="name">{{ row.realName || row.username }}</div>
              <div class="number">{{ row.userNumber }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="attendanceTitle" label="考勤类型" width="180">
          <template #default="{ row }">
            <el-tag 
              :type="row.attendanceTitle === '公共人脸识别记录' ? 'warning' : 'primary'"
              effect="light"
            >
              {{ row.attendanceTitle || '未知类型' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="checkInTime" label="签到时间" width="180" sortable>
          <template #default="{ row }">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ row.checkInTime }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)" 
              effect="light"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="faceVerified" label="人脸验证" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.faceVerified ? 'success' : 'info'" 
              effect="light"
            >
              {{ row.faceVerified ? '已验证' : '未验证' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="location" label="位置" min-width="120">
          <template #default="{ row }">
            <span>{{ row.location || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.notes || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              size="small"
              @click="viewRecordDetail(row)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 记录详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="考勤记录详情" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">
          {{ currentRecord?.realName || currentRecord?.username }}
        </el-descriptions-item>
        <el-descriptions-item label="学号">
          {{ currentRecord?.userNumber || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="考勤类型" :span="2">
          <el-tag 
            :type="currentRecord?.attendanceTitle === '公共人脸识别记录' ? 'warning' : 'primary'"
            effect="light"
          >
            {{ currentRecord?.attendanceTitle || '未知类型' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签到时间" :span="2">
          {{ currentRecord?.checkInTime }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag 
            :type="getStatusType(currentRecord?.status)" 
            effect="light"
          >
            {{ currentRecord?.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="人脸验证">
          <el-tag 
            :type="currentRecord?.faceVerified ? 'success' : 'info'" 
            effect="light"
          >
            {{ currentRecord?.faceVerified ? '已验证' : '未验证' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="位置" :span="2">
          {{ currentRecord?.location || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentRecord?.notes || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  Search, 
  Clock, 
  View 
} from '@element-plus/icons-vue'
import * as attendanceApi from '@/api/attendance'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const recordsList = ref([])
const currentRecord = ref(null)

// 筛选表单
const filterForm = reactive({
  recordType: 'all',
  studentName: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 统计信息
const stats = reactive({
  totalRecords: 0,
  normalRecords: 0,
  specialRecords: 0,
  todayRecords: 0
})

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case '正常':
    case '系统记录':
      return 'success'
    case '迟到':
      return 'warning'
    case '缺勤':
      return 'danger'
    default:
      return 'info'
  }
}

// 加载考勤记录
const loadAttendanceRecords = async () => {
  loading.value = true
  try {
    // 构建查询参数
    let startDate = ''
    let endDate = ''
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      startDate = filterForm.dateRange[0] + ' 00:00:00'
      endDate = filterForm.dateRange[1] + ' 23:59:59'
    }
    
    // 使用新的API方法获取所有打卡记录
    // 注意：后端现在会根据单个条件进行查询，所以我们需要优先级处理
    let recordType = filterForm.recordType
    let username = filterForm.studentName
    
    // 如果有具体的记录类型筛选，优先使用记录类型
    if (recordType && recordType !== 'all') {
      username = '' // 清空用户名筛选
    }
    
    const response = await attendanceApi.getAllAttendanceRecordsForAdmin(
      pagination.currentPage,
      pagination.pageSize,
      startDate,
      endDate,
      username,
      recordType
    )
    
    let records = response.content || []
    
    // 如果后端没有进行用户名筛选，前端进行筛选
    if (filterForm.studentName && (!recordType || recordType === 'all')) {
      records = records.filter(record => 
        (record.realName && record.realName.includes(filterForm.studentName)) ||
        (record.username && record.username.includes(filterForm.studentName)) ||
        (record.userNumber && record.userNumber.includes(filterForm.studentName))
      )
    }
    
    // 如果同时有记录类型和用户名筛选，前端进行二次筛选
    if (filterForm.studentName && recordType && recordType !== 'all') {
      records = records.filter(record => 
        (record.realName && record.realName.includes(filterForm.studentName)) ||
        (record.username && record.username.includes(filterForm.studentName)) ||
        (record.userNumber && record.userNumber.includes(filterForm.studentName))
      )
    }
    
    recordsList.value = records
    pagination.total = records.length // 使用筛选后的记录数
    
    // 加载统计信息
    await loadStatistics()
    
  } catch (error) {
    console.error('加载考勤记录失败:', error)
    ElMessage.error('加载考勤记录失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    let startDate = ''
    let endDate = ''
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      startDate = filterForm.dateRange[0] + ' 00:00:00'
      endDate = filterForm.dateRange[1] + ' 23:59:59'
    }
    
    const statsResponse = await attendanceApi.getAttendanceRecordsStatistics(startDate, endDate)
    
    stats.totalRecords = statsResponse.totalRecords || 0
    stats.normalRecords = statsResponse.normalRecords || 0
    stats.specialRecords = statsResponse.specialRecords || 0
    
    // 计算今日记录
    const today = new Date().toISOString().split('T')[0]
    const todayStartDate = today + ' 00:00:00'
    const todayEndDate = today + ' 23:59:59'
    
    try {
      const todayStatsResponse = await attendanceApi.getAttendanceRecordsStatistics(todayStartDate, todayEndDate)
      stats.todayRecords = todayStatsResponse.totalRecords || 0
    } catch (error) {
      console.warn('获取今日统计失败:', error)
      stats.todayRecords = 0
    }
    
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 更新统计信息
const updateStats = (records: any[]) => {
  stats.totalRecords = records.length
  stats.specialRecords = records.filter(r => r.attendanceTitle === '公共人脸识别记录').length
  stats.normalRecords = records.length - stats.specialRecords
  
  // 计算今日记录
  const today = new Date().toISOString().split('T')[0]
  stats.todayRecords = records.filter(r => 
    r.checkInTime && r.checkInTime.startsWith(today)
  ).length
}

// 搜索处理
const handleSearch = () => {
  pagination.currentPage = 1
  loadAttendanceRecords()
}

// 重置筛选
const resetFilter = () => {
  filterForm.recordType = 'all'
  filterForm.studentName = ''
  filterForm.dateRange = []
  pagination.currentPage = 1
  loadAttendanceRecords()
}

// 刷新记录
const refreshRecords = () => {
  loadAttendanceRecords()
  ElMessage.success('记录已刷新')
}

// 分页处理
const handleSizeChange = (newSize: number) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
  loadAttendanceRecords()
}

const handleCurrentChange = (newPage: number) => {
  pagination.currentPage = newPage
  loadAttendanceRecords()
}

// 查看记录详情
const viewRecordDetail = (record: any) => {
  currentRecord.value = record
  detailDialogVisible.value = true
}

// 组件挂载时加载数据
onMounted(() => {
  loadAttendanceRecords()
})
</script>

<style scoped>
.attendance-records {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px 0;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.table-card {
  margin-bottom: 20px;
}

.student-info .name {
  font-weight: 500;
  color: #303133;
}

.student-info .number {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 表格样式优化 */
:deep(.el-table) {
  .el-table__header {
    th {
      background-color: #fafafa;
      color: #606266;
      font-weight: 500;
    }
  }
}

/* 卡片间距 */
.el-card {
  margin-bottom: 20px;
}

/* 标签样式 */
.el-tag {
  margin-right: 8px;
}
</style> 