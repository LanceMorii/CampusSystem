/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80043
 Source Host           : localhost:3306
 Source Schema         : campus_trading

 Target Server Type    : MySQL
 Target Server Version : 80043
 File Encoding         : 65001

 Date: 25/09/2025 20:21:41
*/

CREATE DATABASE IF NOT EXISTS campus_trading CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_trading;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '电子产品', 0, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (2, '图书教材', 0, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (3, '生活用品', 0, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (4, '服装配饰', 0, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (5, '运动健身', 0, 5, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (6, '其他', 0, 6, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (7, '手机数码', 1, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (8, '电脑配件', 1, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (9, '耳机音响', 1, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (10, '智能设备', 1, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (11, '专业教材', 2, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (12, '考试资料', 2, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (13, '课外读物', 2, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (14, '工具书', 2, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (15, '宿舍用品', 3, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (16, '护肤美妆', 3, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (17, '食品零食', 3, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (18, '日用百货', 3, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (19, '男装', 4, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (20, '女装', 4, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (21, '鞋帽', 4, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (22, '配饰', 4, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (23, '运动器材', 5, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (24, '运动服装', 5, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (25, '户外用品', 5, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');

-- ----------------------------
-- Table structure for chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话标识',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `online_status` tinyint NULL DEFAULT 0 COMMENT '在线状态:0离线,1在线',
  `last_active_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '离开时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_chat_sessions_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_chat_sessions_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_chat_sessions_online_status`(`online_status` ASC) USING BTREE,
  INDEX `idx_chat_sessions_last_active`(`last_active_time` ASC) USING BTREE,
  CONSTRAINT `chat_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_sessions
-- ----------------------------
INSERT INTO `chat_sessions` VALUES (1, 'session_001', 1, '张三', 0, '2025-01-21 15:32:00', '2025-09-25 01:35:11', NULL, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `chat_sessions` VALUES (2, 'session_002', 2, '李四', 1, '2025-01-21 16:00:00', '2025-09-25 01:35:11', NULL, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `chat_sessions` VALUES (3, 'session_003', 1, '张三', 1, '2025-01-21 16:16:00', '2025-09-25 01:35:11', NULL, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `chat_sessions` VALUES (4, 'session_004', 2, '李四', 1, '2025-01-21 16:26:00', '2025-09-25 01:35:11', NULL, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `chat_sessions` VALUES (5, 'session_005', 4, '赵六', 1, '2025-01-21 16:31:00', '2025-09-25 01:35:11', NULL, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `chat_sessions` VALUES (6, 'group_main', 1, '张三', 1, '2025-01-21 16:21:00', '2025-09-25 01:35:11', NULL, '2025-09-25 01:35:11', '2025-09-25 01:35:11');

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NOT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKgh1s14hhb9qb8p2do933hscsf`(`user_id` ASC, `product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `product_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `type` int NULL DEFAULT NULL,
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TEXT' COMMENT '消息类型枚举:TEXT,IMAGE,SYSTEM,GIFT',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sender_nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_read` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `send_time` datetime(6) NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `video_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `read_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_messages_from_user`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_messages_to_user`(`to_user_id` ASC) USING BTREE,
  INDEX `idx_messages_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_messages_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_messages_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_messages_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_messages_message_type`(`message_type` ASC) USING BTREE,
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `messages_ibfk_3` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 3, 1, NULL, '你好，这个iPhone还在吗？', 1, 'TEXT', 'session_001', '王五', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (2, 1, 3, NULL, '在的，你什么时候方便看货？', 1, 'TEXT', 'session_001', '张三', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (3, 3, 1, NULL, '明天下午可以吗？', 1, 'TEXT', 'session_001', '王五', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (4, 1, 3, NULL, '可以，明天下午2点在宿舍楼下见面', 1, 'TEXT', 'session_001', '张三', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (5, 4, 2, NULL, '教材还有吗？笔记多不多？', 1, 'TEXT', 'session_002', '赵六', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (6, 2, 4, NULL, '有的，笔记不多，主要是重点标记', 1, 'TEXT', 'session_002', '李四', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (7, 4, 2, NULL, '好的，我要了', 1, 'TEXT', 'session_002', '赵六', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (8, 1, 5, NULL, '哑铃还在吗？重量可以调节到多少？', 1, 'TEXT', 'session_003', '张三', NULL, 0, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (9, 5, 1, NULL, '在的，可以从5kg调到20kg，很方便', 1, 'TEXT', 'session_003', '钱七', NULL, 0, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (10, 2, 3, NULL, '这个笔记本电脑还在吗？', 1, 'TEXT', 'session_004', '李四', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (11, 3, 2, NULL, '在的，你可以来看看', 1, 'TEXT', 'session_004', '王五', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (12, 4, 5, NULL, '这个化妆品套装怎么样？', 1, 'TEXT', 'session_005', '赵六', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (13, 5, 4, NULL, '很好用，朋友推荐的', 1, 'TEXT', 'session_005', '钱七', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (14, 1, 1, NULL, '大家好，欢迎来到校园二手交易群！', 3, 'SYSTEM', 'group_main', '系统消息', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (15, 2, 2, NULL, '有人需要计算机相关的教材吗？', 1, 'TEXT', 'group_main', '李四', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (16, 3, 3, NULL, '我需要Java编程的书', 1, 'TEXT', 'group_main', '王五', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (17, 4, 4, NULL, '我这里有几本，私聊我', 1, 'TEXT', 'group_main', '赵六', NULL, 1, '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `messages` VALUES (18, 6, 7, 11, '你好', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:10:08', NULL, '2025-09-25 11:10:08.106537', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (19, 6, 7, 11, '请问你这个东西卖出去了吗', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:10:40', NULL, '2025-09-25 11:10:39.509304', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (20, 6, 7, 11, '没有的', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:11:57', NULL, '2025-09-25 11:11:57.225267', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (21, 7, 6, 11, '你好', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:16:41', NULL, '2025-09-25 11:16:40.771928', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (22, 7, 6, 11, '你好', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:16:50', NULL, '2025-09-25 11:16:49.790568', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (23, 7, 6, 11, '我是20231614248', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:17:25', NULL, '2025-09-25 11:17:25.473823', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (24, 6, 7, 11, '我是20231614241', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:17:51', NULL, '2025-09-25 11:17:50.922991', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (25, 6, 7, 11, '你好', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:20:39', NULL, '2025-09-25 11:20:38.679263', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (26, 6, 7, 11, '我是谁谁谁', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:20:45', NULL, '2025-09-25 11:20:44.740720', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (27, 7, 6, 11, '我咋知道你是谁', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:21:19', NULL, '2025-09-25 11:21:18.668422', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (28, 7, 6, 11, '我是48', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:23:18', NULL, '2025-09-25 11:23:17.813329', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (29, 6, 7, 11, '我是41', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:23:37', NULL, '2025-09-25 11:23:37.087818', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (30, 6, 7, 11, '你', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:42:50', NULL, '2025-09-25 11:42:50.371763', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (31, 6, 7, 11, '你还好吗', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:44:04', NULL, '2025-09-25 11:44:04.142058', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (32, 6, 7, 11, 'oi', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:45:37', NULL, '2025-09-25 11:45:36.983976', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (33, 7, 6, 11, '咋了', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:45:55', NULL, '2025-09-25 11:45:55.117476', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (34, 6, 7, 11, '现在还有问题吗', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:49:35', NULL, '2025-09-25 11:49:34.927002', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (35, 6, 7, 11, '你好吗', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:51:35', NULL, '2025-09-25 11:51:34.521998', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (36, 6, 7, 11, '你好', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:52:06', NULL, '2025-09-25 11:52:05.860255', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (37, 6, 7, 11, '现在正常吗', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 11:54:31', NULL, '2025-09-25 11:54:30.692617', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (38, 7, 6, 11, '你好我是48', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 14:33:54', NULL, '2025-09-25 14:33:54.395048', NULL, NULL, NULL);
INSERT INTO `messages` VALUES (39, 6, 7, 11, '你好我是41', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 14:34:03', NULL, '2025-09-25 14:34:03.220837', NULL, NULL, '2025-09-25 14:43:43.574067');
INSERT INTO `messages` VALUES (40, 6, 7, 11, '你看到我发的消息了吗啊啊', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 14:44:08', NULL, '2025-09-25 14:44:07.561130', NULL, NULL, '2025-09-25 14:59:50.625015');
INSERT INTO `messages` VALUES (41, 7, 6, 11, '看到了', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 14:59:54', NULL, '2025-09-25 14:59:54.416512', NULL, NULL, '2025-09-25 15:00:02.438646');
INSERT INTO `messages` VALUES (42, 7, 6, 11, '别叫了', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 14:59:58', NULL, '2025-09-25 14:59:58.461026', NULL, NULL, '2025-09-25 15:00:02.438646');
INSERT INTO `messages` VALUES (43, 6, 7, 11, '....', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 15:00:05', NULL, '2025-09-25 15:00:05.204895', NULL, NULL, '2025-09-25 15:13:45.961410');
INSERT INTO `messages` VALUES (44, 6, 7, 11, '你麻痹', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 15:00:07', NULL, '2025-09-25 15:00:07.099908', NULL, NULL, '2025-09-25 15:13:45.961410');
INSERT INTO `messages` VALUES (45, 6, 7, 11, '你好,这个还在不', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 19:42:05', NULL, '2025-09-25 19:42:04.586659', NULL, NULL, '2025-09-25 19:42:30.382966');
INSERT INTO `messages` VALUES (46, 6, 7, 11, '多少钱了', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 19:42:07', NULL, '2025-09-25 19:42:06.735945', NULL, NULL, '2025-09-25 19:42:30.382966');
INSERT INTO `messages` VALUES (47, 7, 6, 11, '不在了', 1, 'TEXT', NULL, NULL, NULL, 1, '2025-09-25 19:42:33', NULL, '2025-09-25 19:42:33.468500', NULL, NULL, '2025-09-25 19:45:32.843666');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `status` int NULL DEFAULT NULL,
  `buyer_confirm` int NULL DEFAULT NULL,
  `seller_confirm` int NULL DEFAULT NULL,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_orders_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_orders_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_orders_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_orders_status`(`status` ASC) USING BTREE,
  INDEX `idx_orders_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_orders_order_no`(`order_no` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `images` json NULL COMMENT '商品图片JSON数组',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `condition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `include_invoice` bit(1) NULL DEFAULT NULL,
  `include_warranty` bit(1) NULL DEFAULT NULL,
  `is_negotiable` bit(1) NULL DEFAULT NULL,
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `purchase_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `purchase_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `tags` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `trade_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_products_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_products_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_products_status`(`status` ASC) USING BTREE,
  INDEX `idx_products_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_products_price`(`price` ASC) USING BTREE,
  INDEX `idx_products_title`(`title` ASC) USING BTREE,
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `products_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (11, 7, 1, '测试', '测试商品', 12.00, 23.00, '[\"products/12f238555bf443139501e397e86a19d2.jpg\"]', 26, 1, '2025-09-25 10:29:31', '2025-09-25 19:58:39', NULL, NULL, NULL, b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (12, 6, 6, '41的测试商品', '41的测试商品', 12.00, 13.00, '[\"products/113e15cea5fc4cd195751ae7de30bcd1.jpg\"]', 8, 1, '2025-09-25 18:55:03', '2025-09-25 19:55:02', NULL, NULL, NULL, b'0', b'0', b'0', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `ban_expire_time` datetime(6) NULL DEFAULT NULL,
  `ban_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `banned` bit(1) NULL DEFAULT NULL,
  `last_active_time` datetime(6) NULL DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `online` bit(1) NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_users_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_users_status`(`status` ASC) USING BTREE,
  INDEX `idx_users_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '20210001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '张三', '13800138001', 'zhangsan@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (2, '20210002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '李四', '13800138002', 'lisi@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (3, '20210003', '王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '王五', '13800138003', 'wangwu@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (4, '20210004', '赵六', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '赵六', '13800138004', 'zhaoliu@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (5, '20210005', '钱七', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '钱七', '13800138005', 'qianqi@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (6, '20231614241', '20231614241', '$2a$10$IqVntKDB11V2XL5tGiikw.Vyb4l9UniMRNzs3DYPMY3xtYTtsJERG', '李文昊', '15035267995', '15035267995@163.com', NULL, 1, '2025-09-25 01:50:54', '2025-09-25 01:50:54', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);
INSERT INTO `users` VALUES (7, '20231614248', '20231614248', '$2a$10$wpJNqb3uPH5hNi4jK7bvFewIOEyP9ed1NehXPVIvDazzCiA8tCrcK', '20231614248', '19317342735', '19317342735@163.com', NULL, 1, '2025-09-25 10:28:14', '2025-09-25 10:28:14', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);

SET FOREIGN_KEY_CHECKS = 1;
