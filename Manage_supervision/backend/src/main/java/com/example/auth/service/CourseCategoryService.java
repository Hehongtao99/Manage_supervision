package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.auth.model.entity.CourseCategory;

import java.util.List;

public interface CourseCategoryService extends IService<CourseCategory> {
    
    Page<CourseCategory> getCategoryPage(int current, int size, String categoryName, String status);
    
    boolean saveCategory(CourseCategory category);
    
    boolean updateCategory(CourseCategory category);
    
    boolean deleteCategory(Long id);
    
    List<String> getAllCategoryNames();
} 