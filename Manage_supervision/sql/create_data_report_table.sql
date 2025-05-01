-- 创建数据报告表
CREATE TABLE IF NOT EXISTS `data_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint(20) NOT NULL COMMENT '上报人ID',
  `monitor_data_id` bigint(20) DEFAULT NULL COMMENT '被上报的监控数据ID',
  `title` varchar(100) NOT NULL COMMENT '上报标题',
  `description` text COMMENT '详细描述',
  `severity` int(11) NOT NULL COMMENT '上报级别（1-低，2-中，3-高）',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '上报状态（0-待处理，1-处理中，2-已解决，3-已关闭）',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `resolution` text COMMENT '处理结果描述',
  `report_time` datetime NOT NULL COMMENT '上报时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `closed_at` datetime DEFAULT NULL COMMENT '关闭时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_handler_id` (`handler_id`),
  KEY `idx_monitor_data_id` (`monitor_data_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常数据上报表'; 