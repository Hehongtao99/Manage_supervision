<template>
  <div class="face-check-in">
    <el-dialog
      v-model="dialogVisible"
      title="人脸识别签到"
      width="600px"
      :before-close="handleClose"
      :close-on-click-modal="false"
      :close-on-press-escape="!processing"
    >
      <div class="check-in-content">
        <div v-if="!checkInSuccess">
          <div class="camera-container">
            <!-- 视频预览 -->
            <div class="video-container">
              <video ref="videoElement" class="video-preview" autoplay playsinline></video>
              <div v-if="faceDetected" class="face-status face-detected">
                <el-icon><Check /></el-icon>
                <span>已检测到人脸</span>
              </div>
              <div v-else class="face-status face-not-detected">
                <el-icon><Warning /></el-icon>
                <span>未检测到人脸</span>
              </div>
              <div class="face-overlay">
                <div class="face-frame" :class="{ active: faceDetected }"></div>
              </div>
              <div v-if="processing" class="processing-overlay">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <p>正在验证...</p>
              </div>
              <div v-if="autoCapturing" class="auto-capturing-status">
                <el-icon class="pulse-icon"><VideoCamera /></el-icon>
                <span>自动签到中...</span>
              </div>
            </div>
            
            <div class="check-in-form">
              <el-form :model="checkInForm" label-width="80px">
                <el-form-item label="位置">
                  <el-input v-model="checkInForm.location" placeholder="请输入签到位置"></el-input>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="checkInForm.notes" type="textarea" placeholder="可选：添加签到备注"></el-input>
                </el-form-item>
              </el-form>
            </div>
            
            <div class="check-in-controls">
              <el-button 
                type="primary" 
                @click="toggleAutoCapture" 
                :disabled="processing || !cameraActive"
              >
                <el-icon><VideoCamera /></el-icon>
                <span>{{ autoCapturing ? '停止自动签到' : '开始自动签到' }}</span>
              </el-button>
              
              <el-button 
                v-if="!autoCapturing"
                type="success" 
                @click="captureAndVerify" 
                :disabled="processing || !cameraActive"
              >
                <el-icon><Camera /></el-icon>
                <span>手动验证</span>
              </el-button>
              
              <el-button @click="handleClose" :disabled="processing">
                取消
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="check-in-success">
          <el-result
            icon="success"
            title="签到成功"
            sub-title="您已成功完成考勤签到"
          >
            <template #extra>
              <el-button type="primary" @click="handleClose">
                完成
              </el-button>
            </template>
          </el-result>
        </div>
      </div>
      
      <div class="tips-container" v-if="!checkInSuccess">
        <h3>签到提示：</h3>
        <ul>
          <li>请确保光线充足，面部清晰可见</li>
          <li>请直视摄像头，保持自然表情</li>
          <li>开启自动签到后，系统将在识别到您的人脸时自动完成签到</li>
          <li>系统将与您预先注册的人脸信息进行比对，确保本人操作</li>
          <li><strong>为防止冒签，系统采用了高精度人脸比对算法，确保只有本人才能通过验证</strong></li>
          <li>如验证失败，请调整位置、光线或角度后重试</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElLoading, ElNotification, ElMessageBox } from 'element-plus'
import { Check, Warning, Camera, Loading, VideoCamera } from '@element-plus/icons-vue'
import apiService from '@/api/apiService'

export default {
  name: 'FaceCheckIn',
  components: {
    Check,
    Warning,
    Camera,
    Loading,
    VideoCamera
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    attendanceId: {
      type: Number,
      required: true
    },
    attendanceTitle: {
      type: String,
      default: ''
    }
  },
  emits: ['update:visible', 'check-in-success'],
  setup(props, { emit }) {
    const dialogVisible = ref(false)
    const videoElement = ref(null)
    const cameraActive = ref(false)
    const processing = ref(false)
    const faceDetected = ref(false)
    const checkInSuccess = ref(false)
    const autoCapturing = ref(true)
    
    const checkInForm = ref({
      location: '',
      notes: ''
    })
    
    let stream = null
    let detectionInterval = null
    let consecutiveDetections = 0
    
    // 监听props变化，更新对话框状态
    watch(() => props.visible, (newVal) => {
      dialogVisible.value = newVal
      if (newVal) {
        // 打开对话框时启动摄像头
        startCamera()
        // 自动开始捕获
        startAutoCapture()
      } else {
        // 关闭对话框时停止摄像头
        stopCamera()
      }
    })
    
    // 监听对话框状态变化，同步props
    watch(dialogVisible, (newVal) => {
      emit('update:visible', newVal)
    })
    
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
          
          // 定期进行人脸检测
          startFaceDetection()
        }
      } catch (error) {
        console.error('访问摄像头失败:', error)
        ElMessageBox.alert('无法访问摄像头，请确保已授予摄像头权限并且设备有可用的摄像头。', '错误', {
          confirmButtonText: '确定',
          type: 'error'
        })
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
        if (!cameraActive.value || processing.value) {
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
          const response = await apiService.post('/api/face/detect', {
            image: imageData.split(',')[1]
          })
          
          if (response.data.success) {
            faceDetected.value = true
            
            // 自动捕获模式下，跟踪连续检测次数
            if (autoCapturing.value) {
              consecutiveDetections++
              // 降低连续检测次数阈值为2次，使自动识别更快速响应
              if (consecutiveDetections >= 2) {
                await autoVerifyFace(imageData)
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
        message: '自动签到已开启，请面向摄像头保持自然表情',
        type: 'info'
      })
    }
    
    // 停止自动捕获
    const stopAutoCapture = () => {
      autoCapturing.value = false
      consecutiveDetections = 0
    }
    
    // 自动验证人脸并签到
    const autoVerifyFace = async (imageData) => {
      if (processing.value) return
      
      processing.value = true
      
      try {
        // 准备签到数据
        const checkInData = {
          faceData: imageData,
          location: checkInForm.value.location,
          notes: checkInForm.value.notes
        }
        
        // 调用API进行人脸签到
        const response = await apiService.attendance.faceCheckIn(props.attendanceId, checkInData)
        
        if (response && response.data) {
          // 检查返回的数据是否包含错误信息
          if (response.data.error) {
            // 处理错误情况
            console.error('自动人脸验证签到失败:', response.data.message)
            const errorMsg = response.data.message || '人脸验证失败，请确保光线良好并正对摄像头'
            
            // 根据错误类型显示不同提示
            const isFaceMismatch = response.data.error === 'face_mismatch' || 
              errorMsg.includes('人脸验证失败') || errorMsg.includes('人脸不匹配')
            
            // 显示更友好的错误提示
            ElNotification({
              title: isFaceMismatch ? '人脸验证未通过' : '提示',
              message: errorMsg,
              type: isFaceMismatch ? 'error' : 'warning',
              duration: 8000
            })
            
            // 显示帮助提示
            if (isFaceMismatch) {
              ElMessageBox.alert(
                '验证失败可能原因：<br>1. 当前操作人非本人<br>2. 光线不足或过强<br>3. 角度不合适<br>4. 面部表情差异过大<br><br>建议操作：<br>1. 确保光线充足均匀<br>2. 直视摄像头<br>3. 保持自然表情<br>4. 若多次失败，请前往个人中心重新录入人脸',
                '帮助提示',
                {
                  dangerouslyUseHTMLString: true,
                  confirmButtonText: '我知道了'
                }
              ).then(() => {
                // 验证失败后关闭对话框
                dialogVisible.value = false
                stopCamera()
              })
            } else {
              // 其他错误也关闭对话框
              setTimeout(() => {
                dialogVisible.value = false
                stopCamera()
              }, 2000)
            }
            
            // 如果验证失败，停止自动捕获
            stopAutoCapture()
          } else {
            // 签到成功
            checkInSuccess.value = true
            stopAutoCapture()
            
            ElNotification({
              title: '成功',
              message: '考勤签到成功',
              type: 'success'
            })
            
            // 通知父组件签到成功
            emit('check-in-success', response.data)
            
            // 签到成功后自动关闭对话框，延迟1.5秒使用户能看到成功提示
            setTimeout(() => {
              dialogVisible.value = false
              stopCamera()
            }, 1500)
          }
        }
      } catch (error) {
        console.error('自动人脸验证签到请求异常:', error)
        const errorMsg = '签到请求发送失败，请检查网络连接'
        
        ElNotification({
          title: '错误',
          message: errorMsg,
          type: 'error',
          duration: 5000
        })
        
        // 网络错误也关闭对话框
        setTimeout(() => {
          dialogVisible.value = false
          stopCamera()
        }, 2000)
        
        // 如果自动验证失败，停止自动捕获
        stopAutoCapture()
      } finally {
        processing.value = false
      }
    }
    
    // 手动捕获人脸并验证
    const captureAndVerify = async () => {
      if (!cameraActive.value) return
      
      processing.value = true
      const loadingInstance = ElLoading.service({
        target: '.video-container',
        text: '正在进行人脸验证...'
      })
      
      try {
        const canvas = document.createElement('canvas')
        const video = videoElement.value
        
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        
        const ctx = canvas.getContext('2d')
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
        
        const imageData = canvas.toDataURL('image/jpeg', 0.9)
        
        // 准备签到数据
        const checkInData = {
          faceData: imageData,
          location: checkInForm.value.location,
          notes: checkInForm.value.notes
        }
        
        // 调用API进行人脸签到
        const response = await apiService.attendance.faceCheckIn(props.attendanceId, checkInData)
        
        if (response && response.data) {
          // 检查返回的数据是否包含错误信息
          if (response.data.error) {
            // 处理错误情况
            console.error('人脸验证签到失败:', response.data.message)
            const errorMsg = response.data.message || '人脸验证失败，请确保光线良好并正对摄像头'
            
            // 根据错误类型显示不同提示
            const isFaceMismatch = response.data.error === 'face_mismatch' || 
              errorMsg.includes('人脸验证失败') || errorMsg.includes('人脸不匹配')
            
            ElNotification({
              title: isFaceMismatch ? '人脸验证未通过' : '提示',
              message: errorMsg,
              type: isFaceMismatch ? 'error' : 'warning',
              duration: 8000
            })
            
            // 显示帮助提示
            if (isFaceMismatch) {
              ElMessageBox.alert(
                '验证失败可能原因：<br>1. 当前操作人非本人<br>2. 光线不足或过强<br>3. 角度不合适<br>4. 面部表情差异过大<br><br>建议操作：<br>1. 确保光线充足均匀<br>2. 直视摄像头<br>3. 保持自然表情<br>4. 若多次失败，请前往个人中心重新录入人脸',
                '帮助提示',
                {
                  dangerouslyUseHTMLString: true,
                  confirmButtonText: '我知道了'
                }
              ).then(() => {
                // 验证失败后关闭对话框
                dialogVisible.value = false
                stopCamera()
              })
            } else {
              // 其他错误也关闭对话框
              setTimeout(() => {
                dialogVisible.value = false
                stopCamera()
              }, 2000)
            }
          } else {
            // 签到成功
            checkInSuccess.value = true
            ElNotification({
              title: '成功',
              message: '考勤签到成功',
              type: 'success'
            })
            
            // 通知父组件签到成功
            emit('check-in-success', response.data)
            
            // 签到成功后延迟关闭
            setTimeout(() => {
              dialogVisible.value = false
              stopCamera()
            }, 1500)
          }
        }
      } catch (error) {
        console.error('人脸验证签到请求异常:', error)
        const errorMsg = '签到请求发送失败，请检查网络连接'
        
        ElNotification({
          title: '错误',
          message: errorMsg,
          type: 'error',
          duration: 5000
        })
        
        // 网络错误也关闭对话框
        setTimeout(() => {
          dialogVisible.value = false
          stopCamera()
        }, 2000)
      } finally {
        loadingInstance.close()
        processing.value = false
      }
    }
    
    // 关闭对话框
    const handleClose = () => {
      if (processing.value) return
      
      dialogVisible.value = false
      stopCamera()
      
      // 如果已签到成功，重置状态以便下次使用
      if (checkInSuccess.value) {
        setTimeout(() => {
          checkInSuccess.value = false
        }, 300)
      }
    }
    
    // 组件卸载前清理资源
    onUnmounted(() => {
      stopCamera()
    })
    
    return {
      dialogVisible,
      videoElement,
      cameraActive,
      processing,
      faceDetected,
      checkInSuccess,
      checkInForm,
      autoCapturing,
      captureAndVerify,
      handleClose,
      toggleAutoCapture
    }
  }
}
</script>

<style scoped>
.check-in-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.camera-container {
  width: 100%;
}

.video-container {
  position: relative;
  width: 100%;
  max-width: 500px;
  margin: 0 auto 20px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #000;
}

.video-preview {
  width: 100%;
  height: auto;
  display: block;
}

.face-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 5px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  z-index: 5;
}

.face-detected {
  background-color: rgba(103, 194, 58, 0.8);
  color: white;
}

.face-not-detected {
  background-color: rgba(245, 108, 108, 0.8);
  color: white;
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

.face-frame.active {
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
  z-index: 10;
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

.check-in-form {
  margin-bottom: 20px;
  padding: 0 15px;
}

.check-in-controls {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.check-in-success {
  width: 100%;
  padding: 20px;
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
  font-size: 16px;
}

.tips-container ul {
  margin: 0;
  padding-left: 20px;
}

.tips-container li {
  margin-bottom: 8px;
  color: #606266;
}
</style> 