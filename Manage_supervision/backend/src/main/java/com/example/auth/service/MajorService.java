package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.auth.model.Major;

import java.util.List;

public interface MajorService extends IService<Major> {
    
    Page<Major> getMajorPage(int current, int size, String majorName, Long collegeId);
    
    boolean saveMajor(Major major);
    
    boolean updateMajor(Major major);
    
    boolean deleteMajor(Long id);
    
    List<Major> getMajorsByCollegeId(Long collegeId);
} 