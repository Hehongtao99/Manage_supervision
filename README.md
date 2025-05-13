 # 教育督导系统

## 技术栈

### 前端技术栈
- **核心技术**：HTML5, CSS3, JavaScript
- **前端框架**：Vue.js (基于Vue 3 Composition API)
- **UI组件库**：Element Plus
- **路由管理**：Vue Router
- **状态管理**：Pinia
- **HTTP请求**：Axios
- **构建工具**：Vite
- **数据交互**：AJAX技术实现异步数据交互，无需刷新页面即可完成操作

### 后端技术栈
- **编程语言**：Java
- **核心框架**：Spring Boot
- **Web框架**：Spring MVC (用于构建RESTful API)
- **ORM框架**：MyBatis-Plus
- **权限控制**：JWT (JSON Web Token)
- **依赖注入**：Spring IoC容器
- **横切关注点**：Spring AOP

### 数据存储
- **数据库**：MySQL
- **文件存储**：本地文件系统

## 核心功能文件

### 1. 信息注册登录界面
- **前端**：
  - `Manage_supervision/frontend/src/views/Login.vue` - 登录界面
  - `Manage_supervision/frontend/src/views/Register.vue` - 注册界面
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/AuthController.java` - 处理用户认证

### 2. 管理员改变身份
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/AdminController.java` - 管理员功能
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/AdminTeacherStudentController.java` - 管理员控制用户角色和身份

### 3. 学生与老师聊天的后台记录
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/ChatController.java` - 处理聊天信息
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/ChatFileController.java` - 处理聊天文件

### 4. 老师申请课程
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/CourseApplicationController.java` - 处理教师的课程申请

### 5. 学生进行退款流程
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/OrderController.java` - 包含学生申请退款、教师审批退款、申诉相关流程
  - 特别是方法 `refundOrder()`, `appealRefund()` 和相关的教师/管理员响应方法

### 6. 用户个人信息
- **前端**：
  - `Manage_supervision/frontend/src/views/Profile.vue` - 用户信息界面
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/UserController.java` - 通用用户信息处理
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/SupervisorController.java` - 教师信息处理

### 7. 管理员的科目管理
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/SubjectController.java` - 科目增删改查功能

### 8. 收入统计
- **后端**：
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/IncomeReportController.java` - 教师收入报表和统计
  - `Manage_supervision/backend/src/main/java/com/example/auth/controller/DashboardController.java` - 仪表板数据

## 系统特性

- **响应式设计**：适配不同尺寸的设备，提供一致的用户体验
- **前后端分离**：前端负责UI和用户交互，后端处理业务逻辑和数据访问
- **RESTful API**：标准化的接口设计，便于前端调用
- **安全认证**：基于JWT的用户认证和授权
- **性能优化**：分页查询、数据库索引等提升系统性能

## 部署指南

### 开发环境
```bash
# Linux/Mac
./start-dev.sh

# Windows
start-dev.bat
```

### 生产环境
```bash
# Linux/Mac
./start-prod.sh

# Windows
start-prod.bat
```