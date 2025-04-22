package com.example.auth.service;

import com.example.auth.dto.ClassDTO;
import java.util.List;

public interface ClassService {
    ClassDTO createClass(ClassDTO classDTO);
    ClassDTO updateClass(Long id, ClassDTO classDTO);
    void deleteClass(Long id);
    ClassDTO getClassById(Long id);
    List<ClassDTO> getAllClasses();
    List<ClassDTO> searchClasses(String keyword);
} 