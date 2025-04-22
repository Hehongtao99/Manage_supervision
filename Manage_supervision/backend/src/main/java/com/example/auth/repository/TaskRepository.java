package com.example.auth.repository;

import com.example.auth.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务数据访问层
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // 根据督导ID查询任务
    List<Task> findBySupervisorId(Long supervisorId);
    
    // 根据学生ID查询任务
    List<Task> findByAssigneeId(Long assigneeId);
    
    // 根据督导ID和状态查询任务
    List<Task> findBySupervisorIdAndStatus(Long supervisorId, String status);
    
    // 根据学生ID和状态查询任务
    List<Task> findByAssigneeIdAndStatus(Long assigneeId, String status);
    
    // 根据项目ID查询任务
    List<Task> findByProjectId(Long projectId);
    
    // 根据项目ID和完成状态查询任务
    List<Task> findByProjectIdAndCompleted(Long projectId, Boolean completed);
    
    // 根据项目ID查询未完成的任务数量
    long countByProjectIdAndCompleted(Long projectId, Boolean completed);
    
    // 查询项目是否所有任务都已完成
    @Query("SELECT COUNT(t) = 0 FROM Task t WHERE t.projectId = ?1 AND t.completed = false")
    boolean areAllTasksCompletedForProject(Long projectId);
    
    // 根据督导ID查询不属于任何课题的任务
    List<Task> findBySupervisorIdAndProjectIdIsNull(Long supervisorId);
    
    // 根据督导ID和状态查询不属于任何课题的任务
    List<Task> findBySupervisorIdAndStatusAndProjectIdIsNull(Long supervisorId, String status);
} 