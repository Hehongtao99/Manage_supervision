package com.example.auth.repository;

import com.example.auth.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    /**
     * 根据创建者ID查询考试列表
     */
    List<Exam> findByCreatorId(Long creatorId);
    
    /**
     * 根据创建者ID查询考试列表（分页）
     */
    Page<Exam> findByCreatorId(Long creatorId, Pageable pageable);
    
    /**
     * 根据创建者ID和状态查询考试列表
     */
    List<Exam> findByCreatorIdAndStatus(Long creatorId, String status);
    
    /**
     * 根据创建者ID和状态查询考试列表（分页）
     */
    Page<Exam> findByCreatorIdAndStatus(Long creatorId, String status, Pageable pageable);
    
    /**
     * 根据状态查询考试列表
     */
    List<Exam> findByStatus(String status);
    
    /**
     * 根据状态查询考试列表（分页）
     */
    Page<Exam> findByStatus(String status, Pageable pageable);
    
    /**
     * 根据标题模糊搜索考试
     */
    @Query("SELECT e FROM Exam e WHERE e.creatorId = :creatorId AND e.title LIKE %:keyword%")
    Page<Exam> searchByTitleAndCreatorId(@Param("keyword") String keyword, @Param("creatorId") Long creatorId, Pageable pageable);
    
    /**
     * 根据ID集合查询考试列表
     */
    List<Exam> findByIdIn(List<Long> ids);
} 