package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.ClassMapper;
import com.example.auth.model.ClassEntity;
import com.example.auth.service.ClassService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassEntity> implements ClassService {
    
    @Override
    public Page<ClassEntity> getClassPage(int current, int size, String className, Long collegeId, Long majorId) {
        Page<ClassEntity> page = new Page<>(current, size);
        return baseMapper.selectClassPage(page, className, collegeId, majorId);
    }
    
    @Override
    public boolean saveClass(ClassEntity classEntity) {
        classEntity.setStatus("active");
        classEntity.setStudentCount(0);
        classEntity.setCreateTime(LocalDateTime.now());
        classEntity.setUpdateTime(LocalDateTime.now());
        return this.save(classEntity);
    }
    
    @Override
    public boolean updateClass(ClassEntity classEntity) {
        return this.updateById(classEntity);
    }
    
    @Override
    public boolean deleteClass(Long id) {
        ClassEntity classEntity = new ClassEntity();
        classEntity.setId(id);
        classEntity.setStatus("inactive");
        return this.updateById(classEntity);
    }
    
    @Override
    public List<ClassEntity> getClassesByMajorId(Long majorId) {
        LambdaQueryWrapper<ClassEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassEntity::getStatus, "active")
               .eq(majorId != null, ClassEntity::getMajorId, majorId)
               .orderBy(true, true, ClassEntity::getClassName);
        return this.list(wrapper);
    }
    
    @Override
    public List<ClassEntity> getClassesByCollegeId(Long collegeId) {
        LambdaQueryWrapper<ClassEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassEntity::getStatus, "active")
               .eq(collegeId != null, ClassEntity::getCollegeId, collegeId)
               .orderBy(true, true, ClassEntity::getClassName);
        return this.list(wrapper);
    }
    
    @Override
    public List<ClassEntity> getAllActiveClasses() {
        LambdaQueryWrapper<ClassEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassEntity::getStatus, "active")
               .orderBy(true, true, ClassEntity::getClassName);
        return this.list(wrapper);
    }
} 