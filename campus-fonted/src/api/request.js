// HTTP请求封装
const API_BASE_URL = 'http://localhost:8080/api/v1'

/**
 * 发送HTTP请求
 * @param {Object} config - 请求配置
 * @param {string} config.url - 请求URL
 * @param {string} config.method - 请求方法
 * @param {Object} config.data - 请求数据
 * @param {Object} config.headers - 请求头
 */
export const request = async (config) => {
  const { url, method = 'GET', data, headers = {} } = config
  
  // 构建完整URL
  const fullUrl = `${API_BASE_URL}${url}`
  
  // 设置默认请求头
  const defaultHeaders = {
    'Content-Type': 'application/json',
    ...headers
  }
  
  // 从localStorage获取token
  const token = localStorage.getItem('token')
  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`
  }
  
  // 构建fetch配置
  const fetchConfig = {
    method: method.toUpperCase(),
    headers: defaultHeaders
  }
  
  // 如果有数据且不是GET请求，添加请求体
  if (data && method.toUpperCase() !== 'GET') {
    fetchConfig.body = JSON.stringify(data)
  }
  
  try {
    const response = await fetch(fullUrl, fetchConfig)
    
    // 解析JSON响应
    const result = await response.json()
    
    // 检查响应状态
    if (!response.ok) {
      // 如果后端返回了错误信息，使用后端的错误信息
      if (result && result.message) {
        return {
          code: response.status,
          message: result.message,
          data: null
        }
      }
      
      // 否则使用默认错误信息
      let errorMessage = '请求失败'
      switch (response.status) {
        case 400:
          errorMessage = '请求参数错误'
          break
        case 401:
          errorMessage = '未授权，请重新登录'
          break
        case 403:
          errorMessage = '权限不足'
          break
        case 404:
          errorMessage = '请求的资源不存在'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        default:
          errorMessage = `请求失败 (${response.status})`
      }
      
      return {
        code: response.status,
        message: errorMessage,
        data: null
      }
    }
    
    return result
    
  } catch (error) {
    console.error('Request failed:', error)
    
    // 返回错误格式的响应
    return {
      code: 500,
      message: error.message || '网络请求失败',
      data: null
    }
  }
}

/**
 * GET请求
 */
export const get = (url, params = {}) => {
  const queryString = new URLSearchParams(params).toString()
  const fullUrl = queryString ? `${url}?${queryString}` : url
  
  return request({
    url: fullUrl,
    method: 'GET'
  })
}

/**
 * POST请求
 */
export const post = (url, data) => {
  return request({
    url,
    method: 'POST',
    data
  })
}

/**
 * PUT请求
 */
export const put = (url, data) => {
  return request({
    url,
    method: 'PUT',
    data
  })
}

/**
 * DELETE请求
 */
export const del = (url) => {
  return request({
    url,
    method: 'DELETE'
  })
}