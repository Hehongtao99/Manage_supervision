<template>
  <div class="register-container">
    <!-- 装饰元素 -->
    <div class="particles-container">
      <div class="particle particle-1"></div>
      <div class="particle particle-2"></div>
      <div class="particle particle-3"></div>
      <div class="particle particle-4"></div>
      <div class="particle particle-5"></div>
      <div class="particle particle-6"></div>
      <div class="particle particle-7"></div>
      <div class="particle particle-8"></div>
    </div>
    
    <div class="animated-bg"></div>

    <div class="register-content">
      <div class="brand-container">
        <img src="../assets/logo.png" alt="Logo" class="brand-logo" />
        <h1 class="brand-title">创建账号</h1>
        <p class="brand-subtitle">加入我们，体验全新的服务</p>
      </div>
      
      <el-card class="register-card">
        <div class="card-header">
          <h2 class="card-title">用户注册</h2>
          <div class="card-subtitle">填写信息完成注册</div>
        </div>

        <el-form :model="form" @submit.prevent="handleRegister" class="register-form">
          <el-form-item class="form-item">
            <label class="input-label">用户名</label>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              prefix-icon="User"
              :size="'large'"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item class="form-item">
            <label class="input-label">密码</label>
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
          
          <el-form-item class="form-item">
            <label class="input-label">确认密码</label>
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
              <span class="btn-text">注册</span>
              <span class="btn-icon">
                <i class="el-icon-right"></i>
              </span>
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
  position: relative;
  overflow: hidden;
  background-color: #050b29;
}

/* 动态背景 */
.animated-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(-45deg, #0b1642, #1a237e, #4527a0, #7b1fa2);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
  z-index: 1;
  opacity: 0.8;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 粒子效果 */
.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 2;
}

.particle {
  position: absolute;
  display: block;
  pointer-events: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 0 10px 2px rgba(255, 255, 255, 0.3);
  animation: float 25s infinite ease-in-out;
}

.particle-1 {
  width: 80px;
  height: 80px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.particle-2 {
  width: 20px;
  height: 20px;
  top: 30%;
  left: 20%;
  animation-delay: 2s;
  animation-duration: 30s;
}

.particle-3 {
  width: 40px;
  height: 40px;
  top: 60%;
  left: 5%;
  animation-delay: 4s;
  animation-duration: 35s;
}

.particle-4 {
  width: 60px;
  height: 60px;
  top: 10%;
  left: 80%;
  animation-delay: 6s;
  animation-duration: 40s;
}

.particle-5 {
  width: 35px;
  height: 35px;
  top: 50%;
  left: 90%;
  animation-delay: 8s;
  animation-duration: 45s;
}

.particle-6 {
  width: 25px;
  height: 25px;
  top: 80%;
  left: 15%;
  animation-delay: 10s;
  animation-duration: 50s;
}

.particle-7 {
  width: 15px;
  height: 15px;
  top: 70%;
  left: 70%;
  animation-delay: 12s;
  animation-duration: 55s;
}

.particle-8 {
  width: 30px;
  height: 30px;
  top: 90%;
  left: 60%;
  animation-delay: 14s;
  animation-duration: 45s;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(1);
    opacity: 0.8;
  }
  25% {
    transform: translate(100px, -100px) rotate(90deg) scale(1.2);
    opacity: 0.6;
  }
  50% {
    transform: translate(200px, 0) rotate(180deg) scale(1);
    opacity: 0.8;
  }
  75% {
    transform: translate(100px, 100px) rotate(270deg) scale(0.8);
    opacity: 0.6;
  }
  100% {
    transform: translate(0, 0) rotate(360deg) scale(1);
    opacity: 0.8;
  }
}

.register-content {
  width: 100%;
  max-width: 1000px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
  position: relative;
  z-index: 10;
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
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-logo {
  width: 120px;
  height: 120px;
  margin-bottom: 1.5rem;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
  transition: all 0.5s ease;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    filter: drop-shadow(0 8px 16px rgba(255, 255, 255, 0.2));
  }
  50% {
    transform: scale(1.05);
    filter: drop-shadow(0 12px 24px rgba(255, 255, 255, 0.4));
  }
  100% {
    transform: scale(1);
    filter: drop-shadow(0 8px 16px rgba(255, 255, 255, 0.2));
  }
}

.brand-logo:hover {
  transform: rotate(5deg) scale(1.1);
}

.brand-title {
  font-size: 3rem;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  letter-spacing: -0.5px;
  background: linear-gradient(to right, #fff, #c1c8ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-subtitle {
  font-size: 1.2rem;
  opacity: 0.95;
  margin: 0.75rem 0 0;
  font-weight: 300;
  letter-spacing: 0.5px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  border-radius: 24px;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.3), 0 0 30px rgba(81, 105, 240, 0.3);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  padding: 40px;
  animation: fadeIn 1s ease;
  border: none;
  transform: translateY(0);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 35px 70px rgba(0, 0, 0, 0.4), 0 0 40px rgba(81, 105, 240, 0.4);
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-title {
  color: #333;
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: -0.5px;
}

.card-subtitle {
  color: #666;
  font-size: 0.95rem;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.register-form {
  margin-top: 0;
}

.form-item {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.terms {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0.5rem 0 1.8rem;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 0.9rem;
}

.terms-link {
  font-weight: 500;
  transition: all 0.3s ease;
  color: #4a5cff;
}

.terms-link:hover {
  color: #7a3bff;
  transform: translateX(3px);
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 1.1rem;
  border-radius: 16px;
  margin: 0;
  background: linear-gradient(135deg, #4a5cff 0%, #7a3bff 100%);
  border: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: all 0.5s ease;
}

.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(74, 92, 255, 0.5);
}

.submit-btn:hover::before {
  left: 100%;
}

.submit-btn:active {
  transform: translateY(0);
}

.btn-text {
  position: relative;
  z-index: 1;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.login-link {
  text-align: center;
  margin-top: 2.5rem;
  color: #666;
  font-size: 0.95rem;
}

.signin-link {
  font-weight: 600;
  margin-left: 0.5rem;
  transition: all 0.3s ease;
  color: #4a5cff;
}

.signin-link:hover {
  color: #7a3bff;
  transform: translateX(3px);
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  height: 52px;
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
  border-color: #4a5cff;
  box-shadow: 0 0 0 4px rgba(74, 92, 255, 0.15);
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
    font-size: 2.5rem;
  }
  
  .brand-subtitle {
    font-size: 1rem;
  }

  .submit-btn {
    height: 48px;
  }

  :deep(.el-input__wrapper) {
    height: 48px;
  }
  
  .card-title {
    font-size: 1.5rem;
  }
}
</style> 