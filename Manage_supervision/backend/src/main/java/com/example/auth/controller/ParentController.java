package com.example.auth.controller;

import com.example.auth.dto.ParentChildRelationDTO;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.ParentChildRelation;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassStudentRelationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ParentService;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parent")
public class ParentController {

    @Autowired
    private ParentService parentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassStudentRelationRepository classStudentRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
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
            return null;
        }
    }
    
    /**
     * 获取家长的子女列表
     */
    @GetMapping("/children")
    public ResponseEntity<?> getChildren(HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            User parent = userOpt.get();
            List<ParentChildRelation> relations = parentService.getChildRelations(parent.getId());
            
            List<ParentChildRelationDTO> childDTOs = relations.stream().map(relation -> {
                ParentChildRelationDTO dto = new ParentChildRelationDTO();
                dto.setId(relation.getId());
                dto.setParentId(parent.getId());
                dto.setParentName(parent.getRealName() != null ? parent.getRealName() : parent.getUsername());
                dto.setParentUsername(parent.getUsername());
                
                User child = relation.getChild();
                dto.setChildId(child.getId());
                dto.setChildName(child.getRealName() != null ? child.getRealName() : child.getUsername());
                dto.setChildUsername(child.getUsername());
                dto.setChildUserNumber(child.getUserNumber());
                dto.setRelationType(relation.getRelationType());
                dto.setStatus(relation.getStatus());
                dto.setCreateTime(relation.getCreateTime());
                
                // 获取学生班级信息
                List<ClassStudentRelation> classRelations = classStudentRepository.findByStudentAndStatus(child, "active");
                if (!classRelations.isEmpty()) {
                    ClassStudentRelation relation1 = classRelations.get(0);
                    dto.setClassName(relation1.getClassEntity().getClassName());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("relations", childDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取子女列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 关联子女账号
     */
    @PostMapping("/children/relate")
    public ResponseEntity<?> relateChild(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            Long parentId = getUserIdFromRequest(httpRequest);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            String childIdentifier = request.get("childIdentifier");
            String relationType = request.get("relationType");
            
            if (childIdentifier == null || childIdentifier.trim().isEmpty() ||
                relationType == null || relationType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "子女标识符和关系类型不能为空"));
            }
            
            User parent = userOpt.get();
            ParentChildRelation relation = parentService.createRelation(parent.getId(), childIdentifier, relationType);
            
            ParentChildRelationDTO dto = new ParentChildRelationDTO();
            dto.setId(relation.getId());
            dto.setParentId(parent.getId());
            dto.setParentName(parent.getRealName() != null ? parent.getRealName() : parent.getUsername());
            dto.setParentUsername(parent.getUsername());
            
            User child = relation.getChild();
            dto.setChildId(child.getId());
            dto.setChildName(child.getRealName() != null ? child.getRealName() : child.getUsername());
            dto.setChildUsername(child.getUsername());
            dto.setChildUserNumber(child.getUserNumber());
            dto.setRelationType(relation.getRelationType());
            dto.setStatus(relation.getStatus());
            dto.setCreateTime(relation.getCreateTime());
            
            return ResponseEntity.ok(Map.of(
                    "message", "子女关联请求已发送，等待确认",
                    "relation", dto
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "关联子女账号失败: " + e.getMessage()));
        }
    }
    
    /**
     * 解除与子女的关联
     */
    @DeleteMapping("/children/{relationId}")
    public ResponseEntity<?> removeRelation(@PathVariable Long relationId, HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            boolean success = parentService.removeRelation(relationId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "已成功解除关联"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "关联关系不存在或无法解除"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "解除关联失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取子女详细信息
     */
    @GetMapping("/children/{childId}/details")
    public ResponseEntity<?> getChildDetails(@PathVariable Long childId, HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            User parent = userOpt.get();
            if (!parentService.relationExists(parent.getId(), childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "您没有权限查看该学生信息"));
            }
            
            Map<String, Object> details = parentService.getChildDetails(childId);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取子女详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取子女课程和任务信息
     */
    @GetMapping("/children/{childId}/courses-tasks")
    public ResponseEntity<?> getChildCoursesAndTasks(@PathVariable Long childId, HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            User parent = userOpt.get();
            if (!parentService.relationExists(parent.getId(), childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "您没有权限查看该学生信息"));
            }
            
            Map<String, Object> data = parentService.getChildCourseAndTasks(childId);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取子女课程任务信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取家长相关通知
     */
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            User parent = userOpt.get();
            List<Map<String, Object>> notifications = parentService.getParentNotifications(parent.getId());
            return ResponseEntity.ok(Map.of("notifications", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取通知失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取家长仪表盘数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            User parent = userOpt.get();
            List<ParentChildRelation> relations = parentService.getConfirmedChildRelations(parent.getId());
            
            // 构建子女信息
            List<Map<String, Object>> childrenInfo = relations.stream().map(relation -> {
                User child = relation.getChild();
                Map<String, Object> childInfo = new HashMap<>();
                childInfo.put("id", child.getId());
                childInfo.put("name", child.getRealName() != null ? child.getRealName() : child.getUsername());
                childInfo.put("username", child.getUsername());
                childInfo.put("userNumber", child.getUserNumber());
                childInfo.put("avatar", child.getAvatar());
                childInfo.put("relationType", relation.getRelationType());
                
                // 获取学生班级信息
                List<ClassStudentRelation> classRelations = classStudentRepository.findByStudentAndStatus(child, "active");
                if (!classRelations.isEmpty()) {
                    ClassStudentRelation classRelation = classRelations.get(0);
                    childInfo.put("className", classRelation.getClassEntity().getClassName());
                } else {
                    childInfo.put("className", null);
                }
                
                return childInfo;
            }).collect(Collectors.toList());
            
            // 简单获取通知数量
            List<Map<String, Object>> notifications = parentService.getParentNotifications(parent.getId());
            
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("children", childrenInfo);
            dashboardData.put("childrenCount", childrenInfo.size());
            dashboardData.put("notifications", notifications.stream().limit(5).collect(Collectors.toList())); // 只取最新的5条
            dashboardData.put("notificationCount", notifications.size());
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取仪表盘数据失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索学生
     */
    @GetMapping("/search-students")
    public ResponseEntity<?> searchStudents(@RequestParam String keyword, HttpServletRequest request) {
        try {
            // 验证用户登录
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
            }
            
            // 确保关键字长度足够
            if (keyword == null || keyword.trim().length() < 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "搜索关键字至少需要2个字符"));
            }
            
            // 获取所有学生角色的用户
            List<User> students = userRepository.findByRoleName("USER");
            
            // 根据关键字过滤学生
            List<Map<String, Object>> filteredStudents = students.stream()
                .filter(student -> 
                    (student.getUsername() != null && student.getUsername().toLowerCase().contains(keyword.toLowerCase())) ||
                    (student.getRealName() != null && student.getRealName().toLowerCase().contains(keyword.toLowerCase())) ||
                    (student.getUserNumber() != null && student.getUserNumber().toLowerCase().contains(keyword.toLowerCase()))
                )
                .limit(10) // 限制结果数量
                .map(student -> {
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("id", student.getId());
                    studentData.put("username", student.getUsername());
                    studentData.put("realName", student.getRealName());
                    studentData.put("userNumber", student.getUserNumber());
                    return studentData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("students", filteredStudents));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "搜索学生失败: " + e.getMessage()));
        }
    }
} 