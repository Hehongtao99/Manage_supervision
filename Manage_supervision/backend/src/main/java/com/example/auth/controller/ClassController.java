package com.example.auth.controller;

import com.example.auth.dto.ClassDTO;
import com.example.auth.dto.ClassTeacherRelationDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.service.ClassService;
import com.example.auth.service.ClassTeacherService;
import com.example.auth.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassService classService;
    private final ClassTeacherService classTeacherService;
    private final TimetableService timetableService;

    @Autowired
    public ClassController(ClassService classService, ClassTeacherService classTeacherService, 
                          TimetableService timetableService) {
        this.classService = classService;
        this.classTeacherService = classTeacherService;
        this.timetableService = timetableService;
    }

    @PostMapping
    public ResponseEntity<ClassDTO> createClass(@RequestBody ClassDTO classDTO) {
        ClassDTO createdClass = classService.createClass(classDTO);
        return new ResponseEntity<>(createdClass, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassDTO> updateClass(@PathVariable Long id, @RequestBody ClassDTO classDTO) {
        ClassDTO updatedClass = classService.updateClass(id, classDTO);
        return ResponseEntity.ok(updatedClass);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassDTO> getClassById(@PathVariable Long id) {
        ClassDTO classDTO = classService.getClassById(id);
        return ResponseEntity.ok(classDTO);
    }

    @GetMapping
    public ResponseEntity<List<ClassDTO>> getAllClasses(
            @RequestParam(required = false) String keyword) {
        
        List<ClassDTO> classes;
        
        if (keyword != null && !keyword.isEmpty()) {
            classes = classService.searchClasses(keyword);
        } else {
            classes = classService.getAllClasses();
        }
        
        return ResponseEntity.ok(classes);
    }

    @PostMapping("/{classId}/teachers/{teacherId}")
    public ResponseEntity<ClassTeacherRelationDTO> assignTeacherToClass(
            @PathVariable Long classId,
            @PathVariable Long teacherId) {
        
        ClassTeacherRelationDTO relation = classTeacherService.assignTeacherToClass(classId, teacherId);
        return new ResponseEntity<>(relation, HttpStatus.CREATED);
    }

    @DeleteMapping("/{classId}/teachers/{teacherId}")
    public ResponseEntity<Void> removeTeacherFromClass(
            @PathVariable Long classId,
            @PathVariable Long teacherId) {
        
        classTeacherService.removeTeacherFromClass(classId, teacherId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{classId}/teachers")
    public ResponseEntity<List<ClassTeacherRelationDTO>> getTeachersByClassId(@PathVariable Long classId) {
        List<ClassTeacherRelationDTO> teachers = classTeacherService.getTeachersByClassId(classId);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ClassTeacherRelationDTO>> getClassesByTeacherId(@PathVariable Long teacherId) {
        List<ClassTeacherRelationDTO> classes = classTeacherService.getClassesByTeacherId(teacherId);
        return ResponseEntity.ok(classes);
    }
    
    // 获取班级的学生列表
    @GetMapping("/{classId}/students")
    public ResponseEntity<List<UserDTO>> getStudentsByClassId(@PathVariable Long classId) {
        List<UserDTO> students = classService.getStudentsByClassId(classId);
        return ResponseEntity.ok(students);
    }
    
    // 获取未分配到班级的学生列表
    @GetMapping("/{classId}/students/unassigned")
    public ResponseEntity<List<UserDTO>> getUnassignedStudentsByClassId(@PathVariable Long classId) {
        List<UserDTO> students = classService.getUnassignedStudentsByClassId(classId);
        return ResponseEntity.ok(students);
    }
    
    // 获取所有可用于分配的学生（包括已在其他班级的）
    @GetMapping("/{classId}/students/available")
    public ResponseEntity<List<Map<String, Object>>> getAllAvailableStudentsForClass(@PathVariable Long classId) {
        List<Map<String, Object>> students = classService.getAllAvailableStudentsForClass(classId);
        return ResponseEntity.ok(students);
    }
    
    // 添加学生到班级
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> addStudentToClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        
        boolean success = classService.addStudentToClass(classId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "学生添加成功" : "学生添加失败");
        
        return ResponseEntity.ok(response);
    }
    
    // 批量添加学生到班级
    @PostMapping("/{classId}/students")
    public ResponseEntity<Map<String, Object>> addStudentsToClass(
            @PathVariable Long classId,
            @RequestBody List<Long> studentIds) {
        
        Map<String, Object> result = classService.addStudentsToClass(classId, studentIds);
        return ResponseEntity.ok(result);
    }
    
    // 从班级中移除学生
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudentFromClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        
        boolean success = classService.removeStudentFromClass(classId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "学生移除成功" : "学生移除失败");
        
        return ResponseEntity.ok(response);
    }
    
    // 获取当前教师的班级列表
    @GetMapping("/teacher/classes")
    public ResponseEntity<List<Map<String, Object>>> getTeacherClasses(
            @RequestHeader(value = "userId", required = false) Long headerUserId,
            @RequestParam(required = false) Long teacherId) {
        try {
            Long currentUserId;
            if (teacherId != null) {
                // 如果提供了teacherId参数，使用该参数
                currentUserId = teacherId;
                System.out.println("使用请求参数提供的教师ID: " + teacherId);
            } else if (headerUserId != null) {
                // 使用请求头中的userId
                currentUserId = headerUserId;
                System.out.println("使用请求头提供的用户ID: " + headerUserId);
            } else {
                // 否则尝试获取当前登录用户ID
                try {
                    currentUserId = classService.getCurrentUserId();
                    System.out.println("使用当前登录用户ID: " + currentUserId);
                } catch (RuntimeException e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(List.of(Map.of("error", e.getMessage())));
                }
            }

            List<Map<String, Object>> classes = classService.getClassesByTeacherId(currentUserId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            System.err.println("获取教师班级失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(List.of(Map.of("error", e.getMessage())));
        }
    }

    // 获取班级课表 - 处理小程序端请求
    @GetMapping("/{classId}/timetable")
    public ResponseEntity<?> getClassTimetable(@PathVariable Long classId) {
        // 获取班级最新的课表
        List<Map<String, Object>> timetableData = timetableService.getLatestTimetableWithItemsByClassId(classId);
        Map<String, Object> response = new HashMap<>();
        response.put("timetable", timetableData);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 