package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.auth.model.ClassEntity;

import java.util.List;

public interface ClassService extends IService<ClassEntity> {
    
    Page<ClassEntity> getClassPage(int current, int size, String className, Long collegeId, Long majorId);
    
    boolean saveClass(ClassEntity classEntity);
    
    boolean updateClass(ClassEntity classEntity);
    
    boolean deleteClass(Long id);
    
    List<ClassEntity> getClassesByMajorId(Long majorId);
    
    List<ClassEntity> getClassesByCollegeId(Long collegeId);
}