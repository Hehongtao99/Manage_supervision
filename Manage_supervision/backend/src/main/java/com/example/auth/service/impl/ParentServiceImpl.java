package com.example.auth.service.impl;

import com.example.auth.entity.*;
import com.example.auth.repository.*;
import com.example.auth.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParentServiceImpl implements ParentService {

    @Autowired
    private ParentChildRepository parentChildRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassStudentRelationRepository classStudentRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private TimetableItemRepository timetableItemRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Override
    @Transactional
    public ParentChildRelation createRelation(Long parentId, String childIdentifier, String relationType) {
        Optional<User> parentOpt = userRepository.findById(parentId);
        if (!parentOpt.isPresent()) {
            throw new IllegalArgumentException("家长不存在");
        }
        User parent = parentOpt.get();
        
        // 通过用户名或学号查找子女
        User child = null;
        if (childIdentifier.contains("@") || childIdentifier.length() < 5) {
            // 可能是用户名
            child = userRepository.findByUsername(childIdentifier);
            if (child == null) {
                throw new IllegalArgumentException("找不到该学生账号");
            }
        } else {
            // 可能是学号
            // 使用自定义查询或findByRoleName方法查找全部学生，然后过滤学号
            List<User> students = userRepository.findByRoleName("USER");
            child = students.stream()
                .filter(user -> childIdentifier.equals(user.getUserNumber()))
                .findFirst()
                .orElse(null);
                
            if (child == null) {
                throw new IllegalArgumentException("找不到该学号的学生");
            }
        }
        
        // 检查该关系是否已存在
        if (parentChildRepository.existsByParentIdAndChildId(parentId, child.getId())) {
            throw new IllegalStateException("已经存在该家长与学生的关联关系");
        }
        
        // 创建新的关系
        ParentChildRelation relation = new ParentChildRelation();
        relation.setParent(parent);
        relation.setChild(child);
        relation.setRelationType(relationType);
        relation.setStatus("pending"); // 初始状态为待确认
        
        return parentChildRepository.save(relation);
    }

    @Override
    @Transactional
    public ParentChildRelation updateRelationStatus(Long relationId, String status) {
        Optional<ParentChildRelation> relationOpt = parentChildRepository.findById(relationId);
        if (!relationOpt.isPresent()) {
            throw new IllegalArgumentException("关系不存在");
        }
        ParentChildRelation relation = relationOpt.get();
        
        relation.setStatus(status);
        return parentChildRepository.save(relation);
    }

    @Override
    public List<ParentChildRelation> getChildRelations(Long parentId) {
        Optional<User> parentOpt = userRepository.findById(parentId);
        if (!parentOpt.isPresent()) {
            throw new IllegalArgumentException("家长不存在");
        }
        User parent = parentOpt.get();
        
        return parentChildRepository.findByParent(parent);
    }

    @Override
    public List<ParentChildRelation> getConfirmedChildRelations(Long parentId) {
        Optional<User> parentOpt = userRepository.findById(parentId);
        if (!parentOpt.isPresent()) {
            throw new IllegalArgumentException("家长不存在");
        }
        User parent = parentOpt.get();
        
        return parentChildRepository.findByParentAndStatus(parent, "confirmed");
    }

    @Override
    public List<ParentChildRelation> getParentRelations(Long childId) {
        Optional<User> childOpt = userRepository.findById(childId);
        if (!childOpt.isPresent()) {
            throw new IllegalArgumentException("学生不存在");
        }
        User child = childOpt.get();
        
        return parentChildRepository.findByChild(child);
    }

    @Override
    public List<ParentChildRelation> getConfirmedParentRelations(Long childId) {
        Optional<User> childOpt = userRepository.findById(childId);
        if (!childOpt.isPresent()) {
            throw new IllegalArgumentException("学生不存在");
        }
        User child = childOpt.get();
        
        return parentChildRepository.findByChildAndStatus(child, "confirmed");
    }

    @Override
    public Map<String, Object> getChildDetails(Long childId) {
        // 获取子女信息
        Optional<User> childOpt = userRepository.findById(childId);
        if (!childOpt.isPresent()) {
            throw new IllegalArgumentException("学生不存在");
        }
        User child = childOpt.get();
        
        // 创建学生信息映射
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("id", child.getId());
        studentInfo.put("username", child.getUsername());
        studentInfo.put("realName", child.getRealName());
        studentInfo.put("userNumber", child.getUserNumber());
        
        // 处理头像URL - 确保URL符合新的格式
        String avatarUrl = child.getAvatar();
        if (avatarUrl != null) {
            if (avatarUrl.startsWith("/uploads/") && !avatarUrl.startsWith("/api/file/uploads/")) {
                // 转换旧格式的URL为新格式
                avatarUrl = "/api/file/uploads/" + avatarUrl.substring("/uploads/".length());
            } else if (avatarUrl.startsWith("/api/file/uploads/")) {
                // 已经是新格式，不需处理
                avatarUrl = avatarUrl;
            } else if (avatarUrl.contains("/api/file/api/file/uploads/")) {
                // 修复错误的双重前缀
                avatarUrl = avatarUrl.replace("/api/file/api/file/uploads/", "/api/file/uploads/");
            }
        }
        studentInfo.put("avatar", avatarUrl);
        
        // 添加可能缺少的重要字段
        studentInfo.put("gender", child.getGender());
        studentInfo.put("age", child.getAge());
        studentInfo.put("grade", child.getGrade());
        studentInfo.put("phone", child.getPhone());
        studentInfo.put("email", child.getEmail());
        studentInfo.put("address", child.getAddress());
        studentInfo.put("nickname", child.getNickname());
        
        // 获取子女所在班级信息
        List<ClassStudentRelation> classRelations = classStudentRepository.findByStudentAndStatus(child, "active");
        if (!classRelations.isEmpty()) {
            ClassStudentRelation classRelation = classRelations.get(0);
            com.example.auth.entity.Class studentClass = classRelation.getClassEntity();
            
            Map<String, Object> classInfo = new HashMap<>();
            classInfo.put("id", studentClass.getId());
            classInfo.put("name", studentClass.getClassName());
            classInfo.put("grade", studentClass.getGrade());
            
            studentInfo.put("class", classInfo);
            // 添加班级名称作为单独字段，方便前端显示
            studentInfo.put("className", studentClass.getClassName());
        } else {
            studentInfo.put("class", null);
            studentInfo.put("className", "未分配班级");
        }
        
        // 任务信息
        List<Task> tasks = taskRepository.findByAssigneeId(childId);
        List<Map<String, Object>> taskList = tasks.stream()
                .map(task -> {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("id", task.getId());
                    taskInfo.put("title", task.getTitle());
                    taskInfo.put("status", task.getStatus());
                    taskInfo.put("completed", task.getCompleted());
                    taskInfo.put("endTime", task.getEndTime());
                    
                    return taskInfo;
                })
                .collect(Collectors.toList());
        
        // 返回结果
        Map<String, Object> details = new HashMap<>();
        details.put("student", studentInfo);  // 确保使用student键
        details.put("tasks", taskList);
        
        System.out.println("返回子女详情: " + studentInfo.get("realName") + ", ID: " + studentInfo.get("id") + ", className: " + studentInfo.get("className"));
        
        return details;
    }

    @Override
    public Map<String, Object> getChildCourseAndTasks(Long childId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取子女信息
        Optional<User> childOpt = userRepository.findById(childId);
        if (!childOpt.isPresent()) {
            throw new IllegalArgumentException("学生不存在");
        }
        User child = childOpt.get();
        
        // 获取子女所在班级信息
        List<ClassStudentRelation> classRelations = classStudentRepository.findByStudentAndStatus(child, "active");
        if (!classRelations.isEmpty()) {
            ClassStudentRelation classRelation = classRelations.get(0);
            Long classId = classRelation.getClassEntity().getId();
            
            // 获取该班级最新的课表
            List<Timetable> timetables = timetableRepository.findByClassId(classId);
            // 手动按周数降序排序
            if (timetables != null && !timetables.isEmpty()) {
                timetables.sort((t1, t2) -> {
                    Integer w1 = t1.getWeekNumber();
                    Integer w2 = t2.getWeekNumber();
                    return w2.compareTo(w1); // 降序排序
                });
                
                Timetable latestTimetable = timetables.get(0);
                List<TimetableItem> items = timetableItemRepository.findByTimetableId(latestTimetable.getId());
                
                List<Map<String, Object>> courseList = items.stream()
                        .map(item -> {
                            Map<String, Object> courseInfo = new HashMap<>();
                            courseInfo.put("id", item.getId());
                            courseInfo.put("courseName", item.getCourseName());
                            courseInfo.put("dayOfWeek", item.getDayOfWeek());
                            courseInfo.put("startTime", item.getStartTime());
                            courseInfo.put("endTime", item.getEndTime());
                            courseInfo.put("classroom", item.getClassroom());
                            
                            // 如果有教师信息，也添加
                            if (item.getTeacherId() != null) {
                                Optional<User> teacher = userRepository.findById(item.getTeacherId());
                                teacher.ifPresent(t -> {
                                    Map<String, Object> teacherInfo = new HashMap<>();
                                    teacherInfo.put("id", t.getId());
                                    teacherInfo.put("name", t.getRealName() != null ? t.getRealName() : t.getUsername());
                                    courseInfo.put("teacher", teacherInfo);
                                });
                            }
                            
                            return courseInfo;
                        })
                        .collect(Collectors.toList());
                
                result.put("timetable", courseList);
            } else {
                result.put("timetable", Collections.emptyList());
            }
        } else {
            result.put("timetable", Collections.emptyList());
        }
        
        // 任务信息 - 获取所有任务并过滤未完成的
        List<Task> tasks = taskRepository.findByAssigneeId(childId);
        // 过滤未完成的任务
        List<Task> uncompletedTasks = tasks.stream()
            .filter(task -> task.getCompleted() != null && !task.getCompleted())
            .collect(Collectors.toList());
        
        List<Map<String, Object>> taskList = uncompletedTasks.stream()
                .map(task -> {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("id", task.getId());
                    taskInfo.put("title", task.getTitle());
                    taskInfo.put("description", task.getDescription());
                    taskInfo.put("status", task.getStatus());
                    taskInfo.put("priority", task.getPriority());
                    taskInfo.put("startTime", task.getStartTime());
                    taskInfo.put("endTime", task.getEndTime());
                    
                    // 如果有督导教师，也添加
                    if (task.getSupervisorId() != null) {
                        Optional<User> supervisor = userRepository.findById(task.getSupervisorId());
                        supervisor.ifPresent(s -> {
                            Map<String, Object> supervisorInfo = new HashMap<>();
                            supervisorInfo.put("id", s.getId());
                            supervisorInfo.put("name", s.getRealName() != null ? s.getRealName() : s.getUsername());
                            taskInfo.put("supervisor", supervisorInfo);
                        });
                    }
                    
                    return taskInfo;
                })
                .collect(Collectors.toList());
        
        result.put("tasks", taskList);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getParentNotifications(Long parentId) {
        // 获取家长的所有已确认子女
        List<User> children = parentChildRepository.findConfirmedChildrenByParentId(parentId);
        
        // 获取每个子女关联的通知
        List<Map<String, Object>> allNotifications = new ArrayList<>();
        
        // 1. 获取发送给子女的通知
        for (User child : children) {
            // 通过用户通知关联表查找学生的通知
            List<UserNotification> userNotifications = userNotificationRepository.findByUserId(child.getId());
            
            for (UserNotification userNotification : userNotifications) {
                Notification notification = userNotification.getNotification();
                
                if (notification != null) {
                    Map<String, Object> notificationInfo = new HashMap<>();
                    notificationInfo.put("id", notification.getId());
                    notificationInfo.put("title", notification.getTitle());
                    notificationInfo.put("content", notification.getContent());
                    notificationInfo.put("publishTime", notification.getCreateTime());
                    notificationInfo.put("status", notification.getStatus());
                    
                    // 获取发送者信息
                    User sender = notification.getSender();
                    if (sender != null) {
                        notificationInfo.put("sender", sender.getRealName() != null 
                                ? sender.getRealName() : sender.getUsername());
                        
                        // 判断发送者角色，设置通知类型
                        Set<Role> roles = sender.getRoles();
                        if (roles.stream().anyMatch(role -> "ADMIN".equals(role.getName()))) {
                            notificationInfo.put("type", "system");
                        } else if (roles.stream().anyMatch(role -> "SUPERVISOR".equals(role.getName()))) {
                            notificationInfo.put("type", "teacher");
                        } else {
                            notificationInfo.put("type", "other");
                        }
                    } else {
                        notificationInfo.put("sender", "未知");
                        notificationInfo.put("type", "other");
                    }
                    
                    // 添加子女信息
                    notificationInfo.put("childName", child.getRealName() != null ? child.getRealName() : child.getUsername());
                    notificationInfo.put("childId", child.getId());
                    
                    // 设置为已读状态（这里假设家长查看即标记为已读）
                    notificationInfo.put("isRead", true);
                    
                    // 添加到结果列表
                    allNotifications.add(notificationInfo);
                }
            }
        }
        
        // 2. 获取直接发送给家长的通知
        List<UserNotification> parentNotifications = userNotificationRepository.findByUserId(parentId);
        for (UserNotification userNotification : parentNotifications) {
            Notification notification = userNotification.getNotification();
            
            if (notification != null) {
                Map<String, Object> notificationInfo = new HashMap<>();
                notificationInfo.put("id", notification.getId());
                notificationInfo.put("title", notification.getTitle());
                notificationInfo.put("content", notification.getContent());
                notificationInfo.put("publishTime", notification.getCreateTime());
                notificationInfo.put("status", notification.getStatus());
                
                // 获取发送者信息
                User sender = notification.getSender();
                if (sender != null) {
                    notificationInfo.put("sender", sender.getRealName() != null 
                            ? sender.getRealName() : sender.getUsername());
                    
                    // 判断发送者角色，设置通知类型
                    Set<Role> roles = sender.getRoles();
                    if (roles.stream().anyMatch(role -> "ADMIN".equals(role.getName()))) {
                        notificationInfo.put("type", "system");
                    } else if (roles.stream().anyMatch(role -> "SUPERVISOR".equals(role.getName()))) {
                        notificationInfo.put("type", "teacher");
                    } else {
                        notificationInfo.put("type", "other");
                    }
                } else {
                    notificationInfo.put("sender", "未知");
                    notificationInfo.put("type", "other");
                }
                
                // 标记为直接发送给家长的通知
                notificationInfo.put("isDirectToParent", true);
                
                // 设置为已读状态
                notificationInfo.put("isRead", true);
                
                // 添加到结果列表
                allNotifications.add(notificationInfo);
            }
        }
        
        // 按发布时间排序
        allNotifications.sort((n1, n2) -> {
            LocalDateTime date1 = (LocalDateTime) n1.get("publishTime");
            LocalDateTime date2 = (LocalDateTime) n2.get("publishTime");
            return date2.compareTo(date1); // 降序排列，最新的在前
        });
        
        return allNotifications;
    }

    @Override
    @Transactional
    public boolean removeRelation(Long relationId) {
        try {
            if (parentChildRepository.existsById(relationId)) {
                parentChildRepository.deleteById(relationId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean relationExists(Long parentId, Long childId) {
        return parentChildRepository.existsByParentIdAndChildId(parentId, childId);
    }

    @Override
    public Map<String, Object> getChildStats(Long childId) {
        // 获取子女信息
        Optional<User> childOpt = userRepository.findById(childId);
        if (!childOpt.isPresent()) {
            throw new IllegalArgumentException("学生不存在");
        }
        User child = childOpt.get();
        
        Map<String, Object> statsData = new HashMap<>();
        
        // 基本信息
        statsData.put("gender", child.getGender() != null ? child.getGender() : null);
        statsData.put("age", child.getAge() != null ? child.getAge() : null);
        statsData.put("address", child.getAddress() != null ? child.getAddress() : null);
        statsData.put("phone", child.getPhone() != null ? child.getPhone() : null);
        
        try {
            // 获取考勤情况统计
            // 这里需要根据实际情况调用你的考勤服务获取数据
            // 这里仅为示例，实际应用中应该从数据库查询
            double attendanceRate = calculateAttendanceRate(childId);
            statsData.put("attendanceRate", Math.round(attendanceRate * 100) / 100.0); // 保留两位小数
            
            // 获取作业完成率
            double homeworkRate = calculateHomeworkCompletionRate(childId);
            statsData.put("homeworkRate", Math.round(homeworkRate * 100) / 100.0); // 保留两位小数
            
            // 获取平均成绩
            double averageScore = calculateAverageScore(childId);
            statsData.put("averageScore", Math.round(averageScore * 100) / 100.0); // 保留两位小数
        } catch (Exception e) {
            // 如果查询统计数据时出错，记录错误但不影响其他数据返回
            System.err.println("获取学生统计数据出错: " + e.getMessage());
        }
        
        return statsData;
    }
    
    /**
     * 计算学生的考勤率
     * @param childId 学生ID
     * @return 考勤率（0-100）
     */
    private double calculateAttendanceRate(Long childId) {
        // 这里应该是从考勤记录中计算出勤率
        // 为简化示例，这里返回一个模拟值
        // 实际应用中，应该从数据库查询考勤记录并计算
        
        // 模拟计算逻辑: 随机生成80-100的值
        return 80 + Math.random() * 20;
    }
    
    /**
     * 计算学生的作业完成率
     * @param childId 学生ID
     * @return 作业完成率（0-100）
     */
    private double calculateHomeworkCompletionRate(Long childId) {
        // 这里应该从作业记录中计算完成率
        // 为简化示例，这里返回一个模拟值
        // 实际应用中，应该从数据库查询作业记录并计算
        
        // 模拟计算逻辑: 随机生成70-100的值
        return 70 + Math.random() * 30;
    }
    
    /**
     * 计算学生的平均成绩
     * @param childId 学生ID
     * @return 平均成绩（0-100）
     */
    private double calculateAverageScore(Long childId) {
        // 这里应该从成绩记录中计算平均分
        // 为简化示例，这里返回一个模拟值
        // 实际应用中，应该从数据库查询成绩记录并计算
        
        // 模拟计算逻辑: 随机生成60-100的值
        return 60 + Math.random() * 40;
    }
} 