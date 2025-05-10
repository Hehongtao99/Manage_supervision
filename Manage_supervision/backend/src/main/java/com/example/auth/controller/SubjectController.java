package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.SubjectDTO;
import com.example.auth.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程科目控制器
 */
@RestController
@RequestMapping("/api")
public class SubjectController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);
    
    @Autowired
    private SubjectService subjectService;
    
    /**
     * 获取所有科目列表（公共）
     */
    @GetMapping("/subjects")
    public ResponseEntity<?> getAllSubjects() {
        logger.info("获取所有科目列表");
        List<SubjectDTO> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }
    
    /**
     * 获取科目详情（公共）
     */
    @GetMapping("/subjects/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable("id") Long id) {
        logger.info("获取科目详情 - ID: {}", id);
        
        SubjectDTO subject = subjectService.getSubjectById(id);
        if (subject == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "科目不存在"
            ));
        }
        
        return ResponseEntity.ok(subject);
    }
    
    /**
     * 创建科目（管理员）
     */
    @PostMapping("/admin/subjects")
    @RequireRole("ADMIN")
    public ResponseEntity<?> createSubject(@RequestParam("name") String name) {
        logger.info("创建科目 - 名称: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "科目名称不能为空"
            ));
        }
        
        Long subjectId = subjectService.createSubject(name.trim());
        
        if (subjectId != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "科目创建成功");
            response.put("subjectId", subjectId);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "科目创建失败，可能名称已存在"
            ));
        }
    }
    
    /**
     * 更新科目（管理员）
     */
    @PutMapping("/admin/subjects/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateSubject(
            @PathVariable("id") Long id,
            @RequestParam("name") String name) {
        
        logger.info("更新科目 - ID: {}, 新名称: {}", id, name);
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "科目名称不能为空"
            ));
        }
        
        boolean success = subjectService.updateSubject(id, name.trim());
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "科目更新成功"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "科目更新失败，可能科目不存在或名称已被使用"
            ));
        }
    }
    
    /**
     * 删除科目（管理员）
     */
    @DeleteMapping("/admin/subjects/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteSubject(@PathVariable("id") Long id) {
        logger.info("删除科目 - ID: {}", id);
        
        boolean success = subjectService.deleteSubject(id);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "科目删除成功"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "科目删除失败，可能科目不存在或已被使用"
            ));
        }
    }
} 