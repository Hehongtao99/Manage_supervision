package com.example.auth.controller;

import com.example.auth.entity.StudentClassHistory;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.StudentClassHistoryRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student-class-history")
public class ClassStudentController {
    
    private final StudentClassHistoryRepository studentClassHistoryRepository;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    
    @Autowired
    public ClassStudentController(
            StudentClassHistoryRepository studentClassHistoryRepository,
            UserRepository userRepository,
            ClassRepository classRepository) {
        this.studentClassHistoryRepository = studentClassHistoryRepository;
        this.userRepository = userRepository;
        this.classRepository = classRepository;
    }
    
    /**
     * 获取学生的班级变更历史
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentHistory(@PathVariable Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        List<StudentClassHistory> histories = studentClassHistoryRepository.findByStudentOrderByOperationTimeDesc(student);
        List<Map<String, Object>> result = histories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取班级的学生变更历史
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getClassHistory(@PathVariable Long classId) {
        com.example.auth.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        List<StudentClassHistory> histories = studentClassHistoryRepository.findByClassEntityOrderByOperationTimeDesc(classEntity);
        List<Map<String, Object>> result = histories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取最近的班级变更历史
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentHistory(@RequestParam(defaultValue = "10") int limit) {
        List<StudentClassHistory> histories = studentClassHistoryRepository.findRecentHistory(PageRequest.of(0, limit));
        List<Map<String, Object>> result = histories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    private Map<String, Object> convertToMap(StudentClassHistory history) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", history.getId());
        map.put("studentId", history.getStudent().getId());
        map.put("studentName", history.getStudent().getRealName());
        map.put("studentNumber", history.getStudent().getUserNumber());
        map.put("classId", history.getClassEntity().getId());
        map.put("className", history.getClassEntity().getClassName());
        map.put("operationType", history.getOperationType());
        map.put("operationTime", history.getOperationTime());
        map.put("operatorId", history.getOperator().getId());
        map.put("operatorName", history.getOperator().getRealName());
        map.put("remark", history.getRemark());
        return map;
    }
} 
