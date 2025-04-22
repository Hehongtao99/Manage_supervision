package com.example.auth.service.impl;

import com.example.auth.dto.TaskDTO;
import com.example.auth.dto.TaskEvaluationDTO;
import com.example.auth.entity.TaskEvaluation;
import com.example.auth.entity.User;
import com.example.auth.repository.TaskEvaluationRepository;
import com.example.auth.service.TaskEvaluationService;
import com.example.auth.service.TaskService;
import com.example.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 任务评价服务实现类
 */
@Service
public class TaskEvaluationServiceImpl implements TaskEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(TaskEvaluationServiceImpl.class);

    @Autowired
    private TaskEvaluationRepository taskEvaluationRepository;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public TaskEvaluationDTO createOrUpdateEvaluation(Long taskId, Integer score, String comment, Long evaluatedBy) {
        logger.info("创建或更新任务评价，任务ID: {}, 评分: {}, 评价人ID: {}", taskId, score, evaluatedBy);
        
        // 查找已有评价
        Optional<TaskEvaluation> existingEvaluation = taskEvaluationRepository.findByTaskId(taskId);
        
        TaskEvaluation evaluation;
        if (existingEvaluation.isPresent()) {
            // 更新已有评价
            logger.info("更新已有评价，评价ID: {}", existingEvaluation.get().getId());
            evaluation = existingEvaluation.get();
            evaluation.setScore(score);
            evaluation.setComment(comment);
        } else {
            // 创建新评价
            logger.info("创建新评价");
            evaluation = new TaskEvaluation();
            evaluation.setTaskId(taskId);
            evaluation.setScore(score);
            evaluation.setComment(comment);
            evaluation.setEvaluatedBy(evaluatedBy);
        }
        
        // 保存评价
        evaluation = taskEvaluationRepository.save(evaluation);
        logger.info("评价保存成功，评价ID: {}", evaluation.getId());
        
        // 获取任务和评价人信息用于DTO创建
        TaskDTO task = taskService.getTaskById(taskId);
        User evaluator = userService.findById(evaluatedBy);
        String evaluatorName = evaluator != null ? evaluator.getUsername() : "未知用户";
        
        return TaskEvaluationDTO.fromEntity(evaluation, task.getTitle(), evaluatorName);
    }

    @Override
    public TaskEvaluationDTO getEvaluationByTaskId(Long taskId) {
        logger.info("获取任务评价，任务ID: {}", taskId);
        
        Optional<TaskEvaluation> evaluation = taskEvaluationRepository.findByTaskId(taskId);
        
        if (evaluation.isEmpty()) {
            logger.info("任务评价不存在，任务ID: {}", taskId);
            return null;
        }
        
        // 获取任务和评价人信息
        TaskDTO task = taskService.getTaskById(taskId);
        User evaluator = userService.findById(evaluation.get().getEvaluatedBy());
        String evaluatorName = evaluator != null ? evaluator.getUsername() : "未知用户";
        
        logger.info("获取任务评价成功，评价ID: {}", evaluation.get().getId());
        return TaskEvaluationDTO.fromEntity(evaluation.get(), task.getTitle(), evaluatorName);
    }

    @Override
    @Transactional
    public boolean deleteEvaluation(Long taskId) {
        logger.info("删除任务评价，任务ID: {}", taskId);
        
        Optional<TaskEvaluation> evaluation = taskEvaluationRepository.findByTaskId(taskId);
        
        if (evaluation.isEmpty()) {
            logger.info("任务评价不存在，无法删除，任务ID: {}", taskId);
            return false;
        }
        
        taskEvaluationRepository.delete(evaluation.get());
        logger.info("任务评价删除成功，评价ID: {}", evaluation.get().getId());
        return true;
    }
} 