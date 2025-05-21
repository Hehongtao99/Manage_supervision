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
DROP TABLE IF EXISTS advertisement_applications;
DROP TABLE IF EXISTS region;

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

-- 创建省市区区域表
CREATE TABLE region (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    level INT NOT NULL COMMENT '1-省, 2-市, 3-区/县, 4-街道/乡镇',
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_region_parent_id (parent_id),
    INDEX idx_region_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建广告申请表
CREATE TABLE advertisement_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_number VARCHAR(50) NOT NULL UNIQUE,
    area VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL,
    ad_type VARCHAR(100) NOT NULL,
    ad_nature VARCHAR(100) NOT NULL,
    size DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    applicant_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark TEXT,
    province_id BIGINT,
    city_id BIGINT,
    district_id BIGINT,
    street_id BIGINT,
    detailed_address VARCHAR(300),
    CONSTRAINT FK_ad_application_user FOREIGN KEY (applicant_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT FK_ad_application_province FOREIGN KEY (province_id) REFERENCES region (id) ON DELETE SET NULL,
    CONSTRAINT FK_ad_application_city FOREIGN KEY (city_id) REFERENCES region (id) ON DELETE SET NULL,
    CONSTRAINT FK_ad_application_district FOREIGN KEY (district_id) REFERENCES region (id) ON DELETE SET NULL,
    CONSTRAINT FK_ad_application_street FOREIGN KEY (street_id) REFERENCES region (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_conversations_user1_id ON conversations(user1_id);
CREATE INDEX idx_conversations_user2_id ON conversations(user2_id);
CREATE INDEX idx_chat_messages_conversation_id ON chat_messages(conversation_id);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_recipient_id ON chat_messages(recipient_id);
CREATE INDEX idx_teacher_student_teacher_id ON teacher_student_relations(teacher_id);
CREATE INDEX idx_teacher_student_student_id ON teacher_student_relations(student_id);
CREATE INDEX idx_ad_application_status ON advertisement_applications(status);
CREATE INDEX idx_ad_application_applicant ON advertisement_applications(applicant_id);
CREATE INDEX idx_ad_application_province ON advertisement_applications(province_id);
CREATE INDEX idx_ad_application_city ON advertisement_applications(city_id);
CREATE INDEX idx_ad_application_district ON advertisement_applications(district_id);
CREATE INDEX idx_ad_application_street ON advertisement_applications(street_id);

-- 插入基本角色数据
INSERT INTO roles (name, description, permissions, create_time) VALUES 
('ADMIN', '管理员角色，拥有最高权限', ',USER_VIEW,ROLE_VIEW,LOG_VIEW,USER_EDIT,ROLE_EDIT,SYSTEM_SETTINGS,USER_DELETE,ROLE_DELETE,AD_MANAGEMENT,REGION_MANAGEMENT', NOW()),
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
UPDATE roles SET permissions = 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS,AD_MANAGEMENT,REGION_MANAGEMENT' WHERE name = 'ADMIN';
UPDATE roles SET permissions = 'USER_VIEW,USER_EDIT,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW' WHERE name = 'SUPERVISOR';
UPDATE roles SET permissions = 'USER_VIEW' WHERE name = 'USER';

-- 插入一些示例省市区数据
INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(NULL, '北京市', '110000', 1, 1),
(NULL, '上海市', '310000', 1, 2),
(NULL, '广东省', '440000', 1, 3);

-- 北京市下属区域
INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(1, '北京市', '110100', 2, 1);

INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(4, '东城区', '110101', 3, 1),
(4, '西城区', '110102', 3, 2),
(4, '朝阳区', '110105', 3, 3),
(4, '海淀区', '110108', 3, 4);

-- 街道示例
INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(5, '东华门街道', '110101001', 4, 1),
(5, '景山街道', '110101002', 4, 2),
(6, '西长安街街道', '110102001', 4, 1),
(6, '新街口街道', '110102002', 4, 2),
(7, '建外街道', '110105001', 4, 1),
(7, '朝阳门街道', '110105002', 4, 2),
(8, '万寿路街道', '110108001', 4, 1),
(8, '永定路街道', '110108002', 4, 2);

-- 上海市下属区域
INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(2, '上海市', '310100', 2, 1);

INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(13, '黄浦区', '310101', 3, 1),
(13, '徐汇区', '310104', 3, 2),
(13, '长宁区', '310105', 3, 3);

-- 广东省下属区域
INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(3, '广州市', '440100', 2, 1),
(3, '深圳市', '440300', 2, 2);

INSERT INTO region (parent_id, name, code, level, sort_order) VALUES
(17, '越秀区', '440104', 3, 1),
(17, '海珠区', '440105', 3, 2),
(18, '福田区', '440304', 3, 1),
(18, '罗湖区', '440303', 3, 2); 