# 校园交易系统 API 测试文档

## 概述

本文档提供校园交易系统所有API接口的测试指南，包括请求参数、响应格式和测试用例。

## 基础配置

### 服务器地址
- 本地开发环境：`http://localhost:8080`

### 认证方式
- 使用JWT Token进行身份验证
- 在请求头中添加：`Authorization: Bearer {token}`

---

## 1. 认证接口 (AuthController)

### 1.1 用户注册
- **接口**: `POST /auth/register`
- **描述**: 用户注册
- **请求体**:
```json
{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000",
    "realName": "测试用户"
}
```
- **响应示例**:
```json
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com"
    }
}
```

### 1.2 用户登录
- **接口**: `POST /auth/login`
- **描述**: 用户登录
- **请求体**:
```json
{
    "username": "testuser",
    "password": "123456"
}
```
- **响应示例**:
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": {
            "id": 1,
            "username": "testuser",
            "email": "test@example.com"
        }
    }
}
```

### 1.3 获取用户信息
- **接口**: `GET /auth/profile`
- **描述**: 获取当前用户信息
- **请求头**: `Authorization: Bearer {token}`
- **响应示例**:
```json
{
    "code": 200,
    "message": "获取用户信息成功",
    "data": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "phone": "13800138000",
        "realName": "测试用户"
    }
}
```

### 1.4 更新用户信息
- **接口**: `PUT /auth/profile`
- **描述**: 更新用户信息
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
    "email": "newemail@example.com",
    "phone": "13900139000",
    "realName": "新用户名"
}
```

### 1.5 管理员测试接口
- **接口**: `GET /auth/admin-test`
- **描述**: 测试管理员权限
- **请求头**: `Authorization: Bearer {admin_token}`

### 1.6 刷新Token
- **接口**: `POST /auth/refresh`
- **描述**: 刷新JWT Token
- **请求头**: `Authorization: Bearer {token}`

---

## 2. 商品接口 (ProductController)

### 2.1 发布商品
- **接口**: `POST /api/products`
- **描述**: 发布新商品
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
    "title": "二手iPhone 13",
    "description": "9成新，无划痕",
    "price": 4500.00,
    "categoryId": 1,
    "images": ["image1.jpg", "image2.jpg"],
    "condition": "GOOD",
    "location": "宿舍楼A"
}
```

### 2.2 获取商品详情
- **接口**: `GET /api/products/{id}`
- **描述**: 获取商品详情
- **路径参数**: `id` - 商品ID

### 2.3 更新商品信息
- **接口**: `PUT /api/products/{id}`
- **描述**: 更新商品信息
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 商品ID

### 2.4 删除商品
- **接口**: `DELETE /api/products/{id}`
- **描述**: 删除商品
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 商品ID

### 2.5 获取用户发布的商品
- **接口**: `GET /api/products/my`
- **描述**: 获取当前用户发布的商品列表
- **请求头**: `Authorization: Bearer {token}`

### 2.6 搜索商品
- **接口**: `GET /api/products/search`
- **描述**: 搜索商品
- **查询参数**:
  - `keyword` - 搜索关键词
  - `categoryId` - 分类ID（可选）
  - `minPrice` - 最低价格（可选）
  - `maxPrice` - 最高价格（可选）

### 2.7 获取商品列表
- **接口**: `GET /api/products`
- **描述**: 获取商品列表（分页）
- **查询参数**:
  - `page` - 页码（默认0）
  - `size` - 每页大小（默认10）
  - `categoryId` - 分类ID（可选）

### 2.8 按分类统计商品数量
- **接口**: `GET /api/products/count-by-category`
- **描述**: 统计各分类的商品数量

---

## 3. 订单接口 (OrderController)

### 3.1 创建订单
- **接口**: `POST /api/orders`
- **描述**: 创建新订单
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
    "productId": 1,
    "quantity": 1,
    "totalAmount": 4500.00,
    "buyerMessage": "请问还能便宜点吗？"
}
```

### 3.2 获取订单详情
- **接口**: `GET /api/orders/{id}`
- **描述**: 获取订单详情
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 订单ID

### 3.3 根据订单号获取订单
- **接口**: `GET /api/orders/no/{orderNo}`
- **描述**: 根据订单号获取订单
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `orderNo` - 订单号

### 3.4 取消订单
- **接口**: `PUT /api/orders/{id}/cancel`
- **描述**: 取消订单
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 订单ID

### 3.5 买家确认订单
- **接口**: `PUT /api/orders/{id}/confirm-buyer`
- **描述**: 买家确认订单
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 订单ID

### 3.6 卖家确认订单
- **接口**: `PUT /api/orders/{id}/confirm-seller`
- **描述**: 卖家确认订单
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 订单ID

### 3.7 完成订单
- **接口**: `PUT /api/orders/{id}/complete`
- **描述**: 完成订单
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `id` - 订单ID

### 3.8 获取买家订单列表
- **接口**: `GET /api/orders/buyer`
- **描述**: 获取当前用户作为买家的订单列表
- **请求头**: `Authorization: Bearer {token}`

### 3.9 获取卖家订单列表
- **接口**: `GET /api/orders/seller`
- **描述**: 获取当前用户作为卖家的订单列表
- **请求头**: `Authorization: Bearer {token}`

### 3.10 获取我的所有订单
- **接口**: `GET /api/orders/my`
- **描述**: 获取当前用户的所有订单
- **请求头**: `Authorization: Bearer {token}`

### 3.11 获取买家待确认订单
- **接口**: `GET /api/orders/buyer/pending`
- **描述**: 获取买家待确认的订单
- **请求头**: `Authorization: Bearer {token}`

### 3.12 获取卖家待确认订单
- **接口**: `GET /api/orders/seller/pending`
- **描述**: 获取卖家待确认的订单
- **请求头**: `Authorization: Bearer {token}`

---

## 4. 分类接口 (CategoryController)

### 4.1 获取所有分类
- **接口**: `GET /api/v1/categories`
- **描述**: 获取所有激活的分类列表

### 4.2 获取顶级分类
- **接口**: `GET /api/v1/categories/top`
- **描述**: 获取顶级分类列表

### 4.3 获取子分类
- **接口**: `GET /api/v1/categories/parent/{parentId}`
- **描述**: 根据父分类ID获取子分类
- **路径参数**: `parentId` - 父分类ID

### 4.4 创建分类（管理员）
- **接口**: `POST /api/v1/categories`
- **描述**: 创建新分类（需要管理员权限）
- **请求头**: `Authorization: Bearer {admin_token}`
- **请求体**:
```json
{
    "name": "电子产品",
    "parentId": null,
    "sortOrder": 1,
    "status": "ACTIVE"
}
```

### 4.5 更新分类（管理员）
- **接口**: `PUT /api/v1/categories/{id}`
- **描述**: 更新分类信息（需要管理员权限）
- **请求头**: `Authorization: Bearer {admin_token}`
- **路径参数**: `id` - 分类ID

### 4.6 删除分类（管理员）
- **接口**: `DELETE /api/v1/categories/{id}`
- **描述**: 删除分类（需要管理员权限）
- **请求头**: `Authorization: Bearer {admin_token}`
- **路径参数**: `id` - 分类ID

### 4.7 批量删除分类（管理员）
- **接口**: `DELETE /api/v1/categories/batch`
- **描述**: 批量删除分类（需要管理员权限）
- **请求头**: `Authorization: Bearer {admin_token}`
- **请求体**:
```json
[1, 2, 3]
```

---

## 5. 消息接口 (MessageController)

### 5.1 发送消息
- **接口**: `POST /api/messages/send`
- **描述**: 发送消息
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
    "receiverId": 2,
    "content": "你好，这个商品还在吗？",
    "productId": 1,
    "messageType": "TEXT"
}
```

### 5.2 获取对话列表
- **接口**: `GET /api/messages/conversations`
- **描述**: 获取当前用户的对话列表
- **请求头**: `Authorization: Bearer {token}`

### 5.3 获取对话消息
- **接口**: `GET /api/messages/conversation/{contactUserId}`
- **描述**: 获取与指定用户的对话消息
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `contactUserId` - 联系人用户ID

### 5.4 标记消息为已读
- **接口**: `PUT /api/messages/{messageId}/read`
- **描述**: 标记消息为已读
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `messageId` - 消息ID

### 5.5 标记对话为已读
- **接口**: `PUT /api/messages/conversation/{contactUserId}/read`
- **描述**: 标记整个对话为已读
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `contactUserId` - 联系人用户ID

### 5.6 获取未读消息数量
- **接口**: `GET /api/messages/unread-count`
- **描述**: 获取未读消息数量
- **请求头**: `Authorization: Bearer {token}`

### 5.7 获取用户所有消息
- **接口**: `GET /api/messages/all`
- **描述**: 获取用户所有消息列表
- **请求头**: `Authorization: Bearer {token}`

### 5.8 获取商品相关消息
- **接口**: `GET /api/messages/product/{productId}`
- **描述**: 获取关于特定商品的消息列表
- **路径参数**: `productId` - 商品ID

### 5.9 搜索消息
- **接口**: `GET /api/messages/search`
- **描述**: 搜索消息
- **查询参数**: `keyword` - 搜索关键词

### 5.10 在对话中搜索消息
- **接口**: `GET /api/messages/conversation/{contactUserId}/search`
- **描述**: 在指定对话中搜索消息
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `contactUserId` - 联系人用户ID
- **查询参数**: `keyword` - 搜索关键词

### 5.11 删除对话
- **接口**: `DELETE /api/messages/conversation/{contactUserId}`
- **描述**: 删除与指定用户的对话
- **请求头**: `Authorization: Bearer {token}`
- **路径参数**: `contactUserId` - 联系人用户ID

---

## 测试流程建议

### 1. 基础流程测试
1. 用户注册 → 用户登录 → 获取用户信息
2. 创建分类（管理员） → 发布商品 → 搜索商品
3. 创建订单 → 确认订单 → 完成订单
4. 发送消息 → 获取对话 → 标记已读

### 2. 权限测试
- 测试需要登录的接口在未登录时的响应
- 测试管理员接口在普通用户访问时的响应
- 测试用户只能操作自己的数据

### 3. 异常情况测试
- 测试无效参数的处理
- 测试不存在资源的访问
- 测试并发操作的处理

### 4. 性能测试
- 测试分页接口的性能
- 测试搜索接口的响应时间
- 测试大量数据下的接口表现

---

## 常见错误码

- `200` - 成功
- `400` - 请求参数错误
- `401` - 未授权（需要登录）
- `403` - 禁止访问（权限不足）
- `404` - 资源不存在
- `500` - 服务器内部错误

---

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带有效的JWT Token
2. 管理员接口需要具有ADMIN角色的用户才能访问
3. 分页接口的页码从0开始
4. 所有金额字段使用BigDecimal类型，保留两位小数
5. 时间戳使用毫秒级Unix时间戳
6. 文件上传接口需要使用multipart/form-data格式

---

*文档最后更新时间：2024年*