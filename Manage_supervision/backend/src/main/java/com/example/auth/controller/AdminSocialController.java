package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.AdminSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员社交管理控制器
 */
@RestController
@RequestMapping("/api/admin/social")
@RequiredArgsConstructor
public class AdminSocialController {

    private final AdminSocialService adminSocialService;

    /**
     * 获取所有朋友圈帖子分页列表
     *
     * @param page  页码
     * @param size  每页大小
     * @param keyword 搜索关键词（可选）
     * @return 帖子分页列表
     */
    @GetMapping("/posts")
    @RequireRole("ADMIN")
    public ResponseVO<Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<PostResponse> posts = adminSocialService.getAllPosts(page, size, keyword);
        return ResponseVO.success("获取成功", posts);
    }

    /**
     * 获取指定用户的朋友圈帖子
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 帖子分页列表
     */
    @GetMapping("/posts/user/{userId}")
    @RequireRole("ADMIN")
    public ResponseVO<Page<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PostResponse> posts = adminSocialService.getUserPosts(userId, page, size);
        return ResponseVO.success("获取成功", posts);
    }

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/post/{postId}")
    @RequireRole("ADMIN")
    public ResponseVO<PostResponse> getPostDetail(@PathVariable Long postId) {
        PostResponse post = adminSocialService.getPostDetail(postId);
        return ResponseVO.success("获取成功", post);
    }

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/post/{postId}")
    @RequireRole("ADMIN")
    public ResponseVO<Boolean> deletePost(@PathVariable Long postId) {
        Boolean result = adminSocialService.deletePost(postId);
        return ResponseVO.success("删除成功", result);
    }

    /**
     * 获取帖子的评论列表
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/post/{postId}/comments")
    @RequireRole("ADMIN")
    public ResponseVO<List<CommentResponse>> getPostComments(@PathVariable Long postId) {
        List<CommentResponse> comments = adminSocialService.getPostComments(postId);
        return ResponseVO.success("获取成功", comments);
    }

    /**
     * 获取所有评论分页列表
     *
     * @param page  页码
     * @param size  每页大小
     * @param keyword 搜索关键词（可选）
     * @return 评论分页列表
     */
    @GetMapping("/comments")
    @RequireRole("ADMIN")
    public ResponseVO<Page<CommentResponse>> getAllComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<CommentResponse> comments = adminSocialService.getAllComments(page, size, keyword);
        return ResponseVO.success("获取成功", comments);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/comment/{commentId}")
    @RequireRole("ADMIN")
    public ResponseVO<Boolean> deleteComment(@PathVariable Long commentId) {
        Boolean result = adminSocialService.deleteComment(commentId);
        return ResponseVO.success("删除成功", result);
    }
} 