-- 景区表结构
CREATE TABLE `scenic_spots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '景区ID',
  `name` varchar(100) NOT NULL COMMENT '景区名称',
  `description` text COMMENT '景区描述',
  `province_id` bigint(20) NOT NULL COMMENT '省份ID',
  `city_id` bigint(20) COMMENT '城市ID',
  `district_id` bigint(20) COMMENT '区县ID',
  `address` varchar(255) COMMENT '详细地址',
  `level` varchar(20) COMMENT '景区等级，如5A、4A等',
  `business_hours` varchar(100) COMMENT '营业时间',
  `ticket_price` decimal(10,2) COMMENT '门票价格',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `image_url` varchar(255) COMMENT '景区图片URL',
  `status` varchar(10) DEFAULT 'active' COMMENT '状态：active-启用，inactive-停用',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_province` (`province_id`),
  KEY `idx_city` (`city_id`),
  KEY `idx_district` (`district_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景区表';

-- 初始化部分景区数据
INSERT INTO `scenic_spots` (`name`, `description`, `province_id`, `city_id`, `district_id`, `address`, `level`, `business_hours`, `ticket_price`, `contact_phone`, `status`, `sort`) VALUES 
('故宫博物院', '故宫博物院，旧称为紫禁城，是中国明清两代的皇家宫殿，位于北京中轴线的中心。', 1, NULL, 1, '北京市东城区景山前街4号', '5A', '08:30-17:00', 60.00, '010-85007038', 'active', 1),
('八达岭长城', '八达岭长城是中国古代伟大的防御工程万里长城的一部分，位于北京市延庆区。', 1, NULL, 9, '北京市延庆区八达岭特区', '5A', '07:30-17:30', 35.00, '010-69121383', 'active', 2),
('西湖', '西湖，位于浙江省杭州市西面，是中国大陆首批国家重点风景名胜区和中国十大风景名胜之一。', 5, 40, 44, '浙江省杭州市西湖区龙井路1号', '5A', '全天开放', 0.00, '0571-87179617', 'active', 3),
('黄山', '黄山位于安徽省南部黄山市境内，为三山五岳中三山之一，素有"天下第一奇山"之称。', 15, NULL, NULL, '安徽省黄山市黄山区汤口镇', '5A', '07:00-17:00', 230.00, '0559-5580018', 'active', 4),
('张家界国家森林公园', '张家界国家森林公园位于湖南省张家界市武陵源区，是中国第一个国家森林公园。', 18, NULL, NULL, '湖南省张家界市武陵源区', '5A', '07:30-17:30', 248.00, '0744-5566111', 'active', 5),
('桂林山水', '桂林山水位于广西壮族自治区东北部，以山水风光为特色，千百年来享有"桂林山水甲天下"美誉。', 20, NULL, NULL, '广西壮族自治区桂林市', '5A', '全天开放', 0.00, '0773-2823806', 'active', 6),
('天坛公园', '天坛公园是明清两朝帝王祭祀皇天、祈求五谷丰登的场所，位于北京市东城区。', 1, NULL, 1, '北京市东城区天坛路甲1号', '5A', '06:00-22:00', 15.00, '010-67028866', 'active', 7),
('迪士尼乐园', '上海迪士尼乐园是中国内地首座迪士尼主题乐园，也是世界第六座、亚洲第三座。', 2, NULL, 15, '上海市浦东新区川沙新镇上海迪士尼度假区', '5A', '09:00-21:00', 399.00, '400-180-0000', 'active', 8),
('广州塔', '广州塔，又称广州新电视塔、小蛮腰，是广州的地标性建筑，位于广州市海珠区。', 3, 17, 22, '广州市海珠区阅江西路222号', '5A', '09:30-22:30', 150.00, '020-89338222', 'active', 9),
('厦门鼓浪屿', '鼓浪屿位于福建省厦门市，是一个宁静美丽的小岛，被誉为"钢琴之岛"和"万国建筑博览"。', 13, NULL, NULL, '福建省厦门市思明区鼓浪屿', '5A', '全天开放', 0.00, '0592-2060777', 'active', 10); 