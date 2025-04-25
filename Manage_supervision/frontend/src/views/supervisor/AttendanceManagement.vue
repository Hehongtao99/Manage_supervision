<template>
  <div class="attendance-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>学生考勤管理</h3>
          <div class="filter-container">
            <el-select
              v-model="selectedClassId"
              placeholder="选择班级"
              @change="handleClassChange"
              style="width: 180px; margin-right: 10px;"
            >
              <el-option
                v-for="classInfo in classes"
                :key="classInfo.id"
                :label="classInfo.className"
                :value="classInfo.id"
              />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="fetchClassAttendance"
              style="width: 280px; margin-right: 10px;"
            />
            <el-button type="primary" @click="fetchClassAttendance">查询</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 考勤统计卡片 -->
        <div class="attendance-statistics" v-if="selectedClassId">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="8">
              <el-card shadow="hover" class="statistic-card">
                <h4>班级考勤率</h4>
                <div class="statistic-value">{{ statistics.attendanceRate || '0.00' }}%</div>
                <el-progress 
                  :percentage="parseFloat(statistics.attendanceRate || 0)"
                  :color="getAttendanceRateColor(statistics.attendanceRate || 0)"
                ></el-progress>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-card shadow="hover" class="statistic-card">
                <h4>今日考勤人数</h4>
                <div class="statistic-value">{{ statistics.todayAttended || 0 }} / {{ statistics.totalStudents || 0 }}</div>
                <div class="days-progress">
                  <div 
                    class="days-completed" 
                    :style="{ width: statistics.totalStudents ? (statistics.todayAttended / statistics.totalStudents * 100) + '%' : '0%' }"
                  ></div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-card shadow="hover" class="statistic-card">
                <h4>累计考勤人次</h4>
                <div class="statistic-value">{{ statistics.totalAttendances || 0 }}</div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 班级选择提示 -->
        <div v-if="!selectedClassId && !loading" class="empty-prompt">
          <el-empty description="请选择班级查看考勤记录">
            <el-select
              v-model="selectedClassId"
              placeholder="选择班级"
              @change="handleClassChange"
              style="width: 180px;"
            >
              <el-option
                v-for="classInfo in classes"
                :key="classInfo.id"
                :label="classInfo.className"
                :value="classInfo.id"
              />
            </el-select>
          </el-empty>
        </div>

        <!-- 考勤记录表格 -->
        <div v-if="selectedClassId && records.length > 0" class="attendance-records">
          <h3>考勤详细记录</h3>
          
          <!-- 日期选择器 -->
          <div class="date-selector">
            <el-calendar v-model="selectedDate">
              <template #dateCell="{ data }">
                <div :class="['calendar-day', getDateClass(data.day)]">
                  <span>{{ data.day.split('-')[2] }}</span>
                  <div class="attendance-indicator" v-if="hasAttendanceOnDate(data.day)"></div>
                </div>
              </template>
            </el-calendar>
          </div>
          
          <!-- 考勤记录表格 -->
          <el-table
            :data="filteredRecords"
            style="width: 100%"
            :default-sort="{ prop: 'checkInTime', order: 'descending' }"
          >
            <el-table-column prop="studentName" label="学生姓名" width="120" sortable />
            <el-table-column prop="checkInTime" label="签到时间" width="180" sortable>
              <template #default="scope">
                {{ formatDateTime(scope.row.checkInTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="faceRecognized" label="识别状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.faceRecognized ? 'success' : 'danger'">
                  {{ scope.row.faceRecognized ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recognitionDetails" label="识别详情" />
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredRecords.length"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
        
        <!-- 无考勤记录提示 -->
        <el-empty 
          v-if="selectedClassId && records.length === 0 && !loading" 
          description="暂无考勤记录"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'SupervisorAttendanceManagement',
  setup() {
    // 状态变量
    const classes = ref([])
    const selectedClassId = ref(null)
    const dateRange = ref(null)
    const selectedDate = ref(new Date())
    const records = ref([])
    const statistics = ref({
      attendanceRate: '0.00',
      todayAttended: 0,
      totalStudents: 0,
      totalAttendances: 0
    })
    const loading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    // 初始化
    onMounted(async () => {
      await fetchClasses()
    })
    
    // 获取教师管理的班级列表
    const fetchClasses = async () => {
      try {
        loading.value = true
        const response = await axios.get('/api/supervisor/classes')
        // 检查后端返回的数据格式
        console.log('班级数据：', response.data)
        if (Array.isArray(response.data)) {
          classes.value = response.data
          
          // 如果只有一个班级，自动选中
          if (classes.value.length === 1) {
            selectedClassId.value = classes.value[0].id
            await fetchClassAttendance()
          }
        } else if (response.data.success && Array.isArray(response.data.classes)) {
          classes.value = response.data.classes
          
          // 如果只有一个班级，自动选中
          if (classes.value.length === 1) {
            selectedClassId.value = classes.value[0].id
            await fetchClassAttendance()
          }
        } else {
          ElMessage.warning('获取班级列表失败，返回数据格式不正确')
          console.error('返回数据格式不正确：', response.data)
        }
      } catch (error) {
        ElMessage.error('获取班级信息失败: ' + error.message)
      } finally {
        loading.value = false
      }
    }
    
    // 班级选择变更
    const handleClassChange = async () => {
      await fetchClassAttendance()
    }
    
    // 获取班级考勤记录
    const fetchClassAttendance = async () => {
      if (!selectedClassId.value) return
      
      try {
        loading.value = true
        
        // 构建请求参数
        let url = `/api/attendance/class-records/${selectedClassId.value}`
        let params = {}
        
        if (dateRange.value && dateRange.value.length === 2) {
          params.startDate = dateRange.value[0]
          params.endDate = dateRange.value[1]
        }
        
        const response = await axios.get(url, { params })
        
        if (response.data.success) {
          records.value = response.data.records
          
          // 如果后端返回了统计信息，使用它
          if (response.data.statistics) {
            statistics.value = response.data.statistics
          } else {
            // 否则前端计算简单的统计信息
            calculateStatistics()
          }
        } else {
          ElMessage.warning(response.data.message || '获取考勤记录失败')
        }
      } catch (error) {
        ElMessage.error('获取考勤记录失败: ' + error.message)
      } finally {
        loading.value = false
      }
    }
    
    // 前端计算统计信息
    const calculateStatistics = () => {
      // 获取当天日期的考勤记录
      const today = new Date().toISOString().split('T')[0]
      const todayRecords = records.value.filter(r => r.checkInTime.startsWith(today))
      
      // 设置统计信息
      statistics.value = {
        totalAttendances: records.value.length,
        todayAttended: todayRecords.length,
        totalStudents: classes.value.find(c => c.id === selectedClassId.value)?.studentCount || 0,
        attendanceRate: '0.00'
      }
      
      // 计算考勤率
      if (statistics.value.totalStudents > 0) {
        const rate = (statistics.value.todayAttended / statistics.value.totalStudents) * 100
        statistics.value.attendanceRate = rate.toFixed(2)
      }
    }
    
    // 根据选择的日期筛选记录
    const filteredRecords = computed(() => {
      if (!selectedDate.value) return records.value
      
      const dateStr = selectedDate.value.toISOString().split('T')[0]
      return records.value.filter(r => r.checkInTime.startsWith(dateStr))
    })
    
    // 检查指定日期是否有考勤记录
    const hasAttendanceOnDate = (dateStr) => {
      return records.value.some(r => r.checkInTime.startsWith(dateStr))
    }
    
    // 获取日期单元格的样式类
    const getDateClass = (dateStr) => {
      const today = new Date().toISOString().split('T')[0]
      if (dateStr === today) {
        return 'today'
      }
      if (hasAttendanceOnDate(dateStr)) {
        return 'has-attendance'
      }
      return ''
    }
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
    
    // 获取考勤率颜色
    const getAttendanceRateColor = (rate) => {
      const rateNum = parseFloat(rate)
      if (rateNum >= 90) return '#67C23A'
      if (rateNum >= 75) return '#E6A23C'
      return '#F56C6C'
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
    }
    
    // 监听选中日期变化
    watch(selectedDate, () => {
      currentPage.value = 1
    })
    
    return {
      classes,
      selectedClassId,
      dateRange,
      selectedDate,
      records,
      statistics,
      loading,
      currentPage,
      pageSize,
      filteredRecords,
      handleClassChange,
      fetchClassAttendance,
      hasAttendanceOnDate,
      getDateClass,
      formatDateTime,
      getAttendanceRateColor,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.attendance-management {
  padding: 20px;
  height: 100%;
}

.box-card {
  height: calc(100vh - 80px);
  overflow: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  background-color: #fff;
  z-index: 1;
  padding-bottom: 10px;
}

.filter-container {
  display: flex;
  align-items: center;
}

.attendance-statistics {
  margin-bottom: 30px;
}

.statistic-card {
  height: 150px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-bottom: 20px;
}

.statistic-card h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
}

.statistic-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #409EFF;
}

.days-progress {
  width: 100%;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}

.days-completed {
  height: 100%;
  background-color: #67C23A;
  transition: width 0.5s ease;
}

.attendance-records {
  margin-top: 20px;
  max-height: calc(100vh - 320px);
  overflow: auto;
}

.date-selector {
  margin-bottom: 20px;
  max-height: 400px;
  overflow: auto;
}

.calendar-day {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 6px 0;
}

.calendar-day.today {
  background-color: #ecf5ff;
  border-radius: 6px;
  color: #409EFF;
}

.attendance-indicator {
  position: absolute;
  bottom: 2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #67C23A;
}

.calendar-day.has-attendance {
  font-weight: bold;
}

.empty-prompt {
  margin: 40px 0;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  position: sticky;
  bottom: 0;
  background-color: #fff;
  padding-top: 10px;
  z-index: 1;
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 