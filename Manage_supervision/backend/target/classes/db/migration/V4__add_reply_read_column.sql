-- 添加回复已读字段到家长教师留言表
ALTER TABLE parent_teacher_messages
ADD COLUMN reply_read tinyint(1) NOT NULL DEFAULT 0 COMMENT '回复是否已读，0未读，1已读';

-- 更新所有现有的记录，默认设置为未读
UPDATE parent_teacher_messages
SET reply_read = 0
WHERE 1=1; 