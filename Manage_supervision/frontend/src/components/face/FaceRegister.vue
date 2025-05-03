<template>
  <div class="face-register-container">
    <h2 class="face-register-title">人脸录入</h2>
    <p class="face-register-description">
      请保持光线充足，面部清晰，并确保画面中只有您的脸部。
      <span v-if="autoDetectEnabled" class="auto-mode-hint">已开启自动录入模式</span>
    </p>
    
    <div class="camera-container" v-if="!imageCapture">
      <video 
        ref="videoElement"
        :class="{ 'video-container': true, 'has-face': hasDetectedFace }"
        autoplay 
        playsinline
      ></video>
      <canvas 
        ref="canvasElement" 
        class="detection-canvas"
      ></canvas>
      <div class="face-guide" :class="{ 'has-face': hasDetectedFace }">
        <div class="face-outline"></div>
        <div v-if="hasDetectedFace && detectionProgress > 0" class="auto-detect-hint">
          系统正在自动采集
          <div class="progress-text">{{ Math.round(detectionProgress) }}%</div>
        </div>
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
          :disabled="!isCameraStarted || isLoading || !hasDetectedFace"
        >
          手动拍照
        </el-button>
        <el-switch
          v-model="autoDetectEnabled"
          active-text="自动检测"
          inactive-text="手动拍照"
          class="auto-detect-switch"
        />
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
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoCameraFilled, 
  Camera, 
  CircleCheckFilled 
} from '@element-plus/icons-vue'
import { registerFaceWithBase64, deleteFace, checkFaceStatus } from '../../api/face'

const videoElement = ref<HTMLVideoElement | null>(null)
const canvasElement = ref<HTMLCanvasElement | null>(null)
const stream = ref<MediaStream | null>(null)
const imageCapture = ref<string | null>(null)
const isLoading = ref(false)
const isDeleteLoading = ref(false)
const isCameraStarted = ref(false)
const hasDetectedFace = ref(false)
const isRegistered = ref(false)
const faceDetectionInterval = ref<number | null>(null)
const modelsLoaded = ref(false)
const faceDetectionStartTime = ref<number | null>(null)
const autoDetectEnabled = ref(true)
const detectionProgress = ref(0)

// 检查是否已注册人脸
const checkRegistrationStatus = async () => {
  try {
    isRegistered.value = await checkFaceStatus()
  } catch (error) {
    console.error('检查人脸注册状态失败:', error)
  }
}

// 加载人脸检测模型 - 改为简单检测
const loadFaceDetectionModels = async () => {
  try {
    // 使用更简单的方法，不再需要加载模型
    modelsLoaded.value = true;
    console.log('准备就绪');
  } catch (error) {
    console.error('初始化失败:', error);
    ElMessage.error('初始化失败，请刷新页面重试');
  }
}

// 开启摄像头
const startCamera = async () => {
  try {
    // 关闭现有摄像头
    closeCamera()
    
    // 确保模型已加载
    if (!modelsLoaded.value) {
      await loadFaceDetectionModels();
    }
    
    stream.value = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'user'
      }
    })
    
    if (videoElement.value) {
      videoElement.value.srcObject = stream.value
      videoElement.value.play()
      isCameraStarted.value = true
      
      // 启动人脸检测
      startFaceDetection()
    }
  } catch (error) {
    console.error('无法访问摄像头:', error)
    ElMessage.error('无法访问摄像头，请确保允许浏览器使用摄像头')
  }
}

// 启动人脸检测 - 使用更简单的检测方法
const startFaceDetection = () => {
  if (!videoElement.value || !canvasElement.value) return
  
  // 设置canvas大小与视频相同
  canvasElement.value.width = videoElement.value.clientWidth
  canvasElement.value.height = videoElement.value.clientHeight
  
  // 使用简单的检测方法
  faceDetectionInterval.value = window.setInterval(async () => {
    if (!isCameraStarted.value || !videoElement.value || !canvasElement.value) {
      if (faceDetectionInterval.value !== null) {
        clearInterval(faceDetectionInterval.value)
        faceDetectionInterval.value = null
      }
      return
    }
    
    try {
      // 检测画面亮度和对比度作为简单的人脸存在指标
      const tempCanvas = document.createElement('canvas');
      const tempCtx = tempCanvas.getContext('2d');
      tempCanvas.width = 50;  // 缩小尺寸加快处理
      tempCanvas.height = 50;
      
      if (tempCtx && videoElement.value) {
        // 绘制视频帧到临时canvas
        tempCtx.drawImage(
          videoElement.value, 
          0, 0, videoElement.value.videoWidth, videoElement.value.videoHeight,
          0, 0, 50, 50
        );
        
        // 获取画面数据
        const imageData = tempCtx.getImageData(0, 0, 50, 50);
        const data = imageData.data;
        
        // 计算中心区域的亮度变化
        let centerPixels = 0;
        let centerBrightness = 0;
        
        for (let y = 15; y < 35; y++) {
          for (let x = 15; x < 35; x++) {
            const i = (y * 50 + x) * 4;
            const r = data[i];
            const g = data[i + 1];
            const b = data[i + 2];
            const brightness = (r + g + b) / 3;
            centerBrightness += brightness;
            centerPixels++;
          }
        }
        
        centerBrightness /= centerPixels;
        
        // 简单判断：如果中心区域亮度在合理范围，可能存在人脸
        const currentHasFace = centerBrightness > 50 && centerBrightness < 200;
        
        // 更新人脸检测状态
        if (currentHasFace) {
          if (!hasDetectedFace.value) {
            // 首次检测到人脸，开始计时
            hasDetectedFace.value = true;
            faceDetectionStartTime.value = Date.now();
          } else if (autoDetectEnabled.value && faceDetectionStartTime.value) {
            // 已经检测到人脸，计算已检测时间
            const detectionDuration = Date.now() - faceDetectionStartTime.value;
            // 更新进度，最大为100%
            detectionProgress.value = Math.min((detectionDuration / 2000) * 100, 100);
            
            // 如果检测到人脸超过2秒，自动捕获
            if (detectionDuration >= 2000 && !imageCapture.value) {
              captureImage();
              
              // 如果自动检测启用，在捕获照片后延迟500ms自动注册
              if (autoDetectEnabled.value) {
                setTimeout(() => {
                  registerFace();
                }, 500);
              }
            }
          }
        } else {
          // 丢失人脸检测，重置计时
          hasDetectedFace.value = false;
          faceDetectionStartTime.value = null;
          detectionProgress.value = 0;
        }
        
        // 绘制指示框和进度
        const ctx = canvasElement.value.getContext('2d');
        if (ctx) {
          ctx.clearRect(0, 0, canvasElement.value.width, canvasElement.value.height);
          
          if (hasDetectedFace.value) {
            // 绘制人脸框
            const centerX = canvasElement.value.width / 2;
            const centerY = canvasElement.value.height / 2;
            const size = Math.min(canvasElement.value.width, canvasElement.value.height) * 0.5;
            
            // 绘制人脸圆形指示
            ctx.strokeStyle = '#67C23A';  // 绿色
            ctx.lineWidth = 3;
            ctx.beginPath();
            ctx.arc(centerX, centerY, size / 2, 0, Math.PI * 2);
            ctx.stroke();
            
            // 绘制进度环
            if (autoDetectEnabled.value && detectionProgress.value > 0) {
              const startAngle = -Math.PI / 2; // 从顶部开始
              const endAngle = startAngle + (Math.PI * 2 * detectionProgress.value / 100);
              
              ctx.beginPath();
              ctx.arc(centerX, centerY, size / 2 + 10, startAngle, endAngle);
              ctx.strokeStyle = '#409EFF'; // 蓝色进度条
              ctx.lineWidth = 5;
              ctx.stroke();
            }
          }
        }
      }
    } catch (error) {
      console.error('检测错误:', error);
    }
  }, 100); // 提高到100ms更新一次，使动画更流畅
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
  if (faceDetectionInterval.value !== null) {
    clearInterval(faceDetectionInterval.value)
    faceDetectionInterval.value = null
  }

  // 重置人脸检测状态
  faceDetectionStartTime.value = null;
  detectionProgress.value = 0;

  if (stream.value) {
    stream.value.getTracks().forEach(track => track.stop())
    stream.value = null
    isCameraStarted.value = false
    hasDetectedFace.value = false
  }
}

onMounted(async () => {
  await checkRegistrationStatus()
  if (!isRegistered.value) {
    await loadFaceDetectionModels()
    await nextTick()
    startCamera()
  }
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
  flex-wrap: wrap;
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

.detection-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.auto-detect-hint {
  position: absolute;
  bottom: 20%;
  left: 0;
  right: 0;
  text-align: center;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 8px;
  border-radius: 4px;
  font-size: 14px;
  max-width: 200px;
  margin: 0 auto;
}

.progress-text {
  font-size: 16px;
  font-weight: bold;
  margin-top: 4px;
}

.auto-detect-switch {
  margin-top: 8px;
}

.auto-mode-hint {
  display: block;
  color: #409EFF;
  font-weight: bold;
  margin-top: 5px;
  font-size: 14px;
}
</style>