# 校园二手交易系统设计文档

## 系统概述

校园二手交易系统采用前后端分离的架构设计，前端使用Vue3框架构建SPA应用，后端使用Spring Boot构建RESTful API服务，通过HTTP协议进行通信。系统设计遵循高内聚、低耦合的原则，确保系统的可维护性和可扩展性。

## 系统架构

### 整体架构图

```
┌─────────────────┐    HTTP/HTTPS    ┌─────────────────┐
│   前端应用       │ ◄──────────────► │   后端API服务    │
│   (Vue3)        │                  │  (Spring Boot)  │
└─────────────────┘                  └─────────────────┘
                                              │
                                              ▼
                                     ┌─────────────────┐
                                     │   数据存储层     │
                                     │  MySQL + Redis  │
                                     └─────────────────┘
```

### 技术栈架构

**前端技术栈：**
- Vue 3.x - 渐进式JavaScript框架
- Vue Router 4.x - 路由管理
- Pinia - 状态管理
- Axios - HTTP客户端
- Element Plus - UI组件库
- Vite - 构建工具

**后端技术栈：**
- Spring Boot 2.7+ - 应用框架
- Spring Security - 安全框架
- Spring Data JPA - 数据访问层
- MySQL 8.0 - 关系型数据库
- Redis - 缓存数据库
- JWT - 无状态认证
- Maven - 项目管理工具

## 系统模块设计

### 前端模块结构

```
src/
├── components/          # 公共组件
│   ├── common/         # 通用组件
│   ├── layout/         # 布局组件
│   └── business/       # 业务组件
├── views/              # 页面组件
│   ├── auth/           # 认证相关页面
│   ├── product/        # 商品相关页面
│   ├── user/           # 用户相关页面
│   ├── message/        # 消息相关页面
│   └── admin/          # 管理后台页面
├── router/             # 路由配置
├── store/              # 状态管理
├── api/                # API接口
├── utils/              # 工具函数
└── assets/             # 静态资源
```

### 后端模块结构

```
src/main/java/com/example/campussystem/
├── controller/         # 控制器层
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── UserController.java
│   ├── MessageController.java
│   └── AdminController.java
├── service/            # 服务层
│   ├── UserService.java
│   ├── ProductService.java
│   ├── OrderService.java
│   └── MessageService.java
├── repository/         # 数据访问层
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── MessageRepository.java
├── entity/             # 实体类
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   └── Message.java
├── dto/                # 数据传输对象
├── config/             # 配置类
├── security/           # 安全配置
├── exception/          # 异常处理
└── util/               # 工具类
```

## 数据库设计

### 核心数据表

#### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(11) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态:1正常,0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 商品分类表 (categories)
```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态:1启用,0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### 商品表 (products)
```sql
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    title VARCHAR(100) NOT NULL COMMENT '商品标题',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) COMMENT '原价',
    images JSON COMMENT '商品图片JSON数组',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    status TINYINT DEFAULT 1 COMMENT '状态:1上架,2下架,3已售出,0已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

#### 订单表 (orders)
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单号',
    buyer_id BIGINT NOT NULL COMMENT '买家ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    status TINYINT DEFAULT 1 COMMENT '状态:1待确认,2进行中,3已完成,4已取消',
    buyer_confirm TINYINT DEFAULT 0 COMMENT '买家确认:0未确认,1已确认',
    seller_confirm TINYINT DEFAULT 0 COMMENT '卖家确认:0未确认,1已确认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

#### 消息表 (messages)
```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL COMMENT '发送者ID',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    product_id BIGINT COMMENT '关联商品ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type TINYINT DEFAULT 1 COMMENT '消息类型:1文本,2图片',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读:0未读,1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_user_id) REFERENCES users(id),
    FOREIGN KEY (to_user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

### 数据库索引设计

```sql
-- 用户表索引
CREATE INDEX idx_users_student_id ON users(student_id);
CREATE INDEX idx_users_status ON users(status);

-- 商品表索引
CREATE INDEX idx_products_user_id ON products(user_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_create_time ON products(create_time);
CREATE INDEX idx_products_price ON products(price);

-- 订单表索引
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_product_id ON orders(product_id);
CREATE INDEX idx_orders_status ON orders(status);

-- 消息表索引
CREATE INDEX idx_messages_from_user ON messages(from_user_id);
CREATE INDEX idx_messages_to_user ON messages(to_user_id);
CREATE INDEX idx_messages_product ON messages(product_id);
CREATE INDEX idx_messages_create_time ON messages(create_time);
```

## API接口设计

### RESTful API规范

**基础URL:** `http://localhost:8080/api/v1`

**通用响应格式:**
```json
{
    "code": 200,
    "message": "success",
    "data": {},
    "timestamp": "2024-01-01T12:00:00"
}
```

### 核心API接口

#### 认证相关接口
```
POST /auth/register          # 用户注册
POST /auth/login            # 用户登录
POST /auth/logout           # 用户登出
GET  /auth/profile          # 获取用户信息
PUT  /auth/profile          # 更新用户信息
```

#### 商品相关接口
```
GET    /products                    # 获取商品列表(分页+筛选)
GET    /products/{id}              # 获取商品详情
POST   /products                   # 发布商品
PUT    /products/{id}              # 更新商品
DELETE /products/{id}              # 删除商品
GET    /products/my                # 获取我发布的商品
POST   /products/{id}/view         # 增加浏览次数
```

#### 分类相关接口
```
GET /categories                    # 获取分类列表
GET /categories/{id}/products      # 获取分类下的商品
```

#### 订单相关接口
```
POST /orders                       # 创建订单
GET  /orders                       # 获取订单列表
GET  /orders/{id}                  # 获取订单详情
PUT  /orders/{id}/confirm          # 确认订单
PUT  /orders/{id}/cancel           # 取消订单
```

#### 消息相关接口
```
GET  /messages/conversations       # 获取对话列表
GET  /messages/conversations/{userId}  # 获取与指定用户的消息
POST /messages                     # 发送消息
PUT  /messages/{id}/read           # 标记消息已读
```

## 安全设计

### 认证授权机制

**JWT Token结构:**
```json
{
    "header": {
        "alg": "HS256",
        "typ": "JWT"
    },
    "payload": {
        "sub": "user_id",
        "username": "student_id",
        "role": "USER",
        "exp": 1640995200
    }
}
```

**权限控制:**
- 公开接口：商品列表、商品详情、分类列表
- 需要认证：用户信息、发布商品、创建订单、发送消息
- 管理员权限：用户管理、商品审核、数据统计

### 数据安全

**密码安全:**
- 使用BCrypt进行密码加密
- 密码强度验证（至少8位，包含字母和数字）

**输入验证:**
- 所有用户输入进行参数验证
- 防止SQL注入和XSS攻击
- 文件上传类型和大小限制

**数据脱敏:**
- 用户手机号部分隐藏
- 敏感信息不在前端暴露

## 性能优化设计

### 缓存策略

**Redis缓存应用:**
```
- 用户会话信息缓存 (TTL: 30分钟)
- 热门商品列表缓存 (TTL: 10分钟)  
- 分类信息缓存 (TTL: 1小时)
- 用户基本信息缓存 (TTL: 15分钟)
```

**数据库优化:**
- 合理使用索引提升查询性能
- 分页查询避免全表扫描
- 使用连接池管理数据库连接

### 前端性能优化

**资源优化:**
- 图片懒加载和压缩
- 路由懒加载
- 组件按需引入

**用户体验:**
- 骨架屏加载效果
- 防抖和节流处理
- 错误边界处理

## 错误处理策略

### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidation(ValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, e.getMessage()));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(401)
            .body(ApiResponse.error(401, "认证失败"));
    }
}
```

### 前端错误处理

```javascript
// HTTP拦截器
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            // 跳转到登录页
            router.push('/login');
        }
        return Promise.reject(error);
    }
);
```

## 测试策略

### 后端测试

**单元测试:**
- Service层业务逻辑测试
- Repository层数据访问测试
- 工具类方法测试

**集成测试:**
- Controller层API接口测试
- 数据库集成测试
- 安全认证测试

### 前端测试

**组件测试:**
- Vue组件单元测试
- 用户交互测试
- 路由跳转测试

**端到端测试:**
- 关键业务流程测试
- 跨浏览器兼容性测试

## 部署架构

### 开发环境
```
前端: npm run dev (Vite开发服务器)
后端: Spring Boot内嵌Tomcat
数据库: 本地MySQL + Redis
```

### 生产环境
```
前端: Nginx静态文件服务
后端: JAR包 + 外置Tomcat/直接运行
数据库: MySQL主从 + Redis集群
负载均衡: Nginx反向代理
```

这个设计文档涵盖了系统的整体架构、数据库设计、API设计、安全策略和性能优化等关键方面，为后续的开发实施提供了详细的技术指导。