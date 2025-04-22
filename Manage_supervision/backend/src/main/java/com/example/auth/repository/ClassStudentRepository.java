package com.example.auth.repository;

import com.example.auth.entity.Class;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ClassStudentRepository extends JpaRepository<ClassStudentRelation, Long> {
    
    List<ClassStudentRelation> findByClassEntityAndStatus(Class classEntity, String status);
    
    List<ClassStudentRelation> findByStudentAndStatus(User student, String status);
    
    Optional<ClassStudentRelation> findByClassEntityAndStudent(Class classEntity, User student);
    
    boolean existsByClassEntityAndStudent(Class classEntity, User student);
    
    @Query("SELECT csr.student FROM ClassStudentRelation csr WHERE csr.classEntity.id = :classId AND csr.status = 'active'")
    List<User> findActiveStudentsByClassId(@Param("classId") Long classId);
    
    @Query("SELECT csr.classEntity FROM ClassStudentRelation csr WHERE csr.student.id = :studentId AND csr.status = 'active'")
    List<Class> findActiveClassesByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
           "(SELECT csr.student.id FROM ClassStudentRelation csr WHERE csr.classEntity.id = :classId AND csr.status = 'active') " +
           "AND EXISTS (SELECT r FROM u.roles r WHERE r.name = 'USER')")
    List<User> findUnassignedStudentsByClassId(@Param("classId") Long classId);
    
    /**
     * 查找所有可用于班级分配的学生（包括已分配的）
     */
    @Query("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT r FROM u.roles r WHERE r.name = 'USER') " +
           "AND u.id NOT IN (SELECT csr.student.id FROM ClassStudentRelation csr " + 
           "WHERE csr.classEntity.id = :classId AND csr.status = 'active')")
    List<User> findAllAvailableStudentsForClass(@Param("classId") Long classId);
    
    /**
     * 查找学生当前所在的班级
     */
    @Query("SELECT c FROM Class c JOIN ClassStudentRelation csr ON c.id = csr.classEntity.id " +
           "WHERE csr.student.id = :studentId AND csr.status = 'active'")
    Optional<Class> findActiveClassByStudentId(@Param("studentId") Long studentId);
} 