package com.example.auth.service.impl;

import com.example.auth.dto.ActivityDTO;
import com.example.auth.dto.CourseDTO;
import com.example.auth.dto.StudentDTO;
import com.example.auth.dto.StudentDetailDTO;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.UserService;
import com.example.auth.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User register(String username, String password) {
        logger.info("尝试注册用户: {}", username);
        
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            logger.warn("注册失败: 用户名不能为空");
            throw new RuntimeException("用户名不能为空");
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("注册失败: 密码不能为空");
            throw new RuntimeException("密码不能为空");
        }
        
        // 用户名长度验证
        if (username.length() < 3 || username.length() > 20) {
            logger.warn("注册失败: 用户名长度必须在3-20个字符之间");
            throw new RuntimeException("用户名长度必须在3-20个字符之间");
        }
        
        // 密码长度验证
        if (password.length() < 6 || password.length() > 20) {
            logger.warn("注册失败: 密码长度必须在6-20个字符之间");
            throw new RuntimeException("密码长度必须在6-20个字符之间");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(username) != null) {
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

            // 设置默认角色
            Role userRole = roleRepository.findByName("USER");
            if (userRole == null) {
                logger.warn("未找到USER角色，将创建新角色");
                userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
            }
            user.setRoles(Collections.singleton(userRole));

            User savedUser = userRepository.save(user);
            logger.info("用户 {} 注册成功", username);
            return savedUser;
        } catch (Exception e) {
            logger.error("用户注册过程中发生异常", e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
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
                    userRepository.save(user);
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
            userRepository.save(user);
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
            userRepository.save(user);
            logger.info("用户 {} 头像更新成功", user.getUsername());
        } catch (Exception e) {
            logger.error("头像更新过程中发生异常", e);
            throw new RuntimeException("头像更新失败: " + e.getMessage());
        }
    }
    
    @Override
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
            User updatedUser = userRepository.save(user);
            logger.info("用户 {} 个人信息更新成功", user.getUsername());
            return updatedUser;
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
            Role userRole = roleRepository.findByName("USER");
            if (userRole == null) {
                logger.warn("未找到USER角色");
                return new ArrayList<>();
            }
            
            // 获取所有具有USER角色的用户，并确保角色信息完整
            List<User> users = userRepository.findAll();
            return users.stream()
                // 确保用户有角色信息且包含USER角色
                .filter(user -> user != null && user.getRoles() != null && !user.getRoles().isEmpty())
                .filter(user -> user.getRoles().contains(userRole) &&
                        !user.getRoles().stream()
                            .anyMatch(role -> "ADMIN".equals(role.getName()) || "SUPERVISOR".equals(role.getName())))
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
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                logger.warn("未找到ID为{}的学生", id);
                throw new RuntimeException("学生不存在");
            }
            
            User user = userOpt.get();
            
            // 检查该用户是否为学生(具有USER角色)
            Role userRole = roleRepository.findByName("USER");
            if (!user.getRoles().contains(userRole)) {
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
    public boolean updateStudentStatus(Long id, String status) {
        logger.info("更新学生状态, ID: {}, 新状态: {}", id, status);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                logger.warn("未找到ID为{}的学生", id);
                return false;
            }
            
            User user = userOpt.get();
            
            // 检查该用户是否为学生(具有USER角色)
            Role userRole = roleRepository.findByName("USER");
            if (!user.getRoles().contains(userRole)) {
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
            userRepository.save(user);
            logger.info("学生 {} 状态更新为 {}", user.getUsername(), status);
            return true;
        } catch (Exception e) {
            logger.error("更新学生状态过程中发生异常", e);
            return false;
        }
    }

    @Override
    public boolean deleteStudent(Long id) {
        logger.info("删除学生, ID: {}", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                logger.warn("未找到ID为{}的学生", id);
                return false;
            }
            
            User user = userOpt.get();
            
            // 检查该用户是否为学生(具有USER角色)
            Role userRole = roleRepository.findByName("USER");
            if (!user.getRoles().contains(userRole)) {
                logger.warn("ID为{}的用户不是学生", id);
                return false;
            }
            
            // 删除用户
            userRepository.delete(user);
            logger.info("学生 {} 已删除", user.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("删除学生过程中发生异常", e);
            return false;
        }
    }

    @Override
    public List<User> getAllSupervisors() {
        // 获取SUPERVISOR角色
        Role supervisorRole = roleRepository.findByName("SUPERVISOR");
        if (supervisorRole == null) {
            logger.warn("未找到SUPERVISOR角色");
            return Collections.emptyList();
        }
        
        // 查找所有拥有SUPERVISOR角色的用户
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> user.getRoles().contains(supervisorRole))
                .collect(Collectors.toList());
    }

    // ==================== 辅助方法 ====================

    /**
     * 将User实体转换为StudentDTO
     */
    private StudentDTO convertToStudentDTO(User user) {
        String displayName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        String lastLoginStr = formatTime(user.getCreateTime());
        
        // 使用学号生成随机进度(0-100)
        Random random = new Random(user.getId());
        int progress = random.nextInt(101);
        
        return new StudentDTO(
            user.getId(),
            displayName,
            user.getUsername(), // 暂时使用用户名作为学号
            "计算机" + ((user.getId().intValue() % 3) + 1) + "班", // 模拟班级
            user.getEmail(),
            user.getPhone(),
            user.getStatus(),
            lastLoginStr,
            progress
        );
    }
    
    /**
     * 格式化时间为相对时间描述
     */
    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "从未登录";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = java.time.Duration.between(time, now).toDays();
        
        if (daysDiff == 0) {
            return "今天 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (daysDiff == 1) {
            return "昨天 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (daysDiff < 7) {
            return daysDiff + "天前";
        } else {
            return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
    
    /**
     * 为学生生成课程数据
     */
    private List<CourseDTO> generateCoursesForStudent(User user) {
        List<CourseDTO> courses = new ArrayList<>();
        
        // 使用用户ID作为随机种子,确保同一用户总是获得相同的课程数据
        Random random = new Random(user.getId());
        
        // 添加高等数学课程
        int mathCompletion = 10 + random.nextInt(91); // 10-100
        courses.add(new CourseDTO(
            "高等数学",
            mathCompletion == 100 ? "已完成" : "进行中",
            "已完成第" + (mathCompletion / 20 + 1) + "章",
            mathCompletion
        ));
        
        // 添加数据结构课程
        int dataStructureCompletion = random.nextInt(91); // 0-90
        courses.add(new CourseDTO(
            "数据结构",
            "进行中",
            "已完成第" + (dataStructureCompletion / 20 + 1) + "章",
            dataStructureCompletion
        ));
        
        // 添加大学英语课程
        int englishCompletion = 20 + random.nextInt(81); // 20-100
        courses.add(new CourseDTO(
            "大学英语",
            englishCompletion == 100 ? "已完成" : "进行中",
            englishCompletion == 100 ? "全部完成" : "已完成第" + (englishCompletion / 20 + 1) + "章",
            englishCompletion
        ));
        
        return courses;
    }
    
    /**
     * 为学生生成活动数据
     */
    private List<ActivityDTO> generateActivitiesForStudent(User user) {
        List<ActivityDTO> activities = new ArrayList<>();
        
        // 使用用户ID作为随机种子,确保同一用户总是获得相同的活动数据
        Random random = new Random(user.getId());
        LocalDateTime now = LocalDateTime.now();
        
        // 判断学生最近是否活跃
        if ("active".equals(user.getStatus())) {
            // 添加登录系统的活动
            activities.add(new ActivityDTO(
                "info",
                "登录系统",
                "通过" + (random.nextBoolean() ? "移动端" : "电脑端") + "登录",
                formatTime(now.minusHours(random.nextInt(24)))
            ));
            
            // 添加提交作业或参加考试的活动
            if (random.nextBoolean()) {
                activities.add(new ActivityDTO(
                    "success",
                    "提交作业",
                    "提交了《" + (random.nextBoolean() ? "高等数学" : "数据结构") + "》第" + (random.nextInt(5) + 1) + "章作业",
                    formatTime(now.minusHours(random.nextInt(48)))
                ));
            } else {
                activities.add(new ActivityDTO(
                    "success",
                    "参加考试",
                    "完成了《" + (random.nextBoolean() ? "高等数学" : "数据结构") + "》期中考试",
                    formatTime(now.minusDays(random.nextInt(3)))
                ));
            }
        } else {
            // 非活跃用户，添加缺勤通知
            activities.add(new ActivityDTO(
                "warning",
                "缺勤通知",
                "连续" + (random.nextInt(5) + 3) + "天未登录系统",
                formatTime(now)
            ));
            
            // 添加最后一次登录记录
            activities.add(new ActivityDTO(
                "info",
                "登录系统",
                "通过" + (random.nextBoolean() ? "移动端" : "电脑端") + "登录",
                formatTime(now.minusDays(random.nextInt(7) + 3))
            ));
        }
        
        // 可能添加一个作业逾期通知
        if (random.nextInt(3) == 0) { // 1/3的概率
            activities.add(new ActivityDTO(
                "warning",
                "作业逾期",
                "《" + (random.nextBoolean() ? "高等数学" : "数据结构") + "》第" + (random.nextInt(5) + 1) + "章作业逾期未交",
                formatTime(now.minusDays(random.nextInt(3) + 1))
            ));
        }
        
        return activities;
    }
}