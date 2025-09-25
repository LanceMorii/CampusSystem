<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container" :class="typeClass">
          <div class="modal-content">
            <div class="modal-icon">
              <div class="icon-wrapper" :class="iconClass">
                <span v-if="type === 'success'">✓</span>
                <span v-else-if="type === 'error'">✕</span>
                <span v-else-if="type === 'warning'">!</span>
                <span v-else-if="type === 'loading'">
                  <div class="loading-spinner"></div>
                </span>
                <span v-else>i</span>
              </div>
            </div>
            
            <div class="modal-message">
              <h3 v-if="title" class="modal-title">{{ title }}</h3>
              <p class="modal-text">{{ message }}</p>
            </div>
            
            <div class="modal-actions" v-if="!hideActions">
              <button 
                v-if="showCancel" 
                @click="handleCancel" 
                class="modal-btn modal-btn-cancel"
              >
                {{ cancelText }}
              </button>
              <button 
                @click="handleConfirm" 
                class="modal-btn modal-btn-confirm"
                :class="confirmButtonClass"
              >
                {{ confirmText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const props = defineProps({
  message: {
    type: String,
    required: true
  },
  title: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'info', // success, error, warning, info, loading
    validator: (value) => ['success', 'error', 'warning', 'info', 'loading'].includes(value)
  },
  duration: {
    type: Number,
    default: 0 // 0表示不自动关闭
  },
  showCancel: {
    type: Boolean,
    default: false
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  hideActions: {
    type: Boolean,
    default: false
  },
  closeOnOverlay: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

const visible = ref(false)

const typeClass = computed(() => `modal-${props.type}`)
const iconClass = computed(() => `icon-${props.type}`)
const confirmButtonClass = computed(() => {
  const classMap = {
    success: 'btn-success',
    error: 'btn-error',
    warning: 'btn-warning',
    info: 'btn-info',
    loading: 'btn-loading'
  }
  return classMap[props.type] || 'btn-info'
})

const handleConfirm = () => {
  emit('confirm')
  close()
}

const handleCancel = () => {
  emit('cancel')
  close()
}

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    close()
  }
}

const close = () => {
  visible.value = false
  setTimeout(() => {
    emit('close')
  }, 300) // 等待动画完成
}

onMounted(() => {
  visible.value = true
  
  if (props.duration > 0) {
    setTimeout(() => {
      close()
    }, props.duration)
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.modal-container {
  max-width: 400px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  text-align: center;
  position: relative;
}

.modal-icon {
  margin-bottom: 20px;
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  color: white;
  position: relative;
}

.icon-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  animation: successPulse 0.6s ease-out;
}

.icon-error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  animation: errorShake 0.6s ease-out;
}

.icon-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  animation: warningBounce 0.6s ease-out;
}

.icon-info {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  animation: infoPulse 0.6s ease-out;
}

.icon-loading {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.modal-message {
  margin-bottom: 24px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.modal-text {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.modal-btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  min-width: 80px;
}

.modal-btn-cancel {
  background: #f3f4f6;
  color: #6b7280;
}

.modal-btn-cancel:hover {
  background: #e5e7eb;
  color: #4b5563;
}

.modal-btn-confirm {
  color: white;
}

.btn-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.btn-success:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  transform: translateY(-1px);
}

.btn-error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.btn-error:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-1px);
}

.btn-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.btn-warning:hover {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
  transform: translateY(-1px);
}

.btn-info {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.btn-info:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-1px);
}

.btn-loading {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  cursor: not-allowed;
}

/* 动画效果 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content {
  transform: scale(0.9) translateY(-20px);
}

.modal-leave-to .modal-content {
  transform: scale(0.9) translateY(-20px);
}

.modal-enter-to .modal-content,
.modal-leave-from .modal-content {
  transform: scale(1) translateY(0);
}

/* 图标动画 */
@keyframes successPulse {
  0% { transform: scale(0.8); opacity: 0; }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes errorShake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

@keyframes warningBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes infoPulse {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .modal-overlay {
    padding: 16px;
  }
  
  .modal-content {
    padding: 24px 20px 20px;
  }
  
  .icon-wrapper {
    width: 56px;
    height: 56px;
    font-size: 24px;
  }
  
  .modal-title {
    font-size: 18px;
  }
  
  .modal-text {
    font-size: 15px;
  }
  
  .modal-actions {
    flex-direction: column;
  }
  
  .modal-btn {
    width: 100%;
  }
}
</style>