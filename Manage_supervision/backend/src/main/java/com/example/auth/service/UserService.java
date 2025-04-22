package com.example.auth.service;

import com.example.auth.dto.StudentDTO;
import com.example.auth.dto.StudentDetailDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.User;
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