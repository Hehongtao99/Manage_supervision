package com.example.auth.service.impl;

import com.example.auth.dto.ClassDTO;
import com.example.auth.entity.Class;
import com.example.auth.repository.ClassRepository;
import com.example.auth.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;

    @Autowired
    public ClassServiceImpl(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    @Override
    @Transactional
    public ClassDTO createClass(ClassDTO classDTO) {
        Class classEntity = classDTO.toEntity();
        classEntity = classRepository.save(classEntity);
        return new ClassDTO(classEntity);
    }

    @Override
    @Transactional
    public ClassDTO updateClass(Long id, ClassDTO classDTO) {
        Class existingClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + id));
        
        existingClass.setClassName(classDTO.getClassName());
        existingClass.setGrade(classDTO.getGrade());
        existingClass.setDescription(classDTO.getDescription());
        
        existingClass = classRepository.save(existingClass);
        return new ClassDTO(existingClass);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        classRepository.deleteById(id);
    }

    @Override
    public ClassDTO getClassById(Long id) {
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + id));
        return new ClassDTO(classEntity);
    }

    @Override
    public List<ClassDTO> getAllClasses() {
        return classRepository.findAll().stream()
                .map(ClassDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassDTO> searchClasses(String keyword) {
        return classRepository.findByClassNameContaining(keyword).stream()
                .map(ClassDTO::new)
                .collect(Collectors.toList());
    }
} 