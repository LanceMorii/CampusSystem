-- 校园二手交易系统初始化数据脚本
USE campus_trading;

-- 清空现有数据 (开发环境使用)
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE messages;
-- TRUNCATE TABLE orders;
-- TRUNCATE TABLE products;
-- TRUNCATE TABLE categories;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- 插入分类数据 (不指定ID，让数据库自动生成)
INSERT INTO categories (name, parent_id, sort_order, status) VALUES
-- 一级分类
('电子产品', 0, 1, 1),
('图书教材', 0, 2, 1),
('生活用品', 0, 3, 1),
('服装配饰', 0, 4, 1),
('运动健身', 0, 5, 1),
('其他', 0, 6, 1);

-- 获取一级分类的ID并插入子分类
INSERT INTO categories (name, parent_id, sort_order, status) VALUES
-- 电子产品子分类 (parent_id = 1)
('手机数码', 1, 1, 1),
('电脑配件', 1, 2, 1),
('耳机音响', 1, 3, 1),
('智能设备', 1, 4, 1),

-- 图书教材子分类 (parent_id = 2)
('专业教材', 2, 1, 1),
('考试资料', 2, 2, 1),
('课外读物', 2, 3, 1),
('工具书', 2, 4, 1),

-- 生活用品子分类 (parent_id = 3)
('宿舍用品', 3, 1, 1),
('护肤美妆', 3, 2, 1),
('食品零食', 3, 3, 1),
('日用百货', 3, 4, 1),

-- 服装配饰子分类 (parent_id = 4)
('男装', 4, 1, 1),
('女装', 4, 2, 1),
('鞋帽', 4, 3, 1),
('配饰', 4, 4, 1),

-- 运动健身子分类 (parent_id = 5)
('运动器材', 5, 1, 1),
('运动服装', 5, 2, 1),
('户外用品', 5, 3, 1);

-- 插入测试用户数据 (密码为 password123 的BCrypt加密结果)
INSERT INTO users (student_id, username, password, real_name, phone, email, avatar, status) VALUES
('20210001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '张三', '13800138001', 'zhangsan@example.com', NULL, 1),
('20210002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '李四', '13800138002', 'lisi@example.com', NULL, 1),
('20210003', '王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '王五', '13800138003', 'wangwu@example.com', NULL, 1),
('20210004', '赵六', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '赵六', '13800138004', 'zhaoliu@example.com', NULL, 1),
('20210005', '钱七', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '钱七', '13800138005', 'qianqi@example.com', NULL, 1);

-- 插入测试商品数据 (使用动态分类ID)
INSERT INTO products (user_id, category_id, title, description, price, original_price, images, view_count, status) VALUES
-- 电子产品 (假设手机数码分类ID为7)
(1, 7, 'iPhone 13 128GB 蓝色', '9成新iPhone 13，无磕碰，功能正常，配件齐全', 4500.00, 5999.00, '["https://example.com/images/iphone13_1.jpg", "https://example.com/images/iphone13_2.jpg"]', 25, 1),
(2, 8, '联想ThinkPad E14 笔记本', '用了一年的笔记本，i5处理器，8G内存，256G固态硬盘', 3200.00, 4999.00, '["https://example.com/images/thinkpad_1.jpg"]', 18, 1),
(3, 9, 'AirPods Pro 2代', '全新未拆封，朋友送的多了一个', 1800.00, 1999.00, '["https://example.com/images/airpods_1.jpg"]', 32, 1),

-- 图书教材
(2, 11, '高等数学教材（上下册）', '同济大学版高等数学，9成新，有少量笔记', 45.00, 89.00, '["https://example.com/images/math_book.jpg"]', 12, 1),
(4, 12, '英语四级真题集', '星火英语四级真题，做过几套，答案完整', 15.00, 35.00, '["https://example.com/images/cet4_book.jpg"]', 8, 1),

-- 生活用品
(1, 15, '宿舍小冰箱', '美的小冰箱，容量50L，制冷效果好', 280.00, 399.00, '["https://example.com/images/fridge_1.jpg", "https://example.com/images/fridge_2.jpg"]', 15, 1),
(5, 16, '兰蔻粉水套装', '朋友代购的，不适合我的肤质，9成新', 320.00, 450.00, '["https://example.com/images/lancome_1.jpg"]', 22, 1),

-- 服装配饰
(3, 19, 'Nike运动外套', 'L码，深蓝色，穿过几次，无破损', 180.00, 299.00, '["https://example.com/images/nike_jacket.jpg"]', 9, 1),
(4, 21, 'Adidas运动鞋', '42码，黑白配色，鞋底磨损轻微', 220.00, 399.00, '["https://example.com/images/adidas_shoes.jpg"]', 14, 1),

-- 运动健身
(5, 23, '哑铃套装', '可调节哑铃，5-20kg，适合宿舍健身', 150.00, 280.00, '["https://example.com/images/dumbbell.jpg"]', 7, 1);

-- 插入测试订单数据
INSERT INTO orders (order_no, buyer_id, seller_id, product_id, amount, status, buyer_confirm, seller_confirm, remark) VALUES
('ORD202401010001', 3, 1, 1, 4500.00, 3, 1, 1, '交易顺利，商品如描述'),
('ORD202401010002', 4, 2, 4, 45.00, 2, 0, 1, '明天下午3点图书馆门口交易'),
('ORD202401010003', 1, 5, 10, 150.00, 1, 0, 0, '想要这个哑铃套装');

-- 插入测试消息数据
INSERT INTO messages (from_user_id, to_user_id, product_id, content, type, is_read) VALUES
-- 关于iPhone的对话
(3, 1, 1, '你好，这个iPhone还在吗？', 1, 1),
(1, 3, 1, '在的，你什么时候方便看货？', 1, 1),
(3, 1, 1, '明天下午可以吗？', 1, 1),
(1, 3, 1, '可以，明天下午2点在宿舍楼下见面', 1, 1),

-- 关于教材的对话
(4, 2, 4, '教材还有吗？笔记多不多？', 1, 1),
(2, 4, 4, '有的，笔记不多，主要是重点标记', 1, 1),
(4, 2, 4, '好的，我要了', 1, 1),

-- 关于哑铃的对话
(1, 5, 10, '哑铃还在吗？重量可以调节到多少？', 1, 0),
(5, 1, 10, '在的，可以从5kg调到20kg，很方便', 1, 0);

-- 重置自增ID (可选，确保ID从正确的值开始)
-- ALTER TABLE categories AUTO_INCREMENT = 26;
-- ALTER TABLE users AUTO_INCREMENT = 6;
-- ALTER TABLE products AUTO_INCREMENT = 11;
-- ALTER TABLE orders AUTO_INCREMENT = 4;
-- ALTER TABLE messages AUTO_INCREMENT = 10;

-- 查询验证数据
SELECT '=== 分类数据 ===' as info;
SELECT id, name, parent_id, sort_order FROM categories ORDER BY parent_id, sort_order;

SELECT '=== 用户数据 ===' as info;
SELECT id, student_id, username, real_name, phone, email, status FROM users;

SELECT '=== 商品数据 ===' as info;
SELECT p.id, p.title, p.price, c.name as category, u.username as seller, p.status 
FROM products p 
JOIN categories c ON p.category_id = c.id 
JOIN users u ON p.user_id = u.id;

SELECT '=== 订单数据 ===' as info;
SELECT o.id, o.order_no, b.username as buyer, s.username as seller, p.title as product, o.amount, o.status
FROM orders o
JOIN users b ON o.buyer_id = b.id
JOIN users s ON o.seller_id = s.id  
JOIN products p ON o.product_id = p.id;

SELECT '=== 消息数据 ===' as info;
SELECT m.id, f.username as from_user, t.username as to_user, p.title as product, m.content, m.is_read
FROM messages m
JOIN users f ON m.from_user_id = f.id
JOIN users t ON m.to_user_id = t.id
LEFT JOIN products p ON m.product_id = p.id;