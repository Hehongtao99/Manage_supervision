-- 创建陪玩服务表
CREATE TABLE IF NOT EXISTS `companion_services` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `companion_id` bigint(20) NOT NULL COMMENT '陪玩ID',
  `title` varchar(100) NOT NULL COMMENT '服务标题',
  `description` text COMMENT '服务描述',
  `game_types` varchar(255) NOT NULL COMMENT '游戏类型，逗号分隔',
  `price` decimal(10,2) NOT NULL COMMENT '价格（元/小时）',
  `service_start_time` time DEFAULT NULL COMMENT '服务开始时间',
  `service_end_time` time DEFAULT NULL COMMENT '服务结束时间',
  `availability` varchar(255) DEFAULT NULL COMMENT '可用性，如：每天、工作日、周末等',
  `status` varchar(20) NOT NULL DEFAULT 'inactive' COMMENT '状态：active-已上线，inactive-已下线',
  `review_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_comment` text COMMENT '审核意见',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_companion_id` (`companion_id`),
  KEY `idx_status` (`status`),
  KEY `idx_review_status` (`review_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='陪玩服务表';

-- 创建索引以提高查询性能
CREATE INDEX idx_companion_services_companion_id ON companion_services(companion_id);
CREATE INDEX idx_companion_services_status ON companion_services(status); 