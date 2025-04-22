package com.example.auth.service;

import com.example.auth.dto.ClassTeacherRelationDTO;
import java.util.List;

public interface ClassTeacherService {
    ClassTeacherRelationDTO assignTeacherToClass(Long classId, Long teacherId);
    void removeTeacherFromClass(Long classId, Long teacherId);
    List<ClassTeacherRelationDTO> getTeachersByClassId(Long classId);
    List<ClassTeacherRelationDTO> getClassesByTeacherId(Long teacherId);
} 