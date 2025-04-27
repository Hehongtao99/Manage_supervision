-- 题库表
CREATE TABLE IF NOT EXISTS question_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    creator_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    INDEX idx_question_banks_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 题目表
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    type VARCHAR(20) NOT NULL, -- SINGLE_CHOICE, MULTIPLE_CHOICE, JUDGMENT, ESSAY
    options TEXT, -- JSON格式存储选项
    answer TEXT, -- 标准答案
    analysis TEXT, -- 解析
    difficulty INT NOT NULL DEFAULT 3, -- 1-5，表示难度级别
    creator_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_questions_creator_id (creator_id),
    INDEX idx_questions_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 题库-题目关联表
CREATE TABLE IF NOT EXISTS question_bank_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_bank_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    add_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_qbq_question_bank_id (question_bank_id),
    INDEX idx_qbq_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 更新角色权限
UPDATE roles 
SET permissions = CONCAT(permissions, ',QUESTION_MANAGE,QUESTION_BANK_MANAGE') 
WHERE name = 'SUPERVISOR'; 