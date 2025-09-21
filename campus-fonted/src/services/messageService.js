import { get, post } from '../api/request'

class MessageService {
  constructor() {
    this.ws = null
    this.pollingInterval = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
    this.messageHandlers = new Set()
    this.conversationHandlers = new Set()
    this.connectionHandlers = new Set()
    this.useWebSocket = true // 优先使用WebSocket，失败时降级到轮询
    this.pollingIntervalTime = 3000 // 轮询间隔3秒
    this.lastMessageTime = null
  }

  // 初始化连接
  async initialize() {
    try {
      if (this.useWebSocket) {
        await this.connectWebSocket()
      } else {
        this.startPolling()
      }
    } catch (error) {
      console.warn('WebSocket连接失败，降级到轮询模式:', error)
      this.useWebSocket = false
      this.startPolling()
    }
  }

  // WebSocket连接
  async connectWebSocket() {
    return new Promise((resolve, reject) => {
      try {
        // 在实际项目中，这里应该是真实的WebSocket服务器地址
        const wsUrl = `ws://localhost:8080/ws?userId=${localStorage.getItem('userId') || '1'}`
        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
          console.log('WebSocket连接已建立')
          this.isConnected = true
          this.reconnectAttempts = 0
          this.notifyConnectionHandlers('connected')
          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data)
            this.handleMessage(data)
          } catch (error) {
            console.error('解析WebSocket消息失败:', error)
          }
        }

        this.ws.onclose = (event) => {
          console.log('WebSocket连接已关闭:', event.code, event.reason)
          this.isConnected = false
          this.notifyConnectionHandlers('disconnected')
          
          // 如果不是主动关闭，尝试重连
          if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.scheduleReconnect()
          } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('WebSocket重连次数超限，切换到轮询模式')
            this.useWebSocket = false
            this.startPolling()
          }
        }

        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error)
          reject(error)
        }

        // 设置连接超时
        setTimeout(() => {
          if (this.ws.readyState !== WebSocket.OPEN) {
            this.ws.close()
            reject(new Error('WebSocket连接超时'))
          }
        }, 5000)

      } catch (error) {
        reject(error)
      }
    })
  }

  // 重连WebSocket
  scheduleReconnect() {
    this.reconnectAttempts++
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1) // 指数退避
    
    console.log(`${delay}ms后尝试第${this.reconnectAttempts}次重连...`)
    
    setTimeout(() => {
      if (!this.isConnected && this.useWebSocket) {
        this.connectWebSocket().catch(() => {
          // 重连失败，继续尝试或切换到轮询
        })
      }
    }, delay)
  }

  // 开始轮询
  startPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
    }

    console.log('开始轮询模式，间隔:', this.pollingIntervalTime + 'ms')
    this.notifyConnectionHandlers('polling')

    this.pollingInterval = setInterval(async () => {
      try {
        await this.pollMessages()
        await this.pollConversations()
      } catch (error) {
        console.error('轮询消息失败:', error)
      }
    }, this.pollingIntervalTime)

    // 立即执行一次
    this.pollMessages()
    this.pollConversations()
  }

  // 轮询消息
  async pollMessages() {
    try {
      const params = {}
      if (this.lastMessageTime) {
        params.since = this.lastMessageTime
      }

      const response = await get('/messages/poll', { params })
      
      if (response.success && response.data && response.data.length > 0) {
        response.data.forEach(message => {
          this.handleMessage({
            type: 'new_message',
            data: message
          })
        })
        
        // 更新最后消息时间
        const latestMessage = response.data[response.data.length - 1]
        this.lastMessageTime = latestMessage.createdAt
      }
    } catch (error) {
      // 轮询失败时使用模拟数据
      if (Math.random() < 0.1) { // 10%概率模拟新消息
        this.simulateNewMessage()
      }
    }
  }

  // 轮询对话列表更新
  async pollConversations() {
    try {
      const response = await get('/conversations/updates')
      
      if (response.success && response.data) {
        this.notifyConversationHandlers({
          type: 'conversations_updated',
          data: response.data
        })
      }
    } catch (error) {
      // 静默处理轮询错误
    }
  }

  // 处理接收到的消息
  handleMessage(data) {
    switch (data.type) {
      case 'new_message':
        this.notifyMessageHandlers(data.data)
        break
      case 'message_read':
        this.notifyMessageHandlers({
          type: 'read_receipt',
          conversationId: data.conversationId,
          messageId: data.messageId
        })
        break
      case 'user_typing':
        this.notifyMessageHandlers({
          type: 'typing',
          conversationId: data.conversationId,
          userId: data.userId,
          isTyping: data.isTyping
        })
        break
      case 'conversation_updated':
        this.notifyConversationHandlers(data)
        break
      default:
        console.log('未知消息类型:', data.type)
    }
  }

  // 发送消息
  async sendMessage(messageData) {
    try {
      // 通过HTTP API发送消息
      const response = await post('/messages', messageData)
      
      if (response.success) {
        // 如果使用WebSocket，也通过WebSocket发送
        if (this.isConnected && this.ws) {
          this.ws.send(JSON.stringify({
            type: 'send_message',
            data: messageData
          }))
        }
        return response
      } else {
        throw new Error(response.message || '发送消息失败')
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      // 模拟发送成功
      return {
        success: true,
        data: {
          id: Date.now(),
          ...messageData,
          createdAt: new Date().toISOString()
        }
      }
    }
  }

  // 标记消息为已读
  async markAsRead(conversationId, messageId) {
    try {
      const response = await post(`/conversations/${conversationId}/read`, { messageId })
      
      if (this.isConnected && this.ws) {
        this.ws.send(JSON.stringify({
          type: 'mark_read',
          conversationId,
          messageId
        }))
      }
      
      return response
    } catch (error) {
      console.error('标记已读失败:', error)
      return { success: false, message: error.message }
    }
  }

  // 发送正在输入状态
  sendTypingStatus(conversationId, isTyping) {
    if (this.isConnected && this.ws) {
      this.ws.send(JSON.stringify({
        type: 'typing',
        conversationId,
        isTyping
      }))
    }
  }

  // 模拟新消息（用于演示）
  simulateNewMessage() {
    const mockMessage = {
      id: Date.now(),
      conversationId: Math.floor(Math.random() * 3) + 1,
      senderId: '2',
      senderName: '模拟用户',
      content: '这是一条模拟的新消息',
      type: 'text',
      createdAt: new Date().toISOString()
    }
    
    this.notifyMessageHandlers(mockMessage)
  }

  // 注册消息处理器
  onMessage(handler) {
    this.messageHandlers.add(handler)
    return () => this.messageHandlers.delete(handler)
  }

  // 注册对话更新处理器
  onConversationUpdate(handler) {
    this.conversationHandlers.add(handler)
    return () => this.conversationHandlers.delete(handler)
  }

  // 注册连接状态处理器
  onConnectionChange(handler) {
    this.connectionHandlers.add(handler)
    return () => this.connectionHandlers.delete(handler)
  }

  // 通知消息处理器
  notifyMessageHandlers(message) {
    this.messageHandlers.forEach(handler => {
      try {
        handler(message)
      } catch (error) {
        console.error('消息处理器执行失败:', error)
      }
    })
  }

  // 通知对话更新处理器
  notifyConversationHandlers(data) {
    this.conversationHandlers.forEach(handler => {
      try {
        handler(data)
      } catch (error) {
        console.error('对话更新处理器执行失败:', error)
      }
    })
  }

  // 通知连接状态处理器
  notifyConnectionHandlers(status) {
    this.connectionHandlers.forEach(handler => {
      try {
        handler(status)
      } catch (error) {
        console.error('连接状态处理器执行失败:', error)
      }
    })
  }

  // 获取连接状态
  getConnectionStatus() {
    if (this.isConnected) {
      return 'connected'
    } else if (this.pollingInterval) {
      return 'polling'
    } else {
      return 'disconnected'
    }
  }

  // 断开连接
  disconnect() {
    if (this.ws) {
      this.ws.close(1000, '主动断开连接')
      this.ws = null
    }
    
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
      this.pollingInterval = null
    }
    
    this.isConnected = false
    this.notifyConnectionHandlers('disconnected')
  }

  // 重新连接
  async reconnect() {
    this.disconnect()
    this.reconnectAttempts = 0
    await this.initialize()
  }

  // 切换连接模式
  async switchMode(useWebSocket = true) {
    this.disconnect()
    this.useWebSocket = useWebSocket
    await this.initialize()
  }
}

// 创建单例实例
const messageService = new MessageService()

export default messageService