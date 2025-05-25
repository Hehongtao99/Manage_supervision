<template>
  <div class="course-container">
    <div class="header">
      <h2>课程管理</h2>
      <div class="actions">
        <el-button type="primary" @click="handleCreateCourse" v-if="hasPermission('course:add')">创建课程</el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.courseName"
        placeholder="课程名称"
        clearable
        @clear="handleSearch"
        class="search-input"
      />
      <el-select
        v-model="searchForm.category"
        placeholder="课程类别"
        clearable
        @change="handleSearch"
        class="search-select"
      >
        <el-option label="全部类别" value="" />
        <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
      </el-select>
      <el-select
        v-model="searchForm.status"
        placeholder="状态"
        clearable
        @change="handleSearch"
        class="search-select"
      >
        <el-option label="全部状态" value="" />
        <el-option label="活跃" value="active" />
        <el-option label="未激活" value="inactive" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 课程列表 -->
    <el-table
      v-loading="loading"
      :data="courses"
      border
      stripe
      class="course-table"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="courseName" label="课程名称" min-width="150" />
      <el-table-column prop="teacherName" label="授课教师" width="120" />
      <el-table-column prop="duration" label="时长(分钟)" width="120" />
      <el-table-column prop="category" label="课程类别" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
            {{ scope.row.status === 'active' ? '活跃' : '未激活' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleViewCourse(scope.row)">查看</el-button>
          <el-button 
            type="warning" 
            size="small" 
            @click="handleEditCourse(scope.row)"
            v-if="hasPermission('course:edit')"
          >编辑</el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDeleteCourse(scope.row)"
            v-if="hasPermission('course:delete')"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除课程 "{{ courseToDelete?.courseName }}" 吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCourses, deleteCourse } from '../../api/course'
import type { Course, CourseQuery, PageResponse } from '../../api/course'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 权限检查
const hasPermission = (permission: string): boolean => {
  return userStore.hasPermission(permission)
}

// 课程数据
const courses = ref<Course[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const courseToDelete = ref<Course | null>(null)
const deleteDialogVisible = ref(false)

// 类别列表（实际项目中可能需要从API获取）
const categories = ref([
  '计算机科学',
  '数学',
  '物理',
  '化学',
  '生物',
  '经济学',
  '历史',
  '文学',
  '艺术'
])

// 搜索表单
const searchForm = reactive<{
  courseName: string;
  category: string;
  status: string;
}>({
  courseName: '',
  category: '',
  status: ''
})

// 加载课程数据
const loadCourses = async () => {
  loading.value = true
  try {
    const query: CourseQuery = {
      page: currentPage.value,
      size: pageSize.value,
      courseName: searchForm.courseName || undefined,
      category: searchForm.category || undefined,
      status: searchForm.status || undefined
    }
    
    const response = await getCourses(query)
    courses.value = response.content
    total.value = response.total
  } catch (error) {
    console.error('加载课程失败:', error)
    ElMessage.error('加载课程失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadCourses()
}

// 重置搜索
const resetSearch = () => {
  searchForm.courseName = ''
  searchForm.category = ''
  searchForm.status = ''
  currentPage.value = 1
  loadCourses()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadCourses()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadCourses()
}

// 操作处理
const handleViewCourse = (course: Course) => {
  router.push(`/course/${course.id}`)
}

const handleCreateCourse = () => {
  router.push('/course/create')
}

const handleEditCourse = (course: Course) => {
  router.push(`/course/${course.id}/edit`)
}

const handleDeleteCourse = (course: Course) => {
  courseToDelete.value = course
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!courseToDelete.value?.id) return
  
  try {
    await deleteCourse(courseToDelete.value.id)
    ElMessage.success('课程删除成功')
    deleteDialogVisible.value = false
    loadCourses()
  } catch (error) {
    console.error('删除课程失败:', error)
    ElMessage.error('删除课程失败，请稍后重试')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.course-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 150px;
}

.course-table {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 