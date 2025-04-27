package com.example.auth.repository;

import com.example.auth.entity.QuestionBankQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface QuestionBankQuestionRepository extends JpaRepository<QuestionBankQuestion, Long> {
    
    List<QuestionBankQuestion> findByQuestionBankId(Long questionBankId);
    
    List<QuestionBankQuestion> findByQuestionId(Long questionId);
    
    @Query("SELECT qbq.questionId FROM QuestionBankQuestion qbq WHERE qbq.questionBankId = :bankId")
    List<Long> findQuestionIdsByBankId(@Param("bankId") Long questionBankId);
    
    boolean existsByQuestionBankIdAndQuestionId(Long questionBankId, Long questionId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM QuestionBankQuestion qbq WHERE qbq.questionBankId = :bankId AND qbq.questionId = :questionId")
    void deleteByQuestionBankIdAndQuestionId(@Param("bankId") Long questionBankId, @Param("questionId") Long questionId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM QuestionBankQuestion qbq WHERE qbq.questionBankId = :bankId")
    void deleteByQuestionBankId(@Param("bankId") Long questionBankId);
    
    @Query("SELECT COUNT(qbq) FROM QuestionBankQuestion qbq WHERE qbq.questionBankId = :bankId")
    long countByQuestionBankId(@Param("bankId") Long questionBankId);
} 