-- 好友关系表
CREATE TABLE IF NOT EXISTS `user_friendships` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `friend_id` bigint(20) NOT NULL COMMENT '好友ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`,`friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`),
  CONSTRAINT `fk_friendship_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friendship_friend` FOREIGN KEY (`friend_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 好友请求表
CREATE TABLE IF NOT EXISTS `friend_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '请求ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '发送请求的用户ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '接收请求的用户ID',
  `message` varchar(255) DEFAULT NULL COMMENT '请求消息',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-待处理，1-已接受，2-已拒绝',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`),
  CONSTRAINT `fk_request_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_request_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友请求表'; 