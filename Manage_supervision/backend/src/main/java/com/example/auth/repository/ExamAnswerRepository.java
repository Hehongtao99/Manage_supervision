package com.example.auth.repository;

import com.example.auth.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 考试答案仓库接口
 */
@Repository
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    
    /**
     * 根据考试ID和学生ID查询答案
     */
    List<ExamAnswer> findByExamIdAndStudentId(Long examId, Long studentId);
    
    /**
     * 根据考试ID、学生ID和题目ID查询答案
     */
    ExamAnswer findByExamIdAndStudentIdAndQuestionId(Long examId, Long studentId, Long questionId);
    
    /**
     * 根据考试ID查询所有答案
     */
    List<ExamAnswer> findByExamId(Long examId);
    
    /**
     * 根据题目ID查询所有答案
     */
    List<ExamAnswer> findByQuestionId(Long questionId);
    
    /**
     * 删除考试的所有答案
     */
    void deleteByExamId(Long examId);
    
    /**
     * 删除学生在考试中的所有答案
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ExamAnswer ea WHERE ea.examId = :examId AND ea.studentId = :studentId")
    void deleteByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);
    
    /**
     * 统计学生在考试中的答案数量
     */
    Long countByExamIdAndStudentId(Long examId, Long studentId);
    
    /**
     * 统计学生在考试中的正确答案数量
     */
    @Query("SELECT COUNT(ea) FROM ExamAnswer ea WHERE ea.examId = :examId AND ea.studentId = :studentId AND ea.isCorrect = true")
    Long countCorrectAnswersByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);
} 