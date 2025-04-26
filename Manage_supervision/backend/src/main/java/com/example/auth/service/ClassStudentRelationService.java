package com.example.auth.service;

import com.example.auth.entity.ClassStudentRelation;

import java.util.List;
import java.util.Optional;

/**
 * 班级学生关系服务接口
 */
public interface ClassStudentRelationService {
    
    /**
     * 根据学生ID查找活跃的班级关系
     * 
     * @param studentId 学生ID
     * @return 班级关系对象
     */
    Optional<ClassStudentRelation> findActiveRelationByStudentId(Long studentId);
    
    /**
     * 根据班级ID查找所有活跃学生
     * 
     * @param classId 班级ID
     * @return 班级学生关系列表
     */
    List<ClassStudentRelation> findActiveStudentsByClassId(Long classId);
} 