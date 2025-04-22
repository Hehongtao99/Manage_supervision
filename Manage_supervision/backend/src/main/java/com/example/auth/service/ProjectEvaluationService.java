package com.example.auth.service;

import com.example.auth.dto.ProjectEvaluationDTO;

/**
 * 课题评价服务接口
 */
public interface ProjectEvaluationService {
    
    /**
     * 创建或更新课题评价
     * @param projectId 课题ID
     * @param score 评分
     * @param comment 评语
     * @param evaluatedBy 评价人ID
     * @return 评价DTO
     */
    ProjectEvaluationDTO createOrUpdateEvaluation(Long projectId, Integer score, String comment, Long evaluatedBy);
    
    /**
     * 获取课题评价
     * @param projectId 课题ID
     * @return 评价DTO，如果不存在则返回null
     */
    ProjectEvaluationDTO getEvaluationByProjectId(Long projectId);
    
    /**
     * 删除课题评价
     * @param projectId 课题ID
     * @return 是否删除成功
     */
    boolean deleteEvaluation(Long projectId);
} 