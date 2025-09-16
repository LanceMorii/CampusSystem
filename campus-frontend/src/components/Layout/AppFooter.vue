<template>
  <el-footer class="app-footer">
    <div class="footer-content">
      <div class="footer-info">
        <p>&copy; 2024 校园二手交易平台. All rights reserved.</p>
        <p>让闲置物品重新焕发价值，构建绿色校园生活</p>
      </div>
      <div class="footer-links">
        <a href="#" @click.prevent>关于我们</a>
        <a href="#" @click.prevent>联系我们</a>
        <a href="#" @click.prevent>使用条款</a>
        <a href="#" @click.prevent>隐私政策</a>
      </div>
    </div>
  </el-footer>
</template>

<script setup>
// 页脚组件逻辑
</script>

<style scoped>
.app-footer {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>