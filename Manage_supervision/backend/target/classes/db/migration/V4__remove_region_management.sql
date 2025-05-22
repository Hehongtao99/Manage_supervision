-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 修改广告申请表，移除与区域表相关的字段
ALTER TABLE advertisement_applications DROP FOREIGN KEY FK_ad_application_province;
ALTER TABLE advertisement_applications DROP FOREIGN KEY FK_ad_application_city;
ALTER TABLE advertisement_applications DROP FOREIGN KEY FK_ad_application_district;
ALTER TABLE advertisement_applications DROP FOREIGN KEY FK_ad_application_street;

ALTER TABLE advertisement_applications DROP COLUMN province_id;
ALTER TABLE advertisement_applications DROP COLUMN city_id;
ALTER TABLE advertisement_applications DROP COLUMN district_id;
ALTER TABLE advertisement_applications DROP COLUMN street_id;

-- 删除相关索引
ALTER TABLE advertisement_applications DROP INDEX idx_ad_application_province;
ALTER TABLE advertisement_applications DROP INDEX idx_ad_application_city;
ALTER TABLE advertisement_applications DROP INDEX idx_ad_application_district;
ALTER TABLE advertisement_applications DROP INDEX idx_ad_application_street;

-- 添加经纬度字段到广告申请表（如果没有这些字段）
ALTER TABLE advertisement_applications ADD COLUMN IF NOT EXISTS longitude DOUBLE NULL COMMENT '经度';
ALTER TABLE advertisement_applications ADD COLUMN IF NOT EXISTS latitude DOUBLE NULL COMMENT '纬度';

-- 删除region表
DROP TABLE IF EXISTS region;

-- 更新权限
UPDATE roles SET permissions = REPLACE(permissions, ',REGION_MANAGEMENT', '') WHERE name = 'ADMIN';

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1; 