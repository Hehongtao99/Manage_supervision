package com.example.auth.repository;

import com.example.auth.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    
    /**
     * 根据考试ID查询题目列表
     */
    List<ExamQuestion> findByExamId(Long examId);
    
    /**
     * 根据考试ID和题目ID查询
     */
    ExamQuestion findByExamIdAndQuestionId(Long examId, Long questionId);
    
    /**
     * 根据考试ID查询题目列表并按显示顺序排序
     */
    List<ExamQuestion> findByExamIdOrderByDisplayOrderAsc(Long examId);
    
    /**
     * 删除考试的所有题目
     */
    void deleteByExamId(Long examId);
    
    /**
     * 检查题目是否已被添加到考试中
     */
    boolean existsByExamIdAndQuestionId(Long examId, Long questionId);
    
    /**
     * 查询考试题目数量
     */
    @Query("SELECT COUNT(eq) FROM ExamQuestion eq WHERE eq.examId = :examId")
    Long countByExamId(@Param("examId") Long examId);
} 