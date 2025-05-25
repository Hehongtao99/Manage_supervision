package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.model.ClassEntity;
import com.example.auth.model.College;
import com.example.auth.model.Major;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.service.ClassService;
import com.example.auth.service.CollegeService;
import com.example.auth.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrganizationController {
    
    @Autowired
    private CollegeService collegeService;
    
    @Autowired
    private MajorService majorService;
    
    @Autowired
    private ClassService classService;
    
    // ==================== 公共接口（用于用户管理等功能） ====================
    
    @GetMapping("/colleges")
    public ResponseEntity<?> getCollegesForPublic() {
        try {
            List<College> colleges = collegeService.getAllActiveColleges();
            return ResponseEntity.ok(colleges);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取学院列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/majors")
    public ResponseEntity<?> getMajorsForPublic() {
        try {
            List<Major> majors = majorService.getAllActiveMajors();
            return ResponseEntity.ok(majors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取专业列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/classes")
    public ResponseEntity<?> getClassesForPublic() {
        try {
            List<ClassEntity> classes = classService.getAllActiveClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取班级列表失败: " + e.getMessage()));
        }
    }
    
    // ==================== 管理员接口 ====================
    
    @GetMapping("/admin/organization/colleges")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getColleges(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String collegeName) {
        try {
            Page<College> page = collegeService.getCollegePage(current, size, collegeName);
            PageResponse<College> response = new PageResponse<>(
                page.getRecords(), page.getTotal(), current, size
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取学院列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/colleges")
    @RequireRole("ADMIN")
    public ResponseEntity<?> createCollege(@RequestBody College college) {
        try {
            boolean success = collegeService.saveCollege(college);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "学院创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "学院创建失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "学院创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/colleges/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateCollege(@PathVariable Long id, @RequestBody College college) {
        try {
            college.setId(id);
            boolean success = collegeService.updateCollege(college);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "学院更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "学院更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "学院更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/colleges/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteCollege(@PathVariable Long id) {
        try {
            boolean success = collegeService.deleteCollege(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "学院删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "学院删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "学院删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/organization/colleges/all")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAllColleges() {
        try {
            List<College> colleges = collegeService.getAllActiveColleges();
            return ResponseEntity.ok(colleges);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取学院列表失败: " + e.getMessage()));
        }
    }
    
    // ==================== 专业管理 ====================
    
    @GetMapping("/admin/organization/majors")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getMajors(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String majorName,
            @RequestParam(required = false) Long collegeId) {
        try {
            Page<Major> page = majorService.getMajorPage(current, size, majorName, collegeId);
            PageResponse<Major> response = new PageResponse<>(
                page.getRecords(), page.getTotal(), current, size
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取专业列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/admin/organization/majors")
    @RequireRole("ADMIN")
    public ResponseEntity<?> createMajor(@RequestBody Major major) {
        try {
            boolean success = majorService.saveMajor(major);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "专业创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "专业创建失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "专业创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/admin/organization/majors/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateMajor(@PathVariable Long id, @RequestBody Major major) {
        try {
            major.setId(id);
            boolean success = majorService.updateMajor(major);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "专业更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "专业更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "专业更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/admin/organization/majors/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteMajor(@PathVariable Long id) {
        try {
            boolean success = majorService.deleteMajor(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "专业删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "专业删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "专业删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/organization/majors/by-college/{collegeId}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getMajorsByCollege(@PathVariable Long collegeId) {
        try {
            List<Major> majors = majorService.getMajorsByCollegeId(collegeId);
            return ResponseEntity.ok(majors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取专业列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/organization/majors/all")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAllMajors() {
        try {
            List<Major> majors = majorService.getAllActiveMajors();
            return ResponseEntity.ok(majors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取专业列表失败: " + e.getMessage()));
        }
    }
    
    // ==================== 班级管理 ====================
    
    @GetMapping("/admin/organization/classes")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getClasses(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long majorId) {
        try {
            Page<ClassEntity> page = classService.getClassPage(current, size, className, collegeId, majorId);
            PageResponse<ClassEntity> response = new PageResponse<>(
                page.getRecords(), page.getTotal(), current, size
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取班级列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/admin/organization/classes")
    @RequireRole("ADMIN")
    public ResponseEntity<?> createClass(@RequestBody ClassEntity classEntity) {
        try {
            boolean success = classService.saveClass(classEntity);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "班级创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "班级创建失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "班级创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/admin/organization/classes/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateClass(@PathVariable Long id, @RequestBody ClassEntity classEntity) {
        try {
            classEntity.setId(id);
            boolean success = classService.updateClass(classEntity);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "班级更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "班级更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "班级更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/admin/organization/classes/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        try {
            boolean success = classService.deleteClass(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "班级删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "班级删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "班级删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/organization/classes/by-major/{majorId}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getClassesByMajor(@PathVariable Long majorId) {
        try {
            List<ClassEntity> classes = classService.getClassesByMajorId(majorId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取班级列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/organization/classes/by-college/{collegeId}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getClassesByCollege(@PathVariable Long collegeId) {
        try {
            List<ClassEntity> classes = classService.getClassesByCollegeId(collegeId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取班级列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/organization/classes/all")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAllClasses() {
        try {
            List<ClassEntity> classes = classService.getAllActiveClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取班级列表失败: " + e.getMessage()));
        }
    }
} 