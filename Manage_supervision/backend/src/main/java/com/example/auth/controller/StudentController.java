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
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.auth.dto.ClassDTO;
import com.example.auth.entity.Class;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.ParentChildRelation;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.ClassStudentRelationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ParentService;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassStudentRelationRepository classStudentRepository;

    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private ParentService parentService;

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

    /**
     * 获取学生的所有家长关系请求
     */
    @GetMapping("/parent-relations")
    public ResponseEntity<?> getParentRelations(HttpServletRequest request) {
        try {
            // 使用自定义方法获取用户ID
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(studentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            User student = userOpt.get();
            List<ParentChildRelation> relations = parentService.getParentRelations(student.getId());
            
            List<Map<String, Object>> relationDTOs = relations.stream().map(relation -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", relation.getId());
                
                User parent = relation.getParent();
                dto.put("parentId", parent.getId());
                dto.put("parentName", parent.getRealName() != null ? parent.getRealName() : parent.getUsername());
                dto.put("parentUsername", parent.getUsername());
                
                dto.put("relationType", relation.getRelationType());
                dto.put("status", relation.getStatus());
                dto.put("createTime", relation.getCreateTime());
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("relations", relationDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取家长关系请求失败: " + e.getMessage()));
        }
    }

    /**
     * 处理家长关系请求
     */
    @PutMapping("/parent-relations/{relationId}")
    public ResponseEntity<?> handleParentRelation(
            @PathVariable Long relationId, 
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            // 使用自定义方法获取用户身份
            Long studentId = getUserIdFromRequest(httpRequest);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(studentId);
            String username = userOpt.isPresent() ? userOpt.get().getUsername() : "";
            
            String status = request.get("status");
            if (status == null || (!status.equals("confirmed") && !status.equals("rejected"))) {
                return ResponseEntity.badRequest().body(Map.of("message", "状态参数无效"));
            }
            
            ParentChildRelation relation = parentService.updateRelationStatus(relationId, status);
            
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", relation.getId());
            
            User parent = relation.getParent();
            dto.put("parentId", parent.getId());
            dto.put("parentName", parent.getRealName() != null ? parent.getRealName() : parent.getUsername());
            dto.put("parentUsername", parent.getUsername());
            
            dto.put("relationType", relation.getRelationType());
            dto.put("status", relation.getStatus());
            dto.put("createTime", relation.getCreateTime());
            
            String message = status.equals("confirmed") ? "已接受家长关联请求" : "已拒绝家长关联请求";
            
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "relation", dto
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "处理家长关系请求失败: " + e.getMessage()));
        }
    }
} 