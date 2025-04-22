package com.example.auth.repository;

import com.example.auth.entity.ProjectEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 课题评价仓库接口
 */
@Repository
public interface ProjectEvaluationRepository extends JpaRepository<ProjectEvaluation, Long> {
    /**
     * 通过课题ID查找评价
     */
    Optional<ProjectEvaluation> findByProjectId(Long projectId);
    
    /**
     * 通过课题ID删除评价
     */
    void deleteByProjectId(Long projectId);
} 