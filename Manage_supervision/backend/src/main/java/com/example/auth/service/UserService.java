package com.example.auth.service;

import com.example.auth.model.dto.StudentDTO;
import com.example.auth.model.dto.StudentDetailDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Permission;
import com.example.auth.model.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    User register(String username, String password);
    User findByUsername(String username);
    User findById(Long id);
    boolean validatePassword(User user, String password);
    void changePassword(User user, String currentPassword, String newPassword);
    void updateAvatar(User user, String avatarUrl);
    User updateProfile(User user, Map<String, String> profileData);
    
    /**
     * 加载用户权限
     * @param user 用户对象
     * @return 带有权限的用户对象
     */
    User loadUserPermissions(User user);
    
    /**
     * 检查用户是否拥有指定权限
     * @param user 用户对象
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(User user, String permissionCode);
    
    /**
     * 检查用户是否拥有指定的任意一个权限
     * @param user 用户对象
     * @param permissionCodes 权限编码数组
     * @return 是否拥有任意一个权限
     */
    boolean hasAnyPermission(User user, String[] permissionCodes);
    
    /**
     * 检查用户是否拥有指定的所有权限
     * @param user 用户对象
     * @param permissionCodes 权限编码数组
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(User user, String[] permissionCodes);
    
    // 学生管理相关方法
    List<StudentDTO> getAllStudents();
    StudentDetailDTO getStudentDetails(Long id);
    boolean updateStudentStatus(Long id, String status);
    boolean deleteStudent(Long id);
    
    // 督导员管理相关方法
    List<User> getAllSupervisors();

    // 添加获取教师学生的方法
    List<UserDTO> getStudentsByTeacher(Long teacherId);
} 