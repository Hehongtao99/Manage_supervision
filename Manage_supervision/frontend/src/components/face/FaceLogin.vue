<template>
  <div class="face-login-container">
    <h2 class="face-login-title">人脸识别登录</h2>
    <p class="face-login-description">请保持光线充足，面部清晰。确保画面中仅有您的脸部。</p>
    
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
        <el-button 
          type="primary" 
          @click="loginWithFace" 
          :loading="isLoading"
        >
          人脸登录
        </el-button>
        <el-button @click="retakePhoto">重新拍照</el-button>
      </div>
    </div>
    
    <div class="login-options">
      <el-divider>或者</el-divider>
      <el-button @click="backToPasswordLogin" plain>使用密码登录</el-button>
    </div>
    
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      :closable="true"
      @close="errorMessage = ''"
      style="margin-top: 15px; width: 100%;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCameraFilled, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

const videoElement = ref<HTMLVideoElement | null>(null)
const stream = ref<MediaStream | null>(null)
const imageCapture = ref<string | null>(null)
const isLoading = ref(false)
const isCameraStarted = ref(false)
const hasDetectedFace = ref(false)
const errorMessage = ref('')

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
    errorMessage.value = '无法访问摄像头，请确保允许浏览器使用摄像头'
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
  errorMessage.value = ''
}

// 人脸登录
const loginWithFace = async () => {
  if (!imageCapture.value) return
  
  errorMessage.value = ''
  isLoading.value = true
  
  try {
    const success = await userStore.loginWithFaceBase64(imageCapture.value)
    
    if (success) {
      ElMessage.success('人脸识别成功，登录成功')
      closeCamera()
    } else {
      errorMessage.value = userStore.error || '人脸识别失败，请重试'
    }
  } catch (error: any) {
    console.error('人脸登录失败:', error)
    errorMessage.value = '人脸登录失败，请稍后重试'
  } finally {
    isLoading.value = false
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

// 返回密码登录
const backToPasswordLogin = () => {
  closeCamera()
  emit('switch-mode', 'password')
}

// 定义事件
const emit = defineEmits(['switch-mode'])

onMounted(() => {
  startCamera()
})

onUnmounted(() => {
  closeCamera()
})
</script>

<style scoped>
.face-login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.face-login-title {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #409EFF;
}

.face-login-description {
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

.login-options {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 1rem;
}

.login-options .el-divider {
  width: 100%;
}
</style> 