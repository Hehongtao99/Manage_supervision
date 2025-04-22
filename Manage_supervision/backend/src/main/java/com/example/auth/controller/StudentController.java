package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.UserDTO;
import com.example.auth.dto.ClassTeacherRelationDTO;
import com.example.auth.entity.User;
import com.example.auth.service.ClassService;
import com.example.auth.service.ClassTeacherService;
import com.example.auth.service.TeacherStudentService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private TeacherStudentService teacherStudentService;
    
    @Autowired
    private ClassTeacherService classTeacherService;

    /**
     * 获取当前登录用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        token = token.substring(7); // 移除"Bearer "前缀
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("从token中获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取学生的班级信息
     */
    @GetMapping("/my-class")
    @RequireRole("USER")
    public ResponseEntity<?> getMyClass(HttpServletRequest request) {
        try {
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
            }
            
            // 从班级-学生关系中查询该学生的班级
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> classList = classService.getClassesByStudentId(studentId);
            
            if (classList.isEmpty()) {
                result.put("hasClass", false);
                result.put("message", "您当前没有被分配到任何班级");
            } else {
                result.put("hasClass", true);
                result.put("classes", classList);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取学生班级信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取班级信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生的教师信息
     */
    @GetMapping("/my-teachers")
    @RequireRole("USER")
    public ResponseEntity<?> getMyTeachers(HttpServletRequest request) {
        try {
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
            }
            
            // 获取与该学生关联的老师列表
            List<UserDTO> teachers = teacherStudentService.getTeachersByStudentId(studentId);
            
            Map<String, Object> result = new HashMap<>();
            if (teachers.isEmpty()) {
                result.put("hasTeachers", false);
                result.put("message", "您当前没有被分配教师");
            } else {
                result.put("hasTeachers", true);
                result.put("teachers", teachers);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取学生教师信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取教师信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取学生班级中的老师信息
     */
    @GetMapping("/class-teachers")
    @RequireRole("USER")
    public ResponseEntity<?> getClassTeachers(HttpServletRequest request) {
        try {
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
            }
            
            // 获取学生的班级列表
            List<Map<String, Object>> classList = classService.getClassesByStudentId(studentId);
            
            if (classList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "hasClassTeachers", false,
                    "message", "您当前没有被分配到任何班级"
                ));
            }
            
            // 存储每个班级的教师信息
            List<Map<String, Object>> classTeachersList = new ArrayList<>();
            
            for (Map<String, Object> classInfo : classList) {
                Long classId = ((Number) classInfo.get("id")).longValue();
                String className = (String) classInfo.get("className");
                
                // 获取该班级的所有教师
                List<ClassTeacherRelationDTO> teacherRelations = 
                        classTeacherService.getTeachersByClassId(classId);
                
                Map<String, Object> classTeacherInfo = new HashMap<>();
                classTeacherInfo.put("classId", classId);
                classTeacherInfo.put("className", className);
                classTeacherInfo.put("teachers", teacherRelations);
                
                classTeachersList.add(classTeacherInfo);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasClassTeachers", !classTeachersList.isEmpty());
            result.put("classTeachers", classTeachersList);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取班级教师信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取班级教师信息失败：" + e.getMessage());
        }
    }
} 