package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;

/**
 * 管理员社交服务接口
 */
public interface AdminSocialService {
    
    /**
     * 获取所有朋友圈帖子分页列表
     *
     * @param adminId 管理员ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页列表
     */
    Page<PostResponse> getAllPosts(Long adminId, Integer page, Integer size);
    
    /**
     * 获取所有评论分页列表
     *
     * @param adminId 管理员ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论分页列表
     */
    Page<CommentResponse> getAllComments(Long adminId, Integer page, Integer size);
    
    /**
     * 下架朋友圈帖子
     *
     * @param adminId 管理员ID
     * @param postId 帖子ID
     * @param reason 下架原因
     * @return 是否下架成功
     */
    Boolean takedownPost(Long adminId, Long postId, String reason);
    
    /**
     * 下架评论
     *
     * @param adminId 管理员ID
     * @param commentId 评论ID
     * @param reason 下架原因
     * @return 是否下架成功
     */
    Boolean takedownComment(Long adminId, Long commentId, String reason);
    
    /**
     * 恢复朋友圈帖子
     *
     * @param adminId 管理员ID
     * @param postId 帖子ID
     * @return 是否恢复成功
     */
    Boolean restorePost(Long adminId, Long postId);
    
    /**
     * 恢复评论
     *
     * @param adminId 管理员ID
     * @param commentId 评论ID
     * @return 是否恢复成功
     */
    Boolean restoreComment(Long adminId, Long commentId);
    
    /**
     * 搜索朋友圈帖子
     *
     * @param adminId 管理员ID
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页列表
     */
    Page<PostResponse> searchPosts(Long adminId, String keyword, Integer page, Integer size);
    
    /**
     * 搜索评论
     *
     * @param adminId 管理员ID
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 评论分页列表
     */
    Page<CommentResponse> searchComments(Long adminId, String keyword, Integer page, Integer size);
} 