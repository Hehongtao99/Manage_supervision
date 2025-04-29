<template>
  <div class="face-register-container">
    <h2 class="face-register-title">人脸录入</h2>
    <p class="face-register-description">请保持光线充足，面部清晰，并确保画面中只有您的脸部。</p>
    
    <div class="camera-container" v-if="!imageCapture">
      <video 
        ref="videoElement"
        :class="{ 'video-container': true, 'has-face': hasDetectedFace }"
        autoplay 
        playsinline
      ></video>
      <div class="face-guide" :class="{ 'has-face': hasDetectedFace }">
        <div class="face-outline"></div>
      </div>
      <div class="camera-controls">
        <el-button 
          type="primary" 
          :icon="VideoCameraFilled" 
          @click="startCamera" 
          :disabled="isCameraStarted"
        >
          开启摄像头
        </el-button>
        <el-button 
          type="success" 
          :icon="Camera" 
          @click="captureImage" 
          :disabled="!isCameraStarted || isLoading"
        >
          拍照
        </el-button>
      </div>
    </div>
    
    <div class="preview-container" v-else>
      <img :src="imageCapture" alt="人脸照片" class="preview-image" />
      <div class="preview-controls">
        <el-button type="primary" @click="registerFace" :loading="isLoading">确认注册</el-button>
        <el-button @click="retakePhoto">重新拍照</el-button>
      </div>
    </div>
    
    <div class="registered-container" v-if="isRegistered">
      <div class="success-status">
        <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        <span>您已成功注册人脸</span>
      </div>
      <el-button type="danger" @click="handleDeleteFace" :loading="isDeleteLoading">删除人脸信息</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoCameraFilled, 
  Camera, 
  CircleCheckFilled 
} from '@element-plus/icons-vue'
import { registerFaceWithBase64, deleteFace, checkFaceStatus } from '../../api/face'

const videoElement = ref<HTMLVideoElement | null>(null)
const stream = ref<MediaStream | null>(null)
const imageCapture = ref<string | null>(null)
const isLoading = ref(false)
const isDeleteLoading = ref(false)
const isCameraStarted = ref(false)
const hasDetectedFace = ref(false)
const isRegistered = ref(false)

// 检查是否已注册人脸
const checkRegistrationStatus = async () => {
  try {
    isRegistered.value = await checkFaceStatus()
  } catch (error) {
    console.error('检查人脸注册状态失败:', error)
  }
}

// 开启摄像头
const startCamera = async () => {
  try {
    stream.value = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'user'
      }
    })
    
    if (videoElement.value) {
      videoElement.value.srcObject = stream.value
      isCameraStarted.value = true
      
      // 启动人脸检测
      startFaceDetection()
    }
  } catch (error) {
    console.error('无法访问摄像头:', error)
    ElMessage.error('无法访问摄像头，请确保允许浏览器使用摄像头')
  }
}

// 启动人脸检测
const startFaceDetection = () => {
  if (!videoElement.value) return
  
  // 在实际的生产环境中，这里应该使用真实的人脸检测库
  // 这里我们使用简单的定时器模拟人脸检测效果
  const detectionInterval = setInterval(() => {
    if (!isCameraStarted.value) {
      clearInterval(detectionInterval)
      return
    }
    
    // 随机模拟人脸检测结果
    const detectionResult = Math.random() > 0.2
    hasDetectedFace.value = detectionResult
    
    // 如果检测到人脸，并且还没有拍照，可以自动拍照
    if (detectionResult && !imageCapture.value && isCameraStarted.value) {
      // 可以选择自动拍照或让用户手动点击拍照按钮
      // captureImage()
    }
  }, 1000) // 每秒检测一次
}

// 拍照
const captureImage = () => {
  if (!videoElement.value || !isCameraStarted.value) return
  
  const canvas = document.createElement('canvas')
  canvas.width = videoElement.value.videoWidth
  canvas.height = videoElement.value.videoHeight
  const ctx = canvas.getContext('2d')
  
  if (ctx) {
    ctx.drawImage(videoElement.value, 0, 0, canvas.width, canvas.height)
    imageCapture.value = canvas.toDataURL('image/jpeg', 0.9)
  }
}

// 重新拍照
const retakePhoto = () => {
  imageCapture.value = null
}

// 注册人脸
const registerFace = async () => {
  if (!imageCapture.value) return
  
  isLoading.value = true
  try {
    const success = await registerFaceWithBase64(imageCapture.value)
    
    if (success) {
      ElMessage.success('人脸注册成功')
      isRegistered.value = true
      closeCamera()
    } else {
      ElMessage.error('人脸注册失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '人脸注册失败')
  } finally {
    isLoading.value = false
  }
}

// 删除人脸信息
const handleDeleteFace = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除您的人脸信息吗？删除后将无法使用人脸登录。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    isDeleteLoading.value = true
    const success = await deleteFace()
    
    if (success) {
      ElMessage.success('人脸信息删除成功')
      isRegistered.value = false
      imageCapture.value = null
    } else {
      ElMessage.error('人脸信息删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除人脸信息失败')
    }
  } finally {
    isDeleteLoading.value = false
  }
}

// 关闭摄像头
const closeCamera = () => {
  if (stream.value) {
    stream.value.getTracks().forEach(track => track.stop())
    stream.value = null
    isCameraStarted.value = false
    hasDetectedFace.value = false
  }
}

onMounted(() => {
  checkRegistrationStatus()
})

onUnmounted(() => {
  closeCamera()
})
</script>

<style scoped>
.face-register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.face-register-title {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #409EFF;
}

.face-register-description {
  margin-bottom: 1.5rem;
  color: #606266;
  text-align: center;
}

.camera-container {
  position: relative;
  width: 100%;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.video-container {
  display: block;
  width: 100%;
  background-color: #f5f7fa;
  transition: all 0.3s ease;
}

.video-container.has-face {
  border: 2px solid #67C23A;
}

.face-guide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: none;
}

.face-outline {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  border: 2px dashed #606266;
  transition: all 0.3s ease;
}

.face-guide.has-face .face-outline {
  border: 2px solid #67C23A;
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.3);
}

.camera-controls {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.preview-container {
  width: 100%;
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-image {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
}

.preview-controls {
  display: flex;
  gap: 1rem;
}

.registered-container {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  background-color: #f0f9eb;
  padding: 1rem;
  border-radius: 8px;
  width: 100%;
}

.success-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #67C23A;
  font-weight: 500;
}

.success-icon {
  font-size: 1.5rem;
}
</style>