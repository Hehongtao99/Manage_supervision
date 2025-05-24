-- 权限系统更新脚本

-- 创建权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码，唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    description VARCHAR(255) COMMENT '权限描述',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID，0表示顶级权限',
    type VARCHAR(50) COMMENT '权限类型：menu-菜单，button-按钮，api-接口',
    path VARCHAR(255) COMMENT '路由路径（前端路由）',
    component VARCHAR(255) COMMENT '组件路径（前端组件）',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    is_visible BOOLEAN DEFAULT TRUE COMMENT '是否可见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_code (code),
    INDEX idx_permission_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 修改角色表，移除权限字段
ALTER TABLE roles DROP COLUMN permissions;

-- 插入基础权限数据
INSERT INTO permissions (code, name, description, parent_id, type, path, component, icon, sort, is_visible) VALUES
-- 系统管理
('system', '系统管理', '系统管理模块', 0, 'menu', '/system', 'Layout', 'setting', 1, TRUE),

-- 用户管理
('user', '用户管理', '用户管理模块', 1, 'menu', '/system/user', 'system/user/index', 'user', 1, TRUE),
('user:view', '查看用户', '查看用户列表和详情', 2, 'button', '', '', '', 1, TRUE),
('user:add', '添加用户', '添加新用户', 2, 'button', '', '', '', 2, TRUE),
('user:edit', '编辑用户', '编辑用户信息', 2, 'button', '', '', '', 3, TRUE),
('user:delete', '删除用户', '删除用户', 2, 'button', '', '', '', 4, TRUE),
('user:reset_password', '重置密码', '重置用户密码', 2, 'button', '', '', '', 5, TRUE),

-- 角色管理
('role', '角色管理', '角色管理模块', 1, 'menu', '/system/role', 'system/role/index', 'peoples', 2, TRUE),
('role:view', '查看角色', '查看角色列表和详情', 8, 'button', '', '', '', 1, TRUE),
('role:add', '添加角色', '添加新角色', 8, 'button', '', '', '', 2, TRUE),
('role:edit', '编辑角色', '编辑角色信息', 8, 'button', '', '', '', 3, TRUE),
('role:delete', '删除角色', '删除角色', 8, 'button', '', '', '', 4, TRUE),

-- 权限管理
('permission', '权限管理', '权限管理模块', 1, 'menu', '/system/permission', 'system/permission/index', 'lock', 3, TRUE),
('permission:view', '查看权限', '查看权限列表和详情', 13, 'button', '', '', '', 1, TRUE),
('permission:add', '添加权限', '添加新权限', 13, 'button', '', '', '', 2, TRUE),
('permission:edit', '编辑权限', '编辑权限信息', 13, 'button', '', '', '', 3, TRUE),
('permission:delete', '删除权限', '删除权限', 13, 'button', '', '', '', 4, TRUE),
('permission:assign', '分配权限', '为角色分配权限', 13, 'button', '', '', '', 5, TRUE),

-- 学生管理
('student', '学生管理', '学生管理模块', 0, 'menu', '/student', 'Layout', 'peoples', 2, TRUE),
('student:view', '查看学生', '查看学生列表和详情', 19, 'button', '', '', '', 1, TRUE),
('student:add', '添加学生', '添加新学生', 19, 'button', '', '', '', 2, TRUE),
('student:edit', '编辑学生', '编辑学生信息', 19, 'button', '', '', '', 3, TRUE),
('student:delete', '删除学生', '删除学生', 19, 'button', '', '', '', 4, TRUE),
('student:assign', '分配导师', '为学生分配导师', 19, 'button', '', '', '', 5, TRUE),

-- 对话管理
('chat', '对话管理', '对话管理模块', 0, 'menu', '/chat', 'Layout', 'message', 3, TRUE),
('chat:send', '发送消息', '发送聊天消息', 25, 'button', '', '', '', 1, TRUE),
('chat:view', '查看消息', '查看聊天消息', 25, 'button', '', '', '', 2, TRUE),
('chat:delete', '删除消息', '删除聊天消息', 25, 'button', '', '', '', 3, TRUE);

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), id FROM permissions;

-- 为普通用户角色分配基本权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'USER'), id FROM permissions
WHERE code IN ('chat', 'chat:send', 'chat:view');

-- 为督导员角色分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'SUPERVISOR'), id FROM permissions
WHERE code IN ('student', 'student:view', 'student:edit', 'chat', 'chat:send', 'chat:view', 'chat:delete'); 