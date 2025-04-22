package com.example.auth.service;

import com.example.auth.dto.PageResponse;
import com.example.auth.dto.RoleDTO;
import com.example.auth.dto.UserDTO;

import java.util.List;

public interface AdminService {
    
    // 用户管理
    PageResponse<UserDTO> getUserList(int page, int size, String username, String role, String status);
    
    UserDTO createUser(UserDTO userDTO, String password);
    
    UserDTO updateUser(Long id, UserDTO userDTO);
    
    void toggleUserStatus(Long id);
    
    void resetPassword(Long id, String newPassword);
    
    // 角色管理
    List<RoleDTO> getAllRoles();
    
    RoleDTO createRole(RoleDTO roleDTO);
    
    RoleDTO updateRole(Long id, RoleDTO roleDTO);
    
    void deleteRole(Long id);
} 