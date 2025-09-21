<template>
  <div class="register-container">
    <div class="register-form">
      <h2>校园二手交易平台</h2>
      <h3>用户注册</h3>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="studentId">学号 *</label>
          <input
            type="text"
            id="studentId"
            v-model="form.studentId"
            placeholder="请输入8-12位数字学号"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="username">用户名 *</label>
          <input
            type="text"
            id="username"
            v-model="form.username"
            placeholder="请输入用户名"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="realName">真实姓名 *</label>
          <input
            type="text"
            id="realName"
            v-model="form.realName"
            placeholder="请输入真实姓名"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码 *</label>
          <input
            type="password"
            id="password"
            v-model="form.password"
            placeholder="至少6位，包含字母和数字"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">确认密码 *</label>
          <input
            type="password"
            id="confirmPassword"
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="phone">手机号</label>
          <input
            type="tel"
            id="phone"
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </div>
        
        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            type="email"
            id="email"
            v-model="form.email"
            placeholder="请输入邮箱地址"
          />
        </div>
        
        <button type="submit" class="register-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      
      <div class="form-footer">
        <p>已有账号？<a href="#" @click="$router.push('/login')">立即登录</a></p>
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()

const form = ref({
  studentId: '',
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleRegister = async () => {
  // 表单验证
  if (!form.value.studentId || !form.value.username || !form.value.realName || !form.value.password) {
    errorMessage.value = '请填写完整的必填信息'
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  
  if (form.value.password.length < 6) {
    errorMessage.value = '密码长度至少6位'
    return
  }
  
  // 学号格式验证
  if (!/^\d{8,12}$/.test(form.value.studentId)) {
    errorMessage.value = '学号必须是8-12位数字'
    return
  }
  
  // 手机号格式验证（如果填写了）
  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    errorMessage.value = '请输入正确的手机号格式'
    return
  }
  
  // 邮箱格式验证（如果填写了）
  if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errorMessage.value = '请输入正确的邮箱格式'
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    // 构建注册数据（不包含confirmPassword）
    const registerData = {
      studentId: form.value.studentId,
      username: form.value.username,
      realName: form.value.realName,
      password: form.value.password,
      phone: form.value.phone || null,
      email: form.value.email || null
    }
    
    console.log('发送注册请求:', registerData)
    const response = await register(registerData)
    console.log('注册响应:', response)
    
    if (response.code === 200) {
      // 注册成功，显示成功消息并跳转到登录页
      successMessage.value = '注册成功！即将跳转到登录页面...'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      // 显示后端返回的具体错误信息
      errorMessage.value = response.message || '注册失败，请稍后重试'
    }
  } catch (error) {
    console.error('注册请求异常:', error)
    errorMessage.value = '网络错误，请检查网络连接后重试'
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-form {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
  backdrop-filter: blur(10px);
}

.register-form h2 {
  text-align: center;
  color: #333;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.register-form h3 {
  text-align: center;
  color: #666;
  margin-bottom: 32px;
  font-size: 16px;
  font-weight: normal;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
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

.register-btn {
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.register-btn:disabled {
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

.success-message {
  margin-top: 16px;
  padding: 12px 16px;
  background-color: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    padding: 16px;
  }
  
  .register-form {
    padding: 32px 24px;
  }
  
  .register-form h2 {
    font-size: 24px;
  }
  
  .form-group {
    margin-bottom: 16px;
  }
}
</style>