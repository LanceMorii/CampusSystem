<template>
  <div id="app">
    <!-- 对于登录和注册页面，不使用主布局 -->
    <router-view v-if="isAuthPage" />
    <!-- 其他页面使用主布局 -->
    <MainLayout v-else />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/components/Layout/MainLayout.vue'

const route = useRoute()

// 判断是否为认证页面（登录、注册）
const isAuthPage = computed(() => {
  return ['/login', '/register'].includes(route.path)
})
</script>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}

/* Element Plus 样式覆盖 */
.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-main {
  padding: 20px;
}

.el-footer {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
}

/* 自定义工具类 */
.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.full-width {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .el-main {
    padding: 10px;
  }
}
</style>
