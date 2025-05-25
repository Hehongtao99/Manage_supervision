package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RoleDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.service.AdminService;
import com.example.auth.model.entity.User;
import com.example.auth.service.UserService;
import com.example.auth.mapper.TeacherClassRelationMapper;
import com.example.auth.model.TeacherClassRelation;
import com.example.auth.model.College;
import com.example.auth.model.Major;
import com.example.auth.model.ClassEntity;
import com.example.auth.service.CollegeService;
import com.example.auth.service.MajorService;
import com.example.auth.service.ClassService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherClassRelationMapper teacherClassRelationMapper;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private ClassService classService;

    // 用户管理接口
    @GetMapping("/users")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        PageResponse<UserDTO> response = adminService.getUserList(page, size, username, role, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    @RequireRole("ADMIN")
    @Transactional
    public ResponseEntity<UserDTO> createUser(@RequestBody Map<String, Object> request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername((String) request.get("username"));
        userDTO.setRealName((String) request.get("realName"));
        userDTO.setNickname((String) request.get("nickname"));
        userDTO.setEmail((String) request.get("email"));
        userDTO.setPhone((String) request.get("phone"));
        userDTO.setRoles((List<String>) request.get("roles"));
        
        // 设置学院专业班级信息
        if (request.get("collegeId") != null) {
            userDTO.setCollegeId(Long.valueOf(request.get("collegeId").toString()));
        }
        if (request.get("majorId") != null) {
            userDTO.setMajorId(Long.valueOf(request.get("majorId").toString()));
        }
        if (request.get("classId") != null) {
            userDTO.setClassId(Long.valueOf(request.get("classId").toString()));
        }
        
        String password = (String) request.get("password");
        
        UserDTO createdUser = adminService.createUser(userDTO, password);
        
        // 如果是教师角色，处理班级关系
        List<String> roles = userDTO.getRoles();
        if (roles != null && roles.contains("SUPERVISOR") && request.get("classIds") != null) {
            List<Integer> classIds = (List<Integer>) request.get("classIds");
            if (classIds != null && !classIds.isEmpty()) {
                for (Integer classId : classIds) {
                    TeacherClassRelation relation = new TeacherClassRelation();
                    relation.setTeacherId(createdUser.getId());
                    relation.setClassId(classId.longValue());
                    relation.setAssignTime(LocalDateTime.now());
                    relation.setStatus("active");
                    teacherClassRelationMapper.insert(relation);
                }
            }
        }
        
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/users/{id}")
    @RequireRole("ADMIN")
    @Transactional
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setUsername((String) request.get("username"));
        userDTO.setRealName((String) request.get("realName"));
        userDTO.setNickname((String) request.get("nickname"));
        userDTO.setEmail((String) request.get("email"));
        userDTO.setPhone((String) request.get("phone"));
        userDTO.setRoles((List<String>) request.get("roles"));
        
        // 设置学院专业班级信息
        if (request.get("collegeId") != null) {
            userDTO.setCollegeId(Long.valueOf(request.get("collegeId").toString()));
        }
        if (request.get("majorId") != null) {
            userDTO.setMajorId(Long.valueOf(request.get("majorId").toString()));
        }
        if (request.get("classId") != null) {
            userDTO.setClassId(Long.valueOf(request.get("classId").toString()));
        }
        
        UserDTO updatedUser = adminService.updateUser(id, userDTO);
        
        // 如果是教师角色，处理班级关系
        List<String> roles = userDTO.getRoles();
        if (roles != null && roles.contains("SUPERVISOR")) {
            // 先删除现有的班级关系
            LambdaQueryWrapper<TeacherClassRelation> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(TeacherClassRelation::getTeacherId, id);
            teacherClassRelationMapper.delete(deleteWrapper);
            
            // 添加新的班级关系
            if (request.get("classIds") != null) {
                List<Integer> classIds = (List<Integer>) request.get("classIds");
                if (classIds != null && !classIds.isEmpty()) {
                    for (Integer classId : classIds) {
                        TeacherClassRelation relation = new TeacherClassRelation();
                        relation.setTeacherId(id);
                        relation.setClassId(classId.longValue());
                        relation.setAssignTime(LocalDateTime.now());
                        relation.setStatus("active");
                        teacherClassRelationMapper.insert(relation);
                    }
                }
            }
        }
        
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{id}/toggle-status")
    @RequireRole("ADMIN")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/reset-password")
    @RequireRole("ADMIN")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        adminService.resetPassword(id, password);
        return ResponseEntity.ok().build();
    }

    // 角色管理接口
    @GetMapping("/roles")
    @RequireRole("ADMIN")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = adminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/roles")
    @RequireRole("ADMIN")
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        RoleDTO createdRole = adminService.createRole(roleDTO);
        return ResponseEntity.ok(createdRole);
    }

    @PutMapping("/roles/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        RoleDTO updatedRole = adminService.updateRole(id, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/roles/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return ResponseEntity.ok().build();
    }
    
    // 控制台统计数据接口
    @GetMapping("/statistics")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", adminService.countTotalUsers());
        statistics.put("activeUsers", adminService.countActiveUsers());
        statistics.put("totalRoles", adminService.countTotalRoles());
        statistics.put("systemHealth", "正常");
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/role-distribution")
    @RequireRole("ADMIN")
    public ResponseEntity<List<Map<String, Object>>> getRoleDistribution() {
        List<Map<String, Object>> roleDistribution = new ArrayList<>();
        
        // 从service层获取角色分布数据
        Map<String, Long> distribution = adminService.getUserRoleDistribution();
        
        // 转换为前端所需的格式
        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            roleDistribution.add(item);
        }
        
        return ResponseEntity.ok(roleDistribution);
    }
    
    @GetMapping("/statistics/user-activity")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, List<Object>>> getUserActivity() {
        // 从service层获取近7天用户活跃数据
        Map<String, List<Object>> activityData = adminService.getUserActivityLastWeek();
        
        return ResponseEntity.ok(activityData);
    }

    /**
     * 获取教师的班级关系
     */
    @GetMapping("/users/{id}/classes")
    @RequireRole("ADMIN")
    public ResponseEntity<List<Long>> getTeacherClasses(@PathVariable Long id) {
        try {
            LambdaQueryWrapper<TeacherClassRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeacherClassRelation::getTeacherId, id)
                       .eq(TeacherClassRelation::getStatus, "active");
            List<TeacherClassRelation> relations = teacherClassRelationMapper.selectList(queryWrapper);
            
            List<Long> classIds = relations.stream()
                    .map(TeacherClassRelation::getClassId)
                    .toList();
            
            return ResponseEntity.ok(classIds);
        } catch (Exception e) {
            log.error("获取教师班级关系失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取教师的详细信息，包括学院、专业和班级信息
     */
    @GetMapping("/users/{id}/teacher-details")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getTeacherDetails(@PathVariable Long id) {
        try {
            // 获取用户基本信息
            User user = userService.findById(id);
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
            queryWrapper.eq(TeacherClassRelation::getTeacherId, id)
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

    /**
     * 获取所有教师列表（用于下拉框）
     */
    @GetMapping("/teachers/all")
    @RequireRole("ADMIN")
    public ResponseEntity<List<User>> getAllTeachers() {
        try {
            List<User> teachers = userService.getTeacherList();
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("获取教师列表失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 