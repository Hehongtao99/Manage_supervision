package com.example.auth.service.impl;

import com.example.auth.model.dto.ActivityDTO;
import com.example.auth.model.dto.CourseDTO;
import com.example.auth.model.dto.StudentDTO;
import com.example.auth.model.dto.StudentDetailDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.mapper.UserRoleMapper;
import com.example.auth.service.UserService;
import com.example.auth.service.TeacherStudentService;
import com.example.auth.util.PasswordUtils;
import com.example.auth.util.UserNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private UserNumberGenerator userNumberGenerator;
    
    @Autowired
    private TeacherStudentService teacherStudentService;

    @Override
    @Transactional
    public User register(String username, String password) {
        logger.info("尝试注册用户: {}", username);
        
        // 只检查用户名是否已存在
        User existUser = userMapper.findByUsername(username);
        if (existUser != null) {
            logger.warn("注册失败: 用户名 {} 已存在", username);
            throw new RuntimeException("用户名已存在");
        }

        try {
            // 创建新用户
            User user = new User();
            user.setUsername(username);
            // 使用自定义工具类加密密码
            user.setPassword(PasswordUtils.encryptPassword(password));
            
            // 设置默认状态为激活
            user.setStatus("active");
            user.setCreateTime(LocalDateTime.now());
            
            // 生成学生编号(默认注册用户为学生)
            String userNumber = userNumberGenerator.generateStudentNumber();
            user.setUserNumber(userNumber);
            
            // 保存用户基本信息
            userMapper.insert(user);
            
            // 设置默认角色
            Role userRole = roleMapper.findByName("USER");
            if (userRole == null) {
                logger.warn("未找到USER角色，将创建新角色");
                userRole = new Role();
                userRole.setName("USER");
                userRole.setCreateTime(LocalDateTime.now());
                roleMapper.insert(userRole);
            }
            
            // 添加用户-角色关联
            userRoleMapper.insertUserRole(user.getId(), userRole.getId());
            
            logger.info("用户 {} 注册成功，学号: {}", username, userNumber);
            return user;
        } catch (Exception e) {
            logger.error("用户注册过程中发生异常", e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            // 查询用户的角色
            List<Role> roles = roleMapper.findRolesByUserId(user.getId());
            user.setRoles(new HashSet<>(roles));
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            // 查询用户的角色
            List<Role> roles = roleMapper.findRolesByUserId(user.getId());
            user.setRoles(new HashSet<>(roles));
        }
        return user;
    }

    @Override
    public boolean validatePassword(User user, String password) {
        if (user == null || password == null) {
            logger.warn("密码验证失败: 用户或密码为空");
            return false;
        }
        
        // 使用自定义工具类验证密码
        boolean matches = PasswordUtils.matches(password, user.getPassword());
        
        if (matches) {
            // 检查密码是否需要升级到新格式
            if (PasswordUtils.needsUpgrade(user.getPassword())) {
                logger.info("用户 {} 的密码使用旧格式，自动升级到新格式", user.getUsername());
                try {
                    // 使用BCrypt重新加密并保存
                    user.setPassword(PasswordUtils.encryptPassword(password));
                    userMapper.updateById(user);
                    logger.info("用户 {} 的密码已升级到新格式", user.getUsername());
                } catch (Exception e) {
                    // 升级失败仅记录日志，不影响登录结果
                    logger.error("密码格式升级失败", e);
                }
            }
            return true;
        } else {
            logger.warn("密码验证失败: 密码不匹配");
            return false;
        }
    }

    @Override
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        // 验证当前密码
        if (!validatePassword(user, currentPassword)) {
            logger.warn("密码更改失败: 当前密码错误");
            throw new RuntimeException("当前密码错误");
        }

        // 新密码验证
        if (newPassword == null || newPassword.trim().isEmpty()) {
            logger.warn("密码更改失败: 新密码不能为空");
            throw new RuntimeException("新密码不能为空");
        }
        
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            logger.warn("密码更改失败: 新密码长度必须在6-20个字符之间");
            throw new RuntimeException("新密码长度必须在6-20个字符之间");
        }

        try {
            // 使用自定义工具类加密新密码
            user.setPassword(PasswordUtils.encryptPassword(newPassword));
            userMapper.updateById(user);
            logger.info("用户 {} 密码更改成功", user.getUsername());
        } catch (Exception e) {
            logger.error("密码更改过程中发生异常", e);
            throw new RuntimeException("密码更改失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateAvatar(User user, String avatarUrl) {
        if (user == null) {
            logger.warn("头像更新失败: 用户为空");
            throw new RuntimeException("用户不存在");
        }
        
        try {
            user.setAvatar(avatarUrl);
            userMapper.updateById(user);
            logger.info("用户 {} 头像更新成功", user.getUsername());
        } catch (Exception e) {
            logger.error("头像更新过程中发生异常", e);
            throw new RuntimeException("头像更新失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public User updateProfile(User user, Map<String, String> profileData) {
        if (user == null) {
            logger.warn("个人信息更新失败: 用户为空");
            throw new RuntimeException("用户不存在");
        }
        
        try {
            // 更新个人信息字段
            if (profileData.containsKey("realName")) {
                String realName = profileData.get("realName");
                if (realName != null && realName.length() <= 20) {
                    user.setRealName(realName);
                } else {
                    logger.warn("真实姓名长度不合法，应小于等于20个字符");
                }
            }
            
            if (profileData.containsKey("nickname")) {
                String nickname = profileData.get("nickname");
                if (nickname != null && nickname.length() <= 20) {
                    user.setNickname(nickname);
                } else {
                    logger.warn("昵称长度不合法，应小于等于20个字符");
                }
            }
            
            if (profileData.containsKey("email")) {
                String email = profileData.get("email");
                // 简单的电子邮件格式验证
                if (email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    user.setEmail(email);
                } else {
                    logger.warn("电子邮件格式不合法");
                }
            }
            
            if (profileData.containsKey("phone")) {
                String phone = profileData.get("phone");
                // 简单的手机号格式验证（中国大陆手机号）
                if (phone != null && phone.matches("^1[3-9]\\d{9}$")) {
                    user.setPhone(phone);
                } else {
                    logger.warn("手机号格式不合法");
                }
            }
            
            if (profileData.containsKey("bio")) {
                String bio = profileData.get("bio");
                if (bio != null && bio.length() <= 500) {
                    user.setBio(bio);
                } else {
                    logger.warn("个人简介长度不合法，应小于等于500个字符");
                }
            }
            
            // 保存更新
            userMapper.updateById(user);
            logger.info("用户 {} 个人信息更新成功", user.getUsername());
            return user;
        } catch (Exception e) {
            logger.error("个人信息更新过程中发生异常", e);
            throw new RuntimeException("个人信息更新失败: " + e.getMessage());
        }
    }

    // ==================== 学生管理相关方法实现 ====================

    @Override
    public List<StudentDTO> getAllStudents() {
        logger.info("获取所有学生列表");
        try {
            // 获取USER角色
            Role userRole = roleMapper.findByName("USER");
            if (userRole == null) {
                logger.warn("未找到USER角色");
                return new ArrayList<>();
            }
            
            // 获取所有具有USER角色的用户
            List<User> users = userMapper.findByRoleId(userRole.getId());
            
            // 排除同时有ADMIN或SUPERVISOR角色的用户
            Role adminRole = roleMapper.findByName("ADMIN");
            Role supervisorRole = roleMapper.findByName("SUPERVISOR");
            
            final List<Long> adminUserIds = new ArrayList<>();
            final List<Long> supervisorUserIds = new ArrayList<>();
            
            if (adminRole != null) {
                adminUserIds.addAll(userMapper.findByRoleId(adminRole.getId())
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toList()));
            }
            
            if (supervisorRole != null) {
                supervisorUserIds.addAll(userMapper.findByRoleId(supervisorRole.getId())
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toList()));
            }
            
            return users.stream()
                .filter(user -> !adminUserIds.contains(user.getId()) && !supervisorUserIds.contains(user.getId()))
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取学生列表过程中发生异常", e);
            throw new RuntimeException("获取学生列表失败: " + e.getMessage());
        }
    }

    @Override
    public StudentDetailDTO getStudentDetails(Long id) {
        logger.info("获取学生详情, ID: {}", id);
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                logger.warn("未找到ID为{}的学生", id);
                throw new RuntimeException("学生不存在");
            }
            
            // 检查该用户是否为学生(具有USER角色)
            Role userRole = roleMapper.findByName("USER");
            List<Role> userRoles = roleMapper.findRolesByUserId(id);
            
            boolean isStudent = userRoles.stream()
                    .anyMatch(role -> role.getId().equals(userRole.getId()));
            
            if (!isStudent) {
                logger.warn("ID为{}的用户不是学生", id);
                throw new RuntimeException("指定ID的用户不是学生");
            }
            
            // 转换为StudentDTO
            StudentDTO studentDTO = convertToStudentDTO(user);
            
            // 生成课程和活动数据
            List<CourseDTO> courses = generateCoursesForStudent(user);
            List<ActivityDTO> activities = generateActivitiesForStudent(user);
            
            // 创建并返回StudentDetailDTO
            return StudentDetailDTO.fromStudentDTO(studentDTO, courses, activities);
        } catch (Exception e) {
            logger.error("获取学生详情过程中发生异常", e);
            throw new RuntimeException("获取学生详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateStudentStatus(Long id, String status) {
        logger.info("更新学生状态, ID: {}, 新状态: {}", id, status);
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                logger.warn("未找到ID为{}的学生", id);
                return false;
            }
            
            // 检查该用户是否为学生(具有USER角色)
            Role userRole = roleMapper.findByName("USER");
            List<Role> userRoles = roleMapper.findRolesByUserId(id);
            
            boolean isStudent = userRoles.stream()
                    .anyMatch(role -> role.getId().equals(userRole.getId()));
            
            if (!isStudent) {
                logger.warn("ID为{}的用户不是学生", id);
                return false;
            }
            
            // 验证状态值
            if (!"active".equals(status) && !"inactive".equals(status)) {
                logger.warn("无效的状态值: {}", status);
                return false;
            }
            
            // 更新状态
            user.setStatus(status);
            userMapper.updateById(user);
            logger.info("学生 {} 状态更新为 {}", user.getUsername(), status);
            return true;
        } catch (Exception e) {
            logger.error("更新学生状态过程中发生异常", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteStudent(Long id) {
        logger.info("删除学生, ID: {}", id);
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                logger.warn("未找到ID为{}的学生", id);
                return false;
            }
            
            // 检查该用户是否为学生(具有USER角色)
            Role userRole = roleMapper.findByName("USER");
            List<Role> userRoles = roleMapper.findRolesByUserId(id);
            
            boolean isStudent = userRoles.stream()
                    .anyMatch(role -> role.getId().equals(userRole.getId()));
            
            if (!isStudent) {
                logger.warn("ID为{}的用户不是学生", id);
                return false;
            }
            
            // 删除用户角色关联
            userRoleMapper.deleteUserRoles(id);
            
            // 删除用户
            userMapper.deleteById(id);
            logger.info("学生 {} 已删除", user.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("删除学生过程中发生异常", e);
            return false;
        }
    }

    @Override
    public List<User> getAllSupervisors() {
        Role supervisorRole = roleMapper.findByName("SUPERVISOR");
        if (supervisorRole == null) {
            logger.warn("未找到SUPERVISOR角色");
            return new ArrayList<>();
        }
        
        return userMapper.findByRoleId(supervisorRole.getId());
    }

    @Override
    public List<UserDTO> getStudentsByTeacher(Long teacherId) {
        logger.info("获取教师ID为{}的学生列表", teacherId);
        try {
            return teacherStudentService.getStudentsByTeacher(teacherId);
        } catch (Exception e) {
            logger.error("获取教师学生列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateFaceData(User user, String faceData) {
        if (user == null) {
            logger.warn("人脸数据更新失败: 用户为空");
            return false;
        }
        
        try {
            user.setFaceData(faceData);
            userMapper.updateById(user);
            logger.info("用户 {} 人脸数据更新成功", user.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("人脸数据更新过程中发生异常", e);
            return false;
        }
    }
    
    @Override
    public String getFaceData(Long userId) {
        if (userId == null) {
            logger.warn("获取人脸数据失败: 用户ID为空");
            return null;
        }
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                logger.warn("获取人脸数据失败: 用户不存在, ID: {}", userId);
                return null;
            }
            
            return user.getFaceData();
        } catch (Exception e) {
            logger.error("获取人脸数据过程中发生异常", e);
            return null;
        }
    }
    
    @Override
    public boolean hasFaceData(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return false;
            }
            
            return user.getFaceData() != null && !user.getFaceData().isEmpty();
        } catch (Exception e) {
            logger.error("检查人脸数据过程中发生异常", e);
            return false;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将User实体转换为StudentDTO
     */
    private StudentDTO convertToStudentDTO(User user) {
        StudentDTO dto = new StudentDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName() != null ? user.getRealName() : "");
        dto.setUserNumber(user.getUserNumber() != null ? user.getUserNumber() : "");
        
        // 设置前端期望的字段
        dto.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        dto.setStudentId(user.getUserNumber() != null ? user.getUserNumber() : "");
        
        dto.setEmail(user.getEmail() != null ? user.getEmail() : "");
        dto.setPhone(user.getPhone() != null ? user.getPhone() : "");
        dto.setStatus(user.getStatus() != null ? user.getStatus() : "");
        dto.setCreateTime(formatTime(user.getCreateTime()));
        
        // 随机生成一些统计数据
        Random random = new Random(user.getId() != null ? user.getId() : System.currentTimeMillis());
        dto.setAttendanceRate(85 + random.nextInt(16));  // 85% - 100%
        dto.setHomeworkCompleteRate(80 + random.nextInt(21));  // 80% - 100%
        dto.setAverageScore(70 + random.nextInt(31));  // 70-100
        
        return dto;
    }

    /**
     * 格式化时间为字符串
     */
    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(formatter);
    }

    /**
     * 为学生生成模拟课程数据
     */
    private List<CourseDTO> generateCoursesForStudent(User user) {
        List<CourseDTO> courses = new ArrayList<>();
        
        // 使用用户ID作为随机数种子，使得同一用户每次生成相同课程
        Random random = new Random(user.getId() != null ? user.getId() : 0);
        
        // 课程名称列表
        String[] courseNames = {
            "高等数学", "线性代数", "概率论与数理统计", "大学英语", 
            "计算机基础", "数据结构", "操作系统", "数据库原理",
            "计算机网络", "软件工程", "人工智能导论", "机器学习"
        };
        
        // 教师名称列表
        String[] teacherNames = {"李教授", "王教授", "张教授", "刘教授", "陈教授"};
        
        // 随机生成3-6门课程
        int courseCount = 3 + random.nextInt(4);
        for (int i = 0; i < courseCount; i++) {
            CourseDTO course = new CourseDTO();
            course.setId((long) (i + 1));
            course.setName(courseNames[random.nextInt(courseNames.length)]);
            course.setTeacherName(teacherNames[random.nextInt(teacherNames.length)]);
            course.setCredits(random.nextInt(3) + 2);  // 2-4学分
            course.setScore(70 + random.nextInt(31));  // 70-100分
            
            courses.add(course);
        }
        
        return courses;
    }

    /**
     * 为学生生成模拟活动数据
     */
    private List<ActivityDTO> generateActivitiesForStudent(User user) {
        List<ActivityDTO> activities = new ArrayList<>();
        
        // 使用用户ID作为随机数种子
        Random random = new Random(user.getId() != null ? user.getId() : 0);
        
        // 活动名称列表
        String[] activityNames = {
            "参加数学竞赛", "参与志愿服务", "参加编程比赛", "体育比赛", 
            "校园文化节表演", "学术讲座", "科技创新大赛", "社团活动"
        };
        
        // 生成当前时间前60天内的随机活动
        LocalDateTime now = LocalDateTime.now();
        
        // 随机生成3-8个活动
        int activityCount = 3 + random.nextInt(6);
        for (int i = 0; i < activityCount; i++) {
            ActivityDTO activity = new ActivityDTO();
            activity.setId((long) (i + 1));
            activity.setName(activityNames[random.nextInt(activityNames.length)]);
            
            // 随机日期：过去60天内
            int daysAgo = random.nextInt(60);
            LocalDateTime activityTime = now.minusDays(daysAgo);
            activity.setTime(formatTime(activityTime));
            
            // 随机时长：1-5小时
            activity.setDuration(1 + random.nextInt(5));
            
            // 随机积分：1-10分
            activity.setPoints(1 + random.nextInt(10));
            
            activities.add(activity);
        }
        
        // 按时间排序，最近的活动排在前面
        activities.sort((a1, a2) -> a2.getTime().compareTo(a1.getTime()));
        
        return activities;
    }
}