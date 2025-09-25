# 校园二手交易系统 (Campus Trading System)

基于 Spring Boot 3 + Vue 3 构建的现代化校园二手商品交易平台，为校园用户提供安全、便捷的二手商品交易服务。

## 🚀 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security 6 + JWT
- **数据库**: MySQL 8.0 + MyBatis-Plus
- **缓存**: Redis 7.0
- **通信**: WebSocket (实时消息)
- **文档**: Swagger 3 (OpenAPI)
- **构建**: Maven 3.9+

### 前端技术
- **框架**: Vue 3 + Composition API
- **构建**: Vite 4
- **路由**: Vue Router 4
- **HTTP**: Axios
- **UI**: 原生CSS + 响应式设计
- **通知**: 自定义Toast/Modal组件

## ✨ 核心功能

### 🛍️ 商品管理
- **商品发布**: 支持多图片上传、分类管理、详细描述
- **商品浏览**: 分类筛选、搜索、分页展示
- **商品详情**: 高清图片展示、卖家信息、联系方式
- **我的商品**: 发布历史、状态管理、编辑删除

### 👤 用户系统
- **用户注册**: 学号验证、邮箱/手机验证
- **用户登录**: JWT令牌认证、自动续期
- **个人资料**: 头像上传、信息修改、安全设置
- **权限管理**: 基于角色的访问控制

### 💝 收藏功能
- **商品收藏**: 一键收藏/取消收藏
- **收藏管理**: 收藏列表、批量操作
- **智能提醒**: 收藏商品状态变更通知

### 📦 订单系统
- **订单创建**: 商品下单、联系信息填写
- **订单管理**: 订单状态跟踪、历史记录
- **交易流程**: 买卖双方信息对接

### 💬 消息系统
- **实时通信**: WebSocket双向通信
- **消息中心**: 系统通知、交易消息
- **聊天功能**: 买卖双方即时沟通

### 🔧 系统管理
- **分类管理**: 商品分类的增删改查
- **用户管理**: 用户信息管理、权限分配
- **系统监控**: 缓存管理、日志记录
- **文件管理**: 图片上传、存储管理

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端层 (Vue 3)                        │
├─────────────────────────────────────────────────────────┤
│  商品管理  │  用户中心  │  订单系统  │  消息中心  │  系统管理  │
└─────────────────────────────────────────────────────────┘
                              │
                         HTTP/WebSocket
                              │
┌─────────────────────────────────────────────────────────┐
│                   后端层 (Spring Boot)                   │
├─────────────────────────────────────────────────────────┤
│  Controller  │  Service  │  Repository  │  Security     │
├─────────────────────────────────────────────────────────┤
│  JWT认证     │  AOP日志  │  异常处理    │  WebSocket    │
└─────────────────────────────────────────────────────────┘
                              │
                         数据访问层
                              │
┌─────────────────────────────────────────────────────────┐
│              数据存储层                                   │
├─────────────────────────────────────────────────────────┤
│     MySQL 8.0        │      Redis 7.0      │  文件存储   │
│   (主要数据存储)       │    (缓存/会话)       │  (图片文件)  │
└─────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Maven**: 3.9+

### 后端启动

1. **克隆项目**
```bash
git clone <项目地址>
cd CampusSystem/campus-backend
```

2. **配置数据库**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trading?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

3. **配置Redis**
```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

4. **初始化数据库**
```bash
# 执行SQL脚本
mysql -u root -p campus_trading < doc/database/schema.sql
mysql -u root -p campus_trading < doc/database/init-data.sql
```

5. **启动后端服务**
```bash
mvn clean install
mvn spring-boot:run
```

### 前端启动

1. **进入前端目录**
```bash
cd ../campus-fonted
```

2. **安装依赖**
```bash
npm install
```

3. **配置环境变量**
```bash
# .env
VITE_API_BASE_URL=http://localhost:8080
```

4. **启动前端服务**
```bash
npm run dev
```

5. **访问应用**
   - 前端地址: http://localhost:3000
   - 后端API: http://localhost:8080
   - API文档: http://localhost:8080/swagger-ui/index.html

## 📊 项目结构

### 后端结构
```
campus-backend/
├── src/main/java/com/example/campussystem/
│   ├── config/                 # 配置类
│   │   ├── SecurityConfig.java # 安全配置
│   │   ├── RedisConfig.java    # Redis配置
│   │   ├── SwaggerConfig.java  # API文档配置
│   │   └── WebSocketConfig.java# WebSocket配置
│   ├── controller/             # 控制器层
│   │   ├── AuthController.java # 认证控制器
│   │   ├── ProductController.java # 商品控制器
│   │   ├── OrderController.java   # 订单控制器
│   │   └── MessageController.java # 消息控制器
│   ├── service/                # 服务层
│   │   ├── UserService.java    # 用户服务
│   │   ├── ProductService.java # 商品服务
│   │   └── OrderService.java   # 订单服务
│   ├── repository/             # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   └── OrderRepository.java
│   ├── entity/                 # 实体类
│   │   ├── User.java          # 用户实体
│   │   ├── Product.java       # 商品实体
│   │   └── Order.java         # 订单实体
│   ├── dto/                    # 数据传输对象
│   ├── security/               # 安全相关
│   ├── util/                   # 工具类
│   └── websocket/              # WebSocket处理
└── src/main/resources/
    ├── application.yml         # 应用配置
    └── logback-spring.xml      # 日志配置
```

### 前端结构
```
campus-fonted/
├── src/
│   ├── components/             # Vue组件
│   │   ├── Home.vue           # 首页
│   │   ├── ProductList.vue    # 商品列表
│   │   ├── ProductDetail.vue  # 商品详情
│   │   ├── ProductPublish.vue # 商品发布
│   │   ├── MyProducts.vue     # 我的商品
│   │   ├── Favorites.vue      # 收藏管理
│   │   ├── OrderManagement.vue# 订单管理
│   │   └── MessageCenter.vue  # 消息中心
│   ├── services/              # 服务层
│   │   ├── toast.js          # 通知服务
│   │   └── modal.js          # 弹窗服务
│   ├── api/                   # API接口
│   ├── router/                # 路由配置
│   └── assets/                # 静态资源
├── public/                    # 公共资源
└── package.json              # 项目配置
```

## 🔧 配置说明

### 数据库配置

**开发环境**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trading
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**生产环境**
```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/campus_trading
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### JWT配置
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

## 🌟 核心特性详解

### 安全认证
- **JWT令牌**: 无状态认证，支持自动续期
- **密码加密**: BCrypt加密存储
- **权限控制**: 基于角色的访问控制(RBAC)
- **跨域支持**: CORS配置，支持前后端分离

### 文件管理
- **多文件上传**: 支持批量图片上传
- **格式验证**: 限制文件类型和大小
- **存储优化**: 本地存储 + CDN加速
- **图片处理**: 自动压缩和格式转换

### 缓存策略
- **Redis缓存**: 热点数据缓存
- **分层缓存**: 多级缓存策略
- **缓存更新**: 智能缓存失效机制
- **性能优化**: 减少数据库访问

### 实时通信
- **WebSocket**: 双向实时通信
- **消息推送**: 系统通知推送
- **在线状态**: 用户在线状态管理
- **消息持久化**: 消息历史记录

## 📈 性能优化

### 后端优化
- **连接池**: HikariCP数据库连接池
- **缓存策略**: Redis多级缓存
- **异步处理**: @Async异步任务
- **分页查询**: 大数据量分页优化

### 前端优化
- **懒加载**: 图片和组件懒加载
- **代码分割**: 路由级别代码分割
- **缓存策略**: HTTP缓存和本地存储
- **响应式设计**: 移动端适配优化

## 🔒 安全措施

### 数据安全
- **SQL注入防护**: MyBatis参数化查询
- **XSS防护**: 输入输出过滤
- **CSRF防护**: CSRF令牌验证
- **敏感信息**: 密码和关键信息加密

### 接口安全
- **认证授权**: JWT + Spring Security
- **接口限流**: Redis + AOP限流
- **参数验证**: JSR-303数据验证
- **异常处理**: 全局异常处理机制

## 📚 API文档

项目集成了Swagger 3，启动后端服务后访问：
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 主要API端点

#### 认证相关
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新令牌
- `POST /auth/logout` - 用户登出

#### 商品相关
- `GET /api/products` - 获取商品列表
- `POST /api/products` - 发布商品
- `GET /api/products/{id}` - 获取商品详情
- `PUT /api/products/{id}` - 更新商品信息
- `DELETE /api/products/{id}` - 删除商品

#### 订单相关
- `POST /api/orders` - 创建订单
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `PUT /api/orders/{id}/status` - 更新订单状态

## 🚀 部署指南

### Docker部署

1. **构建镜像**
```bash
# 后端
cd campus-backend
docker build -t campus-backend:latest .

# 前端
cd ../campus-fonted
docker build -t campus-frontend:latest .
```

2. **Docker Compose部署**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: campus_trading
    ports:
      - "3306:3306"
  
  redis:
    image: redis:7.0
    ports:
      - "6379:6379"
  
  backend:
    image: campus-backend:latest
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
  
  frontend:
    image: campus-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend
```

### 生产环境部署

1. **环境准备**
   - 服务器: CentOS 7+ / Ubuntu 18+
   - Java: OpenJDK 17+
   - MySQL: 8.0+
   - Redis: 7.0+
   - Nginx: 1.18+

2. **后端部署**
```bash
# 打包应用
mvn clean package -Dmaven.test.skip=true

# 启动服务
java -jar target/campus-backend-1.0.0.jar --spring.profiles.active=prod
```

3. **前端部署**
```bash
# 构建生产版本
npm run build

# Nginx配置
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /var/www/campus-frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

## 📝 更新日志

### v1.0.0 (2024-01-15)
- ✨ 初始版本发布
- 🎉 完整的商品交易功能
- 🔐 用户认证和权限管理
- 💬 实时消息系统
- 📱 响应式前端界面


## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- **项目维护者**: Maven
- **邮箱**: 15035267995@163.com
- **项目地址**:
 Gitee:https://gitee.com/lancemorii-git/campus-system.git
 Github:https://github.com/LanceMorii/CampusSystem.git


---

**注意**: 这是一个校园二手交易系统，仅供学习和研究使用。在生产环境中使用前，请确保遵守相关法律法规和学校政策。