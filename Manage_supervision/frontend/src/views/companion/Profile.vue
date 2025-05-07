<template>
  <div class="companion-profile">
    <el-card class="profile-card">
      <template #header>
        <span>陪玩个人信息</span>
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
              <el-tag type="success">陪玩</el-tag>
              <div class="stat-info" v-if="userStats.playerCount > 0">
                <el-divider/>
                <span class="stat-label">玩家数量:</span>
                <span class="stat-value">{{ userStats.playerCount }}</span>
              </div>
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
                <el-form-item label="陪玩编号" prop="userNumber">
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
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus';
import { useUserStore } from '../../stores/user';
import axios from '../../utils/axios';
import { useRouter } from 'vue-router';
import { useChatStore } from '../../stores/chat';

// 陪玩仪表盘数据接口
interface CompanionDashboardDTO {
  playerCount: number;
  activeToday: number;
  pendingTasks: number;
  weeklyEvents: number;
  playerGameProgresses: any[];
  recentActivities: any[];
}

const userStore = useUserStore();
const activeTab = ref('basic'); // 默认选中基本信息标签页
const statsLoading = ref(false); // 添加加载状态标志
const fileInputRef = ref<HTMLInputElement | null>(null); // 文件输入引用
const router = useRouter();
const chatStore = useChatStore();

// 用户统计数据
const userStats = reactive({
  playerCount: 0 // 初始值设置为0，将通过API获取实际数据
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
      console.log('用户信息已更新，陪玩编号:', userStore.user.userNumber)
      
      // 更新表单数据
      formModel.value.username = userStore.user.username || ''
      formModel.value.userNumber = userStore.user.userNumber || ''
      formModel.value.realName = userStore.user.realName || ''
      formModel.value.nickname = userStore.user.nickname || ''
      formModel.value.email = userStore.user.email || ''
      formModel.value.phone = userStore.user.phone || ''
      formModel.value.bio = userStore.user.bio || ''
      
      // 获取陪玩仪表盘数据，包括真实玩家数量
      await fetchCompanionStats()
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
  
  // 监听新消息通知
  chatStore.onMessage((message) => {
    // 只处理发给当前用户且未读的消息
    if (message.recipientId === userStore.userId && !message.isRead) {
      // 显示通知
      const notification = new Notification('新消息提醒', {
        body: `${message.senderName}: ${message.content}`,
        icon: message.senderAvatar || '/default-avatar.png'
      });
      
      // 点击通知跳转到聊天页面
      notification.onclick = () => {
        router.push('/companion/chat');
        window.focus();
      };
    }
  });
  
  // 请求通知权限
  if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
    await Notification.requestPermission();
  }
})

// 获取陪玩仪表盘数据
const fetchCompanionStats = async () => {
  statsLoading.value = true;
  try {
    const response = await axios.get<CompanionDashboardDTO>('/api/dashboard/supervisor/stats');
    // 更新玩家数量
    userStats.playerCount = response.data.playerCount;
    console.log('获取陪玩玩家数量成功:', response.data.playerCount);
  } catch (error) {
    console.error('获取陪玩数据失败:', error);
    ElMessage.error('获取统计数据失败，请稍后重试');
  } finally {
    statsLoading.value = false;
  }
};

// 处理头像上传
const handleAvatarUpload = () => {
  // 触发文件选择
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

// 文件选择后处理上传
const handleFileSelected = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) {
    return;
  }
  
  const file = target.files[0];
  // 验证文件类型和大小
  const isImage = file.type.startsWith('image/');
  const isValidSize = file.size <= 2 * 1024 * 1024; // 2MB 限制
  
  if (!isImage) {
    ElMessage.error('请选择图片文件');
    return;
  }
  
  if (!isValidSize) {
    ElMessage.error('图片大小不能超过2MB');
    return;
  }
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // 使用现有的/api/user/avatar接口上传头像
    const response = await axios.post('/api/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (response.data && response.data.url) {
      ElMessage.success('头像上传成功');
      // 更新用户头像
      userStore.setUserAvatar(response.data.url);
    } else {
      ElMessage.error('头像上传失败');
    }
  } catch (error) {
    console.error('头像上传失败:', error);
    ElMessage.error('头像上传失败，请稍后重试');
  } finally {
    // 重置文件输入以允许再次选择同一文件
    if (fileInputRef.value) {
      fileInputRef.value.value = '';
    }
  }
};

// 处理基本信息表单提交
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await (formRef.value as any).validate();
    
    const loading = ElLoading.service({
      lock: true,
      text: '保存中...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    try {
      // 构建个人信息数据对象
      const profileData = {
        realName: formModel.value.realName,
        nickname: formModel.value.nickname,
        email: formModel.value.email,
        phone: formModel.value.phone,
        bio: formModel.value.bio
      };
      
      // 调用API更新用户信息
      const success = await userStore.updateProfile(profileData);
      
      if (success) {
        ElMessage.success('个人信息已更新');
      } else {
        ElMessage.error(userStore.error || '更新个人信息失败');
      }
    } finally {
      loading.close();
    }
  } catch (error) {
    console.error('表单验证失败:', error);
    ElMessage.error('请检查表单中的错误');
  }
};

// 处理密码更改
const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return;
  
  try {
    await (passwordFormRef.value as any).validate();
    
    const loading = ElLoading.service({
      lock: true,
      text: '更新密码中...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    try {
      // 使用API更新密码
      await userStore.changePassword({
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword
      });
      
      // 清空密码表单
      passwordForm.value.currentPassword = '';
      passwordForm.value.newPassword = '';
      passwordForm.value.confirmPassword = '';
      
      ElMessage.success('密码已更新，下次登录时生效');
    } catch (error: any) {
      console.error('更新密码失败:', error);
      ElMessage.error(error.response?.data?.message || '密码更新失败，请检查当前密码是否正确');
    } finally {
      loading.close();
    }
  } catch (error) {
    console.error('密码表单验证失败:', error);
    ElMessage.error('请检查密码表单中的错误');
  }
};
</script>

<style scoped>
.companion-profile {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-actions {
  margin-top: 15px;
}

.user-info {
  text-align: center;
}

.user-info h3 {
  margin: 10px 0;
  font-size: 1.2em;
}

.el-tabs {
  margin-top: 10px;
}

.stats-card {
  height: 100%;
}

.stats-item {
  text-align: center;
  padding: 20px 0;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin: 10px 0;
}

.stats-label {
  font-size: 14px;
  color: #606266;
}

.stats-icon {
  font-size: 24px;
  margin-bottom: 10px;
}

.stat-info {
  margin-top: 10px;
  text-align: center;
}

.stat-label {
  color: #606266;
  margin-right: 5px;
}

.stat-value {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
}
</style> 