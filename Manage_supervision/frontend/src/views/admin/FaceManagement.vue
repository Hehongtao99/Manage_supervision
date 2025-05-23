<template>
  <div class="face-management">
    <div class="page-header">
      <h1>人脸信息管理</h1>
      <p>管理所有学生的人脸信息</p>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-bar">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="搜索学生姓名/学号"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="faceStatus"
            placeholder="人脸注册状态"
            style="width: 100%"
            @change="handleSearch"
          >
            <el-option label="全部" value="" />
            <el-option label="已注册" value="true" />
            <el-option label="未注册" value="false" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="refreshStudentList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 学生列表表格 -->
    <el-table
      :data="students"
      v-loading="loading"
      border
      style="width: 100%"
    >
      <el-table-column label="学生姓名" prop="realName" sortable />
      <el-table-column label="学号" prop="userNumber" sortable />
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="人脸状态">
        <template #default="scope">
          <el-tag :type="scope.row.hasFaceData ? 'success' : 'warning'">
            {{ scope.row.hasFaceData ? '已注册' : '未注册' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" sortable />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="handleCaptureFace(scope.row)"
            :disabled="scope.row.hasFaceData"
            :loading="processingStudentId === scope.row.id"
          >
            录入人脸
          </el-button>
          <el-button
            v-if="scope.row.hasFaceData"
            type="primary"
            plain
            size="small"
            @click="handleUpdateFace(scope.row)"
            :loading="processingStudentId === scope.row.id"
          >
            更新
          </el-button>
          <el-popconfirm
            v-if="scope.row.hasFaceData"
            title="确定要删除该学生的人脸数据吗?"
            @confirm="handleDeleteFace(scope.row)"
          >
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 人脸采集对话框 -->
    <el-dialog
      v-model="faceModalVisible"
      :title="modalTitle"
      width="600px"
      destroy-on-close
      @open="startCamera"
      @close="stopCamera"
    >
      <div class="face-capture-container">
        <div class="video-container">
          <video ref="videoElement" width="100%" autoplay></video>
          <div class="capture-overlay">
            <div class="face-guide" :class="{ 'face-detected': faceDetected }"></div>
          </div>
          <div class="detection-status">
            {{ detectionStatusText }}
          </div>
        </div>

        <div class="action-buttons">
          <el-button @click="stopCamera" :disabled="savingFace">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import { getStudents, updateUserFaceData, deleteUserFaceData } from '../../api/admin'
import { detectFace } from '../../api/face'

// 状态变量
const students = ref([])
const loading = ref(false)
const searchQuery = ref('')
const faceStatus = ref('')
const videoElement = ref(null)
const faceModalVisible = ref(false)
const currentStudent = ref(null)
const processingFace = ref(false)
const savingFace = ref(false)
const isUpdating = ref(false)
const faceDetected = ref(false)
const detectionStatusText = ref('等待人脸出现...')
const processingStudentId = ref(null)
let videoStream = null
let faceDetectionInterval = null

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 计算属性
const modalTitle = computed(() => {
  if (!currentStudent.value) return '人脸录入'
  return `${isUpdating.value ? '更新' : '录入'}人脸 - ${currentStudent.value.realName}`
})

// 方法
onMounted(() => {
  refreshStudentList()
})

onUnmounted(() => {
  stopFaceDetection()
  stopCamera()
})

// 刷新学生列表
const refreshStudentList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      query: searchQuery.value,
      roleType: 'USER', // 只获取学生角色的用户
      faceStatus: faceStatus.value
    }
    
    const response = await getStudents(params)
    students.value = response.data.content
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  refreshStudentList()
}

// 分页大小变化处理
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  refreshStudentList()
}

// 分页页码变化处理
const handleCurrentChange = (page: number) => {
  pagination.current = page
  refreshStudentList()
}

// 打开人脸捕获对话框
const handleCaptureFace = (student) => {
  currentStudent.value = student
  processingStudentId.value = student.id
  isUpdating.value = false
  faceModalVisible.value = true
}

// 打开更新人脸对话框
const handleUpdateFace = (student) => {
  currentStudent.value = student
  processingStudentId.value = student.id
  isUpdating.value = true
  faceModalVisible.value = true
}

// 删除人脸数据
const handleDeleteFace = async (student) => {
  try {
    const response = await deleteUserFaceData(student.id)
    
    // 更新本地状态并显示成功消息
    ElMessage.success('人脸数据删除成功')
    
    // 立即刷新列表以获取最新状态
    await refreshStudentList()
    
    // 更新本地数据中的人脸状态
    const index = students.value.findIndex(s => s.id === student.id)
    if (index !== -1) {
      students.value[index].hasFaceData = false
    }
  } catch (error) {
    console.error('删除人脸数据失败:', error)
    ElMessage.error(error.response?.data?.message || '删除人脸数据失败')
    
    // 即使出错也尝试刷新列表
    try {
      await refreshStudentList()
    } catch (refreshError) {
      console.error('刷新列表失败:', refreshError)
    }
    
    // 即使失败也尝试更新本地状态
    const index = students.value.findIndex(s => s.id === student.id)
    if (index !== -1) {
      students.value[index].hasFaceData = false
    }
  }
}

// 启动摄像头
const startCamera = async () => {
  try {
    videoStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: "user"
      }
    })
    
    if (videoElement.value) {
      videoElement.value.srcObject = videoStream
      // 开始自动人脸检测和保存
      startFaceDetection()
    }
  } catch (error) {
    console.error('无法访问摄像头:', error)
    ElMessage.error('无法访问摄像头，请检查设备权限')
    faceModalVisible.value = false
    processingStudentId.value = null
  }
}

// 停止摄像头
const stopCamera = () => {
  stopFaceDetection()
  if (videoStream) {
    videoStream.getTracks().forEach(track => track.stop())
    videoStream = null
  }
  processingStudentId.value = null
}

// 开始人脸检测
const startFaceDetection = () => {
  // 清除任何现有的检测间隔
  stopFaceDetection()
  
  detectionStatusText.value = '等待人脸出现...'
  faceDetected.value = false
  
  // 每2秒尝试检测一次人脸
  faceDetectionInterval = setInterval(async () => {
    if (!videoElement.value || !currentStudent.value) return
    
    try {
      // 从视频元素捕获图像
      const canvas = document.createElement('canvas')
      const context = canvas.getContext('2d')
      canvas.width = videoElement.value.videoWidth
      canvas.height = videoElement.value.videoHeight
      context.drawImage(videoElement.value, 0, 0, canvas.width, canvas.height)
      
      // 转换为Base64
      const imageData = canvas.toDataURL('image/jpeg')
      
      // 检测人脸
      if (!processingFace.value) {
        processingFace.value = true
        detectionStatusText.value = '正在检测人脸...'
        
        try {
          // 调用人脸检测API
          const response = await detectFace({ image: imageData })
          
          if (response.data.success) {
            // 找到有效的人脸
            faceDetected.value = true
            detectionStatusText.value = '人脸检测成功！正在保存...'
            
            // 直接保存检测到的人脸
            await saveFaceData(response.data.faceData)
          } else {
            faceDetected.value = false
            detectionStatusText.value = '请将脸部对准摄像头...'
          }
        } catch (error) {
          console.error('人脸检测失败:', error)
          detectionStatusText.value = '检测失败，请调整位置...'
          faceDetected.value = false
        } finally {
          processingFace.value = false
        }
      }
    } catch (error) {
      console.error('捕获图像失败:', error)
    }
  }, 1500) // 每1.5秒检测一次
}

// 停止人脸检测
const stopFaceDetection = () => {
  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval)
    faceDetectionInterval = null
  }
}

// 保存人脸数据
const saveFaceData = async (faceData) => {
  if (!currentStudent.value) return
  
  savingFace.value = true
  stopFaceDetection() // 停止检测，避免重复保存
  
  try {
    await updateUserFaceData(currentStudent.value.id, { faceData })
    
    // 显示成功消息
    ElMessage.success('人脸数据保存成功')
    
    // 关闭对话框，并刷新列表
    faceModalVisible.value = false
    refreshStudentList()
  } catch (error) {
    console.error('保存人脸数据失败:', error)
    ElMessage.error('保存人脸数据失败')
    
    // 失败后重新开始检测
    startFaceDetection()
  } finally {
    savingFace.value = false
  }
}
</script>

<style scoped>
.face-management {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin-bottom: 8px;
  font-size: 24px;
}

.page-header p {
  color: #666;
}

.search-bar {
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.video-container {
  position: relative;
  width: 100%;
  height: 360px;
  overflow: hidden;
  background-color: #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
}

.capture-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.face-guide {
  width: 200px;
  height: 200px;
  border: 2px dashed #409EFF;
  border-radius: 50%;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.face-guide.face-detected {
  border: 3px solid #67C23A;
  box-shadow: 0 0 15px rgba(103, 194, 58, 0.6);
}

.detection-status {
  position: absolute;
  bottom: 20px;
  left: 0;
  width: 100%;
  text-align: center;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style> 