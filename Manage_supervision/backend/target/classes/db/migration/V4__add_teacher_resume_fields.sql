-- Add teacher resume fields to users table
ALTER TABLE users
ADD COLUMN graduation_school varchar(255) DEFAULT NULL COMMENT '毕业学校',
ADD COLUMN teaching_subjects text DEFAULT NULL COMMENT '主要教授科目'; 