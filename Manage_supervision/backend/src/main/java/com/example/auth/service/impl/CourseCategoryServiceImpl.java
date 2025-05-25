package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.CourseCategoryMapper;
import com.example.auth.model.entity.CourseCategory;
import com.example.auth.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
    
    @Override
    public Page<CourseCategory> getCategoryPage(int current, int size, String categoryName, String status) {
        Page<CourseCategory> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(categoryName != null && !categoryName.trim().isEmpty(), CourseCategory::getCategoryName, categoryName)
               .eq(status != null && !status.trim().isEmpty(), CourseCategory::getStatus, status)
               .orderByDesc(CourseCategory::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public boolean saveCategory(CourseCategory category) {
        if (category.getStatus() == null) {
            category.setStatus("active");
        }
        return this.save(category);
    }
    
    @Override
    public boolean updateCategory(CourseCategory category) {
        return this.updateById(category);
    }
    
    @Override
    public boolean deleteCategory(Long id) {
        CourseCategory category = new CourseCategory();
        category.setId(id);
        category.setStatus("inactive");
        return this.updateById(category);
    }
    
    @Override
    public List<String> getAllCategoryNames() {
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getStatus, "active")
               .select(CourseCategory::getCategoryName);
        List<CourseCategory> categories = this.list(wrapper);
        
        if (categories.isEmpty()) {
            // 返回默认类别
            return Arrays.asList(
                "计算机科学", "数学", "英语", "物理", "化学", 
                "生物", "历史", "地理", "政治", "经济学", 
                "管理学", "艺术设计", "体育", "其他"
            );
        }
        
        return categories.stream()
                .map(CourseCategory::getCategoryName)
                .collect(Collectors.toList());
    }
} 