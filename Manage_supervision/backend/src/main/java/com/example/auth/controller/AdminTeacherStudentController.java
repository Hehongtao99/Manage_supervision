package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.StudentAssignmentDTO;
import com.example.auth.dto.TeacherWithStudentsDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.ParentChildRelation;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ParentService;
import com.example.auth.service.TeacherStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminTeacherStudentController {

    @Autowired
    private TeacherStudentService teacherStudentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ParentService parentService;

    // 获取所有教师列表（带分页）
    @GetMapping("/teachers")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getAllTeachers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<UserDTO> response = teacherStudentService.getAllTeachers(page, size, keyword);
        return ResponseEntity.ok(response);
    }

    // 获取所有学生列表（带分页）
    @GetMapping("/students")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getAllStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<UserDTO> response = teacherStudentService.getAllStudents(page, size, keyword);
        return ResponseEntity.ok(response);
    }

    // 获取未分配学生列表
    @GetMapping("/students/unassigned")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getUnassignedStudents() {
        List<UserDTO> students = teacherStudentService.getUnassignedStudents();
        return ResponseEntity.ok(students);
    }

    // 获取特定教师的学生列表
    @GetMapping("/teachers/{teacherId}/students")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getStudentsByTeacher(@PathVariable Long teacherId) {
        List<UserDTO> students = teacherStudentService.getStudentsByTeacher(teacherId);
        return ResponseEntity.ok(students);
    }
    
    // 获取特定学生的教师列表
    @GetMapping("/teachers/student/{studentId}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getTeachersByStudent(@PathVariable Long studentId) {
        Optional<User> optionalStudent = userRepository.findById(studentId);
        if (optionalStudent.isPresent()) {
            User student = optionalStudent.get();
            
            Role teacherRole = roleRepository.findByName("SUPERVISOR");
            if (teacherRole == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            // 获取与该学生相关的教师
            List<User> teachers = userRepository.findByRolesContaining(teacherRole);
            
            // 转换为DTO并过滤出已经分配给该学生的教师
            List<UserDTO> teacherDTOs = new ArrayList<>();
            for (User teacher : teachers) {
                if (teacherStudentService.isTeacherAssignedToStudent(teacher.getId(), studentId)) {
                    UserDTO teacherDTO = new UserDTO();
                    teacherDTO.setId(teacher.getId());
                    teacherDTO.setUsername(teacher.getUsername());
                    teacherDTO.setRealName(teacher.getRealName());
                    teacherDTO.setUserNumber(teacher.getUserNumber());
                    teacherDTO.setEmail(teacher.getEmail());
                    teacherDTO.setPhone(teacher.getPhone());
                    teacherDTOs.add(teacherDTO);
                }
            }
            
            return ResponseEntity.ok(teacherDTOs);
        }
        
        return ResponseEntity.ok(new ArrayList<>());
    }

    // 分配学生给教师
    @PostMapping("/teachers/assign-students")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> assignStudentsToTeacher(@RequestBody StudentAssignmentDTO assignmentDTO) {
        boolean success = teacherStudentService.assignStudentsToTeacher(
                assignmentDTO.getTeacherId(), assignmentDTO.getStudentIds());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "学生分配成功" : "学生分配失败");
        
        return ResponseEntity.ok(response);
    }

    // 取消分配学生
    @PostMapping("/teachers/{teacherId}/unassign/{studentId}")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> unassignStudent(
            @PathVariable Long teacherId, @PathVariable Long studentId) {
        
        boolean success = teacherStudentService.unassignStudent(teacherId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "取消分配成功" : "取消分配失败");
        
        return ResponseEntity.ok(response);
    }

    // 获取教师详情（包含学生列表）
    @GetMapping("/teachers/{teacherId}/details")
    @RequireRole("ADMIN")
    public ResponseEntity<TeacherWithStudentsDTO> getTeacherDetails(@PathVariable Long teacherId) {
        TeacherWithStudentsDTO teacher = teacherStudentService.getTeacherWithStudents(teacherId);
        return ResponseEntity.ok(teacher);
    }

    // 获取特定学生的家长列表
    @GetMapping("/students/{studentId}/parents")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getParentsByStudent(@PathVariable Long studentId) {
        try {
            Optional<User> optionalStudent = userRepository.findById(studentId);
            if (!optionalStudent.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "学生不存在"));
            }
            
            // 获取学生的已确认家长关系
            List<ParentChildRelation> relations = parentService.getConfirmedParentRelations(studentId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> parentDTOs = relations.stream()
                .map(relation -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("relationId", relation.getId());
                    
                    // 家长信息
                    User parent = relation.getParent();
                    dto.put("id", parent.getId());
                    dto.put("username", parent.getUsername());
                    dto.put("realName", parent.getRealName() != null ? parent.getRealName() : parent.getUsername());
                    dto.put("userNumber", parent.getUserNumber());
                    dto.put("email", parent.getEmail());
                    dto.put("phone", parent.getPhone());
                    
                    // 关系信息
                    dto.put("relationType", relation.getRelationType());
                    dto.put("status", relation.getStatus());
                    dto.put("createTime", relation.getCreateTime());
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("parents", parentDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取家长列表失败: " + e.getMessage()));
        }
    }
} 