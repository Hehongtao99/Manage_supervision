package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RoleDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.User;
import com.example.auth.service.AdminService;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

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
    public ResponseEntity<UserDTO> createUser(@RequestBody Map<String, Object> request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername((String) request.get("username"));
        userDTO.setRealName((String) request.get("realName"));
        userDTO.setNickname((String) request.get("nickname"));
        userDTO.setEmail((String) request.get("email"));
        userDTO.setPhone((String) request.get("phone"));
        userDTO.setRoles((List<String>) request.get("roles"));
        
        String password = (String) request.get("password");
        
        UserDTO createdUser = adminService.createUser(userDTO, password);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/users/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = adminService.updateUser(id, userDTO);
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
     * 获取学生列表（带人脸状态）
     */
    @GetMapping("/students")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<Map<String, Object>>> getStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String faceStatus) {
        
        PageResponse<UserDTO> userResponse = adminService.getUserList(page, size, query, roleType, null);
        
        // 转换数据格式，添加人脸状态信息
        List<Map<String, Object>> content = new ArrayList<>();
        for (UserDTO user : userResponse.getContent()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("realName", user.getRealName());
            userMap.put("userNumber", user.getUserNumber());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("createTime", user.getCreateTime());
            
            // 检查人脸状态 - 每次请求都从数据库获取最新状态
            boolean hasFaceData = userService.hasFaceData(user.getId());
            userMap.put("hasFaceData", hasFaceData);
            
            // 如果有人脸状态筛选，只添加符合条件的用户
            if (faceStatus != null && !faceStatus.isEmpty()) {
                boolean wantFaceData = "true".equals(faceStatus);
                if (wantFaceData != hasFaceData) {
                    continue;
                }
            }
            
            content.add(userMap);
        }
        
        // 创建新的PageResponse对象
        PageResponse<Map<String, Object>> response = new PageResponse<>(
            content, 
            userResponse.getTotal(), 
            userResponse.getPage(), 
            userResponse.getSize()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员更新用户人脸数据
     */
    @PutMapping("/user/{userId}/face")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateUserFaceData(@PathVariable Long userId, @RequestBody Map<String, String> requestBody) {
        try {
            if (!requestBody.containsKey("faceData")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "人脸数据不能为空"
                ));
            }
            
            String faceData = requestBody.get("faceData");
            
            // 保存人脸数据
            boolean saved = faceRecognitionService.saveFaceData(userId, faceData);
            
            if (saved) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "人脸信息更新成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "人脸信息更新失败，请确保图像中包含清晰的人脸"
                ));
            }
        } catch (Exception e) {
            logger.error("管理员更新用户人脸信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新人脸信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 管理员删除用户人脸数据
     */
    @DeleteMapping("/user/{userId}/face")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteUserFaceData(@PathVariable Long userId) {
        try {
            // 查询用户
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            // 清空用户人脸数据 - 使用空字符串而不是null
            user.setFaceData("");
            // 使用正确的updateFaceData方法
            boolean updated = userService.updateFaceData(user, "");
            
            if (!updated) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "删除人脸信息失败"
                ));
            }
            
            // 验证是否真的删除了
            if (userService.hasFaceData(userId)) {
                logger.warn("人脸数据删除失败: 数据仍存在");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "删除人脸信息失败: 数据仍存在"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "人脸信息删除成功"
            ));
        } catch (Exception e) {
            logger.error("管理员删除用户人脸信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除人脸信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户人脸状态
     */
    @GetMapping("/user/{userId}/face-status")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getUserFaceStatus(@PathVariable Long userId) {
        try {
            // 查询用户
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            boolean hasFaceData = userService.hasFaceData(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "registered", hasFaceData,
                "message", hasFaceData ? "用户已注册人脸信息" : "用户未注册人脸信息"
            ));
        } catch (Exception e) {
            logger.error("查询用户人脸注册状态失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询人脸注册状态失败: " + e.getMessage()
            ));
        }
    }
} 