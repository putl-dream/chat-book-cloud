-- 初始化社交数据库
-- 创建用户关注关系表 (替换 user_friends)
CREATE TABLE IF NOT EXISTS user_follow (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL COMMENT '关注者用户ID',
    follow_id   INT NOT NULL COMMENT '被关注者用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_follow (user_id, follow_id),
    INDEX idx_follow_id (follow_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';
