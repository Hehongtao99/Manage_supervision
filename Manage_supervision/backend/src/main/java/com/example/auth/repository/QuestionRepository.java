package com.example.auth.repository;

import com.example.auth.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    Page<Question> findByCreatorId(Long creatorId, Pageable pageable);
    
    Page<Question> findByTypeAndCreatorId(String type, Long creatorId, Pageable pageable);
    
    @Query("SELECT q FROM Question q WHERE q.id IN :ids")
    List<Question> findByQuestionIds(@Param("ids") List<Long> questionIds);
    
    @Query("SELECT q FROM Question q JOIN QuestionBankQuestion qbq ON q.id = qbq.questionId WHERE qbq.questionBankId = :bankId")
    Page<Question> findByQuestionBankId(@Param("bankId") Long questionBankId, Pageable pageable);
    
    @Query("SELECT q FROM Question q JOIN QuestionBankQuestion qbq ON q.id = qbq.questionId WHERE qbq.questionBankId = :bankId AND q.type = :type")
    Page<Question> findByQuestionBankIdAndType(@Param("bankId") Long questionBankId, @Param("type") String type, Pageable pageable);
    
    @Query("SELECT q FROM Question q WHERE " +
           "(LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "q.creatorId = :creatorId")
    Page<Question> searchQuestions(@Param("keyword") String keyword, @Param("creatorId") Long creatorId, Pageable pageable);
} 