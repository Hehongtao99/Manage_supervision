package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.request.CommentCreateRequest;
import com.example.auth.model.dto.request.PostCreateRequest;
import com.example.auth.model.dto.request.PostUpdateRequest;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 社交服务接口
 */
public interface SocialService {
    
    /**
     * 发布朋友圈帖子
     *
     * @param userId 用户ID
     * @param request 帖子创建请求
     * @return 帖子响应
     */
    PostResponse createPost(Long userId, PostCreateRequest request);
    
    /**
     * 上传图片
     *
     * @param userId 用户ID
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(Long userId, MultipartFile file);
    
    /**
     * 获取朋友圈帖子分页列表
     *
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页列表
     */
    Page<PostResponse> getPostList(Long userId, Integer page, Integer size);
    
    /**
     * 获取用户自己的帖子分页列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页列表
     */
    Page<PostResponse> getUserPostList(Long userId, Integer page, Integer size);
    
    /**
     * 获取帖子详情
     *
     * @param userId 当前用户ID
     * @param postId 帖子ID
     * @return 帖子详情
     */
    PostResponse getPostDetail(Long userId, Long postId);
    
    /**
     * 点赞或取消点赞
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否点赞
     */
    Boolean toggleLike(Long userId, Long postId);
    
    /**
     * 发表评论
     *
     * @param userId 用户ID
     * @param request 评论创建请求
     * @return 评论响应
     */
    CommentResponse createComment(Long userId, CommentCreateRequest request);
    
    /**
     * 获取帖子的评论列表
     *
     * @param userId 当前用户ID
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<CommentResponse> getCommentList(Long userId, Long postId);
    
    /**
     * 删除帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否删除成功
     */
    Boolean deletePost(Long userId, Long postId);
    
    /**
     * 删除评论
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return 是否删除成功
     */
    Boolean deleteComment(Long userId, Long commentId);
    
    /**
     * 编辑朋友圈帖子
     *
     * @param userId 用户ID
     * @param request 帖子更新请求
     * @return 帖子响应
     */
    PostResponse updatePost(Long userId, PostUpdateRequest request);
} 