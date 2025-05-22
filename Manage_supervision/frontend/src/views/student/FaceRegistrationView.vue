<template>
  <div class="face-registration-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>人脸信息管理</span>
        </div>
      </template>
      
      <template v-if="loading">
        <div class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
      </template>
      
      <template v-else>
        <div v-if="hasFaceData" class="status-section">
          <el-result
            icon="success"
            title="人脸信息已注册"
            sub-title="您已完成人脸信息录入，可以使用人脸签到功能。系统要求必须使用人脸进行考勤签到。"
          >
            <template #extra>
              <el-button type="primary" @click="resetFaceData">重新录入人脸</el-button>
            </template>
          </el-result>
        </div>
        
        <div v-else>
          <div class="intro-section">
            <h3>人脸信息录入</h3>
            <p>请完成人脸信息录入，以便使用人脸签到功能。考勤系统<strong>必须使用人脸验证</strong>进行签到，请确保录入清晰的人脸信息。系统将采集多张面部照片以提高识别精度。</p>
            <el-alert
              title="注意"
              type="warning"
              description="请确保摄像头权限已开启，并在光线充足的环境下进行录入"
              :closable="false"
              show-icon
            />
          </div>
          
          <FaceRegistration @registration-completed="onRegistrationCompleted" />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import FaceRegistration from '@/components/FaceRegistration.vue';
import * as userApi from '@/api/user';

const loading = ref(true);
const hasFaceData = ref(false);

// 检查用户是否已有人脸数据
const checkFaceData = async () => {
  try {
    loading.value = true;
    hasFaceData.value = await userApi.hasFaceData();
  } catch (error) {
    console.error('检查人脸数据失败:', error);
    ElMessage.error('无法检查人脸数据状态');
  } finally {
    loading.value = false;
  }
};

// 人脸注册完成回调
const onRegistrationCompleted = () => {
  hasFaceData.value = true;
  ElMessage.success('人脸信息已成功注册');
};

// 重置人脸数据
const resetFaceData = () => {
  ElMessageBox.confirm(
    '重新录入将删除已有的人脸数据，需要重新完成采集过程。是否继续？',
    '确认重置',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里我们通过上传空数据来清除人脸信息
      await userApi.uploadFaceData('');
      hasFaceData.value = false;
      ElMessage.success('人脸数据已重置，请重新录入');
    } catch (error) {
      console.error('重置人脸数据失败:', error);
      ElMessage.error('重置人脸数据失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 组件挂载时检查人脸数据
onMounted(() => {
  checkFaceData();
});
</script>

<style scoped>
.face-registration-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.intro-section {
  margin-bottom: 30px;
}

.intro-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
}

.intro-section p {
  margin-bottom: 20px;
  color: #666;
}

.status-section {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
</style> 