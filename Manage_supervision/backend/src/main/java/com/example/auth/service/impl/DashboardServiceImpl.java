package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.SupervisorDashboardDTO;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
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
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

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
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        
        for (User user : users) {
            DashboardStats.UserInfo userInfo = new DashboardStats.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            
            // 查询用户角色
            List<Role> roles = roleMapper.findRolesByUserId(user.getId());
            List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
            
            userInfo.setRoles(roleNames);
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
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        stats.setTotalUsers(totalUsers);
        
        // 获取角色分布
        Map<String, Long> roleDistribution = new HashMap<>();
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<>());
        
        for (Role role : roles) {
            // 查询具有该角色的用户数量
            Long count = (long) userMapper.findByRoleId(role.getId()).size();
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
        
        // 获取USER角色
        Role userRole = roleMapper.findByName("USER");
        
        if (userRole != null) {
            // 查找所有拥有USER角色的用户
            List<User> students = userMapper.findByRoleId(userRole.getId());
            
            // 设置学生总数
            stats.setStudentCount(students.size());
            
            // 计算今日活跃学生数（使用状态字段代替登录时间）
            int activeTodayCount = (int) students.stream()
                .filter(student -> "active".equals(student.getStatus()))
                .count();
            stats.setActiveToday(activeTodayCount);
        } else {
            // 如果没有找到USER角色，设置为0
            stats.setStudentCount(0);
            stats.setActiveToday(0);
        }
        
        // 移除待处理任务的计算
        stats.setPendingTasks(0); // 设置为0或者移除该字段
        
        // 移除每周事件的计算（如果它基于任务）
        stats.setWeeklyEvents(0); // 设置为0或者移除该字段
        
        return stats;
    }
} 