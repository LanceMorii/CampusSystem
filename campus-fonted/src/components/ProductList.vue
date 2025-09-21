<template>
  <div class="product-list-container">
    <div class="header">
      <h1>商品列表</h1>
      <button class="publish-btn" @click="$router.push('/publish')">发布商品</button>
    </div>
    
    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item">
          <label>商品名称：</label>
          <input 
            v-model="filters.name" 
            type="text" 
            placeholder="请输入商品名称"
            @input="handleSearch"
          />
        </div>
        <div class="filter-item">
          <label>商品分类：</label>
          <select v-model="filters.category" @change="handleSearch">
            <option value="">全部分类</option>
            <option value="electronics">电子产品</option>
            <option value="books">图书教材</option>
            <option value="clothing">服装配饰</option>
            <option value="sports">运动用品</option>
            <option value="daily">生活用品</option>
            <option value="other">其他</option>
          </select>
        </div>
        <div class="filter-item">
          <label>价格范围：</label>
          <input 
            v-model="filters.minPrice" 
            type="number" 
            placeholder="最低价"
            @input="handleSearch"
          />
          <span>-</span>
          <input 
            v-model="filters.maxPrice" 
            type="number" 
            placeholder="最高价"
            @input="handleSearch"
          />
        </div>
        <button class="reset-btn" @click="resetFilters">重置</button>
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
const pageSize = ref(12)
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
      page: currentPage.value - 1, // Spring Data JPA的页码从0开始
      size: pageSize.value, // 使用size而不是pageSize
      ...filters.value
    }
    
    // 清理空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    console.log('获取商品列表，参数：', params)
    console.log('请求URL：', '/products')
    
    const response = await get('/products', params)
    
    console.log('API响应：', response)
    
    if (response && response.code === 200) {
      // 后端返回的是Spring Data的Page对象结构
      const pageData = response.data
      console.log('页面数据：', pageData)
      
      if (pageData && pageData.content) {
        products.value = pageData.content
        totalCount.value = pageData.totalElements || 0
        totalPages.value = pageData.totalPages || 0
        console.log('商品列表设置成功，商品数量：', products.value.length)
      } else {
        console.log('页面数据结构异常：', pageData)
        products.value = []
      }
    } else {
      console.log('获取商品列表失败:', response ? response.message : '响应为空')
      products.value = []
    }
  } catch (error) {
    console.log('获取商品列表异常:', error)
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
  if (product.images) {
    let images = product.images
    
    // 如果images是JSON字符串，解析为数组
    if (typeof images === 'string') {
      try {
        images = JSON.parse(images)
      } catch (e) {
        console.error('解析图片JSON失败:', e)
        return '/placeholder.jpg'
      }
    }
    
    // 如果是数组且有内容，取第一张图片
    if (Array.isArray(images) && images.length > 0) {
      let imageUrl = images[0]
      // 如果图片URL被双引号包围，去掉引号
      if (typeof imageUrl === 'string' && imageUrl.startsWith('"') && imageUrl.endsWith('"')) {
        imageUrl = imageUrl.slice(1, -1)
      }
      return imageUrl
    }
  }
  return '/placeholder.jpg'
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  color: #333;
  font-size: 28px;
  margin: 0;
}

.publish-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.2s;
}

.publish-btn:hover {
  transform: translateY(-2px);
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.filter-item input,
.filter-item select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-width: 120px;
}

.filter-item input:focus,
.filter-item select:focus {
  outline: none;
  border-color: #667eea;
}

.filter-item span {
  margin: 0 8px;
  color: #666;
}

.reset-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.reset-btn:hover {
  background: #e5e5e5;
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
  
  .filter-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-item {
    flex-direction: row;
    align-items: center;
  }
  
  .filter-item label {
    min-width: 80px;
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
</style>