-- 社交功能数据库更新脚本

-- 1. 添加缺失的字段到 social_posts 表
ALTER TABLE social_posts ADD COLUMN IF NOT EXISTS forward_count int(11) NOT NULL DEFAULT 0 COMMENT '转发数量' AFTER comment_count;
ALTER TABLE social_posts ADD COLUMN IF NOT EXISTS is_forward tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否是转发的帖子，0-原创，1-转发' AFTER forward_count;
ALTER TABLE social_posts ADD COLUMN IF NOT EXISTS original_post_id bigint(20) NOT NULL DEFAULT 0 COMMENT '原始帖子ID，如果是原创则为0' AFTER is_forward;

-- 2. 创建转发表
CREATE TABLE IF NOT EXISTS `social_post_forwards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '转发记录ID',
  `post_id` bigint(20) NOT NULL COMMENT '转发生成的新帖子ID',
  `original_post_id` bigint(20) NOT NULL COMMENT '原始帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '转发用户ID',
  `forward_comment` text COMMENT '转发时的评论',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_original_post_id` (`original_post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子转发表';

-- 3. 创建好友关系表
CREATE TABLE IF NOT EXISTS `friendships` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `status` INT DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_friend_id` (`friend_id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 4. 插入示例朋友圈数据（请根据实际用户ID调整）
INSERT INTO social_posts (user_id, content, visibility, location, like_count, comment_count, create_time, update_time, is_deleted) VALUES 
(1, '今天完成了10公里晨跑，感觉身体状态越来越好了！坚持运动真的能让人充满活力。#晨跑打卡 #健康生活', 1, '中央公园', 15, 3, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 0),
(1, '参加了昨天的半程马拉松，成绩比上次提升了5分钟！感谢一路上为我加油的跑友们，你们的鼓励是我最大的动力！', 1, '城市马拉松赛道', 32, 8, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 0),
(1, '夜跑的感觉真不错，城市的灯光很美，空气也很清新。跑步不仅锻炼身体，更是一种心灵的放松。', 1, '滨江路', 18, 5, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), 0),
(1, '分享一下我的跑步心得：保持适当的配速很重要，不要一开始就用尽全力。循序渐进，享受跑步的过程比追求速度更重要。', 1, NULL, 28, 12, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR), 0),
(1, '雨后的清晨特别适合跑步，空气清新，路面湿润，跑起来特别舒服。今天配速4分30秒，状态不错！', 1, '环湖公园', 22, 6, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR), 0);

-- 5. 插入一些评论数据
INSERT INTO social_post_comments (post_id, user_id, content, parent_id, reply_user_id, like_count, create_time, update_time, is_deleted) VALUES
(1, 1, '坚持就是胜利！我也要开始晨跑了', 0, 0, 2, DATE_SUB(NOW(), INTERVAL 25 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE), 0),
(1, 1, '晨跑确实很棒，空气新鲜，人也少', 0, 0, 1, DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 0),
(2, 1, '恭喜！进步很大啊', 0, 0, 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 0); 