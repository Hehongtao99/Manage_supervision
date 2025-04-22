-- 创建课程表表
CREATE TABLE timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    week_number INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_timetables_class_id (class_id),
    INDEX idx_timetables_week_number (week_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建课程表项目表
CREATE TABLE timetable_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    teacher_id BIGINT,
    course_name VARCHAR(255) NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    period_number INT NOT NULL,
    classroom VARCHAR(100),
    INDEX idx_timetable_items_timetable_id (timetable_id),
    INDEX idx_timetable_items_teacher_id (teacher_id),
    INDEX idx_timetable_items_day_of_week (day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci; 