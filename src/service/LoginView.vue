<template>
  <div class="login-container">
    <div class="login-background">
      <div class="background-overlay"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>

    <el-card class="login-card">
      <div class="card-header">
        <div class="logo">
          <span class="logo-icon">❤️</span>
          <h1 class="logo-text">健康颐养平台</h1>
        </div>
        <p class="welcome-text">欢迎回来，请登录您的账户</p>
      </div>

      <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>
        
        <el-form-item>
          <VerifyCode ref="verifyCodeRef" v-model="form.verifyCode" />
        </el-form-item>

        <el-button
            type="primary"
            native-type="submit"
            :loading="authStore.isLoading"
            size="large"
            class="login-btn"
        >
          {{ authStore.isLoading ? '登录中...' : '登录' }}
        </el-button>

        <div class="register-link">
          <span>还没有账户？</span>
          <el-button link type="primary" @click="goToRegister">
            立即注册
          </el-button>
        </div>
      </el-form>

      <p v-if="authStore.error" class="error-message">
        <el-icon><Warning /></el-icon>
        {{ authStore.error }}
      </p>
    </el-card>

    <div class="login-footer">
      <p>© 2024 健康颐养平台 - 让关爱无处不在</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import { ElMessage } from 'element-plus'
import { User, Lock, Warning } from '@element-plus/icons-vue'
import VerifyCode from '@/components/VerifyCode.vue'

const router = useRouter()
const authStore = useAuthStore()
const verifyCodeRef = ref(null)

const form = ref({
  username: '',
  password: '',
  verifyCode: ''
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  if (!form.value.verifyCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  if (verifyCodeRef.value && !verifyCodeRef.value.verifyCode()) {
    ElMessage.error('验证码错误')
    verifyCodeRef.value.refreshCode()
    return
  }

  const result = await authStore.login(form.value)
  if (result.success) {
    ElMessage.success('登录成功')
    await router.push('/home')
  } else {
    ElMessage.error(result.error)
    // 登录失败时刷新验证码
    if (verifyCodeRef.value) {
      verifyCodeRef.value.refreshCode()
    }
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 60px;
  height: 60px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 40px;
  height: 40px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  top: 10%;
  right: 10%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-card {
  width: 100%;
  max-width: 400px;
  border: none;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  z-index: 2;
  position: relative;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.logo-text {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-text {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.2);
}

.login-btn {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.error-message {
  color: #f56c6c;
  text-align: center;
  margin: 0;
  padding: 12px;
  background: rgba(245, 108, 108, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-footer {
  margin-top: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  z-index: 2;
  position: relative;
}

@media (max-width: 480px) {
  .login-card {
    margin: 0 20px;
  }

  .logo-text {
    font-size: 24px;
  }
}
</style>