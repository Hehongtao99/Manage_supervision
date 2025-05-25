package com.example.auth.controller;

import com.example.auth.annotation.RequirePermission;
import com.example.auth.model.dto.CourseCategoryDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-categories")
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;
    
    /**
     * 分页获取课程类别列表
     */
    @GetMapping
    public ResponseEntity<PageResponse<CourseCategoryDTO>> getCategoryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        
        PageResponse<CourseCategoryDTO> response = courseCategoryService.getCategoryList(page, size, name);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取所有课程类别（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<List<CourseCategoryDTO>> getAllCategories() {
        List<CourseCategoryDTO> categories = courseCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 根据ID获取课程类别
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseCategoryDTO> getCategoryById(@PathVariable Long id) {
        CourseCategoryDTO category = courseCategoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 创建课程类别（需要管理员权限）
     */
    @PostMapping
    @RequirePermission({"category:add"})
    public ResponseEntity<CourseCategoryDTO> createCategory(@RequestBody CourseCategoryDTO categoryDTO) {
        CourseCategoryDTO createdCategory = courseCategoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(createdCategory);
    }
    
    /**
     * 更新课程类别（需要管理员权限）
     */
    @PutMapping("/{id}")
    @RequirePermission({"category:edit"})
    public ResponseEntity<CourseCategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CourseCategoryDTO categoryDTO) {
        CourseCategoryDTO updatedCategory = courseCategoryService.updateCategory(id, categoryDTO);
        if (updatedCategory != null) {
            return ResponseEntity.ok(updatedCategory);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 删除课程类别（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    @RequirePermission({"category:delete"})
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        boolean deleted = courseCategoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
} 