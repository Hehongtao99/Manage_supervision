-- 插入示例朋友圈数据
-- 注意：这些数据假设已经有一些用户存在于users表中

-- 插入朋友圈帖子
INSERT INTO social_posts (user_id, content, visibility, location, like_count, comment_count, create_time, update_time, is_deleted) VALUES 
(1, '今天完成了10公里晨跑，感觉身体状态越来越好了！坚持运动真的能让人充满活力。#晨跑打卡 #健康生活', 1, '中央公园', 15, 3, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 0),
(1, '参加了昨天的半程马拉松，成绩比上次提升了5分钟！感谢一路上为我加油的跑友们，你们的鼓励是我最大的动力！', 1, '城市马拉松赛道', 32, 8, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 0),
(1, '夜跑的感觉真不错，城市的灯光很美，空气也很清新。跑步不仅锻炼身体，更是一种心灵的放松。', 1, '滨江路', 18, 5, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), 0),
(1, '分享一下我的跑步心得：保持适当的配速很重要，不要一开始就用尽全力。循序渐进，享受跑步的过程比追求速度更重要。', 1, NULL, 28, 12, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR), 0),
(1, '雨后的清晨特别适合跑步，空气清新，路面湿润，跑起来特别舒服。今天配速4分30秒，状态不错！', 1, '环湖公园', 22, 6, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR), 0),
(1, '新入手的跑鞋真的很不错，缓震效果明显，跑完10公里脚也不累。装备虽然不是最重要的，但好的装备确实能提升跑步体验。', 1, NULL, 35, 15, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(1, '今天尝试了间歇跑训练，虽然很累但是效果很好。400米快跑+200米慢跑，重复8组，感觉速度有提升！', 1, '体育场', 20, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(1, '周末长距离拉练完成！25公里用时2小时15分钟，为下个月的全马做准备。路上遇到好多跑友，大家互相鼓励的感觉真好！', 1, '马拉松训练基地', 45, 20, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0);

-- 插入一些点赞记录
INSERT INTO social_post_likes (post_id, user_id, create_time) 
SELECT 
    p.id, 
    1,
    DATE_SUB(p.create_time, INTERVAL FLOOR(RAND() * 60) MINUTE)
FROM social_posts p
WHERE p.is_deleted = 0
ORDER BY RAND()
LIMIT 5;

-- 插入一些评论
INSERT INTO social_post_comments (post_id, user_id, content, parent_id, reply_user_id, like_count, create_time, update_time, is_deleted) VALUES
(1, 1, '坚持就是胜利！我也要开始晨跑了', 0, 0, 2, DATE_SUB(NOW(), INTERVAL 25 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE), 0),
(1, 1, '晨跑确实很棒，空气新鲜，人也少', 0, 0, 1, DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 0),
(1, 1, '请问你一般几点起床跑步呢？', 0, 0, 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), 0),
(2, 1, '恭喜！进步很大啊', 0, 0, 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 0),
(2, 1, '半马能跑进2小时已经很厉害了', 0, 0, 2, DATE_SUB(NOW(), INTERVAL 80 MINUTE), DATE_SUB(NOW(), INTERVAL 80 MINUTE), 0);

-- 创建朋友关系表（如果不存在）
CREATE TABLE IF NOT EXISTS friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    status INT DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_friend_id (friend_id),
    UNIQUE KEY uk_user_friend (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 插入一些好友关系（假设有其他用户）
-- 注意：实际使用时需要确保这些用户ID存在
-- INSERT INTO friendships (user_id, friend_id, status) VALUES
-- (1, 2, 1),
-- (2, 1, 1),
-- (1, 3, 1),
-- (3, 1, 1); 