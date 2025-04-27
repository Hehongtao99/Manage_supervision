-- 考试表
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, ONGOING, FINISHED
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration INT NOT NULL, -- 考试时长（分钟）
    passing_score DECIMAL(5,1) NOT NULL DEFAULT 60, -- 及格分数
    total_score DECIMAL(5,1) NOT NULL DEFAULT 100, -- 总分
    creator_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exams_creator_id (creator_id),
    INDEX idx_exams_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 考试题目关联表
CREATE TABLE IF NOT EXISTS exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_title VARCHAR(255) NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    question_score DECIMAL(5,1) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exam_questions_exam_id (exam_id),
    INDEX idx_exam_questions_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 考试学生关联表
CREATE TABLE IF NOT EXISTS exam_students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED
    start_time DATETIME,
    submit_time DATETIME,
    score DECIMAL(5,1),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exam_students_exam_id (exam_id),
    INDEX idx_exam_students_student_id (student_id),
    INDEX idx_exam_students_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 考试答卷表
CREATE TABLE IF NOT EXISTS exam_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer TEXT,
    is_correct BOOLEAN,
    score DECIMAL(5,1),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exam_answers_exam_id (exam_id),
    INDEX idx_exam_answers_student_id (student_id),
    INDEX idx_exam_answers_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 更新角色权限
UPDATE roles 
SET permissions = CONCAT(permissions, ',EXAM_MANAGE') 
WHERE name = 'SUPERVISOR'; 