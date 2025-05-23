<template>
  <div class="attendance-panel-container">
    <div class="panel-header">
      <h1 class="panel-title">实验室考勤系统-学生</h1>
      <div class="switch-mode">
        <el-button type="primary" @click="switchToAdminLogin">
          切换到管理员登录
        </el-button>
      </div>
    </div>

    <div class="panel-content">
      <div class="camera-section">
        <div class="camera-container">
          <div class="camera-label">摄像头</div>
          <div class="camera-view" ref="cameraView">
            <video ref="video" autoplay muted playsinline></video>
            <canvas ref="canvas" style="display: none;"></canvas>
            <div v-if="isCameraActive" class="detection-overlay">
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
              <div v-if="faceDetected && detectionCount > 0" class="detection-progress">
                <div class="progress-text">
                  {{ autoRecognizing ? '正在验证打卡...' : '人脸识别中...' }}
                </div>
                <div class="progress-count">{{ Math.min(detectionCount, requiredDetections) }}/{{ requiredDetections }}</div>
                <el-progress 
                  :percentage="Math.min((detectionCount / requiredDetections) * 100, 100)" 
                  color="#67C23A"
                  :stroke-width="6"
                />
              </div>
              <div v-if="faceDetected" class="face-detected-icon">
                <el-icon class="check-icon"><Check /></el-icon>
              </div>
            </div>
          </div>
          <div class="camera-controls">
            <el-button type="primary" @click="startCamera" :disabled="isCameraActive">
              <el-icon><VideoCamera /></el-icon> 打开
            </el-button>
            <el-button type="danger" @click="stopCamera" :disabled="!isCameraActive">
              <el-icon><CircleClose /></el-icon> 关闭
            </el-button>
          </div>
        </div>

        <div class="recognition-section">
          <div class="face-recognition-container">
            <div class="face-recognition-label">自动识别状态</div>
            <div class="status-display">
              <div v-if="!isCameraActive" class="status-item">
                <el-icon class="status-icon warning"><Warning /></el-icon>
                <span>摄像头未启动</span>
              </div>
              <div v-else-if="!faceDetected" class="status-item">
                <el-icon class="status-icon info"><InfoFilled /></el-icon>
                <span>等待检测人脸...</span>
              </div>
              <div v-else-if="detectionCount < requiredDetections && !autoRecognizing" class="status-item">
                <el-icon class="status-icon loading"><Loading /></el-icon>
                <span>人脸检测中 ({{ detectionCount }}/{{ requiredDetections }})</span>
              </div>
              <div v-else-if="autoRecognizing" class="status-item">
                <el-icon class="status-icon loading"><Loading /></el-icon>
                <span>正在验证打卡...</span>
              </div>
              <div v-else class="status-item">
                <el-icon class="status-icon success"><SuccessFilled /></el-icon>
                <span>检测完成，即将考勤</span>
              </div>
            </div>
            
            <div class="instruction-text">
              <p>📋 <strong>操作说明：</strong></p>
              <p>1. 点击"打开"按钮启动摄像头</p>
              <p>2. 将人脸对准摄像头保持5秒钟</p>
              <p>3. 系统将自动完成人脸识别和考勤</p>
            </div>
          </div>
        </div>
      </div>

      <div class="result-section">
        <div class="result-header">识别结果</div>
        <div class="result-container" v-if="recognitionResult">
          <div class="result-item">
            <span class="label">姓名:</span>
            <span class="value">{{ recognitionResult.name || '未识别' }}</span>
          </div>
          <div class="result-item">
            <span class="label">学号:</span>
            <span class="value">{{ recognitionResult.studentId || '未识别' }}</span>
          </div>
          <div class="result-item">
            <span class="label">相似度:</span>
            <div class="similarity-container">
              <el-progress 
                :percentage="recognitionResult.averageSimilarity || 0" 
                :color="getSimilarityColor()"
                :stroke-width="18"
              />
              <span class="similarity-text">{{ recognitionResult.averageSimilarity }}%</span>
            </div>
          </div>
          <div class="result-item" v-if="recognitionResult.verificationDetails">
            <span class="label">验证详情:</span>
            <div class="verification-details">
              <div class="detail-item">
                <span>成功率: {{ recognitionResult.successRate }}%</span>
              </div>
              <div class="detail-item">
                <span>成功次数: {{ recognitionResult.successCount }}/{{ recognitionResult.totalAttempts }}</span>
              </div>
            </div>
          </div>
          <div class="result-item">
            <span class="label">进入时间:</span>
            <span class="value">{{ recognitionResult.entryTime || '未识别' }}</span>
          </div>
          <div class="result-item">
            <span class="label">考勤指示灯:</span>
            <div class="indicator-light" :class="{ 'success': recognitionResult.status === 'success' }">
              <el-icon v-if="recognitionResult.status === 'success'"><Check /></el-icon>
              <el-icon v-else><Close /></el-icon>
            </div>
          </div>
        </div>
        <div class="result-empty" v-else>
          <el-empty description="请启动摄像头并保持人脸对准5秒钟完成打卡" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  VideoCamera, 
  CircleClose, 
  UserFilled, 
  Key, 
  Check, 
  Close,
  Warning,
  InfoFilled,
  Loading,
  SuccessFilled
} from '@element-plus/icons-vue'
import axios from 'axios'
import { debounce } from 'lodash-es'

const router = useRouter()
const video = ref<HTMLVideoElement | null>(null)
const canvas = ref<HTMLCanvasElement | null>(null)
const cameraView = ref<HTMLDivElement | null>(null)
const stream = ref<MediaStream | null>(null)
const isCameraActive = ref(false)
const isRecognizing = ref(false)
const faceData = ref<string | null>(null)
const recognitionResult = ref<{
  name: string, 
  studentId: string, 
  averageSimilarity: number,
  successRate: number,
  successCount: number,
  totalAttempts: number,
  entryTime: string,
  status: string,
  verificationDetails?: any
} | null>(null)

const faceDetected = ref(false)
const detectionCount = ref(0)
const requiredDetections = 5
const autoRecognizing = ref(false)
const facePosition = ref<{x: number, y: number, width: number, height: number} | null>(null)

let faceDetectionInterval: number | null = null

const switchToAdminLogin = () => {
  stopCamera()
  router.push('/login')
}

const startCamera = async () => {
  try {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      stream.value = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'user', width: 640, height: 480 }
      })
      
      if (video.value) {
        video.value.srcObject = stream.value
        isCameraActive.value = true
        
        resetDetectionState()
        
        startFaceDetection()
      }
    } else {
      ElMessage.error('您的浏览器不支持摄像头功能')
    }
  } catch (error) {
    console.error('启动摄像头失败:', error)
    ElMessage.error('无法访问摄像头，请确保摄像头已连接并授予了权限')
  }
}

const stopCamera = () => {
  if (stream.value) {
    stream.value.getTracks().forEach(track => {
      track.stop()
    })
    if (video.value) {
      video.value.srcObject = null
    }
    stream.value = null
    isCameraActive.value = false
    faceData.value = null
    facePosition.value = null
    
    resetDetectionState()
  }
  
  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval)
    faceDetectionInterval = null
  }
}

const resetDetectionState = () => {
  faceDetected.value = false
  detectionCount.value = 0
  autoRecognizing.value = false
  isRecognizing.value = false
  facePosition.value = null
}

const startFaceDetection = () => {
  faceDetectionInterval = window.setInterval(() => {
    if (isCameraActive.value && !isRecognizing.value && !autoRecognizing.value) {
      detectFace()
    }
  }, 200)
}

const detectFaceDebounced = debounce(async (imageData: string) => {
  try {
    const response = await axios.post('/api/face/detect', {
      image: imageData
    })
    
    if (response.data.success) {
      faceData.value = `data:image/jpeg;base64,${response.data.faceData}`
      faceDetected.value = true
      
      if (response.data.position) {
        const newPosition = {
          x: response.data.position.x,
          y: response.data.position.y,
          width: response.data.position.width,
          height: response.data.position.height
        }
        
        if (facePosition.value) {
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
      
      if (detectionCount.value >= requiredDetections && !autoRecognizing.value) {
        console.log('达到连续检测阈值，开始自动考勤')
        autoRecognizing.value = true
        
        setTimeout(() => {
          recognizeFace()
        }, 1500)
      }
    } else {
      faceData.value = null
      faceDetected.value = false
      facePosition.value = null
      
      detectionCount.value = 0
    }
  } catch (error) {
    console.error('人脸检测失败:', error)
    faceData.value = null
    faceDetected.value = false
    facePosition.value = null
    detectionCount.value = 0
  }
}, 100)

const detectFace = async () => {
  if (!video.value || !canvas.value) return
  
  const context = canvas.value.getContext('2d')
  if (!context) return
  
  canvas.value.width = video.value.videoWidth
  canvas.value.height = video.value.videoHeight
  
  context.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)
  
  const imageData = canvas.value.toDataURL('image/jpeg', 0.8)
  
  detectFaceDebounced(imageData)
}

const recognizeFace = async () => {
  if (!faceData.value) {
    ElMessage.warning('未检测到人脸，请正对摄像头')
    autoRecognizing.value = false
    return
  }
  
  isRecognizing.value = true
  
  try {
    const attendanceResponse = await axios.post('/api/attendance/face-checkin', {
      faceData: faceData.value
    })
    
    if (attendanceResponse.data && attendanceResponse.data.success) {
      const userData = attendanceResponse.data
      
      recognitionResult.value = {
        name: userData.userName || '未识别',
        studentId: userData.userNumber || '未识别',
        averageSimilarity: userData.averageSimilarity || 0,
        successRate: userData.successRate || 0,
        successCount: userData.successCount || 0,
        totalAttempts: userData.totalAttempts || 0,
        entryTime: userData.entryTime || new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        }),
        status: userData.status || 'success',
        verificationDetails: userData.verificationDetails
      }
      
      ElMessage.success(`打卡成功！欢迎 ${userData.userName}`)
      
      setTimeout(() => {
        resetDetectionState()
      }, 5000)
    } else {
      const errorMessage = attendanceResponse.data && attendanceResponse.data.message 
        ? attendanceResponse.data.message 
        : '打卡失败，请确保光线充足并正对摄像头'
      
      ElMessage({
        message: errorMessage,
        type: 'warning',
        duration: 5000,
        showClose: true
      })
      
      recognitionResult.value = null
      
      setTimeout(() => {
        resetDetectionState()
      }, 3000)
    }
  } catch (error) {
    console.error('打卡过程中发生错误:', error)
    
    let errorMessage = '打卡失败，请稍后重试'
    if (error.response && error.response.data) {
      errorMessage = error.response.data.message || errorMessage
    }
    
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 5000,
      showClose: true
    })
    
    recognitionResult.value = null
    
    setTimeout(() => {
      resetDetectionState()
    }, 3000)
  } finally {
    isRecognizing.value = false
    autoRecognizing.value = false
  }
}

const getSimilarityColor = () => {
  const similarity = recognitionResult.value?.averageSimilarity || 0
  if (similarity >= 80) return '#67C23A'
  if (similarity >= 70) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => {
  startCamera()
})

onUnmounted(() => {
  stopCamera()
  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval)
  }
})
</script>

<style scoped>
.attendance-panel-container {
  min-height: 100vh;
  background: linear-gradient(120deg, #f0f2f5 0%, #e6f7ff 100%);
  display: flex;
  flex-direction: column;
}

.panel-header {
  background-color: #1e3a8a;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.panel-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
}

.panel-content {
  flex: 1;
  display: flex;
  padding: 2rem;
  gap: 2rem;
  flex-wrap: wrap;
}

.camera-section {
  flex: 1;
  min-width: 320px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.camera-container, .face-recognition-container {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.camera-label, .face-recognition-label {
  font-size: 1.2rem;
  font-weight: 500;
  margin-bottom: 1rem;
  color: #1e3a8a;
}

.camera-view {
  width: 100%;
  height: 360px;
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.camera-view video {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.camera-controls {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.recognition-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.status-display {
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  font-size: 16px;
}

.status-icon {
  margin-right: 8px;
  font-size: 18px;
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
}

.instruction-text p {
  margin: 0;
  margin-bottom: 8px;
}

.instruction-text p:last-child {
  margin-bottom: 0;
}

.face-frame {
  width: 100%;
  height: 240px;
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.face-frame img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.face-frame.empty {
  flex-direction: column;
  gap: 0.5rem;
  color: #909399;
}

.face-frame.empty .el-icon {
  font-size: 3rem;
}

.recognition-btn {
  height: 48px;
  font-size: 1rem;
}

.result-section {
  flex: 1;
  min-width: 320px;
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-header {
  font-size: 1.2rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  color: #1e3a8a;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 0.5rem;
}

.result-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.result-item {
  display: flex;
  align-items: center;
}

.result-item .label {
  width: 100px;
  color: #606266;
  font-weight: 500;
}

.result-item .value {
  flex: 1;
  font-size: 1.1rem;
}

.similarity-container {
  flex: 1;
}

.similarity-text {
  margin-left: 10px;
  font-size: 14px;
  color: #606266;
}

.indicator-light {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #dcdfe6;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
  color: white;
}

.indicator-light.success {
  background-color: #67C23A;
}

.verification-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item span {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

.result-empty {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem 0;
}

@media (max-width: 768px) {
  .panel-content {
    flex-direction: column;
    padding: 1rem;
  }
  
  .camera-section, .result-section {
    width: 100%;
  }
}
</style> 