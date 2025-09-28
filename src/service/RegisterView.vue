<template>
  <div class="register-container">
    <div class="register-background">
      <div class="background-overlay"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>

    <el-card class="register-card">
      <div class="card-header">
        <div class="logo">
          <span class="logo-icon">❤️</span>
          <h1 class="logo-text">用户注册</h1>
        </div>
        <p class="welcome-text">创建您的账户，开启健康管理之旅</p>
      </div>

      <el-form :model="form" @submit.prevent="handleRegister" class="register-form">
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
              placeholder="请输入密码（至少6位）"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>
        
        <el-form-item>
          <VerifyCode ref="verifyCodeRef" v-model="form.verifyCode" />
        </el-form-item>

        <div class="password-strength">
          <div class="strength-bar" :class="passwordStrength"></div>
          <span class="strength-text">{{ strengthText }}</span>
        </div>

        <el-button
            type="primary"
            native-type="submit"
            :loading="authStore.isLoading"
            size="large"
            class="register-btn"
            :disabled="!isFormValid"
        >
          {{ authStore.isLoading ? '注册中...' : '立即注册' }}
        </el-button>

        <div class="login-link">
          <span>已有账户？</span>
          <el-button link type="primary" @click="goToLogin">
            立即登录
          </el-button>
        </div>
      </el-form>

      <div class="terms-agreement">
        <el-checkbox v-model="agreeTerms">
          我已阅读并同意
        </el-checkbox>
        <el-button link type="primary">《用户协议》</el-button>
        <span>和</span>
        <el-button link type="primary">《隐私政策》</el-button>
      </div>

      <p v-if="authStore.error" class="error-message">
        <el-icon><Warning /></el-icon>
        {{ authStore.error }}
      </p>
    </el-card>

    <div class="register-footer">
      <p>© 2024 健康颐养平台 - 让关爱无处不在</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
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
  confirmPassword: '',
  verifyCode: ''
})

const agreeTerms = ref(false)

// 密码强度计算
const passwordStrength = computed(() => {
  if (!form.value.password) return 'weak'
  if (form.value.password.length < 6) return 'weak'
  if (form.value.password.length >= 8 && /[A-Z]/.test(form.value.password) && /[0-9]/.test(form.value.password)) {
    return 'strong'
  }
  return 'medium'
})

const strengthText = computed(() => {
  switch (passwordStrength.value) {
    case 'weak': return '密码强度：弱'
    case 'medium': return '密码强度：中'
    case 'strong': return '密码强度：强'
    default: return '密码强度：弱'
  }
})

// 表单验证
const isFormValid = computed(() => {
  return form.value.username && form.value.password && form.value.confirmPassword && agreeTerms.value
})

const handleRegister = async () => {
  // 表单验证
  if (!form.value.username || !form.value.password || !form.value.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  if (form.value.password.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return
  }

  if (!agreeTerms.value) {
    ElMessage.warning('请同意用户协议和隐私政策')
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

  const credentials = {
    username: form.value.username,
    password: form.value.password
  }

  const result = await authStore.register(credentials)
  if (result.success) {
    ElMessage.success(result.message || '注册成功')
    // 注册成功后跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } else {
    ElMessage.error(result.error || '注册失败')
    // 注册失败时刷新验证码
    if (verifyCodeRef.value) {
      verifyCodeRef.value.refreshCode()
    }
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
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

.register-background {
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
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.register-card {
  width: 100%;
  max-width: 450px;
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

.register-form {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.2);
}

.password-strength {
  margin: 16px 0;
}

.strength-bar {
  height: 6px;
  border-radius: 3px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.strength-bar.weak {
  width: 33%;
  background: #ff4d4f;
}

.strength-bar.medium {
  width: 66%;
  background: #faad14;
}

.strength-bar.strong {
  width: 100%;
  background: #52c41a;
}

.strength-text {
  font-size: 12px;
  color: #666;
}

.register-btn {
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.terms-agreement {
  text-align: center;
  margin: 20px 0;
  padding: 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  font-size: 12px;
  color: #666;
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

.register-footer {
  margin-top: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  z-index: 2;
  position: relative;
}

@media (max-width: 480px) {
  .register-card {
    margin: 0 20px;
  }

  .logo-text {
    font-size: 24px;
  }

  .terms-agreement {
    font-size: 11px;
  }
}
</style>