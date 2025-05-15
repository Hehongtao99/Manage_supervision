package com.example.auth.service.impl;

import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.TeacherDashboardStatsDTO;
import com.example.auth.entity.Exam;
import com.example.auth.entity.ExamStudent;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.*;
import com.example.auth.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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
    private ExamRepository examRepository;

    @Autowired
    private ExamStudentRepository examStudentRepository;

    @Autowired
    private TeacherStudentRepository teacherStudentRepository;

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
    public TeacherDashboardStatsDTO getTeacherDashboardStats(Long teacherId) {
        TeacherDashboardStatsDTO stats = new TeacherDashboardStatsDTO();

        // 获取教师对象
        User teacher = userRepository.findById(teacherId).orElse(null);
        
        // 计算教师管理的学生数量（基于教师-学生关系，而不是基于考试）
        long studentCount = 0;
        if (teacher != null) {
            List<User> students = teacherStudentRepository.findActiveStudentsByTeacher(teacher);
            studentCount = students.size();
        }
        stats.setStudentCount(studentCount);

        // 获取该教师发布的考试数量
        long examCount = examRepository.countByCreatorId(teacherId);
        stats.setExamCount(examCount);

        return stats;
    }

    @Override
    public Map<String, Long> getExamGradeDistribution(Long examId) {
        // 获取该考试所有已发布成绩的学生记录 (PUBLISHED 状态)
        List<ExamStudent> gradedStudents = examStudentRepository.findAllByExamIdAndStatus(examId, "PUBLISHED");

        Map<String, Long> distribution = new LinkedHashMap<>(); // Use LinkedHashMap to keep insertion order
        distribution.put("不及格", 0L);
        distribution.put("60-70分", 0L);
        distribution.put("70-80分", 0L);
        distribution.put("80-90分", 0L);
        distribution.put("90-100分", 0L);

        for (ExamStudent student : gradedStudents) {
            BigDecimal score = student.getScore();
            if (score == null) continue; // Skip if score is null

            int scoreVal = score.intValue();

            if (scoreVal < 60) {
                distribution.compute("不及格", (k, v) -> (v == null ? 0 : v) + 1);
            } else if (scoreVal < 70) {
                distribution.compute("60-70分", (k, v) -> (v == null ? 0 : v) + 1);
            } else if (scoreVal < 80) {
                distribution.compute("70-80分", (k, v) -> (v == null ? 0 : v) + 1);
            } else if (scoreVal < 90) {
                distribution.compute("80-90分", (k, v) -> (v == null ? 0 : v) + 1);
            } else {
                distribution.compute("90-100分", (k, v) -> (v == null ? 0 : v) + 1);
            }
        }

        return distribution;
    }

    @Override
    public Map<String, Long> getExamParticipationStats(Long teacherId) {
        // 1. 获取该教师创建的所有考试 ID
        List<Long> examIds = examRepository.findExamIdsByCreatorId(teacherId);

        if (examIds.isEmpty()) {
            return Map.of("未开始", 0L, "进行中", 0L, "待批阅", 0L, "已完成", 0L);
        }

        // 2. 获取这些考试的所有学生参与记录
        List<ExamStudent> allParticipations = examStudentRepository.findByExamIdIn(examIds);

        // 3. 统计各种状态的数量
        Map<String, Long> participationStats = new LinkedHashMap<>();
        participationStats.put("未开始", 0L);   // NOT_STARTED
        participationStats.put("进行中", 0L);   // IN_PROGRESS
        participationStats.put("待批阅", 0L);   // SUBMITTED or PENDING_PUBLISH
        participationStats.put("已完成", 0L);   // PUBLISHED

        for (ExamStudent participation : allParticipations) {
            String status = participation.getStatus();
            if (status == null) continue;

            switch (status) {
                case "NOT_STARTED":
                    participationStats.compute("未开始", (k, v) -> v + 1);
                    break;
                case "IN_PROGRESS":
                    participationStats.compute("进行中", (k, v) -> v + 1);
                    break;
                case "SUBMITTED":
                case "PENDING_PUBLISH": // Consider PENDING_PUBLISH as '待批阅' for the chart
                    participationStats.compute("待批阅", (k, v) -> v + 1);
                    break;
                case "PUBLISHED":
                    participationStats.compute("已完成", (k, v) -> v + 1);
                    break;
                // Ignore other potential statuses like GRADED if PUBLISHED is the final display state
            }
        }

        return participationStats;
    }

    @Override
    public Map<String, Object> getGradeTrendData(Long teacherId, String range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime = now; // End date is always now

        // Determine the start date based on the range
        switch (range) {
            case "week":
                startDateTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "month":
                startDateTime = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "semester":
                // Assuming a semester is roughly 6 months
                startDateTime = now.minusMonths(6).with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            default:
                // Default to month if range is invalid
                startDateTime = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        // 1. Find exams created by the teacher within the date range and are finished/graded (status PUBLISHED)
        //    We need exams that *ended* within the range to calculate trends based on completion time.
        List<Exam> relevantExams = examRepository.findByCreatorIdAndEndTimeBetween(teacherId, startDateTime.atZone(ZoneId.systemDefault()).toInstant(), endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        if (relevantExams.isEmpty()) {
            return Map.of("dates", Collections.emptyList(), "averageScores", Collections.emptyList(), "highestScores", Collections.emptyList());
        }

        List<Long> relevantExamIds = relevantExams.stream().map(Exam::getId).collect(Collectors.toList());

        // 2. Find all student results for these exams with status PUBLISHED
        List<ExamStudent> results = examStudentRepository.findByExamIdInAndStatus(relevantExamIds, "PUBLISHED");

        // 3. Group results by exam end date (or a suitable grouping factor like week/month)
        //    Let's group by exam completion date (approximated by exam end time for simplicity)
        Map<LocalDate, List<ExamStudent>> resultsByDate = results.stream()
                .collect(Collectors.groupingBy(es -> {
                    Exam exam = relevantExams.stream().filter(e -> e.getId().equals(es.getExamId())).findFirst().orElse(null);
                    return exam != null && exam.getEndTime() != null 
                           ? LocalDateTime.ofInstant(exam.getEndTime(), ZoneId.systemDefault()).toLocalDate() 
                           : LocalDate.MIN; // Should not happen ideally
                }, TreeMap::new, Collectors.toList())); // Use TreeMap to sort by date

        List<String> dates = new ArrayList<>();
        List<Double> averageScores = new ArrayList<>();
        List<Double> highestScores = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd"); // Format for display

        // 4. Calculate average and highest score for each date group
        resultsByDate.forEach((date, studentResults) -> {
            if (date.equals(LocalDate.MIN) || studentResults.isEmpty()) return; // Skip invalid dates or empty lists

            double sum = 0;
            double maxScore = 0;
            int count = 0;
            for (ExamStudent result : studentResults) {
                if (result.getScore() != null) {
                    double score = result.getScore().doubleValue();
                    sum += score;
                    if (score > maxScore) {
                        maxScore = score;
                    }
                    count++;
                }
            }

            if (count > 0) {
                dates.add(date.format(dateFormatter));
                averageScores.add(sum / count);
                highestScores.add(maxScore);
            }
        });

        return Map.of("dates", dates, "averageScores", averageScores, "highestScores", highestScores);
    }
} 