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
            <div class="face-recognition-label">人脸识别</div>
            <div class="face-frame" v-if="faceData">
              <img :src="faceData" alt="检测到的人脸" />
            </div>
            <div class="face-frame empty" v-else>
              <el-icon><UserFilled /></el-icon>
              <span>等待人脸检测</span>
            </div>
          </div>
          
          <el-button 
            type="success" 
            class="recognition-btn" 
            @click="recognizeFace" 
            :disabled="!isCameraActive || isRecognizing"
            :loading="isRecognizing"
          >
            <el-icon><Key /></el-icon> 验证并考勤
          </el-button>
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
                :percentage="recognitionResult.similarity || 0" 
                :color="getSimilarityColor()"
                :stroke-width="18"
              />
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
          <el-empty description="暂无识别结果" />
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
  Close 
} from '@element-plus/icons-vue'
import axios from 'axios'

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
  similarity: number, 
  entryTime: string,
  status: string
} | null>(null)

// 切换到管理员登录
const switchToAdminLogin = () => {
  stopCamera()
  router.push('/login')
}

// 启动摄像头
const startCamera = async () => {
  try {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      stream.value = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'user', width: 640, height: 480 }
      })
      
      if (video.value) {
        video.value.srcObject = stream.value
        isCameraActive.value = true
        
        // 启动定时人脸检测
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

// 停止摄像头
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
  }
}

// 定时检测人脸
let faceDetectionInterval: number | null = null
const startFaceDetection = () => {
  // 每2秒检测一次人脸
  faceDetectionInterval = window.setInterval(() => {
    if (isCameraActive.value && !isRecognizing.value) {
      detectFace()
    }
  }, 2000)
}

// 检测人脸
const detectFace = async () => {
  if (!video.value || !canvas.value) return
  
  const context = canvas.value.getContext('2d')
  if (!context) return
  
  // 设置画布尺寸与视频一致
  canvas.value.width = video.value.videoWidth
  canvas.value.height = video.value.videoHeight
  
  // 在画布上绘制当前视频帧
  context.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)
  
  // 将画布内容转换为Base64
  const imageData = canvas.value.toDataURL('image/jpeg')
  
  try {
    // 调用后端人脸检测API
    const response = await axios.post('/api/face/detect', {
      image: imageData
    })
    
    if (response.data.success) {
      faceData.value = `data:image/jpeg;base64,${response.data.faceData}`
    } else {
      faceData.value = null
    }
  } catch (error) {
    console.error('人脸检测失败:', error)
    faceData.value = null
  }
}

// 人脸识别并考勤
const recognizeFace = async () => {
  if (!faceData.value) {
    ElMessage.warning('未检测到人脸，请正对摄像头')
    return
  }
  
  isRecognizing.value = true
  try {
    // 调用后端考勤API
    const attendanceResponse = await axios.post('/api/attendance/face-checkin', {
      faceData: faceData.value
    })
    
    // 根据返回的success字段判断是否成功，即使返回200也要检查
    if (attendanceResponse.data && attendanceResponse.data.success) {
      const userData = attendanceResponse.data
      
      // 更新识别结果
      recognitionResult.value = {
        name: userData.userName || '测试',
        studentId: userData.userNumber || '123',
        similarity: userData.similarity || 71,
        entryTime: userData.entryTime || new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        }),
        status: userData.status || 'success'
      }
      
      ElMessage.success('考勤成功')
    } else {
      // 错误信息优先显示服务器返回的消息
      const errorMessage = attendanceResponse.data && attendanceResponse.data.message 
        ? attendanceResponse.data.message 
        : '未知错误，请联系管理员'
      
      ElMessage({
        message: errorMessage,
        type: 'warning',
        duration: 5000,
        showClose: true
      })
      
      // 不显示测试数据，让用户看到真实错误
      recognitionResult.value = null
    }
  } catch (error) {
    console.error('考勤过程中发生错误:', error)
    
    // 尝试从错误响应中提取错误信息
    let errorMessage = '考勤失败，请稍后重试'
    if (error.response && error.response.data) {
      errorMessage = error.response.data.message || errorMessage
    }
    
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 5000,
      showClose: true
    })
    
    // 不显示测试数据，让用户看到真实错误
    recognitionResult.value = null
  } finally {
    isRecognizing.value = false
  }
}

// 获取相似度颜色
const getSimilarityColor = () => {
  const similarity = recognitionResult.value?.similarity || 0
  if (similarity >= 80) return '#67C23A' // 绿色
  if (similarity >= 60) return '#E6A23C' // 黄色
  return '#F56C6C' // 红色
}

// 在组件挂载时初始化
onMounted(() => {
  // 默认打开摄像头
  startCamera()
})

// 在组件卸载时清理
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