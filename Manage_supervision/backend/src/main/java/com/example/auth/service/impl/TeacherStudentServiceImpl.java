package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.TeacherStudentDTO;
import com.example.auth.model.dto.TeacherWithStudentsDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.TeacherStudentRelation;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.TeacherStudentMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.TeacherStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherStudentServiceImpl implements TeacherStudentService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private TeacherStudentMapper teacherStudentMapper;

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
    public List<UserDTO> getAllTeachersWithoutPaging() {
        // 获取教师角色
        Role teacherRole = roleMapper.findByName("SUPERVISOR");
        if (teacherRole == null) {
            throw new RuntimeException("教师角色不存在");
        }
        
        // 查询所有教师
        List<User> teachers = userMapper.findByRoleId(teacherRole.getId());
        
        // 转换为DTO
        return teachers.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
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
    public List<UserDTO> getUnassignedStudents() {
        // 获取学生角色ID
        Role studentRole = roleMapper.findByName("USER");
        if (studentRole == null) {
            throw new RuntimeException("学生角色不存在");
        }
        
        // 查询未分配给任何教师的学生ID
        List<Long> unassignedStudentIds = teacherStudentMapper.findUnassignedStudentIdsByRoleId(studentRole.getId());
        
        if (unassignedStudentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据ID查询学生详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, unassignedStudentIds);
        List<User> unassignedStudents = userMapper.selectList(queryWrapper);
        
        return unassignedStudents.stream()
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
        // 检查教师和学生是否存在
        User teacher = userMapper.selectById(teacherId);
        User student = userMapper.selectById(studentId);
        
        if (teacher == null || student == null) {
            return false;
        }
        
        // 查询是否存在活跃的师生关系
        return teacherStudentMapper.existsByTeacherIdAndStudentId(teacherId, studentId);
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
} 