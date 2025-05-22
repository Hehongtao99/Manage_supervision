<template>
  <div class="attendance-view">
    <el-card class="attendance-card">
      <template #header>
        <div class="card-header">
          <span class="title">考勤列表</span>
        </div>
      </template>
      
      <!-- 考勤列表 -->
      <el-table :data="attendanceList" v-loading="loading" stripe border>
        <el-table-column prop="title" label="标题" min-width="120"></el-table-column>
        <el-table-column prop="description" label="说明" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column label="有效时间" width="240">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }} ~ {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag type="success" v-if="isCheckedIn(row)">已签到</el-tag>
            <el-tag type="danger" v-else-if="isExpired(row)">未签到</el-tag>
            <el-tag type="warning" v-else>待签到</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              @click="handleCheckIn(row)"
              :disabled="isCheckedIn(row) || isExpired(row) || !isActive(row)">
              签到
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-card class="attendance-card mt-20">
      <template #header>
        <div class="card-header">
          <span class="title">我的签到记录</span>
        </div>
      </template>
      
      <!-- 签到记录列表 -->
      <el-table :data="myRecords" v-loading="recordsLoading" stripe border>
        <el-table-column prop="attendanceTitle" label="考勤标题" min-width="120"></el-table-column>
        <el-table-column label="签到时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.checkInTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="success" v-if="row.status === '正常'">正常</el-tag>
            <el-tag type="warning" v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="120"></el-table-column>
        <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip></el-table-column>
      </el-table>
    </el-card>
    
    <!-- 签到对话框 -->
    <el-dialog 
      title="考勤签到" 
      v-model="checkInDialogVisible"
      width="400px"
      @open="handleDialogOpen"
      destroy-on-close>
      <div v-if="currentAttendance" class="check-in-form">
        <h3>{{ currentAttendance.title }}</h3>
        <p class="attendance-time">
          有效时间: {{ formatDateTime(currentAttendance.startTime) }} ~ {{ formatDateTime(currentAttendance.endTime) }}
        </p>
        <p v-if="currentAttendance.description" class="attendance-desc">
          {{ currentAttendance.description }}
        </p>
        
        <el-form :model="checkInForm" ref="checkInFormRef" label-width="80px">
          <el-form-item label="位置">
            <el-input v-model="checkInForm.location" placeholder="请输入您的位置"></el-input>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="checkInForm.notes" type="textarea" :rows="3" placeholder="可选填写备注信息"></el-input>
          </el-form-item>
          
          <div class="face-checkin-section">
            <div v-if="!hasFaceData" class="no-face-data">
              <el-alert
                title="未检测到人脸数据"
                type="warning"
                description="您尚未录入人脸信息，无法完成签到。请先前往个人信息页面完成人脸录入"
                :closable="false"
                show-icon
              />
              <el-button type="primary" size="small" class="register-face-btn" @click="goToUserProfile">
                去个人中心录入人脸
              </el-button>
            </div>
            <div v-else class="face-capture">
              <div v-if="!cameraActive" class="camera-start">
                <el-button type="primary" @click="startCamera">
                  <el-icon><VideoCamera /></el-icon>
                  启动摄像头
                </el-button>
                <p class="camera-tip">人脸识别签到需要使用摄像头，请确保已授予摄像头权限</p>
              </div>
              <div v-else class="camera-preview" id="camera-container">
                <div class="tracking-box"></div>
                <div v-if="faceDetected" class="face-detected">
                  <el-icon class="face-icon"><Check /></el-icon>
                </div>
                <div v-if="faceDetected && detectionCount > 0" class="auto-signin-progress">
                  <div>{{ detectionCount }}/{{ requiredDetections }}</div>
                  <div class="progress-text">{{ autoSigningIn ? '正在自动签到...' : '人脸识别中...' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-form>
        
        <div class="check-in-time">
          当前时间: {{ currentTime }}
        </div>
        
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            请注意：签到必须通过人脸识别验证才能完成！系统将自动比对您的人脸信息。
          </template>
        </el-alert>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeCheckInDialog">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitCheckIn" 
            :loading="submitting"
            :disabled="!hasFaceData || !cameraActive || !faceDetected"
          >
            人脸验证签到
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import * as attendanceApi from '@/api/attendance';
import * as userApi from '@/api/user';
import { AttendanceDTO, AttendanceRecordDTO } from '@/api/attendance';
import { formatDateTime } from '@/utils/format';
import { Check, VideoCamera } from '@element-plus/icons-vue';
import apiService from '@/api/apiService';

// 路由
const router = useRouter();

// 考勤列表数据
const attendanceList = ref<AttendanceDTO[]>([]);
const loading = ref(false);
const myRecords = ref<AttendanceRecordDTO[]>([]);
const recordsLoading = ref(false);

// 签到对话框数据
const checkInDialogVisible = ref(false);
const currentAttendance = ref<AttendanceDTO | null>(null);
const submitting = ref(false);
const currentTime = ref('');
const timer = ref<number | null>(null);
const checkInFormRef = ref();
const stream = ref<MediaStream | null>(null);
const hasFaceData = ref(false);
const cameraActive = ref(false);
const faceDetected = ref(false);
const faceDetectionTimer = ref<number | null>(null);
const autoSigningIn = ref(false);  // 是否正在自动签到
const detectionCount = ref(0);  // 连续检测计数
const requiredDetections = 2;  // 需要连续检测的次数
const checkInForm = reactive({
  location: '',
  notes: ''
});

// 更新当前时间
const updateCurrentTime = () => {
  currentTime.value = formatDateTime(new Date().toISOString());
};

// 检查用户是否有人脸数据
const checkHasFaceData = async () => {
  try {
    hasFaceData.value = await userApi.hasFaceData();
  } catch (error) {
    console.error('检查人脸数据失败:', error);
    hasFaceData.value = false;
  }
};

// 对话框打开时的处理函数
const handleDialogOpen = async () => {
  console.log('对话框打开');
  
  // 重置视频相关状态
  cameraActive.value = false;
  faceDetected.value = false;
  
  // 如果之前有流，确保它被停止
  stopCamera();
};

// 启动摄像头
const startCamera = async () => {
  console.log('开始启动摄像头...');
  
  // 先设置cameraActive为true，使camera-preview容器显示
  cameraActive.value = true;
  
  // 等待DOM更新，确保容器先显示出来
  await nextTick();
  // 额外添加延迟，确保DOM完全更新
  await new Promise(resolve => setTimeout(resolve, 500));
  
  try {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      console.error('浏览器不支持摄像头访问');
      ElMessage.error('您的浏览器不支持摄像头访问');
      cameraActive.value = false;
      return false;
    }
    
    // 检查容器是否可用
    const container = document.getElementById('camera-container');
    if (!container) {
      console.error('找不到相机容器');
      ElMessage.error('初始化摄像头失败：找不到视频容器');
      cameraActive.value = false;
      return false;
    }
    
    console.log('找到容器:', container);
    
    console.log('请求摄像头权限...');
    stream.value = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user'
      }
    });
    
    console.log('摄像头权限获取成功');
    
    // 创建视频元素
    const video = document.createElement('video');
    video.id = 'faceCamera';
    video.className = 'camera-video';
    video.autoplay = true;
    video.playsInline = true;
    
    // 再次检查容器是否存在 (可能在获取权限期间被销毁)
    if (!document.getElementById('camera-container')) {
      console.error('容器在处理过程中被销毁');
      if (stream.value) {
        stream.value.getTracks().forEach(track => track.stop());
      }
      cameraActive.value = false;
      return false;
    }
    
    // 安全地清空容器
    try {
      while (container.firstChild) {
        container.removeChild(container.firstChild);
      }
    } catch (e) {
      console.error('清空容器失败:', e);
      // 如果清空失败，仍然尝试继续
    }
    
    // 安全地将视频元素添加到容器
    try {
      container.appendChild(video);
    } catch (e) {
      console.error('添加视频元素失败:', e);
      ElMessage.error('初始化摄像头失败: 无法添加视频元素');
      if (stream.value) {
        stream.value.getTracks().forEach(track => track.stop());
      }
      cameraActive.value = false;
      return false;
    }
    
    // 设置视频源
    video.srcObject = stream.value;
    
    // 当元数据加载完成后播放视频
    video.onloadedmetadata = () => {
      console.log('视频元数据已加载');
      
      // 再次检查容器和视频元素是否存在
      const videoEl = document.getElementById('faceCamera');
      const containerEl = document.getElementById('camera-container');
      if (!videoEl || !containerEl) {
        console.error('视频元素或容器在元数据加载后不存在');
        return;
      }
      
      // 尝试播放视频
      video.play().catch(error => {
        console.error('视频播放失败:', error);
        ElMessage.error('视频播放失败，请刷新页面重试');
      });
      
      // 安全地重新添加跟踪框
      try {
        const trackingBox = document.createElement('div');
        trackingBox.className = 'tracking-box';
        containerEl.appendChild(trackingBox);
      } catch (e) {
        console.error('添加跟踪框失败:', e);
        // 错误非致命，继续执行
      }
      
      // 启动人脸检测
      console.log('启动人脸检测...');
      startFaceDetection();
    };
    
    // 视频错误处理
    video.onerror = (e) => {
      console.error('视频元素错误:', e);
      ElMessage.error('视频加载错误，请刷新页面重试');
    };
    
    return true;
  } catch (error) {
    console.error('摄像头访问失败:', error);
    ElMessage.error('无法访问摄像头，请检查权限设置: ' + (error instanceof Error ? error.message : '未知错误'));
    cameraActive.value = false;
    return false;
  }
};

// 开始人脸检测
const startFaceDetection = () => {
  // 先清除可能存在的定时器
  if (faceDetectionTimer.value) {
    clearInterval(faceDetectionTimer.value);
  }
  
  // 重置计数和状态
  detectionCount.value = 0;
  autoSigningIn.value = false;
  
  // 每2秒检测一次人脸
  faceDetectionTimer.value = window.setInterval(async () => {
    if (!cameraActive.value) return;
    
    try {
      // 通过ID获取视频元素
      const video = document.getElementById('faceCamera') as HTMLVideoElement | null;
      
      if (!video) {
        console.error('视频元素不存在');
        return;
      }
      
      if (!video.videoWidth || !video.videoHeight) {
        console.error('视频尺寸未就绪');
        return;
      }
      
      // 捕获当前画面
      const canvas = document.createElement('canvas');
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      
      const ctx = canvas.getContext('2d');
      if (!ctx) return;
      
      try {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const imageData = canvas.toDataURL('image/jpeg', 0.8);
        // 确保提取base64数据部分（去掉前缀）
        const base64Data = imageData.split(',')[1];
        
        console.log('正在进行人脸检测...');
        // 调用人脸检测API
        const response = await apiService.face.detectFace(base64Data);
        console.log('人脸检测结果:', response.data);
        faceDetected.value = response.data.success;
        
        // 如果检测到人脸
        if (faceDetected.value) {
          detectionCount.value++;
          console.log(`连续检测到人脸: ${detectionCount.value}/${requiredDetections}`);
          
          // 连续检测到指定次数的人脸，触发自动签到
          if (detectionCount.value >= requiredDetections && !submitting.value && !autoSigningIn.value) {
            console.log('达到连续检测阈值，开始自动签到');
            // 设置自动签到状态
            autoSigningIn.value = true;
            
            // 延迟一下再签到，给用户一个视觉反馈的时间
            setTimeout(() => {
              submitCheckIn();
            }, 1500);
          }
        } else {
          // 重置连续检测计数
          detectionCount.value = 0;
        }
      } catch (e) {
        console.error('绘制或处理图像失败:', e);
        // 错误非致命，继续下一次检测
        detectionCount.value = 0;
      }
    } catch (error) {
      console.error('人脸检测失败:', error);
      faceDetected.value = false;
      detectionCount.value = 0;
    }
  }, 2000);
};

// 停止摄像头
const stopCamera = () => {
  // 先停止定时器，避免继续访问可能不存在的元素
  if (faceDetectionTimer.value) {
    clearInterval(faceDetectionTimer.value);
    faceDetectionTimer.value = null;
  }
  
  // 停止媒体流
  if (stream.value) {
    try {
      stream.value.getTracks().forEach(track => track.stop());
    } catch (e) {
      console.error('停止媒体流失败:', e);
    }
    stream.value = null;
  }
  
  cameraActive.value = false;
  faceDetected.value = false;
  
  // 尝试移除视频元素
  try {
    const video = document.getElementById('faceCamera');
    if (video) {
      video.srcObject = null;
      if (video.parentNode) {
        video.parentNode.removeChild(video);
      }
    }
  } catch (e) {
    console.error('移除视频元素失败:', e);
    // 非致命错误，继续执行
  }
};

// 前往用户个人信息页录入人脸
const goToUserProfile = () => {
  router.push('/profile');
  closeCheckInDialog();
};

// 关闭签到对话框
const closeCheckInDialog = () => {
  checkInDialogVisible.value = false;
  stopCamera();
  autoSigningIn.value = false;
  detectionCount.value = 0;
  
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
  }
};

// 判断状态
const getStatusType = (attendance: AttendanceDTO) => {
  const now = new Date();
  const startTime = new Date(attendance.startTime);
  const endTime = new Date(attendance.endTime);
  
  if (now < startTime) {
    return 'info';
  } else if (now >= startTime && now <= endTime) {
    return 'success';
  } else {
    return 'danger';
  }
};

const getStatusText = (attendance: AttendanceDTO) => {
  const now = new Date();
  const startTime = new Date(attendance.startTime);
  const endTime = new Date(attendance.endTime);
  
  if (now < startTime) {
    return '未开始';
  } else if (now >= startTime && now <= endTime) {
    return '进行中';
  } else {
    return '已结束';
  }
};

// 判断是否已签到
const isCheckedIn = (attendance: AttendanceDTO) => {
  // 这里根据考勤ID查找签到记录中是否已经有该考勤的记录
  return myRecords.value.some(record => record.attendanceId === attendance.id);
};

// 判断考勤是否已过期
const isExpired = (attendance: AttendanceDTO) => {
  const now = new Date();
  const endTime = new Date(attendance.endTime);
  return now > endTime;
};

// 判断考勤是否处于活跃状态
const isActive = (attendance: AttendanceDTO) => {
  const now = new Date();
  const startTime = new Date(attendance.startTime);
  const endTime = new Date(attendance.endTime);
  return now >= startTime && now <= endTime;
};

// 加载考勤列表
const loadAttendanceList = async () => {
  loading.value = true;
  try {
    const data = await attendanceApi.getCurrentUserAttendances();
    attendanceList.value = data;
  } catch (error) {
    console.error('Failed to load attendance list', error);
    ElMessage.error('加载考勤列表失败');
  } finally {
    loading.value = false;
  }
};

// 加载我的签到记录
const loadMyRecords = async () => {
  recordsLoading.value = true;
  try {
    const data = await attendanceApi.getCurrentUserAttendanceRecords();
    
    // 为每条记录添加考勤标题
    myRecords.value = data.map(record => {
      const attendance = attendanceList.value.find(a => a.id === record.attendanceId);
      return {
        ...record,
        attendanceTitle: attendance ? attendance.title : '未知考勤'
      };
    });
  } catch (error) {
    console.error('Failed to load my records', error);
    ElMessage.error('加载签到记录失败');
  } finally {
    recordsLoading.value = false;
  }
};

// 处理签到按钮点击
const handleCheckIn = async (attendance: AttendanceDTO) => {
  currentAttendance.value = attendance;
  checkInForm.location = '';
  checkInForm.notes = '';
  faceDetected.value = false;
  cameraActive.value = false;
  
  // 检查用户是否有人脸数据
  await checkHasFaceData();
  
  // 打开对话框
  checkInDialogVisible.value = true;
  
  // 开始更新当前时间
  updateCurrentTime();
  timer.value = window.setInterval(updateCurrentTime, 1000);
};

// 捕获当前人脸图像
const captureFace = async () => {
  console.log('准备捕获人脸图像...');
  
  // 通过ID获取视频元素
  const video = document.getElementById('faceCamera') as HTMLVideoElement | null;
  
  if (!video) {
    console.error('视频元素未找到');
    ElMessage.error('摄像头未就绪，请稍候再试');
    return null;
  }
  
  if (!video.videoWidth || !video.videoHeight) {
    console.error('视频尺寸未就绪:', video.videoWidth, video.videoHeight);
    ElMessage.error('摄像头视频流未就绪，请稍候再试');
    return null;
  }
  
  // 创建canvas捕获当前视频帧
  const canvas = document.createElement('canvas');
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    console.error('无法创建2D上下文');
    ElMessage.error('无法创建图像上下文');
    return null;
  }
  
  // 绘制视频帧到canvas
  try {
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    
    // 转换为base64图像
    const dataUrl = canvas.toDataURL('image/jpeg', 0.9);
    console.log('人脸图像已捕获');
    return dataUrl;
  } catch (error) {
    console.error('捕获人脸图像失败:', error);
    ElMessage.error('捕获人脸图像失败: ' + (error instanceof Error ? error.message : '未知错误'));
    return null;
  }
};

// 提交签到
const submitCheckIn = async () => {
  console.log('开始提交签到...');
  if (!currentAttendance.value?.id) {
    console.error('没有选中的考勤');
    autoSigningIn.value = false;
    return;
  }
  
  if (!hasFaceData.value) {
    console.error('用户未录入人脸信息');
    ElMessage.error('您尚未录入人脸信息，请先在个人中心完成人脸信息录入');
    autoSigningIn.value = false;
    return;
  }
  
  if (!cameraActive.value || !faceDetected.value) {
    console.error('摄像头未启动或未检测到人脸');
    ElMessage.error('请先启动摄像头并确保您的人脸被正确识别');
    autoSigningIn.value = false;
    return;
  }
  
  submitting.value = true;
  try {
    const record: AttendanceRecordDTO = {
      location: checkInForm.location,
      notes: checkInForm.notes
    };
    
    console.log('准备捕获人脸图像...');
    // 捕获当前人脸图像
    const faceData = await captureFace();
    if (!faceData) {
      console.error('人脸图像捕获失败');
      submitting.value = false;
      autoSigningIn.value = false;
      return;
    }
    
    console.log('调用人脸签到API...');
    // 使用人脸签到API
    const response = await attendanceApi.faceCheckIn(currentAttendance.value.id, record, faceData);
    
    // 检查响应中是否包含错误信息
    if (response && response.data && response.data.error) {
      console.error('签到失败:', response.data.message);
      const errorMessage = response.data.message || '签到失败';
      
      // 显示错误提示
      ElMessage({
        type: 'error',
        message: errorMessage,
        duration: 5000,
        showClose: true
      });
      
      // 根据错误类型决定是否关闭对话框
      if (response.data.error === 'face_mismatch' || 
          errorMessage.includes('人脸验证失败') || 
          errorMessage.includes('人脸不匹配')) {
        // 人脸不匹配错误，关闭对话框
        closeCheckInDialog();
        
        // 显示详细提示
        ElMessageBox.alert(
          '验证失败可能原因：<br>1. 当前操作人非本人<br>2. 光线不足或过强<br>3. 角度不合适<br>4. 面部表情差异过大<br><br>建议操作：<br>1. 确保光线充足均匀<br>2. 直视摄像头<br>3. 保持自然表情<br>4. 若多次失败，请前往个人中心重新录入人脸',
          '人脸验证未通过',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我知道了'
          }
        );
      } else {
        // 其他错误，也关闭对话框
        closeCheckInDialog();
      }
      
      submitting.value = false;
      autoSigningIn.value = false;
      return;
    }
    
    console.log('签到成功');
    
    // 显示成功消息，根据是自动签到还是手动签到显示不同提示
    if (autoSigningIn.value) {
      ElMessage.success('人脸自动验证签到成功');
    } else {
      ElMessage.success('人脸验证签到成功');
    }
    
    closeCheckInDialog();
    
    // 重新加载数据
    await loadAttendanceList();
    await loadMyRecords();
  } catch (error) {
    console.error('签到失败:', error);
    
    // 处理人脸验证失败的特定提示
    const errorMessage = error instanceof Error ? error.message : '未知错误';
    
    if (errorMessage.includes('人脸验证失败') || errorMessage.includes('人脸验证未通过')) {
      // 使用警告类型而不是错误类型来显示验证失败提示
      ElMessage({
        type: 'warning',
        message: '人脸验证未通过，请确保光线良好并正对摄像头',
        duration: 5000,
        showClose: true
      });
      
      // 关闭对话框
      closeCheckInDialog();
    } else {
      // 处理其他错误
      ElMessage.error('签到失败: ' + errorMessage);
      closeCheckInDialog();
    }
  } finally {
    submitting.value = false;
    autoSigningIn.value = false;
  }
};

// 页面加载时获取数据
onMounted(async () => {
  await loadAttendanceList();
  await loadMyRecords();
});

// 页面卸载时清除定时器
onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
  }
  
  if (faceDetectionTimer.value) {
    clearInterval(faceDetectionTimer.value);
    faceDetectionTimer.value = null;
  }
  
  stopCamera();
});
</script>

<style scoped>
.attendance-view {
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.attendance-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.mt-20 {
  margin-top: 20px;
}

.check-in-form {
  text-align: center;
}

.attendance-time {
  color: #666;
  margin: 10px 0;
}

.attendance-desc {
  color: #666;
  margin-bottom: 20px;
  white-space: pre-line;
}

.check-in-time {
  margin: 20px 0;
  text-align: center;
  font-size: 16px;
  color: #409EFF;
  font-weight: bold;
}

.face-checkin-section {
  margin-top: 15px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 15px;
}

.no-face-data {
  padding: 10px 0;
}

.register-face-btn {
  margin-top: 15px;
}

.camera-start {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.camera-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.camera-preview {
  position: relative;
  width: 100%;
  height: 240px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000;
  overflow: hidden;
  border-radius: 4px;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tracking-box {
  position: absolute;
  width: 150px;
  height: 150px;
  border: 2px solid #67C23A;
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.face-detected {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #67C23A;
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.face-icon {
  font-size: 20px;
}

/* 自动签到进度提示 */
.auto-signin-progress {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
}

.progress-text {
  font-size: 12px;
  margin-top: 2px;
}
</style> 