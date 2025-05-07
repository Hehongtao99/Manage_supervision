<template>
  <el-popover
    placement="right"
    :width="300"
    trigger="click"
    popper-class="user-info-popover"
  >
    <template #reference>
      <slot></slot>
    </template>
    
    <div class="user-info-content" v-loading="loading">
      <div v-if="userInfo" class="user-info-wrapper">
        <div class="user-info-header">
          <el-avatar :size="60" :src="userInfo.avatar" class="user-avatar">
            {{ userInfo.username?.charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="user-basic-info">
            <div class="user-name">{{ userInfo.nickname || userInfo.realName || userInfo.username }}</div>
            <div class="user-number" v-if="userInfo.userNumber">学号：{{ userInfo.userNumber }}</div>
            <div class="user-roles">
              <el-tag 
                v-for="role in userInfo.roles" 
                :key="role" 
                size="small"
                :type="role === 'ADMIN' ? 'danger' : role === 'SUPERVISOR' ? 'success' : 'info'"
              >
                {{ role === 'USER' ? '学生' : role === 'ADMIN' ? '管理员' : '教师' }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="user-info-body">
          <div class="user-bio" v-if="userInfo.bio">
            <div class="info-title">个人简介</div>
            <div class="info-content">{{ userInfo.bio }}</div>
          </div>
          
          <div class="user-stats">
            <div class="stats-item">
              <div class="stats-value">{{ stats.postCount || 0 }}</div>
              <div class="stats-label">朋友圈</div>
            </div>
            <div class="stats-item">
              <div class="stats-value">{{ stats.runningCount || 0 }}</div>
              <div class="stats-label">跑步记录</div>
            </div>
          </div>
        </div>
        
        <div class="user-info-footer">
          <el-button 
            type="primary" 
            size="small" 
            @click="viewUserProfile"
            :disabled="isCurrentUser"
          >
            查看主页
          </el-button>
        </div>
      </div>
      <div v-else-if="error" class="error-message">
        {{ error }}
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo, getUserStats } from '@/api/user'

const props = defineProps<{
  userId: number
}>()

const userStore = useUserStore()
const router = useRouter()
const userInfo = ref(null)
const loading = ref(false)
const error = ref('')
const stats = ref({
  postCount: 0,
  runningCount: 0
})

// 检查是否是当前用户
const isCurrentUser = computed(() => {
  return props.userId === userStore.userId
})

// 获取用户信息
const fetchUserInfo = async () => {
  if (!props.userId) return
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await getUserInfo(props.userId)
    if (response && response.data) {
      userInfo.value = response.data
      // 获取用户统计信息
      fetchUserStats()
    } else {
      error.value = '获取用户信息失败'
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
    error.value = '获取用户信息失败'
  } finally {
    loading.value = false
  }
}

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    const response = await getUserStats(props.userId)
    if (response && response.data) {
      stats.value = response.data
    }
  } catch (err) {
    console.error('获取用户统计信息失败:', err)
    // 不设置error值，因为获取统计信息失败不影响显示用户基本信息
  }
}

// 查看用户主页
const viewUserProfile = () => {
  router.push(`/user/${props.userId}/profile`)
}

// 监听用户ID变化
watch(() => props.userId, (newVal) => {
  if (newVal) {
    fetchUserInfo()
  }
}, { immediate: true })
</script>

<style scoped>
.user-info-content {
  padding: 10px;
  min-height: 200px;
}

.user-info-wrapper {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-info-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  border: 2px solid #f0f2f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-basic-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}

.user-number {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.user-roles {
  display: flex;
  gap: 5px;
}

.user-info-body {
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}

.user-bio {
  margin-bottom: 15px;
}

.info-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.info-content {
  font-size: 14px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
}

.user-stats {
  display: flex;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  margin-top: 5px;
}

.stats-item {
  flex: 1;
  text-align: center;
  position: relative;
}

.stats-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  height: 60%;
  width: 1px;
  background-color: #ebeef5;
}

.stats-value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.stats-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.user-info-footer {
  display: flex;
  justify-content: center;
  margin-top: 5px;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.error-message {
  text-align: center;
  color: #f56c6c;
  padding: 20px 0;
}
</style> 