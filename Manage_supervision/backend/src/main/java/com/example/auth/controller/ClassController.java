package com.example.auth.controller;

import com.example.auth.dto.ClassDTO;
import com.example.auth.dto.ClassTeacherRelationDTO;
import com.example.auth.service.ClassService;
import com.example.auth.service.ClassTeacherService;
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

    @Autowired
    public ClassController(ClassService classService, ClassTeacherService classTeacherService) {
        this.classService = classService;
        this.classTeacherService = classTeacherService;
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 