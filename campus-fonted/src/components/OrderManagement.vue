<template>
  <div class="order-management">
    <div class="page-header">
      <h1>订单管理</h1>
      <p>管理你的买入和卖出订单</p>
    </div>
    
    <div class="order-tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'all' }]"
        @click="switchTab('all')"
      >
        全部订单 ({{ totalOrders }})
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'buy' }]"
        @click="switchTab('buy')"
      >
        买入订单 ({{ buyOrders.length }})
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'sell' }]"
        @click="switchTab('sell')"
      >
        卖出订单 ({{ sellOrders.length }})
      </button>
    </div>
    
    <div class="order-filters">
      <div class="filter-group">
        <label>订单状态：</label>
        <select v-model="statusFilter" @change="filterOrders">
          <option value="">全部状态</option>
          <option value="pending">待确认</option>
          <option value="confirmed">已确认</option>
          <option value="shipping">配送中</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>时间范围：</label>
        <select v-model="timeFilter" @change="filterOrders">
          <option value="">全部时间</option>
          <option value="today">今天</option>
          <option value="week">本周</option>
          <option value="month">本月</option>
          <option value="quarter">本季度</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>搜索：</label>
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索商品名称或订单号..."
          @input="filterOrders"
        >
      </div>
      
      <button @click="resetFilters" class="reset-btn">重置筛选</button>
    </div>
    
    <div class="order-stats">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <h3>{{ totalOrders }}</h3>
          <p>总订单数</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-info">
          <h3>¥{{ totalAmount.toFixed(2) }}</h3>
          <p>总交易金额</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <h3>{{ completedOrders }}</h3>
          <p>已完成订单</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <h3>{{ pendingOrders }}</h3>
          <p>待处理订单</p>
        </div>
      </div>
    </div>
    
    <div class="order-list">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载订单中...</p>
      </div>
      
      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无订单</h3>
        <p>{{ getEmptyMessage() }}</p>
        <router-link to="/products" class="browse-btn">去浏览商品</router-link>
      </div>
      
      <div v-else class="orders">
        <div 
          v-for="order in paginatedOrders" 
          :key="order.id"
          class="order-card"
        >
          <div class="order-header">
            <div class="order-info">
              <h3 class="order-number">订单号: {{ order.orderNumber }}</h3>
              <span :class="['order-type', order.type]">
                {{ order.type === 'buy' ? '买入' : '卖出' }}
              </span>
              <span :class="['order-status', order.status]">
                {{ getStatusText(order.status) }}
              </span>
            </div>
            <div class="order-actions">
              <button 
                v-if="canConfirm(order)" 
                @click="confirmOrder(order)"
                class="action-btn confirm"
              >
                确认订单
              </button>
              <button 
                v-if="canCancel(order)" 
                @click="cancelOrder(order)"
                class="action-btn cancel"
              >
                取消订单
              </button>
              <button 
                v-if="canComplete(order)" 
                @click="completeOrder(order)"
                class="action-btn complete"
              >
                确认收货
              </button>
              <button 
                @click="viewOrderDetail(order)"
                class="action-btn detail"
              >
                查看详情
              </button>
            </div>
          </div>
          
          <div class="order-content">
            <div class="product-info">
              <img :src="order.productImage" :alt="order.productName" class="product-image">
              <div class="product-details">
                <h4 class="product-name">{{ order.productName }}</h4>
                <p class="product-description">{{ order.productDescription }}</p>
                <div class="product-meta">
                  <span class="category">{{ order.category }}</span>
                  <span class="quantity">数量: {{ order.quantity }}</span>
                </div>
              </div>
            </div>
            
            <div class="order-details">
              <div class="price-info">
                <div class="unit-price">
                  <label>单价:</label>
                  <span>¥{{ order.unitPrice.toFixed(2) }}</span>
                </div>
                <div class="total-price">
                  <label>总价:</label>
                  <span class="price">¥{{ order.totalPrice.toFixed(2) }}</span>
                </div>
              </div>
              
              <div class="contact-info">
                <div class="contact-person">
                  <label>{{ order.type === 'buy' ? '卖家:' : '买家:' }}</label>
                  <span>{{ order.contactName }}</span>
                </div>
                <div class="contact-phone">
                  <label>联系方式:</label>
                  <span>{{ order.contactPhone }}</span>
                </div>
              </div>
              
              <div class="time-info">
                <div class="create-time">
                  <label>创建时间:</label>
                  <span>{{ formatTime(order.createdAt) }}</span>
                </div>
                <div class="update-time" v-if="order.updatedAt !== order.createdAt">
                  <label>更新时间:</label>
                  <span>{{ formatTime(order.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="order-footer" v-if="order.notes || order.deliveryAddress">
            <div class="delivery-address" v-if="order.deliveryAddress">
              <label>配送地址:</label>
              <span>{{ order.deliveryAddress }}</span>
            </div>
            <div class="order-notes" v-if="order.notes">
              <label>备注:</label>
              <span>{{ order.notes }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="page-btn"
        >
          上一页
        </button>
        
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages"
            :key="page"
            @click="changePage(page)"
            :class="['page-btn', { active: page === currentPage }]"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>
    
    <!-- 订单详情弹窗 -->
    <div v-if="showOrderDetail" class="order-detail-modal" @click="closeOrderDetail">
      <div class="detail-container" @click.stop>
        <div class="detail-header">
          <h2>订单详情</h2>
          <button @click="closeOrderDetail" class="close-btn">×</button>
        </div>
        
        <div class="detail-content" v-if="selectedOrder">
          <div class="detail-section">
            <h3>基本信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>订单号:</label>
                <span>{{ selectedOrder.orderNumber }}</span>
              </div>
              <div class="detail-item">
                <label>订单类型:</label>
                <span>{{ selectedOrder.type === 'buy' ? '买入' : '卖出' }}</span>
              </div>
              <div class="detail-item">
                <label>订单状态:</label>
                <span>{{ getStatusText(selectedOrder.status) }}</span>
              </div>
              <div class="detail-item">
                <label>创建时间:</label>
                <span>{{ formatTime(selectedOrder.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>商品信息</h3>
            <div class="product-detail">
              <img :src="selectedOrder.productImage" :alt="selectedOrder.productName">
              <div class="product-info">
                <h4>{{ selectedOrder.productName }}</h4>
                <p>{{ selectedOrder.productDescription }}</p>
                <div class="product-meta">
                  <span>分类: {{ selectedOrder.category }}</span>
                  <span>数量: {{ selectedOrder.quantity }}</span>
                  <span>单价: ¥{{ selectedOrder.unitPrice.toFixed(2) }}</span>
                  <span>总价: ¥{{ selectedOrder.totalPrice.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>联系信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>{{ selectedOrder.type === 'buy' ? '卖家姓名:' : '买家姓名:' }}</label>
                <span>{{ selectedOrder.contactName }}</span>
              </div>
              <div class="detail-item">
                <label>联系电话:</label>
                <span>{{ selectedOrder.contactPhone }}</span>
              </div>
              <div class="detail-item" v-if="selectedOrder.deliveryAddress">
                <label>配送地址:</label>
                <span>{{ selectedOrder.deliveryAddress }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedOrder.statusHistory">
            <h3>状态历史</h3>
            <div class="status-timeline">
              <div 
                v-for="(history, index) in selectedOrder.statusHistory" 
                :key="index"
                class="timeline-item"
              >
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-status">{{ getStatusText(history.status) }}</div>
                  <div class="timeline-time">{{ formatTime(history.timestamp) }}</div>
                  <div class="timeline-note" v-if="history.note">{{ history.note }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="detail-actions">
          <button 
            v-if="selectedOrder && canConfirm(selectedOrder)" 
            @click="confirmOrder(selectedOrder)"
            class="action-btn confirm"
          >
            确认订单
          </button>
          <button 
            v-if="selectedOrder && canCancel(selectedOrder)" 
            @click="cancelOrder(selectedOrder)"
            class="action-btn cancel"
          >
            取消订单
          </button>
          <button 
            v-if="selectedOrder && canComplete(selectedOrder)" 
            @click="completeOrder(selectedOrder)"
            class="action-btn complete"
          >
            确认收货
          </button>
          <button @click="closeOrderDetail" class="action-btn">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, put } from '../api/request'

export default {
  name: 'OrderManagement',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(false)
    const orders = ref([])
    const activeTab = ref('all')
    const statusFilter = ref('')
    const timeFilter = ref('')
    const searchKeyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const showOrderDetail = ref(false)
    const selectedOrder = ref(null)
    
    // 计算属性
    const buyOrders = computed(() => orders.value.filter(order => order.type === 'buy'))
    const sellOrders = computed(() => orders.value.filter(order => order.type === 'sell'))
    const totalOrders = computed(() => orders.value.length)
    const completedOrders = computed(() => orders.value.filter(order => order.status === 'completed').length)
    const pendingOrders = computed(() => orders.value.filter(order => ['pending', 'confirmed'].includes(order.status)).length)
    const totalAmount = computed(() => orders.value.reduce((sum, order) => sum + order.totalPrice, 0))
    
    const filteredOrders = computed(() => {
      let result = orders.value
      
      // 按标签页筛选
      if (activeTab.value === 'buy') {
        result = result.filter(order => order.type === 'buy')
      } else if (activeTab.value === 'sell') {
        result = result.filter(order => order.type === 'sell')
      }
      
      // 按状态筛选
      if (statusFilter.value) {
        result = result.filter(order => order.status === statusFilter.value)
      }
      
      // 按时间筛选
      if (timeFilter.value) {
        const now = new Date()
        const filterDate = new Date()
        
        switch (timeFilter.value) {
          case 'today':
            filterDate.setHours(0, 0, 0, 0)
            break
          case 'week':
            filterDate.setDate(now.getDate() - 7)
            break
          case 'month':
            filterDate.setMonth(now.getMonth() - 1)
            break
          case 'quarter':
            filterDate.setMonth(now.getMonth() - 3)
            break
        }
        
        result = result.filter(order => new Date(order.createdAt) >= filterDate)
      }
      
      // 按关键词搜索
      if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(order => 
          order.productName.toLowerCase().includes(keyword) ||
          order.orderNumber.toLowerCase().includes(keyword) ||
          order.contactName.toLowerCase().includes(keyword)
        )
      }
      
      return result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    })
    
    const totalPages = computed(() => Math.ceil(filteredOrders.value.length / pageSize.value))
    
    const paginatedOrders = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredOrders.value.slice(start, end)
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(total)
        } else if (current >= total - 3) {
          pages.push(1)
          pages.push('...')
          for (let i = total - 4; i <= total; i++) {
            pages.push(i)
          }
        } else {
          pages.push(1)
          pages.push('...')
          for (let i = current - 1; i <= current + 1; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(total)
        }
      }
      
      return pages
    })
    
    // 辅助函数
    const getProductImage = (product) => {
      if (!product) return '/placeholder.png'
      
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      
      // 优先使用 image 字段
      if (product.image) {
        if (product.image.startsWith('http')) {
          return product.image
        } else if (product.image.startsWith('/uploads') || product.image.startsWith('uploads')) {
          return `${baseUrl}/${product.image.replace(/^\//, '')}`
        } else {
          return `${baseUrl}/uploads/products/${product.image}`
        }
      }
      
      // 如果没有 image，尝试使用 images 数组
      if (product.images) {
        try {
          const imageArray = typeof product.images === 'string' ? JSON.parse(product.images) : product.images
          if (Array.isArray(imageArray) && imageArray.length > 0) {
            const firstImage = imageArray[0]
            if (firstImage.startsWith('http')) {
              return firstImage
            } else if (firstImage.startsWith('/uploads') || firstImage.startsWith('uploads')) {
              return `${baseUrl}/${firstImage.replace(/^\//, '')}`
            } else {
              return `${baseUrl}/uploads/products/${firstImage}`
            }
          }
        } catch (e) {
          // 如果解析失败，尝试直接使用字符串作为图片路径
          if (typeof product.images === 'string' && product.images.trim()) {
            const imagePath = product.images.trim()
            if (imagePath.startsWith('http')) {
              return imagePath
            } else if (imagePath.startsWith('/uploads') || imagePath.startsWith('uploads')) {
              return `${baseUrl}/${imagePath.replace(/^\//, '')}`
            } else {
              return `${baseUrl}/uploads/products/${imagePath}`
            }
          }
        }
      }
      
      return '/placeholder.png'
    }

    const getOrderStatus = (status) => {
      const statusMap = {
        0: 'pending',      // 待确认
        1: 'confirmed',    // 已确认
        2: 'shipping',     // 配送中
        3: 'completed',    // 已完成
        4: 'cancelled'     // 已取消
      }
      return statusMap[status] || 'pending'
    }

    // 方法
    const fetchOrders = async () => {
      try {
        loading.value = true
        
        // 并行获取买家和卖家订单
        const [buyerResponse, sellerResponse] = await Promise.all([
          get('/api/orders/buyer'),
          get('/api/orders/seller')
        ])
        
        const allOrders = []
        
        // 处理买家订单
        if (buyerResponse.code === 200 && buyerResponse.data) {
          const buyOrders = buyerResponse.data.map(order => ({
            id: order.id,
            orderNumber: order.orderNo,
            type: 'buy',
            status: getOrderStatus(order.status),
            productId: order.product?.id,
            productName: order.product?.name || '商品名称',
            productDescription: order.product?.description || '',
            productImage: getProductImage(order.product),
            productCategory: order.product?.category?.name || '未分类',
            quantity: order.quantity || 1,
            unitPrice: order.product?.price || 0,
            totalPrice: order.totalAmount || 0,
            contactName: order.seller?.realName || order.seller?.username || '卖家',
            contactPhone: order.seller?.phone || '未提供',
            deliveryAddress: order.buyerMessage || '',
            notes: order.buyerMessage || '',
            createdAt: order.createdAt,
            updatedAt: order.updatedAt,
            statusHistory: []
          }))
          allOrders.push(...buyOrders)
        }
        
        // 处理卖家订单
        if (sellerResponse.code === 200 && sellerResponse.data) {
          const sellOrders = sellerResponse.data.map(order => ({
            id: order.id,
            orderNumber: order.orderNo,
            type: 'sell',
            status: getOrderStatus(order.status),
            productId: order.product?.id,
            productName: order.product?.name || '商品名称',
            productDescription: order.product?.description || '',
            productImage: getProductImage(order.product),
            productCategory: order.product?.category?.name || '未分类',
            quantity: order.quantity || 1,
            unitPrice: order.product?.price || 0,
            totalPrice: order.totalAmount || 0,
            contactName: order.buyer?.realName || order.buyer?.username || '买家',
            contactPhone: order.buyer?.phone || '未提供',
            deliveryAddress: order.buyerMessage || '',
            notes: order.buyerMessage || '',
            createdAt: order.createdAt,
            updatedAt: order.updatedAt,
            statusHistory: []
          }))
          allOrders.push(...sellOrders)
        }
        
        // 按创建时间排序
        orders.value = allOrders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        
      } catch (error) {
        console.error('获取订单失败:', error)
        orders.value = []
      } finally {
        loading.value = false
      }
    }
    

    
    const switchTab = (tab) => {
      activeTab.value = tab
      currentPage.value = 1
    }
    
    const filterOrders = () => {
      currentPage.value = 1
    }
    
    const resetFilters = () => {
      statusFilter.value = ''
      timeFilter.value = ''
      searchKeyword.value = ''
      currentPage.value = 1
    }
    
    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        pending: '待确认',
        confirmed: '已确认',
        shipping: '配送中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || status
    }
    
    const getEmptyMessage = () => {
      if (activeTab.value === 'buy') {
        return '你还没有买入任何商品'
      } else if (activeTab.value === 'sell') {
        return '你还没有卖出任何商品'
      } else {
        return '你还没有任何订单'
      }
    }
    
    const canConfirm = (order) => {
      return order.status === 'pending' && order.type === 'sell'
    }
    
    const canCancel = (order) => {
      return ['pending', 'confirmed'].includes(order.status)
    }
    
    const canComplete = (order) => {
      return order.status === 'shipping' && order.type === 'buy'
    }
    
    const confirmOrder = async (order) => {
      try {
        let response
        
        // 根据订单类型调用不同的确认接口
        if (order.type === 'buy') {
          // 买家确认订单
          response = await put(`/api/orders/${order.id}/confirm-buyer`)
        } else {
          // 卖家确认订单
          response = await put(`/api/orders/${order.id}/confirm-seller`)
        }
        
        if (response.code === 200) {
          // 更新本地订单状态
          order.status = 'confirmed'
          order.updatedAt = new Date().toISOString()
          
          // 添加状态历史
          if (!order.statusHistory) order.statusHistory = []
          order.statusHistory.push({
            status: 'confirmed',
            timestamp: new Date().toISOString(),
            note: order.type === 'buy' ? '买家已确认订单' : '卖家已确认订单'
          })
          
          alert('订单确认成功！')
        } else {
          alert(response.message || '确认订单失败')
        }
      } catch (error) {
        console.error('确认订单失败:', error)
        alert('确认订单失败，请重试')
      }
    }
    
    const cancelOrder = async (order) => {
      if (!confirm('确定要取消这个订单吗？')) return
      
      try {
        const response = await put(`/api/orders/${order.id}/cancel`)
        
        if (response.code === 200) {
          // 更新本地订单状态
          order.status = 'cancelled'
          order.updatedAt = new Date().toISOString()
          
          // 添加状态历史
          if (!order.statusHistory) order.statusHistory = []
          order.statusHistory.push({
            status: 'cancelled',
            timestamp: new Date().toISOString(),
            note: '订单已取消'
          })
          
          alert('订单取消成功！')
        } else {
          alert(response.message || '取消订单失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        alert('取消订单失败，请重试')
      }
    }
    
    const completeOrder = async (order) => {
      if (!confirm('确认已收到商品吗？')) return
      
      try {
        const response = await put(`/api/orders/${order.id}/complete`)
        
        if (response.code === 200) {
          // 更新本地订单状态
          order.status = 'completed'
          order.updatedAt = new Date().toISOString()
          
          // 添加状态历史
          if (!order.statusHistory) order.statusHistory = []
          order.statusHistory.push({
            status: 'completed',
            timestamp: new Date().toISOString(),
            note: '买家确认收货，订单完成'
          })
          
          alert('订单完成！')
        } else {
          alert(response.message || '完成订单失败')
        }
      } catch (error) {
        console.error('完成订单失败:', error)
        alert('完成订单失败，请重试')
      }
    }
    
    const viewOrderDetail = (order) => {
      selectedOrder.value = order
      showOrderDetail.value = true
    }
    
    const closeOrderDetail = () => {
      showOrderDetail.value = false
      selectedOrder.value = null
    }
    
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleString('zh-CN')
    }
    
    // 生命周期
    onMounted(() => {
      fetchOrders()
    })
    
    return {
      loading,
      orders,
      activeTab,
      statusFilter,
      timeFilter,
      searchKeyword,
      currentPage,
      pageSize,
      showOrderDetail,
      selectedOrder,
      buyOrders,
      sellOrders,
      totalOrders,
      completedOrders,
      pendingOrders,
      totalAmount,
      filteredOrders,
      totalPages,
      paginatedOrders,
      visiblePages,
      fetchOrders,
      switchTab,
      filterOrders,
      resetFilters,
      changePage,
      getStatusText,
      getEmptyMessage,
      canConfirm,
      canCancel,
      canComplete,
      confirmOrder,
      cancelOrder,
      completeOrder,
      viewOrderDetail,
      closeOrderDetail,
      formatTime
    }
  }
}
</script>

<style scoped>
.order-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.95);
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.page-header h1 {
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 16px;
  font-weight: 400;
}

/* 标签页 */
.order-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.tab-btn {
  padding: 14px 28px;
  border: none;
  background: transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #7f8c8d;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 0 6px;
  position: relative;
  overflow: hidden;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.tab-btn:hover:not(.active) {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
  color: #667eea;
}

/* 筛选器 */
.order-filters {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  white-space: nowrap;
}

.filter-group select,
.filter-group input {
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 14px;
  background: white;
  transition: all 0.3s ease;
  outline: none;
}

.filter-group select:focus,
.filter-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-group input {
  width: 220px;
}

.reset-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border: 2px solid #dee2e6;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: linear-gradient(135deg, #e9ecef, #dee2e6);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 统计卡片 */
.order-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 36px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.stat-info h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-info p {
  margin: 4px 0 0 0;
  font-size: 14px;
  font-weight: 500;
  color: #7f8c8d;
}

/* 订单列表 */
.order-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px;
  color: #7f8c8d;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(102, 126, 234, 0.2);
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-state h3 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-state p {
  color: #7f8c8d;
  font-size: 16px;
  margin-bottom: 24px;
}

.browse-btn {
  margin-top: 20px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.browse-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 订单卡片 */
.order-card {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  padding: 24px;
  transition: all 0.3s ease;
  position: relative;
}

.order-card:hover {
  background: rgba(102, 126, 234, 0.02);
}

.order-card:last-child {
  border-bottom: none;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-number {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.order-type {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.order-type.buy {
  background: linear-gradient(135deg, #e8f5e8, #c8e6c9);
  color: #2e7d32;
  box-shadow: 0 2px 8px rgba(46, 125, 50, 0.2);
}

.order-type.sell {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #f57c00;
  box-shadow: 0 2px 8px rgba(245, 124, 0, 0.2);
}

.order-status {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.order-status.pending {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #f57c00;
  box-shadow: 0 2px 8px rgba(245, 124, 0, 0.2);
}

.order-status.confirmed {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1976d2;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.2);
}

.order-status.shipping {
  background: linear-gradient(135deg, #f3e5f5, #e1bee7);
  color: #7b1fa2;
  box-shadow: 0 2px 8px rgba(123, 31, 162, 0.2);
}

.order-status.completed {
  background: linear-gradient(135deg, #e8f5e8, #c8e6c9);
  color: #2e7d32;
  box-shadow: 0 2px 8px rgba(46, 125, 50, 0.2);
}

.order-status.cancelled {
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
  color: #d32f2f;
  box-shadow: 0 2px 8px rgba(211, 47, 47, 0.2);
}

.order-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 16px;
  border: 2px solid transparent;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-btn.confirm {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: white;
  border-color: #4caf50;
}

.action-btn.confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.3);
}

.action-btn.cancel {
  background: linear-gradient(135deg, #f44336, #ef5350);
  color: white;
  border-color: #f44336;
}

.action-btn.cancel:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(244, 67, 54, 0.3);
}

.action-btn.complete {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

.action-btn.detail {
  background: #ff9800;
  color: white;
  border-color: #ff9800;
}

.action-btn:hover {
  opacity: 0.8;
}

.order-content {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.product-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.product-details {
  flex: 1;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.product-description {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 200px;
}

.price-info,
.contact-info,
.time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-info > div,
.contact-info > div,
.time-info > div {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.price-info label,
.contact-info label,
.time-info label {
  color: #666;
}

.total-price .price {
  font-weight: 600;
  color: #f44336;
}

.order-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #666;
}

.delivery-address,
.order-notes {
  margin-bottom: 8px;
}

.delivery-address label,
.order-notes label {
  font-weight: 500;
  margin-right: 8px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  gap: 8px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn.active {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

/* 订单详情弹窗 */
.order-detail-modal {
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

.detail-container {
  background: white;
  border-radius: 8px;
  max-width: 800px;
  width: 90%;
  max-height: 90%;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.detail-header h2 {
  margin: 0;
  color: #333;
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

.detail-content {
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item label {
  font-weight: 500;
  color: #666;
}

.product-detail {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.product-detail img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.product-detail .product-info h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.product-detail .product-info p {
  margin: 0 0 12px 0;
  color: #666;
  line-height: 1.4;
}

.product-detail .product-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.status-timeline {
  position: relative;
  padding-left: 20px;
}

.timeline-item {
  position: relative;
  padding-bottom: 16px;
}

.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: -16px;
  top: 12px;
  bottom: -4px;
  width: 2px;
  background: #e0e0e0;
}

.timeline-dot {
  position: absolute;
  left: -20px;
  top: 4px;
  width: 8px;
  height: 8px;
  background: #2196f3;
  border-radius: 50%;
}

.timeline-content {
  font-size: 14px;
}

.timeline-status {
  font-weight: 500;
  color: #333;
}

.timeline-time {
  color: #666;
  font-size: 12px;
  margin-top: 2px;
}

.timeline-note {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-management {
    padding: 10px;
  }
  
  .order-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group input {
    width: 100%;
  }
  
  .order-stats {
    grid-template-columns: 1fr;
  }
  
  .order-content {
    flex-direction: column;
  }
  
  .order-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .order-info {
    flex-wrap: wrap;
  }
  
  .order-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .detail-container {
    width: 95%;
    margin: 20px;
  }
  
  .product-detail {
    flex-direction: column;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>