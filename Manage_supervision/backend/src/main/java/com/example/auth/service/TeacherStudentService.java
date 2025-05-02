package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.TeacherStudentDTO;
import com.example.auth.model.dto.TeacherWithStudentsDTO;
import com.example.auth.model.dto.UserDTO;

import java.util.List;

public interface TeacherStudentService {
    
    // 获取所有教师列表（带分页）
    PageResponse<UserDTO> getAllTeachers(int page, int size, String keyword);
    
    // 获取所有学生列表（带分页）
    PageResponse<UserDTO> getAllStudents(int page, int size, String keyword);
    
    // 获取未分配给任何教师的学生列表
    List<UserDTO> getUnassignedStudents();
    
    // 获取特定教师的学生列表
    List<UserDTO> getStudentsByTeacher(Long teacherId);
    
    // 批量分配学生给教师
    boolean assignStudentsToTeacher(Long teacherId, List<Long> studentIds);
    
    // 取消分配学生给教师
    boolean unassignStudent(Long teacherId, Long studentId);
    
    // 获取特定教师（包含其学生列表）
    TeacherWithStudentsDTO getTeacherWithStudents(Long teacherId);
    
    // 获取教师-学生关系详情
    TeacherStudentDTO getRelationDetail(Long relationId);
    
    // 判断教师是否分配给了指定学生
    boolean isTeacherAssignedToStudent(Long teacherId, Long studentId);
} 