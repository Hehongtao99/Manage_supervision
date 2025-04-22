package com.example.auth.repository;

import com.example.auth.entity.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务提交数据访问层
 */
@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    
    /**
     * 根据任务ID查询提交记录
     */
    List<TaskSubmission> findByTaskId(Long taskId);
    
    /**
     * 根据任务ID和提交者ID查询提交记录
     */
    List<TaskSubmission> findByTaskIdAndSubmitterId(Long taskId, Long submitterId);
    
    /**
     * 查找任务最新的提交记录
     */
    Optional<TaskSubmission> findFirstByTaskIdOrderBySubmissionTimeDesc(Long taskId);
    
    /**
     * 查找用户所有提交记录
     */
    List<TaskSubmission> findBySubmitterId(Long submitterId);
    
    /**
     * 统计任务的提交次数
     */
    long countByTaskId(Long taskId);
    
    /**
     * 检查任务是否有提交记录
     */
    boolean existsByTaskId(Long taskId);
    
    /**
     * 根据任务ID列表统计有提交记录的任务数量
     */
    long countByTaskIdIn(List<Long> taskIds);
} 