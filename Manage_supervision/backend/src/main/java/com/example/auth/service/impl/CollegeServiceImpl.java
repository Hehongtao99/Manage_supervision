package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.CollegeMapper;
import com.example.auth.model.College;
import com.example.auth.service.CollegeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {
    
    @Override
    public Page<College> getCollegePage(int current, int size, String collegeName) {
        Page<College> page = new Page<>(current, size);
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getStatus, "active")
               .like(collegeName != null && !collegeName.trim().isEmpty(), College::getCollegeName, collegeName)
               .orderByDesc(College::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public boolean saveCollege(College college) {
        college.setStatus("active");
        college.setCreateTime(LocalDateTime.now());
        college.setUpdateTime(LocalDateTime.now());
        return this.save(college);
    }
    
    @Override
    public boolean updateCollege(College college) {
        return this.updateById(college);
    }
    
    @Override
    public boolean deleteCollege(Long id) {
        College college = new College();
        college.setId(id);
        college.setStatus("inactive");
        return this.updateById(college);
    }
    
    @Override
    public List<College> getAllActiveColleges() {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getStatus, "active")
               .orderBy(true, true, College::getCollegeName);
        return this.list(wrapper);
    }
} 