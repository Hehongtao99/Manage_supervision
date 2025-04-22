package com.example.auth.repository;

import com.example.auth.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课题数据访问层
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // 根据督导ID查询课题
    List<Project> findBySupervisorId(Long supervisorId);
    
    // 根据学生ID查询课题
    List<Project> findByAssigneeId(Long assigneeId);
    
    // 根据督导ID和状态查询课题
    List<Project> findBySupervisorIdAndStatus(Long supervisorId, String status);
    
    // 根据学生ID和状态查询课题
    List<Project> findByAssigneeIdAndStatus(Long assigneeId, String status);
    
    // 根据类别查询课题
    List<Project> findByCategory(String category);
} 