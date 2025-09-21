<template>
  <div class="my-products">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>我的商品</h1>
      <router-link to="/publish" class="publish-btn">
        <i class="icon-plus"></i>
        发布新商品
      </router-link>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-tabs">
        <button 
          v-for="status in statusOptions" 
          :key="status.value"
          :class="['tab-btn', { active: currentStatus === status.value }]"
          @click="currentStatus = status.value"
        >
          {{ status.label }}
          <span class="count" v-if="status.count > 0">({{ status.count }})</span>
        </button>
      </div>
      
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索我的商品..."
          @input="handleSearch"
        >
        <i class="icon-search"></i>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="products-container">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无商品</h3>
        <p>{{ currentStatus === 'all' ? '您还没有发布任何商品' : '该状态下暂无商品' }}</p>
        <router-link to="/publish" class="publish-btn-empty">发布第一个商品</router-link>
      </div>

      <div v-else class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-image">
            <img :src="product.image || '/placeholder.jpg'" :alt="product.title">
            <div class="status-badge" :class="product.status">
              {{ getStatusText(product.status) }}
            </div>
          </div>
          
          <div class="product-info">
            <h3 class="product-title">{{ product.title }}</h3>
            <p class="product-price">¥{{ product.price }}</p>
            <div class="product-meta">
              <span class="views">浏览 {{ product.views || 0 }}</span>
              <span class="date">{{ formatDate(product.createdAt) }}</span>
            </div>
          </div>
          
          <div class="product-actions">
            <button @click="viewProduct(product.id)" class="action-btn view">
              <i class="icon-eye"></i>
              查看
            </button>
            <button @click="editProduct(product.id)" class="action-btn edit">
              <i class="icon-edit"></i>
              编辑
            </button>
            <button 
              @click="toggleStatus(product)" 
              class="action-btn toggle"
              :class="product.status"
            >
              <i :class="product.status === 'active' ? 'icon-pause' : 'icon-play'"></i>
              {{ product.status === 'active' ? '下架' : '上架' }}
            </button>
            <button @click="deleteProduct(product.id)" class="action-btn delete">
              <i class="icon-trash"></i>
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="currentPage = currentPage - 1" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        上一页
      </button>
      
      <div class="page-numbers">
        <button 
          v-for="page in visiblePages" 
          :key="page"
          :class="['page-btn', { active: page === currentPage }]"
          @click="currentPage = page"
        >
          {{ page }}
        </button>
      </div>
      
      <button 
        @click="currentPage = currentPage + 1" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除这个商品吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button @click="showDeleteModal = false" class="btn-cancel">取消</button>
          <button @click="confirmDelete" class="btn-confirm">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { get, put, del } from '../api/request'

export default {
  name: 'MyProducts',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(false)
    const products = ref([])
    const currentStatus = ref('all')
    const searchKeyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalCount = ref(0)
    const showDeleteModal = ref(false)
    const deleteProductId = ref(null)
    
    // 状态选项
    const statusOptions = ref([
      { label: '全部', value: 'all', count: 0 },
      { label: '在售', value: 'active', count: 0 },
      { label: '已下架', value: 'inactive', count: 0 },
      { label: '草稿', value: 'draft', count: 0 }
    ])
    
    // 计算属性
    const totalPages = computed(() => {
      return Math.ceil(totalCount.value / pageSize.value)
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const start = Math.max(1, currentPage.value - 2)
      const end = Math.min(totalPages.value, start + 4)
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    })
    
    // 方法
    const fetchProducts = async () => {
      try {
        loading.value = true
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value,
          status: currentStatus.value === 'all' ? undefined : currentStatus.value,
          keyword: searchKeyword.value || undefined
        }
        
        const response = await get('/products/my', params)
        
        if (response.success) {
          products.value = response.data.products || []
          totalCount.value = response.data.totalCount || 0
          
          // 更新状态计数
          if (response.data.statusCounts) {
            statusOptions.value.forEach(option => {
              option.count = response.data.statusCounts[option.value] || 0
            })
          }
        } else {
          console.error('获取商品列表失败:', response.message)
          products.value = []
        }
      } catch (error) {
        console.error('获取商品列表失败:', error)
        products.value = []
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = () => {
      currentPage.value = 1
      fetchProducts()
    }
    
    const viewProduct = (id) => {
      router.push(`/product/${id}`)
    }
    
    const editProduct = (id) => {
      router.push(`/publish?edit=${id}`)
    }
    
    const toggleStatus = async (product) => {
      try {
        const newStatus = product.status === 'active' ? 'inactive' : 'active'
        const response = await put(`/products/${product.id}/status`, { status: newStatus })
        
        if (response.success) {
          product.status = newStatus
          // 重新获取数据以更新计数
          fetchProducts()
        } else {
          alert('操作失败：' + response.message)
        }
      } catch (error) {
        console.error('切换商品状态失败:', error)
        alert('操作失败，请稍后重试')
      }
    }
    
    const deleteProduct = (id) => {
      deleteProductId.value = id
      showDeleteModal.value = true
    }
    
    const confirmDelete = async () => {
      try {
        const response = await del(`/products/${deleteProductId.value}`)
        
        if (response.success) {
          showDeleteModal.value = false
          deleteProductId.value = null
          fetchProducts()
        } else {
          alert('删除失败：' + response.message)
        }
      } catch (error) {
        console.error('删除商品失败:', error)
        alert('删除失败，请稍后重试')
      }
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        active: '在售',
        inactive: '已下架',
        draft: '草稿'
      }
      return statusMap[status] || status
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
    
    // 监听器
    watch([currentStatus, currentPage], () => {
      fetchProducts()
    })
    
    // 生命周期
    onMounted(() => {
      fetchProducts()
    })
    
    return {
      loading,
      products,
      currentStatus,
      searchKeyword,
      currentPage,
      totalPages,
      visiblePages,
      statusOptions,
      showDeleteModal,
      fetchProducts,
      handleSearch,
      viewProduct,
      editProduct,
      toggleStatus,
      deleteProduct,
      confirmDelete,
      getStatusText,
      formatDate
    }
  }
}
</script>

<style scoped>
.my-products {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.page-header h1 {
  margin: 0;
  color: #333;
  font-size: 28px;
}

.publish-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  gap: 20px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 10px 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.tab-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.count {
  font-size: 12px;
  opacity: 0.8;
}

.search-box {
  position: relative;
  width: 300px;
}

.search-box input {
  width: 100%;
  padding: 10px 40px 10px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.search-box .icon-search {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.publish-btn-empty {
  display: inline-block;
  margin-top: 20px;
  padding: 12px 24px;
  background: #667eea;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.publish-btn-empty:hover {
  background: #5a67d8;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.status-badge.active {
  background: #10b981;
}

.status-badge.inactive {
  background: #6b7280;
}

.status-badge.draft {
  background: #f59e0b;
}

.product-info {
  padding: 16px;
}

.product-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 700;
  color: #e53e3e;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.product-actions {
  display: flex;
  padding: 12px 16px;
  gap: 8px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #f8f9fa;
}

.action-btn.view:hover {
  border-color: #667eea;
  color: #667eea;
}

.action-btn.edit:hover {
  border-color: #10b981;
  color: #10b981;
}

.action-btn.toggle.active:hover {
  border-color: #f59e0b;
  color: #f59e0b;
}

.action-btn.toggle.inactive:hover {
  border-color: #10b981;
  color: #10b981;
}

.action-btn.delete:hover {
  border-color: #e53e3e;
  color: #e53e3e;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  gap: 8px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.page-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.modal-overlay {
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

.modal {
  background: white;
  padding: 24px;
  border-radius: 8px;
  max-width: 400px;
  width: 90%;
}

.modal h3 {
  margin: 0 0 16px 0;
  color: #333;
}

.modal p {
  margin: 0 0 24px 0;
  color: #666;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel, .btn-confirm {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: white;
  color: #666;
}

.btn-cancel:hover {
  background: #f8f9fa;
}

.btn-confirm {
  background: #e53e3e;
  color: white;
  border-color: #e53e3e;
}

.btn-confirm:hover {
  background: #dc2626;
}

/* 图标样式 */
.icon-plus::before { content: '+'; }
.icon-search::before { content: '🔍'; }
.icon-eye::before { content: '👁'; }
.icon-edit::before { content: '✏️'; }
.icon-play::before { content: '▶️'; }
.icon-pause::before { content: '⏸️'; }
.icon-trash::before { content: '🗑️'; }

/* 响应式设计 */
@media (max-width: 768px) {
  .my-products {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-tabs {
    flex-wrap: wrap;
  }
  
  .search-box {
    width: 100%;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .product-actions {
    flex-wrap: wrap;
  }
  
  .action-btn {
    min-width: 0;
    flex: 1 1 calc(50% - 4px);
  }
}
</style>