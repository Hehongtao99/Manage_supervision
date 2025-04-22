<template>
  <div class="settings-container">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <h3>账户设置</h3>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="settings-form"
      >
        <el-form-item label="安全等级">
          <el-rate
            v-model="securityLevel"
            :colors="colors"
            :max="3"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}"
          ></el-rate>
          <div class="security-tips">
            <el-alert
              :title="securityTips[securityLevel - 1]"
              :type="securityLevel === 3 ? 'success' : 'warning'"
              show-icon
            ></el-alert>
          </div>
        </el-form-item>

        <el-form-item label="两步验证" prop="twoFactorAuth">
          <el-switch
            v-model="form.twoFactorAuth"
            active-text="已启用"
            inactive-text="已禁用"
            @change="handleTwoFactorChange"
          ></el-switch>
        </el-form-item>

        <el-form-item label="登录通知" prop="loginNotification">
          <el-switch
            v-model="form.loginNotification"
            active-text="已启用"
            inactive-text="已禁用"
          ></el-switch>
        </el-form-item>

        <el-form-item label="账号绑定">
          <div class="binding-list">
            <div class="binding-item">
              <span class="binding-icon">
                <el-icon><Message /></el-icon>
              </span>
              <span class="binding-info">
                <strong>邮箱</strong>
                <span v-if="userStore.user.email">{{ userStore.user.email }}</span>
                <span v-else class="not-bound">未绑定</span>
              </span>
              <el-button
                :type="userStore.user.email ? 'danger' : 'primary'"
                link
                @click="handleEmailBinding"
              >
                {{ userStore.user.email ? '解绑' : '绑定' }}
              </el-button>
            </div>

            <div class="binding-item">
              <span class="binding-icon">
                <el-icon><Iphone /></el-icon>
              </span>
              <span class="binding-info">
                <strong>手机</strong>
                <span v-if="userStore.user.phone">{{ userStore.user.phone }}</span>
                <span v-else class="not-bound">未绑定</span>
              </span>
              <el-button
                :type="userStore.user.phone ? 'danger' : 'primary'"
                link
                @click="handlePhoneBinding"
              >
                {{ userStore.user.phone ? '解绑' : '绑定' }}
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">
            保存设置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 账号注销卡片 -->
    <el-card class="settings-card danger-zone" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>危险区域</h3>
        </div>
      </template>

      <div class="danger-zone-content">
        <div class="danger-item">
          <div class="danger-info">
            <h4>注销账号</h4>
            <p>注销后，您的账号将被永久删除，所有数据将无法恢复。</p>
          </div>
          <el-button type="danger" @click="handleDeactivate">注销账号</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Iphone } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const form = reactive({
  twoFactorAuth: false,
  loginNotification: true
})

// 表单验证规则
const rules = {
  twoFactorAuth: [
    { required: true, message: '请选择是否启用两步验证', trigger: 'change' }
  ],
  loginNotification: [
    { required: true, message: '请选择是否启用登录通知', trigger: 'change' }
  ]
}

// 安全等级相关
const securityLevel = ref(1)
const colors = ['#F56C6C', '#E6A23C', '#67C23A']
const securityTips = [
  '您的账号安全等级较低，建议启用两步验证和登录通知',
  '您的账号安全等级中等，可以考虑绑定手机或邮箱',
  '您的账号安全等级较高，请继续保持'
]

// 计算安全等级
const calculateSecurityLevel = () => {
  let level = 1
  if (form.twoFactorAuth) level++
  if (form.loginNotification) level++
  securityLevel.value = Math.min(level, 3)
}

// 处理两步验证变更
const handleTwoFactorChange = (value: boolean) => {
  if (value) {
    // TODO: 实现两步验证的开启流程
    ElMessage.info('两步验证功能即将推出')
  }
  calculateSecurityLevel()
}

// 处理邮箱绑定
const handleEmailBinding = async () => {
  if (userStore.user.email) {
    // 解绑邮箱
    try {
      await ElMessageBox.confirm('确定要解绑邮箱吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      // TODO: 调用解绑邮箱API
      ElMessage.success('邮箱解绑成功')
    } catch (error) {
      // 用户取消操作
    }
  } else {
    // TODO: 实现邮箱绑定流程
    ElMessage.info('邮箱绑定功能即将推出')
  }
}

// 处理手机绑定
const handlePhoneBinding = async () => {
  if (userStore.user.phone) {
    // 解绑手机
    try {
      await ElMessageBox.confirm('确定要解绑手机吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      // TODO: 调用解绑手机API
      ElMessage.success('手机解绑成功')
    } catch (error) {
      // 用户取消操作
    }
  } else {
    // TODO: 实现手机绑定流程
    ElMessage.info('手机绑定功能即将推出')
  }
}

// 保存设置
const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // TODO: 调用保存设置API
        await new Promise(resolve => setTimeout(resolve, 500)) // 模拟API请求
        ElMessage.success('设置保存成功')
      } catch (error) {
        ElMessage.error('保存设置失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 处理账号注销
const handleDeactivate = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将永久删除您的账号，所有数据将无法恢复。是否继续？',
      '警告',
      {
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    try {
      // TODO: 调用账号注销API
      ElMessage.success('账号已注销')
      await userStore.logout()
    } catch (error) {
      ElMessage.error('账号注销失败')
    }
  } catch (error) {
    // 用户取消操作
  }
}
</script>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.settings-card {
  background-color: #fff;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.settings-form {
  margin-top: 20px;
}

.security-tips {
  margin-top: 10px;
}

.binding-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.binding-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.binding-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #e6e8eb;
  border-radius: 50%;
  margin-right: 12px;
}

.binding-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.not-bound {
  color: #909399;
  font-size: 13px;
}

.danger-zone {
  border: 1px solid #f56c6c;
}

.danger-zone .card-header {
  border-bottom-color: #f56c6c;
}

.danger-zone-content {
  padding: 16px 0;
}

.danger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fff5f5;
  border-radius: 4px;
}

.danger-info h4 {
  margin: 0 0 8px 0;
  color: #f56c6c;
}

.danger-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}
</style> 