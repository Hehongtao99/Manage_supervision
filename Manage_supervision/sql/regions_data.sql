-- 省市区数据插入
-- 省级数据
INSERT INTO `regions` VALUES (1, '北京市', '110000', NULL, 1, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (2, '上海市', '310000', NULL, 1, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (3, '广东省', '440000', NULL, 1, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (4, '江苏省', '320000', NULL, 1, 4, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (5, '浙江省', '330000', NULL, 1, 5, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (6, '四川省', '510000', NULL, 1, 6, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (7, '云南省', '530000', NULL, 1, 7, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (8, '西藏自治区', '540000', NULL, 1, 8, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (9, '海南省', '460000', NULL, 1, 9, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (10, '安徽省', '340000', NULL, 1, 10, 'active', NOW(), NOW());

-- 市级数据
-- 北京市下属区县
INSERT INTO `regions` VALUES (11, '朝阳区', '110105', 1, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (12, '海淀区', '110108', 1, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (13, '东城区', '110101', 1, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (14, '西城区', '110102', 1, 2, 4, 'active', NOW(), NOW());

-- 上海市下属区县
INSERT INTO `regions` VALUES (15, '浦东新区', '310115', 2, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (16, '黄浦区', '310101', 2, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (17, '徐汇区', '310104', 2, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (18, '静安区', '310106', 2, 2, 4, 'active', NOW(), NOW());

-- 广东省下属城市
INSERT INTO `regions` VALUES (19, '广州市', '440100', 3, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (20, '深圳市', '440300', 3, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (21, '珠海市', '440400', 3, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (22, '佛山市', '440600', 3, 2, 4, 'active', NOW(), NOW());

-- 江苏省下属城市
INSERT INTO `regions` VALUES (23, '南京市', '320100', 4, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (24, '苏州市', '320500', 4, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (25, '无锡市', '320200', 4, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (26, '常州市', '320400', 4, 2, 4, 'active', NOW(), NOW());

-- 浙江省下属城市
INSERT INTO `regions` VALUES (27, '杭州市', '330100', 5, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (28, '宁波市', '330200', 5, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (29, '温州市', '330300', 5, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (30, '绍兴市', '330600', 5, 2, 4, 'active', NOW(), NOW());

-- 四川省下属城市
INSERT INTO `regions` VALUES (31, '成都市', '510100', 6, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (32, '绵阳市', '510700', 6, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (33, '乐山市', '511100', 6, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (34, '德阳市', '510600', 6, 2, 4, 'active', NOW(), NOW());

-- 云南省下属城市
INSERT INTO `regions` VALUES (35, '昆明市', '530100', 7, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (36, '大理白族自治州', '532900', 7, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (37, '丽江市', '530700', 7, 2, 3, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (38, '西双版纳傣族自治州', '532800', 7, 2, 4, 'active', NOW(), NOW());

-- 西藏自治区下属城市
INSERT INTO `regions` VALUES (39, '拉萨市', '540100', 8, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (40, '林芝市', '540400', 8, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (41, '日喀则市', '540200', 8, 2, 3, 'active', NOW(), NOW());

-- 海南省下属城市
INSERT INTO `regions` VALUES (42, '海口市', '460100', 9, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (43, '三亚市', '460200', 9, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (44, '三沙市', '460300', 9, 2, 3, 'active', NOW(), NOW());

-- 安徽省下属城市
INSERT INTO `regions` VALUES (45, '合肥市', '340100', 10, 2, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (46, '黄山市', '341000', 10, 2, 2, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (47, '安庆市', '340800', 10, 2, 3, 'active', NOW(), NOW());

-- 区县数据
-- 广州市下属区
INSERT INTO `regions` VALUES (48, '天河区', '440106', 19, 3, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (49, '海珠区', '440105', 19, 3, 2, 'active', NOW(), NOW());

-- 深圳市下属区
INSERT INTO `regions` VALUES (50, '南山区', '440305', 20, 3, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (51, '福田区', '440304', 20, 3, 2, 'active', NOW(), NOW());

-- 杭州市下属区
INSERT INTO `regions` VALUES (52, '西湖区', '330106', 27, 3, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (53, '滨江区', '330108', 27, 3, 2, 'active', NOW(), NOW());

-- 成都市下属区
INSERT INTO `regions` VALUES (54, '锦江区', '510104', 31, 3, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (55, '武侯区', '510107', 31, 3, 2, 'active', NOW(), NOW());

-- 昆明市下属区
INSERT INTO `regions` VALUES (56, '盘龙区', '530103', 35, 3, 1, 'active', NOW(), NOW());
INSERT INTO `regions` VALUES (57, '官渡区', '530111', 35, 3, 2, 'active', NOW(), NOW()); 