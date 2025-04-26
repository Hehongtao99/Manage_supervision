package com.example.auth.service.impl;

import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.StudentProjectProgressDTO;
import com.example.auth.dto.SupervisorDashboardDTO;
import com.example.auth.entity.Project;
import com.example.auth.entity.Role;
import com.example.auth.entity.Task;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.NotificationRepository;
import com.example.auth.repository.ProjectRepository;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.TaskRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

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
        
        // 获取活跃用户数
        Long activeUsers = userRepository.countByStatus("active");
        stats.setActiveUsers(activeUsers);
        
        // 获取角色总数
        Long totalRoles = roleRepository.count();
        stats.setTotalRoles(totalRoles);
        
        // 设置系统状态
        stats.setSystemStatus("正常");
        
        // 获取学生总数
        // 使用USER角色作为学生角色
        Long studentCount = userRepository.countByRolesNameContaining("USER");
        stats.setTotalStudents(studentCount);
        
        // 获取教师总数
        // 使用SUPERVISOR角色作为教师角色
        Long teacherCount = userRepository.countByRolesNameContaining("SUPERVISOR");
        stats.setTotalTeachers(teacherCount);
        
        // 获取班级总数
        Long classCount = classRepository.count();
        stats.setTotalClasses(classCount);
        
        // 获取通知总数
        Long notificationCount = notificationRepository.count();
        stats.setTotalNotifications(notificationCount);
        
        // 获取角色分布
        Map<String, Long> roleDistribution = new HashMap<>();
        for (Role role : roleRepository.findAll()) {
            Long count = userRepository.countByRolesContaining(role);
            roleDistribution.put(role.getName(), count);
        }
        stats.setRoleDistribution(roleDistribution);
        
        // 获取班级分布数据
        Map<String, Long> classDistribution = new HashMap<>();
        classRepository.findAll().forEach(clazz -> {
            String className = clazz.getClassName();
            if (className != null && !className.isEmpty()) {
                // 对年级进行分组，如"一年级(1)班" -> "一年级"
                String grade = className;
                if (className.contains("年级")) {
                    grade = className.substring(0, className.indexOf("年级") + 2);
                }
                
                Long count = classDistribution.getOrDefault(grade, 0L);
                classDistribution.put(grade, count + 1);
            }
        });
        stats.setClassDistribution(classDistribution);
        
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
        
        // 不包含班级分布
        stats.setClassDistribution(new HashMap<>());
        
        // 不包含用户列表
        stats.setUserList(new ArrayList<>());
        
        // 只返回系统信息
        stats.setSystemInfo(getSystemInfo());
        
        return stats;
    }
    
    @Override
    public SupervisorDashboardDTO getSupervisorDashboardStats(Long supervisorId) {
        SupervisorDashboardDTO stats = new SupervisorDashboardDTO();
        
        // 查找督导员所负责的所有学生
        List<User> students = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream().anyMatch(role -> "STUDENT".equals(role.getName())))
            .collect(Collectors.toList());
            
        // 设置学生总数
        stats.setStudentCount(students.size());
        
        // 计算今日活跃学生数（使用状态字段代替登录时间）
        int activeTodayCount = (int) students.stream()
            .filter(student -> "active".equals(student.getStatus()))
            .count();
        stats.setActiveToday(activeTodayCount);
        
        // 查找督导员负责的所有任务，手动筛选未完成的
        List<Task> allTasks = taskRepository.findBySupervisorId(supervisorId);
        List<Task> pendingTasks = allTasks.stream()
            .filter(task -> !task.getCompleted())
            .collect(Collectors.toList());
        stats.setPendingTasks(pendingTasks.size());
        
        // 计算本周活动数
        LocalDateTime weekStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1);
        int weeklyEventsCount = (int) pendingTasks.stream()
            .filter(task -> task.getCreateTime() != null && task.getCreateTime().isAfter(weekStart))
            .count();
        stats.setWeeklyEvents(weeklyEventsCount);
        
        // 计算学生课题进度
        List<StudentProjectProgressDTO> progressList = new ArrayList<>();
        
        // 查找督导员负责的所有课题
        List<Project> supervisorProjects = projectRepository.findBySupervisorId(supervisorId);
        
        for (Project project : supervisorProjects) {
            // 如果课题分配给了学生
            if (project.getAssigneeId() != null) {
                // 查找该学生
                User student = userRepository.findById(project.getAssigneeId()).orElse(null);
                if (student != null) {
                    StudentProjectProgressDTO progressDTO = new StudentProjectProgressDTO();
                    
                    // 设置学生信息
                    progressDTO.setStudentId(student.getId());
                    progressDTO.setStudentName(student.getRealName() != null && !student.getRealName().isEmpty() 
                        ? student.getRealName() 
                        : student.getUsername());
                    progressDTO.setStudentNumber(student.getId().toString());
                    
                    // 设置课题信息
                    progressDTO.setProjectId(project.getId());
                    progressDTO.setProjectTitle(project.getTitle());
                    progressDTO.setProjectStatus(project.getStatus());
                    
                    // 设置期限信息
                    progressDTO.setDeadline(project.getEndTime());
                    progressDTO.setIsOverdue(project.getEndTime() != null && 
                                            LocalDateTime.now().isAfter(project.getEndTime()));
                    
                    // 查询该课题的所有任务
                    List<Task> projectTasks = taskRepository.findByProjectId(project.getId());
                    int totalTasks = projectTasks.size();
                    progressDTO.setTotalTasks(totalTasks);
                    
                    // 查询已完成的任务
                    List<Task> completedTasks = projectTasks.stream()
                        .filter(Task::getCompleted)
                        .collect(Collectors.toList());
                    int completedTasksCount = completedTasks.size();
                    progressDTO.setCompletedTasks(completedTasksCount);
                    
                    // 计算进度百分比
                    double progressPercentage = totalTasks > 0 ? 
                            ((double) completedTasksCount / totalTasks) * 100 : 0;
                    progressDTO.setProgressPercentage(progressPercentage);
                    
                    // 根据用户状态判断活跃状态
                    progressDTO.setStatus("active".equals(student.getStatus()) ? 
                                        "active" : "inactive");
                    
                    // 最近活动情况（简化处理）
                    if (!projectTasks.isEmpty()) {
                        Task latestTask = projectTasks.stream()
                                .max((t1, t2) -> t1.getUpdateTime().compareTo(t2.getUpdateTime()))
                                .orElse(null);
                        if (latestTask != null) {
                            progressDTO.setLastActivity(latestTask.getUpdateTime());
                            progressDTO.setLastActivityDescription("更新了任务: " + latestTask.getTitle());
                        }
                    }
                    
                    progressList.add(progressDTO);
                }
            }
        }
        
        stats.setStudentProjectProgresses(progressList);
        
        // 设置最近活动
        List<SupervisorDashboardDTO.ActivityItem> activities = new ArrayList<>();
        
        // 任务活动（简化示例）
        for (Task task : pendingTasks.stream()
                .sorted((t1, t2) -> t2.getUpdateTime().compareTo(t1.getUpdateTime()))
                .limit(5)
                .collect(Collectors.toList())) {
            
            // 添加空值检查，确保assigneeId不为null
            if (task.getAssigneeId() == null) {
                continue; // 跳过没有分配任务人的任务
            }
            
            User assignee = userRepository.findById(task.getAssigneeId()).orElse(null);
            if (assignee != null) {
                SupervisorDashboardDTO.ActivityItem activity = new SupervisorDashboardDTO.ActivityItem();
                
                // 设置活动信息
                activity.setTitle("学生" + assignee.getUsername() + "的任务状态更新");
                activity.setDescription(assignee.getUsername() + (task.getCompleted() ? "完成了" : "更新了") + "《" + task.getTitle() + "》");
                
                // 格式化时间
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime updateTime = task.getUpdateTime();
                LocalDateTime now = LocalDateTime.now();
                
                String timeDisplay;
                if (updateTime.toLocalDate().equals(now.toLocalDate())) {
                    timeDisplay = "今天 " + updateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                } else if (updateTime.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                    timeDisplay = "昨天 " + updateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    timeDisplay = updateTime.format(formatter);
                }
                
                activity.setTime(timeDisplay);
                activity.setType("任务更新");
                
                activities.add(activity);
            }
        }
        
        stats.setRecentActivities(activities);
        
        return stats;
    }
} 