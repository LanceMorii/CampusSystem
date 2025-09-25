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
            <img :src="getImageUrl(currentImage)" :alt="product.title || product.name" />
          </div>
          <div class="image-thumbnails" v-if="productImages.length > 1">
            <img 
              v-for="(image, index) in productImages" 
              :key="index"
              :src="getImageUrl(image)"
              :alt="`${product.title || product.name} ${index + 1}`"
              :class="{ active: currentImage === image }"
              @click="currentImage = image"
            />
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-title">{{ product.title || product.name }}</h1>
          
          <div class="price-section">
            <span class="current-price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
              ¥{{ product.originalPrice }}
            </span>
          </div>
          
          <div class="product-meta">
            <div class="meta-item">
              <span class="label">商品分类：</span>
              <span class="value">{{ getCategoryName(product.categoryId || product.category) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">商品状态：</span>
              <span class="value" :class="`status-${getStatusClass(product.status)}`">
                {{ getStatusName(product.status) }}
              </span>
            </div>
            <div class="meta-item">
              <span class="label">发布时间：</span>
              <span class="value">{{ formatTime(product.createTime) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">浏览次数：</span>
              <span class="value">{{ product.viewCount || 0 }}</span>
            </div>
          </div>
          
          <!-- 卖家信息 -->
          <div class="seller-info">
            <h3>卖家信息</h3>
            <div class="seller-card">
              <div class="seller-avatar">
                <img :src="getSellerAvatar()" :alt="getSellerName()" />
              </div>
              <div class="seller-details">
                <div class="seller-name">{{ getSellerName() }}</div>
                <div class="seller-contact">
                  <span v-if="product.phone || product.seller?.phone">联系电话：{{ product.phone || product.seller?.phone }}</span>
                  <span v-if="product.email || product.seller?.email">邮箱：{{ product.email || product.seller?.email }}</span>
                  <span v-if="!product.phone && !product.seller?.phone && !product.email && !product.seller?.email">暂无联系方式</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons" v-if="product.status === 1">
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
          
          <div v-else class="unavailable-notice">
            <p>该商品暂时不可购买</p>
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
            <img :src="getImageUrl(item.image || (item.images && item.images[0]))" :alt="item.title || item.name" />
            <div class="related-info">
              <h4>{{ item.title || item.name }}</h4>
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
            <img :src="getImageUrl(currentImage)" :alt="product.title || product.name" class="summary-image">
            <div class="summary-info">
              <h3>{{ product.title || product.name }}</h3>
              <div class="summary-price">¥{{ product.price }}</div>
            </div>
          </div>
          
          <div class="purchase-form">
            <div class="form-group">
              <label>购买数量：</label>
              <div class="quantity-control">
                <button @click="decreaseQuantity" :disabled="purchaseQuantity <= 1">-</button>
                <input v-model="purchaseQuantity" type="number" min="1" max="10" />
                <button @click="increaseQuantity" :disabled="purchaseQuantity >= 10">+</button>
              </div>
            </div>
            
            <div class="form-group">
              <label>联系方式：</label>
              <input v-model="buyerContact" type="text" placeholder="请输入您的联系方式" />
            </div>
            
            <div class="form-group">
              <label>收货地址：</label>
              <input v-model="deliveryAddress" type="text" placeholder="请输入收货地址" />
            </div>
            
            <div class="form-group">
              <label>备注信息：</label>
              <textarea v-model="orderNotes" placeholder="请输入备注信息（可选）"></textarea>
            </div>
            
            <div class="total-price">
              总价：¥{{ (product.price * purchaseQuantity).toFixed(2) }}
            </div>
          </div>
          
          <div class="purchase-actions">
            <button @click="closePurchaseDialog" class="cancel-btn">取消</button>
            <button @click="confirmPurchase" class="confirm-btn">确认购买</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del } from '../api/request'
import toast from '../services/toast'
import modal from '../services/modal'

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

// 计算商品图片数组
const productImages = computed(() => {
  if (!product.value) return []
  
  const images = []
  
  // 添加主图片
  if (product.value.image) {
    images.push(product.value.image)
  }
  
  // 添加其他图片
  if (product.value.images && Array.isArray(product.value.images)) {
    product.value.images.forEach(img => {
      if (img && !images.includes(img)) {
        images.push(img)
      }
    })
  }
  
  return images
})

// 图片URL处理方法
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

// 获取卖家头像
const getSellerAvatar = () => {
  // 优先使用userAvatar字段
  if (product.value?.userAvatar) {
    return getImageUrl(product.value.userAvatar)
  }
  // 备选：seller对象中的avatar
  if (product.value?.seller?.avatar) {
    return getImageUrl(product.value.seller.avatar)
  }
  return '/logo.jpg'
}

// 获取卖家名称
const getSellerName = () => {
  // 优先使用username字段
  return product.value?.username || 
         product.value?.seller?.name || 
         product.value?.sellerName || 
         '未知卖家'
}

// 方法
const fetchProduct = async () => {
  loading.value = true
  try {
    console.log('获取商品详情，ID：', productId.value)
    
    const response = await get(`/products/${productId.value}`)
    
    console.log('商品详情API响应：', response)
    
    if (response && response.code === 200) {
      product.value = response.data
      
      // 设置当前显示的图片
      const images = productImages.value
      currentImage.value = images.length > 0 ? images[0] : ''
      
      console.log('商品详情获取成功：', response.data)
      
      // 获取相关商品
      fetchRelatedProducts()
      
      // 增加浏览次数
      incrementViewCount()
    } else {
      console.log('获取商品详情失败:', response ? response.message : '响应为空')
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
    const categoryId = product.value.categoryId || product.value.category
    const response = await get('/products', {
      categoryId: categoryId,
      limit: 4,
      exclude: productId.value
    })
    
    if (response && response.code === 200 && response.data) {
      // 确保response.data是数组再使用slice方法
      if (Array.isArray(response.data)) {
        relatedProducts.value = response.data.slice(0, 4)
      } else {
        console.warn('相关商品数据不是数组格式')
        relatedProducts.value = []
      }
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
  const sellerId = product.value?.sellerId || product.value?.userId
  const currentUserId = Number(localStorage.getItem('userId') || '0')
  
  console.log('联系卖家调试:', {
    sellerId,
    currentUserId,
    productUserId: product.value?.userId,
    productSellerId: product.value?.sellerId
  })
  
  if (sellerId) {
    // 检查是否是自己的商品
    if (sellerId === currentUserId) {
      alert('这是您自己发布的商品，无法联系自己')
      return
    }
    
    router.push({
      path: '/messages',
      query: {
        to: String(sellerId),
        productId: String(productId.value || ''),
        name: getSellerName(),
        avatar: getSellerAvatar()
      }
    })
  } else {
    const sellerPhone = product.value?.phone || product.value?.seller?.phone
    const sellerEmail = product.value?.email || product.value?.seller?.email
    if (sellerPhone) {
      alert(`卖家联系方式：${sellerPhone}`)
    } else if (sellerEmail) {
      alert(`卖家邮箱：${sellerEmail}`)
    } else {
      alert('暂无卖家联系方式')
    }
  }
}

const toggleFavorite = async () => {
  try {
    if (isFavorite.value) {
      // 取消收藏
      const response = await del(`/favorites/${productId.value}`)
      if (response.code === 200) {
        isFavorite.value = false
        toast.success('已取消收藏', '操作成功')
      } else {
        toast.error(response.message || '取消收藏失败，请重试', '操作失败')
      }
    } else {
      // 添加收藏
      const response = await post(`/favorites/${productId.value}`, {})
      if (response.code === 200) {
        isFavorite.value = true
        toast.success('收藏成功！可在"我的收藏"中查看', '收藏成功')
      } else {
        toast.error(response.message || '收藏失败，请重试', '操作失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败：', error)
    toast.error('网络错误，请检查网络连接后重试', '操作失败')
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
  // 表单验证
  const errors = []
  
  if (!buyerContact.value.trim()) {
    errors.push('请输入联系方式')
  } else {
    // 验证联系方式格式（手机号或邮箱）
    const phoneRegex = /^1[3-9]\d{9}$/
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    
    if (!phoneRegex.test(buyerContact.value) && !emailRegex.test(buyerContact.value)) {
      errors.push('请输入有效的手机号或邮箱地址')
    }
  }
  
  if (!deliveryAddress.value.trim()) {
    errors.push('请输入收货地址')
  } else if (deliveryAddress.value.trim().length < 5) {
    errors.push('收货地址至少需要5个字符')
  }
  
  if (purchaseQuantity.value < 1 || purchaseQuantity.value > 10) {
    errors.push('购买数量必须在1-10之间')
  }
  
  // 显示验证错误
  if (errors.length > 0) {
    await modal.error(errors.join('\n'), '请修正以下错误')
    return
  }
  
  // 确认购买
  const result = await modal.confirm(
    `确认购买 ${purchaseQuantity.value} 件商品，总价 ¥${(product.value.price * purchaseQuantity.value).toFixed(2)}？`,
    '确认购买'
  )
  
  if (result !== 'confirm') {
    return
  }
  
  // 显示加载状态
  const loadingModal = modal.loading('正在创建订单，请稍候...', '处理中')
  
  try {
    const orderData = {
      productId: productId.value,
      amount: product.value.price * purchaseQuantity.value,
      remark: orderNotes.value || ''
    }
    
    console.log('创建订单数据：', orderData)
    
    const response = await post('/orders', orderData)
    
    // 关闭加载弹窗
    modal.clear()
    
    if (response.success || response.code === 200) {
      await modal.success('订单创建成功！请等待卖家确认。', '下单成功')
      closePurchaseDialog()
      
      // 跳转到订单管理页面
      router.push('/orders')
    } else {
      await modal.error(response.message || '订单创建失败，请重试', '下单失败')
    }
  } catch (error) {
    console.error('创建订单失败：', error)
    // 关闭加载弹窗
    modal.clear()
    await modal.error('网络错误，请检查网络连接后重试', '下单失败')
  }
}

const getCategoryName = (categoryId) => {
  const categoryMap = {
    1: '电子产品',
    2: '图书教材', 
    3: '服装配饰',
    4: '运动用品',
    5: '生活用品',
    6: '其他',
    'electronics': '电子产品',
    'books': '图书教材',
    'clothing': '服装配饰',
    'sports': '运动用品',
    'daily': '生活用品',
    'other': '其他'
  }
  return categoryMap[categoryId] || '未知分类'
}

const getStatusName = (status) => {
  const statusMap = {
    1: '在售',
    0: '已下架',
    2: '已售出',
    3: '已预订',
    'available': '在售',
    'sold': '已售出',
    'reserved': '已预订',
    'offline': '已下架'
  }
  return statusMap[status] || '未知状态'
}

const getStatusClass = (status) => {
  if (status === 1 || status === 'available') return 'available'
  if (status === 0 || status === 'offline') return 'offline'
  if (status === 2 || status === 'sold') return 'sold'
  if (status === 3 || status === 'reserved') return 'reserved'
  return 'unknown'
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
  align-items: start;
}

.product-images {
  display: flex;
  flex-direction: column;
  gap: 15px;
  position: sticky;
  top: 20px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 15px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: white;
  transition: transform 0.3s ease;
}

.main-image img:hover {
  transform: scale(1.05);
}

.image-thumbnails {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 10px 0;
  scrollbar-width: thin;
  scrollbar-color: #ccc transparent;
}

.image-thumbnails::-webkit-scrollbar {
  height: 6px;
}

.image-thumbnails::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.image-thumbnails::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.image-thumbnails::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.image-thumbnails img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.image-thumbnails img:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.image-thumbnails img.active {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transform: scale(1.1);
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.product-title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0;
  line-height: 1.3;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 2px solid #f0f0f0;
}

.current-price {
  font-size: 36px;
  font-weight: 700;
  color: #e53e3e;
}

.original-price {
  font-size: 20px;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.meta-item .label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-item .value {
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.status-available { 
  color: #22543d; 
  background: #c6f6d5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}
.status-sold { 
  color: #e53e3e; 
  background: #fed7d7;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}
.status-reserved { 
  color: #d69e2e; 
  background: #faf089;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}
.status-offline { 
  color: #999; 
  background: #e2e8f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.seller-info {
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 25px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.seller-info h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.seller-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
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
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.seller-contact {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.buy-btn,
.contact-btn,
.favorite-btn {
  flex: 1;
  padding: 15px 24px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.buy-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}

.buy-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.4);
}

.contact-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.contact-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.favorite-btn {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.1);
}

.favorite-btn:hover,
.favorite-btn.active {
  background: #667eea;
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.unavailable-notice {
  background: #fed7d7;
  color: #e53e3e;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  font-weight: 600;
}

.product-description {
  padding: 40px;
  border-top: 2px solid #e2e8f0;
}

.product-description h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 20px 0;
  font-weight: 700;
}

.description-content {
  color: #666;
  line-height: 1.8;
  font-size: 16px;
}

.no-description {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.related-products {
  padding: 40px;
  border-top: 2px solid #e2e8f0;
  background: #f8f9fa;
}

.related-products h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 30px 0;
  font-weight: 700;
  text-align: center;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 25px;
}

.related-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.related-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.related-item img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.related-item:hover img {
  transform: scale(1.05);
}

.related-info {
  padding: 20px;
}

.related-info h4 {
  font-size: 16px;
  color: #333;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.related-price {
  font-size: 18px;
  font-weight: 700;
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
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.purchase-container {
  background: white;
  border-radius: 16px;
  max-width: 550px;
  width: 90%;
  max-height: 90%;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.purchase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  border-bottom: 2px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px 16px 0 0;
}

.purchase-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.purchase-content {
  padding: 25px;
}

.product-summary {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 2px solid #e2e8f0;
}

.summary-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.summary-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.summary-price {
  color: #ff6b6b;
  font-weight: 700;
  font-size: 20px;
}

.purchase-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-group label {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.form-group input,
.form-group textarea {
  padding: 12px 15px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
}

.quantity-control button {
  width: 40px;
  height: 40px;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.quantity-control button:hover:not(:disabled) {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.quantity-control button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-control input {
  width: 80px;
  text-align: center;
  margin: 0;
  font-weight: 600;
}

.total-price {
  text-align: right;
  font-size: 22px;
  font-weight: 700;
  color: #ff6b6b;
  padding-top: 20px;
  border-top: 2px solid #e0e0e0;
}

.purchase-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 25px;
  border-top: 2px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 0 0 16px 16px;
}

.cancel-btn,
.confirm-btn {
  padding: 12px 25px;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  border: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #e2e8f0;
  color: #666;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.confirm-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.4);
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
  font-size: 28px;
  margin-bottom: 20px;
  font-weight: 700;
}

.error-content p {
  color: #666;
  font-size: 16px;
  margin-bottom: 30px;
  line-height: 1.6;
}

.error-content .back-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border: none;
  padding: 15px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}

.error-content .back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail-container {
    padding: 16px;
  }
  
  .product-main {
    grid-template-columns: 1fr;
    gap: 25px;
    padding: 20px;
  }
  
  .product-images {
    position: static;
  }
  
  .main-image {
    height: 350px;
  }
  
  .product-title {
    font-size: 26px;
  }
  
  .current-price {
    font-size: 30px;
  }
  
  .product-meta {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .related-grid {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 20px;
  }
  
  .image-thumbnails {
    justify-content: flex-start;
    flex-wrap: nowrap;
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .product-title {
    font-size: 22px;
  }
  
  .current-price {
    font-size: 26px;
  }
  
  .main-image {
    height: 280px;
  }
  
  .image-thumbnails img {
    width: 70px;
    height: 70px;
  }
  
  .purchase-container {
    width: 95%;
  }
}
</style>