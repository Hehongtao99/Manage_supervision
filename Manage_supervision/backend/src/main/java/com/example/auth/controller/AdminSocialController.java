package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.AdminSocialService;
import com.example.auth.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

    /**
     * 获取所有朋友圈帖子列表（分页）
     *
     * @param token 管理员令牌
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页列表
     */
    @GetMapping("/posts")
    public ResponseVO<Page<PostResponse>> getAllPosts(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<PostResponse> posts = adminSocialService.getAllPosts(adminId, page, size);
        return ResponseVO.success("获取成功", posts);
    }

    /**
     * 获取所有评论列表（分页）
     *
     * @param token 管理员令牌
     * @param page 页码
     * @param size 每页大小
     * @return 评论分页列表
     */
    @GetMapping("/comments")
    public ResponseVO<Page<CommentResponse>> getAllComments(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<CommentResponse> comments = adminSocialService.getAllComments(adminId, page, size);
        return ResponseVO.success("获取成功", comments);
    }

    /**
     * 下架朋友圈帖子
     *
     * @param token 管理员令牌
     * @param id 帖子ID
     * @return 操作结果
     */
    @PostMapping("/post/{id}/takedown")
    public ResponseVO<Boolean> takedownPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = adminSocialService.takedownPost(adminId, id, reason);
        return ResponseVO.success("下架成功", result);
    }

    /**
     * 下架评论
     *
     * @param token 管理员令牌
     * @param id 评论ID
     * @return 操作结果
     */
    @PostMapping("/comment/{id}/takedown")
    public ResponseVO<Boolean> takedownComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = adminSocialService.takedownComment(adminId, id, reason);
        return ResponseVO.success("下架成功", result);
    }

    /**
     * 恢复朋友圈帖子
     *
     * @param token 管理员令牌
     * @param id 帖子ID
     * @return 操作结果
     */
    @PostMapping("/post/{id}/restore")
    public ResponseVO<Boolean> restorePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = adminSocialService.restorePost(adminId, id);
        return ResponseVO.success("恢复成功", result);
    }

    /**
     * 恢复评论
     *
     * @param token 管理员令牌
     * @param id 评论ID
     * @return 操作结果
     */
    @PostMapping("/comment/{id}/restore")
    public ResponseVO<Boolean> restoreComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = adminSocialService.restoreComment(adminId, id);
        return ResponseVO.success("恢复成功", result);
    }

    /**
     * 搜索朋友圈帖子
     *
     * @param token 管理员令牌
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页列表
     */
    @GetMapping("/posts/search")
    public ResponseVO<Page<PostResponse>> searchPosts(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<PostResponse> posts = adminSocialService.searchPosts(adminId, keyword, page, size);
        return ResponseVO.success("搜索成功", posts);
    }

    /**
     * 搜索评论
     *
     * @param token 管理员令牌
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 评论分页列表
     */
    @GetMapping("/comments/search")
    public ResponseVO<Page<CommentResponse>> searchComments(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<CommentResponse> comments = adminSocialService.searchComments(adminId, keyword, page, size);
        return ResponseVO.success("搜索成功", comments);
    }
} 