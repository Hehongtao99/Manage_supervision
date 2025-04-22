package com.example.auth.config;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
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
            supervisorRole.setDescription("督导员角色，可以管理学生");
            supervisorRole.setPermissions(",USER_VIEW,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW");
            supervisorRole.setCreateTime(java.time.LocalDateTime.now());
            supervisorRole = roleRepository.save(supervisorRole);
        }

    }
} 