-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- 订单表索引
CREATE INDEX IF NOT EXISTS idx_orders_student_id ON orders(student_id);
CREATE INDEX IF NOT EXISTS idx_orders_teacher_id ON orders(teacher_id);
CREATE INDEX IF NOT EXISTS idx_orders_course_id ON orders(course_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_create_time ON orders(create_time);

-- 课程申请表索引
CREATE INDEX IF NOT EXISTS idx_course_applications_teacher_id ON course_applications(teacher_id);
CREATE INDEX IF NOT EXISTS idx_course_applications_status ON course_applications(status);
CREATE INDEX IF NOT EXISTS idx_course_applications_create_time ON course_applications(create_time);

-- 根据需要添加更多索引 