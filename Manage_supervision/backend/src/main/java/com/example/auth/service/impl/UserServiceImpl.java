package com.example.auth.service.impl;

import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.mapper.UserRoleMapper;
import com.example.auth.model.dto.RunnerDTO;
import com.example.auth.model.dto.RunnerDetailDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.service.UserService;
import com.example.auth.util.PasswordUtils;
import com.example.auth.util.UserNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private JdbcTemplate jdbcTemplate;

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
            
            // 生成用户编号(默认注册用户为跑步爱好者)
            String userNumber = userNumberGenerator.generateUserNumber();
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
            
            logger.info("用户 {} 注册成功，用户编号: {}", username, userNumber);
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

    // ==================== 跑步爱好者管理相关方法实现 ====================

    @Override
    public List<RunnerDTO> getAllRunners() {
        logger.info("获取所有跑步爱好者列表");
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
                .map(this::convertToRunnerDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取跑步爱好者列表过程中发生异常", e);
            throw new RuntimeException("获取跑步爱好者列表失败: " + e.getMessage());
        }
    }

    @Override
    public RunnerDetailDTO getRunnerDetails(Long id) {
        logger.info("获取跑步爱好者详情, ID: {}", id);
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                logger.warn("未找到ID为{}的跑步爱好者", id);
                throw new RuntimeException("跑步爱好者不存在");
            }
            
            // 检查该用户是否为跑步爱好者(具有USER角色)
            Role userRole = roleMapper.findByName("USER");
            List<Role> userRoles = roleMapper.findRolesByUserId(id);
            
            boolean isRunner = userRoles.stream()
                    .anyMatch(role -> role.getId().equals(userRole.getId()));
            
            if (!isRunner) {
                logger.warn("ID为{}的用户不是跑步爱好者", id);
                throw new RuntimeException("指定ID的用户不是跑步爱好者");
            }
            
            // 转换为RunnerDTO
            RunnerDTO runnerDTO = convertToRunnerDTO(user);
            
            // 创建并返回RunnerDetailDTO，不包含课程和活动
            RunnerDetailDTO detailDTO = new RunnerDetailDTO();
            detailDTO.setId(runnerDTO.getId());
            detailDTO.setUsername(runnerDTO.getUsername());
            detailDTO.setRealName(runnerDTO.getRealName());
            detailDTO.setName(runnerDTO.getName());
            detailDTO.setUserNumber(runnerDTO.getUserNumber());
            detailDTO.setRunnerId(runnerDTO.getRunnerId());
            detailDTO.setEmail(runnerDTO.getEmail());
            detailDTO.setPhone(runnerDTO.getPhone());
            detailDTO.setStatus(runnerDTO.getStatus());
            detailDTO.setCreateTime(runnerDTO.getCreateTime());
            
            return detailDTO;
        } catch (Exception e) {
            logger.error("获取跑步爱好者详情过程中发生异常", e);
            throw new RuntimeException("获取跑步爱好者详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateRunnerStatus(Long id, String status) {
        logger.info("更新跑步爱好者状态, ID: {}, 新状态: {}", id, status);
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                logger.warn("未找到ID为{}的跑步爱好者", id);
                return false;
            }
            
            // 检查该用户是否为跑步爱好者(具有USER角色)
            Role userRole = roleMapper.findByName("USER");
            List<Role> userRoles = roleMapper.findRolesByUserId(id);
            
            boolean isRunner = userRoles.stream()
                    .anyMatch(role -> role.getId().equals(userRole.getId()));
            
            if (!isRunner) {
                logger.warn("ID为{}的用户不是跑步爱好者", id);
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
            logger.info("跑步爱好者 {} 状态更新为 {}", user.getUsername(), status);
            return true;
        } catch (Exception e) {
            logger.error("更新跑步爱好者状态过程中发生异常", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteRunner(Long id) {
        logger.info("删除跑步爱好者, ID: {}", id);
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                logger.warn("未找到ID为{}的跑步爱好者", id);
                return false;
            }
            
            // 检查该用户是否为跑步爱好者(具有USER角色)
            Role userRole = roleMapper.findByName("USER");
            List<Role> userRoles = roleMapper.findRolesByUserId(id);
            
            boolean isRunner = userRoles.stream()
                    .anyMatch(role -> role.getId().equals(userRole.getId()));
            
            if (!isRunner) {
                logger.warn("ID为{}的用户不是跑步爱好者", id);
                return false;
            }
            
            // 删除用户角色关联
            userRoleMapper.deleteUserRoles(id);
            
            // 删除用户
            userMapper.deleteById(id);
            logger.info("跑步爱好者 {} 已删除", user.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("删除跑步爱好者过程中发生异常", e);
            return false;
        }
    }
    
    @Override
    public List<UserDTO> getRunnersBySupervisor(Long supervisorId) {
        try {
            // 调用RunnerSupervisorMapper获取该管理员的所有跑步爱好者ID
            String sql = "SELECT runner_id FROM runner_supervisor_relations WHERE supervisor_id = ? AND status = 'active'";
            List<Long> runnerIds = jdbcTemplate.queryForList(sql, Long.class, supervisorId);
            
            if (runnerIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 批量获取跑步爱好者信息
            List<User> runners = new ArrayList<>();
            for (Long runnerId : runnerIds) {
                User runner = userMapper.selectById(runnerId);
                if (runner != null) {
                    runners.add(runner);
                }
            }
            
            // 转换为DTO
            return runners.stream()
                    .map(runner -> {
                        UserDTO dto = new UserDTO();
                        dto.setId(runner.getId());
                        dto.setUsername(runner.getUsername());
                        dto.setRealName(runner.getRealName());
                        dto.setUserNumber(runner.getUserNumber());
                        dto.setEmail(runner.getEmail());
                        dto.setPhone(runner.getPhone());
                        dto.setStatus(runner.getStatus());
                        dto.setAvatar(runner.getAvatar());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取管理员的跑步爱好者列表过程中发生异常", e);
            throw new RuntimeException("获取管理员的跑步爱好者列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 将User实体转换为RunnerDTO
     */
    private RunnerDTO convertToRunnerDTO(User user) {
        RunnerDTO dto = new RunnerDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName() != null ? user.getRealName() : "");
        dto.setUserNumber(user.getUserNumber() != null ? user.getUserNumber() : "");
        
        // 设置前端期望的字段
        dto.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        dto.setRunnerId(user.getUserNumber() != null ? user.getUserNumber() : "");
        
        dto.setEmail(user.getEmail() != null ? user.getEmail() : "");
        dto.setPhone(user.getPhone() != null ? user.getPhone() : "");
        dto.setStatus(user.getStatus() != null ? user.getStatus() : "");
        dto.setCreateTime(formatTime(user.getCreateTime()));
        
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
     * 获取用户发布的帖子数量
     */
    @Override
    public int getUserPostCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM social_posts WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }
    
    /**
     * 获取用户的跑步记录数量
     */
    @Override
    public int getUserRunningRecordCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM running_records WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
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
}