package com.example.auth.service;

import com.example.auth.model.dto.CourseCategoryDTO;
import com.example.auth.model.dto.PageResponse;

import java.util.List;

public interface CourseCategoryService {

    /**
     * 分页查询课程类别
     */
    PageResponse<CourseCategoryDTO> getCategoryList(int page, int size, String name);
    
    /**
     * 获取所有课程类别
     */
    List<CourseCategoryDTO> getAllCategories();
    
    /**
     * 根据ID获取课程类别
     */
    CourseCategoryDTO getCategoryById(Long id);
    
    /**
     * 创建课程类别
     */
    CourseCategoryDTO createCategory(CourseCategoryDTO categoryDTO);
    
    /**
     * 更新课程类别
     */
    CourseCategoryDTO updateCategory(Long id, CourseCategoryDTO categoryDTO);
    
    /**
     * 删除课程类别
     */
    boolean deleteCategory(Long id);
} 