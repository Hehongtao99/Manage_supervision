package com.example.auth.repository;

import com.example.auth.entity.QuestionBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {
    
    Page<QuestionBank> findByCreatorIdAndStatusOrderByCreateTimeDesc(Long creatorId, String status, Pageable pageable);
    
    @Query("SELECT qb FROM QuestionBank qb WHERE " +
           "(LOWER(qb.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(qb.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "qb.creatorId = :creatorId AND qb.status = :status")
    Page<QuestionBank> searchQuestionBanks(
            @Param("keyword") String keyword, 
            @Param("creatorId") Long creatorId, 
            @Param("status") String status, 
            Pageable pageable);
} 