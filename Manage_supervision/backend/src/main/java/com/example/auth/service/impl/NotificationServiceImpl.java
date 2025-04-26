package com.example.auth.service.impl;

import com.example.auth.dto.NotificationRequest;
import com.example.auth.dto.NotificationResponse;
import com.example.auth.entity.*;
import com.example.auth.repository.*;
import com.example.auth.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassStudentRelationRepository classStudentRelationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(Long senderId, NotificationRequest request) {
        System.out.println("开始创建通知: senderId=" + senderId + ", title=" + request.getTitle() + ", type=" + request.getRecipientType());
        
        // 验证请求对象
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("通知标题不能为空");
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("通知内容不能为空");
        }
        
        if (request.getRecipientType() == null) {
            throw new RuntimeException("通知接收者类型不能为空");
        }
        
        // 验证发送者存在
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("用户不存在: ID=" + senderId));
        
        System.out.println("发送者信息: ID=" + sender.getId() + ", 用户名=" + sender.getUsername() + ", 角色=" + sender.getRoles());

        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSender(sender);
        notification.setRecipientType(request.getRecipientType());
        notification.setStatus("active");
        
        // 设置是否为全局通知
        if ("ALL".equals(request.getRecipientType())) {
            notification.setIsGlobal(true);
            System.out.println("这是全局通知");
        } else {
            notification.setIsGlobal(false);
        }

        // 如果是班级通知，设置班级
        if ("CLASS".equals(request.getRecipientType())) {
            if (request.getClassId() == null) {
                throw new RuntimeException("班级通知必须指定班级ID");
            }
            com.example.auth.entity.Class classEntity = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("班级不存在: ID=" + request.getClassId()));
            notification.setClassEntity(classEntity);
            System.out.println("班级通知: 班级ID=" + classEntity.getId() + ", 班级名称=" + classEntity.getClassName());
        }

        // 保存通知
        try {
            notification = notificationRepository.save(notification);
            System.out.println("通知保存成功: ID=" + notification.getId());
        } catch (Exception e) {
            System.err.println("保存通知时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("保存通知失败: " + e.getMessage());
        }

        // 为相关用户创建通知关系
        List<User> recipients = new ArrayList<>();
        
        try {
            if ("ALL".equals(request.getRecipientType())) {
                // 全体通知 - 获取所有用户
                recipients = userRepository.findAll();
                System.out.println("全体通知: 找到 " + recipients.size() + " 个用户");
            } else if ("CLASS".equals(request.getRecipientType())) {
                // 班级通知 - 获取班级内所有学生
                Long classId = request.getClassId();
                recipients = classStudentRelationRepository.findStudentsByClassIdAndStatus(classId, "active");
                System.out.println("班级通知: 班级ID=" + classId + ", 找到 " + recipients.size() + " 个学生");
            }
            
            // 创建用户通知关系
            for (User recipient : recipients) {
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(recipient);
                userNotification.setNotification(notification);
                userNotificationRepository.save(userNotification);
            }
            
            System.out.println("成功创建了 " + recipients.size() + " 个用户通知关系");
        } catch (Exception e) {
            System.err.println("创建用户通知关系时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建用户通知关系失败: " + e.getMessage());
        }

        return convertToDto(notification, null);
    }

    @Override
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        // 获取用户对象
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 检查用户角色
        boolean isParent = user.getRoles().stream()
                .anyMatch(role -> "PARENT".equals(role.getName()));
        
        // 获取用户当前所在的班级(只获取active状态的班级)
        List<ClassStudentRelation> activeClassRelations = classStudentRelationRepository.findByStudentAndStatus(user, "active");
        List<Long> activeClassIds = activeClassRelations.stream()
                .map(relation -> relation.getClassEntity().getId())
                .collect(Collectors.toList());
        
        List<UserNotification> userNotifications = userNotificationRepository.findByUserId(userId);
        List<NotificationResponse> result = new ArrayList<>();
        
        for (UserNotification un : userNotifications) {
            Notification notification = un.getNotification();
            
            // 过滤通知：
            // 1. 如果是全局通知(ALL)，则显示
            // 2. 如果是班级通知(CLASS)且用户当前在该班级，则显示
            // 3. 如果是家长通知(PARENT)且用户是家长，则显示
            // 4. 其他情况不显示
            boolean shouldShow = "ALL".equals(notification.getRecipientType()) || 
                    ("CLASS".equals(notification.getRecipientType()) && 
                     notification.getClassEntity() != null && 
                     activeClassIds.contains(notification.getClassEntity().getId())) ||
                    ("PARENT".equals(notification.getRecipientType()) && isParent);
            
            if (shouldShow) {
                result.add(convertToDto(notification, un));
            }
        }
        
        // 按照创建时间降序排序（新的在前面）
        result.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        return result;
    }
    

    @Override
    public List<NotificationResponse> getSentNotifications(Long teacherId) {
        // 使用优化后的查询方法，直接从数据库获取排序后的结果
        List<Notification> notifications = notificationRepository.findBySenderIdOrderByCreateTimeDesc(teacherId);
        
        return notifications.stream()
                .map(n -> convertToDto(n, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        // 使用优化后的查询方法，直接从数据库获取排序后的结果
        List<Notification> notifications = notificationRepository.findAllOrderByCreateTimeDesc();
        
        return notifications.stream()
                .map(n -> convertToDto(n, null))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public NotificationResponse sendNotificationToAllParents(Long adminId, NotificationRequest request) {
        // 验证用户是否为管理员
        if (!isUserAdmin(adminId)) {
            throw new RuntimeException("只有管理员才能执行此操作");
        }
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSender(admin);
        notification.setRecipientType("PARENT");  // 专门为家长设置类型
        notification.setStatus("active");
        notification.setIsGlobal(false);  // 只发给家长，不是全局通知
        
        // 保存通知
        notification = notificationRepository.save(notification);
        
        // 查找家长角色
        Role parentRole = roleRepository.findByName("PARENT");
        if (parentRole == null) {
            throw new RuntimeException("家长角色不存在");
        }
        
        // 获取所有家长用户
        List<User> parents = userRepository.findByRolesContaining(parentRole);
        
        if (parents.isEmpty()) {
            System.out.println("警告: 系统中没有家长用户");
        } else {
            System.out.println("为 " + parents.size() + " 名家长创建通知关系");
        }
        
        // 为所有家长创建通知关系
        for (User parent : parents) {
            UserNotification userNotification = new UserNotification();
            userNotification.setUser(parent);
            userNotification.setNotification(notification);
            userNotificationRepository.save(userNotification);
        }
        
        return convertToDto(notification, null);
    }
    
    @Override
    @Transactional
    public NotificationResponse sendNotificationToAllStudents(Long adminId, NotificationRequest request) {
        // 验证用户是否为管理员
        if (!isUserAdmin(adminId)) {
            throw new RuntimeException("只有管理员才能执行此操作");
        }
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSender(admin);
        notification.setRecipientType("USER");  // 专门为学生设置类型
        notification.setStatus("active");
        notification.setIsGlobal(false);  // 只发给学生，不是全局通知
        
        // 保存通知
        notification = notificationRepository.save(notification);
        
        // 查找学生角色
        Role studentRole = roleRepository.findByName("USER");
        if (studentRole == null) {
            throw new RuntimeException("学生角色不存在");
        }
        
        // 获取所有学生用户
        List<User> students = userRepository.findByRolesContaining(studentRole);
        
        if (students.isEmpty()) {
            System.out.println("警告: 系统中没有学生用户");
        } else {
            System.out.println("为 " + students.size() + " 名学生创建通知关系");
        }
        
        // 为所有学生创建通知关系
        for (User student : students) {
            UserNotification userNotification = new UserNotification();
            userNotification.setUser(student);
            userNotification.setNotification(notification);
            userNotificationRepository.save(userNotification);
        }
        
        return convertToDto(notification, null);
    }
    
    @Override
    @Transactional
    public NotificationResponse sendNotificationToAllTeachers(Long adminId, NotificationRequest request) {
        // 验证用户是否为管理员
        if (!isUserAdmin(adminId)) {
            throw new RuntimeException("只有管理员才能执行此操作");
        }
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSender(admin);
        notification.setRecipientType("TEACHER");  // 专门为教师设置类型
        notification.setStatus("active");
        notification.setIsGlobal(false);  // 只发给教师，不是全局通知
        
        // 保存通知
        notification = notificationRepository.save(notification);
        
        // 查找教师角色（这里假设角色名为supervisor，实际中需要根据系统中定义的角色名进行调整）
        Role teacherRole = roleRepository.findByName("SUPERVISOR");
        if (teacherRole == null) {
            throw new RuntimeException("教师角色不存在");
        }
        
        // 获取所有教师用户
        List<User> teachers = userRepository.findByRolesContaining(teacherRole);
        
        if (teachers.isEmpty()) {
            System.out.println("警告: 系统中没有教师用户");
        } else {
            System.out.println("为 " + teachers.size() + " 名教师创建通知关系");
        }
        
        // 为所有教师创建通知关系
        for (User teacher : teachers) {
            UserNotification userNotification = new UserNotification();
            userNotification.setUser(teacher);
            userNotification.setNotification(notification);
            userNotificationRepository.save(userNotification);
        }
        
        return convertToDto(notification, null);
    }
    
    @Override
    public boolean isUserAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // 检查用户角色是否包含admin
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        
        // 遍历用户的角色集合，检查是否包含admin角色
        return user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
    }

    @Override
    public List<NotificationResponse> getRecentNotifications(Integer limit) {
        // 使用分页查询获取最近的通知
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Notification> notifications = notificationRepository.findRecentNotifications(pageRequest);
        
        return notifications.stream()
                .map(n -> convertToDto(n, null))
                .collect(Collectors.toList());
    }

    private NotificationResponse convertToDto(Notification notification, UserNotification userNotification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setSenderId(notification.getSender().getId());
        
        // 设置发送者信息
        User sender = notification.getSender();
        response.setSenderName(sender.getRealName() != null ? sender.getRealName() : sender.getUsername());
        response.setSenderAvatar(sender.getAvatar());
        
        response.setRecipientType(notification.getRecipientType());
        response.setCreateTime(notification.getCreateTime());
        response.setIsGlobal(notification.getIsGlobal());
        
        // 如果是班级通知，设置班级信息
        if ("CLASS".equals(notification.getRecipientType()) && notification.getClassEntity() != null) {
            response.setClassId(notification.getClassEntity().getId());
            response.setClassName(notification.getClassEntity().getClassName());
        }
        
        return response;
    }
} 