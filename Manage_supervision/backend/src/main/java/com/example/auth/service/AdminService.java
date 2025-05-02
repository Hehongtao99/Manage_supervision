package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RoleDTO;
import com.example.auth.model.dto.UserDTO;

import java.util.List;
import java.util.Map;

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
    
    // 统计数据相关接口
    Long countTotalUsers();
    
    Long countActiveUsers();
    
    Long countTotalRoles();
    
    Map<String, Long> getUserRoleDistribution();
    
    Map<String, List<Object>> getUserActivityLastWeek();
} 