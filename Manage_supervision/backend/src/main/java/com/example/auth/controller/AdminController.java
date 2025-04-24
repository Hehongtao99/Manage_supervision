package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.RoleDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.ParentChildRelation;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AdminService;
import com.example.auth.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ParentService parentService;
    
    @Autowired
    private UserRepository userRepository;

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
    
    @PostMapping("/parents")
    @RequireRole("ADMIN")
    public ResponseEntity<UserDTO> createParent(@RequestBody Map<String, Object> request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername((String) request.get("username"));
        userDTO.setRealName((String) request.get("realName"));
        userDTO.setNickname((String) request.get("nickname"));
        userDTO.setEmail((String) request.get("email"));
        userDTO.setPhone((String) request.get("phone"));
        userDTO.setBio((String) request.get("bio"));
        
        String password = (String) request.get("password");
        
        UserDTO createdParent = adminService.createParentUser(userDTO, password);
        return ResponseEntity.ok(createdParent);
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
    
    // 家长-子女关系管理接口
    
    /**
     * 获取某个家长的子女关系列表
     */
    @GetMapping("/parents/{parentId}/children")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getParentChildren(@PathVariable Long parentId) {
        try {
            Optional<User> userOpt = userRepository.findById(parentId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "家长用户不存在"));
            }
            
            User parent = userOpt.get();
            List<ParentChildRelation> relations = parentService.getChildRelations(parent.getId());
            
            // 转换为前端需要的格式
            List<Map<String, Object>> relationDTOs = relations.stream()
                .map(relation -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", relation.getId());
                    
                    // 家长信息
                    dto.put("parentId", parent.getId());
                    dto.put("parentName", parent.getRealName() != null ? parent.getRealName() : parent.getUsername());
                    dto.put("parentUsername", parent.getUsername());
                    
                    // 子女信息
                    User child = relation.getChild();
                    dto.put("childId", child.getId());
                    dto.put("childName", child.getRealName() != null ? child.getRealName() : child.getUsername());
                    dto.put("childUsername", child.getUsername());
                    dto.put("childUserNumber", child.getUserNumber());
                    
                    // 关系信息
                    dto.put("relationType", relation.getRelationType());
                    dto.put("status", relation.getStatus());
                    dto.put("createTime", relation.getCreateTime());
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("relations", relationDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取子女列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 管理员分配子女给家长
     */
    @PostMapping("/parents/{parentId}/assign-child")
    @RequireRole("ADMIN")
    public ResponseEntity<?> assignChildToParent(
            @PathVariable Long parentId, 
            @RequestBody Map<String, String> request) {
        try {
            String childIdentifier = request.get("childIdentifier"); // 子女用户名或学号
            String relationType = request.get("relationType"); // 关系类型
            
            if (childIdentifier == null || childIdentifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "子女标识符不能为空"));
            }
            
            if (relationType == null || relationType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "关系类型不能为空"));
            }
            
            Optional<User> userOpt = userRepository.findById(parentId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "家长用户不存在"));
            }
            
            // 创建关系并设置为已确认状态
            ParentChildRelation relation = parentService.createRelation(parentId, childIdentifier, relationType);
            relation = parentService.updateRelationStatus(relation.getId(), "confirmed");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "已成功分配子女给家长");
            response.put("relation", relation);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "分配子女失败: " + e.getMessage()));
        }
    }
    
    /**
     * 管理员解除家长和子女的关系
     */
    @DeleteMapping("/parent-child-relations/{relationId}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> removeParentChildRelation(@PathVariable Long relationId) {
        try {
            boolean success = parentService.removeRelation(relationId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "已成功解除家长和子女的关系"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "关系不存在或无法解除"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "解除关系失败: " + e.getMessage()));
        }
    }
} 