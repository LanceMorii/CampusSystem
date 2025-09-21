<template>
  <div class="home-container">
    <div class="header">
      <h1>校园二手交易平台</h1>
      <div class="user-info">
        <span>欢迎，{{ userInfo?.username || '用户' }}</span>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </div>
    
    <div class="content">
      <h2>欢迎来到校园二手交易平台</h2>
      <p>发现校园好物，分享闲置宝贝，让每一件物品都能找到它的新主人。</p>
      
      <!-- 功能导航卡片 -->
      <div class="feature-cards">
        <router-link to="/products" class="feature-card">
          <div class="card-icon">🛍️</div>
          <h3>浏览商品</h3>
          <p>发现校园里的各种好物，找到你需要的宝贝</p>
        </router-link>
        
        <router-link to="/publish" class="feature-card">
          <div class="card-icon">📝</div>
          <h3>发布商品</h3>
          <p>将你的闲置物品分享给需要的同学</p>
        </router-link>
        
        <router-link to="/messages" class="feature-card">
          <div class="card-icon">💬</div>
          <h3>消息中心</h3>
          <p>查看和管理与买家卖家的对话</p>
        </router-link>
        
        <router-link to="/my-products" class="feature-card">
          <div class="card-icon">📦</div>
          <h3>我的商品</h3>
          <p>管理你发布的商品，查看交易状态</p>
        </router-link>
        
        <router-link to="/orders" class="feature-card">
          <div class="card-icon">📋</div>
          <h3>订单管理</h3>
          <p>查看和管理你的买入卖出订单</p>
        </router-link>
      </div>
      
      <div class="user-details" v-if="userInfo">
        <h3>用户信息</h3>
        <p><strong>学号:</strong> {{ userInfo.studentId }}</p>
        <p><strong>用户名:</strong> {{ userInfo.username }}</p>
        <p><strong>真实姓名:</strong> {{ userInfo.realName }}</p>
        <p><strong>手机号:</strong> {{ userInfo.phone || '未设置' }}</p>
        <p><strong>邮箱:</strong> {{ userInfo.email || '未设置' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = ref(null)

onMounted(() => {
  // 检查是否有token
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  
  // 获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo && storedUserInfo !== 'undefined') {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      localStorage.removeItem('userInfo')
    }
  }
})

const logout = () => {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  
  // 跳转到登录页
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header h1 {
  color: #333;
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info span {
  color: #333;
  font-weight: 500;
  font-size: 16px;
}

.logout-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.content {
  padding: 60px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.content h2 {
  color: white;
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.content p {
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 40px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.user-details {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.user-details h3 {
  color: #333;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-details p {
  color: #555;
  font-size: 16px;
  margin-bottom: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.user-details strong {
  color: #333;
  font-weight: 600;
  display: inline-block;
  min-width: 80px;
}

/* 功能导航卡片样式 */
.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin: 40px 0 60px 0;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 32px 24px;
  border-radius: 16px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 1);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.feature-card h3 {
  color: #333;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.feature-card p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
  text-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .header h1 {
    font-size: 24px;
  }
  
  .content {
    padding: 40px 20px;
  }
  
  .content h2 {
    font-size: 28px;
  }
  
  .content p {
    font-size: 16px;
  }
  
  .user-details {
    padding: 24px 20px;
  }
  
  .user-details h3 {
    font-size: 20px;
  }
  
  .user-details p {
    font-size: 14px;
    padding: 10px 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 12px 16px;
  }
  
  .header h1 {
    font-size: 20px;
  }
  
  .user-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .content {
    padding: 32px 16px;
  }
  
  .content h2 {
    font-size: 24px;
  }
  
  .user-details {
    padding: 20px 16px;
  }
}
</style>