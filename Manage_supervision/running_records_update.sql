-- 更新跑步记录表结构
-- 1. 添加record_datetime字段支持精确到秒的时间
ALTER TABLE running_records 
ADD COLUMN record_datetime DATETIME COMMENT '记录时间（精确到秒）';

-- 2. 更新duration字段注释为秒单位（如果字段已存在，仅更新注释）
ALTER TABLE running_records 
MODIFY COLUMN duration INT COMMENT '跑步时长，单位：秒';

-- 3. 为已有数据设置默认的record_datetime（基于record_date）
UPDATE running_records 
SET record_datetime = DATE(record_date)
WHERE record_datetime IS NULL;

-- 4. 创建索引以提高查询性能
CREATE INDEX idx_running_records_datetime ON running_records(record_datetime);
CREATE INDEX idx_running_records_user_datetime ON running_records(user_id, record_datetime DESC); 