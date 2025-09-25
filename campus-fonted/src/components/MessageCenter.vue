<template>
  <div class="mc">
    <aside class="mc-left">
      <header class="mc-left-header">
        <h2>消息中心</h2>
        <input v-model="keyword" class="mc-search" placeholder="搜索联系人" @input="filterConvs" />
      </header>
      <div class="mc-list" v-if="!loading">
        <div v-if="filteredConvs.length===0" class="mc-empty">暂无对话</div>
          <button v-for="c in filteredConvs" :key="c.id" class="mc-item" :class="{active: current?.id===c.id}" @click="selectConv(c)">
            <img :src="c.avatar || '/logo.jpg'" alt="avatar" />
            <div class="mc-item-main">
              <div class="mc-item-top">
                <span class="name">{{ c.name }}</span>
                <span class="time">{{ formatTime(c.lastMessageTime) }}</span>
              </div>
              <div class="mc-item-bottom">
                <span class="last" :class="{unread: c.unreadCount>0}">{{ c.lastMessage || '暂无消息' }}</span>
                <div class="status-indicators">
                  <span v-if="c.unreadCount>0" class="badge pulse">{{ c.unreadCount>99?'99+':c.unreadCount }}</span>
                  <span v-else-if="c.unreadCount === 0 && c.lastMessage" class="read-indicator">✓</span>
                </div>
              </div>
            </div>
          </button>
      </div>
      <div v-else class="mc-loading">加载中...</div>
    </aside>

    <section class="mc-chat">
      <div v-if="!current" class="mc-welcome">选择一个对话开始聊天</div>
      <div v-else class="mc-chat-wrap">
        <header class="mc-chat-header">
          <div class="user-info">
            <img :src="current.avatar || '/logo.jpg'" alt="avatar" class="user-avatar" />
            <div class="user-details">
              <h3 class="user-name">{{ current.name }}</h3>
              <div class="user-status">
                <span :class="statusClass" class="connection-status">{{ connStatusText }}</span>
                <span v-if="current.productTitle" class="product-info">
                  关于商品：{{ current.productTitle }}
                </span>
              </div>
            </div>
          </div>
          <div class="chat-actions">
            <button class="action-btn" @click="clearUnread" title="标记全部已读">
              <span class="icon">✓</span>
            </button>
            <button class="action-btn" @click="refreshMessages" title="刷新消息">
              <span class="icon">↻</span>
            </button>
          </div>
        </header>
        <main class="mc-msgs" ref="msgsRef">
          <div v-for="m in msgs" :key="m.id" class="msg" :class="{own: m.senderId===me}">
            <div class="msg-avatar" v-if="!m.senderId || m.senderId !== me">
              <img :src="m.senderAvatar || '/logo.jpg'" alt="avatar" />
            </div>
            <div class="msg-content">
              <div class="bubble" :class="{ unread: m.senderId !== me && m.isUnread }">
                <template v-if="m.type==='text'">{{ m.content }}</template>
                <template v-else-if="m.type==='image'"><img class="img" :src="m.content" alt="图片" /></template>
                <template v-else>{{ m.content }}</template>
                <!-- 未读标识 -->
                <div v-if="m.senderId !== me && m.isUnread" class="unread-indicator">
                  <span class="unread-dot"></span>
                </div>
              </div>
              <div class="msg-meta">
                <span class="ts">{{ formatTime(m.createdAt) }}</span>
                <!-- 已读状态 -->
                <span v-if="m.senderId === me" class="read-status" :class="getReadStatusClass(m)">
                  {{ getReadStatusText(m) }}
                </span>
              </div>
            </div>
            <div class="msg-avatar" v-if="m.senderId === me">
              <img :src="getUserAvatar() || '/logo.jpg'" alt="avatar" />
            </div>
          </div>
        </main>
        <footer class="mc-input">
          <textarea v-model="draft" rows="1" placeholder="输入消息..." @keydown.enter.prevent="send" />
          <button class="send" :disabled="!canSend" @click="send">发送</button>
        </footer>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put } from '../api/request'
import messageService from '../services/messageService'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const convs = ref([])
const filteredConvs = ref([])
const keyword = ref('')
const current = ref(null)
const msgs = ref([])
const draft = ref('')
const me = Number(localStorage.getItem('userId') || '0')
const msgsRef = ref(null)

const connStatus = ref('disconnected')
const connStatusText = computed(() => connStatus.value==='connected' ? '🟢 实时连接' : (connStatus.value==='polling' ? '🟡 轮询模式' : '🔴 连接断开'))
const statusClass = computed(() => ({connected: connStatus.value==='connected'}))

const canSend = computed(() => !!current.value && !!draft.value.trim())

const getUserAvatar = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.avatar
  } catch {
    return null
  }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

const mapConv = (c) => ({
  id: c.contactUserId,
  name: c.contactNickname || c.contactUsername || `用户${c.contactUserId}`,
  avatar: c.contactAvatar,
  lastMessage: c.lastMessageContent,
  lastMessageTime: c.lastMessageTime,
  unreadCount: c.unreadCount || 0,
  productId: c.productId,
  productTitle: c.productTitle,
  productImage: c.productImage
})

const mapMsg = (m) => ({
  id: m.id,
  senderId: m.senderId || m.fromUserId,
  receiverId: m.receiverId || m.toUserId,
  content: m.content,
  type: (m.typeText ? String(m.typeText).toLowerCase() : (m.type===2?'image':'text')),
  createdAt: m.createdAt || m.createTime,
  isRead: m.isRead,
  isUnread: m.isUnread !== undefined ? m.isUnread : (m.isRead === 0),
  readStatus: m.readStatus,
  readTime: m.readTime,
  senderAvatar: m.senderAvatar || m.fromAvatar
})

const loadConvs = async () => {
  loading.value = true
  try {
    const res = await get('/messages/conversations')
    const list = Array.isArray(res?.data) ? res.data : []
    convs.value = list.map(mapConv)
    filterConvs()
  } catch (e) { console.error(e) } finally { loading.value=false }
}

const filterConvs = () => {
  const k = keyword.value.trim().toLowerCase()
  filteredConvs.value = k ? convs.value.filter(c => (c.name||'').toLowerCase().includes(k)) : [...convs.value]
}

const loadMsgs = async (uid) => {
  try {
    const res = await get(`/messages/conversation/${uid}`)
    const list = Array.isArray(res?.data) ? res.data : []
    msgs.value = list.map(mapMsg)
    await nextTick(); scrollToBottom()
  } catch (e) { console.error(e) }
}

const selectConv = async (c) => {
  current.value = c
  await loadMsgs(c.id)
  if ((c.unreadCount||0)>0) { await markConvRead(c.id); c.unreadCount=0 }
}

// 从商品详情进入：根据路由参数初始化会话
const initByRoute = async () => {
  const q = route.query || {}
  const to = q.to ? Number(q.to) : null
  if (!to || !Number.isFinite(to)) return
  // 构造临时会话项
  const temp = {
    id: to,
    name: (q.name || `用户${to}`),
    avatar: (q.avatar || '/logo.jpg'),
    lastMessage: '',
    lastMessageTime: '',
    unreadCount: 0,
    productId: q.productId ? Number(q.productId) : null,
    productTitle: q.productTitle || '',
    productImage: q.productImage || ''
  }
  // 如果会话列表中不存在该联系人，插入到顶部
  if (!convs.value.find(c => c.id === to)) {
    convs.value.unshift(temp)
    filterConvs()
  }
  // 选择该会话并尝试加载历史消息
  await selectConv(temp)
  // 清理一次查询参数，避免重复触发
  router.replace({ path: '/messages' })
}

const scrollToBottom = () => {
  const el = msgsRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const send = async () => {
  if (!canSend.value) return
  
  // 防止重复发送：发送时禁用按钮
  const originalContent = draft.value.trim()
  if (!originalContent) return
  
  // 立即清空输入框和禁用发送，防止重复点击
  draft.value = ''
  
  const payload = { toUserId: current.value.id, productId: current.value.productId || null, content: originalContent, type: 1 }
  try {
    const res = await post('/messages/send', payload)
    if (res && res.code === 200 && res.data) {
      // 发送成功后立即添加消息到当前会话
      const newMessage = {
        id: res.data.id || Date.now(),
        content: originalContent,
        senderId: me,
        receiverId: current.value.id,
        createdAt: new Date().toISOString(),
        type: 'text'
      }
      msgs.value.push(newMessage)
      nextTick().then(scrollToBottom)
      
      // 更新会话的最后消息信息
      current.value.lastMessage = originalContent
      current.value.lastMessageTime = newMessage.createdAt
    } else {
      const msg = res?.message || '发送失败，请稍后重试'
      console.warn('发送消息失败：', res)
      // 发送失败时恢复输入内容
      draft.value = originalContent
      // 简单提示（项目未引入全局消息组件时使用 alert）
      try { window.alert(msg) } catch {}
    }
  } catch (e) {
    console.error('发送失败', e)
    // 发送失败时恢复输入内容
    draft.value = originalContent
    try { window.alert('网络异常，发送失败') } catch {}
  }
}

const markConvRead = async (uid) => {
  try { await put(`/messages/conversation/${uid}/read`, {}) } catch (e) { console.warn('标记已读失败', e) }
}

const clearUnread = async () => {
  if (!current.value) return
  await markConvRead(current.value.id)
  // 更新本地消息状态
  msgs.value.forEach(m => {
    if (m.senderId !== me) {
      m.isUnread = false
      m.readStatus = '已读'
    }
  })
  // 更新会话未读数
  current.value.unreadCount = 0
}

const refreshMessages = async () => {
  if (!current.value) return
  await loadMsgs(current.value.id)
}

const getReadStatusText = (msg) => {
  if (msg.readStatus) return msg.readStatus
  if (msg.isRead === 0) return '未读'
  if (msg.isRead === 1) return '已读'
  return '已发送'
}

const getReadStatusClass = (msg) => {
  const status = getReadStatusText(msg)
  return {
    'status-unread': status === '未读',
    'status-read': status === '已读',
    'status-sent': status === '已发送'
  }
}

// WebSocket: 接收后端 MessageResponse 推送
let unsubMsg = null, unsubConn = null
const handlePush = (message) => {
  const m = mapMsg(message)
  // 更新会话列表（最后消息/未读计数）
  const conv = convs.value.find(x => x.id === (m.senderId===me ? m.receiverId : m.senderId))
  if (conv) {
    conv.lastMessage = m.content
    conv.lastMessageTime = m.createdAt
    if (!current.value || current.value.id !== conv.id) conv.unreadCount = (conv.unreadCount||0) + 1
    filterConvs()
  }
  // 若在当前会话，检查消息是否已存在，避免重复添加
  if (current.value && ((m.senderId === me && m.receiverId === current.value.id) || (m.senderId === current.value.id && m.receiverId === me))) {
    // 检查是否已存在相同的消息（根据时间戳和内容判断）
    const exists = msgs.value.some(existing => 
      existing.content === m.content && 
      existing.senderId === m.senderId && 
      Math.abs(new Date(existing.createdAt) - new Date(m.createdAt)) < 1000 // 1秒内的消息认为是重复的
    )
    if (!exists) {
      msgs.value.push(m)
      nextTick().then(scrollToBottom)
    }
  }
}

const handleConn = (ok) => { connStatus.value = ok ? 'connected' : 'disconnected' }

onMounted(async () => {
  await loadConvs()
  // 路由参数初始化
  await initByRoute()
  try {
    await messageService.initialize()
    unsubMsg = messageService.onMessage(handlePush)
    unsubConn = messageService.onConnectionChange(handleConn)
  } catch (e) { console.warn('WS 初始化失败', e) }
})

onUnmounted(() => {
  if (unsubMsg) unsubMsg(); if (unsubConn) unsubConn();
  try { messageService.disconnect() } catch {}
})
</script>

<style scoped>
.mc{display:flex;height:100vh;background:#f6f7fb;color:#333;overflow:hidden}
.mc-left{width:340px;border-right:1px solid #eaeaea;background:#fff;display:flex;flex-direction:column;height:100vh;position:fixed;left:0;top:0;z-index:10}
.mc-left-header{padding:16px;border-bottom:1px solid #eee;flex-shrink:0}
.mc-left-header h2{margin:0 0 8px;font-size:18px}
.mc-search{width:100%;padding:10px 12px;border:1px solid #ddd;border-radius:8px;background:#fafafa}
.mc-list{flex:1;overflow-y:auto;min-height:0}
.mc-item{display:flex;gap:12px;width:100%;padding:12px 16px;border:none;background:transparent;cursor:pointer;border-bottom:1px solid #f0f0f0}
.mc-item.active{background:#f3f6ff}
.mc-item img{width:42px;height:42px;border-radius:50%;object-fit:cover}
.mc-item-main{flex:1;min-width:0}
.mc-item-top{display:flex;justify-content:space-between;align-items:center}
.name{font-weight:600}
.time{font-size:12px;color:#999}
.mc-item-bottom{display:flex;justify-content:space-between;align-items:center}
.last{color:#666;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.last.unread{font-weight:600;color:#333}
.status-indicators{display:flex;align-items:center;gap:4px}
.badge{background:#ff4d4f;color:#fff;border-radius:10px;padding:0 6px;font-size:12px;min-width:18px;text-align:center;animation:pulse 2s infinite}
.badge.pulse{animation:pulse 2s infinite}
@keyframes pulse{0%{transform:scale(1)}50%{transform:scale(1.1)}100%{transform:scale(1)}}
.read-indicator{color:#52c41a;font-size:12px;opacity:0.7}
.mc-empty,.mc-loading{padding:24px;color:#888}
.mc-chat{flex:1;display:flex;flex-direction:column;height:100vh;margin-left:340px}
.mc-welcome{margin:auto;color:#666}
.mc-chat-header{height:70px;display:flex;align-items:center;justify-content:space-between;padding:0 20px;border-bottom:1px solid #eee;background:#fff;flex-shrink:0;box-shadow:0 1px 3px rgba(0,0,0,0.1)}
.user-info{display:flex;align-items:center;gap:12px}
.user-avatar{width:44px;height:44px;border-radius:50%;object-fit:cover;border:2px solid #f0f0f0}
.user-details{display:flex;flex-direction:column;gap:2px}
.user-name{margin:0;font-size:16px;font-weight:600;color:#333}
.user-status{display:flex;flex-direction:column;gap:2px}
.connection-status{font-size:12px;color:#666}
.connection-status.connected{color:#52c41a}
.product-info{font-size:11px;color:#1890ff;background:#f0f8ff;padding:2px 6px;border-radius:10px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.chat-actions{display:flex;gap:8px}
.action-btn{width:32px;height:32px;border:none;border-radius:50%;background:#f5f5f5;color:#666;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:all 0.2s}
.action-btn:hover{background:#e6f7ff;color:#1890ff}
.action-btn .icon{font-size:14px}
.mc-msgs{flex:1;overflow-y:auto;overflow-x:hidden;padding:16px;display:flex;flex-direction:column;gap:12px;min-height:0;max-height:calc(100vh - 120px)}
.msg{display:flex;align-items:flex-end;gap:8px;margin-bottom:8px}
.msg.own{flex-direction:row-reverse}
.msg-avatar{flex-shrink:0}
.msg-avatar img{width:32px;height:32px;border-radius:50%;object-fit:cover}
.msg-content{display:flex;flex-direction:column;max-width:70%}
.msg.own .msg-content{align-items:flex-end}
.bubble{padding:10px 14px;border-radius:18px;background:#fff;box-shadow:0 1px 2px rgba(0,0,0,.1);word-wrap:break-word;position:relative;transition:all 0.2s}
.bubble.unread{background:#fff7e6;border-left:3px solid #faad14;animation:highlight 3s ease-out}
@keyframes highlight{0%{background:#fff2e8;transform:scale(1.02)}100%{background:#fff7e6;transform:scale(1)}}
.msg.own .bubble{background:#007aff;color:#fff}
.msg.own .bubble::after{content:'';position:absolute;right:-6px;bottom:8px;width:0;height:0;border:6px solid transparent;border-left-color:#007aff}
.bubble::after{content:'';position:absolute;left:-6px;bottom:8px;width:0;height:0;border:6px solid transparent;border-right-color:#fff}
.msg.own .bubble::after{border-left-color:#007aff;border-right-color:transparent;right:-6px;left:auto}
.unread-indicator{position:absolute;top:-4px;right:-4px;width:8px;height:8px}
.unread-dot{display:block;width:8px;height:8px;background:#ff4d4f;border-radius:50%;animation:blink 1.5s infinite}
.img{max-width:240px;border-radius:8px}
.msg-meta{display:flex;align-items:center;gap:8px;margin-top:4px;padding:0 4px}
.ts{font-size:11px;color:#999}
.read-status{font-size:10px;padding:1px 4px;border-radius:8px;transition:all 0.2s}
.status-unread{color:#faad14;background:#fff7e6}
.status-read{color:#52c41a;background:#f6ffed}
.status-sent{color:#999;background:#f5f5f5}
@keyframes blink{0%,50%{opacity:1}51%,100%{opacity:0.3}}
.mc-input{display:flex;gap:8px;padding:12px;border-top:1px solid #eee;background:#fff;flex-shrink:0;position:sticky;bottom:0}
.mc-input textarea{flex:1;resize:none;padding:10px 12px;border:1px solid #ddd;border-radius:8px;background:#fafafa}
.send{padding:0 16px;border:none;border-radius:8px;background:#667eea;color:#fff}
.send:disabled{opacity:.6;cursor:not-allowed}
</style>