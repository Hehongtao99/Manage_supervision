package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.StudentDTO;
import com.example.auth.model.dto.StudentDetailDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.User;
import com.example.auth.model.College;
import com.example.auth.model.Major;
import com.example.auth.model.ClassEntity;
import com.example.auth.model.TeacherClassRelation;
import com.example.auth.service.UserService;
import com.example.auth.service.CollegeService;
import com.example.auth.service.MajorService;
import com.example.auth.service.ClassService;
import com.example.auth.mapper.TeacherClassRelationMapper;
import com.example.auth.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling supervisor-related requests
 */
@Slf4j
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private ClassService classService;

    @Autowired
    private TeacherClassRelationMapper teacherClassRelationMapper;

    /**
     * Get all students list
     */
    @GetMapping("/students")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getAllStudents() {
        logger.info("Supervisor requested all students list");
        try {
            List<StudentDTO> students = userService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Failed to get students list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get students list: " + e.getMessage()));
        }
    }

    /**
     * Get students assigned to supervisor
     */
    @GetMapping("/students/assigned")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getAssignedStudents(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long supervisorId = jwtUtil.getUserIdFromToken(token);
            logger.info("Supervisor requested their assigned students list, ID: {}", supervisorId);
            
            List<UserDTO> students = userService.getStudentsByTeacher(supervisorId);
            if (!students.isEmpty()) {
                logger.info("学生数据示例: id={}, name={}, userNumber={}", 
                            students.get(0).getId(), 
                            students.get(0).getRealName(), 
                            students.get(0).getUserNumber());
            } else {
                logger.info("未找到分配给督导员ID: {}的学生", supervisorId);
            }
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Failed to get assigned students list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get assigned students list: " + e.getMessage()));
        }
    }

    /**
     * Get student details
     */
    @GetMapping("/students/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getStudentDetails(@PathVariable Long id) {
        logger.info("Supervisor requested student details, ID: {}", id);
        try {
            StudentDetailDTO student = userService.getStudentDetails(id);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            logger.error("Failed to get student details", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get student details: " + e.getMessage()));
        }
    }

    /**
     * Update student status
     */
    @PutMapping("/students/{id}/status")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateStudentStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        logger.info("Supervisor requested to update student status, ID: {}, New status: {}", id, status);
        
        if (status == null || (!status.equals("active") && !status.equals("inactive"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status value, must be 'active' or 'inactive'"));
        }
        
        try {
            boolean success = userService.updateStudentStatus(id, status);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Student status has been updated to: " + status,
                    "status", status
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to update student status, student may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to update student status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update student status: " + e.getMessage()));
        }
    }

    /**
     * Delete student
     */
    @DeleteMapping("/students/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        logger.info("Supervisor requested to delete student, ID: {}", id);
        try {
            boolean success = userService.deleteStudent(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Student has been successfully deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete student, student may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete student", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete student: " + e.getMessage()));
        }
    }

    /**
     * 获取当前教师的详细信息，包括学院、专业和班级信息
     */
    @GetMapping("/profile/details")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<Map<String, Object>> getTeacherProfileDetails(HttpServletRequest request) {
        try {
            // 从token中获取当前用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "无效的token"));
            }
            
            // 获取用户基本信息
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("userNumber", user.getUserNumber());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("bio", user.getBio());
            result.put("collegeId", user.getCollegeId());
            result.put("majorId", user.getMajorId());
            result.put("classId", user.getClassId());
            
            // 获取学院名称
            if (user.getCollegeId() != null) {
                try {
                    College college = collegeService.getById(user.getCollegeId());
                    if (college != null) {
                        result.put("collegeName", college.getCollegeName());
                    }
                } catch (Exception e) {
                    log.warn("获取学院信息失败: {}", e.getMessage());
                }
            }
            
            // 获取专业名称
            if (user.getMajorId() != null) {
                try {
                    Major major = majorService.getById(user.getMajorId());
                    if (major != null) {
                        result.put("majorName", major.getMajorName());
                    }
                } catch (Exception e) {
                    log.warn("获取专业信息失败: {}", e.getMessage());
                }
            }
            
            // 获取教师的班级关系
            LambdaQueryWrapper<TeacherClassRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeacherClassRelation::getTeacherId, userId)
                       .eq(TeacherClassRelation::getStatus, "active");
            List<TeacherClassRelation> relations = teacherClassRelationMapper.selectList(queryWrapper);
            
            List<Map<String, Object>> classes = new ArrayList<>();
            for (TeacherClassRelation relation : relations) {
                try {
                    ClassEntity classEntity = classService.getById(relation.getClassId());
                    if (classEntity != null) {
                        Map<String, Object> classInfo = new HashMap<>();
                        classInfo.put("id", classEntity.getId());
                        classInfo.put("className", classEntity.getClassName());
                        classInfo.put("classCode", classEntity.getClassCode());
                        classInfo.put("grade", classEntity.getGrade());
                        classes.add(classInfo);
                    }
                } catch (Exception e) {
                    log.warn("获取班级信息失败: {}", e.getMessage());
                }
            }
            result.put("classes", classes);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取教师详细信息失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取教师详细信息失败: " + e.getMessage()));
        }
    }
}