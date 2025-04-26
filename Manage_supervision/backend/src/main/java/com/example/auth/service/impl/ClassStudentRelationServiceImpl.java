package com.example.auth.service.impl;

import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassStudentRelationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ClassStudentRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 班级学生关系服务实现类
 */
@Service
@Slf4j
public class ClassStudentRelationServiceImpl implements ClassStudentRelationService {

    @Autowired
    private ClassStudentRelationRepository classStudentRelationRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<ClassStudentRelation> findActiveRelationByStudentId(Long studentId) {
        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User student = studentOpt.get();
        List<ClassStudentRelation> relations = classStudentRelationRepository.findByStudentAndStatus(student, "active");
        
        if (relations.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(relations.get(0));
    }

    @Override
    public List<ClassStudentRelation> findActiveStudentsByClassId(Long classId) {
        return classStudentRelationRepository.findByClassEntityIdAndStatus(classId, "active");
    }
} 