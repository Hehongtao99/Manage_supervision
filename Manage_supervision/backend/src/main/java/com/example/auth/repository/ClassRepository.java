package com.example.auth.repository;

import com.example.auth.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findByClassNameContaining(String keyword);
    
    /**
     * 根据学生ID查询班级名称
     */
    @Query("SELECT c.className FROM Class c JOIN ClassStudentRelation csr ON c.id = csr.classEntity.id " +
           "WHERE csr.student.id = :studentId AND csr.status = 'active'")
    String findClassNameByStudentId(@Param("studentId") Long studentId);
} 