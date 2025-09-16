<template>
  <el-card class="product-card" @click="goToDetail">
    <!-- 商品图片 -->
    <div class="product-image">
      <el-image
        :src="productImage"
        :alt="product.title"
        fit="cover"
        class="image"
        lazy
      >
        <template #error>
          <div class="image-error">
            <el-icon><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </template>
      </el-image>
      
      <!-- 商品状态标签 -->
      <div class="status-tag" v-if="statusText">
        <el-tag :type="statusType" size="small">{{ statusText }}</el-tag>
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="product-info">
      <h4 class="product-title" :title="product.title">{{ product.title }}</h4>
      
      <div class="product-price">
        <span class="current-price">¥{{ product.price }}</span>
        <span class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
          ¥{{ product.originalPrice }}
        </span>
      </div>
      
      <div class="product-meta">
        <span class="condition">{{ product.condition }}</span>
        <span class="view-count">
          <el-icon><View /></el-icon>
          {{ product.viewCount || 0 }}
        </span>
      </div>
      
      <div class="product-footer">
        <div class="seller-info">
          <el-avatar :size="20" :src="product.user?.avatar">
            {{ product.user?.username?.charAt(0) }}
          </el-avatar>
          <span class="seller-name">{{ product.user?.username }}</span>
        </div>
        <span class="publish-time">{{ formatTime(product.createTime) }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, View } from '@element-plus/icons-vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 商品图片
const productImage = computed(() => {
  if (props.product.images && props.product.images.length > 0) {
    const images = typeof props.product.images === 'string' 
      ? JSON.parse(props.product.images) 
      : props.product.images
    return `http://localhost:8080/api/v1/files/view/${images[0]}`
  }
  return ''
})

// 商品状态
const statusText = computed(() => {
  switch (props.product.status) {
    case 0: return '已下架'
    case 2: return '已售出'
    case 3: return '审核中'
    default: return ''
  }
})

const statusType = computed(() => {
  switch (props.product.status) {
    case 0: return 'info'
    case 2: return 'success'
    case 3: return 'warning'
    default: return 'primary'
  }
})

// 跳转到商品详情
const goToDetail = () => {
  router.push(`/products/${props.product.id}`)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  position: relative;
  height: 200px;
  margin-bottom: 12px;
}

.image {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}

.product-info {
  padding: 0 4px;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-bottom: 8px;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 12px;
  color: #909399;
}

.condition {
  background-color: #f0f9ff;
  color: #409eff;
  padding: 2px 6px;
  border-radius: 4px;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 2px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.publish-time {
  font-size: 11px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-image {
    height: 150px;
  }
  
  .product-title {
    font-size: 14px;
  }
  
  .current-price {
    font-size: 16px;
  }
}
</style>