package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.model.dto.DashboardStats;
import com.example.auth.model.dto.SupervisorDashboardDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.CourseApplication;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.mapper.CourseApplicationMapper;
import com.example.auth.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CourseApplicationMapper courseApplicationMapper;

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
    
    @Override
    public Map<String, Object> getUserCreationTrend() {
        // 获取所有用户
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<>());
        
        // 计算近7天的日期范围
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // 7天包括今天
        
        // 初始化日期和用户数量数组
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        // 日期格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        
        // 为过去7天每一天创建日期并初始化计数为0
        Map<LocalDate, Integer> dateCountMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dateCountMap.put(date, 0);
            dates.add(date.format(formatter));
        }
        
        // 统计每天创建的用户数量
        for (User user : allUsers) {
            LocalDateTime createTime = user.getCreateTime();
            if (createTime != null) {
                LocalDate createDate = createTime.toLocalDate();
                if (!createDate.isBefore(startDate) && !createDate.isAfter(today)) {
                    // 如果创建日期在近7天内，增加对应日期的计数
                    dateCountMap.put(createDate, dateCountMap.getOrDefault(createDate, 0) + 1);
                }
            }
        }
        
        // 收集统计结果到计数列表
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            counts.add(dateCountMap.get(date));
        }
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getStudentLearningRecords(Long studentId) {
        System.out.println("获取学习记录：学生ID = " + studentId);
        
        // 获取学生的所有订单，不限制状态
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStudentId, studentId)
               .orderByDesc(Order::getCreateTime);
        List<Order> orders = orderMapper.selectList(wrapper);
        
        System.out.println("查询到订单数量: " + orders.size());
        
        // 按课程名称分组统计学习时长
        Map<String, Integer> courseHoursMap = new HashMap<>();
        
        for (Order order : orders) {
            // 获取课程标题，如果不存在则查询课程应用表
            String courseTitle = null;
            if (order.getCourseId() != null) {
                CourseApplication courseApp = courseApplicationMapper.selectById(order.getCourseId());
                courseTitle = courseApp != null ? courseApp.getTitle() : "未知课程";
            } else {
                courseTitle = "未知课程";
            }
            
            // 使用订单的hours字段，如果为空则默认为1
            Integer hours = order.getHours() != null ? order.getHours() : 1;
            
            System.out.println("订单ID: " + order.getId() + ", 课程: " + courseTitle + ", 状态: " + order.getStatus() + ", 学时: " + hours);
            
            // 累加该课程的学习时长
            courseHoursMap.put(courseTitle, courseHoursMap.getOrDefault(courseTitle, 0) + hours);
        }
        
        // 准备返回数据
        List<String> courseNames = new ArrayList<>(courseHoursMap.keySet());
        List<Integer> courseHours = new ArrayList<>();
        
        for (String courseName : courseNames) {
            courseHours.add(courseHoursMap.get(courseName));
        }
        
        int totalHours = courseHours.stream().mapToInt(Integer::intValue).sum();
        
        System.out.println("总课程数: " + courseNames.size() + ", 总学时: " + totalHours);
        
        Map<String, Object> result = new HashMap<>();
        result.put("courseNames", courseNames);
        result.put("courseHours", courseHours);
        result.put("totalCourses", courseNames.size());
        result.put("totalHours", totalHours);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getStudentLearningTrend(Long studentId) {
        System.out.println("获取学习趋势：学生ID = " + studentId);
        
        // 获取学生的所有订单，不限制状态
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStudentId, studentId)
               .orderByAsc(Order::getCreateTime);
        List<Order> orders = orderMapper.selectList(wrapper);
        
        System.out.println("查询到订单数量: " + orders.size());
        
        // 按月份统计学习时长
        Map<String, Integer> monthlyHoursMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // 初始化过去6个月的数据
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = today.minusMonths(i);
            String monthKey = date.format(formatter);
            monthlyHoursMap.put(monthKey, 0);
        }
        
        // 统计每月的学习时长
        for (Order order : orders) {
            if (order.getCreateTime() != null) {
                String monthKey = order.getCreateTime().format(formatter);
                
                // 获取学时，如果为空则默认为1
                Integer hours = order.getHours() != null ? order.getHours() : 1;
                
                System.out.println("订单ID: " + order.getId() + ", 创建时间: " + order.getCreateTime() + 
                                 ", 月份: " + monthKey + ", 学时: " + hours);
                
                // 只统计最近6个月的数据
                if (monthlyHoursMap.containsKey(monthKey)) {
                    monthlyHoursMap.put(monthKey, monthlyHoursMap.get(monthKey) + hours);
                }
            }
        }
        
        // 准备返回数据
        List<String> months = new ArrayList<>(monthlyHoursMap.keySet());
        List<Integer> hours = new ArrayList<>();
        
        for (String month : months) {
            hours.add(monthlyHoursMap.get(month));
            System.out.println("月份: " + month + ", 学时: " + monthlyHoursMap.get(month));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("hours", hours);
        
        return result;
    }
} 