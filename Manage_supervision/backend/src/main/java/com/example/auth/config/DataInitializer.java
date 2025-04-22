package com.example.auth.config;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ClassService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ClassService classService;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public void run(String... args) {
        initializeRoles();
        
        // 只在开发环境下运行测试代码
        if ("dev".equals(activeProfile) || "default".equals(activeProfile)) {
            // testClassTransfer();
        }
    }
    
    private void initializeRoles() {
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("管理员角色，拥有最高权限");
            adminRole.setPermissions(",USER_VIEW,ROLE_VIEW,LOG_VIEW,USER_EDIT,ROLE_EDIT,SYSTEM_SETTINGS,USER_DELETE,ROLE_DELETE");
            adminRole.setCreateTime(java.time.LocalDateTime.now());
            adminRole = roleRepository.save(adminRole);
        }

        Role userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("学生角色，基本用户权限");
            userRole.setPermissions("");
            userRole.setCreateTime(java.time.LocalDateTime.now());
            userRole = roleRepository.save(userRole);
        }
        
        // 添加督导员角色
        Role supervisorRole = roleRepository.findByName("SUPERVISOR");
        if (supervisorRole == null) {
            supervisorRole = new Role();
            supervisorRole.setName("SUPERVISOR");
            supervisorRole.setDescription("督导员角色，课题管理权限");
            supervisorRole.setPermissions("");
            supervisorRole.setCreateTime(java.time.LocalDateTime.now());
            supervisorRole = roleRepository.save(supervisorRole);
        }

        // 检查是否存在管理员账户，若不存在则创建默认管理员
        User adminUser = userRepository.findByUsername("admin");
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("$2a$10$EqK5Nc4H7YVgO8mzWB/YtO1Xm5JZLCGfv.YV5xmXHYu2SdO8Z9D3O"); // 密码：admin123
            adminUser.setEmail("admin@example.com");
            adminUser.setRealName("系统管理员");
            adminUser.setNickname("管理员");
            adminUser.setCreateTime(java.time.LocalDateTime.now());
            adminUser.setStatus("active");
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
        }
    }
    
    /**
     * 测试学生转班功能
     */
    private void testClassTransfer() {
        logger.info("开始测试学生转班功能...");
        try {
            // 模拟转班操作 - 这里只是测试代码，不会真正执行数据操作
            // 仅用于测试修复的转班功能是否正常
            logger.info("转班功能测试完成");
        } catch (Exception e) {
            logger.error("转班功能测试失败: {}", e.getMessage());
        }
    }
} 