-- =============================================
-- 互动服务数据库初始化脚本
-- 创建时间: 2026-03-13
-- 说明: 从 user-service 迁移 user_foot 和 review 表
-- =============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS chat_book_interaction DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE chat_book_interaction;

-- 2. 用户足迹表（从 user 数据库迁移）
CREATE TABLE IF NOT EXISTS `user_foot` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键id',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `document_id` INT NOT NULL COMMENT '文本id(文章/评论)',
    `document_type` INT DEFAULT 1 COMMENT '文本类型：1-文章，2-评论',
    `document_user_id` INT NOT NULL COMMENT '发布该文本的用户id',
    `collection_stat` INT DEFAULT 0 COMMENT '收藏状态 0-未收藏 1-已收藏',
    `read_stat` INT DEFAULT 0 COMMENT '浏览状态 0-未读 1-已读',
    `comment_stat` INT DEFAULT 0 COMMENT '评论状态 0-未评论 1-已评论',
    `praise_stat` INT DEFAULT 0 COMMENT '点赞状态 0-未点赞 1-已点赞',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_document` (`user_id`, `document_id`, `document_type`),
    KEY `idx_document_user` (`document_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户足迹表';

-- 3. 评论表（从 user 数据库迁移）
CREATE TABLE IF NOT EXISTS `review` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '评论主键',
    `text_id` INT NOT NULL COMMENT '文章ID，关联到文章表',
    `user_id` INT NOT NULL COMMENT '评论者ID',
    `parent_id` INT DEFAULT 0 COMMENT '父评论ID，0-评论文章，other-评论用户评论',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_text_id` (`text_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 4. 通知表（新增，为后续阶段准备）
CREATE TABLE IF NOT EXISTS `notification` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    `receiver_id` INT NOT NULL COMMENT '接收通知的用户',
    `sender_id` INT NOT NULL COMMENT '触发通知的用户',
    `action_type` VARCHAR(20) NOT NULL COMMENT 'PRAISE/COLLECT/COMMENT/FOLLOW',
    `target_id` INT COMMENT '目标内容ID（文章/评论）',
    `target_type` VARCHAR(20) COMMENT 'ARTICLE/COMMENT',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_receiver` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
