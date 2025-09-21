<template>
  <div class="login-container">
    <div class="login-form">
      <h2>校园二手交易平台</h2>
      <h3>用户登录</h3>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="studentId">学号</label>
          <input
            type="text"
            id="studentId"
            v-model="form.studentId"
            placeholder="请输入学号"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="form.password"
            placeholder="请输入密码"
            required
          />
        </div>
        
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <div class="form-footer">
        <p>还没有账号？<a href="#" @click="$router.push('/register')">立即注册</a></p>
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()

const form = ref({
  studentId: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!form.value.studentId || !form.value.password) {
    errorMessage.value = '请填写完整的登录信息'
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const response = await login(form.value)
    
    if (response.code === 200) {
      // 登录成功，保存token和用户信息
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
      
      // 跳转到首页
      router.push('/home')
    } else {
      errorMessage.value = response.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 420px;
  backdrop-filter: blur(10px);
}

.login-form h2 {
  text-align: center;
  color: #333;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.login-form h3 {
  text-align: center;
  color: #666;
  margin-bottom: 32px;
  font-size: 16px;
  font-weight: normal;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #f8f9fa;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.form-footer p {
  color: #666;
  font-size: 14px;
}

.form-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.form-footer a:hover {
  text-decoration: underline;
}

.error-message {
  margin-top: 16px;
  padding: 12px 16px;
  background-color: #fee;
  color: #c53030;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }
  
  .login-form {
    padding: 32px 24px;
  }
  
  .login-form h2 {
    font-size: 24px;
  }
}
</style>