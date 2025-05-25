package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.MajorMapper;
import com.example.auth.model.Major;
import com.example.auth.service.MajorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
    
    @Override
    public Page<Major> getMajorPage(int current, int size, String majorName, Long collegeId) {
        Page<Major> page = new Page<>(current, size);
        return baseMapper.selectMajorPage(page, majorName, collegeId);
    }
    
    @Override
    public boolean saveMajor(Major major) {
        major.setStatus("active");
        major.setCreateTime(LocalDateTime.now());
        major.setUpdateTime(LocalDateTime.now());
        return this.save(major);
    }
    
    @Override
    public boolean updateMajor(Major major) {
        return this.updateById(major);
    }
    
    @Override
    public boolean deleteMajor(Long id) {
        Major major = new Major();
        major.setId(id);
        major.setStatus("inactive");
        return this.updateById(major);
    }
    
    @Override
    public List<Major> getMajorsByCollegeId(Long collegeId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getStatus, "active")
               .eq(collegeId != null, Major::getCollegeId, collegeId)
               .orderBy(true, true, Major::getMajorName);
        return this.list(wrapper);
    }
    
    @Override
    public List<Major> getAllActiveMajors() {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getStatus, "active")
               .orderBy(true, true, Major::getMajorName);
        return this.list(wrapper);
    }
} 