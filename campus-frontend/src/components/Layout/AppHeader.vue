<template>
  <el-header class="app-header">
    <div class="header-content">
      <!-- Logo和标题 -->
      <div class="logo-section">
        <router-link to="/" class="logo-link">
          <el-icon class="logo-icon"><Shop /></el-icon>
          <span class="logo-text">校园二手交易</span>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeIndex"
        class="header-menu"
        mode="horizontal"
        @select="handleSelect"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/products">商品市场</el-menu-item>
        <el-menu-item index="/publish" v-if="userStore.isLoggedIn">发布商品</el-menu-item>
      </el-menu>

      <!-- 用户操作区 -->
      <div class="user-section">
        <!-- 未登录状态 -->
        <div v-if="!userStore.isLoggedIn" class="auth-buttons">
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>

        <!-- 已登录状态 -->
        <div v-else class="user-menu">
          <!-- 消息通知 -->
          <el-badge :value="unreadCount" class="message-badge">
            <el-button 
              :icon="Message" 
              circle 
              @click="$router.push('/messages')"
              title="消息中心"
            />
          </el-badge>

          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userName.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="my-products">
                  <el-icon><Goods /></el-icon>
                  我的商品
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><List /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="admin" v-if="userStore.isAdmin">
                  <el-icon><Setting /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  Message,
  User,
  Goods,
  List,
  Setting,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单项
const activeIndex = computed(() => route.path)

// 未读消息数量（模拟数据）
const unreadCount = ref(0)

// 菜单选择处理
const handleSelect = (key) => {
  router.push(key)
}

// 用户菜单命令处理
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'my-products':
      router.push('/my-products')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // 用户取消
  }
}

// 获取未读消息数量
const fetchUnreadCount = async () => {
  if (userStore.isLoggedIn) {
    try {
      // 这里调用API获取未读消息数量
      // const response = await api.get('/messages/unread-count')
      // unreadCount.value = response.data.data
      unreadCount.value = 0 // 暂时设为0
    } catch (error) {
      console.error('获取未读消息数量失败:', error)
    }
  }
}

onMounted(() => {
  fetchUnreadCount()
})
</script>

<style scoped>
.app-header {
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo-section {
  flex-shrink: 0;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #409eff;
  font-size: 18px;
  font-weight: bold;
}

.logo-icon {
  font-size: 24px;
  margin-right: 8px;
}

.logo-text {
  color: #303133;
}

.header-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
}

.user-section {
  flex-shrink: 0;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 15px;
}

.message-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }
  
  .header-menu {
    margin: 0 20px;
  }
  
  .logo-text {
    display: none;
  }
  
  .username {
    display: none;
  }
}
</style>