-- 跑步记录模拟数据
-- 此SQL文件用于生成测试环境下的模拟跑步数据

-- 首先确保用户存在（假设用户ID为1、2、3的用户已存在）
-- 如果需要，请根据实际情况修改用户ID

-- 为用户ID=1的学生添加跑步记录（过去3个月的数据）
INSERT INTO running_records (user_id, distance, duration, pace, record_date, create_time, update_time) VALUES
-- 本周数据
(1, 5.23, 35, '6:42', DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), NOW(), NOW()),
(1, 3.11, 19, '6:07', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), NOW(), NOW()),
(1, 8.45, 52, '6:09', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), NOW(), NOW()),

-- 上周数据
(1, 4.78, 30, '6:16', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), NOW(), NOW()),
(1, 6.32, 42, '6:39', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), NOW(), NOW()),
(1, 5.91, 37, '6:15', DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), NOW(), NOW()),

-- 前两周数据
(1, 7.14, 46, '6:26', DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), NOW(), NOW()),
(1, 3.56, 21, '5:53', DATE_SUB(CURRENT_DATE, INTERVAL 16 DAY), NOW(), NOW()),
(1, 5.02, 33, '6:34', DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY), NOW(), NOW()),

-- 前三周数据
(1, 6.75, 42, '6:13', DATE_SUB(CURRENT_DATE, INTERVAL 21 DAY), NOW(), NOW()),
(1, 4.33, 28, '6:28', DATE_SUB(CURRENT_DATE, INTERVAL 23 DAY), NOW(), NOW()),
(1, 5.67, 36, '6:21', DATE_SUB(CURRENT_DATE, INTERVAL 25 DAY), NOW(), NOW()),

-- 上个月数据
(1, 4.21, 26, '6:10', DATE_SUB(CURRENT_DATE, INTERVAL 32 DAY), NOW(), NOW()),
(1, 7.88, 51, '6:29', DATE_SUB(CURRENT_DATE, INTERVAL 35 DAY), NOW(), NOW()),
(1, 5.46, 35, '6:24', DATE_SUB(CURRENT_DATE, INTERVAL 38 DAY), NOW(), NOW()),
(1, 6.33, 41, '6:29', DATE_SUB(CURRENT_DATE, INTERVAL 40 DAY), NOW(), NOW()),
(1, 3.22, 21, '6:31', DATE_SUB(CURRENT_DATE, INTERVAL 42 DAY), NOW(), NOW()),
(1, 8.10, 53, '6:33', DATE_SUB(CURRENT_DATE, INTERVAL 45 DAY), NOW(), NOW()),

-- 两个月前数据
(1, 5.76, 38, '6:36', DATE_SUB(CURRENT_DATE, INTERVAL 52 DAY), NOW(), NOW()),
(1, 4.89, 32, '6:33', DATE_SUB(CURRENT_DATE, INTERVAL 55 DAY), NOW(), NOW()),
(1, 7.21, 44, '6:06', DATE_SUB(CURRENT_DATE, INTERVAL 58 DAY), NOW(), NOW()),
(1, 3.45, 23, '6:40', DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY), NOW(), NOW()),
(1, 6.78, 43, '6:20', DATE_SUB(CURRENT_DATE, INTERVAL 62 DAY), NOW(), NOW()),
(1, 5.12, 33, '6:26', DATE_SUB(CURRENT_DATE, INTERVAL 65 DAY), NOW(), NOW()),

-- 三个月前数据
(1, 4.56, 29, '6:22', DATE_SUB(CURRENT_DATE, INTERVAL 72 DAY), NOW(), NOW()),
(1, 7.33, 47, '6:25', DATE_SUB(CURRENT_DATE, INTERVAL 75 DAY), NOW(), NOW()),
(1, 5.89, 36, '6:07', DATE_SUB(CURRENT_DATE, INTERVAL 78 DAY), NOW(), NOW()),
(1, 6.44, 42, '6:31', DATE_SUB(CURRENT_DATE, INTERVAL 80 DAY), NOW(), NOW()),
(1, 3.77, 25, '6:37', DATE_SUB(CURRENT_DATE, INTERVAL 85 DAY), NOW(), NOW()),
(1, 8.22, 50, '6:04', DATE_SUB(CURRENT_DATE, INTERVAL 89 DAY), NOW(), NOW());

-- 为用户ID=2的学生添加跑步记录（过去2个月的数据，配速较快）
INSERT INTO running_records (user_id, distance, duration, pace, record_date, create_time, update_time) VALUES
-- 本周数据
(2, 6.12, 33, '5:23', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), NOW(), NOW()),
(2, 4.33, 23, '5:19', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), NOW(), NOW()),
(2, 9.27, 48, '5:11', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), NOW(), NOW()),

-- 上周数据
(2, 5.46, 28, '5:08', DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), NOW(), NOW()),
(2, 7.85, 41, '5:13', DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), NOW(), NOW()),
(2, 6.33, 34, '5:22', DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), NOW(), NOW()),

-- 前两周数据
(2, 8.75, 46, '5:17', DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), NOW(), NOW()),
(2, 4.22, 22, '5:14', DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY), NOW(), NOW()),
(2, 6.91, 37, '5:21', DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY), NOW(), NOW()),

-- 上个月数据
(2, 5.33, 28, '5:15', DATE_SUB(CURRENT_DATE, INTERVAL 32 DAY), NOW(), NOW()),
(2, 8.45, 44, '5:12', DATE_SUB(CURRENT_DATE, INTERVAL 35 DAY), NOW(), NOW()),
(2, 6.77, 35, '5:10', DATE_SUB(CURRENT_DATE, INTERVAL 38 DAY), NOW(), NOW()),
(2, 7.12, 37, '5:12', DATE_SUB(CURRENT_DATE, INTERVAL 41 DAY), NOW(), NOW()),
(2, 3.56, 19, '5:20', DATE_SUB(CURRENT_DATE, INTERVAL 44 DAY), NOW(), NOW()),
(2, 10.21, 53, '5:11', DATE_SUB(CURRENT_DATE, INTERVAL 47 DAY), NOW(), NOW()),

-- 两个月前数据
(2, 6.33, 33, '5:13', DATE_SUB(CURRENT_DATE, INTERVAL 52 DAY), NOW(), NOW()),
(2, 5.44, 28, '5:09', DATE_SUB(CURRENT_DATE, INTERVAL 55 DAY), NOW(), NOW()),
(2, 8.90, 47, '5:17', DATE_SUB(CURRENT_DATE, INTERVAL 58 DAY), NOW(), NOW()),
(2, 4.35, 23, '5:18', DATE_SUB(CURRENT_DATE, INTERVAL 61 DAY), NOW(), NOW());

-- 为用户ID=3的学生添加跑步记录（过去1个月的数据，进步明显）
INSERT INTO running_records (user_id, distance, duration, pace, record_date, create_time, update_time) VALUES
-- 本周数据（配速有明显提升）
(3, 4.55, 29, '6:22', DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), NOW(), NOW()),
(3, 3.20, 19, '5:56', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), NOW(), NOW()),
(3, 5.75, 35, '6:05', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), NOW(), NOW()),

-- 上周数据（配速中等）
(3, 4.10, 27, '6:35', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), NOW(), NOW()),
(3, 5.30, 36, '6:48', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), NOW(), NOW()),
(3, 2.85, 20, '7:01', DATE_SUB(CURRENT_DATE, INTERVAL 11 DAY), NOW(), NOW()),

-- 前两周数据（配速较慢）
(3, 3.75, 28, '7:28', DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY), NOW(), NOW()),
(3, 4.55, 35, '7:41', DATE_SUB(CURRENT_DATE, INTERVAL 16 DAY), NOW(), NOW()),
(3, 2.95, 23, '7:48', DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY), NOW(), NOW()),

-- 前三周数据（配速最慢）
(3, 2.35, 19, '8:05', DATE_SUB(CURRENT_DATE, INTERVAL 21 DAY), NOW(), NOW()),
(3, 3.60, 30, '8:20', DATE_SUB(CURRENT_DATE, INTERVAL 23 DAY), NOW(), NOW()),
(3, 2.10, 18, '8:34', DATE_SUB(CURRENT_DATE, INTERVAL 25 DAY), NOW(), NOW()),
(3, 4.25, 37, '8:42', DATE_SUB(CURRENT_DATE, INTERVAL 28 DAY), NOW(), NOW());

-- 添加跑步偏好数据
INSERT INTO running_preferences (user_id, frequency, preferred_distance, pace, environment, motto, create_time, update_time) VALUES
(1, '每周3-5次', '5-10公里', '6:00-7:00分钟/公里', '公园跑道', '坚持就是胜利', NOW(), NOW()),
(2, '每周5-6次', '8-12公里', '5:00-5:30分钟/公里', '城市道路', '更快，更强', NOW(), NOW()),
(3, '每周2-3次', '3-5公里', '6:00-7:00分钟/公里', '小区步道', '慢慢来，比较快', NOW(), NOW());

-- 添加跑步相关的朋友圈帖子
INSERT INTO social_posts (id, user_id, content, visibility, create_time, update_time) VALUES
(101, 1, '今天的跑步感觉非常好！天气也很舒适，希望能坚持下去。', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(102, 2, '新的跑鞋到了，今天第一次穿着它跑步，配速提高了不少！', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(103, 3, '每天坚持跑步一个月了，感觉体力有明显提升，开心！', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(104, 1, '今天突破了自己的记录，第一次跑到8公里，虽然很累但很满足！', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 关联跑步记录和帖子
INSERT INTO social_post_running_records (post_id, running_record_id, create_time) VALUES
(101, (SELECT id FROM running_records WHERE user_id = 1 ORDER BY record_date DESC LIMIT 1), NOW()),
(102, (SELECT id FROM running_records WHERE user_id = 2 ORDER BY record_date DESC LIMIT 1), NOW()),
(103, (SELECT id FROM running_records WHERE user_id = 3 ORDER BY record_date DESC LIMIT 1), NOW()),
(104, (SELECT id FROM running_records WHERE user_id = 1 AND distance > 8 LIMIT 1), NOW());

-- 添加一些点赞数据
INSERT INTO social_post_likes (post_id, user_id, create_time, update_time) VALUES
(101, 2, NOW(), NOW()),
(101, 3, NOW(), NOW()),
(102, 1, NOW(), NOW()),
(102, 3, NOW(), NOW()),
(103, 1, NOW(), NOW()),
(104, 2, NOW(), NOW()),
(104, 3, NOW(), NOW());

-- 添加一些评论数据
INSERT INTO social_post_comments (post_id, user_id, content, create_time, update_time) VALUES
(101, 2, '加油！跑步是最好的运动', NOW(), NOW()),
(101, 3, '天气好确实适合跑步，我也去试试', NOW(), NOW()),
(102, 1, '什么牌子的跑鞋？推荐一下', NOW(), NOW()),
(102, 3, '配速提升好多啊，厉害！', NOW(), NOW()),
(103, 1, '坚持就是胜利，继续加油！', NOW(), NOW()),
(104, 2, '太棒了！突破自我', NOW(), NOW()),
(104, 3, '8公里！我的目标', NOW(), NOW()); 