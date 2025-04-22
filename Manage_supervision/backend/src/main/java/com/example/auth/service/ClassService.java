package com.example.auth.service;

import com.example.auth.dto.ClassDTO;
import com.example.auth.dto.UserDTO;
import java.util.List;
import java.util.Map;

public interface ClassService {
    ClassDTO createClass(ClassDTO classDTO);
    ClassDTO updateClass(Long id, ClassDTO classDTO);
    void deleteClass(Long id);
    ClassDTO getClassById(Long id);
    List<ClassDTO> getAllClasses();
    List<ClassDTO> searchClasses(String keyword);
    
    // 班级-学生管理
    List<UserDTO> getStudentsByClassId(Long classId);
    List<UserDTO> getUnassignedStudentsByClassId(Long classId);
    List<Map<String, Object>> getAllAvailableStudentsForClass(Long classId);
    boolean addStudentToClass(Long classId, Long studentId);
    Map<String, Object> addStudentsToClass(Long classId, List<Long> studentIds);
    boolean removeStudentFromClass(Long classId, Long studentId);
    
    /**
     * 获取教师教授的班级列表
     * @param teacherId 教师ID
     * @return 班级列表
     */
    List<Map<String, Object>> getClassesByTeacherId(Long teacherId);
    
    /**
     * 获取学生所在的班级列表
     * @param studentId 学生ID
     * @return 班级列表
     */
    List<Map<String, Object>> getClassesByStudentId(Long studentId);
    
    /**
     * 获取班级的学生列表(详细信息)
     * @param classId 班级ID
     * @return 学生详细信息列表
     */
    List<Map<String, Object>> getClassStudentsDetail(Long classId);
    
    /**
     * 验证教师是否被分配到班级
     * @param teacherId 教师ID
     * @param classId 班级ID
     * @return 是否被分配
     */
    boolean isTeacherAssignedToClass(Long teacherId, Long classId);
} 