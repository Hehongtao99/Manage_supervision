-- 检查表是否存在，如果不存在则创建API接口表
CREATE TABLE IF NOT EXISTS `api_endpoints` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `path` varchar(255) NOT NULL COMMENT 'API路径',
  `method` varchar(10) NOT NULL COMMENT 'HTTP方法',
  `controller_name` varchar(100) DEFAULT NULL COMMENT '控制器名称',
  `method_name` varchar(100) DEFAULT NULL COMMENT '方法名称',
  `request_params` varchar(255) DEFAULT NULL COMMENT '请求参数',
  `description` text DEFAULT NULL COMMENT 'API描述',
  `required_roles` varchar(255) DEFAULT NULL COMMENT '所需角色',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `last_accessed` datetime DEFAULT NULL COMMENT '最后访问时间',
  `access_count` bigint(20) DEFAULT 0 COMMENT '访问次数',
  `average_response_time` bigint(20) DEFAULT 0 COMMENT '平均响应时间(毫秒)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_path_method` (`path`, `method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API接口信息表';

-- 为现有控制器添加API描述注解的示例语句
-- 注意：这只是示例，实际使用需要修改Java代码添加@ApiDescription注解

-- 添加API接口记录的示例（实际上系统会自动扫描生成）
-- 使用REPLACE INTO而不是INSERT INTO，以避免重复键错误
REPLACE INTO `api_endpoints` (`path`, `method`, `controller_name`, `method_name`, `description`, `required_roles`, `created_at`, `access_count`)
VALUES 
('/api/auth/login', 'POST', 'AuthController', 'login', '用户登录接口', 'ANONYMOUS', NOW(), 0),
('/api/auth/register', 'POST', 'AuthController', 'register', '用户注册接口', 'ANONYMOUS', NOW(), 0),
('/api/endpoints', 'GET', 'ApiEndpointController', 'getAllApiEndpoints', '获取所有API接口列表', 'ADMIN,USER', NOW(), 0),
('/api/endpoints/by-controller', 'GET', 'ApiEndpointController', 'getApiEndpointsByController', '按控制器分组获取API接口', 'ADMIN,USER', NOW(), 0),
('/api/endpoints/controllers', 'GET', 'ApiEndpointController', 'getAllControllerNames', '获取所有控制器名称', 'ADMIN,USER', NOW(), 0),
('/api/endpoints/by-controller/{controllerName}', 'GET', 'ApiEndpointController', 'getApiEndpointsByControllerName', '根据控制器名称获取API接口', 'ADMIN,USER', NOW(), 0),
('/api/endpoints/stats', 'GET', 'ApiEndpointController', 'getApiAccessStats', '获取API访问统计信息', 'ADMIN,USER', NOW(), 0); 