-- 修复advertisement_applications表结构
-- 添加缺失的经纬度和地区ID字段

-- 添加经度字段
ALTER TABLE advertisement_applications 
ADD COLUMN longitude DOUBLE NULL COMMENT '经度';

-- 添加纬度字段
ALTER TABLE advertisement_applications 
ADD COLUMN latitude DOUBLE NULL COMMENT '纬度';

-- 添加省份ID字段
ALTER TABLE advertisement_applications 
ADD COLUMN province_id BIGINT NULL COMMENT '省份ID';

-- 添加城市ID字段
ALTER TABLE advertisement_applications 
ADD COLUMN city_id BIGINT NULL COMMENT '城市ID';

-- 添加区县ID字段
ALTER TABLE advertisement_applications 
ADD COLUMN district_id BIGINT NULL COMMENT '区县ID';

-- 添加街道ID字段
ALTER TABLE advertisement_applications 
ADD COLUMN street_id BIGINT NULL COMMENT '街道ID'; 