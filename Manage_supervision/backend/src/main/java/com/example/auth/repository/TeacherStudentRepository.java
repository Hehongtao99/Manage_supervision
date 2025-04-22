package com.example.auth.repository;

import com.example.auth.entity.TeacherStudentRelation;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherStudentRepository extends JpaRepository<TeacherStudentRelation, Long> {
    
    List<TeacherStudentRelation> findByTeacher(User teacher);
    
    List<TeacherStudentRelation> findByStudent(User student);
    
    @Query("SELECT COUNT(r) > 0 FROM TeacherStudentRelation r WHERE r.teacher = :teacher AND r.student = :student")
    boolean existsByTeacherAndStudent(@Param("teacher") User teacher, @Param("student") User student);
    
    Optional<TeacherStudentRelation> findByTeacherAndStudent(User teacher, User student);
    
    @Query("SELECT r.student FROM TeacherStudentRelation r WHERE r.teacher = :teacher AND r.status = 'active'")
    List<User> findActiveStudentsByTeacher(@Param("teacher") User teacher);
    
    @Query("SELECT r.teacher FROM TeacherStudentRelation r WHERE r.student = :student AND r.status = 'active'")
    List<User> findActiveTeachersByStudent(@Param("student") User student);

    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT r.student.id FROM TeacherStudentRelation r WHERE r.status = 'active') AND :roleId IN (SELECT role.id FROM u.roles role)")
    List<User> findUnassignedStudentsByRoleId(@Param("roleId") Long roleId);
} 