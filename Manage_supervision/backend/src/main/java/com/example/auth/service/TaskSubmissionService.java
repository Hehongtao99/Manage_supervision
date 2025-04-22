package com.example.auth.service;

import com.example.auth.dto.TaskSubmissionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 任务提交服务接口
 */
public interface TaskSubmissionService {
    
    /**
     * 提交任务文件
     * @param taskId 任务ID
     * @param file 上传的文件
     * @param submitterId 提交者ID
     * @param comment 提交评论
     * @return 提交记录DTO
     */
    TaskSubmissionDTO submitTaskFile(Long taskId, MultipartFile file, Long submitterId, String comment);
    
    /**
     * 获取任务提交记录
     * @param taskId 任务ID
     * @param userId 当前用户ID，用于权限过滤
     * @return 提交记录列表，根据用户权限过滤
     */
    List<TaskSubmissionDTO> getSubmissionsByTaskId(Long taskId, Long userId);
    
    /**
     * 获取用户提交的所有任务文件
     * @param submitterId 提交者ID
     * @return 提交记录列表
     */
    List<TaskSubmissionDTO> getSubmissionsBySubmitterId(Long submitterId);
    
    /**
     * 获取提交详情
     * @param submissionId 提交ID
     * @return 提交记录DTO
     */
    TaskSubmissionDTO getSubmissionById(Long submissionId);
    
    /**
     * 删除提交记录
     * @param submissionId 提交ID
     * @return 是否删除成功
     */
    boolean deleteSubmission(Long submissionId);
    
    /**
     * 更新提交评论
     * @param submissionId 提交ID
     * @param comment 新评论
     * @return 更新后的提交记录DTO
     */
    TaskSubmissionDTO updateSubmissionComment(Long submissionId, String comment);
    
    /**
     * 检查任务是否已提交文件
     * @param taskId 任务ID
     * @return 是否已提交
     */
    boolean isTaskSubmitted(Long taskId);
    
    /**
     * 将文件作为资源加载
     * @param filePath 文件路径
     * @return 文件资源
     */
    org.springframework.core.io.Resource loadFileAsResource(String filePath);
} 