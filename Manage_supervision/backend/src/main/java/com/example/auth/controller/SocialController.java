package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.request.CommentCreateRequest;
import com.example.auth.model.dto.request.PostCreateRequest;
import com.example.auth.model.dto.request.PostUpdateRequest;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.SocialService;
import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 社交控制器
 */
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;
    private final JwtUtil jwtUtil;

    /**
     * 发布朋友圈帖子
     *
     * @param token 用户令牌
     * @param request 帖子创建请求
     * @return 帖子响应
     */
    @PostMapping("/post")
    public ResponseVO<PostResponse> createPost(
            @RequestHeader("Authorization") String token,
            @RequestBody PostCreateRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PostResponse post = socialService.createPost(userId, request);
        return ResponseVO.success("发布成功", post);
    }

    /**
     * 上传图片
     *
     * @param token 用户令牌
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping("/upload/image")
    public ResponseVO<String> uploadImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String imageUrl = socialService.uploadImage(userId, file);
        return ResponseVO.success("上传成功", imageUrl);
    }

    /**
     * 获取朋友圈帖子列表
     *
     * @param token 用户令牌
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @return 帖子分页列表
     */
    @GetMapping("/posts")
    public ResponseVO<Page<PostResponse>> getPostList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<PostResponse> posts = socialService.getPostList(userId, page, size);
        return ResponseVO.success("获取成功", posts);
    }

    /**
     * 获取用户自己的帖子列表
     *
     * @param token 用户令牌
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @return 帖子分页列表
     */
    @GetMapping("/user/posts")
    public ResponseVO<Page<PostResponse>> getUserPostList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<PostResponse> posts = socialService.getUserPostList(userId, page, size);
        return ResponseVO.success("获取成功", posts);
    }

    /**
     * 获取帖子详情
     *
     * @param token 用户令牌
     * @param id 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/post/{id}")
    public ResponseVO<PostResponse> getPostDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PostResponse post = socialService.getPostDetail(userId, id);
        return ResponseVO.success("获取成功", post);
    }

    /**
     * 点赞或取消点赞
     *
     * @param token 用户令牌
     * @param id 帖子ID
     * @return 点赞结果
     */
    @PostMapping("/post/{id}/like")
    public ResponseVO<Boolean> toggleLike(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = socialService.toggleLike(userId, id);
        return ResponseVO.success(result ? "点赞成功" : "取消点赞成功", result);
    }

    /**
     * 发表评论
     *
     * @param token 用户令牌
     * @param request 评论创建请求
     * @return 评论响应
     */
    @PostMapping("/comment")
    public ResponseVO<CommentResponse> createComment(
            @RequestHeader("Authorization") String token,
            @RequestBody CommentCreateRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        CommentResponse comment = socialService.createComment(userId, request);
        return ResponseVO.success("评论成功", comment);
    }

    /**
     * 获取帖子的评论列表
     *
     * @param token 用户令牌
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/post/{postId}/comments")
    public ResponseVO<List<CommentResponse>> getCommentList(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<CommentResponse> comments = socialService.getCommentList(userId, postId);
        return ResponseVO.success("获取成功", comments);
    }

    /**
     * 删除帖子
     *
     * @param token 用户令牌
     * @param id 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/post/{id}")
    public ResponseVO<Boolean> deletePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = socialService.deletePost(userId, id);
        return ResponseVO.success("删除成功", result);
    }

    /**
     * 删除评论
     *
     * @param token 用户令牌
     * @param id 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/comment/{id}")
    public ResponseVO<Boolean> deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Boolean result = socialService.deleteComment(userId, id);
        return ResponseVO.success("删除成功", result);
    }

    /**
     * 编辑朋友圈帖子
     *
     * @param token 用户令牌
     * @param request 帖子更新请求
     * @return 帖子响应
     */
    @PutMapping("/post")
    public ResponseVO<PostResponse> updatePost(
            @RequestHeader("Authorization") String token,
            @RequestBody PostUpdateRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PostResponse post = socialService.updatePost(userId, request);
        return ResponseVO.success("更新成功", post);
    }
} 