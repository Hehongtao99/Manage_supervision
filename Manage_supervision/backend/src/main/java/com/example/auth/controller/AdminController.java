package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.RoleDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

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
} 