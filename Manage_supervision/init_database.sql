-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除已有表（如果存在）
DROP TABLE IF EXISTS teacher_student_relations;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS conversations;
DROP TABLE IF EXISTS task_evaluations;
DROP TABLE IF EXISTS task_submissions;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建角色表
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    permissions VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    avatar VARCHAR(255),
    real_name VARCHAR(50),
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    bio TEXT,
    status VARCHAR(20) DEFAULT 'active',
    user_number VARCHAR(20) UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建用户-角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建会话表
CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    unread_count_user1 INT NOT NULL DEFAULT 0,
    unread_count_user2 INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_conversations_user1 FOREIGN KEY (user1_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_conversations_user2 FOREIGN KEY (user2_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建聊天消息表
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    sent_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    file_url VARCHAR(255),
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    file_size BIGINT,
    CONSTRAINT FK_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT FK_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_chat_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建教师-学生关系表
CREATE TABLE teacher_student_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    CONSTRAINT FK_teacher_student_teacher FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_teacher_student_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT UK_teacher_student UNIQUE (teacher_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_conversations_user1_id ON conversations(user1_id);
CREATE INDEX idx_conversations_user2_id ON conversations(user2_id);
CREATE INDEX idx_chat_messages_conversation_id ON chat_messages(conversation_id);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_recipient_id ON chat_messages(recipient_id);
CREATE INDEX idx_teacher_student_teacher_id ON teacher_student_relations(teacher_id);
CREATE INDEX idx_teacher_student_student_id ON teacher_student_relations(student_id);

-- 插入基本角色数据
INSERT INTO roles (name, description, permissions, create_time) VALUES 
('ADMIN', '管理员角色，拥有最高权限', ',USER_VIEW,ROLE_VIEW,LOG_VIEW,USER_EDIT,ROLE_EDIT,SYSTEM_SETTINGS,USER_DELETE,ROLE_DELETE', NOW()),
('USER', '学生角色，基本用户权限', '', NOW()),
('SUPERVISOR', '督导员角色，可以管理学生', ',USER_VIEW,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW', NOW());

-- 插入管理员用户 (username: admin, password: 123456)
-- 密码使用BCrypt加密，这里是"123456"的BCrypt哈希值
INSERT INTO users (username, password, real_name, nickname, email, status, create_time) VALUES 
('admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '系统管理员', 'Admin', 'admin@example.com', 'active', NOW());

-- 为管理员用户分配ADMIN角色
INSERT INTO user_roles (user_id, role_id) SELECT 
(SELECT id FROM users WHERE username = 'admin'), 
(SELECT id FROM roles WHERE name = 'ADMIN');

-- 确保roles表中的权限字段包含正确的权限设置
UPDATE roles SET permissions = 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS' WHERE name = 'ADMIN';
UPDATE roles SET permissions = 'USER_VIEW,USER_EDIT,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW' WHERE name = 'SUPERVISOR';
UPDATE roles SET permissions = 'USER_VIEW' WHERE name = 'USER'; 