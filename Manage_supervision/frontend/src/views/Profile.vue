<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
          <el-button type="primary" @click="handleSave" :loading="loading">保存更改</el-button>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像上传 -->
        <div class="avatar-container">
          <el-upload
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
              :src="form.avatar || userStore.user.avatar"
              class="avatar"
            >
              {{ userStore.user.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="avatar-upload-tip">点击更换头像</div>
          </el-upload>
        </div>

        <!-- 个人信息表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input v-model="userStore.user.username" disabled />
          </el-form-item>

          <el-form-item label="用户编号">
            <el-input v-model="userStore.user.userNumber" disabled />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入电话号码" />
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 修改密码卡片 -->
    <el-card class="profile-card" style="margin-top: 20px">
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
          {{ role === 'USER' ? '学生' : role === 'ADMIN' ? '管理员' : '教师' }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadProps } from 'element-plus'
import type { UpdateProfileRequest, UpdatePasswordRequest } from '../types/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)

// 个人信息表单
const form = reactive<UpdateProfileRequest & { avatar?: string }>({
  realName: userStore.user.realName || '',
  nickname: userStore.user.nickname || '',
  email: userStore.user.email || '',
  phone: userStore.user.phone || '',
  bio: userStore.user.bio || '',
  avatar: userStore.user.avatar || ''
})

// 页面加载时确保用户信息已更新
onMounted(async () => {
  try {
    // 强制刷新用户信息
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
    } else {
      // 不要显示错误消息，避免多次显示
      console.warn('获取用户信息未成功，可能需要重新登录')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 只在不是认证错误时显示提示
    if (!(error as any).response || (error as any).response.status !== 401) {
      ElMessage.error('获取用户信息失败，请刷新页面重试')
    }
  }
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
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
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

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        const data: UpdatePasswordRequest = {
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        }
        await userStore.changePassword(data)
        ElMessage.success('密码修改成功')
        passwordForm.currentPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        passwordFormRef.value.clearValidate()
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '修改密码失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
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
</style> 