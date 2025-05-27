-- 用户管理功能改进 SQL 记录
-- 本次修改主要涉及前端显示逻辑和后端API，无需修改数据库结构
-- 以下记录相关的数据库字段说明：

-- 1. 确保users表有real_name字段用于显示真实姓名
-- 检查users表结构
-- DESCRIBE users;

-- 2. 如果real_name字段不存在，可以添加：
-- ALTER TABLE users ADD COLUMN real_name VARCHAR(50) COMMENT '真实姓名' AFTER username;

-- 3. 确保用户角色关联表存在，用于删除用户时清理角色关联
-- 检查user_roles表结构
-- DESCRIBE user_roles;

-- 4. 验证现有数据的real_name字段是否有值
-- SELECT id, username, real_name, user_number FROM users WHERE real_name IS NOT NULL OR real_name != '';

-- 注意：本次修改主要是前端显示逻辑的优化：
-- - 用户管理页面的姓名列现在优先显示real_name字段，如果为空则显示username
-- - 添加了删除用户功能，会同时删除用户记录和角色关联记录
-- - 人脸信息管理页面已经正确显示real_name字段 