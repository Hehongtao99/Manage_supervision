<template>
  <div class="register-container">
    <div class="register-content">
      <div class="brand-container">
        <img src="../assets/logo.png" alt="Logo" class="brand-logo" />
        <h1 class="brand-title">创建账号</h1>
        <p class="brand-subtitle">加入我们，体验全新的服务</p>
      </div>
      
      <el-card class="register-card">
        <el-form :model="form" @submit.prevent="handleRegister" class="register-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              prefix-icon="User"
              :size="'large'"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
              prefix-icon="Lock"
              :size="'large'"
              show-password
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="确认密码"
              prefix-icon="Lock"
              :size="'large'"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <div class="terms">
            <el-checkbox v-model="agreeToTerms">我已阅读并同意</el-checkbox>
            <el-link type="primary" :underline="false" class="terms-link">服务条款</el-link>
            和
            <el-link type="primary" :underline="false" class="terms-link">隐私政策</el-link>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              native-type="submit" 
              :loading="loading"
              class="submit-btn"
              :size="'large'"
              :disabled="!agreeToTerms"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-link">
          已有账号？
          <el-link type="primary" @click="$router.push('/login')" :underline="false" class="signin-link">
            立即登录
          </el-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const agreeToTerms = ref(false)

const handleRegister = async () => {
  if (!agreeToTerms.value) {
    ElMessage.warning('请阅读并同意服务条款和隐私政策')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const success = await userStore.register(form.value.username, form.value.password)
    if (success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(userStore.error || '注册失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
  padding: 20px;
  animation: gradientBG 15s ease infinite;
  background-size: 200% 200%;
}

@keyframes gradientBG {
  0% { background-position: 0% 50% }
  50% { background-position: 100% 50% }
  100% { background-position: 0% 50% }
}

.register-content {
  width: 100%;
  max-width: 1000px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.brand-container {
  text-align: center;
  color: white;
  margin-bottom: 1rem;
  animation: fadeInDown 0.8s ease;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-logo {
  width: 100px;
  height: 100px;
  margin-bottom: 1.5rem;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
  transition: transform 0.3s ease;
}

.brand-logo:hover {
  transform: scale(1.05);
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 1.1rem;
  opacity: 0.95;
  margin: 0.75rem 0 0;
  font-weight: 300;
}

.register-card {
  width: 100%;
  max-width: 400px;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px;
  animation: fadeIn 0.8s ease;
  border: none;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.register-form {
  margin-top: 0;
}

.terms {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0.5rem 0 1.5rem;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 0.9rem;
}

.terms-link {
  font-weight: 500;
  transition: color 0.3s ease;
}

.terms-link:hover {
  color: #4facfe;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 1.1rem;
  border-radius: 12px;
  margin: 0;
  background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
  border: none;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(79, 172, 254, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}

.login-link {
  text-align: center;
  margin-top: 2rem;
  color: #666;
  font-size: 0.95rem;
}

.signin-link {
  font-weight: 500;
  margin-left: 0.5rem;
  transition: color 0.3s ease;
}

.signin-link:hover {
  color: #4facfe;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  height: 48px;
  background: #f8fafc;
  box-shadow: none;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  background: #fff;
  border-color: #e2e8f0;
}

:deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #4facfe;
  box-shadow: 0 0 0 4px rgba(79, 172, 254, 0.1);
}

:deep(.el-input__inner) {
  font-size: 1rem;
  color: #334155;
}

:deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

:deep(.el-checkbox__label) {
  font-size: 0.9rem;
  color: #64748b;
}

@media (max-width: 768px) {
  .register-card {
    margin: 0 20px;
    padding: 30px;
  }
  
  .brand-title {
    font-size: 2rem;
  }
  
  .brand-subtitle {
    font-size: 1rem;
  }

  .submit-btn {
    height: 44px;
  }

  :deep(.el-input__wrapper) {
    height: 44px;
  }
}
</style> 