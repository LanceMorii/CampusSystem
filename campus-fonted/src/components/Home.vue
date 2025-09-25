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
        
        <router-link to="/messages" class="feature-card message-card">
          <div class="card-icon">💬</div>
          <h3>消息中心</h3>
          <p>查看和管理与买家卖家的对话</p>
          <UnreadMessageIndicator :show-count="true" />
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
        
        <router-link to="/favorites" class="feature-card">
          <div class="card-icon">❤️</div>
          <h3>我的收藏</h3>
          <p>查看收藏的商品</p>
        </router-link>
      </div>
      
      <div class="user-details" v-if="userInfo">
        <div class="user-card-header">
          <div class="user-avatar">
            <img :src="userInfo.avatar || '/logo.jpg'" alt="用户头像" />
          </div>
          <div class="user-title">
            <h3>个人信息</h3>
            <p class="user-subtitle">{{ userInfo.realName || userInfo.username }}</p>
          </div>
        </div>
        
        <!-- 第一行：基本信息 -->
        <div class="user-info-row">
          <div class="info-item" style="animation-delay: 0.1s">
            <div class="info-icon">🎓</div>
            <div class="info-content">
              <span class="info-label">学号</span>
              <span class="info-value student-id">{{ userInfo.studentId }}</span>
            </div>
          </div>
          
          <div class="info-item" style="animation-delay: 0.2s">
            <div class="info-icon">👤</div>
            <div class="info-content">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userInfo.username }}</span>
            </div>
          </div>
          
          <div class="info-item" style="animation-delay: 0.3s">
            <div class="info-icon">📱</div>
            <div class="info-content">
              <span class="info-label">手机号</span>
              <span class="info-value" :class="{ 'not-set': !userInfo.phone }">
                {{ userInfo.phone || '未设置' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 第二行：扩展信息 -->
        <div class="user-info-row">
          <div class="info-item" style="animation-delay: 0.4s">
            <div class="info-icon">📧</div>
            <div class="info-content">
              <span class="info-label">邮箱</span>
              <span class="info-value" :class="{ 'not-set': !userInfo.email }">
                {{ userInfo.email || '未设置' }}
              </span>
            </div>
          </div>
          
          <div class="info-item" style="animation-delay: 0.5s">
            <div class="info-icon">⏰</div>
            <div class="info-content">
              <span class="info-label">注册时长</span>
              <span class="info-value">{{ registrationDuration }}</span>
            </div>
          </div>
          
          <div class="info-item" style="animation-delay: 0.6s">
            <div class="info-icon">📊</div>
            <div class="info-content">
              <div class="trade-header">
                <span class="info-label">交易统计</span>
                <button class="toggle-amount-btn" @click="toggleTradeAmount" :title="showTradeAmount ? '隐藏金额' : '显示金额'">
                  <span class="eye-icon">{{ showTradeAmount ? '👁️' : '👁️‍🗨️' }}</span>
                </button>
              </div>
              <div class="trade-stats">
                <div class="trade-count">{{ tradeStats.successCount }}次成功交易</div>
                <div class="trade-amount" v-if="showTradeAmount">
                  总金额：¥{{ tradeStats.totalAmount.toFixed(2) }}
                </div>
                <div class="trade-amount-hidden" v-else>
                  总金额：****
                </div>
              </div>
            </div>
          </div>
        </div>


      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import UnreadMessageIndicator from './UnreadMessageIndicator.vue'
import { get } from '../api/request'

const router = useRouter()
const userInfo = ref(null)
const showTradeAmount = ref(false)
const registrationTime = ref(null)
const currentTime = ref(new Date())

// 交易统计数据（从API获取真实数据）
const tradeStats = ref({
  successCount: 0,
  totalAmount: 0
})

// 计算注册时长（精确到秒）
const registrationDuration = computed(() => {
  if (!registrationTime.value) return '未知'
  
  const now = currentTime.value
  const regTime = new Date(registrationTime.value)
  const diffMs = now - regTime
  
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)
  const diffMonths = Math.floor(diffDays / 30)
  const diffYears = Math.floor(diffDays / 365)
  
  if (diffYears > 0) {
    const remainingMonths = Math.floor((diffDays % 365) / 30)
    const remainingDays = diffDays % 30
    return `${diffYears}年${remainingMonths}个月${remainingDays}天`
  } else if (diffMonths > 0) {
    const remainingDays = diffDays % 30
    const remainingHours = diffHours % 24
    return `${diffMonths}个月${remainingDays}天${remainingHours}小时`
  } else if (diffDays > 0) {
    const remainingHours = diffHours % 24
    const remainingMinutes = diffMinutes % 60
    return `${diffDays}天${remainingHours}小时${remainingMinutes}分钟`
  } else if (diffHours > 0) {
    const remainingMinutes = diffMinutes % 60
    const remainingSeconds = diffSeconds % 60
    return `${diffHours}小时${remainingMinutes}分钟${remainingSeconds}秒`
  } else if (diffMinutes > 0) {
    const remainingSeconds = diffSeconds % 60
    return `${diffMinutes}分钟${remainingSeconds}秒`
  } else if (diffSeconds > 0) {
    return `${diffSeconds}秒`
  } else {
    return '刚刚注册'
  }
})

let timeUpdateInterval = null

onMounted(async () => {
  // 检查是否有token
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  
  // 获取用户完整数据和统计信息
  await fetchUserData()
  
  // 每秒更新一次时间，实现精确到秒的显示
  timeUpdateInterval = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }
})

// 获取用户完整信息和统计数据
const fetchUserData = async () => {
  try {
    // 获取用户详细信息
    const profileResponse = await get('/auth/profile')
    if (profileResponse && profileResponse.code === 200) {
      userInfo.value = profileResponse.data
      // 设置注册时间
      if (userInfo.value.createTime) {
        registrationTime.value = userInfo.value.createTime
      }
      // 更新localStorage中的用户信息
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    // 获取用户统计数据
    const statsResponse = await get('/auth/stats')
    if (statsResponse && statsResponse.code === 200) {
      const stats = statsResponse.data
      tradeStats.value = {
        successCount: stats.successCount || 0,
        totalAmount: stats.totalAmount || 0
      }
    } else {
      // API调用失败时设置默认值
      tradeStats.value = {
        successCount: 0,
        totalAmount: 0
      }
    }
  } catch (error) {
    console.error('获取用户数据失败:', error)
    // 如果API调用失败，使用localStorage中的数据作为备用
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo && storedUserInfo !== 'undefined') {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        if (userInfo.value.createTime) {
          registrationTime.value = userInfo.value.createTime
        }
      } catch (parseError) {
        console.error('解析用户信息失败:', parseError)
      }
    }
    
    // 设置默认统计数据
    tradeStats.value = {
      successCount: 0,
      totalAmount: 0
    }
  }
}

// 切换交易金额显示状态
const toggleTradeAmount = () => {
  showTradeAmount.value = !showTradeAmount.value
}

// 刷新统计数据
const refreshStats = async () => {
  try {
    const statsResponse = await get('/auth/stats')
    if (statsResponse && statsResponse.code === 200) {
      const stats = statsResponse.data
      tradeStats.value = {
        successCount: stats.successCount || 0,
        totalAmount: stats.totalAmount || 0
      }
    }
  } catch (error) {
    console.error('刷新统计数据失败:', error)
  }
}



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
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.user-card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
  position: relative;
}

.user-avatar:hover {
  transform: scale(1.05);
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.user-avatar::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.user-avatar:hover::before {
  opacity: 0.2;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-title h3 {
  color: #333;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-subtitle {
  color: #666;
  background:  rgba(102, 126, 234, 0.4);
  font-size: 16px;
  margin: 0;
  font-weight: 500;
}

.user-info-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.user-info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  animation: slideInLeft 0.6s ease-out both;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
}

.info-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.info-value.not-set {
  color: #999;
  font-style: italic;
  font-weight: 400;
}

.student-id {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
  letter-spacing: 1px;
}

.trade-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.trade-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trade-count {
  font-size: 14px;
  color: #52c41a;
  font-weight: 600;
}

.trade-amount,
.trade-amount-hidden {
  font-size: 13px;
  color: #fa8c16;
  font-weight: 500;
}

.trade-amount-hidden {
  color: #999;
  font-style: italic;
}

.toggle-amount-btn {
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-size: 12px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-amount-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.4);
  transform: scale(1.05);
}

.eye-icon {
  display: inline-block;
  transition: transform 0.2s ease;
  font-size: 14px;
}

.toggle-amount-btn:hover .eye-icon {
  transform: scale(1.1);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.message-stats {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.message-total {
  font-size: 14px;
  color: #1890ff;
  font-weight: 600;
}

.message-unread {
  font-size: 12px;
  color: #ff4d4f;
  font-weight: 500;
}

.message-all-read {
  font-size: 12px;
  color: #52c41a;
  font-weight: 500;
}

.user-level {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.level-badge {
  font-size: 14px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s ease;
}

.level-diamond {
  background: linear-gradient(135deg, #b8860b, #ffd700);
  color: white;
  box-shadow: 0 2px 8px rgba(184, 134, 11, 0.3);
}

.level-gold {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #8b4513;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.level-silver {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #666;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.3);
}

.level-bronze {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: white;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
}

.level-newbie {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.level-desc {
  font-size: 11px;
  color: #999;
  font-style: italic;
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

.message-card {
  position: relative;
}

/* 响应式设计 */
@media (max-width: 1024px) and (min-width: 769px) {
  .user-info-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 18px;
  }
  
  .info-item {
    padding: 18px;
  }
  
  .student-id {
    font-size: 17px;
  }
}

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
    padding: 24px 20px;
  }
  
  .user-card-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    margin-bottom: 24px;
  }
  
  .user-avatar {
    width: 70px;
    height: 70px;
  }
  
  .user-title h3 {
    font-size: 24px;
  }
  
  .user-info-row {
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 16px;
  }
  
  .info-item {
    padding: 16px;
  }
  
  .info-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .user-details {
    padding: 20px 16px;
  }
  
  .user-card-header {
    margin-bottom: 20px;
    padding-bottom: 16px;
  }
  
  .user-avatar {
    width: 60px;
    height: 60px;
    border-width: 3px;
  }
  
  .user-title h3 {
    font-size: 22px;
  }
  
  .user-subtitle {
    font-size: 14px;
  }
  
  .info-item {
    padding: 14px;
    gap: 12px;
  }
  
  .info-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .info-label {
    font-size: 12px;
  }
  
  .info-value {
    font-size: 15px;
  }
  
  .student-id {
    font-size: 16px;
  }
  
  .trade-header {
    margin-bottom: 6px;
  }
  
  .trade-stats {
    gap: 2px;
  }
  
  .trade-count {
    font-size: 12px;
  }
  
  .trade-amount,
  .trade-amount-hidden {
    font-size: 11px;
  }
  
  .toggle-amount-btn {
    font-size: 10px;
    width: 24px;
    height: 24px;
    padding: 3px 4px;
  }
  
  .eye-icon {
    font-size: 12px;
  }
}
</style>