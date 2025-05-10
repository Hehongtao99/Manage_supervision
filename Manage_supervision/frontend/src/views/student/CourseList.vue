<template>
  <div class="course-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程浏览</span>
          <div class="filter-container">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程名称或教师"
              clearable
              @input="handleSearch"
              class="search-input"
              prefix-icon="Search"
            />
            <el-select v-model="subjectFilter" placeholder="课程科目" clearable @change="handleSearch">
              <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
            </el-select>
            <el-select v-model="priceFilter" placeholder="价格区间" clearable @change="handleSearch">
              <el-option label="全部价格" value="" />
              <el-option label="0-50元/小时" value="0-50" />
              <el-option label="50-100元/小时" value="50-100" />
              <el-option label="100-200元/小时" value="100-200" />
              <el-option label="200元以上/小时" value="200+" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="course-grid" v-loading="loading">
        <el-empty v-if="filteredCourses.length === 0" description="暂无可浏览的课程" />
        
        <div v-else class="courses-wrapper">
          <el-row :gutter="20">
            <el-col 
              v-for="course in filteredCourses" 
              :key="course.id" 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6"
              :xl="6"
            >
              <el-card class="course-card" shadow="hover" @click="showCourseDetail(course)">
                <div class="course-image">
                  <el-image 
                    v-if="course.imagePaths && course.imagePaths.length > 0"
                    :src="course.imagePaths[0]" 
                    fit="cover"
                    @error="handleImageError(course)"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </div>
                <div class="course-info">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <div class="course-teacher">
                    <el-icon><User /></el-icon>
                    <span>{{ course.teacherName }}</span>
                  </div>
                  <div class="course-subject">
                    <el-icon><Collection /></el-icon>
                    <span>{{ course.subject }}</span>
                  </div>
                  <div class="course-price">
                    <span class="price-label">¥{{ course.hourlyPrice }}/小时</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
    
    <!-- 课程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="课程详情"
      width="800px"
      top="5vh"
      destroy-on-close
      class="course-detail-dialog"
    >
      <div v-if="currentCourse" class="course-detail">
        <!-- 轮播图部分 -->
        <div class="course-carousel-container" v-if="currentCourse.imagePaths && currentCourse.imagePaths.length > 0">
          <el-carousel 
            :interval="3000" 
            :autoplay="true"
            height="300px" 
            indicator-position="outside" 
            arrow="always" 
            class="course-carousel"
          >
            <el-carousel-item v-for="(path, index) in currentCourse.imagePaths" :key="index">
              <div class="carousel-item-container">
                <el-image
                  :src="path"
                  fit="cover"
                  class="carousel-image"
                  @error="handleDetailImageError(index)"
                >
                  <template #error>
                    <div class="image-error-placeholder">
                      <el-icon><Picture /></el-icon>
                      <div>图片加载失败</div>
                    </div>
                  </template>
                </el-image>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="no-images-placeholder" v-else>
          <el-icon><Picture /></el-icon>
          <span>暂无课程图片</span>
        </div>

        <div class="course-info-container">
          <h2 class="course-detail-title">{{ currentCourse.title }}</h2>
          
          <div class="course-meta">
            <div class="meta-item">
              <el-icon><Collection /></el-icon>
              <span>科目：{{ currentCourse.subject }}</span>
            </div>
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>教师：{{ currentCourse.teacherName }}</span>
            </div>
            <div class="meta-item price-tag">
              <el-icon><Money /></el-icon>
              <span>每小时价格：</span>
              <span class="highlight-price">¥{{ currentCourse.hourlyPrice }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>工作时间：{{ currentCourse.workTimeStart }}:00 - {{ currentCourse.workTimeEnd }}:00</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>发布时间：{{ currentCourse.createTime }}</span>
            </div>
          </div>
          
          <div class="course-description">
            <h3>课程描述</h3>
            <p>{{ currentCourse.description }}</p>
          </div>

          <div class="actions">
            <el-button type="primary" size="large" @click="startChat(currentCourse.teacherId)">
              <el-icon><ChatDotRound /></el-icon>联系教师
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Picture, User, Collection, ChatDotRound, Money, Clock, Calendar } from '@element-plus/icons-vue'
import { getAllApprovedCourses, CourseDTO, startChatWithTeacher } from '../../api/courses'
import { useChatStore } from '../../stores/chat'
import { useRouter } from 'vue-router'

// 状态变量
const loading = ref(false)
const courses = ref<CourseDTO[]>([])
const searchKeyword = ref('')
const subjectFilter = ref('')
const priceFilter = ref('')
const subjects = ref<string[]>([])
const detailDialogVisible = ref(false)
const currentCourse = ref<CourseDTO | null>(null)
const currentConversation = ref(null)
const chatStore = useChatStore()
const router = useRouter()

// 计算过滤后的课程列表
const filteredCourses = computed(() => {
  let result = [...courses.value]
  
  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(course => 
      course.title.toLowerCase().includes(keyword) ||
      course.teacherName.toLowerCase().includes(keyword) ||
      course.description.toLowerCase().includes(keyword)
    )
  }
  
  // 科目过滤
  if (subjectFilter.value) {
    result = result.filter(course => course.subject === subjectFilter.value)
  }
  
  // 价格过滤
  if (priceFilter.value) {
    const priceRange = priceFilter.value.split('-')
    if (priceRange.length === 2) {
      const minPrice = Number(priceRange[0])
      const maxPrice = Number(priceRange[1])
      result = result.filter(course => 
        course.hourlyPrice >= minPrice && course.hourlyPrice <= maxPrice
      )
    } else if (priceFilter.value.endsWith('+')) {
      const minPrice = Number(priceFilter.value.replace('+', ''))
      result = result.filter(course => course.hourlyPrice >= minPrice)
    }
  }
  
  return result
})

// 初始化
onMounted(async () => {
  await fetchCourses()
})

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const data = await getAllApprovedCourses()
    courses.value = data
    
    // 提取所有科目
    const subjectSet = new Set<string>()
    data.forEach((course: CourseDTO) => {
      if (course.subject) {
        subjectSet.add(course.subject)
      }
    })
    subjects.value = Array.from(subjectSet)
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  // 使用计算属性自动过滤
}

// 显示课程详情
const showCourseDetail = (course: CourseDTO) => {
  currentCourse.value = course
  detailDialogVisible.value = true
}

// 处理图片加载错误
const handleImageError = (course: CourseDTO) => {
  // 当图片加载失败时，设置为默认图片或从数组中移除
  if (course.imagePaths && course.imagePaths.length > 0) {
    // 简单处理：将第一张图片替换为默认图片
    course.imagePaths[0] = '/path/to/default/image.jpg'
  }
}

// 处理详情页图片加载错误
const handleDetailImageError = (index: number) => {
  if (currentCourse.value?.imagePaths) {
    // 可以选择移除图片或替换为默认图片
    currentCourse.value.imagePaths.splice(index, 1)
  }
}

// 开始与老师聊天
const startChat = async (teacherId: number) => {
  if (!teacherId) {
    ElMessage.error('教师ID无效，无法开始聊天')
    return
  }

  try {
    ElMessage({
      message: '正在连接聊天...',
      type: 'info',
      duration: 1500,
      showClose: false
    })

    console.log('开始与教师ID为', teacherId, '聊天')
    const response = await startChatWithTeacher(teacherId)
    console.log('聊天会话创建响应:', response)

    // 修复：response可能直接是会话对象，也可能在data字段中
    const conversationData = response.data || response
    
    if (!conversationData || !conversationData.id) {
      ElMessage.closeAll()
      ElMessage.error('创建聊天会话失败')
      console.error('创建聊天会话失败:', response)
      return
    }

    // 设置当前会话
    currentConversation.value = conversationData
    console.log('当前会话设置为:', currentConversation.value)

    // 检查会话是否已加载到聊天存储
    if (!chatStore.conversations.some(c => c.id === conversationData.id)) {
      // 需要添加会话到聊天存储
      chatStore.conversations.push(conversationData)
    }

    // 关闭加载消息
    ElMessage.closeAll()
    
    // 隐藏课程详情对话框
    detailDialogVisible.value = false
    
    // 重定向到聊天页面，而不是显示聊天对话框
    router.push({ 
      path: '/chat',
      query: { 
        conversationId: conversationData.id.toString()
      } 
    })
  } catch (error) {
    ElMessage.closeAll()
    console.error('聊天初始化错误:', error)
    ElMessage.error('连接聊天失败，请稍后再试')
    
    // 显示详细错误信息在控制台
    if (error.response) {
      console.error('错误响应数据:', error.response.data)
      console.error('错误状态码:', error.response.status)
    }
  }
}
</script>

<style scoped>
.course-list {
  margin: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.filter-container {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-container {
    width: 100%;
    margin-top: 15px;
    flex-wrap: wrap;
  }
  
  .search-input {
    width: 100%;
  }
}

.search-input {
  width: 200px;
}

.courses-wrapper {
  margin-top: 20px;
}

.course-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.15);
}

.course-image {
  height: 180px;
  overflow: hidden;
}

.course-image .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
}

.course-card:hover .course-image .el-image {
  transform: scale(1.05);
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  font-size: 40px;
  color: #c0c4cc;
}

.course-info {
  padding: 16px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.course-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 45px;
}

.course-teacher, .course-subject {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.course-teacher .el-icon, .course-subject .el-icon {
  margin-right: 6px;
  font-size: 16px;
  color: #909399;
}

.course-price {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.price-label {
  color: #ff6700;
  font-weight: bold;
  font-size: 17px;
}

/* 课程详情页样式 */
.course-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.course-detail {
  padding: 20px;
}

.course-carousel-container {
  margin-bottom: 30px;
}

.course-carousel {
  width: 100%;
}

.carousel-item-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 12px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.course-carousel:hover .carousel-image {
  transform: scale(1.02);
}

.image-error-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.image-error-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.no-images-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 8px;
  margin-bottom: 30px;
}

.no-images-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.course-info-container {
  padding: 0 10px;
}

.course-detail-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
  text-align: center;
  position: relative;
}

.course-detail-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #1989fa, #409EFF);
  border-radius: 3px;
}

.course-meta {
  margin-bottom: 25px;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.meta-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.meta-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #409EFF;
}

.price-tag .highlight-price {
  color: #ff6700;
  font-weight: bold;
  font-size: 18px;
  margin-left: 4px;
}

.course-description {
  margin-bottom: 30px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.course-description h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.course-description p {
  line-height: 1.8;
  color: #606266;
  white-space: pre-line;
}

.actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.actions .el-button {
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 25px;
  transition: all 0.3s;
}

.actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(64, 158, 255, 0.3);
}

/* 聊天窗口样式 */
.chat-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: 500px;
}

.chat-error-container {
  padding: 20px;
  text-align: center;
  border-radius: 8px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  margin: 20px 0;
}

.chat-error-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.chat-loading {
  padding: 40px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 20px;
}

.chat-loading .is-loading {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 16px;
}

.chat-dialog {
  width: 700px;
  max-width: 90vw !important;
}

@media (max-width: 768px) {
  .chat-dialog {
    width: 90vw !important;
  }
}
</style> 