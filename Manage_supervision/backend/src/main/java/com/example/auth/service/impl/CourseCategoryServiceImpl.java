package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.CourseCategoryMapper;
import com.example.auth.model.dto.CourseCategoryDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.CourseCategory;
import com.example.auth.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResponse<CourseCategoryDTO> getCategoryList(int page, int size, String name) {
        Page<CourseCategory> pageParam = new Page<>(page, size);
        Page<CourseCategory> categoryPage;
        
        if (name != null && !name.trim().isEmpty()) {
            categoryPage = (Page<CourseCategory>) courseCategoryMapper.findPageByName(pageParam, name);
        } else {
            categoryPage = (Page<CourseCategory>) courseCategoryMapper.findPage(pageParam);
        }
        
        List<CourseCategoryDTO> categoryDTOs = categoryPage.getRecords().stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(categoryDTOs, categoryPage.getTotal(), (int)categoryPage.getCurrent(), (int)categoryPage.getSize());
    }

    @Override
    public List<CourseCategoryDTO> getAllCategories() {
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(CourseCategory::getName);
        
        List<CourseCategory> categories = courseCategoryMapper.selectList(queryWrapper);
        return categories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourseCategoryDTO getCategoryById(Long id) {
        CourseCategory category = courseCategoryMapper.selectById(id);
        return category != null ? convertToCategoryDTO(category) : null;
    }

    @Override
    @Transactional
    public CourseCategoryDTO createCategory(CourseCategoryDTO categoryDTO) {
        CourseCategory category = new CourseCategory();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        
        courseCategoryMapper.insert(category);
        return convertToCategoryDTO(category);
    }

    @Override
    @Transactional
    public CourseCategoryDTO updateCategory(Long id, CourseCategoryDTO categoryDTO) {
        CourseCategory existingCategory = courseCategoryMapper.selectById(id);
        if (existingCategory == null) {
            return null;
        }
        
        if (categoryDTO.getName() != null) {
            existingCategory.setName(categoryDTO.getName());
        }
        
        if (categoryDTO.getDescription() != null) {
            existingCategory.setDescription(categoryDTO.getDescription());
        }
        
        existingCategory.setUpdateTime(LocalDateTime.now());
        courseCategoryMapper.updateById(existingCategory);
        
        return convertToCategoryDTO(existingCategory);
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        int result = courseCategoryMapper.deleteById(id);
        return result > 0;
    }
    
    /**
     * 将CourseCategory实体转换为CourseCategoryDTO
     */
    private CourseCategoryDTO convertToCategoryDTO(CourseCategory category) {
        CourseCategoryDTO dto = new CourseCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreateTime(category.getCreateTime());
        dto.setUpdateTime(category.getUpdateTime());
        return dto;
    }
} 