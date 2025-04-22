package com.example.auth.service.impl;

import com.example.auth.dto.ProjectDTO;
import com.example.auth.dto.ProjectEvaluationDTO;
import com.example.auth.entity.ProjectEvaluation;
import com.example.auth.entity.User;
import com.example.auth.repository.ProjectEvaluationRepository;
import com.example.auth.service.ProjectEvaluationService;
import com.example.auth.service.ProjectService;
import com.example.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 课题评价服务实现类
 */
@Service
public class ProjectEvaluationServiceImpl implements ProjectEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectEvaluationServiceImpl.class);

    @Autowired
    private ProjectEvaluationRepository projectEvaluationRepository;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public ProjectEvaluationDTO createOrUpdateEvaluation(Long projectId, Integer score, String comment, Long evaluatedBy) {
        logger.info("创建或更新课题评价，课题ID: {}, 评分: {}, 评价人ID: {}", projectId, score, evaluatedBy);
        
        // 查找已有评价
        Optional<ProjectEvaluation> existingEvaluation = projectEvaluationRepository.findByProjectId(projectId);
        
        ProjectEvaluation evaluation;
        if (existingEvaluation.isPresent()) {
            // 更新已有评价
            logger.info("更新已有评价，评价ID: {}", existingEvaluation.get().getId());
            evaluation = existingEvaluation.get();
            evaluation.setScore(score);
            evaluation.setComment(comment);
        } else {
            // 创建新评价
            logger.info("创建新评价");
            evaluation = new ProjectEvaluation();
            evaluation.setProjectId(projectId);
            evaluation.setScore(score);
            evaluation.setComment(comment);
            evaluation.setEvaluatedBy(evaluatedBy);
        }
        
        // 保存评价
        evaluation = projectEvaluationRepository.save(evaluation);
        logger.info("评价保存成功，评价ID: {}", evaluation.getId());
        
        // 获取课题和评价人信息用于DTO创建
        ProjectDTO project = projectService.getProjectById(projectId);
        User evaluator = userService.findById(evaluatedBy);
        String evaluatorName = evaluator != null ? evaluator.getUsername() : "未知用户";
        
        return ProjectEvaluationDTO.fromEntity(evaluation, project.getTitle(), evaluatorName);
    }

    @Override
    public ProjectEvaluationDTO getEvaluationByProjectId(Long projectId) {
        logger.info("获取课题评价，课题ID: {}", projectId);
        
        Optional<ProjectEvaluation> evaluation = projectEvaluationRepository.findByProjectId(projectId);
        
        if (evaluation.isEmpty()) {
            logger.info("课题评价不存在，课题ID: {}", projectId);
            return null;
        }
        
        // 获取课题和评价人信息
        ProjectDTO project = projectService.getProjectById(projectId);
        User evaluator = userService.findById(evaluation.get().getEvaluatedBy());
        String evaluatorName = evaluator != null ? evaluator.getUsername() : "未知用户";
        
        logger.info("获取课题评价成功，评价ID: {}", evaluation.get().getId());
        return ProjectEvaluationDTO.fromEntity(evaluation.get(), project.getTitle(), evaluatorName);
    }

    @Override
    @Transactional
    public boolean deleteEvaluation(Long projectId) {
        logger.info("删除课题评价，课题ID: {}", projectId);
        
        Optional<ProjectEvaluation> evaluation = projectEvaluationRepository.findByProjectId(projectId);
        
        if (evaluation.isEmpty()) {
            logger.info("课题评价不存在，无法删除，课题ID: {}", projectId);
            return false;
        }
        
        projectEvaluationRepository.delete(evaluation.get());
        logger.info("课题评价删除成功，评价ID: {}", evaluation.get().getId());
        return true;
    }
} 