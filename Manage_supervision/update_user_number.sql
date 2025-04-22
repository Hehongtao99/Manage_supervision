-- 为users表添加user_number列（如果不存在）
-- 先检查列是否存在
SET @columnExists = 0;
SELECT COUNT(*) INTO @columnExists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'user_number';

SET @alterTable = IF(@columnExists = 0, 
                    'ALTER TABLE users ADD COLUMN user_number VARCHAR(20) UNIQUE', 
                    'SELECT "Column already exists"');
                    
PREPARE alterStmt FROM @alterTable;
EXECUTE alterStmt;
DEALLOCATE PREPARE alterStmt;

-- 为每个学生用户生成学号
-- 格式：SYYDDDDDD (S + 年份两位 + 6位随机数)
UPDATE users u 
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
SET u.user_number = CONCAT('S', 
                          RIGHT(YEAR(CURRENT_DATE), 2),
                          LPAD(FLOOR(RAND() * 1000000), 6, '0'))
WHERE r.name = 'USER' AND (u.user_number IS NULL OR u.user_number = '');

-- 为每个教师用户生成教师号
-- 格式：TYYDDDDD (T + 年份两位 + 5位随机数)
UPDATE users u 
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
SET u.user_number = CONCAT('T', 
                          RIGHT(YEAR(CURRENT_DATE), 2),
                          LPAD(FLOOR(RAND() * 100000), 5, '0'))
WHERE r.name = 'SUPERVISOR' AND (u.user_number IS NULL OR u.user_number = '');

-- 为其它角色的用户生成默认编号
UPDATE users u
SET u.user_number = CONCAT('A', 
                          RIGHT(YEAR(CURRENT_DATE), 2),
                          LPAD(FLOOR(RAND() * 10000), 4, '0'))
WHERE u.user_number IS NULL OR u.user_number = '';

-- 删除已存在的触发器（如果有）
DROP TRIGGER IF EXISTS before_user_insert;

-- 创建触发器，保证用户编号唯一性
DELIMITER //
CREATE TRIGGER before_user_insert 
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM users WHERE user_number = NEW.user_number;
    
    WHILE cnt > 0 DO
        -- 根据前缀重新生成
        IF LEFT(NEW.user_number, 1) = 'S' THEN
            -- 学生编号格式
            SET NEW.user_number = CONCAT('S', 
                                        RIGHT(YEAR(CURRENT_DATE), 2),
                                        LPAD(FLOOR(RAND() * 1000000), 6, '0'));
        ELSEIF LEFT(NEW.user_number, 1) = 'T' THEN
            -- 教师编号格式
            SET NEW.user_number = CONCAT('T', 
                                        RIGHT(YEAR(CURRENT_DATE), 2),
                                        LPAD(FLOOR(RAND() * 100000), 5, '0'));
        ELSE
            -- 其他编号格式
            SET NEW.user_number = CONCAT('A', 
                                        RIGHT(YEAR(CURRENT_DATE), 2),
                                        LPAD(FLOOR(RAND() * 10000), 4, '0'));
        END IF;
        
        SELECT COUNT(*) INTO cnt FROM users WHERE user_number = NEW.user_number;
    END WHILE;
END;
//
DELIMITER ; 