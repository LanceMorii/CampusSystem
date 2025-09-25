import { createApp } from 'vue'
import ToastNotification from '../components/ToastNotification.vue'

class ToastService {
  constructor() {
    this.toasts = []
  }

  show(options) {
    const {
      message,
      title = '',
      type = 'info',
      duration = 3000,
      persistent = false
    } = options

    // 创建容器
    const container = document.createElement('div')
    document.body.appendChild(container)

    // 创建Vue应用实例
    const app = createApp(ToastNotification, {
      message,
      title,
      type,
      duration,
      persistent,
      onClose: () => {
        // 清理DOM
        app.unmount()
        if (container.parentNode) {
          container.parentNode.removeChild(container)
        }
        
        // 从数组中移除
        const index = this.toasts.indexOf(app)
        if (index > -1) {
          this.toasts.splice(index, 1)
        }
      }
    })

    // 挂载应用
    app.mount(container)
    this.toasts.push(app)

    return app
  }

  success(message, title = '成功') {
    return this.show({
      message,
      title,
      type: 'success',
      duration: 3000
    })
  }

  error(message, title = '错误') {
    return this.show({
      message,
      title,
      type: 'error',
      duration: 4000
    })
  }

  warning(message, title = '警告') {
    return this.show({
      message,
      title,
      type: 'warning',
      duration: 3500
    })
  }

  info(message, title = '提示') {
    return this.show({
      message,
      title,
      type: 'info',
      duration: 3000
    })
  }

  // 清除所有toast
  clear() {
    this.toasts.forEach(app => {
      try {
        app.unmount()
      } catch (e) {
        console.warn('Error unmounting toast:', e)
      }
    })
    this.toasts = []
  }
}

// 创建全局实例
const toast = new ToastService()

export default toast