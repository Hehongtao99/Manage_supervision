-- 创建景区表
CREATE TABLE IF NOT EXISTS `scenic_spots` (
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
  `status` varchar(10) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-停用',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_province` (`province_id`),
  KEY `idx_city` (`city_id`),
  KEY `idx_district` (`district_id`),
  CONSTRAINT `fk_scenic_spots_province` FOREIGN KEY (`province_id`) REFERENCES `regions` (`id`),
  CONSTRAINT `fk_scenic_spots_city` FOREIGN KEY (`city_id`) REFERENCES `regions` (`id`),
  CONSTRAINT `fk_scenic_spots_district` FOREIGN KEY (`district_id`) REFERENCES `regions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景区表';

-- 初始化部分景区数据
INSERT INTO `scenic_spots` (`name`, `description`, `province_id`, `city_id`, `district_id`, `address`, `level`, `business_hours`, `ticket_price`, `contact_phone`, `status`, `sort`) VALUES 
('故宫博物院', '故宫博物院，旧称为紫禁城，是中国明清两代的皇家宫殿，位于北京中轴线的中心。', 1, NULL, 7, '北京市东城区景山前街4号', '5A', '08:30-17:00', 60.00, '010-85007038', 'ACTIVE', 1),
('八达岭长城', '八达岭长城是中国古代伟大的防御工程万里长城的一部分，位于北京市延庆区。', 1, NULL, 11, '北京市延庆区八达岭特区', '5A', '07:30-17:30', 35.00, '010-69121383', 'ACTIVE', 2),
('西湖', '西湖，位于浙江省杭州市西面，是中国大陆首批国家重点风景名胜区和中国十大风景名胜之一。', 5, 22, 23, '浙江省杭州市西湖区龙井路1号', '5A', '全天开放', 0.00, '0571-87179617', 'ACTIVE', 3),
('上海迪士尼乐园', '上海迪士尼乐园是中国内地首座迪士尼主题乐园，也是世界第六座、亚洲第三座迪士尼主题乐园。', 2, NULL, 17, '上海市浦东新区川沙新镇上海迪士尼度假区', '5A', '09:00-21:00', 399.00, '400-180-0000', 'ACTIVE', 4),
('广州塔', '广州塔，又称广州新电视塔、小蛮腰，是广州的地标性建筑，位于广州市海珠区。', 3, 19, 20, '广州市海珠区阅江西路222号', '5A', '09:30-22:30', 150.00, '020-89338222', 'ACTIVE', 5); 