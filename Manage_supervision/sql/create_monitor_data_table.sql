-- 创建监控数据表
CREATE TABLE IF NOT EXISTS `monitor_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '关联的用户ID',
  `data_type` varchar(50) NOT NULL COMMENT '数据类型（如CPU、内存、网络等）',
  `value` double NOT NULL COMMENT '数据值',
  `expected_min` double DEFAULT NULL COMMENT '预期最小值',
  `expected_max` double DEFAULT NULL COMMENT '预期最大值',
  `deviation` double DEFAULT NULL COMMENT '偏差率（百分比）',
  `is_anomaly` tinyint(1) DEFAULT '0' COMMENT '是否为异常数据',
  `resolved` tinyint(1) DEFAULT '0' COMMENT '是否已处理',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `description` varchar(500) DEFAULT NULL COMMENT '描述或备注',
  `source` varchar(100) DEFAULT NULL COMMENT '数据来源',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_time` (`record_time`),
  KEY `idx_is_anomaly` (`is_anomaly`),
  KEY `idx_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控数据表';

-- 插入一些示例数据用于演示
INSERT INTO `monitor_data` (`user_id`, `data_type`, `value`, `expected_min`, `expected_max`, `deviation`, `is_anomaly`, `resolved`, `record_time`, `created_at`, `description`, `source`)
VALUES
  -- 正常数据
  (1, 'CPU使用率', 45.2, 0, 80, 12.5, 0, 0, DATE_SUB(NOW(), INTERVAL 48 HOUR), NOW(), 'CPU使用正常', '系统监控'),
  (1, 'CPU使用率', 52.1, 0, 80, 15.3, 0, 0, DATE_SUB(NOW(), INTERVAL 36 HOUR), NOW(), 'CPU使用正常', '系统监控'),
  (1, '内存使用率', 62.4, 0, 90, 10.2, 0, 0, DATE_SUB(NOW(), INTERVAL 48 HOUR), NOW(), '内存使用正常', '系统监控'),
  (1, '内存使用率', 68.7, 0, 90, 12.6, 0, 0, DATE_SUB(NOW(), INTERVAL 36 HOUR), NOW(), '内存使用正常', '系统监控'),
  (1, '磁盘使用率', 55.8, 0, 95, 5.2, 0, 0, DATE_SUB(NOW(), INTERVAL 48 HOUR), NOW(), '磁盘使用正常', '系统监控'),
  (1, '磁盘使用率', 57.3, 0, 95, 6.1, 0, 0, DATE_SUB(NOW(), INTERVAL 36 HOUR), NOW(), '磁盘使用正常', '系统监控'),
  (1, '网络流量', 45.2, 0, 100, 9.8, 0, 0, DATE_SUB(NOW(), INTERVAL 48 HOUR), NOW(), '网络流量正常', '系统监控'),
  (1, '网络流量', 62.1, 0, 100, 12.4, 0, 0, DATE_SUB(NOW(), INTERVAL 36 HOUR), NOW(), '网络流量正常', '系统监控'),
  
  -- 异常数据
  (1, 'CPU使用率', 95.7, 0, 80, 45.2, 1, 1, DATE_SUB(NOW(), INTERVAL 24 HOUR), NOW(), 'CPU使用率异常高，已处理', '系统监控'),
  (1, 'CPU使用率', 92.3, 0, 80, 42.7, 1, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), 'CPU使用率异常高，需处理', '系统监控'),
  (1, '内存使用率', 98.1, 0, 90, 36.9, 1, 1, DATE_SUB(NOW(), INTERVAL 24 HOUR), NOW(), '内存使用率异常高，已处理', '系统监控'),
  (1, '内存使用率', 95.6, 0, 90, 33.2, 1, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '内存使用率异常高，需处理', '系统监控'),
  (1, '磁盘使用率', 97.2, 0, 95, 22.3, 1, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '磁盘使用率异常高，需处理', '系统监控'),
  (1, '网络流量', 120.5, 0, 100, 40.5, 1, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '网络流量异常高，需处理', '系统监控'),
  
  -- 最近的正常数据
  (1, 'CPU使用率', 48.6, 0, 80, 10.8, 0, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW(), 'CPU使用正常', '系统监控'),
  (1, '内存使用率', 65.2, 0, 90, 8.9, 0, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW(), '内存使用正常', '系统监控'),
  (1, '磁盘使用率', 60.1, 0, 95, 7.4, 0, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW(), '磁盘使用正常', '系统监控'),
  (1, '网络流量', 58.3, 0, 100, 8.3, 0, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW(), '网络流量正常', '系统监控');
  
-- 为更多用户添加示例数据
INSERT INTO `monitor_data` (`user_id`, `data_type`, `value`, `expected_min`, `expected_max`, `deviation`, `is_anomaly`, `resolved`, `record_time`, `created_at`, `description`, `source`)
VALUES
  -- 用户2的数据
  (2, 'CPU使用率', 42.1, 0, 80, 9.5, 0, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), 'CPU使用正常', '系统监控'),
  (2, '内存使用率', 88.5, 0, 90, 22.8, 0, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '内存使用接近上限', '系统监控'),
  (2, '磁盘使用率', 96.2, 0, 95, 24.2, 1, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '磁盘使用率异常高，需处理', '系统监控'),
  
  -- 用户3的数据
  (3, 'CPU使用率', 38.6, 0, 80, 3.5, 0, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), 'CPU使用正常', '系统监控'),
  (3, '内存使用率', 72.3, 0, 90, 10.3, 0, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '内存使用正常', '系统监控'),
  (3, '磁盘使用率', 91.8, 0, 95, 18.8, 0, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW(), '磁盘使用接近上限', '系统监控'); 