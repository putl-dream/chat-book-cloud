-- 即时通讯消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `sender_id` INT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` INT NOT NULL COMMENT '接收者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `msg_type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '消息状态: 0=已发送, 1=未读, 2=已读',
    `sent_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `read_time` DATETIME DEFAULT NULL COMMENT '读取时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_receiver_status (receiver_id, status),
    INDEX idx_sent_time (sent_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='即时通讯消息表';
