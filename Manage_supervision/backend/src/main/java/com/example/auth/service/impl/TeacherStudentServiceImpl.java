package com.example.auth.service.impl;

import com.example.auth.dto.PageResponse;
import com.example.auth.dto.TeacherStudentDTO;
import com.example.auth.dto.TeacherWithStudentsDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.Role;
import com.example.auth.entity.TeacherStudentRelation;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.TeacherStudentRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.TeacherStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeacherStudentServiceImpl implements TeacherStudentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeacherStudentRepository teacherStudentRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResponse<UserDTO> getAllTeachers(int page, int size, String keyword) {
        // 获取教师角色
        Role teacherRole = roleRepository.findByName("SUPERVISOR");
        if (teacherRole == null) {
            throw new RuntimeException("教师角色不存在");
        }
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> teachers;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            teachers = userRepository.findByRolesContainingAndUsernameContainingOrRealNameContaining(
                    teacherRole, keyword, keyword, pageable);
        } else {
            teachers = userRepository.findByRolesContaining(teacherRole, pageable);
        }
        
        List<UserDTO> teacherDTOs = teachers.getContent().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(teacherDTOs, teachers.getTotalElements(), page, size);
    }

    @Override
    public PageResponse<UserDTO> getAllStudents(int page, int size, String keyword) {
        // 获取学生角色
        Role studentRole = roleRepository.findByName("USER");
        if (studentRole == null) {
            throw new RuntimeException("学生角色不存在");
        }
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> students;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            students = userRepository.findByRolesContainingAndUsernameContainingOrRealNameContaining(
                    studentRole, keyword, keyword, pageable);
        } else {
            students = userRepository.findByRolesContaining(studentRole, pageable);
        }
        
        List<UserDTO> studentDTOs = students.getContent().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(studentDTOs, students.getTotalElements(), page, size);
    }

    @Override
    public List<UserDTO> getUnassignedStudents() {
        // 获取学生角色ID
        Role studentRole = roleRepository.findByName("USER");
        if (studentRole == null) {
            throw new RuntimeException("学生角色不存在");
        }
        
        // 查询未分配给任何教师的学生
        List<User> unassignedStudents = teacherStudentRepository.findUnassignedStudentsByRoleId(studentRole.getId());
        
        return unassignedStudents.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getStudentsByTeacher(Long teacherId) {
        Optional<User> optionalTeacher = userRepository.findById(teacherId);
        if (optionalTeacher.isPresent()) {
            User teacher = optionalTeacher.get();
            List<User> students = teacherStudentRepository.findActiveStudentsByTeacher(teacher);
            
            return students.stream()
                    .map(this::convertToUserDTO)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public boolean assignStudentsToTeacher(Long teacherId, List<Long> studentIds) {
        Optional<User> optionalTeacher = userRepository.findById(teacherId);
        if (!optionalTeacher.isPresent()) {
            System.out.println("教师不存在: " + teacherId);
            return false;
        }
        
        User teacher = optionalTeacher.get();
        List<User> students = userRepository.findAllById(studentIds);
        
        if (students.isEmpty() || students.size() != studentIds.size()) {
            System.out.println("找不到所有学生: 要求分配 " + studentIds.size() + " 个学生，但只找到 " + students.size() + " 个");
            return false;
        }
        
        System.out.println("开始分配学生给教师 " + teacher.getRealName() + " (ID: " + teacher.getId() + ")");
        
        for (User student : students) {
            System.out.println("处理学生: " + student.getRealName() + " (ID: " + student.getId() + ")");
            
            // 检查该学生是否已分配给教师
            Optional<TeacherStudentRelation> existingRelation = 
                    teacherStudentRepository.findByTeacherAndStudent(teacher, student);
            
            if (existingRelation.isPresent()) {
                // 如果关系存在但状态为inactive，则更新为active
                TeacherStudentRelation relation = existingRelation.get();
                System.out.println("已存在教师-学生关系, 状态: " + relation.getStatus());
                
                if (!"active".equals(relation.getStatus())) {
                    relation.setStatus("active");
                    relation.setAssignTime(LocalDateTime.now());
                    teacherStudentRepository.save(relation);
                    System.out.println("已更新教师-学生关系状态为active");
                } else {
                    System.out.println("教师-学生关系已经是active状态，无需更新");
                }
            } else {
                // 如果关系不存在，则创建新关系
                TeacherStudentRelation relation = new TeacherStudentRelation();
                relation.setTeacher(teacher);
                relation.setStudent(student);
                relation.setStatus("active");
                relation.setAssignTime(LocalDateTime.now());
                teacherStudentRepository.save(relation);
                System.out.println("已创建新的教师-学生关系");
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean unassignStudent(Long teacherId, Long studentId) {
        Optional<User> optionalTeacher = userRepository.findById(teacherId);
        Optional<User> optionalStudent = userRepository.findById(studentId);
        
        if (!optionalTeacher.isPresent() || !optionalStudent.isPresent()) {
            return false;
        }
        
        User teacher = optionalTeacher.get();
        User student = optionalStudent.get();
        
        Optional<TeacherStudentRelation> optionalRelation = 
                teacherStudentRepository.findByTeacherAndStudent(teacher, student);
        
        if (optionalRelation.isPresent()) {
            TeacherStudentRelation relation = optionalRelation.get();
            relation.setStatus("inactive");
            teacherStudentRepository.save(relation);
            return true;
        }
        
        return false;
    }

    @Override
    public TeacherWithStudentsDTO getTeacherWithStudents(Long teacherId) {
        Optional<User> optionalTeacher = userRepository.findById(teacherId);
        if (!optionalTeacher.isPresent()) {
            return null;
        }
        
        User teacher = optionalTeacher.get();
        List<User> students = teacherStudentRepository.findActiveStudentsByTeacher(teacher);
        
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
        Optional<TeacherStudentRelation> optionalRelation = teacherStudentRepository.findById(relationId);
        if (!optionalRelation.isPresent()) {
            return null;
        }
        
        TeacherStudentRelation relation = optionalRelation.get();
        return convertToTeacherStudentDTO(relation);
    }

    @Override
    public boolean isTeacherAssignedToStudent(Long teacherId, Long studentId) {
        Optional<User> optionalTeacher = userRepository.findById(teacherId);
        Optional<User> optionalStudent = userRepository.findById(studentId);
        
        if (!optionalTeacher.isPresent() || !optionalStudent.isPresent()) {
            return false;
        }
        
        User teacher = optionalTeacher.get();
        User student = optionalStudent.get();
        
        Optional<TeacherStudentRelation> optionalRelation = 
                teacherStudentRepository.findByTeacherAndStudent(teacher, student);
        
        return optionalRelation.isPresent() && "active".equals(optionalRelation.get().getStatus());
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
        dto.setCreateTime(user.getCreateTime() != null ? 
                user.getCreateTime().format(formatter) : null);
        
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        dto.setRoles(roleNames);
        
        return dto;
    }

    private TeacherStudentDTO convertToTeacherStudentDTO(TeacherStudentRelation relation) {
        TeacherStudentDTO dto = new TeacherStudentDTO();
        dto.setId(relation.getId());
        dto.setTeacherId(relation.getTeacher().getId());
        dto.setTeacherName(relation.getTeacher().getRealName());
        dto.setTeacherUserNumber(relation.getTeacher().getUserNumber());
        dto.setStudentId(relation.getStudent().getId());
        dto.setStudentName(relation.getStudent().getRealName());
        dto.setStudentUserNumber(relation.getStudent().getUserNumber());
        dto.setStatus(relation.getStatus());
        dto.setAssignTime(relation.getAssignTime() != null ? 
                relation.getAssignTime().format(formatter) : null);
        
        return dto;
    }
} 