package com.example.auth.service;

import com.example.auth.dto.TaskEvaluationDTO;

/**
 * 任务评价服务接口
 */
public interface TaskEvaluationService {
    
    /**
     * 创建或更新任务评价
     * @param taskId 任务ID
     * @param score 评分
     * @param comment 评语
     * @param evaluatedBy 评价人ID
     * @return 评价DTO
     */
    TaskEvaluationDTO createOrUpdateEvaluation(Long taskId, Integer score, String comment, Long evaluatedBy);
    
    /**
     * 获取任务评价
     * @param taskId 任务ID
     * @return 评价DTO，如果不存在则返回null
     */
    TaskEvaluationDTO getEvaluationByTaskId(Long taskId);
    
    /**
     * 删除任务评价
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteEvaluation(Long taskId);
} 