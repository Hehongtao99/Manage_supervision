<template>
  <div class="course-list">
    <div class="header">
      <h2>课程列表</h2>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input
            v-model="searchForm.courseName"
            placeholder="请输入课程名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="授课教师">
          <el-input
            v-model="searchForm.teacherName"
            placeholder="请输入教师姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="课程类别">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择课程类别"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCourses">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 课程卡片列表 -->
    <div class="course-cards">
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="course in courseList" :key="course.id" class="course-card-col">
          <el-card class="course-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="course-title">{{ course.courseName }}</span>
                <el-tag :type="course.status === 'active' ? 'success' : 'danger'" size="small">
                  {{ course.status === 'active' ? '活跃' : '停用' }}
                </el-tag>
              </div>
            </template>
            
            <div class="course-content">
              <p class="course-description">{{ course.courseDescription }}</p>
              
              <div class="course-info">
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span>授课教师：{{ course.teacherName }}</span>
                </div>
                
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>课程时长：{{ course.courseDuration }}小时</span>
                </div>
                
                <div class="info-item">
                  <el-icon><Collection /></el-icon>
                  <span>课程类别：{{ course.courseCategory }}</span>
                </div>
                
                <div class="info-item">
                  <el-icon><Calendar /></el-icon>
                  <span>创建时间：{{ formatDate(course.createTime) }}</span>
                </div>
              </div>
            </div>
            
            <template #footer>
              <div class="card-footer">
                <el-button type="primary" @click="viewCourseDetail(course)">
                  查看详情
                </el-button>
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <div v-if="!loading && courseList.length === 0" class="empty-state">
        <el-empty description="暂无课程数据" />
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="courseList.length > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[9, 18, 36]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 课程详情对话框 -->
    <el-dialog
      title="课程详情"
      v-model="detailDialogVisible"
      width="600px"
    >
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">
            {{ selectedCourse.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="课程简介">
            {{ selectedCourse.courseDescription }}
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">
            {{ selectedCourse.teacherName }}
          </el-descriptions-item>
          <el-descriptions-item label="课程时长">
            {{ selectedCourse.courseDuration }}小时
          </el-descriptions-item>
          <el-descriptions-item label="课程类别">
            {{ selectedCourse.courseCategory }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedCourse.status === 'active' ? 'success' : 'danger'">
              {{ selectedCourse.status === 'active' ? '活跃' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(selectedCourse.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(selectedCourse.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Clock, Collection, Calendar } from '@element-plus/icons-vue'
import {
  getCourseList,
  getAllCategories,
  type Course,
  type PageResponse
} from '../../api/course'
import { format } from 'date-fns'

// 响应式数据
const loading = ref(false)
const courseList = ref<Course[]>([])
const categories = ref<string[]>([])
const detailDialogVisible = ref(false)
const selectedCourse = ref<Course | null>(null)

// 搜索表单
const searchForm = reactive({
  courseName: '',
  teacherName: '',
  category: '',
  status: 'active' // 学生只能看到活跃的课程
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 9, // 每页显示9个卡片（3x3布局）
  total: 0
})

// 方法
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const response: PageResponse<Course> = await getCourseList(params)
    courseList.value = response.content
    pagination.total = response.total
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    ElMessage.error('获取课程类别失败')
  }
}

const searchCourses = () => {
  pagination.page = 1
  loadCourses()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    courseName: '',
    teacherName: '',
    category: '',
    status: 'active'
  })
  searchCourses()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadCourses()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadCourses()
}

const viewCourseDetail = (course: Course) => {
  selectedCourse.value = course
  detailDialogVisible.value = true
}

const formatDate = (dateTime: string) => {
  if (!dateTime) return ''
  return format(new Date(dateTime), 'yyyy-MM-dd')
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return format(new Date(dateTime), 'yyyy-MM-dd HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadCourses()
  loadCategories()
})
</script>

<style scoped>
.course-list {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.search-section {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.course-cards {
  min-height: 400px;
}

.course-card-col {
  margin-bottom: 20px;
}

.course-card {
  height: 100%;
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-title {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}

.course-content {
  padding: 10px 0;
}

.course-description {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.info-item .el-icon {
  color: #409eff;
}

.card-footer {
  display: flex;
  justify-content: center;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.course-detail {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 