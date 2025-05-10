package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.StudentDTO;
import com.example.auth.model.dto.StudentDetailDTO;
import com.example.auth.model.dto.TeacherStudentCourseDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.service.TeacherStudentService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling supervisor-related requests
 */
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherStudentService teacherStudentService;

    @Autowired
    private JwtUtil jwtUtil;

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
     * Get students with course information assigned to supervisor
     */
    @GetMapping("/students/with-courses")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getStudentsWithCourses(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long supervisorId = jwtUtil.getUserIdFromToken(token);
            logger.info("Supervisor requested their students with course information, ID: {}", supervisorId);
            
            List<TeacherStudentCourseDTO> studentsWithCourses = teacherStudentService.getStudentsWithCoursesByTeacher(supervisorId);
            return ResponseEntity.ok(studentsWithCourses);
        } catch (Exception e) {
            logger.error("Failed to get students with course information", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get students with course information: " + e.getMessage()));
        }
    }
}