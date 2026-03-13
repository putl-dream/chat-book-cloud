-- =============================================
-- 清理脚本：从 user 数据库删除已迁移的表
-- 执行时间: 在新服务稳定运行后执行
-- 警告: 此操作不可逆，执行前务必确认数据已完整迁移且新服务运行正常！
-- =============================================

-- 注意：执行前请再次备份数据库！

USE chat_book_user;

-- 1. 删除 user_foot 表（可选：先重命名为备份表）
-- RENAME TABLE user_foot TO user_foot_backup_20260313;
DROP TABLE IF EXISTS user_foot;

-- 2. 删除 review 表（可选：先重命名为备份表）
-- RENAME TABLE review TO review_backup_20260313;
DROP TABLE IF EXISTS review;

-- 验证清理结果
SHOW TABLES LIKE '%foot%';
SHOW TABLES LIKE '%review%';
