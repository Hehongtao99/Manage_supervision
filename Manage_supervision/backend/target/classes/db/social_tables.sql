-- 朋友圈帖子表
CREATE TABLE IF NOT EXISTS `social_posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `content` text COMMENT '帖子内容',
  `visibility` tinyint(4) NOT NULL DEFAULT '0' COMMENT '可见范围：0-全部可见，1-仅好友可见',
  `location` varchar(255) DEFAULT NULL COMMENT '位置信息',
  `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `comment_count` int(11) NOT NULL DEFAULT '0' COMMENT '评论数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子表';

-- 朋友圈帖子图片表
CREATE TABLE IF NOT EXISTS `social_post_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `image_url` varchar(255) NOT NULL COMMENT '图片URL',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子图片表';

-- 朋友圈帖子关联跑步记录表
CREATE TABLE IF NOT EXISTS `social_post_running_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `running_record_id` bigint(20) NOT NULL COMMENT '跑步记录ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_id` (`post_id`),
  KEY `idx_running_record_id` (`running_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子关联跑步记录表';

-- 朋友圈帖子评论表
CREATE TABLE IF NOT EXISTS `social_post_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父评论ID，如果是一级评论则为0',
  `reply_user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '被回复的用户ID，如果是一级评论则为0',
  `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子评论表';

-- 朋友圈帖子点赞表
CREATE TABLE IF NOT EXISTS `social_post_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子点赞表'; 