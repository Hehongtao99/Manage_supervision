<template>
  <div class="face-login-container">
    <h2 class="face-login-title">人脸识别登录</h2>
    <p class="face-login-description">
      <span v-if="!isCameraStarted">请点击下方按钮开启摄像头进行人脸识别</span>
      <span v-else>请保持光线充足，面部清晰。确保画面中仅有您的脸部。
        <span v-if="autoDetectEnabled" class="auto-mode-hint">已开启自动识别模式</span>
      </span>
    </p>
    
    <div v-if="!isCameraStarted" class="camera-start-container">
      <el-button 
        type="primary" 
        size="large"
        :icon="VideoCameraFilled" 
        @click="startCamera" 
        class="start-camera-btn"
      >
        开启摄像头
      </el-button>
    </div>
    
    <div class="camera-container" v-else-if="!imageCapture">
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
          系统正在自动识别
          <div class="progress-text">{{ Math.round(detectionProgress) }}%</div>
        </div>
      </div>
      <div class="camera-controls">
        <el-button 
          type="danger" 
          :icon="VideoCameraFilled" 
          @click="closeCamera" 
        >
          关闭摄像头
        </el-button>
        <el-button 
          type="success" 
          :icon="Camera" 
          @click="captureImage" 
          :disabled="isLoading || !hasDetectedFace"
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
    
    <!-- 照片预览 -->
    <div class="preview-container" v-else-if="imageCapture && !showUserSelection">
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
    
    <!-- 多用户选择界面 -->
    <div class="user-selection-container" v-else-if="showUserSelection">
      <h3 class="selection-title">请选择要登录的账号</h3>
      <p class="selection-description">系统检测到多个匹配的账号，请选择一个进行登录</p>
      
      <div class="user-list">
        <div 
          v-for="user in uniqueMatchingUsers" 
          :key="user.id" 
          class="user-card"
          @click="selectUser(user.id)"
        >
          <div class="user-avatar">
            <img v-if="user.avatar" :src="user.avatar" alt="用户头像" />
            <el-avatar v-else :size="64" :src="defaultAvatar" />
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.nickname || user.username }}</div>
            <div class="user-role">{{ getUserRoleText(user.roles) }}</div>
            <div class="user-number" v-if="user.userNumber">{{ user.userNumber }}</div>
          </div>
        </div>
      </div>
      
      <div class="selection-controls">
        <el-button @click="cancelUserSelection">取消选择</el-button>
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
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCameraFilled, Camera, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

const videoElement = ref<HTMLVideoElement | null>(null)
const canvasElement = ref<HTMLCanvasElement | null>(null)
const stream = ref<MediaStream | null>(null)
const imageCapture = ref<string | null>(null)
const isLoading = ref(false)
const isCameraStarted = ref(false)
const hasDetectedFace = ref(false)
const errorMessage = ref('')
const faceDetectionInterval = ref<number | null>(null)
const modelsLoaded = ref(false)
const faceDetectionTimer = ref<number | null>(null)
const faceDetectionStartTime = ref<number | null>(null)
const autoDetectEnabled = ref(true)
const detectionProgress = ref(0)
const showUserSelection = ref(false)
const matchingUsers = ref<any[]>([])
const defaultAvatar = ref('/placeholder-avatar.png')

// 添加计算属性，去重匹配到的用户
const uniqueMatchingUsers = computed(() => {
  // 使用 Map 以用户 ID 为键去重
  const uniqueUsersMap = new Map();
  matchingUsers.value.forEach(user => {
    if (!uniqueUsersMap.has(user.id)) {
      uniqueUsersMap.set(user.id, user);
    }
  });
  
  // 转换回数组
  return Array.from(uniqueUsersMap.values());
});

// 加载人脸检测模型
const loadFaceDetectionModels = async () => {
  try {
    // 由于我们使用的是简单方法，不依赖TensorFlow模型，只需标记为已加载
    modelsLoaded.value = true;
    console.log('人脸检测模块准备就绪');
    return true;
  } catch (error) {
    console.error('初始化人脸检测模块失败:', error);
    errorMessage.value = '初始化失败，请刷新页面重试';
    return false;
  }
}

// 开启摄像头
const startCamera = async () => {
  try {
    // 确保任何可能存在的旧流已经关闭
    closeCamera()
    
    // 添加小延迟，避免设备资源竞争
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // 确保模型已加载
    if (!modelsLoaded.value) {
      await loadFaceDetectionModels();
    }
    
    // 设置摄像头状态为启动中，防止重复点击
    isCameraStarted.value = true
    
    // 等待DOM更新
    await nextTick()
    
    // 检查视频元素是否存在
    if (!videoElement.value) {
      console.error('视频元素未找到，可能DOM还未完全加载')
      errorMessage.value = '初始化摄像头失败，请刷新页面后重试'
      isCameraStarted.value = false
      return
    }
    
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
        
        // 确保视频元素加载完成
        videoElement.value.onloadedmetadata = () => {
          // 确保视频播放
          videoElement.value?.play().catch(err => {
            console.error('视频播放失败:', err)
            errorMessage.value = '视频播放失败，请刷新页面重试'
            isCameraStarted.value = false
          })
          
          // 启动人脸检测
          startFaceDetection()
        }
      } else {
        throw new Error('视频元素在获取媒体流后未找到')
      }
    } catch (mediaError) {
      console.error('获取媒体流失败:', mediaError)
      errorMessage.value = '无法访问摄像头，请确保允许浏览器使用摄像头，并确保没有其他应用程序正在使用摄像头'
      isCameraStarted.value = false
      stream.value = null
    }
  } catch (error) {
    console.error('摄像头初始化过程出错:', error)
    isCameraStarted.value = false
    stream.value = null
    errorMessage.value = '摄像头初始化失败，请刷新页面后重试'
  }
}

// 启动人脸检测
const startFaceDetection = async () => {
  try {
    // 等待DOM更新，确保canvas元素存在
    await nextTick()
    
    if (!videoElement.value || !canvasElement.value) {
      console.error('视频或Canvas元素未找到，无法启动人脸检测')
      errorMessage.value = '无法启动人脸检测，必要元素未找到'
      return
    }
    
    // 设置canvas大小与视频相同
    canvasElement.value.width = videoElement.value.clientWidth
    canvasElement.value.height = videoElement.value.clientHeight
    
    // 使用简单的检测方法，不依赖face-api.js的复杂模型
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
          let edgeBrightness = 0;
          let edgePixels = 0;
          let brightnessVariance = 0;
          
          // 计算中心区域亮度
          for (let y = 15; y < 35; y++) {
            for (let x = 15; x < 35; x++) {
              const i = (y * 50 + x) * 4;
              const r = data[i];
              const g = data[i + 1];
              const b = data[i + 2];
              const brightness = (r + g + b) / 3;
              centerBrightness += brightness;
              centerPixels++;
              
              // 计算亮度方差，用于检测对比度
              brightnessVariance += Math.pow(brightness - 127.5, 2);
            }
          }
          
          // 计算边缘区域亮度（用于比较）
          for (let y = 0; y < 10; y++) {
            for (let x = 0; x < 50; x++) {
              const i = (y * 50 + x) * 4;
              const r = data[i];
              const g = data[i + 1];
              const b = data[i + 2];
              const brightness = (r + g + b) / 3;
              edgeBrightness += brightness;
              edgePixels++;
            }
          }
          
          centerBrightness /= centerPixels;
          edgeBrightness /= (edgePixels || 1);
          brightnessVariance /= (centerPixels || 1);
          
          // 更严格的人脸判定标准:
          // 1. 中心区域亮度在合理范围内
          // 2. 中心区域亮度与边缘亮度差异足够大
          // 3. 亮度方差足够大（表示有足够的对比度）
          const brightnessDiff = Math.abs(centerBrightness - edgeBrightness);
          const currentHasFace = 
            centerBrightness > 50 && 
            centerBrightness < 200 && 
            brightnessDiff > 10 && 
            brightnessVariance > 200;
          
          // 更新人脸检测状态
          if (currentHasFace) {
            if (!hasDetectedFace.value) {
              // 首次检测到人脸，开始计时
              hasDetectedFace.value = true;
              faceDetectionStartTime.value = Date.now();
              console.log('检测到人脸，开始计时');
            } else if (autoDetectEnabled.value && faceDetectionStartTime.value) {
              // 已经检测到人脸，计算已检测时间
              const detectionDuration = Date.now() - faceDetectionStartTime.value;
              // 更新进度，最大为100%
              detectionProgress.value = Math.min((detectionDuration / 2000) * 100, 100);
              
              // 如果检测到人脸超过2秒，自动捕获并登录
              if (detectionDuration >= 2000 && !imageCapture.value) {
                captureImage();
                console.log('人脸检测持续2秒，自动捕获图像');
                // 延迟200ms后自动登录，给用户时间看到捕获的图像
                setTimeout(() => {
                  loginWithFace();
                }, 200);
              }
            }
          } else {
            // 丢失人脸检测，重置计时
            if (hasDetectedFace.value) {
              console.log('丢失人脸检测，重置状态');
              hasDetectedFace.value = false;
              faceDetectionStartTime.value = null;
              detectionProgress.value = 0;
            }
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
              
              // 绘制"已检测到人脸"文本
              ctx.font = '16px Arial';
              ctx.fillStyle = '#67C23A';
              ctx.textAlign = 'center';
              ctx.fillText('已检测到人脸', centerX, centerY + size / 2 + 30);
              
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
            } else {
              // 未检测到人脸时，绘制提示框
              const centerX = canvasElement.value.width / 2;
              const centerY = canvasElement.value.height / 2;
              const size = Math.min(canvasElement.value.width, canvasElement.value.height) * 0.5;
              
              // 绘制人脸框 - 灰色
              ctx.strokeStyle = '#909399';
              ctx.lineWidth = 2;
              ctx.beginPath();
              ctx.arc(centerX, centerY, size / 2, 0, Math.PI * 2);
              ctx.stroke();
              
              // 绘制"请将面部对准框内"文本
              ctx.font = '16px Arial';
              ctx.fillStyle = '#909399';
              ctx.textAlign = 'center';
              ctx.fillText('请将面部对准框内', centerX, centerY + size / 2 + 30);
            }
          }
        }
      } catch (error) {
        console.error('检测过程中出错:', error);
      }
    }, 100); // 提高到100ms更新一次，使动画更流畅
  } catch (error) {
    console.error('启动人脸检测失败:', error);
    errorMessage.value = '无法启动人脸检测，请刷新页面后重试';
  }
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
const retakePhoto = async () => {
  imageCapture.value = null
  errorMessage.value = ''
  
  // 确保先关闭现有摄像头再重新启动
  closeCamera()
  
  // 延迟一小段时间，确保摄像头资源被正确释放
  await new Promise(resolve => setTimeout(resolve, 300))
  
  // 重新启动摄像头
  startCamera()
}

// 人脸登录
const loginWithFace = async () => {
  if (!imageCapture.value) {
    errorMessage.value = '请先拍照';
    return;
  }
  
  // 确保真的检测到了人脸
  if (!hasDetectedFace.value) {
    errorMessage.value = '未检测到人脸，请确保面部在摄像头范围内';
    return;
  }
  
  errorMessage.value = ''
  isLoading.value = true
  
  try {
    const result = await userStore.loginWithFaceBase64(imageCapture.value)
    
    // 检查是否是人脸检测失败
    if (result && result.faceDetectionFailed) {
      console.error('后端人脸检测失败:', result.message)
      errorMessage.value = result.message || '人脸检测失败，请确保光线充足并正对摄像头'
      // 显示人脸检测失败的UI状态
      hasDetectedFace.value = false
      isLoading.value = false
      return
    }
    
    // 检查是否返回多个匹配用户
    if (result && result.multipleUsers && result.users) {
      console.log('检测到多个匹配用户:', result.users);
      matchingUsers.value = result.users
      showUserSelection.value = true
      isLoading.value = false
      return
    }
    
    if (result === true) {
      ElMessage.success('人脸识别成功，登录成功')
      closeCamera()
    } else {
      errorMessage.value = userStore.error || '人脸识别失败，请重试'
    }
  } catch (error: any) {
    console.error('人脸登录失败:', error)
    errorMessage.value = error?.response?.data?.message || '人脸登录失败，请稍后重试'
    
    // 重置人脸检测状态
    hasDetectedFace.value = false
  } finally {
    isLoading.value = false
  }
}

// 选择用户登录
const selectUser = async (userId: number) => {
  errorMessage.value = ''
  isLoading.value = true
  
  try {
    const success = await userStore.loginWithSelectedUser(userId)
    
    if (success) {
      ElMessage.success('登录成功')
      closeCamera()
    } else {
      errorMessage.value = userStore.error || '登录失败，请重试'
      showUserSelection.value = false
    }
  } catch (error: any) {
    console.error('选择用户登录失败:', error)
    errorMessage.value = '登录失败，请稍后重试'
    showUserSelection.value = false
  } finally {
    isLoading.value = false
  }
}

// 取消用户选择
const cancelUserSelection = () => {
  showUserSelection.value = false
  matchingUsers.value = []
}

// 获取用户角色文本
const getUserRoleText = (roles: string[]) => {
  if (!roles || roles.length === 0) return '用户'
  
  if (roles.includes('ADMIN') || roles.includes('admin')) {
    return '管理员'
  } else if (roles.includes('SUPERVISOR') || roles.includes('supervisor')) {
    return '教师'
  } else {
    return '学生'
  }
}

// 关闭摄像头
const closeCamera = () => {
  try {
    if (faceDetectionInterval.value !== null) {
      clearInterval(faceDetectionInterval.value)
      faceDetectionInterval.value = null
    }

    // 重置人脸检测状态
    faceDetectionStartTime.value = null;
    detectionProgress.value = 0;
    hasDetectedFace.value = false;

    if (stream.value) {
      stream.value.getTracks().forEach(track => {
        try {
          track.stop()
        } catch (error) {
          console.error('停止视频轨道出错:', error)
        }
      })
      
      // 确保视频元素的 srcObject 被清除
      if (videoElement.value) {
        try {
          videoElement.value.srcObject = null
        } catch (error) {
          console.error('清除视频源出错:', error)
        }
      }
      
      stream.value = null
    }
    
    isCameraStarted.value = false
    
    // 清除可能的错误消息
    if (errorMessage.value.includes('摄像头') || errorMessage.value.includes('视频')) {
      errorMessage.value = ''
    }
  } catch (error) {
    console.error('关闭摄像头时出错:', error)
    // 即使出错，也要确保状态被重置
    isCameraStarted.value = false
    stream.value = null
  }
}

// 返回密码登录
const backToPasswordLogin = () => {
  closeCamera()
  emit('switch-mode', 'password')
}

// 定义事件
const emit = defineEmits(['switch-mode'])

onMounted(async () => {
  try {
    // 确保所有状态被正确初始化
    isCameraStarted.value = false;
    imageCapture.value = null;
    hasDetectedFace.value = false;
    errorMessage.value = '';
    faceDetectionStartTime.value = null;
    detectionProgress.value = 0;
    
    // 加载人脸检测模型，但不自动开启摄像头
    await loadFaceDetectionModels();
    
    // 检查摄像头权限状态
    try {
      // 仅检查权限，不实际保留流
      const permissionStatus = await navigator.permissions.query({ name: 'camera' as PermissionName });
      if (permissionStatus.state === 'denied') {
        errorMessage.value = '摄像头权限已被拒绝，请在浏览器设置中允许访问摄像头';
      }
    } catch (permissionError) {
      // 某些浏览器可能不支持权限查询API，忽略错误
      console.log('无法查询摄像头权限:', permissionError);
    }
  } catch (error) {
    console.error('组件初始化出错:', error);
  }
})

onUnmounted(() => {
  // 确保摄像头资源被释放
  closeCamera();
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

.camera-start-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 200px;
  margin-bottom: 1.5rem;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 20px;
}

.start-camera-btn {
  padding: 15px 30px;
  font-size: 16px;
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

/* 用户选择样式 */
.user-selection-container {
  width: 100%;
  margin-bottom: 1.5rem;
}

.selection-title {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  color: #409EFF;
  text-align: center;
}

.selection-description {
  margin-bottom: 1.5rem;
  color: #606266;
  text-align: center;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 1.5rem;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 5px;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-card:hover {
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  margin-right: 15px;
}

.user-avatar img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 4px;
}

.user-role {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 2px;
}

.user-number {
  color: #606266;
  font-size: 0.9rem;
}

.selection-controls {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}
</style> 