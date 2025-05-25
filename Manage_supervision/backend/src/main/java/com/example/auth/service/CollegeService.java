package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.auth.model.College;

import java.util.List;

public interface CollegeService extends IService<College> {
    
    Page<College> getCollegePage(int current, int size, String collegeName);
    
    boolean saveCollege(College college);
    
    boolean updateCollege(College college);
    
    boolean deleteCollege(Long id);
    
    List<College> getAllActiveColleges();
} 