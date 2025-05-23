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
      width="700px"
      destroy-on-close
      @open="startCamera"
      @close="stopCamera"
    >
      <div class="face-capture-container">
        <div class="video-container">
          <video ref="videoElement" autoplay muted playsinline></video>
          <canvas ref="canvasElement" style="display: none;"></canvas>
          
          <!-- 人脸检测叠加层 -->
          <div v-if="cameraActive" class="detection-overlay">
            <!-- 动态人脸框或静态引导框 -->
            <div 
              v-if="facePosition" 
              class="face-detection-box dynamic"
              :style="{
                left: facePosition.x * 100 + '%',
                top: facePosition.y * 100 + '%',
                width: facePosition.width * 100 + '%',
                height: facePosition.height * 100 + '%'
              }"
            ></div>
            <div 
              v-else 
              class="face-detection-box static"
            ></div>
            
            <!-- 检测进度显示 -->
            <div v-if="faceDetected && detectionCount > 0" class="detection-progress">
              <div class="progress-text">
                {{ autoProcessing ? '正在录入人脸...' : '人脸识别中...' }}
              </div>
              <div class="progress-count">{{ Math.min(detectionCount, requiredDetections) }}/{{ requiredDetections }}</div>
              <el-progress 
                :percentage="Math.min((detectionCount / requiredDetections) * 100, 100)" 
                color="#67C23A"
                :stroke-width="6"
              />
            </div>
            
            <!-- 人脸检测成功图标 -->
            <div v-if="faceDetected" class="face-detected-icon">
              <el-icon class="check-icon"><Check /></el-icon>
            </div>
          </div>
          
          <!-- 状态显示 -->
          <div class="status-display">
            <div v-if="!cameraActive" class="status-item">
              <el-icon class="status-icon warning"><Warning /></el-icon>
              <span>摄像头未启动</span>
            </div>
            <div v-else-if="!faceDetected" class="status-item">
              <el-icon class="status-icon info"><InfoFilled /></el-icon>
              <span>等待检测人脸...</span>
            </div>
            <div v-else-if="detectionCount < requiredDetections && !autoProcessing" class="status-item">
              <el-icon class="status-icon loading"><Loading /></el-icon>
              <span>人脸检测中 ({{ detectionCount }}/{{ requiredDetections }})</span>
            </div>
            <div v-else-if="autoProcessing" class="status-item">
              <el-icon class="status-icon loading"><Loading /></el-icon>
              <span>正在录入人脸...</span>
            </div>
            <div v-else class="status-item">
              <el-icon class="status-icon success"><SuccessFilled /></el-icon>
              <span>检测完成，即将录入</span>
            </div>
          </div>
        </div>

        <div class="instruction-text">
          <p>📋 <strong>录入说明：</strong></p>
          <p>1. 请保持人脸在摄像头中央</p>
          <p>2. 确保光线充足，背景简单</p>
          <p>3. 连续检测5秒后将自动录入</p>
        </div>

        <div class="action-buttons">
          <el-button @click="stopCamera" :disabled="savingFace">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
          <el-button type="primary" @click="manualCapture" :disabled="!faceDetected || savingFace || autoProcessing">
            <el-icon><Camera /></el-icon>
            手动录入
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, Check, Warning, InfoFilled, Loading, SuccessFilled, Close, Camera } from '@element-plus/icons-vue'
import { getStudents, updateUserFaceData, deleteUserFaceData } from '../../api/admin'
import { detectFace } from '../../api/face'
import { debounce } from 'lodash-es'

// 状态变量
const students = ref([])
const loading = ref(false)
const searchQuery = ref('')
const faceStatus = ref('')
const videoElement = ref(null)
const canvasElement = ref(null)
const faceModalVisible = ref(false)
const currentStudent = ref(null)
const processingFace = ref(false)
const savingFace = ref(false)
const isUpdating = ref(false)
const faceDetected = ref(false)
const processingStudentId = ref(null)

// 精细检测相关状态
const cameraActive = ref(false)
const facePosition = ref(null)
const detectionCount = ref(0)
const requiredDetections = 5
const autoProcessing = ref(false)
const faceData = ref(null)

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
      cameraActive.value = true
      
      resetDetectionState()
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
  
  cameraActive.value = false
  resetDetectionState()
  processingStudentId.value = null
}

// 重置检测状态
const resetDetectionState = () => {
  faceDetected.value = false
  detectionCount.value = 0
  autoProcessing.value = false
  processingFace.value = false
  facePosition.value = null
  faceData.value = null
}

// 开始人脸检测
const startFaceDetection = () => {
  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval)
    faceDetectionInterval = null
  }
  
  faceDetectionInterval = setInterval(() => {
    if (cameraActive.value && !processingFace.value && !autoProcessing.value) {
      detectFaceAdvanced()
    }
  }, 200) // 每200ms检测一次，提高精度
}

// 停止人脸检测
const stopFaceDetection = () => {
  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval)
    faceDetectionInterval = null
  }
}

// 精细人脸检测（防抖）
const detectFaceDebounced = debounce(async (imageData) => {
  try {
    const response = await detectFace({ image: imageData })
    
    if (response.data.success) {
      faceData.value = `data:image/jpeg;base64,${response.data.faceData}`
      faceDetected.value = true
      
      // 更新人脸位置（如果API返回位置信息）
      if (response.data.position) {
        const newPosition = {
          x: response.data.position.x,
          y: response.data.position.y,
          width: response.data.position.width,
          height: response.data.position.height
        }
        
        if (facePosition.value) {
          // 平滑过渡
          const smoothingFactor = 0.7
          facePosition.value = {
            x: facePosition.value.x * (1 - smoothingFactor) + newPosition.x * smoothingFactor,
            y: facePosition.value.y * (1 - smoothingFactor) + newPosition.y * smoothingFactor,
            width: facePosition.value.width * (1 - smoothingFactor) + newPosition.width * smoothingFactor,
            height: facePosition.value.height * (1 - smoothingFactor) + newPosition.height * smoothingFactor
          }
        } else {
          facePosition.value = newPosition
        }
      }
      
      detectionCount.value++
      console.log(`连续检测到人脸: ${detectionCount.value}/${requiredDetections}`)
      
      // 达到连续检测阈值，自动录入
      if (detectionCount.value >= requiredDetections && !autoProcessing.value) {
        console.log('达到连续检测阈值，开始自动录入')
        autoProcessing.value = true
        
        setTimeout(() => {
          autoSaveFace()
        }, 1000) // 延迟1秒后开始录入
      }
    } else {
      faceData.value = null
      faceDetected.value = false
      facePosition.value = null
      detectionCount.value = 0 // 重置计数
    }
  } catch (error) {
    console.error('人脸检测失败:', error)
    faceData.value = null
    faceDetected.value = false
    facePosition.value = null
    detectionCount.value = 0
  }
}, 100) // 100ms防抖

// 执行人脸检测
const detectFaceAdvanced = () => {
  if (!videoElement.value || !canvasElement.value) return
  
  const context = canvasElement.value.getContext('2d')
  if (!context) return
  
  canvasElement.value.width = videoElement.value.videoWidth
  canvasElement.value.height = videoElement.value.videoHeight
  
  context.drawImage(videoElement.value, 0, 0, canvasElement.value.width, canvasElement.value.height)
  
  const imageData = canvasElement.value.toDataURL('image/jpeg', 0.8)
  
  detectFaceDebounced(imageData)
}

// 自动保存人脸数据
const autoSaveFace = async () => {
  if (!faceData.value || !currentStudent.value) {
    autoProcessing.value = false
    return
  }
  
  await saveFaceData(faceData.value)
}

// 手动捕获人脸
const manualCapture = async () => {
  if (!faceData.value || !currentStudent.value) {
    ElMessage.warning('请等待人脸检测完成')
    return
  }
  
  await saveFaceData(faceData.value)
}

// 保存人脸数据
const saveFaceData = async (faceDataToSave) => {
  if (!currentStudent.value) return
  
  savingFace.value = true
  stopFaceDetection() // 停止检测，避免重复保存
  
  try {
    await updateUserFaceData(currentStudent.value.id, { faceData: faceDataToSave })
    
    // 显示成功消息
    ElMessage.success(`${currentStudent.value.realName} 的人脸数据${isUpdating.value ? '更新' : '录入'}成功`)
    
    // 关闭对话框，并刷新列表
    faceModalVisible.value = false
    refreshStudentList()
  } catch (error) {
    console.error('保存人脸数据失败:', error)
    ElMessage.error(`人脸数据${isUpdating.value ? '更新' : '录入'}失败: ${error.response?.data?.message || '未知错误'}`)
    
    // 失败后重新开始检测
    startFaceDetection()
  } finally {
    savingFace.value = false
    autoProcessing.value = false
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
  height: 400px;
  overflow: hidden;
  background-color: #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
}

.video-container video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.detection-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.face-detection-box {
  position: absolute;
  border: 3px solid #67C23A;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(103, 194, 58, 0.3);
  transition: all 0.1s ease-out;
}

.face-detection-box.static {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  opacity: 0.5;
  border-style: dashed;
  border-color: #409EFF;
}

.face-detection-box.dynamic {
  opacity: 1;
  border-style: solid;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 20px rgba(103, 194, 58, 0.3);
  }
  50% {
    box-shadow: 0 0 30px rgba(103, 194, 58, 0.6);
  }
  100% {
    box-shadow: 0 0 20px rgba(103, 194, 58, 0.3);
  }
}

.detection-progress {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}

.progress-text {
  font-size: 14px;
  margin-bottom: 8px;
}

.progress-count {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #67C23A;
}

.face-detected-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  background-color: #67C23A;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 15px rgba(103, 194, 58, 0.5);
}

.check-icon {
  font-size: 24px;
}

.status-display {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  display: flex;
  justify-content: center;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 6px;
  font-size: 14px;
  color: white;
}

.status-icon {
  margin-right: 8px;
  font-size: 16px;
}

.status-icon.warning {
  color: #E6A23C;
}

.status-icon.info {
  color: #409EFF;
}

.status-icon.loading {
  color: #909399;
  animation: rotate 2s linear infinite;
}

.status-icon.success {
  color: #67C23A;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.instruction-text {
  background-color: #f0f9ff;
  border-left: 4px solid #409EFF;
  padding: 15px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}

.instruction-text p {
  margin: 0;
  margin-bottom: 8px;
}

.instruction-text p:last-child {
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
</style> 