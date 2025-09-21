<template>
  <div class="message-center">
    <div class="message-layout">
      <!-- 左侧对话列表 -->
      <div class="conversation-list">
        <div class="list-header">
          <h2>消息中心</h2>
          <div class="search-box">
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="搜索对话..."
              @input="filterConversations"
            >
            <i class="icon-search"></i>
          </div>
        </div>
        
        <div class="conversation-items">
          <div v-if="loading" class="loading">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
          
          <div v-else-if="filteredConversations.length === 0" class="empty-state">
            <div class="empty-icon">💬</div>
            <p>暂无对话</p>
          </div>
          
          <div 
            v-else
            v-for="conversation in filteredConversations" 
            :key="conversation.id"
            :class="['conversation-item', { active: currentConversation?.id === conversation.id }]"
            @click="selectConversation(conversation)"
          >
            <div class="avatar">
              <img :src="conversation.avatar || '/default-avatar.png'" :alt="conversation.name">
              <div v-if="conversation.unreadCount > 0" class="unread-badge">
                {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
              </div>
            </div>
            
            <div class="conversation-info">
              <div class="conversation-header">
                <h4 class="name">{{ conversation.name }}</h4>
                <span class="time">{{ formatTime(conversation.lastMessageTime) }}</span>
              </div>
              <div class="last-message">
                <span :class="{ unread: conversation.unreadCount > 0 }">
                  {{ conversation.lastMessage || '暂无消息' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧聊天界面 -->
      <div class="chat-area">
        <div v-if="!currentConversation" class="no-conversation">
          <div class="no-conversation-icon">💬</div>
          <h3>选择一个对话开始聊天</h3>
          <p>从左侧选择一个对话，或者开始新的对话</p>
        </div>
        
        <div v-else class="chat-container">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-user-info">
              <img :src="currentConversation.avatar || '/default-avatar.png'" :alt="currentConversation.name">
              <div class="user-details">
                <h3>{{ currentConversation.name }}</h3>
                <span class="status">{{ currentConversation.online ? '在线' : '离线' }}</span>
              </div>
            </div>
            <div class="chat-actions">
              <div class="connection-status" :class="connectionStatus">
                <span v-if="connectionStatus === 'connected'">🟢 实时连接</span>
                <span v-else-if="connectionStatus === 'polling'">🟡 轮询模式</span>
                <span v-else>🔴 连接断开</span>
              </div>
              <button @click="viewUserProfile" class="action-btn">
                <i class="icon-user"></i>
                查看资料
              </button>
              <button @click="viewRelatedProduct" class="action-btn" v-if="currentConversation.productId">
                <i class="icon-product"></i>
                查看商品
              </button>
            </div>
          </div>
          
          <!-- 消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div v-if="messagesLoading" class="messages-loading">
              <div class="spinner"></div>
              <p>加载消息中...</p>
            </div>
            
            <div v-else class="messages">
              <div 
                v-for="message in messages" 
                :key="message.id"
                :class="['message-item', { 'own-message': message.senderId === currentUserId }]"
              >
                <div class="message-avatar" v-if="message.senderId !== currentUserId">
                  <img :src="message.senderAvatar || '/default-avatar.png'" :alt="message.senderName">
                </div>
                
                <div class="message-content">
                  <div class="message-bubble">
                    <div v-if="message.type === 'text'" class="text-message">
                      {{ message.content }}
                    </div>
                    <div v-else-if="message.type === 'image'" class="image-message">
                      <img :src="message.content" :alt="'图片消息'" @click="previewImage(message.content)">
                    </div>
                    <div v-else-if="message.type === 'product'" class="product-message">
                      <div class="product-card">
                        <img :src="message.productInfo.image" :alt="message.productInfo.name">
                        <div class="product-info">
                          <h4>{{ message.productInfo.name }}</h4>
                          <p class="price">¥{{ message.productInfo.price }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="message-time">
                    {{ formatMessageTime(message.createdAt) }}
                  </div>
                </div>
                
                <div class="message-avatar" v-if="message.senderId === currentUserId">
                  <img :src="message.senderAvatar || '/default-avatar.png'" :alt="message.senderName">
                </div>
              </div>
            </div>
          </div>
          
          <!-- 消息输入区 -->
          <div class="message-input-area">
            <div class="input-toolbar">
              <button @click="selectImage" class="toolbar-btn">
                <i class="icon-image"></i>
              </button>
              <button @click="selectProduct" class="toolbar-btn">
                <i class="icon-product"></i>
              </button>
            </div>
            
            <div class="input-container">
              <textarea 
                v-model="newMessage"
                placeholder="输入消息..."
                @keydown.enter.prevent="sendMessage"
                @keydown.ctrl.enter="newMessage += '\n'"
                rows="1"
                ref="messageInputRef"
              ></textarea>
              <button 
                @click="sendMessage" 
                :disabled="!newMessage.trim() || sending"
                class="send-btn"
              >
                <i v-if="sending" class="icon-loading"></i>
                <i v-else class="icon-send"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片预览弹窗 -->
    <div v-if="previewImageUrl" class="image-preview-modal" @click="closeImagePreview">
      <div class="preview-container" @click.stop>
        <img :src="previewImageUrl" alt="图片预览">
        <button @click="closeImagePreview" class="close-btn">×</button>
      </div>
    </div>
    
    <!-- 商品选择弹窗 -->
    <div v-if="showProductSelector" class="product-selector-modal" @click="closeProductSelector">
      <div class="selector-container" @click.stop>
        <h3>选择要分享的商品</h3>
        <div class="product-list">
          <div 
            v-for="product in userProducts" 
            :key="product.id"
            class="product-item"
            @click="shareProduct(product)"
          >
            <img :src="product.image" :alt="product.name">
            <div class="product-info">
              <h4>{{ product.name }}</h4>
              <p class="price">¥{{ product.price }}</p>
            </div>
          </div>
        </div>
        <button @click="closeProductSelector" class="close-btn">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { get, post } from '../api/request'
import messageService from '../services/messageService'

export default {
  name: 'MessageCenter',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(false)
    const messagesLoading = ref(false)
    const sending = ref(false)
    const conversations = ref([])
    const filteredConversations = ref([])
    const currentConversation = ref(null)
    const messages = ref([])
    const newMessage = ref('')
    const searchKeyword = ref('')
    const currentUserId = ref(localStorage.getItem('userId') || '1')
    const previewImageUrl = ref('')
    const showProductSelector = ref(false)
    const userProducts = ref([])
    const connectionStatus = ref('disconnected')
    const typingUsers = ref(new Set())
    
    // 引用
    const messageListRef = ref(null)
    const messageInputRef = ref(null)
    
    // 消息服务的清理函数
    let messageUnsubscribe = null
    let conversationUnsubscribe = null
    let connectionUnsubscribe = null
    
    // 计算属性
    const hasUnreadMessages = computed(() => {
      return conversations.value.some(conv => conv.unreadCount > 0)
    })
    
    // 方法
    const fetchConversations = async () => {
      try {
        loading.value = true
        const response = await get('/messages/conversations')
        
        if (response.code === 200) {
          conversations.value = response.data || []
          filteredConversations.value = conversations.value
        } else {
          console.log('获取对话列表失败:', response.message)
        }
      } catch (error) {
        console.log('获取对话列表失败:', error.message)
      } finally {
        loading.value = false
      }
    }
    
    const fetchMessages = async (conversationId) => {
      try {
        messagesLoading.value = true
        const response = await get(`/messages/conversation/${conversationId}`)
        
        if (response.code === 200) {
          messages.value = response.data || []
          console.log('消息列表获取成功：', response.data)
        } else {
          console.log('获取消息列表失败:', response.message)
          // 如果API失败，使用模拟数据
          messages.value = mockMessages
        }
      } catch (error) {
        console.error('获取消息列表失败:', error)
        // 使用模拟数据
        messages.value = mockMessages
      } finally {
        messagesLoading.value = false
        await nextTick()
        scrollToBottom()
      }
    }
    
    const selectConversation = async (conversation) => {
      currentConversation.value = conversation
      await fetchMessages(conversation.id)
      
      // 标记为已读
      if (conversation.unreadCount > 0) {
        markAsRead(conversation.id)
        conversation.unreadCount = 0
      }
    }
    
    const sendMessage = async () => {
      if (!newMessage.value.trim() || !currentConversation.value || sending.value) return
      
      try {
        sending.value = true
        
        const messageData = {
          content: newMessage.value.trim(),
          receiverId: currentConversation.value.id,
          type: 'text'
        }
        
        const response = await post('/messages/send', messageData)
        
        if (response.code === 200) {
          // 添加消息到本地列表
          const newMsg = {
            id: response.data.id || Date.now(),
            senderId: currentUserId.value,
            senderName: '我',
            content: newMessage.value.trim(),
            type: 'text',
            createTime: new Date().toISOString()
          }
          messages.value.push(newMsg)
          
          // 清空输入框
          newMessage.value = ''
          
          // 滚动到底部
          await nextTick()
          scrollToBottom()
          
          console.log('消息发送成功')
        } else {
          console.log('消息发送失败:', response.message)
        }
      } catch (error) {
        console.error('发送消息失败:', error)
      } finally {
        sending.value = false
      }
    }
    
    const filterConversations = () => {
      if (!searchKeyword.value.trim()) {
        filteredConversations.value = conversations.value
      } else {
        filteredConversations.value = conversations.value.filter(conv =>
          conv.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
        )
      }
    }
    
    const markAsRead = async (conversationId) => {
      try {
        await messageService.markAsRead(conversationId)
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    }
    
    const scrollToBottom = () => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    }
    
    const selectImage = () => {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = 'image/*'
      input.onchange = (e) => {
        const file = e.target.files[0]
        if (file) {
          // 这里应该上传图片并发送消息
          console.log('选择了图片:', file.name)
        }
      }
      input.click()
    }
    
    const selectProduct = async () => {
      try {
        const response = await get('/products/my')
        if (response.success) {
          userProducts.value = response.data.products || []
        } else {
          console.log('获取用户商品失败:', response.message)
        }
        showProductSelector.value = true
      } catch (error) {
        console.log('获取用户商品失败:', error.message)
        showProductSelector.value = true
      }
    }
    
    const shareProduct = async (product) => {
      try {
        const messageData = {
          conversationId: currentConversation.value.id,
          content: JSON.stringify(product),
          type: 'product',
          productInfo: product
        }
        
        const response = await post('/messages', messageData)
        
        if (response.success || true) { // 模拟成功
          const newMsg = {
            id: Date.now(),
            senderId: currentUserId.value,
            senderName: '我',
            content: '',
            type: 'product',
            productInfo: product,
            createdAt: new Date().toISOString()
          }
          messages.value.push(newMsg)
          
          await nextTick()
          scrollToBottom()
        }
      } catch (error) {
        console.error('分享商品失败:', error)
      } finally {
        closeProductSelector()
      }
    }
    
    const previewImage = (imageUrl) => {
      previewImageUrl.value = imageUrl
    }
    
    const closeImagePreview = () => {
      previewImageUrl.value = ''
    }
    
    const closeProductSelector = () => {
      showProductSelector.value = false
    }
    
    const viewUserProfile = () => {
      // 跳转到用户资料页面
      console.log('查看用户资料')
    }
    
    const viewRelatedProduct = () => {
      if (currentConversation.value.productId) {
        router.push(`/product/${currentConversation.value.productId}`)
      }
    }
    
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      return date.toLocaleDateString('zh-CN')
    }
    
    const formatMessageTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    
    // 生命周期
    onMounted(async () => {
      await fetchConversations()
      await initializeMessageService()
    })
    
    onUnmounted(() => {
      cleanupMessageService()
    })
    
    // 初始化消息服务
    const initializeMessageService = async () => {
      try {
        // 初始化连接
        await messageService.initialize()
        
        // 注册消息处理器
        messageUnsubscribe = messageService.onMessage(handleNewMessage)
        
        // 注册对话更新处理器
        conversationUnsubscribe = messageService.onConversationUpdate(handleConversationUpdate)
        
        // 注册连接状态处理器
        connectionUnsubscribe = messageService.onConnectionChange(handleConnectionChange)
        
        console.log('消息服务初始化完成')
      } catch (error) {
        console.error('消息服务初始化失败:', error)
      }
    }
    
    // 清理消息服务
    const cleanupMessageService = () => {
      if (messageUnsubscribe) messageUnsubscribe()
      if (conversationUnsubscribe) conversationUnsubscribe()
      if (connectionUnsubscribe) connectionUnsubscribe()
      messageService.disconnect()
    }
    
    // 处理新消息
    const handleNewMessage = (message) => {
      if (typeof message === 'object' && message.type === 'typing') {
        // 处理正在输入状态
        if (message.isTyping) {
          typingUsers.value.add(message.userId)
        } else {
          typingUsers.value.delete(message.userId)
        }
        return
      }
      
      if (typeof message === 'object' && message.type === 'read_receipt') {
        // 处理已读回执
        console.log('消息已读:', message)
        return
      }
      
      // 处理普通消息
      if (currentConversation.value && message.conversationId === currentConversation.value.id) {
        // 如果是当前对话的消息，直接添加到消息列表
        messages.value.push(message)
        nextTick(() => {
          scrollToBottom()
        })
        
        // 如果不是自己发送的消息，标记为已读
        if (message.senderId !== currentUserId.value) {
          messageService.markAsRead(message.conversationId, message.id)
        }
      }
      
      // 更新对话列表中的最后消息
      const conversation = conversations.value.find(conv => conv.id === message.conversationId)
      if (conversation) {
        conversation.lastMessage = message.content || '发送了一条消息'
        conversation.lastMessageTime = message.createdAt
        
        // 如果不是当前对话且不是自己发送的消息，增加未读数
        if ((!currentConversation.value || message.conversationId !== currentConversation.value.id) 
            && message.senderId !== currentUserId.value) {
          conversation.unreadCount = (conversation.unreadCount || 0) + 1
        }
        
        // 将对话移到列表顶部
        const index = conversations.value.indexOf(conversation)
        if (index > 0) {
          conversations.value.splice(index, 1)
          conversations.value.unshift(conversation)
          filterConversations() // 重新过滤
        }
      }
    }
    
    // 处理对话更新
    const handleConversationUpdate = (data) => {
      if (data.type === 'conversations_updated') {
        // 更新对话列表
        conversations.value = data.data || conversations.value
        filterConversations()
      }
    }
    
    // 处理连接状态变化
    const handleConnectionChange = (status) => {
      connectionStatus.value = status
      console.log('连接状态变化:', status)
    }
    
    return {
      loading,
      messagesLoading,
      sending,
      conversations,
      filteredConversations,
      currentConversation,
      messages,
      newMessage,
      searchKeyword,
      currentUserId,
      previewImageUrl,
      showProductSelector,
      userProducts,
      messageListRef,
      messageInputRef,
      connectionStatus,
        typingUsers,
        hasUnreadMessages,
      fetchConversations,
      fetchMessages,
      selectConversation,
      sendMessage,
      filterConversations,
      selectImage,
      selectProduct,
      shareProduct,
      previewImage,
      closeImagePreview,
      closeProductSelector,
      viewUserProfile,
      viewRelatedProduct,
      formatTime,
      formatMessageTime
    }
  }
}
</script>

<style scoped>
.message-center {
  height: 100vh;
  background: #f5f5f5;
}

.message-layout {
  display: flex;
  height: 100%;
}

/* 左侧对话列表 */
.conversation-list {
  width: 320px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.list-header h2 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 20px;
}

.search-box {
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 10px 40px 10px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  background: #f8f9fa;
}

.search-box .icon-search {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.conversation-item:hover {
  background: #f8f9fa;
}

.conversation-item.active {
  background: #e3f2fd;
  border-right: 3px solid #2196f3;
}

.avatar {
  position: relative;
  margin-right: 12px;
}

.avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #f44336;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 12px;
  min-width: 18px;
  text-align: center;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.name {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.last-message {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-message .unread {
  font-weight: 600;
  color: #333;
}

/* 右侧聊天界面 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.no-conversation {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}

.no-conversation-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-user-info {
  display: flex;
  align-items: center;
}

.chat-user-info img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
}

.user-details h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.status {
  font-size: 12px;
  color: #4caf50;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.connection-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.connection-status.connected {
  background: #e8f5e8;
  color: #2e7d32;
}

.connection-status.polling {
  background: #fff3e0;
  color: #f57c00;
}

.connection-status.disconnected {
  background: #ffebee;
  color: #d32f2f;
}

.action-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover {
  background: #f5f5f5;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.messages-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #666;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-end;
}

.message-item.own-message {
  flex-direction: row-reverse;
}

.message-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin: 0 8px;
}

.message-content {
  max-width: 60%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  margin-bottom: 4px;
}

.message-item:not(.own-message) .message-bubble {
  background: #f0f0f0;
  color: #333;
}

.message-item.own-message .message-bubble {
  background: #2196f3;
  color: white;
}

.image-message img {
  max-width: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.product-message .product-card {
  display: flex;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  max-width: 250px;
}

.product-card img {
  width: 60px;
  height: 60px;
  object-fit: cover;
}

.product-card .product-info {
  padding: 8px 12px;
  flex: 1;
}

.product-card h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
}

.product-card .price {
  margin: 0;
  font-size: 14px;
  color: #f44336;
  font-weight: 600;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: center;
}

.message-input-area {
  border-top: 1px solid #e0e0e0;
  padding: 16px 20px;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.toolbar-btn {
  padding: 8px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.toolbar-btn:hover {
  background: #f5f5f5;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.input-container textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  resize: none;
  font-size: 14px;
  line-height: 1.4;
  max-height: 100px;
}

.send-btn {
  padding: 12px 16px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 16px;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 加载和空状态 */
.loading, .spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #2196f3;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
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
  padding: 40px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 弹窗样式 */
.image-preview-modal, .product-selector-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.preview-container {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.preview-container img {
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 20px;
  cursor: pointer;
}

.selector-container {
  background: white;
  border-radius: 8px;
  padding: 24px;
  max-width: 500px;
  width: 90%;
  max-height: 80%;
  overflow-y: auto;
}

.selector-container h3 {
  margin: 0 0 20px 0;
  text-align: center;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.product-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-item:hover {
  border-color: #2196f3;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.2);
}

.product-item img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 8px;
}

.product-item h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
}

.product-item .price {
  margin: 0;
  font-size: 16px;
  color: #f44336;
  font-weight: 600;
}

.selector-container .close-btn {
  position: static;
  width: 100%;
  padding: 10px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

/* 图标样式 */
.icon-search::before { content: '🔍'; }
.icon-user::before { content: '👤'; }
.icon-product::before { content: '📦'; }
.icon-image::before { content: '🖼️'; }
.icon-send::before { content: '➤'; }
.icon-loading::before { content: '⏳'; }

/* 响应式设计 */
@media (max-width: 768px) {
  .message-layout {
    flex-direction: column;
  }
  
  .conversation-list {
    width: 100%;
    height: 40%;
  }
  
  .chat-area {
    height: 60%;
  }
  
  .message-item .message-content {
    max-width: 80%;
  }
}
</style>