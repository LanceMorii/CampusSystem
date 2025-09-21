import { request } from './request'

/**
 * 用户登录
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.studentId - 学号
 * @param {string} loginData.password - 密码
 */
export const login = (loginData) => {
  return request({
    url: '/auth/login',
    method: 'POST',
    data: loginData
  })
}

/**
 * 用户注册
 * @param {Object} registerData - 注册数据
 * @param {string} registerData.studentId - 学号
 * @param {string} registerData.username - 用户名
 * @param {string} registerData.realName - 真实姓名
 * @param {string} registerData.password - 密码
 * @param {string} registerData.phone - 手机号（可选）
 * @param {string} registerData.email - 邮箱（可选）
 */
export const register = (registerData) => {
  return request({
    url: '/auth/register',
    method: 'POST',
    data: registerData
  })
}

/**
 * 获取用户信息
 */
export const getUserProfile = () => {
  return request({
    url: '/auth/profile',
    method: 'GET'
  })
}