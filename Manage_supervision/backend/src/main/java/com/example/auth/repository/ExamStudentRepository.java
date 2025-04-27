package com.example.auth.repository;

import com.example.auth.entity.ExamStudent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamStudentRepository extends JpaRepository<ExamStudent, Long> {
    
    /**
     * 根据考试ID查询学生列表
     */
    List<ExamStudent> findByExamId(Long examId);
    
    /**
     * 根据学生ID查询参加的考试
     */
    List<ExamStudent> findByStudentId(Long studentId);
    
    /**
     * 根据学生ID查询参加的考试（分页）
     */
    Page<ExamStudent> findByStudentId(Long studentId, Pageable pageable);
    
    /**
     * 根据学生ID和考试状态查询参加的考试
     */
    List<ExamStudent> findByStudentIdAndStatus(Long studentId, String status);
    
    /**
     * 根据学生ID和考试状态查询参加的考试（分页）
     */
    Page<ExamStudent> findByStudentIdAndStatus(Long studentId, String status, Pageable pageable);
    
    /**
     * 根据考试ID和学生ID查询
     */
    Optional<ExamStudent> findByExamIdAndStudentId(Long examId, Long studentId);
    
    /**
     * 检查学生是否已被添加到考试中
     */
    boolean existsByExamIdAndStudentId(Long examId, Long studentId);
    
    /**
     * 删除考试的所有学生
     */
    void deleteByExamId(Long examId);
    
    /**
     * 统计考试的学生数量
     */
    Long countByExamId(Long examId);
    
    /**
     * 统计已提交的学生数量
     */
    Long countByExamIdAndStatusIn(Long examId, List<String> statuses);
    
    /**
     * 统计已批改的学生数量
     */
    @Query("SELECT COUNT(es) FROM ExamStudent es WHERE es.examId = :examId AND es.status = 'GRADED'")
    Long countGradedByExamId(@Param("examId") Long examId);
    
    /**
     * 统计学生参加的考试数量
     */
    Long countByStudentId(Long studentId);
} 