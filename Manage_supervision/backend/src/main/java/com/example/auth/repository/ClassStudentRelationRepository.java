package com.example.auth.repository;

import com.example.auth.entity.Class;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassStudentRelationRepository extends JpaRepository<ClassStudentRelation, Long> {
    
    /**
     * 根据班级ID查找所有班级学生关系
     */
    List<ClassStudentRelation> findByClassEntityId(Long classId);
    
    /**
     * 根据班级ID查找所有班级学生关系（用于生成账单）
     */
    @Query("SELECT csr FROM ClassStudentRelation csr WHERE csr.classEntity.id = :classId AND csr.status = 'active'")
    List<ClassStudentRelation> findByClassId(@Param("classId") Long classId);
    
    /**
     * 根据班级和状态查找班级学生关系
     */
    List<ClassStudentRelation> findByClassEntityAndStatus(Class classEntity, String status);
    
    /**
     * 根据学生和状态查找班级学生关系
     */
    List<ClassStudentRelation> findByStudentAndStatus(User student, String status);
    
    /**
     * 根据班级和学生查找班级学生关系
     */
    Optional<ClassStudentRelation> findByClassEntityAndStudent(Class classEntity, User student);
    
    /**
     * 检查班级和学生的关系是否存在
     */
    boolean existsByClassEntityAndStudent(Class classEntity, User student);
    
    /**
     * 查找班级中所有活跃的学生
     */
    @Query("SELECT csr.student FROM ClassStudentRelation csr WHERE csr.classEntity.id = :classId AND csr.status = 'active'")
    List<User> findActiveStudentsByClassId(@Param("classId") Long classId);
    
    /**
     * 查找学生所在的所有活跃班级
     */
    @Query("SELECT csr.classEntity FROM ClassStudentRelation csr WHERE csr.student.id = :studentId AND csr.status = 'active'")
    List<Class> findActiveClassesByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查找学生当前所在的班级
     */
    @Query("SELECT c FROM Class c JOIN ClassStudentRelation csr ON c.id = csr.classEntity.id " +
           "WHERE csr.student.id = :studentId AND csr.status = 'active'")
    Optional<Class> findActiveClassByStudentId(@Param("studentId") Long studentId);
} 