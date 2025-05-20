<template>
  <div class="running-management-container">
    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <span>跑步数据统计</span>
          <el-button type="primary" size="small" @click="refreshStatistics">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </template>
      <div class="statistics-panel">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-title">总跑步记录</div>
              <div class="statistic-value">{{ statistics.totalRecords || 0 }}</div>
              <div class="statistic-subtitle">条记录</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-title">活跃跑步用户</div>
              <div class="statistic-value">{{ statistics.activeUsers || 0 }}</div>
              <div class="statistic-subtitle">人</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-title">本周新增记录</div>
              <div class="statistic-value">{{ statistics.thisWeekRecords || 0 }}</div>
              <div class="statistic-subtitle">条</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-title">总跑步里程</div>
              <div class="statistic-value">{{ statistics.totalDistance?.toFixed(1) || '0.0' }}</div>
              <div class="statistic-subtitle">公里</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="records-card">
      <template #header>
        <div class="card-header">
          <span>用户跑步记录</span>
          <div class="filter-area">
            <el-input
              v-model="filters.username"
              placeholder="用户名搜索"
              clearable
              @clear="handleSearch"
              style="width: 200px; margin-right: 10px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px; margin-right: 10px;"
            />

            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="records"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="distance" label="跑步距离(km)" width="120">
          <template #default="scope">
            {{ scope.row.distance ? scope.row.distance.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="用时(分钟)" width="120" />
        <el-table-column prop="pace" label="配速(分:秒/公里)" width="150" />
        <el-table-column prop="recordDate" label="记录日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-popconfirm
              title="确定要删除这条记录吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" text>
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAllUserRunningRecords, getRunningStatistics, deleteRunningRecord } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Search, Refresh } from '@element-plus/icons-vue'

// 统计数据
const statistics = reactive({
  totalRecords: 0,
  activeUsers: 0,
  thisWeekRecords: 0,
  thisMonthRecords: 0,
  totalDistance: 0,
  latestRecordDate: '',
  daysSinceLatestRecord: 0
})

// 记录列表
const records = ref<any[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 过滤条件
const filters = reactive({
  username: '',
  startDate: '',
  endDate: ''
})

// 日期选择器
const dateRange = ref<[string, string] | null>(null)

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await getRunningStatistics()
    if (response.data.code === 200) {
      Object.assign(statistics, response.data.data)
    } else {
      ElMessage.error(response.data.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据出错:', error)
    ElMessage.error('获取统计数据失败，请稍后再试')
  }
}

// 刷新统计数据
const refreshStatistics = async () => {
  await fetchStatistics()
  ElMessage.success('统计数据已刷新')
}

// 获取跑步记录
const fetchRecords = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value) {
      filters.startDate = dateRange.value[0]
      filters.endDate = dateRange.value[1]
    } else {
      filters.startDate = ''
      filters.endDate = ''
    }
    
    const response = await getAllUserRunningRecords(
      pagination.page,
      pagination.size,
      filters.username,
      filters.startDate,
      filters.endDate
    )
    
    if (response.data.code === 200) {
      const data = response.data.data
      records.value = data.content
      pagination.total = data.total
    } else {
      ElMessage.error(response.data.message || '获取跑步记录失败')
    }
  } catch (error) {
    console.error('获取跑步记录出错:', error)
    ElMessage.error('获取跑步记录失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.page = 1
  fetchRecords()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchRecords()
}

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.page = 1
  fetchRecords()
}

// 处理删除
const handleDelete = async (id: number) => {
  try {
    const response = await deleteRunningRecord(id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      fetchRecords()
      fetchStatistics()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除记录出错:', error)
    ElMessage.error('删除失败，请稍后再试')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchStatistics()
  fetchRecords()
})
</script>

<style scoped>
.running-management-container {
  padding: 20px;
}

.statistics-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-panel {
  padding: 10px 0;
}

.statistic-item {
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  background: #f8f9fb;
  height: 100%;
}

.statistic-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.statistic-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.statistic-subtitle {
  font-size: 12px;
  color: #909399;
}

.filter-area {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 