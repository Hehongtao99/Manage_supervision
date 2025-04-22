<template>
  <div class="teacher-management">
    <div class="page-header">
      <h2>教师管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入教师姓名/用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 教师列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="teacherList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="教师编号" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'active' ? 'success' : 'danger'"
            >
              {{ row.status === 'active' ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewStudents(row)"
            >
              学生管理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 教师学生管理对话框 -->
    <el-dialog
      title="教师学生管理"
      v-model="studentsDialogVisible"
      width="800px"
    >
      <div v-if="selectedTeacher" class="teacher-info">
        <h3>{{ selectedTeacher.realName }} 的学生列表</h3>
        
        <div class="action-bar">
          <el-button type="primary" @click="handleAssignStudents">
            分配学生
          </el-button>
        </div>
        
        <el-table
          v-loading="studentsLoading"
          :data="teacherStudents"
          border
          style="width: 100%; margin-top: 15px;"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userNumber" label="学号" width="120" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="danger"
                link
                @click="handleUnassignStudent(row)"
              >
                取消分配
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="teacherStudents.length === 0" class="empty-data">
          该教师暂无分配的学生
        </div>
      </div>
    </el-dialog>

    <!-- 分配学生对话框 -->
    <el-dialog
      title="分配学生"
      v-model="assignDialogVisible"
      width="800px"
    >
      <div class="assign-students">
        <el-form :inline="true" class="search-form">
          <el-form-item label="搜索">
            <el-input
              v-model="studentSearchKeyword"
              placeholder="请输入学生姓名/学号"
              clearable
              @input="filterUnassignedStudents"
            />
          </el-form-item>
        </el-form>
        
        <el-table
          v-loading="unassignedStudentsLoading"
          :data="filteredUnassignedStudents"
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userNumber" label="学号" width="120" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="120" />
        </el-table>
        
        <div v-if="unassignedStudents.length === 0" class="empty-data">
          没有未分配的学生
        </div>
        
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="confirmAssignStudents"
            :disabled="selectedStudents.length === 0"
            :loading="assignLoading"
          >
            确认分配
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import {
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeachers, getTeacherStudents, assignStudentsToTeacher, unassignStudent } from '../../api/teacher'
import { getUnassignedStudents } from '../../api/student'
import { Student } from '../../api/student'
import type { UserProfile } from '../../types/user'

// 状态
const loading = ref(false)
const studentsLoading = ref(false)
const unassignedStudentsLoading = ref(false)
const assignLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const teacherList = ref<UserProfile[]>([])
const studentsDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const selectedTeacher = ref<UserProfile | null>(null)
const teacherStudents = ref<UserProfile[]>([])
const unassignedStudents = ref<UserProfile[]>([])
const studentSearchKeyword = ref('')
const selectedStudents = ref<UserProfile[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 计算属性
const filteredUnassignedStudents = computed(() => {
  if (!studentSearchKeyword.value) {
    return unassignedStudents.value
  }
  
  const keyword = studentSearchKeyword.value.toLowerCase()
  return unassignedStudents.value.filter(student => 
    student.username.toLowerCase().includes(keyword) || 
    student.realName?.toLowerCase().includes(keyword) || 
    student.userNumber?.toLowerCase().includes(keyword)
  )
})

// 方法
const fetchTeachers = async () => {
  loading.value = true
  try {
    const response = await getTeachers(currentPage.value, pageSize.value, searchForm.keyword)
    teacherList.value = response.content
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTeacherStudents = async (teacherId: number) => {
  studentsLoading.value = true
  try {
    const students = await getTeacherStudents(teacherId)
    teacherStudents.value = students
  } catch (error) {
    ElMessage.error('获取教师学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

const fetchUnassignedStudents = async () => {
  unassignedStudentsLoading.value = true
  try {
    const students = await getUnassignedStudents()
    unassignedStudents.value = students
  } catch (error) {
    ElMessage.error('获取未分配学生列表失败')
  } finally {
    unassignedStudentsLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchTeachers()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTeachers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchTeachers()
}

const handleViewStudents = async (teacher: UserProfile) => {
  selectedTeacher.value = teacher
  studentsDialogVisible.value = true
  await fetchTeacherStudents(teacher.id)
}

const handleAssignStudents = async () => {
  if (!selectedTeacher.value) return
  
  assignDialogVisible.value = true
  selectedStudents.value = []
  await fetchUnassignedStudents()
}

const handleSelectionChange = (selection: UserProfile[]) => {
  selectedStudents.value = selection
}

const filterUnassignedStudents = () => {
  // 过滤由computed属性处理
}

const confirmAssignStudents = async () => {
  if (!selectedTeacher.value || selectedStudents.value.length === 0) return
  
  assignLoading.value = true
  try {
    const studentIds = selectedStudents.value.map(student => student.id)
    const result = await assignStudentsToTeacher(selectedTeacher.value.id, studentIds)
    
    if (result.success) {
      ElMessage.success('分配学生成功')
      assignDialogVisible.value = false
      await fetchTeacherStudents(selectedTeacher.value.id)
    } else {
      ElMessage.error(result.message || '分配学生失败')
    }
  } catch (error) {
    ElMessage.error('分配学生失败')
  } finally {
    assignLoading.value = false
  }
}

const handleUnassignStudent = async (student: UserProfile) => {
  if (!selectedTeacher.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要取消分配学生 ${student.realName || student.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const result = await unassignStudent(selectedTeacher.value.id, student.id)
    
    if (result.success) {
      ElMessage.success('取消分配成功')
      await fetchTeacherStudents(selectedTeacher.value.id)
    } else {
      ElMessage.error(result.message || '取消分配失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('取消分配失败')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchTeachers()
})
</script>

<style scoped>
.teacher-management {
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
  font-size: 20px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.teacher-info h3 {
  margin-top: 0;
  margin-bottom: 20px;
}

.action-bar {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}

.empty-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 