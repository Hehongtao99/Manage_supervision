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
import java.util.Set;

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
     * 统计指定考试中处于特定状态的学生数量
     */
    Long countByExamIdAndStatus(Long examId, String status);

    /**
     * 统计已提交待批阅的学生数量 (SUBMITTED)
     */
    default Long countSubmittedByExamId(Long examId) {
        return countByExamIdAndStatus(examId, "SUBMITTED");
    }

    /**
     * 统计待发布成绩的学生数量 (PENDING_PUBLISH)
     */
    default Long countPendingPublishByExamId(Long examId) {
        return countByExamIdAndStatus(examId, "PENDING_PUBLISH");
    }

    /**
     * 统计已发布成绩的学生数量 (PUBLISHED)
     */
    default Long countPublishedByExamId(Long examId) {
        return countByExamIdAndStatus(examId, "PUBLISHED");
    }

    /**
     * 根据考试ID和状态列表查询学生考试记录
     */
    List<ExamStudent> findByExamIdAndStatusIn(Long examId, List<String> statuses);
    
    /**
     * 根据考试ID和状态列表查询学生考试记录（分页）
     */
    Page<ExamStudent> findByExamIdAndStatusIn(Long examId, List<String> statuses, Pageable pageable);
    
    /**
     * 根据多个考试ID和状态查询学生考试记录
     */
    List<ExamStudent> findByExamIdInAndStatus(List<Long> examIds, String status);
    
    /**
     * 统计学生参加的考试数量
     */
    Long countByStudentId(Long studentId);

    /**
     * 根据考试ID和状态查询学生考试记录（分页）
     */
    Page<ExamStudent> findByExamIdAndStatus(Long examId, String status, Pageable pageable);

    /**
     * 根据多个考试ID查询学生考试记录
     */
    List<ExamStudent> findByExamIdIn(List<Long> examIds);

    /**
     * 根据考试ID和状态查询学生考试记录列表（非分页）
     */
    List<ExamStudent> findAllByExamIdAndStatus(Long examId, String status);

    /**
     * 根据多个考试ID查询唯一的学生ID列表
     */
    @Query("SELECT DISTINCT es.studentId FROM ExamStudent es WHERE es.examId IN :examIds")
    Set<Long> findDistinctStudentIdsByExamIdIn(@Param("examIds") List<Long> examIds);
} 