-- 消息表结构迁移：为 chat-service 添加字段
-- 执行前请确保 ice_user.message 表存在

-- 添加消息类型字段
ALTER TABLE message
ADD COLUMN IF NOT EXISTS msg_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE' AFTER content;

-- 添加读取时间字段
ALTER TABLE message
ADD COLUMN IF NOT EXISTS read_time DATETIME DEFAULT NULL COMMENT '读取时间' AFTER status;

-- 添加逻辑删除字段（如果不存在）
ALTER TABLE message
ADD COLUMN IF NOT EXISTS is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是' AFTER read_time;

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_receiver_status ON message(receiver_id, status);
CREATE INDEX IF NOT EXISTS idx_sent_time ON message(sent_time);
