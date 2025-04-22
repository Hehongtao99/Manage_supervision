<template>
  <div class="student-management">
    <div class="page-header">
      <h2>学生管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入学生姓名/用户名/学号"
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

    <!-- 学生列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="studentList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="学号" width="120" />
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
        <el-table-column label="分配状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.assignStatus ? 'success' : 'info'"
            >
              {{ row.assignStatus ? '已分配' : '未分配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleShowTeacher(row)"
              v-if="row.assignStatus"
            >
              查看教师
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

    <!-- 学生教师信息对话框 -->
    <el-dialog
      title="分配教师信息"
      v-model="teacherDialogVisible"
      width="500px"
    >
      <div v-if="selectedStudent && studentTeachers.length > 0" class="teacher-info">
        <h3>{{ selectedStudent.realName || selectedStudent.username }} 的教师</h3>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="教师姓名">
            {{ studentTeachers[0].realName || studentTeachers[0].username }}
          </el-descriptions-item>
          <el-descriptions-item label="教师编号">
            {{ studentTeachers[0].userNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱">
            {{ studentTeachers[0].email }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ studentTeachers[0].phone }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="dialog-footer">
          <el-button @click="teacherDialogVisible = false">关闭</el-button>
          <el-button
            type="danger"
            @click="handleUnassignStudent"
          >
            取消分配
          </el-button>
        </div>
      </div>
      
      <div v-else class="empty-data">
        该学生暂未分配教师
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watchEffect } from 'vue'
import {
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudents } from '../../api/student'
import { unassignStudent } from '../../api/teacher'
import type { UserProfile } from '../../types/user'
import axios from '../../utils/axios'

// 状态
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const studentList = ref<(UserProfile & { assignStatus?: boolean })[]>([])
const teacherDialogVisible = ref(false)
const selectedStudent = ref<UserProfile | null>(null)
const studentTeachers = ref<UserProfile[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 方法
const fetchStudents = async () => {
  loading.value = true
  try {
    const response = await getStudents(currentPage.value, pageSize.value, searchForm.keyword)
    const students = response.content
    
    // 获取学生分配状态
    await Promise.all(students.map(async (student: any) => {
      try {
        const teachersResponse = await axios.get(`/api/admin/teachers/student/${student.id}`)
        student.assignStatus = teachersResponse.data.length > 0
      } catch (error) {
        student.assignStatus = false
      }
    }))
    
    studentList.value = students
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchStudents()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchStudents()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchStudents()
}

const handleShowTeacher = async (student: UserProfile) => {
  selectedStudent.value = student
  teacherDialogVisible.value = true
  
  try {
    const response = await axios.get(`/api/admin/teachers/student/${student.id}`)
    studentTeachers.value = response.data
  } catch (error) {
    ElMessage.error('获取教师信息失败')
    studentTeachers.value = []
  }
}

const handleUnassignStudent = async () => {
  if (!selectedStudent.value || studentTeachers.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要取消 ${selectedStudent.value.realName || selectedStudent.value.username} 与教师的分配关系吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const result = await unassignStudent(studentTeachers.value[0].id, selectedStudent.value.id)
    
    if (result.success) {
      ElMessage.success('取消分配成功')
      teacherDialogVisible.value = false
      fetchStudents() // 刷新列表
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
  fetchStudents()
})
</script>

<style scoped>
.student-management {
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
  gap: 10px;
}
</style> 