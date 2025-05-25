<template>
  <div class="course-detail-container">
    <div class="header">
      <h2>课程详情</h2>
      <div class="actions">
        <el-button @click="goBack">返回</el-button>
        <el-button 
          type="primary" 
          @click="handleEdit" 
          v-if="hasPermission('course:edit')"
        >编辑</el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="course-card">
      <template #header>
        <div class="card-header">
          <span>{{ course.courseName }}</span>
          <el-tag :type="course.status === 'active' ? 'success' : 'info'" class="status-tag">
            {{ course.status === 'active' ? '活跃' : '未激活' }}
          </el-tag>
        </div>
      </template>
      
      <div class="course-info">
        <div class="info-item">
          <span class="label">课程ID:</span>
          <span>{{ course.id }}</span>
        </div>
        <div class="info-item">
          <span class="label">授课教师:</span>
          <span>{{ course.teacherName || '暂无' }}</span>
        </div>
        <div class="info-item">
          <span class="label">课程时长:</span>
          <span>{{ course.duration }} 分钟</span>
        </div>
        <div class="info-item">
          <span class="label">课程类别:</span>
          <span>{{ course.category || '未分类' }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间:</span>
          <span>{{ course.createTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">最后更新:</span>
          <span>{{ course.updateTime }}</span>
        </div>
        <div class="info-item full-width">
          <span class="label">课程封面:</span>
          <div class="cover-image">
            <img v-if="course.coverImage" :src="course.coverImage" alt="课程封面" />
            <el-empty v-else description="暂无封面图片" />
          </div>
        </div>
        <div class="info-item full-width">
          <span class="label">课程简介:</span>
          <div class="description">
            {{ course.description || '暂无简介' }}
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseById } from '../../api/course'
import type { Course } from '../../api/course'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 权限检查
const hasPermission = (permission: string): boolean => {
  return userStore.hasPermission(permission)
}

const loading = ref(false)
const course = ref<Course>({
  courseName: '',
  description: '',
  teacherId: 0,
  teacherName: '',
  duration: 0,
  category: '',
  status: 'active',
  createTime: '',
  updateTime: ''
})

const courseId = computed(() => {
  return Number(route.params.id)
})

// 加载课程详情
const loadCourseDetail = async () => {
  if (!courseId.value) {
    ElMessage.error('课程ID无效')
    router.push('/course')
    return
  }

  loading.value = true
  try {
    const data = await getCourseById(courseId.value)
    course.value = data
  } catch (error) {
    console.error('加载课程详情失败:', error)
    ElMessage.error('加载课程详情失败，请稍后重试')
    router.push('/course')
  } finally {
    loading.value = false
  }
}

// 返回列表页
const goBack = () => {
  router.back()
}

// 跳转到编辑页面
const handleEdit = () => {
  router.push(`/course/${courseId.value}/edit`)
}

onMounted(() => {
  loadCourseDetail()
})
</script>

<style scoped>
.course-detail-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.course-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.status-tag {
  margin-left: 10px;
}

.course-info {
  display: flex;
  flex-wrap: wrap;
  margin-top: 20px;
}

.info-item {
  width: 50%;
  margin-bottom: 15px;
  display: flex;
}

.full-width {
  width: 100%;
  flex-direction: column;
}

.label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 100px;
}

.cover-image {
  margin-top: 10px;
  max-width: 100%;
}

.cover-image img {
  max-width: 300px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.description {
  margin-top: 10px;
  white-space: pre-line;
  line-height: 1.5;
}
</style> 