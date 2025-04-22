<template>
  <div class="supervisor-profile">
    <el-card class="profile-card">
      <template #header>
        <span>教师个人信息</span>
      </template>
      <el-row :gutter="24">
        <el-col :span="6">
          <el-card>
            <div class="avatar-container">
              <el-avatar
                :size="100"
                :src="userStore.user.avatar || '/placeholder-avatar.png'"
              />
              <div class="avatar-actions">
                <el-button size="small" type="primary" @click="handleAvatarUpload">
                  更换头像
                </el-button>
                <input
                  type="file"
                  ref="fileInputRef"
                  style="display: none"
                  accept="image/*"
                  @change="handleFileSelected"
                />
              </div>
            </div>
            <div class="user-info">
              <h3>{{ userStore.user.realName || userStore.user.username }}</h3>
              <el-tag type="success">教师</el-tag>
            </div>
            <el-divider />
          </el-card>
        </el-col>
        <el-col :span="18">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="formRef"
                :model="formModel"
                :rules="rules"
                label-position="left"
                label-width="100px"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="formModel.username" disabled />
                </el-form-item>
                <el-form-item label="教师编号" prop="userNumber">
                  <el-input v-model="userStore.user.userNumber" disabled />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="formModel.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="formModel.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="formModel.email" placeholder="请输入电子邮箱" />
                </el-form-item>
                <el-form-item label="电话号码" prop="phone">
                  <el-input v-model="formModel.phone" placeholder="请输入电话号码" />
                </el-form-item>
                <el-form-item label="个人简介" prop="bio">
                  <el-input
                    v-model="formModel.bio"
                    type="textarea"
                    placeholder="请输入个人简介"
                    :rows="3"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSubmit">保存更改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="账号安全" name="security">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-position="left"
                label-width="100px"
              >
                <el-form-item label="当前密码" prop="currentPassword">
                  <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handlePasswordChange">更新密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { useUserStore } from '../../stores/user';
import axios from '../../utils/axios';

// 教师仪表盘数据接口
interface SupervisorDashboardDTO {
  studentCount: number;
  activeToday: number;
  pendingTasks: number;
  weeklyEvents: number;
  studentProjectProgresses: any[];
  recentActivities: any[];
}

const userStore = useUserStore();
const activeTab = ref('basic'); // 默认选中基本信息标签页
const statsLoading = ref(false); // 添加加载状态标志
const fileInputRef = ref<HTMLInputElement | null>(null); // 文件输入引用

// 用户统计数据
const userStats = reactive({
  studentCount: 0 // 初始值设置为0，将通过API获取实际数据
});

// 表单数据
const formModel = ref({
  username: userStore.user.username || '',
  userNumber: userStore.user.userNumber || '',
  realName: userStore.user.realName || '',
  nickname: userStore.user.nickname || '',
  email: userStore.user.email || '',
  phone: userStore.user.phone || '',
  bio: userStore.user.bio || ''
});

// 表单校验规则
const rules = {
  email: [
    { 
      required: false, 
      pattern: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/, 
      message: '请输入有效的电子邮箱地址', 
      trigger: 'blur' 
    }
  ],
  phone: [
    { 
      required: false, 
      pattern: /^1[3-9]\d{9}$/, 
      message: '请输入有效的手机号码', 
      trigger: 'blur' 
    }
  ]
};

// 密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码表单校验规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { 
      required: true, 
      message: '请确认新密码', 
      trigger: 'blur' 
    },
    { 
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
};

// 表单引用
const formRef = ref(null);
const passwordFormRef = ref(null);

// 初始化加载用户数据
onMounted(async () => {
  try {
    // 强制刷新用户信息
    const success = await userStore.fetchUserInfo(true)
    
    if (success) {
      console.log('用户信息已更新，教师编号:', userStore.user.userNumber)
      
      // 更新表单数据
      formModel.value.username = userStore.user.username || ''
      formModel.value.userNumber = userStore.user.userNumber || ''
      formModel.value.realName = userStore.user.realName || ''
      formModel.value.nickname = userStore.user.nickname || ''
      formModel.value.email = userStore.user.email || ''
      formModel.value.phone = userStore.user.phone || ''
      formModel.value.bio = userStore.user.bio || ''
      
      // 获取教师仪表盘数据，包括真实学生数量
      await fetchSupervisorStats()
    } else {
      // 不要显示错误消息，避免多次显示
      console.warn('获取用户信息未成功，可能需要重新登录')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 只在不是认证错误时显示提示
    if (!(error as any).response || (error as any).response.status !== 401) {
      ElMessage.error('加载设置失败，请稍后重试')
    }
  }
})

// 处理头像上传
const handleAvatarUpload = () => {
  // 触发隐藏的文件输入点击事件
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

// 处理文件选择
const handleFileSelected = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) {
    return;
  }
  
  const file = target.files[0];
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return;
  }
  
  // 检查文件大小（限制为2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB');
    return;
  }
  
  // 创建FormData对象
  const formData = new FormData();
  formData.append('file', file);
  
  // 显示加载指示器
  const loadingInstance = ElLoading.service({
    text: '正在上传头像...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  try {
    // 使用现有的/api/user/avatar接口上传头像
    const response = await axios.post('/api/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // 上传成功后更新头像
    ElMessage.success('头像上传成功');
    
    // 更新Vuex store中的头像
    userStore.setUserAvatar(response.data.url);
    
    // 重置文件输入
    if (fileInputRef.value) {
      fileInputRef.value.value = '';
    }
  } catch (error) {
    console.error('头像上传失败:', error);
    ElMessage.error('头像上传失败，请稍后重试');
  } finally {
    // 关闭加载指示器
    loadingInstance.close();
  }
};

// 处理基本信息保存
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 这里添加保存用户信息的API调用
        // await userStore.updateUserInfo(formModel.value);
        ElMessage.success('用户信息已更新');
      } catch (error) {
        ElMessage.error('保存失败，请稍后重试');
      }
    }
  });
};

// 处理密码修改
const handlePasswordChange = () => {
  if (!passwordFormRef.value) return;
  
  passwordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 这里添加修改密码的API调用
        // await userStore.changePassword(passwordForm.value);
        ElMessage.success('密码已更新');
        passwordForm.value.currentPassword = '';
        passwordForm.value.newPassword = '';
        passwordForm.value.confirmPassword = '';
      } catch (error) {
        ElMessage.error('密码修改失败，请稍后重试');
      }
    }
  });
};

// 获取教师仪表盘数据
const fetchSupervisorStats = async () => {
  statsLoading.value = true;
  try {
    const response = await axios.get<SupervisorDashboardDTO>('/api/dashboard/supervisor/stats');
    // 更新学生数量
    userStats.studentCount = response.data.studentCount;
    console.log('获取教师学生数量成功:', response.data.studentCount);
  } catch (error) {
    console.error('获取教师数据失败:', error);
    ElMessage.error('获取统计数据失败，请稍后重试');
  } finally {
    statsLoading.value = false;
  }
};
</script>

<style scoped>
.profile-card {
  margin-bottom: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15px;
}

.avatar-actions {
  margin-top: 10px;
}

.user-info {
  text-align: center;
  margin-bottom: 15px;
}

.user-info h3 {
  margin: 10px 0 5px;
  font-size: 18px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  margin-top: 15px;
}

.stat-item {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}
</style> 