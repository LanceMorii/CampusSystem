<template>
  <div class="main-layout">
    <el-container>
      <!-- 头部 -->
      <AppHeader />
      
      <!-- 主体内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
      
      <!-- 底部 -->
      <AppFooter />
    </el-container>
  </div>
</template>

<script setup>
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.main-content {
  min-height: calc(100vh - 120px);
  padding: 20px;
  background-color: #f5f5f5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
}
</style>