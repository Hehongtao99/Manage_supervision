-- 修复朋友圈和跑步记录关联的bug
-- 确保朋友圈帖子表包含转发计数字段
ALTER TABLE `social_posts` ADD COLUMN IF NOT EXISTS `forward_count` int(11) NOT NULL DEFAULT '0' COMMENT '转发数量' AFTER `comment_count`;

-- 确保朋友圈帖子表包含转发相关字段
ALTER TABLE `social_posts` ADD COLUMN IF NOT EXISTS `is_forward` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是转发的帖子，0-原创，1-转发' AFTER `forward_count`;
ALTER TABLE `social_posts` ADD COLUMN IF NOT EXISTS `original_post_id` bigint(20) DEFAULT NULL COMMENT '原始帖子ID，如果是原创则为NULL' AFTER `is_forward`;

-- 创建朋友圈转发表（如果不存在）
CREATE TABLE IF NOT EXISTS `social_post_forwards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '转发者用户ID',
  `original_post_id` bigint(20) NOT NULL COMMENT '原始帖子ID',
  `new_post_id` bigint(20) NOT NULL COMMENT '新帖子ID',
  `forward_comment` text COMMENT '转发评论内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_original_post_id` (`original_post_id`),
  KEY `idx_new_post_id` (`new_post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子转发表';

-- 更新现有帖子的转发计数为0（如果字段刚添加）
UPDATE `social_posts` SET `forward_count` = 0 WHERE `forward_count` IS NULL;
UPDATE `social_posts` SET `is_forward` = 0 WHERE `is_forward` IS NULL;

-- 确保朋友圈帖子关联跑步记录表存在
CREATE TABLE IF NOT EXISTS `social_post_running_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `running_record_id` bigint(20) NOT NULL COMMENT '跑步记录ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_id` (`post_id`),
  KEY `idx_running_record_id` (`running_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子关联跑步记录表';

-- 确保朋友圈点赞表存在
CREATE TABLE IF NOT EXISTS `social_post_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子点赞表';

-- 确保朋友圈评论表存在
CREATE TABLE IF NOT EXISTS `social_post_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID，如果是回复则不为空',
  `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子评论表'; 