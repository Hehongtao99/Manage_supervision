-- 向tasks表添加project_id和completed字段
ALTER TABLE tasks 
ADD COLUMN project_id BIGINT,
ADD COLUMN completed BOOLEAN DEFAULT FALSE NOT NULL;

-- 创建外键约束
ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_projects
FOREIGN KEY (project_id) REFERENCES projects(id);

-- 创建任务提交表
CREATE TABLE task_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    file_type VARCHAR(50),
    file_size BIGINT,
    submitter_id BIGINT NOT NULL,
    submission_time DATETIME NOT NULL,
    comment TEXT,
    CONSTRAINT fk_submissions_tasks FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_submissions_users FOREIGN KEY (submitter_id) REFERENCES users(id)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_completed ON tasks(completed);
CREATE INDEX idx_submissions_task_id ON task_submissions(task_id);
CREATE INDEX idx_submissions_submitter_id ON task_submissions(submitter_id); 