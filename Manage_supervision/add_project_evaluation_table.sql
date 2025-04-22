-- 创建课题评价表
CREATE TABLE IF NOT EXISTS project_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    score INT,
    comment TEXT,
    evaluated_by BIGINT NOT NULL,
    evaluation_time DATETIME,
    CONSTRAINT fk_project_evaluation_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_evaluation_user FOREIGN KEY (evaluated_by) REFERENCES users(id) ON DELETE CASCADE
);

-- 添加索引以提高查询效率
CREATE INDEX idx_project_evaluations_project_id ON project_evaluations(project_id);
CREATE INDEX idx_project_evaluations_evaluated_by ON project_evaluations(evaluated_by); 