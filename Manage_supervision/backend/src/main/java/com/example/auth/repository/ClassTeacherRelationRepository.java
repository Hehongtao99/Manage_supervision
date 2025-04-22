package com.example.auth.repository;

import com.example.auth.entity.Class;
import com.example.auth.entity.ClassTeacherRelation;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassTeacherRelationRepository extends JpaRepository<ClassTeacherRelation, Long> {
    List<ClassTeacherRelation> findByTeacherAndStatus(User teacher, String status);
    List<ClassTeacherRelation> findByClassEntityAndStatus(Class classEntity, String status);
    boolean existsByClassEntityAndTeacherAndStatus(Class classEntity, User teacher, String status);
    void deleteByClassEntityAndTeacher(Class classEntity, User teacher);
} 