<template>
  <div class="parent-profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
        </div>
      </template>

      <div class="profile-container">
        <div class="avatar-section">
          <el-avatar :size="100" :src="userAvatar || defaultAvatar"></el-avatar>
          <el-button type="primary" class="mt-10" @click="handleAvatarUpload">更换头像</el-button>
        </div>

        <div class="info-section">
          <el-form ref="profileForm" :model="profile" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" disabled></el-input>
            </el-form-item>

            <el-form-item label="真实姓名">
              <el-input v-model="profile.realName"></el-input>
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="profile.nickname"></el-input>
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="profile.email" type="email"></el-input>
            </el-form-item>

            <el-form-item label="联系电话">
              <el-input v-model="profile.phone"></el-input>
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input v-model="profile.bio" type="textarea" :rows="4"></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const defaultAvatar = '/default-avatar.png'
const userAvatar = ref('')

const profile = reactive({
  username: '',
  realName: '',
  nickname: '',
  email: '',
  phone: '',
  bio: ''
})

const loadUserProfile = () => {
  userAvatar.value = userStore.user.avatar || defaultAvatar
  profile.username = userStore.user.username || ''
  profile.realName = userStore.user.realName || ''
  profile.nickname = userStore.user.nickname || ''
  profile.email = userStore.user.email || ''
  profile.phone = userStore.user.phone || ''
  profile.bio = userStore.user.bio || ''
}

const handleAvatarUpload = () => {
  ElMessage.info('头像上传功能尚未实现')
}

const saveProfile = async () => {
  try {
    ElMessage.info('个人资料保存功能尚未实现')
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.parent-profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.profile-container {
  display: flex;
  gap: 30px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-section {
  flex: 1;
}

.mt-10 {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }

  .avatar-section {
    margin-bottom: 20px;
  }
}
</style> 