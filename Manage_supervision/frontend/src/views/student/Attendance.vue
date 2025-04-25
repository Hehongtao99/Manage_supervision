<template>
  <div class="attendance-view">
    <el-card class="box-card main-card">
      <template #header>
        <div class="card-header">
          <h3>考勤签到</h3>
          <el-tag v-if="attendedToday" type="success">今日已签到</el-tag>
          <el-tag v-else type="warning">尚未签到</el-tag>
        </div>
      </template>

      <div v-if="attendedToday" class="attendance-success">
        <el-result
          icon="success"
          title="今日考勤已完成"
          sub-title="您已经完成今日考勤，无需重复签到"
        >
          <template #extra>
            <el-button type="primary" @click="viewAttendanceHistory">查看考勤记录</el-button>
          </template>
        </el-result>
      </div>
      
      <div v-else>
        <!-- 班级选择部分 -->
        <div class="class-selection" v-if="!startRecognition">
          <h3>请选择班级进行考勤</h3>
          <el-form>
            <el-form-item label="选择班级">
              <el-select v-model="selectedClassId" placeholder="请选择班级">
                <el-option
                  v-for="classItem in classes"
                  :key="classItem.id"
                  :label="classItem.className"
                  :value="classItem.id"
                />
              </el-select>
            </el-form-item>
          </el-form>
          <el-button 
            type="primary" 
            :disabled="!selectedClassId" 
            @click="startAttendance"
          >开始考勤</el-button>
        </div>
        
        <!-- 人脸识别部分 -->
        <div v-if="startRecognition" class="face-recognition-container">
          <h3>人脸识别考勤</h3>
          <p class="subtitle">请保持面部在摄像头范围内，系统将自动识别</p>
          
          <div class="video-container">
            <video ref="video" autoplay playsinline class="face-video"></video>
            <canvas ref="canvas" class="face-canvas"></canvas>
            
            <div class="recognition-overlay" :class="{ 'recognizing': isRecognizing, 'recognized': isRecognized }">
              <div v-if="isRecognizing && !isRecognized" class="scanning-animation">
                <el-icon class="scanning-icon"><View /></el-icon>
                <span>正在识别中...</span>
                <div class="progress-container">
                  <el-progress :percentage="recognitionProgress"></el-progress>
                </div>
              </div>
              
              <div v-if="isRecognized" class="success-animation">
                <el-icon class="success-icon"><Check /></el-icon>
                <span>识别成功</span>
              </div>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button type="default" @click="cancelRecognition">取消</el-button>
            <el-button 
              type="primary" 
              :disabled="isRecognizing"
              @click="captureAndSubmit"
            >手动拍照提交</el-button>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 考勤记录部分 -->
    <el-drawer
      v-model="showHistory"
      direction="rtl"
      title="考勤记录"
      size="60%"
    >
      <div class="attendance-history">
        <div class="statistics-cards">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-card shadow="hover" class="statistic-card">
                <h4>本月考勤率</h4>
                <div class="statistic-value">{{ statistics.attendanceRate }}%</div>
                <el-progress 
                  :percentage="parseFloat(statistics.attendanceRate)" 
                  :color="getAttendanceRateColor(statistics.attendanceRate)"
                ></el-progress>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-card shadow="hover" class="statistic-card">
                <h4>考勤天数</h4>
                <div class="statistic-value">{{ statistics.attendedDays }} / {{ statistics.totalDays }}</div>
                <div class="days-progress">
                  <div class="days-completed" :style="{ width: (statistics.attendedDays / statistics.totalDays * 100) + '%' }"></div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-card shadow="hover" class="statistic-card">
                <h4>最近签到</h4>
                <div class="statistic-value" v-if="records.length > 0">
                  {{ formatDate(records[0].checkInTime) }}
                </div>
                <div class="statistic-value" v-else>暂无记录</div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <div class="attendance-records">
          <h3>考勤详细记录</h3>
          <el-table
            :data="records"
            style="width: 100%"
            v-loading="loadingRecords"
          >
            <el-table-column prop="className" label="班级" width="150" />
            <el-table-column prop="checkInTime" label="签到时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.checkInTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="faceRecognized" label="识别状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.faceRecognized ? 'success' : 'danger'">
                  {{ scope.row.faceRecognized ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recognitionDetails" label="识别详情" />
          </el-table>
        </div>
      </div>
    </el-drawer>

    <!-- 提示对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="30%"
    >
      <span>{{ dialogMessage }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleDialogConfirm">
            {{ dialogConfirmText }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { View, Check } from '@element-plus/icons-vue'

export default {
  name: 'StudentAttendance',
  components: {
    View,
    Check
  },
  setup() {
    // 状态变量
    const classes = ref([])
    const selectedClassId = ref(null)
    const startRecognition = ref(false)
    const isRecognizing = ref(false)
    const isRecognized = ref(false)
    const recognitionProgress = ref(0)
    const attendedToday = ref(false)
    const records = ref([])
    const statistics = ref({
      attendedDays: 0,
      totalDays: 30,
      attendanceRate: '0.00'
    })
    const showHistory = ref(false)
    const loadingRecords = ref(false)
    
    // 对话框相关
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const dialogMessage = ref('')
    const dialogConfirmText = ref('确定')
    const dialogAction = ref(null)
    
    // DOM引用
    const video = ref(null)
    const canvas = ref(null)
    
    // 摄像头流对象
    let stream = null
    let recognitionTimer = null
    let recognitionStartTime = 0
    
    // 获取学生班级列表
    const fetchStudentClasses = async () => {
      try {
        const response = await axios.get('/api/student/my-class')
        if (response.data.hasClass) {
          classes.value = response.data.classes
          
          // 如果只有一个班级，自动选中
          if (classes.value.length === 1) {
            selectedClassId.value = classes.value[0].id
          }
        }
      } catch (error) {
        ElMessage.error('获取班级信息失败: ' + error.message)
      }
    }
    
    // 获取考勤记录
    const fetchAttendanceRecords = async () => {
      loadingRecords.value = true
      try {
        const response = await axios.get('/api/attendance/student-records')
        if (response.data.success) {
          records.value = response.data.records
          statistics.value = response.data.statistics
          attendedToday.value = response.data.attendedToday
        }
      } catch (error) {
        ElMessage.error('获取考勤记录失败: ' + error.message)
      } finally {
        loadingRecords.value = false
      }
    }
    
    // 开始考勤处理
    const startAttendance = async () => {
      if (!selectedClassId.value) {
        ElMessage.warning('请先选择班级')
        return
      }
      
      // 确保班级ID是有效值
      if (typeof selectedClassId.value !== 'number' && isNaN(parseInt(selectedClassId.value))) {
        ElMessage.warning('所选班级无效，请重新选择')
        return
      }
      
      try {
        // 检查MediaDevices API的可用性
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
          throw new Error('您的浏览器不支持摄像头访问，请使用Chrome、Firefox或Edge最新版本')
        }
        
        // 先设置显示摄像头界面，触发DOM更新
        startRecognition.value = true
        
        // 等待DOM更新完成
        await nextTick()
        
        // 请求获取摄像头权限
        stream = await navigator.mediaDevices.getUserMedia({ 
          video: { 
            width: { ideal: 640 },
            height: { ideal: 480 },
            facingMode: 'user'
          }, 
          audio: false 
        })
        
        // 检查video元素是否已初始化
        if (!video.value) {
          startRecognition.value = false
          throw new Error('视频元素未初始化，请重试')
        }
        
        // 将摄像头视频流连接到video元素
        video.value.srcObject = stream
        
        // 延迟2秒后自动开始识别
        setTimeout(() => {
          startFaceRecognition()
        }, 2000)
        
      } catch (error) {
        // 出错时重置状态
        startRecognition.value = false
        if (stream) {
          stream.getTracks().forEach(track => track.stop())
          stream = null
        }
        ElMessage.error('无法访问摄像头: ' + error.message)
      }
    }
    
    // 开始人脸识别过程
    const startFaceRecognition = () => {
      isRecognizing.value = true
      recognitionProgress.value = 0
      recognitionStartTime = Date.now()
      
      // 创建计时器，更新进度条
      recognitionTimer = setInterval(() => {
        const elapsed = Date.now() - recognitionStartTime
        recognitionProgress.value = Math.min((elapsed / 2000) * 100, 100)
        
        // 当达到2秒时，完成识别
        if (elapsed >= 2000) {
          clearInterval(recognitionTimer)
          completeFaceRecognition()
        }
      }, 50)
    }
    
    // 完成人脸识别
    const completeFaceRecognition = () => {
      isRecognized.value = true
      
      // 捕获视频画面
      captureVideoFrame()
      
      // 延迟1秒后提交
      setTimeout(() => {
        submitAttendance()
      }, 1000)
    }
    
    // 捕获视频帧
    const captureVideoFrame = () => {
      // 更严格的检查
      if (!canvas.value || !video.value || !video.value.videoWidth || !video.value.videoHeight) {
        console.error('视频或画布元素未准备好')
        return null
      }
      
      try {
        const ctx = canvas.value.getContext('2d')
        if (!ctx) {
          console.error('无法获取canvas上下文')
          return null
        }
        
        canvas.value.width = video.value.videoWidth
        canvas.value.height = video.value.videoHeight
        
        // 绘制视频帧到canvas
        ctx.drawImage(video.value, 0, 0, canvas.value.width, canvas.value.height)
        
        // 获取base64图像数据
        return canvas.value.toDataURL('image/jpeg')
      } catch (error) {
        console.error('捕获视频帧失败:', error)
        return null
      }
    }
    
    // 手动捕获并提交
    const captureAndSubmit = () => {
      if (isRecognizing.value) return
      
      isRecognizing.value = true
      recognitionProgress.value = 100
      isRecognized.value = true
      
      // 捕获并立即提交
      captureVideoFrame()
      submitAttendance()
    }
    
    // 提交考勤
    const submitAttendance = async () => {
      try {
        // 获取base64图像
        const base64Image = captureVideoFrame()
        if (!base64Image) {
          throw new Error('无法捕获摄像头图像')
        }
        
        // 发送到后端
        const params = new URLSearchParams()
        params.append('classId', selectedClassId.value)
        params.append('base64Image', base64Image)
        
        const response = await axios.post('/api/attendance/face-recognition', params)
        
        if (response.data.success) {
          showSuccessDialog('考勤成功', '您已成功完成今日考勤', '查看记录')
          dialogAction.value = 'viewRecords'
          attendedToday.value = true
        } else {
          ElMessage.warning(response.data.message)
          resetFaceRecognition()
        }
      } catch (error) {
        ElMessage.error('提交考勤失败: ' + error.message)
        resetFaceRecognition()
      }
    }
    
    // 重置人脸识别状态
    const resetFaceRecognition = () => {
      isRecognizing.value = false
      isRecognized.value = false
      recognitionProgress.value = 0
      
      if (recognitionTimer) {
        clearInterval(recognitionTimer)
        recognitionTimer = null
      }
    }
    
    // 取消人脸识别
    const cancelRecognition = () => {
      resetFaceRecognition()
      startRecognition.value = false
      
      // 关闭摄像头
      closeCamera()
    }
    
    // 关闭摄像头
    const closeCamera = () => {
      if (stream) {
        stream.getTracks().forEach(track => track.stop())
        stream = null
      }
      
      if (video.value) {
        video.value.srcObject = null
      }
    }
    
    // 查看考勤历史
    const viewAttendanceHistory = () => {
      showHistory.value = true
      fetchAttendanceRecords()
    }
    
    // 显示成功对话框
    const showSuccessDialog = (title, message, confirmText) => {
      dialogTitle.value = title
      dialogMessage.value = message
      dialogConfirmText.value = confirmText
      dialogVisible.value = true
    }
    
    // 处理对话框确认
    const handleDialogConfirm = () => {
      dialogVisible.value = false
      
      if (dialogAction.value === 'viewRecords') {
        viewAttendanceHistory()
      }
      
      // 重置摄像头和识别状态
      cancelRecognition()
    }
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
    
    // 格式化日期
    const formatDate = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }
    
    // 获取考勤率颜色
    const getAttendanceRateColor = (rate) => {
      const rateNum = parseFloat(rate)
      if (rateNum >= 90) return '#67C23A'
      if (rateNum >= 75) return '#E6A23C'
      return '#F56C6C'
    }

    // 组件挂载时
    onMounted(() => {
      // 确保之前的资源被清理
      if (stream) {
        stream.getTracks().forEach(track => track.stop())
        stream = null
      }
      
      if (recognitionTimer) {
        clearInterval(recognitionTimer)
        recognitionTimer = null
      }
      
      fetchStudentClasses()
      fetchAttendanceRecords()
    })
    
    // 组件卸载时
    onUnmounted(() => {
      closeCamera()
      
      if (recognitionTimer) {
        clearInterval(recognitionTimer)
        recognitionTimer = null
      }
    })

    return {
      classes,
      selectedClassId,
      startRecognition,
      isRecognizing,
      isRecognized,
      recognitionProgress,
      attendedToday,
      records,
      statistics,
      showHistory,
      loadingRecords,
      dialogVisible,
      dialogTitle,
      dialogMessage,
      dialogConfirmText,
      video,
      canvas,
      startAttendance,
      captureAndSubmit,
      cancelRecognition,
      viewAttendanceHistory,
      handleDialogConfirm,
      formatDateTime,
      formatDate,
      getAttendanceRateColor
    }
  }
}
</script>

<style scoped>
.attendance-view {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.class-selection {
  text-align: center;
  padding: 20px;
}

.face-recognition-container {
  text-align: center;
  padding: 20px;
}

.subtitle {
  color: #606266;
  margin-bottom: 20px;
}

.video-container {
  position: relative;
  width: 640px;
  height: 480px;
  margin: 0 auto 20px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.face-video, .face-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-canvas {
  display: none;
}

.recognition-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.1);
  z-index: 2;
}

.recognition-overlay.recognizing {
  background-color: rgba(255, 255, 255, 0.3);
}

.recognition-overlay.recognized {
  background-color: rgba(103, 194, 58, 0.3);
}

.scanning-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
  text-shadow: 0 0 5px rgba(0, 0, 0, 0.8);
}

.scanning-icon {
  font-size: 40px;
  margin-bottom: 10px;
  animation: pulse 1.5s infinite;
}

.success-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
  text-shadow: 0 0 5px rgba(0, 0, 0, 0.8);
}

.success-icon {
  font-size: 60px;
  margin-bottom: 10px;
  animation: scale-in 0.5s ease-out;
}

.progress-container {
  width: 80%;
  margin-top: 10px;
}

.action-buttons {
  margin-top: 20px;
}

.attendance-success {
  padding: 20px;
}

.attendance-history {
  padding: 20px;
}

.statistics-cards {
  margin-bottom: 30px;
}

.statistic-card {
  height: 150px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-bottom: 20px;
}

.statistic-card h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
}

.statistic-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #409EFF;
}

.days-progress {
  width: 100%;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}

.days-completed {
  height: 100%;
  background-color: #67C23A;
  transition: width 0.5s ease;
}

.attendance-records {
  margin-top: 20px;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes scale-in {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style> 