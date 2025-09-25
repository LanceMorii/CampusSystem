<template>
  <div class="product-list-container">
    <div class="header">
      <h1>商品列表</h1>
      <button class="publish-btn" @click="$router.push('/publish')">发布商品</button>
    </div>
    
    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-row">
        <!-- 搜索框 -->
        <div class="search-item">
          <div class="search-input-wrapper">
            <input 
              v-model="filters.name" 
              type="text" 
              placeholder="搜索商品名称..."
              @input="handleSearch"
              class="search-input"
            />
            <button class="search-btn" @click="handleSearch">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
            </button>
          </div>
        </div>
        
        <!-- 分类选择 -->
        <div class="category-item">
          <select v-model="filters.category" @change="handleSearch" class="category-select">
            <option value="">全部分类</option>
            <option value="1">电子产品</option>
            <option value="2">图书教材</option>
            <option value="3">生活用品</option>
            <option value="4">服装配饰</option>
            <option value="5">运动健身</option>
            <option value="6">其他</option>
          </select>
        </div>
        
        <!-- 价格范围 -->
        <div class="price-item">
          <div class="price-inputs">
            <input 
              v-model="filters.minPrice" 
              type="number" 
              placeholder="最低价"
              @input="handleSearch"
              class="price-input"
            />
            <span class="price-separator">-</span>
            <input 
              v-model="filters.maxPrice" 
              type="number" 
              placeholder="最高价"
              @input="handleSearch"
              class="price-input"
            />
          </div>
        </div>
        
        <!-- 重置按钮 -->
        <button class="reset-btn" @click="resetFilters">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"></path>
            <path d="M21 3v5h-5"></path>
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"></path>
            <path d="M3 21v-5h5"></path>
          </svg>
          重置
        </button>
      </div>
    </div>
    
    <!-- 商品列表 -->
    <div class="product-grid" v-if="!loading && products.length > 0">
      <div 
        class="product-card" 
        v-for="product in products" 
        :key="product.id"
        @click="viewProduct(product.id)"
      >
        <div class="product-image">
          <img :src="getProductImage(product)" :alt="product.title || product.name" />
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.title || product.name }}</h3>
          <p class="product-description">{{ product.description }}</p>
          <div class="product-meta">
            <span class="product-price">¥{{ product.price }}</span>
            <span class="product-category">{{ product.categoryName || getCategoryName(product.category) }}</span>
          </div>
          <div class="product-footer">
            <span class="product-seller">卖家：{{ product.username || product.sellerName || '未知' }}</span>
            <span class="product-time">{{ formatTime(product.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && products.length === 0" class="empty-state">
      <p>暂无商品数据</p>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="!loading && totalPages > 1">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      
      <span class="page-numbers">
        <button 
          v-for="page in visiblePages" 
          :key="page"
          class="page-number"
          :class="{ active: page === currentPage }"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
      </span>
      
      <button 
        class="page-btn" 
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
      
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页，共 {{ totalCount }} 条记录
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '../api/request'

const router = useRouter()

// 响应式数据
const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20) // 增加页面大小以显示更多产品
const totalCount = ref(0)
const totalPages = ref(0)

// 筛选条件
const filters = ref({
  name: '',
  category: '',
  minPrice: '',
  maxPrice: ''
})

// 计算属性
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 方法
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value
    }
    
    // 添加搜索条件
    if (filters.value.name) {
      params.name = filters.value.name
    }
    if (filters.value.category) {
      params.categoryId = filters.value.category
    }
    if (filters.value.minPrice) {
      params.minPrice = parseFloat(filters.value.minPrice)
    }
    if (filters.value.maxPrice) {
      params.maxPrice = parseFloat(filters.value.maxPrice)
    }
    
    // 过滤掉空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    console.log('获取商品列表，参数：', params)
    
    const response = await get('/products', params)
    
    console.log('API响应：', response)
    
    if (response && response.code === 200) {
      const pageData = response.data
      console.log('页面数据：', pageData)
      
      if (pageData && pageData.content) {
        products.value = pageData.content
        totalCount.value = pageData.totalElements || 0
        totalPages.value = pageData.totalPages || 0
        console.log('商品列表设置成功，商品数量：', products.value.length)
        console.log('商品数据：', products.value)
      } else {
        console.log('页面数据结构异常：', pageData)
        products.value = []
      }
    } else {
      console.error('获取商品列表失败:', response)
      products.value = []
    }
  } catch (error) {
    console.error('获取商品列表异常:', error)
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

const resetFilters = () => {
  filters.value = {
    name: '',
    category: '',
    minPrice: '',
    maxPrice: ''
  }
  currentPage.value = 1
  fetchProducts()
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    fetchProducts()
  }
}

const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
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
  
  // 移除可能存在的引号
  if (cleanPath.startsWith('"') && cleanPath.endsWith('"')) {
    cleanPath = cleanPath.slice(1, -1)
  }
  
  // 如果已经是完整的HTTP URL，直接返回
  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://')) {
    return cleanPath
  }
  
  // 如果以/api开头，说明是完整的API路径
  if (cleanPath.startsWith('/api')) {
    return `http://localhost:8080${cleanPath}`
  }
  
  // 如果以/开头，添加API前缀
  if (cleanPath.startsWith('/')) {
    return `http://localhost:8080/api/v1/files/view${cleanPath}`
  }
  
  // 其他情况，作为相对路径处理
  return `http://localhost:8080/api/v1/files/view/${cleanPath}`
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchProducts()
})

// 监听路由变化，当从发布页面返回时刷新数据
watch(() => router.currentRoute.value, (newRoute, oldRoute) => {
  if (newRoute.path === '/products' && oldRoute?.path === '/publish') {
    // 从发布页面返回，刷新商品列表
    fetchProducts()
  }
}, { immediate: false })

// 监听筛选条件变化
watch(filters, () => {
  // 防抖处理
  clearTimeout(window.searchTimeout)
  window.searchTimeout = setTimeout(() => {
    handleSearch()
  }, 500)
}, { deep: true })
</script>

<style scoped>
.product-list-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header h1 {
  color: #333;
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.publish-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 筛选区域样式 */
.filter-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 32px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.search-item {
  flex: 2;
  min-width: 300px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  position: relative;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: white;
}

.search-input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.search-input {
  flex: 1;
  padding: 14px 18px;
  border: none;
  outline: none;
  font-size: 15px;
  color: #333;
  background: transparent;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  padding: 14px 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  min-width: 52px;
}

.search-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: scale(1.05);
}

.category-item {
  flex: 1;
  min-width: 160px;
}

.category-select {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-size: 15px;
  color: #333;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.price-item {
  flex: 1;
  min-width: 200px;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-input {
  flex: 1;
  padding: 14px 16px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-size: 15px;
  color: #333;
  background: white;
  transition: all 0.3s ease;
  min-width: 80px;
}

.price-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.price-separator {
  color: #667eea;
  font-weight: 600;
  font-size: 16px;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 2px solid rgba(102, 126, 234, 0.2);
  padding: 14px 20px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.reset-btn:hover {
  background: rgba(102, 126, 234, 0.15);
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.product-image {
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-description {
  color: #666;
  font-size: 14px;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #e53e3e;
}

.product-category {
  background: #f0f0f0;
  color: #666;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.loading,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 30px;
}

.page-btn {
  background: white;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 5px;
}

.page-number {
  background: white;
  border: 1px solid #ddd;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.page-number:hover {
  background: #f5f5f5;
}

.page-number.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.page-info {
  color: #666;
  font-size: 14px;
  margin-left: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-list-container {
    padding: 16px;
  }
  
  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-section {
    padding: 16px;
  }
  
  .filter-row {
    gap: 16px;
  }
  
  .search-group {
    width: 100%;
  }
  
  .search-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .search-input-wrapper {
    max-width: none;
  }
  
  .filters-group {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-item {
    min-width: auto;
    width: 100%;
  }
  
  .price-range {
    min-width: auto;
  }
  
  .price-inputs {
    flex-direction: column;
    gap: 12px;
  }
  
  .price-input {
    min-width: auto;
  }
  
  .price-separator {
    display: none;
  }
  
  .reset-btn {
    width: 100%;
    justify-content: center;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .page-info {
    margin-left: 0;
    margin-top: 10px;
    text-align: center;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .product-list-container {
    padding: 12px;
  }
  
  .filter-section {
    padding: 12px;
  }
  
  .search-input-wrapper {
    border-radius: 6px;
  }
  
  .search-input {
    padding: 10px 12px;
    font-size: 16px; /* 防止iOS缩放 */
  }
  
  .search-btn {
    padding: 10px 12px;
    min-width: 44px;
  }
  
  .filter-item input,
  .filter-item select {
    padding: 10px 12px;
    font-size: 16px; /* 防止iOS缩放 */
  }
  
  .product-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .product-card {
    border-radius: 8px;
  }
}
</style>