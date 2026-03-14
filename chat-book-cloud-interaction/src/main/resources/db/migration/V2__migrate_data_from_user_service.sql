-- =============================================
-- 数据迁移脚本：从 user 数据库迁移到 interaction 数据库
-- 执行时间: 在新服务上线前执行
-- 说明: 将现有数据从 chat_book_user 迁移到 chat_book_interaction
-- =============================================

-- 注意：执行前请先备份数据库！

-- 1. 迁移 user_foot 表数据
INSERT INTO chat_book_interaction.user_foot
    (id, user_id, document_id, document_type, document_user_id, collection_stat, read_stat, comment_stat, praise_stat, create_time, update_time)
SELECT
    id, user_id, document_id, document_type, document_user_id, collection_stat, read_stat, comment_stat, praise_stat, create_time, update_time
FROM ice_user.user_foot
ON DUPLICATE KEY UPDATE
    update_time = VALUES(update_time);

-- 2. 迁移 review 表数据
INSERT INTO chat_book_interaction.review
    (id, text_id, user_id, parent_id, content, create_time, update_time)
SELECT
    id, text_id, user_id, parent_id, content, create_time, update_time
FROM ice_user.review
ON DUPLICATE KEY UPDATE
    update_time = VALUES(update_time);

-- 3. 验证数据迁移结果
SELECT 'user_foot 迁移完成' AS status,
    (SELECT COUNT(*) FROM ice_user.user_foot) AS source_count,
    (SELECT COUNT(*) FROM chat_book_interaction.user_foot) AS target_count;

SELECT 'review 迁移完成' AS status,
    (SELECT COUNT(*) FROM ice_user.review) AS source_count,
    (SELECT COUNT(*) FROM chat_book_interaction.review) AS target_count;
