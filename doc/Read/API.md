# 校园二手交易系统 API 文档

本文档详细描述了校园二手交易系统的所有API接口，包括请求参数、响应格式和使用示例。

## 📋 目录

- [基础信息](#基础信息)
- [认证授权](#认证授权)
- [用户管理](#用户管理)
- [商品管理](#商品管理)
- [订单管理](#订单管理)
- [收藏管理](#收藏管理)
- [消息系统](#消息系统)
- [文件上传](#文件上传)
- [系统管理](#系统管理)
- [错误码说明](#错误码说明)

## 🔧 基础信息

### 服务地址
- **开发环境**: http://localhost:8080
- **生产环境**: https://api.campus-trading.com

### 请求格式
- **Content-Type**: application/json
- **字符编码**: UTF-8
- **时间格式**: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')

### 响应格式
所有API响应都遵循统一的格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### 分页格式
分页查询的响应格式：

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

## 🔐 认证授权

### JWT Token
- **Header**: `Authorization: Bearer <token>`
- **过期时间**: 24小时
- **刷新时间**: 7天

### 权限级别
- **USER**: 普通用户权限
- **ADMIN**: 管理员权限

## 👤 用户管理

### 用户注册

**POST** `/auth/register`

注册新用户账号。

**请求参数:**
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "phone": "13800138000",
  "studentId": "20240001",
  "realName": "张三"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，3-20字符 |
| password | String | 是 | 密码，6-20字符 |
| email | String | 是 | 邮箱地址 |
| phone | String | 否 | 手机号码 |
| studentId | String | 是 | 学号，8-12位数字 |
| realName | String | 是 | 真实姓名 |

**响应示例:**
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "studentId": "20240001",
    "realName": "张三",
    "createdAt": "2024-01-15T10:30:00.000Z"
  }
}
```

### 用户登录

**POST** `/auth/login`

用户登录获取访问令牌。

**请求参数:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**响应示例:**
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER",
      "avatar": "http://localhost:8080/uploads/avatars/default.png"
    }
  }
}
```

### 刷新令牌

**POST** `/auth/refresh`

使用刷新令牌获取新的访问令牌。

**请求参数:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例:**
```json
{
  "success": true,
  "message": "令牌刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

### 获取用户信息

**GET** `/api/users/profile`

获取当前登录用户的详细信息。

**请求头:**
```
Authorization: Bearer <token>
```

**响应示例:**
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "studentId": "20240001",
    "realName": "张三",
    "avatar": "http://localhost:8080/uploads/avatars/user1.jpg",
    "role": "USER",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z"
  }
}
```

### 更新用户信息

**PUT** `/api/users/profile`

更新当前用户的个人信息。

**请求参数:**
```json
{
  "email": "newemail@example.com",
  "phone": "13900139000",
  "realName": "李四",
  "avatar": "http://localhost:8080/uploads/avatars/new-avatar.jpg"
}
```

**响应示例:**
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "newemail@example.com",
    "phone": "13900139000",
    "realName": "李四",
    "avatar": "http://localhost:8080/uploads/avatars/new-avatar.jpg",
    "updatedAt": "2024-01-15T11:30:00.000Z"
  }
}
```

## 🛍️ 商品管理

### 获取商品列表

**GET** `/api/products`

获取商品列表，支持分页和筛选。

**查询参数:**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码，从0开始 |
| size | Integer | 否 | 10 | 每页数量，最大100 |
| categoryId | Long | 否 | - | 分类ID |
| keyword | String | 否 | - | 搜索关键词 |
| minPrice | BigDecimal | 否 | - | 最低价格 |
| maxPrice | BigDecimal | 否 | - | 最高价格 |
| status | Integer | 否 | 1 | 商品状态：1-在售，2-已售出，3-已下架 |
| sortBy | String | 否 | createdAt | 排序字段：createdAt, price, views |
| sortDir | String | 否 | desc | 排序方向：asc, desc |

**请求示例:**
```
GET /api/products?page=0&size=10&categoryId=1&keyword=手机&minPrice=100&maxPrice=5000&sortBy=price&sortDir=asc
```

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "iPhone 13 Pro 128GB",
        "description": "9成新，无磕碰，功能正常",
        "price": 4500.00,
        "originalPrice": 7999.00,
        "categoryId": 1,
        "categoryName": "电子产品",
        "images": [
          "http://localhost:8080/uploads/products/phone1.jpg",
          "http://localhost:8080/uploads/products/phone2.jpg"
        ],
        "status": 1,
        "statusName": "在售",
        "views": 156,
        "seller": {
          "id": 2,
          "username": "seller01",
          "avatar": "http://localhost:8080/uploads/avatars/seller01.jpg",
          "studentId": "20240002"
        },
        "createdAt": "2024-01-15T09:00:00.000Z",
        "updatedAt": "2024-01-15T09:00:00.000Z"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

### 获取商品详情

**GET** `/api/products/{id}`

获取指定商品的详细信息。

**路径参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品ID |

**响应示例:**
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 1,
    "title": "iPhone 13 Pro 128GB",
    "description": "9成新，无磕碰，功能正常。购买时间2023年3月，平时使用爱护，贴膜带壳。配件齐全：原装充电器、数据线、耳机、包装盒等。",
    "price": 4500.00,
    "originalPrice": 7999.00,
    "categoryId": 1,
    "categoryName": "电子产品",
    "images": [
      "http://localhost:8080/uploads/products/phone1.jpg",
      "http://localhost:8080/uploads/products/phone2.jpg",
      "http://localhost:8080/uploads/products/phone3.jpg"
    ],
    "status": 1,
    "statusName": "在售",
    "views": 157,
    "location": "东校区宿舍楼A座",
    "contactInfo": "微信：abc123456",
    "seller": {
      "id": 2,
      "username": "seller01",
      "realName": "王五",
      "avatar": "http://localhost:8080/uploads/avatars/seller01.jpg",
      "studentId": "20240002",
      "phone": "13700137000"
    },
    "isFavorited": false,
    "createdAt": "2024-01-15T09:00:00.000Z",
    "updatedAt": "2024-01-15T09:00:00.000Z"
  }
}
```

### 发布商品

**POST** `/api/products`

发布新的商品信息。

**请求头:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求参数:**
```json
{
  "title": "MacBook Pro 13寸 2022款",
  "description": "M2芯片，8GB内存，256GB存储。使用半年，9成新，性能优秀。适合编程、设计等专业用途。",
  "price": 8500.00,
  "originalPrice": 12999.00,
  "categoryId": 1,
  "images": [
    "http://localhost:8080/uploads/products/macbook1.jpg",
    "http://localhost:8080/uploads/products/macbook2.jpg"
  ],
  "location": "西校区图书馆",
  "contactInfo": "QQ：123456789"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 商品标题，最长100字符 |
| description | String | 是 | 商品描述，最长2000字符 |
| price | BigDecimal | 是 | 售价，大于0 |
| originalPrice | BigDecimal | 否 | 原价 |
| categoryId | Long | 是 | 分类ID |
| images | Array | 是 | 图片URL数组，至少1张，最多9张 |
| location | String | 否 | 交易地点 |
| contactInfo | String | 是 | 联系方式 |

**响应示例:**
```json
{
  "success": true,
  "message": "发布成功",
  "data": {
    "id": 26,
    "title": "MacBook Pro 13寸 2022款",
    "price": 8500.00,
    "status": 1,
    "createdAt": "2024-01-15T14:30:00.000Z"
  }
}
```

### 更新商品信息

**PUT** `/api/products/{id}`

更新商品信息（仅商品发布者可操作）。

**路径参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品ID |

**请求参数:**
```json
{
  "title": "MacBook Pro 13寸 2022款 - 降价出售",
  "description": "M2芯片，8GB内存，256GB存储。使用半年，9成新，性能优秀。急需资金，降价出售。",
  "price": 8000.00,
  "status": 1,
  "location": "西校区图书馆或东校区食堂",
  "contactInfo": "QQ：123456789，微信：abc123"
}
```

**响应示例:**
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 26,
    "title": "MacBook Pro 13寸 2022款 - 降价出售",
    "price": 8000.00,
    "updatedAt": "2024-01-15T15:30:00.000Z"
  }
}
```

### 删除商品

**DELETE** `/api/products/{id}`

删除商品（仅商品发布者可操作）。

**路径参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品ID |

**响应示例:**
```json
{
  "success": true,
  "message": "删除成功",
  "data": null
}
```

### 获取我的商品

**GET** `/api/products/my`

获取当前用户发布的商品列表。

**查询参数:**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |
| status | Integer | 否 | - | 商品状态筛选 |

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 26,
        "title": "MacBook Pro 13寸 2022款",
        "price": 8000.00,
        "status": 1,
        "statusName": "在售",
        "views": 23,
        "images": ["http://localhost:8080/uploads/products/macbook1.jpg"],
        "createdAt": "2024-01-15T14:30:00.000Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

## 📦 订单管理

### 创建订单

**POST** `/api/orders`

创建新订单。

**请求参数:**
```json
{
  "productId": 1,
  "buyerName": "张三",
  "buyerContact": "13800138000",
  "message": "希望能在东校区食堂交易，谢谢！"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品ID |
| buyerName | String | 是 | 买家姓名 |
| buyerContact | String | 是 | 买家联系方式 |
| message | String | 否 | 留言信息 |

**响应示例:**
```json
{
  "success": true,
  "message": "订单创建成功",
  "data": {
    "id": 1,
    "orderNo": "ORD20240115001",
    "productId": 1,
    "productName": "iPhone 13 Pro 128GB",
    "productPrice": 4500.00,
    "productImage": "http://localhost:8080/uploads/products/phone1.jpg",
    "sellerId": 2,
    "sellerName": "seller01",
    "buyerId": 1,
    "buyerName": "张三",
    "buyerContact": "13800138000",
    "status": "PENDING",
    "statusName": "待确认",
    "message": "希望能在东校区食堂交易，谢谢！",
    "createdAt": "2024-01-15T16:00:00.000Z"
  }
}
```

### 获取订单列表

**GET** `/api/orders`

获取当前用户的订单列表（买家和卖家订单）。

**查询参数:**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |
| type | String | 否 | all | 订单类型：all-全部，buy-购买订单，sell-销售订单 |
| status | String | 否 | - | 订单状态筛选 |

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 1,
        "orderNo": "ORD20240115001",
        "productId": 1,
        "productName": "iPhone 13 Pro 128GB",
        "productPrice": 4500.00,
        "productImage": "http://localhost:8080/uploads/products/phone1.jpg",
        "sellerId": 2,
        "sellerName": "seller01",
        "buyerId": 1,
        "buyerName": "张三",
        "buyerContact": "13800138000",
        "status": "PENDING",
        "statusName": "待确认",
        "type": "BUY",
        "typeName": "购买订单",
        "createdAt": "2024-01-15T16:00:00.000Z"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### 获取订单详情

**GET** `/api/orders/{id}`

获取指定订单的详细信息。

**响应示例:**
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 1,
    "orderNo": "ORD20240115001",
    "product": {
      "id": 1,
      "title": "iPhone 13 Pro 128GB",
      "price": 4500.00,
      "images": ["http://localhost:8080/uploads/products/phone1.jpg"],
      "location": "东校区宿舍楼A座",
      "contactInfo": "微信：abc123456"
    },
    "seller": {
      "id": 2,
      "username": "seller01",
      "realName": "王五",
      "studentId": "20240002",
      "phone": "13700137000"
    },
    "buyer": {
      "id": 1,
      "username": "testuser",
      "realName": "张三",
      "studentId": "20240001"
    },
    "buyerName": "张三",
    "buyerContact": "13800138000",
    "status": "PENDING",
    "statusName": "待确认",
    "message": "希望能在东校区食堂交易，谢谢！",
    "createdAt": "2024-01-15T16:00:00.000Z",
    "updatedAt": "2024-01-15T16:00:00.000Z"
  }
}
```

### 更新订单状态

**PUT** `/api/orders/{id}/status`

更新订单状态（仅卖家可操作）。

**请求参数:**
```json
{
  "status": "CONFIRMED",
  "message": "已确认订单，请联系我安排交易时间和地点。"
}
```

**订单状态说明:**
- `PENDING`: 待确认
- `CONFIRMED`: 已确认
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

**响应示例:**
```json
{
  "success": true,
  "message": "状态更新成功",
  "data": {
    "id": 1,
    "status": "CONFIRMED",
    "statusName": "已确认",
    "updatedAt": "2024-01-15T17:00:00.000Z"
  }
}
```

## 💝 收藏管理

### 添加收藏

**POST** `/api/favorites`

将商品添加到收藏夹。

**请求参数:**
```json
{
  "productId": 1
}
```

**响应示例:**
```json
{
  "success": true,
  "message": "收藏成功",
  "data": {
    "id": 1,
    "productId": 1,
    "createdAt": "2024-01-15T18:00:00.000Z"
  }
}
```

### 取消收藏

**DELETE** `/api/favorites/{productId}`

取消收藏指定商品。

**路径参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| productId | Long | 商品ID |

**响应示例:**
```json
{
  "success": true,
  "message": "取消收藏成功",
  "data": null
}
```

### 获取收藏列表

**GET** `/api/favorites`

获取当前用户的收藏商品列表。

**查询参数:**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "title": "iPhone 13 Pro 128GB",
          "price": 4500.00,
          "status": 1,
          "statusName": "在售",
          "images": ["http://localhost:8080/uploads/products/phone1.jpg"],
          "seller": {
            "username": "seller01",
            "avatar": "http://localhost:8080/uploads/avatars/seller01.jpg"
          }
        },
        "createdAt": "2024-01-15T18:00:00.000Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

## 💬 消息系统

### 发送消息

**POST** `/api/messages`

发送消息给指定用户。

**请求参数:**
```json
{
  "receiverId": 2,
  "content": "你好，我对你的iPhone很感兴趣，可以详细介绍一下吗？",
  "type": "TEXT"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiverId | Long | 是 | 接收者用户ID |
| content | String | 是 | 消息内容 |
| type | String | 否 | 消息类型：TEXT-文本，IMAGE-图片 |

**响应示例:**
```json
{
  "success": true,
  "message": "发送成功",
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "content": "你好，我对你的iPhone很感兴趣，可以详细介绍一下吗？",
    "type": "TEXT",
    "status": "SENT",
    "createdAt": "2024-01-15T19:00:00.000Z"
  }
}
```

### 获取对话列表

**GET** `/api/messages/conversations`

获取当前用户的对话列表。

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": [
    {
      "userId": 2,
      "username": "seller01",
      "avatar": "http://localhost:8080/uploads/avatars/seller01.jpg",
      "lastMessage": {
        "content": "好的，我们明天下午3点在东校区食堂见面吧",
        "type": "TEXT",
        "createdAt": "2024-01-15T20:30:00.000Z"
      },
      "unreadCount": 2
    }
  ]
}
```

### 获取对话消息

**GET** `/api/messages/conversations/{userId}`

获取与指定用户的对话消息。

**路径参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| userId | Long | 对话用户ID |

**查询参数:**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码 |
| size | Integer | 否 | 20 | 每页数量 |

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 3,
        "senderId": 2,
        "senderName": "seller01",
        "senderAvatar": "http://localhost:8080/uploads/avatars/seller01.jpg",
        "receiverId": 1,
        "content": "好的，我们明天下午3点在东校区食堂见面吧",
        "type": "TEXT",
        "status": "READ",
        "createdAt": "2024-01-15T20:30:00.000Z"
      },
      {
        "id": 2,
        "senderId": 1,
        "senderName": "testuser",
        "senderAvatar": "http://localhost:8080/uploads/avatars/testuser.jpg",
        "receiverId": 2,
        "content": "可以面交吗？在哪里比较方便？",
        "type": "TEXT",
        "status": "READ",
        "createdAt": "2024-01-15T20:00:00.000Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

### 标记消息已读

**PUT** `/api/messages/{id}/read`

标记指定消息为已读。

**路径参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 消息ID |

**响应示例:**
```json
{
  "success": true,
  "message": "标记成功",
  "data": null
}
```

## 📁 文件上传

### 上传图片

**POST** `/api/files/upload`

上传图片文件。

**请求头:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**请求参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片文件，支持jpg、png、gif格式，最大5MB |
| type | String | 否 | 文件类型：avatar-头像，product-商品图片 |

**响应示例:**
```json
{
  "success": true,
  "message": "上传成功",
  "data": {
    "filename": "20240115_product_abc123.jpg",
    "originalName": "iphone.jpg",
    "url": "http://localhost:8080/uploads/products/20240115_product_abc123.jpg",
    "size": 1024000,
    "type": "image/jpeg",
    "uploadedAt": "2024-01-15T21:00:00.000Z"
  }
}
```

### 批量上传图片

**POST** `/api/files/upload/batch`

批量上传多个图片文件。

**请求参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | File[] | 是 | 图片文件数组，最多9个文件 |
| type | String | 否 | 文件类型 |

**响应示例:**
```json
{
  "success": true,
  "message": "上传成功",
  "data": [
    {
      "filename": "20240115_product_abc123.jpg",
      "url": "http://localhost:8080/uploads/products/20240115_product_abc123.jpg",
      "size": 1024000
    },
    {
      "filename": "20240115_product_def456.jpg",
      "url": "http://localhost:8080/uploads/products/20240115_product_def456.jpg",
      "size": 856000
    }
  ]
}
```

## 🔧 系统管理

### 获取商品分类

**GET** `/api/categories`

获取所有商品分类。

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "电子产品",
      "description": "手机、电脑、平板等电子设备",
      "icon": "📱",
      "sort": 1,
      "status": "ACTIVE",
      "productCount": 156
    },
    {
      "id": 2,
      "name": "图书教材",
      "description": "教科书、参考书、小说等",
      "icon": "📚",
      "sort": 2,
      "status": "ACTIVE",
      "productCount": 89
    }
  ]
}
```

### 系统统计信息

**GET** `/api/system/stats`

获取系统统计信息（需要管理员权限）。

**响应示例:**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "userCount": 1250,
    "productCount": 856,
    "orderCount": 342,
    "todayNewUsers": 15,
    "todayNewProducts": 23,
    "todayNewOrders": 8,
    "categoryStats": [
      {
        "categoryName": "电子产品",
        "productCount": 156,
        "percentage": 18.2
      }
    ]
  }
}
```

### 健康检查

**GET** `/actuator/health`

系统健康检查接口。

**响应示例:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

## ❌ 错误码说明

### HTTP状态码
- **200**: 请求成功
- **201**: 创建成功
- **400**: 请求参数错误
- **401**: 未授权，需要登录
- **403**: 禁止访问，权限不足
- **404**: 资源不存在
- **409**: 资源冲突
- **500**: 服务器内部错误

### 业务错误码
| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名已存在 |
| 1002 | 邮箱已被注册 |
| 1003 | 学号已被注册 |
| 1004 | 用户名或密码错误 |
| 1005 | 账号已被禁用 |
| 2001 | 商品不存在 |
| 2002 | 商品已下架 |
| 2003 | 不能购买自己的商品 |
| 2004 | 商品已被收藏 |
| 2005 | 商品未被收藏 |
| 3001 | 订单不存在 |
| 3002 | 订单状态不允许此操作 |
| 3003 | 无权限操作此订单 |
| 4001 | 文件格式不支持 |
| 4002 | 文件大小超出限制 |
| 4003 | 文件上传失败 |
| 5001 | 分类不存在 |
| 5002 | 系统维护中 |

### 错误响应格式
```json
{
  "success": false,
  "message": "用户名已存在",
  "code": 1001,
  "data": null,
  "timestamp": "2024-01-15T22:00:00.000Z"
}
```

## 🔌 WebSocket接口

### 连接地址
```
ws://localhost:8080/ws?token=<jwt_token>
```

### 消息格式
```json
{
  "type": "CHAT_MESSAGE",
  "data": {
    "receiverId": 2,
    "content": "你好",
    "messageType": "TEXT"
  }
}
```

### 消息类型
- `CHAT_MESSAGE`: 聊天消息
- `SYSTEM_NOTIFICATION`: 系统通知
- `ORDER_UPDATE`: 订单状态更新
- `USER_ONLINE`: 用户上线
- `USER_OFFLINE`: 用户下线

## 📞 技术支持

如果在使用API过程中遇到问题，请：

1. 检查请求参数和格式
2. 确认认证令牌有效性
3. 查看错误码和错误信息
4. 参考示例代码
5. 联系技术支持团队

---

**注意**: 本API文档基于v1.0.0版本，如有更新请关注版本变更说明。所有时间均为UTC时间格式。