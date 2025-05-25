package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.TeacherStudentDTO;
import com.example.auth.model.dto.TeacherWithStudentsDTO;
import com.example.auth.model.dto.UserDTO;

import java.util.List;

public interface TeacherStudentService {
    
    // 获取所有教师（分页）
    PageResponse<UserDTO> getAllTeachers(int page, int size, String keyword);
    
    // 获取所有教师（不分页）
    List<UserDTO> getAllTeachersWithoutPaging();
    
    // 获取所有学生（分页）
    PageResponse<UserDTO> getAllStudents(int page, int size, String keyword);
    
    // 获取未分配的学生
    List<UserDTO> getUnassignedStudents();
    
    // 根据教师ID获取学生列表
    List<UserDTO> getStudentsByTeacher(Long teacherId);
    
    // 分配学生给教师
    boolean assignStudentsToTeacher(Long teacherId, List<Long> studentIds);
    
    // 取消学生分配
    boolean unassignStudent(Long teacherId, Long studentId);
    
    // 获取教师详情（包含学生列表）
    TeacherWithStudentsDTO getTeacherWithStudents(Long teacherId);
    
    // 获取教师-学生关系详情
    TeacherStudentDTO getRelationDetail(Long relationId);
    
    // 检查教师是否已分配给学生
    boolean isTeacherAssignedToStudent(Long teacherId, Long studentId);
} 