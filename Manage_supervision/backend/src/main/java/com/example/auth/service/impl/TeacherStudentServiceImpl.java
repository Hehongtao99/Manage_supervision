package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.TeacherStudentMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.mapper.CourseApplicationMapper;
import com.example.auth.model.dto.*;
import com.example.auth.model.entity.*;
import com.example.auth.service.TeacherStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherStudentServiceImpl implements TeacherStudentService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private TeacherStudentMapper teacherStudentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CourseApplicationMapper courseApplicationMapper;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResponse<UserDTO> getAllTeachers(int page, int size, String keyword) {
        // 获取教师角色
        Role teacherRole = roleMapper.findByName("SUPERVISOR");
        if (teacherRole == null) {
            throw new RuntimeException("教师角色不存在");
        }
        
        // 创建MyBatis-Plus分页对象，注意MyBatis-Plus是从0开始
        Page<User> pageParam = new Page<>(page - 1, size);
        
        // 使用UserMapper中的方法进行分页查询
        IPage<User> resultPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            resultPage = userMapper.findByConditions(
                    pageParam, 
                    keyword, 
                    "SUPERVISOR", 
                    null);
        } else {
            resultPage = userMapper.findByRoleIdPage(pageParam, teacherRole.getId());
        }
        
        List<UserDTO> teacherDTOs = resultPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(teacherDTOs, resultPage.getTotal(), page, size);
    }

    @Override
    public PageResponse<UserDTO> getAllStudents(int page, int size, String keyword) {
        // 获取学生角色
        Role studentRole = roleMapper.findByName("USER");
        if (studentRole == null) {
            throw new RuntimeException("学生角色不存在");
        }
        
        // 创建MyBatis-Plus分页对象
        Page<User> pageParam = new Page<>(page - 1, size);
        
        // 使用UserMapper中的方法进行分页查询
        IPage<User> resultPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            resultPage = userMapper.findByConditions(
                    pageParam, 
                    keyword, 
                    "USER", 
                    null);
        } else {
            resultPage = userMapper.findByRoleIdPage(pageParam, studentRole.getId());
        }
        
        List<UserDTO> studentDTOs = resultPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(studentDTOs, resultPage.getTotal(), page, size);
    }

    @Override
    public List<UserDTO> getUnassignedStudents(int pageSize, int pageNum) {
        // 获取学生角色ID
        Role studentRole = roleMapper.findByName("USER");
        if (studentRole == null) {
            throw new RuntimeException("学生角色不存在");
        }
        
        // 获取未分配给教师的学生ID列表
        List<Long> unassignedStudentIds = teacherStudentMapper.findUnassignedStudentIdsByRoleId(studentRole.getId());
        
        if (unassignedStudentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 创建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        
        // 查询学生详细信息，带分页
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, unassignedStudentIds)
                   .eq(User::getStatus, "active")
                   .orderByDesc(User::getCreateTime);
        
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换成DTO
        return userPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getStudentsByTeacher(Long teacherId) {
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            return new ArrayList<>();
        }
        
        // 获取该教师的所有活跃学生ID
        List<Long> studentIds = teacherStudentMapper.findActiveStudentIdsByTeacherId(teacherId);
        
        if (studentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询学生详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, studentIds);
        List<User> students = userMapper.selectList(queryWrapper);
        
        return students.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignStudentsToTeacher(Long teacherId, List<Long> studentIds) {
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            return false;
        }
        
        // 验证所有学生ID是否有效
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, studentIds);
        long count = userMapper.selectCount(queryWrapper);
        
        if (count != studentIds.size()) {
            return false;
        }
        
        for (Long studentId : studentIds) {
            // 检查该学生是否已分配给教师
            boolean exists = teacherStudentMapper.existsByTeacherIdAndStudentId(teacherId, studentId);
            if (!exists) {
                TeacherStudentRelation relation = new TeacherStudentRelation();
                relation.setTeacherId(teacherId);
                relation.setStudentId(studentId);
                relation.setStatus("active");
                relation.setAssignTime(LocalDateTime.now());
                teacherStudentMapper.insert(relation);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean unassignStudent(Long teacherId, Long studentId) {
        // 检查教师和学生是否存在
        User teacher = userMapper.selectById(teacherId);
        User student = userMapper.selectById(studentId);
        
        if (teacher == null || student == null) {
            return false;
        }
        
        // 查询关系记录
        TeacherStudentRelation relation = teacherStudentMapper.findByTeacherIdAndStudentId(teacherId, studentId);
        
        if (relation != null) {
            relation.setStatus("inactive");
            teacherStudentMapper.updateById(relation);
            return true;
        }
        
        return false;
    }

    @Override
    public TeacherWithStudentsDTO getTeacherWithStudents(Long teacherId) {
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            return null;
        }
        
        // 获取该教师的所有活跃学生ID
        List<Long> studentIds = teacherStudentMapper.findActiveStudentIdsByTeacherId(teacherId);
        
        // 查询学生详细信息
        List<User> students = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(User::getId, studentIds);
            students = userMapper.selectList(queryWrapper);
        }
        
        TeacherWithStudentsDTO dto = new TeacherWithStudentsDTO();
        dto.setId(teacher.getId());
        dto.setUsername(teacher.getUsername());
        dto.setRealName(teacher.getRealName());
        dto.setUserNumber(teacher.getUserNumber());
        dto.setEmail(teacher.getEmail());
        dto.setPhone(teacher.getPhone());
        dto.setStudents(students.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public TeacherStudentDTO getRelationDetail(Long relationId) {
        TeacherStudentRelation relation = teacherStudentMapper.selectById(relationId);
        if (relation == null) {
            return null;
        }
        
        // 补充教师和学生信息
        User teacher = userMapper.selectById(relation.getTeacherId());
        User student = userMapper.selectById(relation.getStudentId());
        
        relation.setTeacher(teacher);
        relation.setStudent(student);
        
        return convertToTeacherStudentDTO(relation);
    }

    @Override
    public boolean isTeacherAssignedToStudent(Long teacherId, Long studentId) {
        return teacherStudentMapper.existsByTeacherIdAndStudentId(teacherId, studentId);
    }
    
    @Override
    public List<TeacherStudentCourseDTO> getStudentsWithCoursesByTeacher(Long teacherId) {
        System.out.println("开始获取教师(ID:" + teacherId + ")下的学生和课程信息");
        
        // 查询该教师的订单，只获取学生ID
        LambdaQueryWrapper<Order> studentIdQueryWrapper = new LambdaQueryWrapper<>();
        studentIdQueryWrapper.eq(Order::getTeacherId, teacherId)
                             .select(Order::getStudentId)
                             .groupBy(Order::getStudentId);
        
        List<Order> studentOrders = orderMapper.selectList(studentIdQueryWrapper);
        
        System.out.println("查询到的学生订单数量: " + studentOrders.size());
        
        if (studentOrders.isEmpty()) {
            System.out.println("没有找到该教师下的学生订单，返回空列表");
            return new ArrayList<>();
        }
        
        // 提取所有学生ID
        List<Long> studentIds = studentOrders.stream()
                                            .map(Order::getStudentId)
                                            .distinct()
                                            .collect(Collectors.toList());
        
        System.out.println("学生ID列表: " + studentIds);
        
        // 获取学生详细信息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, studentIds);
        List<User> students = userMapper.selectList(userWrapper);
        
        System.out.println("查询到的学生数量: " + students.size());
        if (!students.isEmpty()) {
            System.out.println("第一个学生信息示例: ID=" + students.get(0).getId() + 
                            ", 姓名=" + students.get(0).getRealName() + 
                            ", 学号=" + students.get(0).getUserNumber());
        }
        
        Map<Long, TeacherStudentCourseDTO> studentDtoMap = new HashMap<>();
        
        // 转换为DTO对象
        for (User student : students) {
            TeacherStudentCourseDTO dto = new TeacherStudentCourseDTO();
            dto.setId(student.getId());
            dto.setUsername(student.getUsername());
            dto.setRealName(student.getRealName());
            dto.setNickname(student.getNickname());
            dto.setEmail(student.getEmail());
            dto.setPhone(student.getPhone());
            dto.setUserNumber(student.getUserNumber());
            dto.setAvatar(student.getAvatar());
            dto.setStatus(student.getStatus());
            dto.setCourses(new ArrayList<>());
            
            studentDtoMap.put(student.getId(), dto);
        }
        
        // 获取每个学生购买的所有课程
        for (Long studentId : studentIds) {
            // 使用新的方法直接获取关联了课程信息的订单
            List<Order> studentCourseOrders = orderMapper.findOrdersByTeacherAndStudentId(teacherId, studentId);
            
            System.out.println("学生(ID:" + studentId + ")的订单数量: " + studentCourseOrders.size());
            
            TeacherStudentCourseDTO studentDto = studentDtoMap.get(studentId);
            if (studentDto != null) {
                for (Order order : studentCourseOrders) {
                    System.out.println("  订单信息: ID=" + order.getId() + 
                                    ", 课程ID=" + order.getCourseId() + 
                                    ", 课程标题=" + order.getCourseTitle() + 
                                    ", 状态=" + order.getStatus());
                    
                    // 如果订单的课程标题为空，尝试查询课程表获取标题
                    if (order.getCourseTitle() == null && order.getCourseId() != null) {
                        try {
                            CourseApplication courseApp = courseApplicationMapper.selectById(order.getCourseId());
                            if (courseApp != null) {
                                order.setCourseTitle(courseApp.getTitle());
                                order.setCourseSubject(courseApp.getSubject());
                                System.out.println("  从课程表获取标题: " + courseApp.getTitle());
                            } else {
                                order.setCourseTitle("未知课程");
                                System.out.println("  未找到课程信息, 设置为'未知课程'");
                            }
                        } catch (Exception e) {
                            System.out.println("  查询课程信息出错: " + e.getMessage());
                            order.setCourseTitle("未知课程");
                        }
                    }
                    
                    TeacherStudentCourseDTO.StudentCourseInfo courseInfo = new TeacherStudentCourseDTO.StudentCourseInfo();
                    courseInfo.setOrderId(order.getId());
                    courseInfo.setCourseId(order.getCourseId());
                    courseInfo.setCourseTitle(order.getCourseTitle());
                    courseInfo.setCourseSubject(order.getCourseSubject());
                    courseInfo.setPrice(order.getPrice());
                    courseInfo.setHours(order.getHours());
                    courseInfo.setTotalAmount(order.getTotalAmount());
                    courseInfo.setStatus(order.getStatus());
                    courseInfo.setCreateTime(order.getCreateTime());
                    
                    studentDto.getCourses().add(courseInfo);
                }
            } else {
                System.out.println("  未找到学生(ID:" + studentId + ")的DTO对象");
            }
        }
        
        List<TeacherStudentCourseDTO> result = new ArrayList<>(studentDtoMap.values());
        System.out.println("返回结果学生数量: " + result.size());
        return result;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setUserNumber(user.getUserNumber());
        dto.setAvatar(user.getAvatar());
        return dto;
    }

    private TeacherStudentDTO convertToTeacherStudentDTO(TeacherStudentRelation relation) {
        TeacherStudentDTO dto = new TeacherStudentDTO();
        dto.setId(relation.getId());
        
        if (relation.getTeacher() != null) {
            dto.setTeacherId(relation.getTeacher().getId());
            dto.setTeacherName(relation.getTeacher().getRealName() != null 
                    ? relation.getTeacher().getRealName() 
                    : relation.getTeacher().getUsername());
        } else {
            dto.setTeacherId(relation.getTeacherId());
        }
        
        if (relation.getStudent() != null) {
            dto.setStudentId(relation.getStudent().getId());
            dto.setStudentName(relation.getStudent().getRealName() != null 
                    ? relation.getStudent().getRealName() 
                    : relation.getStudent().getUsername());
        } else {
            dto.setStudentId(relation.getStudentId());
        }
        
        dto.setStatus(relation.getStatus());
        dto.setAssignTime(relation.getAssignTime() != null 
                ? relation.getAssignTime().format(formatter) 
                : null);
        
        return dto;
    }

    private List<Order> getStudentOrders(Long studentId, int pageSize, int pageNum) {
        LambdaQueryWrapper<Order> studentOrderWrapper = new LambdaQueryWrapper<>();
        studentOrderWrapper.eq(Order::getStudentId, studentId);
        // 创建分页对象
        Page<Order> page = new Page<>(pageNum, pageSize);
        // 使用分页查询
        Page<Order> orderPage = orderMapper.selectPage(page, studentOrderWrapper);
        return orderPage.getRecords();
    }
} 