package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.common.exception.BusinessException;
import com.example.auth.mapper.*;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.entity.*;
import com.example.auth.service.AdminSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员社交服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminSocialServiceImpl implements AdminSocialService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostImageMapper postImageMapper;
    private final UserMapper userMapper;
    private final SocialTakedownMapper socialTakedownMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostRunningRecordMapper postRunningRecordMapper;
    private final RunningRecordMapper runningRecordMapper;
    private final RoleMapper roleMapper;

    @Override
    public Page<PostResponse> getAllPosts(Long adminId, Integer page, Integer size) {
        // 检查是否是管理员
        checkAdminRole(adminId);

        // 分页查询所有帖子，包括已删除的
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Post::getCreateTime);
        
        Page<Post> resultPage = postMapper.selectPage(postPage, queryWrapper);
        
        // 转换为PostResponse
        return convertToPostResponsePage(resultPage, adminId);
    }
    
    @Override
    public Page<CommentResponse> getAllComments(Long adminId, Integer page, Integer size) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 分页查询所有评论，包括已删除的
        Page<PostComment> commentPage = new Page<>(page, size);
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PostComment::getCreateTime);
        
        Page<PostComment> resultPage = postCommentMapper.selectPage(commentPage, queryWrapper);
        
        // 转换为CommentResponse
        return convertToCommentResponsePage(resultPage);
    }
    
    @Override
    @Transactional
    public Boolean takedownPost(Long adminId, Long postId, String reason) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 如果帖子已经被删除，则返回成功
        if (post.getIsDeleted() == 1) {
            return true;
        }
        
        // 更新帖子为已删除状态
        post.setIsDeleted(1);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        
        // 记录下架操作
        SocialTakedown takedown = new SocialTakedown();
        takedown.setContentType(1); // 1-帖子
        takedown.setContentId(postId);
        takedown.setAdminId(adminId);
        takedown.setReason(reason);
        takedown.setTakedownTime(LocalDateTime.now());
        takedown.setCreateTime(LocalDateTime.now());
        takedown.setUpdateTime(LocalDateTime.now());
        
        socialTakedownMapper.insert(takedown);
        
        return true;
    }
    
    @Override
    @Transactional
    public Boolean takedownComment(Long adminId, Long commentId, String reason) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 查询评论
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 如果评论已经被删除，则返回成功
        if (comment.getIsDeleted() == 1) {
            return true;
        }
        
        // 更新评论为已删除状态
        comment.setIsDeleted(1);
        comment.setUpdateTime(LocalDateTime.now());
        postCommentMapper.updateById(comment);
        
        // 获取评论所属的帖子，并更新评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() - 1);
            postMapper.updateById(post);
        }
        
        // 记录下架操作
        SocialTakedown takedown = new SocialTakedown();
        takedown.setContentType(2); // 2-评论
        takedown.setContentId(commentId);
        takedown.setAdminId(adminId);
        takedown.setReason(reason);
        takedown.setTakedownTime(LocalDateTime.now());
        takedown.setCreateTime(LocalDateTime.now());
        takedown.setUpdateTime(LocalDateTime.now());
        
        socialTakedownMapper.insert(takedown);
        
        return true;
    }
    
    @Override
    @Transactional
    public Boolean restorePost(Long adminId, Long postId) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 如果帖子未被删除，则返回成功
        if (post.getIsDeleted() == 0) {
            return true;
        }
        
        // 更新帖子为未删除状态
        post.setIsDeleted(0);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        
        // 更新下架记录
        LambdaQueryWrapper<SocialTakedown> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SocialTakedown::getContentType, 1)
                   .eq(SocialTakedown::getContentId, postId)
                   .isNull(SocialTakedown::getRestoreTime);
        
        SocialTakedown takedown = socialTakedownMapper.selectOne(queryWrapper);
        if (takedown != null) {
            takedown.setRestoreTime(LocalDateTime.now());
            takedown.setRestoreAdminId(adminId);
            takedown.setUpdateTime(LocalDateTime.now());
            socialTakedownMapper.updateById(takedown);
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public Boolean restoreComment(Long adminId, Long commentId) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 查询评论
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 如果评论未被删除，则返回成功
        if (comment.getIsDeleted() == 0) {
            return true;
        }
        
        // 更新评论为未删除状态
        comment.setIsDeleted(0);
        comment.setUpdateTime(LocalDateTime.now());
        postCommentMapper.updateById(comment);
        
        // 获取评论所属的帖子，并更新评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }
        
        // 更新下架记录
        LambdaQueryWrapper<SocialTakedown> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SocialTakedown::getContentType, 2)
                   .eq(SocialTakedown::getContentId, commentId)
                   .isNull(SocialTakedown::getRestoreTime);
        
        SocialTakedown takedown = socialTakedownMapper.selectOne(queryWrapper);
        if (takedown != null) {
            takedown.setRestoreTime(LocalDateTime.now());
            takedown.setRestoreAdminId(adminId);
            takedown.setUpdateTime(LocalDateTime.now());
            socialTakedownMapper.updateById(takedown);
        }
        
        return true;
    }
    
    @Override
    public Page<PostResponse> searchPosts(Long adminId, String keyword, Integer page, Integer size) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 分页搜索帖子
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Post::getContent, keyword)
                   .orderByDesc(Post::getCreateTime);
        
        Page<Post> resultPage = postMapper.selectPage(postPage, queryWrapper);
        
        // 转换为PostResponse
        return convertToPostResponsePage(resultPage, adminId);
    }
    
    @Override
    public Page<CommentResponse> searchComments(Long adminId, String keyword, Integer page, Integer size) {
        // 检查是否是管理员
        checkAdminRole(adminId);
        
        // 分页搜索评论
        Page<PostComment> commentPage = new Page<>(page, size);
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(PostComment::getContent, keyword)
                   .orderByDesc(PostComment::getCreateTime);
        
        Page<PostComment> resultPage = postCommentMapper.selectPage(commentPage, queryWrapper);
        
        // 转换为CommentResponse
        return convertToCommentResponsePage(resultPage);
    }
    
    /**
     * 检查用户是否是管理员
     *
     * @param userId 用户ID
     */
    private void checkAdminRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 查询用户的角色信息
        List<Role> roles = roleMapper.findRolesByUserId(userId);
        boolean isAdmin = false;
        for (Role role : roles) {
            if ("ADMIN".equals(role.getName())) {
                isAdmin = true;
                break;
            }
        }
        
        if (!isAdmin) {
            throw new BusinessException("无权限执行此操作");
        }
    }
    
    /**
     * 将帖子分页结果转换为PostResponse分页结果
     *
     * @param postPage 帖子分页结果
     * @param adminId 管理员ID
     * @return PostResponse分页结果
     */
    private Page<PostResponse> convertToPostResponsePage(Page<Post> postPage, Long adminId) {
        Page<PostResponse> responsePage = new Page<>();
        responsePage.setCurrent(postPage.getCurrent());
        responsePage.setSize(postPage.getSize());
        responsePage.setTotal(postPage.getTotal());
        responsePage.setPages(postPage.getPages());
        
        List<Post> posts = postPage.getRecords();
        if (posts == null || posts.isEmpty()) {
            responsePage.setRecords(new ArrayList<>());
            return responsePage;
        }
        
        // 收集所有用户ID
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .collect(Collectors.toSet());
                
        // 查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 收集所有帖子ID
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());
        
        // 查询图片信息
        LambdaQueryWrapper<PostImage> imageQueryWrapper = new LambdaQueryWrapper<>();
        imageQueryWrapper.in(PostImage::getPostId, postIds)
                        .orderByAsc(PostImage::getSortOrder);
        List<PostImage> postImages = postImageMapper.selectList(imageQueryWrapper);
        
        // 按帖子ID分组图片
        Map<Long, List<PostImage>> postImageMap = postImages.stream()
                .collect(Collectors.groupingBy(PostImage::getPostId));
        
        // 查询跑步记录信息
        LambdaQueryWrapper<PostRunningRecord> runningRecordQueryWrapper = new LambdaQueryWrapper<>();
        runningRecordQueryWrapper.in(PostRunningRecord::getPostId, postIds);
        List<PostRunningRecord> postRunningRecords = postRunningRecordMapper.selectList(runningRecordQueryWrapper);
        
        // 按帖子ID映射跑步记录
        Map<Long, PostRunningRecord> postRunningRecordMap = postRunningRecords.stream()
                .collect(Collectors.toMap(PostRunningRecord::getPostId, record -> record, (v1, v2) -> v1));
        
        // 查询跑步记录详情
        Set<Long> runningRecordIds = postRunningRecords.stream()
                .map(PostRunningRecord::getRunningRecordId)
                .collect(Collectors.toSet());
        
        Map<Long, RunningRecord> runningRecordMap = new HashMap<>();
        if (!runningRecordIds.isEmpty()) {
            List<RunningRecord> runningRecords = runningRecordMapper.selectBatchIds(runningRecordIds);
            runningRecordMap = runningRecords.stream()
                    .collect(Collectors.toMap(RunningRecord::getId, record -> record, (v1, v2) -> v1));
        }
        
        // 转换为PostResponse列表
        List<PostResponse> postResponses = new ArrayList<>();
        for (Post post : posts) {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            
            // 设置用户信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                response.setUserId(user.getId());
                response.setUsername(user.getUsername());
                response.setAvatar(user.getAvatar());
            }
            
            response.setContent(post.getContent());
            response.setLocation(post.getLocation());
            response.setLikeCount(post.getLikeCount());
            response.setCommentCount(post.getCommentCount());
            response.setForwardCount(post.getForwardCount());
            response.setVisibility(post.getVisibility());
            response.setIsForward(post.getIsForward() == 1);
            response.setOriginalPostId(post.getOriginalPostId());
            response.setCreateTime(post.getCreateTime());
            
            // 设置图片列表
            List<PostImage> images = postImageMap.getOrDefault(post.getId(), new ArrayList<>());
            List<String> imageUrls = images.stream()
                    .map(PostImage::getImageUrl)
                    .collect(Collectors.toList());
            response.setImageUrls(imageUrls);
            
            // 设置跑步记录
            PostRunningRecord postRunningRecord = postRunningRecordMap.get(post.getId());
            if (postRunningRecord != null) {
                RunningRecord runningRecord = runningRecordMap.get(postRunningRecord.getRunningRecordId());
                if (runningRecord != null) {
                    RunningRecordResponse runningRecordResponse = new RunningRecordResponse();
                    runningRecordResponse.setId(runningRecord.getId());
                    runningRecordResponse.setDistance(runningRecord.getDistance());
                    runningRecordResponse.setDuration(runningRecord.getDuration());
                    runningRecordResponse.setPace(runningRecord.getPace());
                    runningRecordResponse.setRecordDate(runningRecord.getRecordDate());
                    response.setRunningRecord(runningRecordResponse);
                }
            }
            
            // 管理员可以看到是否帖子已删除状态
            if (post.getIsDeleted() == 1) {
                response.setContent("[该内容已被管理员下架] " + response.getContent());
            }
            
            postResponses.add(response);
        }
        
        responsePage.setRecords(postResponses);
        return responsePage;
    }
    
    /**
     * 将评论分页结果转换为CommentResponse分页结果
     *
     * @param commentPage 评论分页结果
     * @return CommentResponse分页结果
     */
    private Page<CommentResponse> convertToCommentResponsePage(Page<PostComment> commentPage) {
        Page<CommentResponse> responsePage = new Page<>();
        responsePage.setCurrent(commentPage.getCurrent());
        responsePage.setSize(commentPage.getSize());
        responsePage.setTotal(commentPage.getTotal());
        responsePage.setPages(commentPage.getPages());
        
        List<PostComment> comments = commentPage.getRecords();
        if (comments == null || comments.isEmpty()) {
            responsePage.setRecords(new ArrayList<>());
            return responsePage;
        }
        
        // 收集所有用户ID和回复用户ID
        Set<Long> userIds = new HashSet<>();
        for (PostComment comment : comments) {
            userIds.add(comment.getUserId());
            if (comment.getReplyUserId() != null && comment.getReplyUserId() > 0) {
                userIds.add(comment.getReplyUserId());
            }
        }
        
        // 查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user, (v1, v2) -> v1));
        
        // 收集所有帖子ID
        Set<Long> postIds = comments.stream()
                .map(PostComment::getPostId)
                .collect(Collectors.toSet());
        
        // 转换为CommentResponse列表
        List<CommentResponse> commentResponses = new ArrayList<>();
        for (PostComment comment : comments) {
            User commentUser = userMap.get(comment.getUserId());
            if (commentUser == null) continue;
            
            CommentResponse commentResponse = new CommentResponse();
            commentResponse.setId(comment.getId());
            commentResponse.setPostId(comment.getPostId());
            commentResponse.setUserId(comment.getUserId());
            commentResponse.setUsername(commentUser.getUsername());
            commentResponse.setAvatar(commentUser.getAvatar());
            commentResponse.setContent(comment.getContent());
            commentResponse.setParentId(comment.getParentId());
            commentResponse.setReplyUserId(comment.getReplyUserId());
            
            // 设置被回复的用户名
            if (comment.getReplyUserId() != null && comment.getReplyUserId() > 0) {
                User replyUser = userMap.get(comment.getReplyUserId());
                if (replyUser != null) {
                    commentResponse.setReplyUsername(replyUser.getUsername());
                }
            }
            
            commentResponse.setLikeCount(comment.getLikeCount());
            commentResponse.setCreateTime(comment.getCreateTime());
            
            // 管理员可以看到是否评论已删除状态
            if (comment.getIsDeleted() == 1) {
                commentResponse.setContent("[该内容已被管理员下架] " + commentResponse.getContent());
            }
            
            commentResponses.add(commentResponse);
        }
        
        responsePage.setRecords(commentResponses);
        return responsePage;
    }
} 