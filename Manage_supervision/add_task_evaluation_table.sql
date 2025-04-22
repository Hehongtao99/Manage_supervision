-- 创建任务评价表
CREATE TABLE IF NOT EXISTS task_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    score INT,
    comment TEXT,
    evaluated_by BIGINT NOT NULL,
    evaluation_time DATETIME,
    CONSTRAINT fk_task_evaluation_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_evaluation_user FOREIGN KEY (evaluated_by) REFERENCES users(id) ON DELETE CASCADE
);

-- 添加索引以提高查询效率
CREATE INDEX idx_task_evaluations_task_id ON task_evaluations(task_id);
CREATE INDEX idx_task_evaluations_evaluated_by ON task_evaluations(evaluated_by); 