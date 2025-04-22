package com.example.auth.repository;

import com.example.auth.entity.TaskEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 任务评价数据库操作接口
 */
@Repository
public interface TaskEvaluationRepository extends JpaRepository<TaskEvaluation, Long> {
    
    /**
     * 根据任务ID查找评价
     */
    Optional<TaskEvaluation> findByTaskId(Long taskId);
    
    /**
     * 根据任务ID删除评价
     */
    void deleteByTaskId(Long taskId);
} 