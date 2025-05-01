-- 创建主机表
CREATE TABLE IF NOT EXISTS hosts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostname VARCHAR(100) NOT NULL COMMENT '主机名',
    ip VARCHAR(50) NOT NULL COMMENT 'IP地址',
    os VARCHAR(100) NOT NULL COMMENT '操作系统',
    cpu_model VARCHAR(255) NOT NULL COMMENT 'CPU型号',
    cpu_cores INT NOT NULL COMMENT 'CPU核心数',
    memory_total BIGINT NOT NULL COMMENT '内存总量(MB)',
    disk_total BIGINT NOT NULL COMMENT '磁盘总量(MB)',
    status VARCHAR(20) NOT NULL COMMENT '状态(online, offline, warning, error)',
    last_update_time DATETIME NOT NULL COMMENT '最后更新时间',
    description VARCHAR(1000) COMMENT '描述',
    user_id BIGINT COMMENT '关联用户ID',
    UNIQUE KEY uk_hostname (hostname),
    UNIQUE KEY uk_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主机信息表';

-- 创建主机监控数据表
CREATE TABLE IF NOT EXISTS host_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id BIGINT NOT NULL COMMENT '主机ID',
    cpu_usage DOUBLE NOT NULL COMMENT 'CPU使用率(%)',
    memory_usage DOUBLE NOT NULL COMMENT '内存使用率(%)',
    memory_used BIGINT NOT NULL COMMENT '已用内存(MB)',
    disk_usage DOUBLE NOT NULL COMMENT '磁盘使用率(%)',
    disk_used BIGINT NOT NULL COMMENT '已用磁盘空间(MB)',
    network_in BIGINT NOT NULL COMMENT '入站流量(KB/s)',
    network_out BIGINT NOT NULL COMMENT '出站流量(KB/s)',
    collection_time DATETIME NOT NULL COMMENT '采集时间',
    INDEX idx_host_id (host_id),
    INDEX idx_collection_time (collection_time),
    CONSTRAINT fk_host_metrics_host_id FOREIGN KEY (host_id) REFERENCES hosts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主机监控数据表';

-- 插入测试数据
INSERT INTO hosts (hostname, ip, os, cpu_model, cpu_cores, memory_total, disk_total, status, last_update_time, description)
VALUES
('server-01', '192.168.1.101', 'CentOS 7.9', 'Intel Xeon E5-2680 v3', 12, 32768, 1048576, 'online', NOW(), '应用服务器1'),
('server-02', '192.168.1.102', 'Ubuntu 20.04 LTS', 'AMD EPYC 7302', 16, 65536, 2097152, 'warning', NOW(), '应用服务器2'),
('server-03', '192.168.1.103', 'Debian 11', 'Intel Xeon Gold 6230', 20, 131072, 4194304, 'offline', NOW(), '数据库服务器'),
('server-04', '192.168.1.104', 'CentOS 8', 'Intel Xeon E5-2690 v4', 14, 49152, 3145728, 'online', NOW(), '文件服务器'),
('server-05', '192.168.1.105', 'Ubuntu 18.04 LTS', 'AMD EPYC 7282', 16, 32768, 1048576, 'error', NOW(), '备份服务器');

-- 为每个主机生成一些历史监控数据（最近10分钟）
-- server-01 (正常在线状态)
INSERT INTO host_metrics (host_id, cpu_usage, memory_usage, memory_used, disk_usage, disk_used, network_in, network_out, collection_time)
VALUES
(1, 23, 40, 13107, 30, 314572, 120, 110, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(1, 25, 42, 13763, 30, 314572, 132, 122, DATE_SUB(NOW(), INTERVAL 9 MINUTE)),
(1, 28, 45, 14745, 30, 314572, 101, 91, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(1, 22, 48, 15728, 30, 314572, 134, 124, DATE_SUB(NOW(), INTERVAL 7 MINUTE)),
(1, 26, 50, 16384, 30, 314572, 90, 80, DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
(1, 30, 52, 17039, 30, 314572, 230, 210, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(1, 35, 55, 18022, 30, 314572, 210, 190, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(1, 43, 58, 19004, 30, 314572, 120, 110, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(1, 36, 56, 18350, 30, 314572, 132, 122, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(1, 30, 54, 17694, 30, 314572, 101, 91, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- server-02 (警告状态 - CPU使用率高)
INSERT INTO host_metrics (host_id, cpu_usage, memory_usage, memory_used, disk_usage, disk_used, network_in, network_out, collection_time)
VALUES
(2, 56, 60, 39321, 40, 838860, 320, 280, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(2, 60, 62, 40632, 40, 838860, 332, 312, DATE_SUB(NOW(), INTERVAL 9 MINUTE)),
(2, 65, 65, 42598, 40, 838860, 301, 291, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(2, 70, 70, 45875, 40, 838860, 334, 324, DATE_SUB(NOW(), INTERVAL 7 MINUTE)),
(2, 75, 75, 49152, 40, 838860, 390, 350, DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
(2, 80, 80, 52428, 40, 838860, 430, 410, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(2, 85, 85, 55705, 40, 838860, 410, 390, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(2, 82, 82, 53739, 40, 838860, 320, 310, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(2, 78, 80, 52428, 40, 838860, 332, 322, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(2, 75, 75, 49152, 40, 838860, 301, 291, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- server-03 (离线状态)
INSERT INTO host_metrics (host_id, cpu_usage, memory_usage, memory_used, disk_usage, disk_used, network_in, network_out, collection_time)
VALUES
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 9 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 7 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(3, 0, 0, 0, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- server-04 (正常在线状态)
INSERT INTO host_metrics (host_id, cpu_usage, memory_usage, memory_used, disk_usage, disk_used, network_in, network_out, collection_time)
VALUES
(4, 25, 45, 22118, 20, 629145, 220, 130, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(4, 28, 48, 23592, 20, 629145, 232, 142, DATE_SUB(NOW(), INTERVAL 9 MINUTE)),
(4, 30, 50, 24576, 20, 629145, 201, 121, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(4, 32, 52, 25559, 20, 629145, 234, 154, DATE_SUB(NOW(), INTERVAL 7 MINUTE)),
(4, 28, 55, 27033, 20, 629145, 190, 110, DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
(4, 25, 53, 26049, 20, 629145, 180, 100, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(4, 22, 51, 25066, 20, 629145, 170, 90, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(4, 20, 49, 24084, 20, 629145, 160, 80, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(4, 19, 47, 23101, 20, 629145, 150, 70, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(4, 20, 45, 22118, 20, 629145, 140, 60, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- server-05 (错误状态 - CPU和内存使用率极高)
INSERT INTO host_metrics (host_id, cpu_usage, memory_usage, memory_used, disk_usage, disk_used, network_in, network_out, collection_time)
VALUES
(5, 95, 92, 30146, 90, 943718, 50, 30, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(5, 96, 93, 30474, 90, 943718, 52, 31, DATE_SUB(NOW(), INTERVAL 9 MINUTE)),
(5, 98, 95, 31129, 90, 943718, 51, 32, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(5, 99, 96, 31457, 90, 943718, 53, 30, DATE_SUB(NOW(), INTERVAL 7 MINUTE)),
(5, 97, 98, 32112, 90, 943718, 52, 29, DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
(5, 96, 97, 31785, 90, 943718, 50, 28, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(5, 95, 96, 31457, 90, 943718, 49, 27, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(5, 96, 95, 31129, 90, 943718, 48, 26, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(5, 97, 96, 31457, 90, 943718, 47, 25, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(5, 98, 97, 31785, 90, 943718, 46, 24, DATE_SUB(NOW(), INTERVAL 1 MINUTE)); 