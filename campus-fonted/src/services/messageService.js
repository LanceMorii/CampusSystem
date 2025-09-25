// import SockJS from 'sockjs-client' // 已切换为原生 WebSocket
import { Client } from '@stomp/stompjs'

class MessageService {
  constructor() {
    this.client = null
    this.isConnected = false
    this.messageHandlers = []
    this.connectionHandlers = []
    this.conversationHandlers = []
  }

  // 初始化WebSocket连接（使用原生 WebSocket，避免 SockJS 的 /info 404）
  async initialize() {
    try {
      console.log('初始化WebSocket连接...')
      
      // 获取token
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('未找到认证token，无法建立WebSocket连接')
        return false
      }
      
      // 计算后端 WS 地址：考虑到后端设置了 context-path /api/v1，实际端点为 /api/v1/ws
      const apiBaseEnv = (import.meta && import.meta.env && import.meta.env.VITE_API_BASE_URL) ? import.meta.env.VITE_API_BASE_URL : 'http://localhost:8080'
      let wsUrl
      try {
        const api = new URL(apiBaseEnv)
        const wsProtocol = api.protocol === 'https:' ? 'wss:' : 'ws:'
        const basePath = api.pathname && api.pathname !== '/' ? api.pathname.replace(/\/+$/, '') : ''
        const ctxPath = basePath.endsWith('/api/v1') ? basePath : `${basePath}/api/v1`
        wsUrl = `${wsProtocol}//${api.host}${ctxPath}/ws?token=${encodeURIComponent(token)}`
      } catch (e) {
        // 兜底处理：若 env 值不是合法 URL，则直接替换前缀并拼接 context-path
        const base = apiBaseEnv.startsWith('https') ? apiBaseEnv.replace(/^https/, 'wss') : apiBaseEnv.replace(/^http/, 'ws')
        const trimmed = base.replace(/\/+$/, '')
        const withCtx = trimmed.endsWith('/api/v1') ? trimmed : `${trimmed}/api/v1`
        wsUrl = `${withCtx}/ws?token=${encodeURIComponent(token)}`
      }
      console.log('连接原生WebSocket地址:', wsUrl)
      
      // 创建STOMP客户端（使用原生 WebSocket）
      this.client = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          'Authorization': `Bearer ${token}`
        },
        // 禁用自动重连，手动控制
        reconnectDelay: 0,
        // 启用心跳
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: (str) => {
          console.log('STOMP Debug:', str)
        },
        onConnect: (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.isConnected = true
          this.onConnected()
          // 通知连接状态变更
          this.connectionHandlers.forEach(handler => {
            try { handler(true) } catch (e) { console.error('连接回调错误:', e) }
          })
        },
        onDisconnect: () => {
          console.log('WebSocket连接断开')
          this.isConnected = false
          // 通知连接状态变更
          this.connectionHandlers.forEach(handler => {
            try { handler(false) } catch (e) { console.error('连接回调错误:', e) }
          })
        },
        onStompError: (frame) => {
          console.error('STOMP错误:', frame)
        }
      })

      // 激活连接
      this.client.activate()
      
      return true
    } catch (error) {
      console.error('WebSocket初始化失败:', error)
      return false
    }
  }

  // 连接成功后的处理
  onConnected() {
    try {
      // 从localStorage获取userId，如果没有则从userInfo中提取
      let userId = localStorage.getItem('userId')
      if (!userId) {
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo && userInfo !== 'undefined') {
          try {
            const parsedUserInfo = JSON.parse(userInfo)
            userId = parsedUserInfo.id || parsedUserInfo.userId
            if (userId) {
              localStorage.setItem('userId', userId.toString())
            }
          } catch (error) {
            console.error('解析用户信息失败:', error)
          }
        }
      }
      
      if (!userId) {
        console.error('无法获取用户ID，无法订阅消息')
        return
      }
      
      console.log('使用用户ID订阅消息:', userId)
      
      // 订阅用户专属消息目的地（后端推送到 /user/queue/new-message）
      this.client.subscribe(`/user/queue/new-message`, (message) => {
        console.log('收到新消息通知:', message.body)
        try {
          const payload = JSON.parse(message.body)
          this.handleMessage(payload)
        } catch (e) {
          console.error('解析新消息失败:', e, message.body)
        }
      })
      
      // 订阅系统通知
      this.client.subscribe('/topic/notifications', (message) => {
        console.log('收到系统通知:', message.body)
        try {
          const payload = JSON.parse(message.body)
          this.handleMessage(payload)
        } catch (e) {
          console.error('解析系统通知失败:', e, message.body)
        }
      })
      
      console.log('消息订阅完成')
    } catch (error) {
      console.error('订阅消息失败:', error)
    }
  }

  // 处理收到的消息（后端直接推送 MessageResponse）
  handleMessage(data) {
    console.log('处理消息:', data)
    try {
      const mapped = this.mapMessage(data)
      this.messageHandlers.forEach(handler => {
        try { handler(mapped) } catch (error) { console.error('消息处理器错误:', error) }
      })
    } catch (err) {
      console.error('处理消息失败:', err, data)
    }
  }

  // 发送消息
  sendMessage(message) {
    if (!this.isConnected || !this.client) {
      console.error('WebSocket未连接，无法发送消息')
      return false
    }

    try {
      this.client.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message)
      })
      console.log('消息发送成功:', message)
      return true
    } catch (error) {
      console.error('发送消息失败:', error)
      return false
    }
  }

  // 订阅消息（返回取消订阅函数）
  onMessage(handler) {
    this.messageHandlers.push(handler)
    return () => {
      const index = this.messageHandlers.indexOf(handler)
      if (index > -1) this.messageHandlers.splice(index, 1)
    }
  }

  // 会话更新订阅（返回取消订阅函数）
  onConversationUpdate(handler) {
    this.conversationHandlers.push(handler)
    return () => {
      const index = this.conversationHandlers.indexOf(handler)
      if (index > -1) this.conversationHandlers.splice(index, 1)
    }
  }

  // 连接状态订阅（立即回调一次当前状态；返回取消订阅函数）
  onConnectionChange(handler) {
    this.connectionHandlers.push(handler)
    try { handler(this.isConnected) } catch (e) { console.error('连接回调错误:', e) }
    return () => {
      const index = this.connectionHandlers.indexOf(handler)
      if (index > -1) this.connectionHandlers.splice(index, 1)
    }
  }

  // 标记消息/会话为已读
  markAsRead(conversationId, messageId) {
    if (!this.isConnected || !this.client) {
      console.error('WebSocket未连接，无法标记已读')
      return false
    }
    try {
      if (!messageId) {
        console.error('markAsRead 需要提供 messageId')
        return false
      }
      const body = { conversationId, messageId }
      this.client.publish({
        destination: '/app/chat.markRead',
        body: JSON.stringify(body)
      })
      console.log('已发送已读标记:', body)
      return true
    } catch (error) {
      console.error('发送已读标记失败:', error)
      return false
    }
  }

  // 输入中状态通知
  typing(toUserId, conversationId, isTyping = true) {
    if (!this.isConnected || !this.client) {
      console.error('WebSocket未连接，无法发送输入状态')
      return false
    }
    try {
      const body = { toUserId, conversationId, isTyping: !!isTyping }
      this.client.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(body)
      })
      console.log('已发送输入状态:', body)
      return true
    } catch (error) {
      console.error('发送输入状态失败:', error)
      return false
    }
  }

  // 将后端MessageResponse映射为前端使用的消息模型
  mapMessage(msg) {
    if (!msg || typeof msg !== 'object') return msg
    const currentId = parseInt(localStorage.getItem('userId') || '0', 10)
    const convId = msg.conversationId != null ? msg.conversationId : (msg.fromUserId === currentId ? msg.toUserId : msg.fromUserId)
    return {
      id: msg.id,
      conversationId: convId,
      fromUserId: msg.fromUserId,
      toUserId: msg.toUserId,
      productId: msg.productId,
      content: msg.content,
      type: msg.type,
      isRead: msg.isRead,
      isUnread: msg.isUnread !== undefined ? msg.isUnread : (msg.isRead === 0),
      readStatus: msg.readStatus,
      readTime: msg.readTime,
      createdAt: msg.createTime,
      // 顶层常用字段，便于MessageCenter直接使用
      senderId: msg.fromUserId,
      senderName: msg.fromNickname || msg.fromUsername,
      senderAvatar: msg.fromAvatar,
      receiverId: msg.toUserId,
      receiverName: msg.toNickname || msg.toUsername,
      receiverAvatar: msg.toAvatar,
      // 保留嵌套对象以兼容其他组件
      sender: {
        id: msg.fromUserId,
        username: msg.fromUsername,
        nickname: msg.fromNickname,
        avatar: msg.fromAvatar
      },
      receiver: {
        id: msg.toUserId,
        username: msg.toUsername,
        nickname: msg.toNickname,
        avatar: msg.toAvatar
      },
      product: msg.productId ? {
        id: msg.productId,
        title: msg.productTitle,
        image: msg.productImage
      } : null
    }
  }

  // 添加消息处理器
  addMessageHandler(handler) {
    this.messageHandlers.push(handler)
  }

  // 移除消息处理器
  removeMessageHandler(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
  }

  // 断开连接
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.isConnected = false
      console.log('WebSocket连接已断开')
    }
  }

  // 新增：重连方法，供登录后刷新token使用
  async reconnect() {
    try {
      console.log('准备重连WebSocket...')
      if (this.client) {
        this.client.deactivate()
        this.client = null
        this.isConnected = false
      }
      // 重新初始化（会读取最新的token）
      return await this.initialize()
    } catch (e) {
      console.error('重连失败:', e)
      return false
    }
  }

  // 获取连接状态
  getConnectionStatus() {
    return this.isConnected
  }
}

// 创建单例
const messageService = new MessageService()

export default messageService