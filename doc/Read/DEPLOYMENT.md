# 校园二手交易系统部署指南

本文档详细介绍了校园二手交易系统的部署流程，包括开发环境、测试环境和生产环境的部署方案。

## 📋 目录

- [环境要求](#环境要求)
- [开发环境部署](#开发环境部署)
- [生产环境部署](#生产环境部署)
- [Docker部署](#docker部署)
- [数据库配置](#数据库配置)
- [Nginx配置](#nginx配置)
- [监控和日志](#监控和日志)
- [故障排除](#故障排除)

## 🔧 环境要求

### 基础环境
- **操作系统**: CentOS 7+ / Ubuntu 18+ / Windows 10+
- **Java**: OpenJDK 17 或 Oracle JDK 17+
- **Node.js**: 16.0+
- **npm**: 8.0+
- **Maven**: 3.9+

### 数据库和中间件
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Nginx**: 1.18+ (生产环境)

### 硬件要求

#### 最小配置
- **CPU**: 2核
- **内存**: 4GB
- **存储**: 50GB
- **网络**: 10Mbps

#### 推荐配置
- **CPU**: 4核
- **内存**: 8GB
- **存储**: 100GB SSD
- **网络**: 100Mbps

## 🚀 开发环境部署

### 1. 环境准备

#### 安装Java 17
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# 验证安装
java -version
javac -version
```

#### 安装Node.js
```bash
# 使用NodeSource仓库
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 验证安装
node --version
npm --version
```

#### 安装Maven
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# 验证安装
mvn --version
```

### 2. 数据库安装配置

#### MySQL 8.0安装
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server-8.0

# CentOS/RHEL
sudo yum install mysql-server

# 启动MySQL服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

#### 创建数据库和用户
```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE campus_trading CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'campus_user'@'localhost' IDENTIFIED BY 'campus_password_2024';

-- 授权
GRANT ALL PRIVILEGES ON campus_trading.* TO 'campus_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

#### Redis安装
```bash
# Ubuntu/Debian
sudo apt install redis-server

# CentOS/RHEL
sudo yum install redis

# 启动Redis服务
sudo systemctl start redis
sudo systemctl enable redis

# 测试连接
redis-cli ping
```

### 3. 项目部署

#### 克隆项目
```bash
git clone <项目仓库地址>
cd CampusSystem
```

#### 后端部署
```bash
cd campus-backend

# 配置数据库连接
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# 编辑配置文件，修改数据库连接信息

# 初始化数据库
mysql -u campus_user -p campus_trading < ../doc/database/schema.sql
mysql -u campus_user -p campus_trading < ../doc/database/init-data.sql

# 编译和启动
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 前端部署
```bash
cd ../campus-fonted

# 安装依赖
npm install

# 配置环境变量
cp .env.example .env
# 编辑.env文件，配置API地址

# 启动开发服务器
npm run dev
```

### 4. 验证部署

- **后端API**: http://localhost:8080
- **前端应用**: http://localhost:3000

## 🏭 生产环境部署

### 1. 服务器准备

### 2. 生产环境配置

#### MySQL生产配置
```bash
# 编辑MySQL配置
sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf

# 添加以下配置
[mysqld]
# 性能优化
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 连接优化
max_connections = 500
max_connect_errors = 1000
connect_timeout = 60
wait_timeout = 28800

# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 重启MySQL
sudo systemctl restart mysql
```

#### Redis生产配置
```bash
# 编辑Redis配置
sudo vim /etc/redis/redis.conf

# 修改以下配置
bind 127.0.0.1
port 6379
timeout 300
tcp-keepalive 300
maxmemory 1gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000

# 重启Redis
sudo systemctl restart redis
```

### 3. 应用部署

#### 后端生产部署
```bash
# 创建应用目录
mkdir -p /home/campus/app/backend
cd /home/campus/app/backend

# 上传应用包
# 将编译好的jar包上传到服务器

# 创建配置文件
cat > application-prod.yml << EOF
server:
  port: 8080
  servlet:
    context-path: /

spring:
  profiles:
    active: prod
  
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trading?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: campus_user
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

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

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  refresh-expiration: 604800000

logging:
  level:
    com.example.campussystem: INFO
    org.springframework.security: WARN
  file:
    name: /home/campus/logs/campus-backend.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
EOF

# 设置执行权限
chmod +x start.sh stop.sh

# 启动应用
./start.sh
```

#### 前端生产部署
```bash
# 在开发机器上构建
cd campus-fonted
npm run build

# 将dist目录上传到服务器
# 在服务器上创建前端目录
sudo mkdir -p /var/www/campus-frontend
sudo chown -R campus:campus /var/www/campus-frontend

# 解压前端文件到目录
cd /var/www/campus-frontend
# 上传并解压dist.tar.gz
```

### 4. Nginx配置

#### 安装Nginx
```bash
# Ubuntu/Debian
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx

# 启动Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### 配置Nginx
```bash
# 创建站点配置
sudo vim /etc/nginx/sites-available/campus-trading

# 添加以下配置
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    # SSL证书配置
    ssl_certificate /etc/ssl/certs/your-domain.crt;
    ssl_certificate_key /etc/ssl/private/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # 前端静态文件
    location / {
        root /var/www/campus-frontend/dist;
        try_files $uri $uri/ /index.html;
        
        # 缓存配置
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        # 缓冲配置
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }
    
    # WebSocket代理
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket超时
        proxy_read_timeout 86400;
    }
    
    # 文件上传
    location /uploads {
        alias /home/campus/app/backend/uploads;
        expires 1y;
        add_header Cache-Control "public";
    }
    
    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}

# 启用站点
sudo ln -s /etc/nginx/sites-available/campus-trading /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重载Nginx
sudo systemctl reload nginx
```

## 🐳 Docker部署

### 1. Dockerfile配置

#### 后端Dockerfile
```dockerfile
# campus-backend/Dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="campus-trading-team"
LABEL version="1.0.0"

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用目录
WORKDIR /app

# 复制jar文件
COPY target/campus-backend-*.jar app.jar

# 创建非root用户
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring:spring

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM参数
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 前端Dockerfile
```dockerfile
# campus-fonted/Dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源码
COPY . .

# 构建应用
RUN npm run build

# 生产阶段
FROM nginx:alpine

LABEL maintainer="campus-trading-team"
LABEL version="1.0.0"

# 复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制Nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

# 启动Nginx
CMD ["nginx", "-g", "daemon off;"]
```

### 2. Docker Compose配置

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: campus-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: campus_trading
      MYSQL_USER: campus_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./doc/database/schema.sql:/docker-entrypoint-initdb.d/1-schema.sql
      - ./doc/database/init-data.sql:/docker-entrypoint-initdb.d/2-init-data.sql
      - ./mysql.cnf:/etc/mysql/conf.d/mysql.cnf
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  redis:
    image: redis:7-alpine
    container_name: campus-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD}
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  backend:
    build:
      context: ./campus-backend
      dockerfile: Dockerfile
    container_name: campus-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: campus_trading
      DB_USERNAME: campus_user
      DB_PASSWORD: ${MYSQL_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    volumes:
      - backend_uploads:/app/uploads
      - backend_logs:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  frontend:
    build:
      context: ./campus-fonted
      dockerfile: Dockerfile
    container_name: campus-frontend
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      backend:
        condition: service_healthy
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  mysql_data:
  redis_data:
  backend_uploads:
  backend_logs:

networks:
  campus-network:
    driver: bridge
```

### 3. 环境变量配置

```bash
# .env
MYSQL_ROOT_PASSWORD=root_password_2024
MYSQL_PASSWORD=campus_password_2024
REDIS_PASSWORD=redis_password_2024
JWT_SECRET=your_jwt_secret_key_here_must_be_very_long_and_secure
```

### 4. Docker部署命令

```bash
# 构建和启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 更新服务
docker-compose pull
docker-compose up -d --force-recreate
```

## 📊 监控和日志

### 1. 应用监控

#### Spring Boot Actuator
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Prometheus配置
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'campus-backend'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

### 2. 日志管理

#### Logback配置
```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/home/campus/logs/campus-backend.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/home/campus/logs/campus-backend.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### 日志轮转脚本
```bash
#!/bin/bash
# log-rotate.sh

LOG_DIR="/home/campus/logs"
DAYS_TO_KEEP=30

# 压缩7天前的日志
find $LOG_DIR -name "*.log" -mtime +7 -exec gzip {} \;

# 删除30天前的压缩日志
find $LOG_DIR -name "*.log.gz" -mtime +$DAYS_TO_KEEP -delete

# 清理空文件
find $LOG_DIR -size 0 -delete

echo "Log rotation completed at $(date)"
```

### 3. 系统监控脚本

```bash
#!/bin/bash
# monitor.sh

# 检查服务状态
check_service() {
    local service=$1
    local port=$2
    
    if curl -f -s "http://localhost:$port/actuator/health" > /dev/null; then
        echo "✅ $service is healthy"
    else
        echo "❌ $service is down"
        # 发送告警通知
        # send_alert "$service is down"
    fi
}

# 检查磁盘空间
check_disk_space() {
    local usage=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ $usage -gt 80 ]; then
        echo "⚠️  Disk usage is ${usage}%"
        # send_alert "Disk usage is high: ${usage}%"
    else
        echo "✅ Disk usage is ${usage}%"
    fi
}

# 检查内存使用
check_memory() {
    local usage=$(free | awk 'NR==2{printf "%.0f", $3*100/$2}')
    if [ $usage -gt 80 ]; then
        echo "⚠️  Memory usage is ${usage}%"
        # send_alert "Memory usage is high: ${usage}%"
    else
        echo "✅ Memory usage is ${usage}%"
    fi
}

echo "=== System Health Check $(date) ==="
check_service "Backend" 8080
check_service "Frontend" 80
check_disk_space
check_memory
echo "=================================="
```

## 🔧 故障排除

### 常见问题

#### 1. 数据库连接失败
```bash
# 检查MySQL服务状态
sudo systemctl status mysql

# 检查端口监听
netstat -tlnp | grep 3306

# 检查防火墙
sudo firewall-cmd --list-ports

# 测试连接
mysql -h localhost -u campus_user -p campus_trading
```

#### 2. Redis连接失败
```bash
# 检查Redis服务状态
sudo systemctl status redis

# 测试连接
redis-cli ping

# 检查配置
sudo cat /etc/redis/redis.conf | grep -E "bind|port|requirepass"
```

#### 3. 应用启动失败
```bash
# 查看应用日志
tail -f /home/campus/logs/campus-backend.log

# 检查JVM参数
ps aux | grep java

# 检查端口占用
netstat -tlnp | grep 8080
```

#### 4. 前端访问失败
```bash
# 检查Nginx状态
sudo systemctl status nginx

# 测试Nginx配置
sudo nginx -t

# 查看Nginx日志
sudo tail -f /var/log/nginx/error.log
```

### 性能调优

#### JVM调优
```bash
# 生产环境JVM参数
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseStringDeduplication \
  -XX:+PrintGC \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Xloggc:/home/campus/logs/gc.log \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=10M"
```

#### MySQL调优
```sql
-- 查看当前配置
SHOW VARIABLES LIKE 'innodb%';

-- 查看状态
SHOW STATUS LIKE 'innodb%';

-- 优化查询
EXPLAIN SELECT * FROM products WHERE category_id = 1;

-- 添加索引
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_status ON products(status);
```

#### Redis调优
```bash
# 查看Redis信息
redis-cli info memory
redis-cli info stats

# 监控Redis性能
redis-cli monitor

# 分析慢查询
redis-cli slowlog get 10
```

## 📞 技术支持

如果在部署过程中遇到问题，请：

1. 查看相关日志文件
2. 检查系统资源使用情况
3. 验证网络连接和防火墙设置
4. 参考故障排除章节
5. 联系技术支持团队(15035267995@163.com)

---

**注意**: 本部署指南适用于生产环境，请根据实际情况调整配置参数。在部署前请确保备份重要数据。