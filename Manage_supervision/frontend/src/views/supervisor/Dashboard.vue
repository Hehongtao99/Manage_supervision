<template>
  <div class="dashboard-container">
    <h1 class="page-title">教师控制台</h1>
    
    <el-row :gutter="20">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">
            <el-icon :size="32">
              <component :is="stat.icon"></component>
            </el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="dashboard-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>班级列表</h3>
            </div>
          </template>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="teacherClasses && teacherClasses.length > 0">
            <el-table :data="teacherClasses" style="width: 100%">
              <el-table-column prop="className" label="班级名称" />
              <el-table-column prop="grade" label="年级" />
              <el-table-column prop="studentCount" label="学生人数" width="120" />
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="viewClassStudents(scope.row)">
                    查看学生
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="empty-data">
            <el-empty description="暂未分配班级" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="dashboard-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>学生列表</h3>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索学生"
                style="width: 300px"
                clearable
                @input="filterStudents"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="filteredStudents && filteredStudents.length > 0">
            <el-table :data="filteredStudents" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="userNumber" label="学号" width="120" />
              <el-table-column prop="realName" label="姓名" width="120" />
              <el-table-column prop="className" label="所在班级" width="150" />
              <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
              <el-table-column prop="phone" label="电话" width="120" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
                    {{ scope.row.status === 'active' ? '活跃' : '非活跃' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="viewStudentDetails(scope.row)" plain>
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="empty-data">
            <el-empty description="暂无学生数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="studentDetailsVisible"
      :title="selectedStudent ? `${selectedStudent.realName || selectedStudent.username} 的详细信息` : '学生详情'"
      width="50%"
    >
      <div v-if="selectedStudent" class="student-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生ID">{{ selectedStudent.id }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ selectedStudent.userNumber }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedStudent.realName || selectedStudent.username }}</el-descriptions-item>
          <el-descriptions-item label="所在班级">{{ selectedStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱" :span="2">{{ selectedStudent.email }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedStudent.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedStudent.status === 'active' ? 'success' : 'info'">
              {{ selectedStudent.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { 
  User, 
  School,
  Document, 
  Bell, 
  Histogram, 
  Search, 
  List
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSupervisorDashboardStats } from '@/api/dashboard'
import { getTeacherClasses } from '@/api/supervisor'
import { getCurrentTeacherStudents } from '@/api/teacher'
import type { SupervisorDashboardDTO } from '@/types/dashboard'
import type { ClassInfo } from '@/types/class'
import type { UserProfile } from '@/types/user'

const loading = ref(true)
const dashboardData = ref<SupervisorDashboardDTO | null>(null)
const teacherClasses = ref<ClassInfo[]>([])
const teacherStudents = ref<UserProfile[]>([])
const filteredStudents = ref<UserProfile[]>([])
const searchKeyword = ref('')

// 学生详情对话框
const studentDetailsVisible = ref(false)
const selectedStudent = ref<UserProfile | null>(null)

// 统计数据
const stats = computed(() => {
  return [
    {
      label: '学生总数',
      value: teacherStudents.value.length || 0,
      icon: User
    },
    {
      label: '班级数量',
      value: teacherClasses.value.length || 0,
      icon: School
    }
  ]
})

// 过滤学生
const filterStudents = () => {
  if (!searchKeyword.value) {
    filteredStudents.value = teacherStudents.value
    return
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  filteredStudents.value = teacherStudents.value.filter(student => 
    (student.realName && student.realName.toLowerCase().includes(keyword)) ||
    (student.username && student.username.toLowerCase().includes(keyword)) ||
    (student.userNumber && student.userNumber.toLowerCase().includes(keyword))
  )
}

// 加载数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 先加载教师班级
    const classesData = await getTeacherClasses()
    teacherClasses.value = classesData
    
    // 再加载教师学生
    const studentsData = await getCurrentTeacherStudents()
    teacherStudents.value = studentsData.map(student => {
      // 查找学生所在班级
      const studentClass = teacherClasses.value.find(cls => 
        cls.students && cls.students.some(s => s.id === student.id)
      )
      
      return {
        ...student,
        className: studentClass ? studentClass.className : '未分配班级'
      }
    })
    
    // 初始化过滤后的学生列表
    filteredStudents.value = teacherStudents.value
    
    // 最后加载仪表盘统计数据，这样不会影响到我们自己计算的学生总数
    const data = await getSupervisorDashboardStats()
    dashboardData.value = data
  } catch (error) {
    console.error('加载控制台数据失败:', error)
    ElMessage.error('加载控制台数据失败')
  } finally {
    loading.value = false
  }
}

// 查看班级学生
const viewClassStudents = (classInfo: ClassInfo) => {
  searchKeyword.value = classInfo.className
  filterStudents()
}

// 查看学生详情
const viewStudentDetails = (student: UserProfile) => {
  selectedStudent.value = student
  studentDetailsVisible.value = true
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background-color: #f0f9ff;
  color: #409eff;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.dashboard-section {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.loading-container {
  margin: 20px 0;
}

.empty-data {
  padding: 30px 0;
}

.student-details {
  padding: 10px;
}
</style> 