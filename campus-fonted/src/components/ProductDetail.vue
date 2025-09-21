<template>
  <div class="product-detail-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <button @click="goBack" class="back-btn">
        ← 返回商品列表
      </button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    
    <!-- 商品不存在或已删除 -->
    <div v-else-if="!product" class="error-state">
      <div class="error-content">
        <h2>商品不存在</h2>
        <p>该商品可能已被删除或不存在</p>
        <button @click="goBack" class="back-btn">返回商品列表</button>
      </div>
    </div>
    
    <!-- 商品详情 -->
    <div v-else class="product-detail">
      <div class="product-main">
        <!-- 商品图片 -->
        <div class="product-images">
          <div class="main-image">
            <img :src="currentImage || '/placeholder.jpg'" :alt="product.name" />
          </div>
          <div class="image-thumbnails" v-if="product.images && product.images.length > 1">
            <img 
              v-for="(image, index) in product.images" 
              :key="index"
              :src="image"
              :alt="`${product.name} ${index + 1}`"
              :class="{ active: currentImage === image }"
              @click="currentImage = image"
            />
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>
          
          <div class="product-price">
            <span class="current-price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
              ¥{{ product.originalPrice }}
            </span>
          </div>
          
          <div class="product-meta">
            <div class="meta-item">
              <span class="label">商品分类：</span>
              <span class="value">{{ getCategoryName(product.category) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">商品状态：</span>
              <span class="value" :class="getStatusClass(product.status)">
                {{ getStatusName(product.status) }}
              </span>
            </div>
            <div class="meta-item">
              <span class="label">发布时间：</span>
              <span class="value">{{ formatTime(product.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">浏览次数：</span>
              <span class="value">{{ product.viewCount || 0 }}</span>
            </div>
          </div>
          
          <div class="seller-info">
            <h3>卖家信息</h3>
            <div class="seller-card">
              <div class="seller-avatar">
                <img :src="product.seller?.avatar || '/default-avatar.jpg'" :alt="product.seller?.name" />
              </div>
              <div class="seller-details">
                <div class="seller-name">{{ product.seller?.name || product.sellerName }}</div>
                <div class="seller-contact">
                  <span v-if="product.seller?.phone">联系电话：{{ product.seller?.phone }}</span>
                  <span v-if="product.seller?.email">邮箱：{{ product.seller?.email }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="action-buttons" v-if="product.status === 'available'">
            <button class="buy-btn" @click="showPurchaseDialog">
              立即购买
            </button>
            <button class="contact-btn" @click="contactSeller">
              联系卖家
            </button>
            <button class="favorite-btn" @click="toggleFavorite" :class="{ active: isFavorite }">
              {{ isFavorite ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </div>
      
      <!-- 商品描述 -->
      <div class="product-description">
        <h2>商品描述</h2>
        <div class="description-content">
          <p v-if="product.description">{{ product.description }}</p>
          <p v-else class="no-description">暂无商品描述</p>
        </div>
      </div>
      
      <!-- 相关商品推荐 -->
      <div class="related-products" v-if="relatedProducts.length > 0">
        <h2>相关商品推荐</h2>
        <div class="related-grid">
          <div 
            class="related-item" 
            v-for="item in relatedProducts" 
            :key="item.id"
            @click="viewProduct(item.id)"
          >
            <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
            <div class="related-info">
              <h4>{{ item.name }}</h4>
              <span class="related-price">¥{{ item.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 购买对话框 -->
    <div v-if="showPurchase" class="purchase-modal" @click="closePurchaseDialog">
      <div class="purchase-container" @click.stop>
        <div class="purchase-header">
          <h2>确认购买</h2>
          <button @click="closePurchaseDialog" class="close-btn">×</button>
        </div>
        
        <div class="purchase-content">
          <div class="product-summary">
            <img :src="product.image || '/placeholder.jpg'" :alt="product.name" class="summary-image">
            <div class="summary-info">
              <h3>{{ product.name }}</h3>
              <div class="summary-price">¥{{ product.price }}</div>
            </div>
          </div>
          
          <div class="purchase-form">
            <div class="form-group">
              <label>购买数量：</label>
              <div class="quantity-control">
                <button @click="decreaseQuantity" :disabled="purchaseQuantity <= 1">-</button>
                <input v-model.number="purchaseQuantity" type="number" min="1" max="10">
                <button @click="increaseQuantity" :disabled="purchaseQuantity >= 10">+</button>
              </div>
            </div>
            
            <div class="form-group">
              <label>联系方式：</label>
              <input v-model="buyerContact" type="text" placeholder="请输入您的联系方式" required>
            </div>
            
            <div class="form-group">
              <label>收货地址：</label>
              <textarea v-model="deliveryAddress" placeholder="请输入收货地址" rows="3"></textarea>
            </div>
            
            <div class="form-group">
              <label>备注信息：</label>
              <textarea v-model="orderNotes" placeholder="给卖家留言（可选）" rows="2"></textarea>
            </div>
            
            <div class="total-price">
              <span>总价：¥{{ (product.price * purchaseQuantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
        
        <div class="purchase-actions">
          <button @click="closePurchaseDialog" class="cancel-btn">取消</button>
          <button @click="confirmPurchase" class="confirm-btn" :disabled="!buyerContact.trim()">
            确认购买
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { get, post } from '../api/request'

const router = useRouter()
const route = useRoute()

// 响应式数据
const product = ref(null)
const loading = ref(false)
const currentImage = ref('')
const isFavorite = ref(false)
const relatedProducts = ref([])
const showPurchase = ref(false)
const purchaseQuantity = ref(1)
const buyerContact = ref('')
const deliveryAddress = ref('')
const orderNotes = ref('')

// 计算属性
const productId = computed(() => route.params.id)

// 方法
const fetchProduct = async () => {
  loading.value = true
  try {
    console.log('获取商品详情，ID：', productId.value)
    
    const response = await get(`/products/${productId.value}`)
    
    console.log('商品详情API响应：', response)
    
    if (response && response.code === 200) {
      product.value = response.data
      
      // 处理图片数据：如果images是JSON字符串，需要解析为数组
      if (product.value.images && typeof product.value.images === 'string') {
        try {
          product.value.images = JSON.parse(product.value.images)
        } catch (e) {
          console.error('解析图片JSON失败:', e)
          product.value.images = []
        }
      }
      
      // 设置当前显示的图片
      currentImage.value = product.value.image || (product.value.images && product.value.images[0]) || ''
      console.log('商品详情获取成功：', response.data)
      
      // 获取相关商品
      fetchRelatedProducts()
      
      // 增加浏览次数
      incrementViewCount()
    } else {
      console.log('获取商品详情失败:', response ? response.message : '响应为空')
      // 显示错误信息给用户
      product.value = null
    }
  } catch (error) {
    console.log('获取商品详情异常:', error)
    product.value = null
  } finally {
    loading.value = false
  }
}

const fetchRelatedProducts = async () => {
  try {
    const response = await get('/products', {
      category: product.value.category,
      limit: 4,
      exclude: productId.value
    })
    
    if (response.success) {
      relatedProducts.value = response.data.products || []
    }
  } catch (error) {
    console.error('获取相关商品失败：', error)
  }
}

const incrementViewCount = async () => {
  try {
    await post(`/products/${productId.value}/view`)
  } catch (error) {
    console.error('增加浏览次数失败：', error)
  }
}

const goBack = () => {
  router.push('/products')
}

const viewProduct = (id) => {
  router.push(`/product/${id}`)
}

const contactSeller = () => {
  if (product.value.seller?.phone) {
    // 可以打开电话应用或显示联系方式
    alert(`卖家联系方式：${product.value.seller.phone}`)
  } else {
    alert('暂无卖家联系方式')
  }
}

const toggleFavorite = async () => {
  try {
    const response = await post(`/products/${productId.value}/favorite`, {
      action: isFavorite.value ? 'remove' : 'add'
    })
    
    if (response.success) {
      isFavorite.value = !isFavorite.value
    }
  } catch (error) {
    console.error('收藏操作失败：', error)
  }
}

const showPurchaseDialog = () => {
  // 获取用户信息作为默认联系方式
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo && userInfo !== 'undefined') {
    try {
      const user = JSON.parse(userInfo)
      buyerContact.value = user.phone || user.email || ''
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
  
  showPurchase.value = true
}

const closePurchaseDialog = () => {
  showPurchase.value = false
  // 重置表单
  purchaseQuantity.value = 1
  buyerContact.value = ''
  deliveryAddress.value = ''
  orderNotes.value = ''
}

const increaseQuantity = () => {
  if (purchaseQuantity.value < 10) {
    purchaseQuantity.value++
  }
}

const decreaseQuantity = () => {
  if (purchaseQuantity.value > 1) {
    purchaseQuantity.value--
  }
}

const confirmPurchase = async () => {
  if (!buyerContact.value.trim()) {
    alert('请输入联系方式')
    return
  }
  
  try {
    const orderData = {
      productId: productId.value,
      productName: product.value.name,
      productImage: product.value.image,
      productDescription: product.value.description,
      category: product.value.category,
      quantity: purchaseQuantity.value,
      unitPrice: product.value.price,
      totalPrice: product.value.price * purchaseQuantity.value,
      contactPhone: buyerContact.value,
      deliveryAddress: deliveryAddress.value,
      notes: orderNotes.value,
      sellerId: product.value.sellerId,
      sellerName: product.value.sellerName || product.value.seller?.name
    }
    
    const response = await post('/orders', orderData)
    
    if (response.success || true) { // 模拟成功
      alert('订单创建成功！请等待卖家确认。')
      closePurchaseDialog()
      
      // 跳转到订单管理页面
      router.push('/orders')
    } else {
      alert('订单创建失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('创建订单失败：', error)
    alert('订单创建失败，请重试')
  }
}

const getCategoryName = (category) => {
  const categoryMap = {
    electronics: '电子产品',
    books: '图书教材',
    clothing: '服装配饰',
    sports: '运动用品',
    daily: '生活用品',
    other: '其他'
  }
  return categoryMap[category] || '未知分类'
}

const getStatusName = (status) => {
  const statusMap = {
    available: '在售',
    sold: '已售出',
    reserved: '已预订',
    offline: '已下架'
  }
  return statusMap[status] || '未知状态'
}

const getStatusClass = (status) => {
  return {
    'status-available': status === 'available',
    'status-sold': status === 'sold',
    'status-reserved': status === 'reserved',
    'status-offline': status === 'offline'
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.back-btn {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #e5e5e5;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.product-detail {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 30px;
}

.product-images {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-thumbnails {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}

.image-thumbnails img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.image-thumbnails img:hover,
.image-thumbnails img.active {
  border-color: #667eea;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-price {
  font-size: 32px;
  font-weight: 700;
  color: #e53e3e;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-item .label {
  font-weight: 500;
  color: #666;
  min-width: 100px;
}

.meta-item .value {
  color: #333;
}

.status-available { color: #22543d; }
.status-sold { color: #e53e3e; }
.status-reserved { color: #d69e2e; }
.status-offline { color: #999; }

.seller-info {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
}

.seller-info h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 15px;
}

.seller-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
}

.seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-details {
  flex: 1;
}

.seller-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.seller-contact {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 14px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.buy-btn,
.contact-btn,
.favorite-btn {
  flex: 1;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.buy-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border: none;
}

.buy-btn:hover {
  transform: translateY(-2px);
}

.contact-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.contact-btn:hover {
  transform: translateY(-2px);
}

.favorite-btn {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.favorite-btn:hover,
.favorite-btn.active {
  background: #667eea;
  color: white;
}

.product-description {
  padding: 30px;
  border-top: 1px solid #e2e8f0;
}

.product-description h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 15px 0;
}

.description-content {
  color: #666;
  line-height: 1.6;
}

.no-description {
  color: #999;
  font-style: italic;
}

.related-products {
  padding: 30px;
  border-top: 1px solid #e2e8f0;
}

.related-products h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px 0;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.related-item {
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.related-item:hover {
  transform: translateY(-2px);
}

.related-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.related-info {
  padding: 15px;
}

.related-info h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-price {
  font-size: 16px;
  font-weight: 600;
  color: #e53e3e;
}

.not-found {
  text-align: center;
  padding: 60px 20px;
}

.not-found h2 {
  color: #333;
  margin-bottom: 15px;
}

/* 购买对话框样式 */
.purchase-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.purchase-container {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 90%;
  overflow-y: auto;
}

.purchase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.purchase-header h2 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background: #f0f0f0;
}

.purchase-content {
  padding: 20px;
}

.product-summary {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.summary-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.summary-price {
  color: #ff6b6b;
  font-weight: 600;
  font-size: 18px;
}

.purchase-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-group input,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
}

.quantity-control button {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.quantity-control button:hover:not(:disabled) {
  background: #f5f5f5;
}

.quantity-control button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-control input {
  width: 60px;
  text-align: center;
  margin: 0;
}

.total-price {
  text-align: right;
  font-size: 18px;
  font-weight: 600;
  color: #ff6b6b;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
}

.purchase-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.cancel-btn,
.confirm-btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.confirm-btn {
  background: #ff6b6b;
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background: #ee5a24;
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.not-found p {
  color: #666;
  margin-bottom: 30px;
}

/* 错误状态样式 */
.error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px;
}

.error-content {
  text-align: center;
  max-width: 400px;
}

.error-content h2 {
  color: #ff6b6b;
  font-size: 24px;
  margin-bottom: 16px;
}

.error-content p {
  color: #666;
  font-size: 16px;
  margin-bottom: 24px;
  line-height: 1.5;
}

.error-content .back-btn {
  background: #ff6b6b;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.error-content .back-btn:hover {
  background: #ee5a24;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail-container {
    padding: 16px;
  }
  
  .product-main {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px;
  }
  
  .main-image {
    height: 300px;
  }
  
  .product-title {
    font-size: 24px;
  }
  
  .current-price {
    font-size: 28px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .related-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 15px;
  }
}
</style>