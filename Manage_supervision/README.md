# 管理督导系统

## 部署指南

### 开发环境
开发环境下使用详细日志，便于调试：

```bash
# Linux/Mac
./start-dev.sh

# Windows
start-dev.bat
```

### 生产环境
生产环境下禁用详细日志，提高性能：

```bash
# Linux/Mac
./start-prod.sh

# Windows
start-prod.bat
```

## 性能优化措施

为提高系统性能，特别是列表查询，本系统采用了以下优化措施：

1. **分页查询**：所有列表查询都支持分页，避免全表扫描
2. **数据库索引**：为常用查询字段添加索引，加速查询
3. **SQL日志控制**：生产环境下关闭SQL日志，减少IO开销
4. **MyBatis-Plus优化**：
   - 生产环境使用NoLoggingImpl
   - 分页查询使用count优化
   - 设置最大单页限制

## 数据库索引

系统为以下表的常查询字段添加了索引：

```sql
-- 用户表索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);

-- 订单表索引
CREATE INDEX idx_orders_student_id ON orders(student_id);
CREATE INDEX idx_orders_teacher_id ON orders(teacher_id);
CREATE INDEX idx_orders_course_id ON orders(course_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_create_time ON orders(create_time);

-- 课程申请表索引
CREATE INDEX idx_course_applications_teacher_id ON course_applications(teacher_id);
CREATE INDEX idx_course_applications_status ON course_applications(status);
CREATE INDEX idx_course_applications_create_time ON course_applications(create_time);
```

索引脚本位于 `backend/src/main/resources/db/index.sql`

## 前端优化

1. 采用分页加载数据，避免一次加载过多数据
2. 实现了本地缓存机制，减少重复请求
3. 使用延迟加载方式处理图片资源 