<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
          <el-button v-if="isCurrentUser" type="primary" @click="handleSave" :loading="loading">保存更改</el-button>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像上传 -->
        <div class="avatar-container">
          <el-upload
            v-if="isCurrentUser"
            class="avatar-uploader"
            action="/api/user/avatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :on-error="handleAvatarError"
            :headers="uploadHeaders"
          >
            <el-avatar
              :size="100"
              :src="form.avatar"
              class="avatar"
            >
              {{ form.realName?.charAt(0) || form.nickname?.charAt(0) || '?' }}
            </el-avatar>
            <div class="avatar-upload-tip">点击更换头像</div>
          </el-upload>
          <el-avatar
            v-else
            :size="100"
            :src="form.avatar"
            class="avatar"
          >
            {{ form.realName?.charAt(0) || form.nickname?.charAt(0) || '?' }}
          </el-avatar>
        </div>

        <!-- 个人信息表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="isCurrentUser ? rules : {}"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item v-if="form.username" label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>

          <el-form-item v-if="form.userNumber" label="用户编号">
            <el-input v-model="form.userNumber" disabled />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" :disabled="!isCurrentUser" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" :disabled="!isCurrentUser" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" :disabled="!isCurrentUser" />
          </el-form-item>

          <el-form-item label="电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入电话号码" :disabled="!isCurrentUser" />
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
              :disabled="!isCurrentUser"
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 修改密码卡片 -->
    <el-card v-if="isCurrentUser" class="profile-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>修改密码</h3>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        class="password-form"
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

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleChangePassword"
            :loading="passwordLoading"
          >
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 跑步成就卡片 -->
    <el-card class="profile-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>跑步成就</h3>
        </div>
      </template>

      <AchievementCard :user-id="userId" />
    </el-card>

    <!-- 跑步偏好卡片 -->
    <el-card class="profile-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>跑步偏好</h3>
          <el-button v-if="isCurrentUser" type="primary" @click="handleSaveRunningPreference" :loading="runningLoading">保存偏好</el-button>
        </div>
      </template>

      <el-form
        ref="runningFormRef"
        :model="runningForm"
        label-width="100px"
        class="running-form"
      >
        <el-form-item label="跑步频率">
          <el-select v-model="runningForm.frequency" placeholder="请选择跑步频率" style="width: 100%" :disabled="!isCurrentUser">
            <el-option label="每天" value="每天"></el-option>
            <el-option label="每周3-5次" value="每周3-5次"></el-option>
            <el-option label="每周1-2次" value="每周1-2次"></el-option>
            <el-option label="每月几次" value="每月几次"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="偏好距离">
          <el-select v-model="runningForm.preferredDistance" placeholder="请选择偏好距离" style="width: 100%" :disabled="!isCurrentUser">
            <el-option label="5公里以内" value="5公里以内"></el-option>
            <el-option label="5-10公里" value="5-10公里"></el-option>
            <el-option label="半程马拉松" value="半程马拉松"></el-option>
            <el-option label="全程马拉松" value="全程马拉松"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="跑步配速">
          <el-select v-model="runningForm.pace" placeholder="请选择跑步配速" style="width: 100%" :disabled="!isCurrentUser">
            <el-option label="5分钟/公里以内" value="5分钟/公里以内"></el-option>
            <el-option label="5-6分钟/公里" value="5-6分钟/公里"></el-option>
            <el-option label="6-7分钟/公里" value="6-7分钟/公里"></el-option>
            <el-option label="7分钟/公里以上" value="7分钟/公里以上"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="跑步环境">
          <el-select v-model="runningForm.environment" placeholder="请选择跑步环境" style="width: 100%" :disabled="!isCurrentUser">
            <el-option label="公园" value="公园"></el-option>
            <el-option label="城市道路" value="城市道路"></el-option>
            <el-option label="跑步机" value="跑步机"></el-option>
            <el-option label="田径场" value="田径场"></el-option>
            <el-option label="山地越野" value="山地越野"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 社交资料卡片 -->
    <el-card class="profile-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>社交资料</h3>
          <el-button v-if="isCurrentUser" type="primary" @click="handleSaveMotto" :loading="mottoLoading">保存宣言</el-button>
        </div>
      </template>

      <el-form 
        ref="mottoFormRef"
        :model="mottoForm"
        label-width="100px"
        class="motto-form"
      >
        <el-form-item label="个人宣言">
          <el-input
            v-model="mottoForm.motto"
            type="textarea"
            :rows="4"
            placeholder="请输入您的跑步宣言，分享您的跑步理念和目标"
            maxlength="200"
            show-word-limit
            :disabled="!isCurrentUser"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 角色信息卡片 -->
    <el-card class="profile-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>角色信息</h3>
        </div>
      </template>

      <div class="role-info">
        <el-tag
          v-for="role in userStore.user.roles"
          :key="role"
          :type="role === 'ADMIN' ? 'danger' : 'success'"
          class="role-tag"
        >
          {{ role === 'USER' ? '跑步爱好者' : role === 'ADMIN' ? '管理员' : '教师' }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadProps } from 'element-plus'
import type { UpdateProfileRequest, UpdatePasswordRequest } from '../types/user'
import { getRunningPreference, saveRunningPreference, saveMotto } from '../api/running'
import { getUserInfo, getUserStats } from '../api/user'
import AchievementCard from '../components/AchievementCard.vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)

// 判断是否查看其他用户的个人主页
const userId = computed(() => {
  return route.params.id ? Number(route.params.id) : userStore.userId
})

// 判断是否是当前登录用户
const isCurrentUser = computed(() => {
  return userId.value === userStore.userId
})

// 个人信息表单
const form = reactive<UpdateProfileRequest & { avatar?: string, username?: string, userNumber?: string }>({
  realName: userStore.user.realName || '',
  nickname: userStore.user.nickname || '',
  email: userStore.user.email || '',
  phone: userStore.user.phone || '',
  bio: userStore.user.bio || '',
  avatar: userStore.user.avatar || '',
  username: userStore.user.username || '',
  userNumber: userStore.user.userNumber || ''
})

// 修改密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度应为2到20个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度应为2到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的电话号码', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

// 密码修改表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

// 跑步偏好表单
const runningFormRef = ref<FormInstance>()
const runningLoading = ref(false)
const runningForm = reactive({
  frequency: '',
  preferredDistance: '',
  pace: '',
  environment: ''
})

// 个人宣言表单
const mottoFormRef = ref<FormInstance>()
const mottoLoading = ref(false)
const mottoForm = reactive({
  motto: ''
})

// 页面加载时确保用户信息已更新
onMounted(async () => {
  try {
    if (isCurrentUser.value) {
      // 如果是当前用户，使用store中的用户信息
      const success = await userStore.fetchUserInfo(true)
      
      if (success) {
        console.log('用户信息已更新，学号:', userStore.user.userNumber)
        
        // 更新表单数据
        form.realName = userStore.user.realName || ''
        form.nickname = userStore.user.nickname || ''
        form.email = userStore.user.email || ''
        form.phone = userStore.user.phone || ''
        form.bio = userStore.user.bio || ''
        form.avatar = userStore.user.avatar || ''
        form.username = userStore.user.username || ''
        form.userNumber = userStore.user.userNumber || ''
        
        // 获取用户跑步偏好信息
        await fetchRunningPreference()
      } else {
        // 不要显示错误消息，避免多次显示
        console.warn('获取用户信息未成功，可能需要重新登录')
      }
    } else {
      // 如果查看其他用户，从API获取用户信息
      await fetchUserInfo()
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 只在不是认证错误时显示提示
    if (!(error as any).response || (error as any).response.status !== 401) {
      ElMessage.error('获取用户信息失败，请刷新页面重试')
    }
  }
})

// 获取其他用户信息
const fetchUserInfo = async () => {
  try {
    const response = await getUserInfo(userId.value)
    if (response && response.data) {
      const userData = response.data
      
      // 更新表单数据显示用户信息
      form.realName = userData.realName || ''
      form.nickname = userData.nickname || ''
      form.email = userData.email || ''
      form.phone = userData.phone || ''
      form.bio = userData.bio || ''
      form.avatar = userData.avatar || ''
      form.username = userData.username || ''
      form.userNumber = userData.userNumber || ''
      
      // 获取用户统计信息
      await fetchUserStats()
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    const response = await getUserStats(userId.value)
    if (response && response.data) {
      // 可以在这里设置用户的统计信息到UI上
      console.log('用户统计信息:', response.data)
    }
  } catch (error) {
    console.error('获取用户统计信息失败:', error)
  }
}

// 获取用户跑步偏好
const fetchRunningPreference = async () => {
  try {
    const response = await getRunningPreference()
    
    if (response.code === 200 && response.data) {
      const preference = response.data
      runningForm.frequency = preference.frequency || ''
      runningForm.preferredDistance = preference.preferredDistance || ''
      runningForm.pace = preference.pace || ''
      runningForm.environment = preference.environment || ''
      mottoForm.motto = preference.motto || ''
    }
  } catch (error) {
    console.error('获取跑步偏好失败:', error)
  }
}

// 保存跑步偏好
const handleSaveRunningPreference = async () => {
  runningLoading.value = true
  try {
    const response = await saveRunningPreference(runningForm)
    if (response.code === 200) {
      ElMessage.success('跑步偏好保存成功')
    } else {
      ElMessage.error(response.message || '保存跑步偏好失败')
    }
  } catch (error) {
    console.error('保存跑步偏好失败:', error)
    ElMessage.error('保存跑步偏好失败')
  } finally {
    runningLoading.value = false
  }
}

// 保存个人宣言
const handleSaveMotto = async () => {
  mottoLoading.value = true
  try {
    const response = await saveMotto(mottoForm.motto)
    if (response.code === 200) {
      ElMessage.success('个人宣言保存成功')
    } else {
      ElMessage.error(response.message || '保存个人宣言失败')
    }
  } catch (error) {
    console.error('保存个人宣言失败:', error)
    ElMessage.error('保存个人宣言失败')
  } finally {
    mottoLoading.value = false
  }
}

// 保存个人信息
const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 创建包含所有必要字段的更新对象
        const profileData = {
          realName: form.realName,
          nickname: form.nickname,
          email: form.email,
          phone: form.phone,
          bio: form.bio
          // 注意：avatar 应该已经通过 handleAvatarSuccess 上传并保存
        }
        
        console.log('Submitting profile data:', profileData)
        
        const success = await userStore.updateProfile(profileData)
        if (success) {
          ElMessage.success('个人信息更新成功')
          
          // 刷新表单数据，确保显示最新的用户信息
          form.realName = userStore.user.realName || ''
          form.nickname = userStore.user.nickname || ''
          form.email = userStore.user.email || ''
          form.phone = userStore.user.phone || ''
          form.bio = userStore.user.bio || ''
          form.avatar = userStore.user.avatar || ''
          form.username = userStore.user.username || ''
          form.userNumber = userStore.user.userNumber || ''
        } else {
          ElMessage.error(userStore.error || '更新个人信息失败')
        }
      } catch (error: any) {
        console.error('Failed to save profile:', error)
        const errorMsg = error.response?.data?.message || '更新个人信息失败，请稍后再试'
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    
    // 验证通过后执行
    passwordLoading.value = true
    const data: UpdatePasswordRequest = {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    }
    
    await userStore.changePassword(data)
    ElMessage.success('密码修改成功')
    
    // 清空表单
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.clearValidate()
  } catch (error: any) {
    // 验证失败或API调用失败
    if (error.response) {
      ElMessage.error(error.response?.data?.message || '修改密码失败')
    }
  } finally {
    passwordLoading.value = false
  }
}

// 头像上传相关
const handleAvatarSuccess = (response: any) => {
  if (response && response.url) {
    // 更新表单数据
    form.avatar = response.url
    
    // 更新用户存储中的头像
    userStore.user.avatar = response.url
    
    // 调用updateAvatar方法保存到后端
    userStore.updateAvatar(response.url)
      .then(success => {
        if (success) {
          ElMessage.success('头像上传成功')
        } else {
          ElMessage.error('头像上传但保存失败')
        }
      })
      .catch(error => {
        console.error('Failed to save avatar:', error)
        ElMessage.error('头像上传但保存失败')
      })
  } else {
    ElMessage.error('头像上传响应格式错误')
    console.error('头像上传响应格式错误:', response)
  }
}

const handleAvatarError = (error: any) => {
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败，请稍后再试')
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是JPG或PNG格式!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB!')
    return false
  }
  
  return isJPG || isPNG && isLt2M
}
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  overflow-y: auto; /* 允许页面内容垂直滚动 */
}

.profile-card {
  background-color: #fff;
  border-radius: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.profile-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.avatar-container {
  text-align: center;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar {
  display: block;
  margin-bottom: 10px;
}

.avatar-upload-tip {
  color: #909399;
  font-size: 12px;
}

.profile-form {
  flex: 1;
}

.password-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px 0;
}

.running-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px 0;
}

.motto-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px 0;
}

.role-info {
  padding: 20px 0;
}

.role-tag {
  margin-right: 10px;
}

:deep(.el-upload) {
  cursor: pointer;
}

:deep(.el-upload:hover) {
  border-color: #409EFF;
}

/* 添加媒体查询来确保在小屏幕上的滚动体验 */
@media screen and (max-height: 800px) {
  .profile-container {
    padding-bottom: 50px; /* 增加底部空间，确保滚动时可以完全显示最后一个元素 */
  }
}
</style> 