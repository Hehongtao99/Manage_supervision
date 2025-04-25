package com.example.auth.service;

import com.example.auth.dto.ParentTeacherMessageDTO;
import com.example.auth.dto.ParentTeacherMessageRequest;
import com.example.auth.entity.User;
import org.springframework.data.domain.Page;

/**
 * 家长-教师留言服务接口
 * 提供家长向教师发送留言的功能
 */
public interface ParentTeacherMessageService {
    
    /**
     * 家长发送留言给教师
     * @param parent 家长用户
     * @param request 留言请求
     * @return 留言DTO
     */
    ParentTeacherMessageDTO sendMessage(User parent, ParentTeacherMessageRequest request);
    
    /**
     * 获取家长发送的所有留言（分页）
     * @param parentId 家长ID
     * @param page 页码（从0开始）
     * @param size 每页记录数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessageDTO> getMessagesByParent(Long parentId, int page, int size);
    
    /**
     * 将家长的所有留言回复标记为已读
     * @param parentId 家长ID
     * @return 更新的记录数
     */
    int markAllRepliesAsReadByParent(Long parentId);
} 