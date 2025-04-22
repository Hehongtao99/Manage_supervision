package com.example.auth.service.impl;

import com.example.auth.dto.ClassTeacherRelationDTO;
import com.example.auth.entity.Class;
import com.example.auth.entity.ClassTeacherRelation;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.ClassTeacherRelationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ClassTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassTeacherServiceImpl implements ClassTeacherService {

    private final ClassTeacherRelationRepository classTeacherRelationRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Autowired
    public ClassTeacherServiceImpl(
            ClassTeacherRelationRepository classTeacherRelationRepository,
            ClassRepository classRepository,
            UserRepository userRepository) {
        this.classTeacherRelationRepository = classTeacherRelationRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ClassTeacherRelationDTO assignTeacherToClass(Long classId, Long teacherId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("教师不存在: " + teacherId));
        
        // 检查是否已经存在关联
        if (classTeacherRelationRepository.existsByClassEntityAndTeacherAndStatus(classEntity, teacher, "active")) {
            throw new RuntimeException("该教师已分配到此班级");
        }
        
        ClassTeacherRelation relation = new ClassTeacherRelation();
        relation.setClassEntity(classEntity);
        relation.setTeacher(teacher);
        relation.setStatus("active");
        
        relation = classTeacherRelationRepository.save(relation);
        
        return new ClassTeacherRelationDTO(relation);
    }

    @Override
    @Transactional
    public void removeTeacherFromClass(Long classId, Long teacherId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("教师不存在: " + teacherId));
        
        classTeacherRelationRepository.deleteByClassEntityAndTeacher(classEntity, teacher);
    }

    @Override
    public List<ClassTeacherRelationDTO> getTeachersByClassId(Long classId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        return classTeacherRelationRepository.findByClassEntityAndStatus(classEntity, "active")
                .stream()
                .map(ClassTeacherRelationDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassTeacherRelationDTO> getClassesByTeacherId(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("教师不存在: " + teacherId));
        
        return classTeacherRelationRepository.findByTeacherAndStatus(teacher, "active")
                .stream()
                .map(ClassTeacherRelationDTO::new)
                .collect(Collectors.toList());
    }
} 