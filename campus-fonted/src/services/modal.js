import { createApp } from 'vue'
import ModalNotification from '../components/ModalNotification.vue'

class ModalService {
  constructor() {
    this.modals = []
  }

  show(options) {
    const {
      message,
      title = '',
      type = 'info',
      duration = 0,
      showCancel = false,
      confirmText = '确定',
      cancelText = '取消',
      hideActions = false,
      closeOnOverlay = true,
      onConfirm = () => {},
      onCancel = () => {},
      onClose = () => {}
    } = options

    return new Promise((resolve) => {
      // 创建容器
      const container = document.createElement('div')
      document.body.appendChild(container)

      // 创建Vue应用实例
      const app = createApp(ModalNotification, {
        message,
        title,
        type,
        duration,
        showCancel,
        confirmText,
        cancelText,
        hideActions,
        closeOnOverlay,
        onConfirm: () => {
          onConfirm()
          resolve('confirm')
        },
        onCancel: () => {
          onCancel()
          resolve('cancel')
        },
        onClose: () => {
          // 清理DOM
          app.unmount()
          if (container.parentNode) {
            container.parentNode.removeChild(container)
          }
          
          // 从数组中移除
          const index = this.modals.indexOf(app)
          if (index > -1) {
            this.modals.splice(index, 1)
          }
          
          onClose()
          resolve('close')
        }
      })

      // 挂载应用
      app.mount(container)
      this.modals.push(app)
    })
  }

  success(message, title = '操作成功') {
    return this.show({
      message,
      title,
      type: 'success',
      duration: 2000,
      hideActions: true
    })
  }

  error(message, title = '操作失败') {
    return this.show({
      message,
      title,
      type: 'error',
      showCancel: false,
      confirmText: '知道了'
    })
  }

  warning(message, title = '警告') {
    return this.show({
      message,
      title,
      type: 'warning',
      showCancel: false,
      confirmText: '知道了'
    })
  }

  info(message, title = '提示') {
    return this.show({
      message,
      title,
      type: 'info',
      showCancel: false,
      confirmText: '知道了'
    })
  }

  confirm(message, title = '确认操作') {
    return this.show({
      message,
      title,
      type: 'warning',
      showCancel: true,
      confirmText: '确认',
      cancelText: '取消'
    })
  }

  loading(message, title = '处理中') {
    return this.show({
      message,
      title,
      type: 'loading',
      hideActions: true,
      closeOnOverlay: false
    })
  }

  // 清除所有modal
  clear() {
    this.modals.forEach(app => {
      try {
        app.unmount()
      } catch (e) {
        console.warn('Error unmounting modal:', e)
      }
    })
    this.modals = []
  }
}

// 创建全局实例
const modal = new ModalService()

export default modal