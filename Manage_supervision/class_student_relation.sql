-- 创建班级-学生关系表
CREATE TABLE IF NOT EXISTS class_student_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    CONSTRAINT FK_class_student_class FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE,
    CONSTRAINT FK_class_student_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT UK_class_student UNIQUE (class_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_class_student_class_id ON class_student_relations(class_id);
CREATE INDEX idx_class_student_student_id ON class_student_relations(student_id);
CREATE INDEX idx_class_student_status ON class_student_relations(status); 