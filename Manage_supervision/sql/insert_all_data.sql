/*
 旅游管理系统数据插入
 包含：省市区数据、景区数据、酒店数据
 日期: 2024-05-03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空现有数据（如果需要重新导入）
TRUNCATE TABLE `hotels`;
TRUNCATE TABLE `scenic_spots`;
TRUNCATE TABLE `regions`;

-- 导入地区数据
SOURCE regions_data.sql;

-- 导入景区数据
SOURCE scenic_spots_data.sql;

-- 导入酒店数据
SOURCE hotels_data.sql;

SET FOREIGN_KEY_CHECKS = 1;

-- 确认插入的数据条数
SELECT '地区数据' AS 数据类型, COUNT(*) AS 总条数 FROM `regions`;
SELECT '景区数据' AS 数据类型, COUNT(*) AS 总条数 FROM `scenic_spots`;
SELECT '酒店数据' AS 数据类型, COUNT(*) AS 总条数 FROM `hotels`; 