-- 地区表结构
CREATE TABLE `regions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地区ID',
  `name` varchar(50) NOT NULL COMMENT '地区名称',
  `code` varchar(20) NOT NULL COMMENT '地区编码',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父级地区ID',
  `level` tinyint(1) NOT NULL COMMENT '地区级别：1-省份，2-城市，3-区县',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `status` varchar(10) DEFAULT 'active' COMMENT '状态：active-启用，inactive-停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `region_code` (`code`),
  KEY `parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区表';

-- 初始化部分省市区数据
INSERT INTO `regions` (`name`, `code`, `parent_id`, `level`, `sort`, `status`) VALUES 
-- 省份
('北京市', '110000', NULL, 1, 1, 'active'),
('上海市', '310000', NULL, 1, 2, 'active'),
('广东省', '440000', NULL, 1, 3, 'active'),
('江苏省', '320000', NULL, 1, 4, 'active'),
('浙江省', '330000', NULL, 1, 5, 'active'),
('四川省', '510000', NULL, 1, 6, 'active'),

-- 北京市下属区县
('东城区', '110101', 1, 3, 1, 'active'),
('西城区', '110102', 1, 3, 2, 'active'),
('朝阳区', '110105', 1, 3, 3, 'active'),
('海淀区', '110108', 1, 3, 4, 'active'),
('丰台区', '110106', 1, 3, 5, 'active'),

-- 上海市下属区县
('黄浦区', '310101', 2, 3, 1, 'active'),
('徐汇区', '310104', 2, 3, 2, 'active'),
('长宁区', '310105', 2, 3, 3, 'active'),
('静安区', '310106', 2, 3, 4, 'active'),
('浦东新区', '310115', 2, 3, 5, 'active'),

-- 广东省下属城市
('广州市', '440100', 3, 2, 1, 'active'),
('深圳市', '440300', 3, 2, 2, 'active'),
('珠海市', '440400', 3, 2, 3, 'active'),
('佛山市', '440600', 3, 2, 4, 'active'),

-- 广州市下属区县
('越秀区', '440104', 17, 3, 1, 'active'),
('海珠区', '440105', 17, 3, 2, 'active'),
('荔湾区', '440103', 17, 3, 3, 'active'),
('天河区', '440106', 17, 3, 4, 'active'),
('白云区', '440111', 17, 3, 5, 'active'),

-- 深圳市下属区县
('福田区', '440304', 18, 3, 1, 'active'),
('罗湖区', '440303', 18, 3, 2, 'active'),
('南山区', '440305', 18, 3, 3, 'active'),
('宝安区', '440306', 18, 3, 4, 'active'),
('龙岗区', '440307', 18, 3, 5, 'active'),

-- 江苏省下属城市
('南京市', '320100', 4, 2, 1, 'active'),
('苏州市', '320500', 4, 2, 2, 'active'),
('无锡市', '320200', 4, 2, 3, 'active'),

-- 南京市下属区县
('玄武区', '320102', 31, 3, 1, 'active'),
('秦淮区', '320104', 31, 3, 2, 'active'),
('建邺区', '320105', 31, 3, 3, 'active'),
('鼓楼区', '320106', 31, 3, 4, 'active'),

-- 苏州市下属区县
('姑苏区', '320508', 32, 3, 1, 'active'),
('虎丘区', '320505', 32, 3, 2, 'active'),
('吴中区', '320506', 32, 3, 3, 'active'),
('相城区', '320507', 32, 3, 4, 'active'),

-- 浙江省下属城市
('杭州市', '330100', 5, 2, 1, 'active'),
('宁波市', '330200', 5, 2, 2, 'active'),
('温州市', '330300', 5, 2, 3, 'active'),

-- 杭州市下属区县
('上城区', '330102', 40, 3, 1, 'active'),
('下城区', '330103', 40, 3, 2, 'active'),
('江干区', '330104', 40, 3, 3, 'active'),
('拱墅区', '330105', 40, 3, 4, 'active'),
('西湖区', '330106', 40, 3, 5, 'active'),

-- 四川省下属城市
('成都市', '510100', 6, 2, 1, 'active'),
('绵阳市', '510700', 6, 2, 2, 'active'),
('乐山市', '511100', 6, 2, 3, 'active'),

-- 成都市下属区县
('锦江区', '510104', 46, 3, 1, 'active'),
('青羊区', '510105', 46, 3, 2, 'active'),
('金牛区', '510106', 46, 3, 3, 'active'),
('武侯区', '510107', 46, 3, 4, 'active'),
('成华区', '510108', 46, 3, 5, 'active'); 