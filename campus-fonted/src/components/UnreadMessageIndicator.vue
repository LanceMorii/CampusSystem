<template>
  <div class="unread-indicator" v-if="hasUnread">
    <div class="red-dot" :class="{ pulse: shouldPulse }">
      <span v-if="showCount && unreadCount > 0" class="count">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { get } from '../api/request'

const props = defineProps({
  showCount: {
    type: Boolean,
    default: false
  },
  pulseAnimation: {
    type: Boolean,
    default: true
  },
  refreshInterval: {
    type: Number,
    default: 30000 // 30秒刷新一次
  }
})

const unreadCount = ref(0)
const hasUnread = computed(() => unreadCount.value > 0)
const shouldPulse = computed(() => props.pulseAnimation && hasUnread.value)

let refreshTimer = null

const fetchUnreadCount = async () => {
  try {
    const response = await get('/messages/unread/total')
    if (response && response.code === 200) {
      unreadCount.value = response.data.totalUnread || 0
    }
  } catch (error) {
    console.warn('获取未读消息数失败:', error)
  }
}

onMounted(() => {
  fetchUnreadCount()
  if (props.refreshInterval > 0) {
    refreshTimer = setInterval(fetchUnreadCount, props.refreshInterval)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 暴露方法供父组件调用
defineExpose({
  refresh: fetchUnreadCount,
  getUnreadCount: () => unreadCount.value
})
</script>

<style scoped>
.unread-indicator {
  position: relative;
  display: inline-block;
}

.red-dot {
  position: absolute;
  top: -8px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  background: #ff4d4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: white;
  font-weight: bold;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.red-dot.pulse {
  animation: pulse 2s infinite;
}

.count {
  line-height: 1;
  padding: 0 2px;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .red-dot {
    min-width: 14px;
    height: 14px;
    font-size: 9px;
    top: -6px;
    right: -6px;
  }
}
</style>