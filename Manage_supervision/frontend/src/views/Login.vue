<template>
  <div class="login-container">
    <div class="login-content">
      <div class="brand-container">
        <img src="../assets/logo.png" alt="Logo" class="brand-logo" />
        <h1 class="brand-title">欢迎回来</h1>
        <p class="brand-subtitle">登录以继续使用我们的服务</p>
      </div>
      
      <el-card class="login-card">
        <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
          <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            show-icon
            :closable="true"
            @close="errorMessage = ''"
            style="margin-bottom: 15px;"
          />
          
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
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false" class="forgot-link">忘记密码?</el-link>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              native-type="submit" 
              :loading="loading"
              class="submit-btn"
              :size="'large'"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-link">
          还没有账号?
          <el-link type="primary" @click="$router.push('/register')" :underline="false" class="signup-link">
            立即注册
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
  password: ''
})

const loading = ref(false)
const rememberMe = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  errorMessage.value = ''
  
  if (!form.value.username.trim()) {
    errorMessage.value = '请输入用户名'
    return
  }
  
  if (!form.value.password) {
    errorMessage.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const success = await userStore.login(form.value.username.trim(), form.value.password)
    if (success) {
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      errorMessage.value = userStore.error || '登录失败，请检查用户名和密码'
      console.log('Login failure details:', userStore.error)
    }
  } catch (error: any) {
    console.error('Login exception:', error)
    errorMessage.value = '登录过程中发生错误，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
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

.login-content {
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

.login-card {
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

.login-form {
  margin-top: 0;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0.5rem 0 1.5rem;
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

.register-link {
  text-align: center;
  margin-top: 2rem;
  color: #666;
  font-size: 0.95rem;
}

.signup-link {
  font-weight: 500;
  margin-left: 0.5rem;
  transition: color 0.3s ease;
}

.signup-link:hover {
  color: #4facfe;
}

.forgot-link {
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.forgot-link:hover {
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
  .login-card {
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