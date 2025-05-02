package com.example.auth.service.impl;

import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.SupervisorDashboardDTO;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private DashboardStats.SystemInfo getSystemInfo() {
        DashboardStats.SystemInfo systemInfo = new DashboardStats.SystemInfo();
        Runtime runtime = Runtime.getRuntime();
        
        systemInfo.setJavaVersion(System.getProperty("java.version"));
        systemInfo.setOsName(System.getProperty("os.name"));
        systemInfo.setOsVersion(System.getProperty("os.version"));
        systemInfo.setTotalMemory(runtime.totalMemory());
        systemInfo.setFreeMemory(runtime.freeMemory());
        systemInfo.setAvailableProcessors(runtime.availableProcessors());
        
        return systemInfo;
    }

    private List<DashboardStats.UserInfo> getUserList() {
        List<DashboardStats.UserInfo> userList = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            DashboardStats.UserInfo userInfo = new DashboardStats.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRoles(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList()));
            userInfo.setCreateTime(user.getCreateTime() != null ? 
                    user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
                    "未知");
            userList.add(userInfo);
        }
        return userList;
    }

    @Override
    public DashboardStats getFullDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        // 获取用户总数
        stats.setTotalUsers(userRepository.count());
        
        // 获取角色分布
        Map<String, Long> roleDistribution = new HashMap<>();
        for (Role role : roleRepository.findAll()) {
            Long count = userRepository.countByRolesContaining(role);
            roleDistribution.put(role.getName(), count);
        }
        stats.setRoleDistribution(roleDistribution);
        
        // 获取系统信息
        stats.setSystemInfo(getSystemInfo());
        
        // 获取用户列表
        stats.setUserList(getUserList());
        
        return stats;
    }

    @Override
    public DashboardStats getBasicDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        // 普通用户不显示用户统计
        stats.setTotalUsers(0L);
        
        // 不包含角色分布
        stats.setRoleDistribution(new HashMap<>());
        
        // 不包含用户列表
        stats.setUserList(new ArrayList<>());
        
        // 只返回系统信息
        stats.setSystemInfo(getSystemInfo());
        
        return stats;
    }
    
    @Override
    public SupervisorDashboardDTO getSupervisorDashboardStats(Long supervisorId) {
        SupervisorDashboardDTO stats = new SupervisorDashboardDTO();
        
        // 查找督导员所负责的所有学生 (此处假设督导员负责所有学生，可能需要调整逻辑)
        List<User> students = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream().anyMatch(role -> "USER".equals(role.getName()))) // 假设学生角色为 'USER'
            .collect(Collectors.toList());
            
        // 设置学生总数
        stats.setStudentCount(students.size());
        
        // 计算今日活跃学生数（使用状态字段代替登录时间）
        int activeTodayCount = (int) students.stream()
            .filter(student -> "active".equals(student.getStatus()))
            .count();
        stats.setActiveToday(activeTodayCount);
        
        // 移除待处理任务的计算
        stats.setPendingTasks(0); // 设置为0或者移除该字段
        
        // 移除每周事件的计算（如果它基于任务）
        stats.setWeeklyEvents(0); // 设置为0或者移除该字段

        
        return stats;
    }
} 