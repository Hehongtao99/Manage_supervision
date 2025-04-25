package com.example.auth.service;

import com.example.auth.dto.ParentTeacherMessageDTO;
import org.springframework.data.domain.Page;

/**
 * 教师留言服务接口
 * 提供教师查看和回复留言的功能
 */
public interface TeacherMessageService {
    
    /**
     * 获取教师收到的所有留言（分页）
     * @param teacherId 教师ID
     * @param page 页码（从0开始）
     * @param size 每页记录数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessageDTO> getMessagesByTeacher(Long teacherId, int page, int size);
    
    /**
     * 获取教师收到的关于特定学生的留言（分页）
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @param page 页码（从0开始）
     * @param size 每页记录数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessageDTO> getMessagesByTeacherAndStudent(Long teacherId, Long studentId, int page, int size);
    
    /**
     * 教师回复留言
     * @param messageId 留言ID
     * @param teacherId 教师ID
     * @param replyContent 回复内容
     * @return 更新后的留言DTO
     */
    ParentTeacherMessageDTO replyMessage(Long messageId, Long teacherId, String replyContent);
    
    /**
     * 将教师的所有留言标记为已读
     * @param teacherId 教师ID
     * @return 更新的记录数
     */
    int markAllAsRead(Long teacherId);
} 