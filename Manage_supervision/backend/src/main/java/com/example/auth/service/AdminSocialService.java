package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;

import java.util.List;

/**
 * 管理员社交服务接口
 */
public interface AdminSocialService {

    /**
     * 获取所有朋友圈帖子分页列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词（可选）
     * @return 帖子分页列表
     */
    Page<PostResponse> getAllPosts(Integer page, Integer size, String keyword);

    /**
     * 获取指定用户的朋友圈帖子
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 帖子分页列表
     */
    Page<PostResponse> getUserPosts(Long userId, Integer page, Integer size);

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    PostResponse getPostDetail(Long postId);

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @return 删除结果
     */
    Boolean deletePost(Long postId);

    /**
     * 获取帖子的评论列表
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<CommentResponse> getPostComments(Long postId);

    /**
     * 获取所有评论分页列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词（可选）
     * @return 评论分页列表
     */
    Page<CommentResponse> getAllComments(Integer page, Integer size, String keyword);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    Boolean deleteComment(Long commentId);
} 