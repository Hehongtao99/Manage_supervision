package com.example.auth.service;

import com.example.auth.model.dto.SubjectDTO;

import java.util.List;

/**
 * 课程科目服务接口
 */
public interface SubjectService {
    
    /**
     * 获取所有科目列表
     * @return 科目列表
     */
    List<SubjectDTO> getAllSubjects();
    
    /**
     * 根据ID获取科目
     * @param id 科目ID
     * @return 科目信息
     */
    SubjectDTO getSubjectById(Long id);
    
    /**
     * 创建新科目
     * @param name 科目名称
     * @return 创建的科目ID
     */
    Long createSubject(String name);
    
    /**
     * 更新科目
     * @param id 科目ID
     * @param name 科目名称
     * @return 是否成功
     */
    boolean updateSubject(Long id, String name);
    
    /**
     * 删除科目
     * @param id 科目ID
     * @return 是否成功
     */
    boolean deleteSubject(Long id);
} 