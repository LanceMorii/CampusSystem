<template>
  <div class="favorites-container">
    <div class="header">
      <h1>我的收藏</h1>
      <router-link to="/home" class="back-btn">返回首页</router-link>
    </div>
    
    <div class="content">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="favoriteProducts.length === 0" class="empty-favorites">
        <div class="empty-icon">💝</div>
        <h3>暂无收藏商品</h3>
        <p>快去发现一些好物品吧！</p>
        <router-link to="/products" class="browse-btn">去逛逛</router-link>
      </div>
      
      <div v-else class="favorites-grid">
        <div 
          v-for="product in favoriteProducts" 
          :key="product.id"
          class="favorite-item"
          @click="viewProduct(product.id)"
        >
          <div class="favorite-image">
            <img :src="getProductImage(product)" :alt="product.title || product.name" />
          </div>
          <div class="favorite-info">
            <h4 class="favorite-title">{{ product.title || product.name }}</h4>
            <p class="favorite-price">¥{{ product.price }}</p>
            <p class="favorite-description">{{ product.description || '暂无描述' }}</p>
            <div class="favorite-actions">
              <button @click.stop="removeFavorite(product.id)" class="remove-btn">
                取消收藏
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, del } from '../api/request'

const router = useRouter()
const favoriteProducts = ref([])
const loading = ref(true)

// 获取收藏商品
const fetchFavoriteProducts = async () => {
  try {
    loading.value = true
    const response = await get('/favorites/my')
    if (response && response.code === 200) {
      favoriteProducts.value = response.data || []
    }
  } catch (error) {
    console.error('获取收藏商品失败:', error)
    favoriteProducts.value = []
  } finally {
    loading.value = false
  }
}

// 获取商品图片
const getProductImage = (product) => {
  if (!product) return '/placeholder.png'
  
  // 处理图片数组
  if (product.images && Array.isArray(product.images) && product.images.length > 0) {
    const firstImage = product.images[0]
    return getImageUrl(firstImage)
  }
  
  // 处理单个图片
  if (product.image) {
    return getImageUrl(product.image)
  }
  
  return '/placeholder.png'
}

// 图片URL处理
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return '/placeholder.png'
  
  let cleanPath = imageUrl.toString().trim()
  if (cleanPath.startsWith('"') && cleanPath.endsWith('"')) {
    cleanPath = cleanPath.slice(1, -1)
  }
  
  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://')) {
    return cleanPath
  }
  
  if (cleanPath.startsWith('/api')) {
    return `http://localhost:8080${cleanPath}`
  }
  
  if (cleanPath.startsWith('/')) {
    return `http://localhost:8080/api/v1/files/view${cleanPath}`
  }
  
  return `http://localhost:8080/api/v1/files/view/${cleanPath}`
}

// 查看商品详情
const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

// 取消收藏
const removeFavorite = async (productId) => {
  try {
    const response = await del(`/favorites/${productId}`)
    if (response && response.code === 200) {
      // 从列表中移除该商品
      favoriteProducts.value = favoriteProducts.value.filter(product => product.id !== productId)
      // 可以添加成功提示
      console.log('取消收藏成功')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    // 可以添加错误提示
  }
}

onMounted(() => {
  // 检查是否有token
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  
  fetchFavoriteProducts()
})
</script>

<style scoped>
.favorites-container {
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

.back-btn {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.content {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: white;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-favorites {
  text-align: center;
  padding: 80px 20px;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-favorites h3 {
  font-size: 24px;
  margin-bottom: 10px;
}

.empty-favorites p {
  font-size: 16px;
  margin-bottom: 30px;
  opacity: 0.8;
}

.browse-btn {
  display: inline-block;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.browse-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.favorite-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.favorite-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.favorite-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.favorite-item:hover .favorite-image img {
  transform: scale(1.05);
}

.favorite-info {
  padding: 20px;
}

.favorite-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.favorite-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff6b6b;
  margin: 0 0 10px 0;
}

.favorite-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.favorite-actions {
  display: flex;
  justify-content: flex-end;
}

.remove-btn {
  background: #ff6b6b;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: #ff5252;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
  }
  
  .header h1 {
    font-size: 24px;
  }
  
  .content {
    padding: 20px;
  }
  
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
  
  .favorite-image {
    height: 160px;
  }
  
  .favorite-info {
    padding: 16px;
  }
  
  .favorite-title {
    font-size: 16px;
  }
  
  .favorite-price {
    font-size: 18px;
  }
}
</style>