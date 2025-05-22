<template>
  <div class="face-registration">
    <el-card class="face-card">
      <template #header>
        <div class="card-header">
          <h2>人脸信息录入</h2>
          <p class="description">请保持面部在摄像头中央，光线充足，并确保背景简单</p>
        </div>
      </template>
      
      <div class="camera-container">
        <div v-if="!faceRegistered">
          <!-- 视频预览 -->
          <div class="video-container">
            <video ref="videoElement" class="video-preview" autoplay playsinline></video>
            <div class="face-overlay" :class="{ active: faceDetected }">
              <div class="face-frame"></div>
            </div>
            <div v-if="processing" class="processing-overlay">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <p>正在处理...</p>
            </div>
            <div v-if="autoCapturing" class="auto-capturing-status">
              <el-icon class="pulse-icon"><VideoCamera /></el-icon>
              <span>自动识别中...</span>
            </div>
          </div>
          
          <div class="capture-controls">
            <el-button 
              type="primary" 
              :disabled="!cameraActive || processing" 
              @click="toggleAutoCapture"
            >
              <el-icon><VideoCamera /></el-icon>
              <span>{{ autoCapturing ? '停止自动识别' : '开始自动识别' }}</span>
            </el-button>
            
            <el-button 
              v-if="!autoCapturing"
              type="success" 
              :disabled="!cameraActive || processing" 
              @click="captureFace"
            >
              <el-icon><Camera /></el-icon>
              <span>手动拍摄</span>
            </el-button>
            
            <el-button @click="toggleCamera" :type="cameraActive ? 'danger' : 'success'" :disabled="processing">
              {{ cameraActive ? '关闭摄像头' : '打开摄像头' }}
            </el-button>
          </div>
          
          <div v-if="capturedImage" class="preview-container">
            <h3>预览</h3>
            <div class="preview-image">
              <img :src="capturedImage" alt="人脸预览" />
            </div>
            <div class="preview-controls">
              <el-button type="primary" @click="saveFace" :disabled="processing || !faceDetected">
                <el-icon><Check /></el-icon>
                <span>保存</span>
              </el-button>
              <el-button @click="resetCapture" :disabled="processing">
                <el-icon><Refresh /></el-icon>
                <span>重新拍摄</span>
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="registration-success">
          <el-result
            icon="success"
            title="人脸信息注册成功"
            sub-title="您的人脸信息已成功注册，可以用于签到和身份验证"
          >
            <template #extra>
              <el-button type="primary" @click="resetRegistration">
                重新注册
              </el-button>
            </template>
          </el-result>
        </div>
      </div>
      
      <div class="tips-container">
        <h3>注意事项：</h3>
        <ul>
          <li>请确保光线充足，避免背光</li>
          <li>保持面部朝向摄像头，并确保整个面部在画面中</li>
          <li>注册时请保持自然表情，避免使用手机、眼镜等遮挡面部</li>
          <li>每个用户只需注册一次人脸信息，之后可直接用于签到</li>
          <li><strong>开启自动识别后，系统将自动寻找并截取清晰的人脸图像</strong></li>
        </ul>
      </div>
    </el-card>
    
    <!-- 错误提示 -->
    <el-dialog
      v-model="errorVisible"
      title="提示"
      width="30%"
    >
      <span>{{ errorMessage }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="errorVisible = false">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElLoading, ElNotification } from 'element-plus'
import { Camera, Check, Refresh, Loading, VideoCamera } from '@element-plus/icons-vue'
import apiService from '@/api/apiService'

export default {
  name: 'FaceRegistration',
  components: {
    Camera,
    Check,
    Refresh,
    Loading,
    VideoCamera
  },
  // 定义注册完成事件
  emits: ['registration-complete'],
  setup(props, { emit }) {
    const videoElement = ref(null)
    const cameraActive = ref(false)
    const processing = ref(false)
    const capturedImage = ref(null)
    const faceDetected = ref(false)
    const faceRegistered = ref(false)
    const errorVisible = ref(false)
    const errorMessage = ref('')
    const autoCapturing = ref(true)
    
    let stream = null
    let detectionInterval = null
    let autoCaptureInterval = null
    let consecutiveDetections = 0
    
    // 检查用户是否已注册人脸
    const checkFaceRegistrationStatus = async () => {
      try {
        const response = await apiService.face.getFaceStatus()
        if (response.data.registered) {
          faceRegistered.value = true
          ElNotification({
            title: '提示',
            message: '您已完成人脸注册',
            type: 'info'
          })
        } else {
          // 如果未注册人脸，自动打开摄像头
          await startCamera()
        }
      } catch (error) {
        console.error('检查人脸注册状态失败:', error)
      }
    }
    
    // 打开/关闭摄像头
    const toggleCamera = async () => {
      if (cameraActive.value) {
        stopCamera()
      } else {
        await startCamera()
      }
    }
    
    // 启动摄像头
    const startCamera = async () => {
      try {
        stream = await navigator.mediaDevices.getUserMedia({
          video: {
            width: { ideal: 1280 },
            height: { ideal: 720 },
            facingMode: 'user'
          },
          audio: false
        })
        
        if (videoElement.value) {
          videoElement.value.srcObject = stream
          cameraActive.value = true
          
          // 每3秒进行一次人脸检测
          startFaceDetection()
        }
      } catch (error) {
        console.error('访问摄像头失败:', error)
        errorMessage.value = '无法访问摄像头，请确保已授予摄像头权限并且设备有可用的摄像头。'
        errorVisible.value = true
      }
    }
    
    // 停止摄像头
    const stopCamera = () => {
      if (stream) {
        stream.getTracks().forEach(track => track.stop())
        stream = null
        cameraActive.value = false
        faceDetected.value = false
      }
      
      if (detectionInterval) {
        clearInterval(detectionInterval)
        detectionInterval = null
      }
      
      stopAutoCapture()
    }
    
    // 定期检测人脸
    const startFaceDetection = () => {
      detectionInterval = setInterval(async () => {
        if (!cameraActive.value || processing.value || capturedImage.value) {
          return
        }
        
        try {
          const canvas = document.createElement('canvas')
          const video = videoElement.value
          
          if (!video) return
          
          canvas.width = video.videoWidth
          canvas.height = video.videoHeight
          
          const ctx = canvas.getContext('2d')
          ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
          
          const imageData = canvas.toDataURL('image/jpeg', 0.8)
          
          // 调用API检测人脸
          const response = await apiService.face.detectFace(imageData.split(',')[1])
          
          if (response.data.success) {
            faceDetected.value = true
            // 自动捕获模式下，跟踪连续检测次数
            if (autoCapturing.value) {
              consecutiveDetections++
              // 连续3次检测到人脸，自动捕获
              if (consecutiveDetections >= 3) {
                autoCaptureImage(imageData)
                consecutiveDetections = 0
              }
            }
          } else {
            faceDetected.value = false
            consecutiveDetections = 0
          }
        } catch (error) {
          faceDetected.value = false
          consecutiveDetections = 0
          console.error('人脸检测失败:', error)
        }
      }, 1000) // 每1秒检测一次
    }
    
    // 开启/关闭自动捕获
    const toggleAutoCapture = () => {
      if (autoCapturing.value) {
        stopAutoCapture()
      } else {
        startAutoCapture()
      }
    }
    
    // 开启自动捕获
    const startAutoCapture = () => {
      autoCapturing.value = true
      ElNotification({
        title: '提示',
        message: '自动识别已开启，请面向摄像头保持自然表情',
        type: 'info'
      })
    }
    
    // 停止自动捕获
    const stopAutoCapture = () => {
      autoCapturing.value = false
      consecutiveDetections = 0
    }
    
    // 自动捕获图像
    const autoCaptureImage = async (imageData) => {
      if (capturedImage.value || !autoCapturing.value) return
      
      processing.value = true
      
      try {
        // 调用API检测人脸
        const response = await apiService.face.detectFace(imageData.split(',')[1])
        
        if (response.data.success) {
          capturedImage.value = imageData
          faceDetected.value = true
          stopAutoCapture()
          
          ElNotification({
            title: '成功',
            message: '已自动捕获到清晰人脸',
            type: 'success'
          })
          
          // 自动保存人脸
          await saveFace()
        }
      } catch (error) {
        console.error('自动捕获人脸失败:', error)
      } finally {
        processing.value = false
      }
    }
    
    // 手动拍摄照片
    const captureFace = async () => {
      if (!cameraActive.value) return
      
      processing.value = true
      
      try {
        const canvas = document.createElement('canvas')
        const video = videoElement.value
        
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        
        const ctx = canvas.getContext('2d')
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
        
        const imageData = canvas.toDataURL('image/jpeg', 0.9)
        capturedImage.value = imageData
        
        // 调用API检测人脸
        const response = await apiService.face.detectFace(imageData.split(',')[1])
        
        if (response.data.success) {
          faceDetected.value = true
          ElNotification({
            title: '成功',
            message: '已成功捕获人脸',
            type: 'success'
          })
        } else {
          faceDetected.value = false
          ElNotification({
            title: '警告',
            message: '未检测到人脸，请确保面部在摄像头中央并有足够的光线',
            type: 'warning'
          })
        }
      } catch (error) {
        console.error('捕获人脸失败:', error)
        errorMessage.value = '捕获人脸失败，请重试'
        errorVisible.value = true
      } finally {
        processing.value = false
      }
    }
    
    // 重置捕获
    const resetCapture = () => {
      capturedImage.value = null
    }
    
    // 保存人脸信息
    const saveFace = async () => {
      if (!capturedImage.value || !faceDetected.value) return
      
      processing.value = true
      const loadingInstance = ElLoading.service({
        fullscreen: true,
        text: '正在保存人脸信息...'
      })
      
      try {
        const response = await apiService.face.registerFace(capturedImage.value)
        
        if (response.data.success) {
          faceRegistered.value = true
          ElNotification({
            title: '成功',
            message: '人脸信息注册成功',
            type: 'success'
          })
          
          // 停止摄像头
          stopCamera()
          
          // 触发注册完成事件
          emit('registration-complete')
        } else {
          ElNotification({
            title: '错误',
            message: response.data.message || '人脸信息注册失败',
            type: 'error'
          })
        }
      } catch (error) {
        console.error('保存人脸信息失败:', error)
        errorMessage.value = error.response?.data?.message || '保存人脸信息失败，请重试'
        errorVisible.value = true
      } finally {
        loadingInstance.close()
        processing.value = false
      }
    }
    
    // 重置注册状态
    const resetRegistration = () => {
      faceRegistered.value = false
      capturedImage.value = null
      faceDetected.value = false
    }
    
    onMounted(() => {
      checkFaceRegistrationStatus()
    })
    
    onUnmounted(() => {
      // 清理资源
      stopCamera()
      
      if (detectionInterval) {
        clearInterval(detectionInterval)
      }
    })
    
    return {
      videoElement,
      cameraActive,
      processing,
      capturedImage,
      faceDetected,
      faceRegistered,
      errorVisible,
      errorMessage,
      autoCapturing,
      toggleCamera,
      captureFace,
      resetCapture,
      saveFace,
      resetRegistration,
      toggleAutoCapture
    }
  }
}
</script>

<style scoped>
.face-registration {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}

.face-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.description {
  color: #606266;
  margin-top: 8px;
}

.camera-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.video-container {
  position: relative;
  width: 100%;
  max-width: 500px;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #000;
}

.video-preview {
  width: 100%;
  height: auto;
  display: block;
}

.face-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: none;
}

.face-frame {
  width: 220px;
  height: 220px;
  border: 2px dashed #ccc;
  border-radius: 50%;
  transition: all 0.3s;
}

.face-overlay.active .face-frame {
  border-color: #67C23A;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.3);
}

.processing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 10px;
  animation: rotating 2s linear infinite;
}

.auto-capturing-status {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(64, 158, 255, 0.8);
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  z-index: 5;
}

.pulse-icon {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
  100% {
    opacity: 1;
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.capture-controls {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.preview-container {
  width: 100%;
  max-width: 300px;
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-image {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 16px;
  border: 1px solid #dcdfe6;
}

.preview-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-controls {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.tips-container {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 20px;
}

.tips-container h3 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #303133;
}

.tips-container ul {
  margin: 0;
  padding-left: 20px;
}

.tips-container li {
  margin-bottom: 8px;
  color: #606266;
}

.registration-success {
  width: 100%;
  padding: 20px;
}
</style> 