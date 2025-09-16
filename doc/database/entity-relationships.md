# 实体关系说明文档

## 概述

本文档详细说明了校园二手交易系统中各个实体类之间的关联关系和验证规则。

## 实体关系图

```
User (用户)
├── 1:N → Product (商品) [user_id]
├── 1:N → Order (订单-买家) [buyer_id]  
├── 1:N → Order (订单-卖家) [seller_id]
├── 1:N → Message (消息-发送) [from_user_id]
└── 1:N → Message (消息-接收) [to_user_id]

Category (分类)
├── 1:N → Product (商品) [category_id]
├── 1:N → Category (子分类) [parent_id] (自关联)
└── N:1 → Category (父分类) [parent_id] (自关联)

Product (商品)
├── N:1 → User (用户) [user_id]
├── N:1 → Category (分类) [category_id]
├── 1:N → Order (订单) [product_id]
└── 1:N → Message (消息) [product_id]

Order (订单)
├── N:1 → User (买家) [buyer_id]
├── N:1 → User (卖家) [seller_id]
└── N:1 → Product (商品) [product_id]

Message (消息)
├── N:1 → User (发送者) [from_user_id]
├── N:1 → User (接收者) [to_user_id]
└── N:1 → Product (商品) [product_id] (可选)
```

## 详细关系说明

### 1. User (用户) 实体

**主要属性：**
- `id`: 主键，自增长
- `studentId`: 学号，唯一索引
- `username`: 用户名
- `password`: 加密密码
- `realName`: 真实姓名
- `phone`: 手机号
- `email`: 邮箱
- `avatar`: 头像URL
- `status`: 状态 (1正常, 0禁用)

**关联关系：**
- `products`: 一对多，用户发布的商品
- `buyOrders`: 一对多，用户作为买家的订单
- `sellOrders`: 一对多，用户作为卖家的订单
- `sentMessages`: 一对多，用户发送的消息
- `receivedMessages`: 一对多，用户接收的消息

**验证规则：**
- 学号：必填，8-12位数字
- 用户名：必填，2-50个字符
- 密码：必填，最少8位
- 真实姓名：必填，最多50个字符
- 手机号：可选，11位手机号格式
- 邮箱：可选，邮箱格式验证

### 2. Category (分类) 实体

**主要属性：**
- `id`: 主键，自增长
- `name`: 分类名称
- `parentId`: 父分类ID (0表示顶级分类)
- `sortOrder`: 排序字段
- `status`: 状态 (1启用, 0禁用)

**关联关系：**
- `products`: 一对多，分类下的商品
- `parent`: 多对一，父分类 (自关联)
- `children`: 一对多，子分类 (自关联)

**验证规则：**
- 分类名称：必填，最多50个字符

### 3. Product (商品) 实体

**主要属性：**
- `id`: 主键，自增长
- `userId`: 发布用户ID
- `categoryId`: 分类ID
- `title`: 商品标题
- `description`: 商品描述
- `price`: 价格
- `originalPrice`: 原价
- `images`: 图片JSON数组
- `viewCount`: 浏览次数
- `status`: 状态 (1上架, 2下架, 3已售出, 0已删除)

**关联关系：**
- `user`: 多对一，商品发布者
- `category`: 多对一，商品分类
- `orders`: 一对多，商品的订单
- `messages`: 一对多，商品相关的消息

**验证规则：**
- 标题：必填，最多100个字符
- 描述：可选，最多2000个字符
- 价格：必填，0.01-99999999.99
- 原价：可选，0.01-99999999.99

**业务方法：**
- `isAvailable()`: 判断商品是否可用
- `isSold()`: 判断商品是否已售出
- `incrementViewCount()`: 增加浏览次数

### 4. Order (订单) 实体

**主要属性：**
- `id`: 主键，自增长
- `orderNo`: 订单号，唯一
- `buyerId`: 买家ID
- `sellerId`: 卖家ID
- `productId`: 商品ID
- `amount`: 交易金额
- `status`: 状态 (1待确认, 2进行中, 3已完成, 4已取消)
- `buyerConfirm`: 买家确认 (0未确认, 1已确认)
- `sellerConfirm`: 卖家确认 (0未确认, 1已确认)
- `remark`: 备注

**关联关系：**
- `buyer`: 多对一，买家用户
- `seller`: 多对一，卖家用户
- `product`: 多对一，订单商品

**验证规则：**
- 订单号：必填
- 买家ID：必填
- 卖家ID：必填
- 商品ID：必填
- 交易金额：必填，大于0
- 备注：可选，最多500个字符

**业务方法：**
- `isPending()`: 判断是否待确认
- `isInProgress()`: 判断是否进行中
- `isCompleted()`: 判断是否已完成
- `isCancelled()`: 判断是否已取消
- `isBothConfirmed()`: 判断双方是否都已确认
- `confirmByBuyer()`: 买家确认
- `confirmBySeller()`: 卖家确认

### 5. Message (消息) 实体

**主要属性：**
- `id`: 主键，自增长
- `fromUserId`: 发送者ID
- `toUserId`: 接收者ID
- `productId`: 关联商品ID (可选)
- `content`: 消息内容
- `type`: 消息类型 (1文本, 2图片)
- `isRead`: 是否已读 (0未读, 1已读)

**关联关系：**
- `fromUser`: 多对一，消息发送者
- `toUser`: 多对一，消息接收者
- `product`: 多对一，关联商品 (可选)

**验证规则：**
- 发送者ID：必填
- 接收者ID：必填
- 消息内容：必填，最多1000个字符

**业务方法：**
- `isTextMessage()`: 判断是否文本消息
- `isImageMessage()`: 判断是否图片消息
- `isUnread()`: 判断是否未读
- `markAsRead()`: 标记为已读

## 数据库约束

### 外键约束
- `products.user_id` → `users.id` (CASCADE DELETE)
- `products.category_id` → `categories.id` (RESTRICT DELETE)
- `orders.buyer_id` → `users.id` (CASCADE DELETE)
- `orders.seller_id` → `users.id` (CASCADE DELETE)
- `orders.product_id` → `products.id` (CASCADE DELETE)
- `messages.from_user_id` → `users.id` (CASCADE DELETE)
- `messages.to_user_id` → `users.id` (CASCADE DELETE)
- `messages.product_id` → `products.id` (SET NULL)

### 唯一约束
- `users.student_id`: 学号唯一
- `orders.order_no`: 订单号唯一

### 索引优化
- 用户表：学号、状态、创建时间
- 商品表：用户ID、分类ID、状态、创建时间、价格、标题
- 订单表：买家ID、卖家ID、商品ID、状态、创建时间、订单号
- 消息表：发送者ID、接收者ID、商品ID、创建时间、已读状态

## JPA配置说明

### 审计功能
所有实体都启用了JPA审计功能：
- `@EntityListeners(AuditingEntityListener.class)`
- `@CreatedDate`: 自动设置创建时间
- `@LastModifiedDate`: 自动设置更新时间

### 懒加载策略
关联关系默认使用懒加载 (`FetchType.LAZY`)，避免N+1查询问题。

### 级联操作
- 用户删除时，级联删除其商品、订单、消息
- 商品删除时，级联删除其订单、消息
- 分类删除时，限制删除 (如果有商品关联则不能删除)

这种设计确保了数据的完整性和一致性，同时提供了良好的性能和可维护性。