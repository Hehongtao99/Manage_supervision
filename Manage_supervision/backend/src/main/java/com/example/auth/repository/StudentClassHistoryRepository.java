package com.example.auth.repository;

import com.example.auth.entity.Class;
import com.example.auth.entity.StudentClassHistory;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface StudentClassHistoryRepository extends JpaRepository<StudentClassHistory, Long> {

    /**
     * 查找学生的班级变更历史
     */
    List<StudentClassHistory> findByStudentOrderByOperationTimeDesc(User student);
    
    /**
     * 查找班级的学生变更历史
     */
    List<StudentClassHistory> findByClassEntityOrderByOperationTimeDesc(Class classEntity);
    
    /**
     * 查找最近的班级变更历史
     */
    @Query("SELECT sch FROM StudentClassHistory sch ORDER BY sch.operationTime DESC")
    List<StudentClassHistory> findRecentHistory(Pageable pageable);
    
    /**
     * 按操作类型查找学生的班级变更历史
     */
    List<StudentClassHistory> findByStudentAndOperationTypeOrderByOperationTimeDesc(User student, String operationType);
} 