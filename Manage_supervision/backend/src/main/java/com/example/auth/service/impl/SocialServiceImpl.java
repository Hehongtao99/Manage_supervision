package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.common.exception.BusinessException;
import com.example.auth.mapper.*;
import com.example.auth.model.dto.request.CommentCreateRequest;
import com.example.auth.model.dto.request.PostCreateRequest;
import com.example.auth.model.dto.request.PostUpdateRequest;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.entity.*;
import com.example.auth.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 社交服务实现类
 */
@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {
    
    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final UserMapper userMapper;
    private final PostRunningRecordMapper postRunningRecordMapper;
    private final RunningRecordMapper runningRecordMapper;
    private final FriendshipMapper friendshipMapper;
    private final PostForwardMapper postForwardMapper;
    
    // 图片上传目录
    private static final String UPLOAD_DIR = "uploads/social/";
    
    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
    
    // 最大图片数量
    private static final int MAX_IMAGE_COUNT = 6;
    
    // 最大图片大小（5MB）
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    @Override
    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        // 验证内容不能为空
        if (!StringUtils.hasText(request.getContent()) && (request.getImageUrls() == null || request.getImageUrls().isEmpty())) {
            throw new BusinessException("内容和图片不能同时为空");
        }
        
        // 验证图片数量不能超过6张
        if (request.getImageUrls() != null && request.getImageUrls().size() > MAX_IMAGE_COUNT) {
            throw new BusinessException("图片数量不能超过6张");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 创建帖子
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        
        // 强制设置为仅好友可见
        post.setVisibility(1);
        
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setIsDeleted(0);
        
        // 保存帖子
        postMapper.insert(post);
        
        // 保存图片
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                PostImage postImage = new PostImage();
                postImage.setPostId(post.getId());
                postImage.setImageUrl(request.getImageUrls().get(i));
                postImage.setSortOrder(i);
                postImage.setCreateTime(LocalDateTime.now());
                postImage.setUpdateTime(LocalDateTime.now());
                postImageMapper.insert(postImage);
                imageUrls.add(postImage.getImageUrl());
            }
        }
        
        // 保存跑步记录关联
        RunningRecordResponse runningRecordResponse = null;
        if (request.getRunningRecordId() != null) {
            // 检查跑步记录是否存在且属于当前用户
            RunningRecord runningRecord = runningRecordMapper.selectById(request.getRunningRecordId());
            if (runningRecord != null && runningRecord.getUserId().equals(userId)) {
                // 创建关联记录
                PostRunningRecord postRunningRecord = new PostRunningRecord();
                postRunningRecord.setPostId(post.getId());
                postRunningRecord.setRunningRecordId(request.getRunningRecordId());
                postRunningRecord.setCreateTime(LocalDateTime.now());
                postRunningRecordMapper.insert(postRunningRecord);
                
                System.out.println("保存跑步记录关联 - 帖子ID: " + post.getId() + ", 跑步记录ID: " + request.getRunningRecordId());
                
                // 转换为响应对象
                runningRecordResponse = convertToRunningRecordResponse(runningRecord);
                System.out.println("跑步记录数据: " + runningRecordResponse);
            } else {
                System.out.println("未找到用户的跑步记录 - 用户ID: " + userId + ", 跑步记录ID: " + request.getRunningRecordId());
            }
        } else {
            System.out.println("没有提供跑步记录ID");
        }
        
        // 构建返回结果
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setContent(post.getContent());
        response.setImageUrls(imageUrls);
        response.setLocation(post.getLocation());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setLiked(false);
        response.setVisibility(post.getVisibility());
        response.setRunningRecord(runningRecordResponse);
        response.setCreateTime(post.getCreateTime());
        
        return response;
    }

    @Override
    public String uploadImage(Long userId, MultipartFile file) {
        // 验证文件不为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("图片大小不能超过5MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("只支持JPEG/JPG和PNG格式的图片");
        }
        
        // 创建上传目录
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 生成文件名
        String fileExtension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + fileExtension;
        
        // 生成日期子目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String relativePath = UPLOAD_DIR + datePath + "/";
        File dateDir = new File(relativePath);
        if (!dateDir.exists()) {
            dateDir.mkdirs();
        }
        
        // 保存文件
        try {
            Path targetPath = Paths.get(relativePath + fileName);
            Files.copy(file.getInputStream(), targetPath);
            return "/" + relativePath + fileName; // 返回相对路径
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    public Page<PostResponse> getPostList(Long userId, Integer page, Integer size) {
        // 获取当前用户的好友列表
        List<Long> friendUserIds = friendshipMapper.findFriendUserIdsByUserId(userId);
        
        // 分页查询自己和好友的帖子
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getIsDeleted, 0)
                .and(wrapper -> wrapper
                        .eq(Post::getUserId, userId)  // 自己的帖子
                        .or()
                        .in(friendUserIds != null && !friendUserIds.isEmpty(), Post::getUserId, friendUserIds)  // 好友的帖子
                )
                .orderByDesc(Post::getCreateTime);
        Page<Post> resultPage = postMapper.selectPage(postPage, queryWrapper);
        
        // 转换为PostResponse
        return convertToPostResponsePage(resultPage, userId);
    }

    @Override
    public Page<PostResponse> getUserPostList(Long userId, Integer page, Integer size) {
        // 分页查询用户自己的帖子
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, userId)
                .eq(Post::getIsDeleted, 0)
                .orderByDesc(Post::getCreateTime);
        Page<Post> resultPage = postMapper.selectPage(postPage, queryWrapper);
        
        // 转换为PostResponse
        return convertToPostResponsePage(resultPage, userId);
    }

    @Override
    public PostResponse getPostDetail(Long userId, Long postId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 转换为PostResponse
        return convertToPostResponse(post, userId);
    }

    @Override
    @Transactional
    public Boolean toggleLike(Long userId, Long postId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 查询是否已点赞
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        PostLike like = postLikeMapper.selectOne(queryWrapper);
        
        if (like == null) {
            // 未点赞，添加点赞
            PostLike newLike = new PostLike();
            newLike.setPostId(postId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            postLikeMapper.insert(newLike);
            
            // 更新帖子点赞数
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
            
            return true;
        } else {
            // 已点赞，取消点赞
            postLikeMapper.deleteById(like.getId());
            
            // 更新帖子点赞数
            post.setLikeCount(post.getLikeCount() - 1);
            postMapper.updateById(post);
            
            return false;
        }
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long userId, CommentCreateRequest request) {
        // 查询帖子
        Post post = postMapper.selectById(request.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 验证评论内容
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }
        
        // 如果是回复评论，验证父评论存在
        if (request.getParentId() > 0) {
            PostComment parentComment = postCommentMapper.selectById(request.getParentId());
            if (parentComment == null || parentComment.getIsDeleted() == 1) {
                throw new BusinessException("回复的评论不存在或已删除");
            }
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 创建评论
        PostComment comment = new PostComment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setReplyUserId(request.getReplyUserId());
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDeleted(0);
        
        // 保存评论
        postCommentMapper.insert(comment);
        
        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
        
        // 构建返回结果
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPostId());
        response.setUserId(comment.getUserId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParentId());
        response.setReplyUserId(comment.getReplyUserId());
        response.setLikeCount(comment.getLikeCount());
        response.setLiked(false);
        response.setCreateTime(comment.getCreateTime());
        
        // 如果是回复评论，设置被回复用户名
        if (comment.getReplyUserId() > 0) {
            User replyUser = userMapper.selectById(comment.getReplyUserId());
            if (replyUser != null) {
                response.setReplyUsername(replyUser.getUsername());
            }
        }
        
        return response;
    }

    @Override
    public List<CommentResponse> getCommentList(Long userId, Long postId) {
        // 查询帖子的所有一级评论
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostComment::getPostId, postId)
                .eq(PostComment::getParentId, 0)
                .eq(PostComment::getIsDeleted, 0)
                .orderByDesc(PostComment::getCreateTime);
        List<PostComment> comments = postCommentMapper.selectList(queryWrapper);
        
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取评论用户ID列表
        List<Long> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toList());
        
        // 查询所有回复
        LambdaQueryWrapper<PostComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(PostComment::getPostId, postId)
                .gt(PostComment::getParentId, 0)
                .eq(PostComment::getIsDeleted, 0)
                .orderByAsc(PostComment::getCreateTime);
        List<PostComment> replies = postCommentMapper.selectList(replyWrapper);
        
        // 添加回复用户ID
        userIds.addAll(replies.stream().map(PostComment::getUserId).collect(Collectors.toList()));
        userIds.addAll(replies.stream().map(PostComment::getReplyUserId).filter(id -> id > 0).collect(Collectors.toList()));
        
        // 去重
        userIds = userIds.stream().distinct().collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        
        // 组装回复数据
        Map<Long, List<CommentResponse>> replyMap = new HashMap<>();
        for (PostComment reply : replies) {
            User replyUser = userMap.get(reply.getUserId());
            if (replyUser == null) continue;
            
            CommentResponse replyResponse = new CommentResponse();
            replyResponse.setId(reply.getId());
            replyResponse.setPostId(reply.getPostId());
            replyResponse.setUserId(reply.getUserId());
            replyResponse.setUsername(replyUser.getUsername());
            replyResponse.setAvatar(replyUser.getAvatar());
            replyResponse.setContent(reply.getContent());
            replyResponse.setParentId(reply.getParentId());
            replyResponse.setReplyUserId(reply.getReplyUserId());
            
            // 设置被回复用户名
            if (reply.getReplyUserId() > 0) {
                User targetUser = userMap.get(reply.getReplyUserId());
                if (targetUser != null) {
                    replyResponse.setReplyUsername(targetUser.getUsername());
                }
            }
            
            replyResponse.setLikeCount(reply.getLikeCount());
            replyResponse.setLiked(false); // 是否点赞，实际应查询点赞表
            replyResponse.setCreateTime(reply.getCreateTime());
            replyResponse.setChildren(new ArrayList<>());
            
            if (!replyMap.containsKey(reply.getParentId())) {
                replyMap.put(reply.getParentId(), new ArrayList<>());
            }
            replyMap.get(reply.getParentId()).add(replyResponse);
        }
        
        // 组装评论列表
        List<CommentResponse> result = new ArrayList<>();
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
            commentResponse.setLikeCount(comment.getLikeCount());
            commentResponse.setLiked(false); // 是否点赞，实际应查询点赞表
            commentResponse.setCreateTime(comment.getCreateTime());
            
            // 设置子评论
            List<CommentResponse> childReplies = replyMap.getOrDefault(comment.getId(), new ArrayList<>());
            commentResponse.setChildren(childReplies);
            
            result.add(commentResponse);
        }
        
        return result;
    }

    @Override
    @Transactional
    public Boolean deletePost(Long userId, Long postId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 验证权限
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此帖子");
        }
        
        // 逻辑删除
        post.setIsDeleted(1);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteComment(Long userId, Long commentId) {
        // 查询评论
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new BusinessException("评论不存在或已删除");
        }
        
        // 验证权限
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此评论");
        }
        
        // 查询帖子
        Post post = postMapper.selectById(comment.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 逻辑删除评论
        comment.setIsDeleted(1);
        comment.setUpdateTime(LocalDateTime.now());
        postCommentMapper.updateById(comment);
        
        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() - 1);
        postMapper.updateById(post);
        
        // 如果是一级评论，同时删除其所有子评论
        if (comment.getParentId() == 0) {
            LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PostComment::getParentId, commentId)
                    .eq(PostComment::getIsDeleted, 0);
            List<PostComment> childComments = postCommentMapper.selectList(queryWrapper);
            
            for (PostComment child : childComments) {
                child.setIsDeleted(1);
                child.setUpdateTime(LocalDateTime.now());
                postCommentMapper.updateById(child);
                
                // 更新帖子评论数
                post.setCommentCount(post.getCommentCount() - 1);
            }
            
            // 最后更新帖子评论数
            postMapper.updateById(post);
        }
        
        return true;
    }
    
    /**
     * 将帖子分页数据转换为PostResponse分页数据
     */
    private Page<PostResponse> convertToPostResponsePage(Page<Post> postPage, Long userId) {
        Page<PostResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(postPage, responsePage, "records");
        
        List<PostResponse> responseList = new ArrayList<>();
        
        if (postPage.getRecords().isEmpty()) {
            responsePage.setRecords(responseList);
            return responsePage;
        }
        
        // 获取所有帖子ID
        List<Long> postIds = postPage.getRecords().stream().map(Post::getId).collect(Collectors.toList());
        
        // 获取所有帖子的图片
        LambdaQueryWrapper<PostImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.in(PostImage::getPostId, postIds)
                .orderByAsc(PostImage::getSortOrder);
        List<PostImage> images = postImageMapper.selectList(imageWrapper);
        
        // 按帖子ID分组
        Map<Long, List<String>> postImageMap = new HashMap<>();
        for (PostImage image : images) {
            if (!postImageMap.containsKey(image.getPostId())) {
                postImageMap.put(image.getPostId(), new ArrayList<>());
            }
            postImageMap.get(image.getPostId()).add(image.getImageUrl());
        }
        
        // 获取用户点赞信息
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.in(PostLike::getPostId, postIds)
                .eq(PostLike::getUserId, userId);
        List<PostLike> likes = postLikeMapper.selectList(likeWrapper);
        
        // 用户点赞的帖子ID集合
        Set<Long> likedPostIds = likes.stream().map(PostLike::getPostId).collect(Collectors.toSet());
        
        // 获取所有用户信息
        List<Long> userIds = postPage.getRecords().stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        
        // 获取所有帖子关联的跑步记录
        LambdaQueryWrapper<PostRunningRecord> recordQueryWrapper = new LambdaQueryWrapper<>();
        recordQueryWrapper.in(PostRunningRecord::getPostId, postIds);
        List<PostRunningRecord> postRunningRecords = postRunningRecordMapper.selectList(recordQueryWrapper);
        
        // 按帖子ID分组
        Map<Long, Long> postRunningRecordMap = new HashMap<>();
        for (PostRunningRecord record : postRunningRecords) {
            postRunningRecordMap.put(record.getPostId(), record.getRunningRecordId());
        }
        
        // 获取所有关联的跑步记录
        List<Long> runningRecordIds = postRunningRecords.stream()
                .map(PostRunningRecord::getRunningRecordId)
                .collect(Collectors.toList());
        
        Map<Long, RunningRecord> runningRecordMap = new HashMap<>();
        if (!runningRecordIds.isEmpty()) {
            List<RunningRecord> runningRecords = runningRecordMapper.selectBatchIds(runningRecordIds);
            for (RunningRecord record : runningRecords) {
                runningRecordMap.put(record.getId(), record);
            }
        }
        
        // 构建返回结果
        for (Post post : postPage.getRecords()) {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setUserId(post.getUserId());
            
            User user = userMap.get(post.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setAvatar(user.getAvatar());
            }
            
            response.setContent(post.getContent());
            response.setImageUrls(postImageMap.getOrDefault(post.getId(), new ArrayList<>()));
            response.setLocation(post.getLocation());
            response.setLikeCount(post.getLikeCount());
            response.setCommentCount(post.getCommentCount());
            response.setLiked(likedPostIds.contains(post.getId()));
            response.setVisibility(post.getVisibility());
            response.setCreateTime(post.getCreateTime());
            
            // 设置跑步记录
            Long runningRecordId = postRunningRecordMap.get(post.getId());
            if (runningRecordId != null) {
                RunningRecord runningRecord = runningRecordMap.get(runningRecordId);
                if (runningRecord != null) {
                    RunningRecordResponse runningRecordResponse = convertToRunningRecordResponse(runningRecord);
                    response.setRunningRecord(runningRecordResponse);
                    System.out.println("帖子列表中设置跑步记录 - 帖子ID: " + post.getId() + ", 跑步记录ID: " + runningRecordId);
                }
            }
            
            responseList.add(response);
        }
        
        responsePage.setRecords(responseList);
        return responsePage;
    }
    
    /**
     * 将Post转换为PostResponse
     *
     * @param post 帖子实体
     * @param userId 当前用户ID
     * @return 帖子响应DTO
     */
    private PostResponse convertToPostResponse(Post post, Long userId) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        
        // 获取用户信息
        User user = userMapper.selectById(post.getUserId());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setVisibility(post.getVisibility());
        response.setCreateTime(post.getCreateTime());
        
        // 获取图片
        LambdaQueryWrapper<PostImage> imageQueryWrapper = new LambdaQueryWrapper<>();
        imageQueryWrapper.eq(PostImage::getPostId, post.getId())
                .orderByAsc(PostImage::getSortOrder);
        List<PostImage> images = postImageMapper.selectList(imageQueryWrapper);
        List<String> imageUrls = images.stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        
        // 检查当前用户是否点赞
        LambdaQueryWrapper<PostLike> likeQueryWrapper = new LambdaQueryWrapper<>();
        likeQueryWrapper.eq(PostLike::getPostId, post.getId())
                .eq(PostLike::getUserId, userId);
        PostLike like = postLikeMapper.selectOne(likeQueryWrapper);
        response.setLiked(like != null);
        
        // 获取关联的跑步记录
        LambdaQueryWrapper<PostRunningRecord> recordQueryWrapper = new LambdaQueryWrapper<>();
        recordQueryWrapper.eq(PostRunningRecord::getPostId, post.getId());
        PostRunningRecord postRunningRecord = postRunningRecordMapper.selectOne(recordQueryWrapper);
        
        System.out.println("查询帖子关联的跑步记录 - 帖子ID: " + post.getId() + ", 关联记录: " + postRunningRecord);
        
        if (postRunningRecord != null) {
            RunningRecord runningRecord = runningRecordMapper.selectById(postRunningRecord.getRunningRecordId());
            System.out.println("查询到跑步记录 - 记录ID: " + postRunningRecord.getRunningRecordId() + ", 记录: " + runningRecord);
            
            if (runningRecord != null) {
                RunningRecordResponse runningRecordResponse = convertToRunningRecordResponse(runningRecord);
                response.setRunningRecord(runningRecordResponse);
                System.out.println("设置跑步记录响应: " + runningRecordResponse);
            } else {
                System.out.println("未找到跑步记录数据, ID: " + postRunningRecord.getRunningRecordId());
            }
        } else {
            System.out.println("帖子没有关联跑步记录");
        }
        
        return response;
    }

    /**
     * 将RunningRecord转换为RunningRecordResponse
     *
     * @param record 跑步记录实体
     * @return 跑步记录响应DTO
     */
    private RunningRecordResponse convertToRunningRecordResponse(RunningRecord record) {
        if (record == null) {
            return null;
        }
        
        RunningRecordResponse response = new RunningRecordResponse();
        response.setId(record.getId());
        response.setDistance(record.getDistance());
        response.setDuration(record.getDuration());
        response.setPace(record.getPace());
        response.setRecordDate(record.getRecordDate());
        response.setCreateTime(record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return response;
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long userId, PostUpdateRequest request) {
        // 验证内容不能为空
        if (!StringUtils.hasText(request.getContent()) && (request.getImageUrls() == null || request.getImageUrls().isEmpty())) {
            throw new BusinessException("内容和图片不能同时为空");
        }
        
        // 验证图片数量不能超过6张
        if (request.getImageUrls() != null && request.getImageUrls().size() > MAX_IMAGE_COUNT) {
            throw new BusinessException("图片数量不能超过6张");
        }
        
        // 查询帖子
        Post post = postMapper.selectById(request.getId());
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 验证权限
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权限编辑此帖子");
        }
        
        // 更新帖子
        post.setContent(request.getContent());
        post.setUpdateTime(LocalDateTime.now());
        
        // 强制设置为仅好友可见
        post.setVisibility(1);
        
        // 保存帖子
        postMapper.updateById(post);
        
        // 更新图片
        // 先删除原有图片
        LambdaQueryWrapper<PostImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostImage::getPostId, post.getId());
        postImageMapper.delete(queryWrapper);
        
        // 保存新的图片
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int sortOrder = 0;
            for (String imageUrl : request.getImageUrls()) {
                PostImage postImage = new PostImage();
                postImage.setPostId(post.getId());
                postImage.setImageUrl(imageUrl);
                postImage.setSortOrder(sortOrder++);
                postImage.setCreateTime(LocalDateTime.now());
                postImage.setUpdateTime(LocalDateTime.now());
                postImageMapper.insert(postImage);
                
                imageUrls.add(imageUrl);
            }
        }
        
        // 更新跑步记录关联
        LambdaQueryWrapper<PostRunningRecord> recordQueryWrapper = new LambdaQueryWrapper<>();
        recordQueryWrapper.eq(PostRunningRecord::getPostId, post.getId());
        postRunningRecordMapper.delete(recordQueryWrapper);
        
        if (request.getRunningRecordId() != null) {
            // 检查跑步记录是否存在且属于当前用户
            RunningRecord runningRecord = runningRecordMapper.selectById(request.getRunningRecordId());
            if (runningRecord != null && runningRecord.getUserId().equals(userId)) {
                // 创建关联记录
                PostRunningRecord postRunningRecord = new PostRunningRecord();
                postRunningRecord.setPostId(post.getId());
                postRunningRecord.setRunningRecordId(request.getRunningRecordId());
                postRunningRecord.setCreateTime(LocalDateTime.now());
                postRunningRecordMapper.insert(postRunningRecord);
                
                System.out.println("更新跑步记录关联 - 帖子ID: " + post.getId() + ", 跑步记录ID: " + request.getRunningRecordId());
            } else {
                System.out.println("未找到用户的跑步记录 - 用户ID: " + userId + ", 跑步记录ID: " + request.getRunningRecordId());
            }
        } else {
            System.out.println("移除帖子与跑步记录的关联 - 帖子ID: " + post.getId());
        }
        
        // 返回帖子详情
        return getPostDetail(userId, post.getId());
    }

    /**
     * 转发朋友圈帖子
     *
     * @param userId 用户ID
     * @param request 转发请求
     * @return 转发后的帖子响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostResponse forwardPost(Long userId, PostForwardRequest request) {
        // 1. 验证原始帖子是否存在
        Long originalPostId = request.getOriginalPostId();
        Post originalPost = postMapper.selectById(originalPostId);
        if (originalPost == null || originalPost.getIsDeleted() == 1) {
            throw new RuntimeException("原始帖子不存在或已删除");
        }
        
        // 2. 检查用户是否有权限查看原帖子(好友可见的帖子只有好友才能转发)
        if (originalPost.getVisibility() == 1) {
            // 检查是否是好友关系
            LambdaQueryWrapper<Friendship> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Friendship::getUserId, userId)
                    .eq(Friendship::getFriendId, originalPost.getUserId())
                    .eq(Friendship::getStatus, 1)
                    .eq(Friendship::getIsDeleted, 0);
            Friendship friendship = friendshipMapper.selectOne(queryWrapper);
            if (friendship == null) {
                throw new RuntimeException("无权限查看或转发该帖子");
            }
        }
        
        // 3. 创建新帖子
        Post newPost = new Post();
        newPost.setUserId(userId);
        newPost.setContent(request.getForwardComment());
        newPost.setVisibility(request.getVisibility());
        newPost.setLocation(request.getLocation());
        newPost.setLikeCount(0);
        newPost.setCommentCount(0);
        newPost.setForwardCount(0);
        newPost.setIsForward(1); // 标记为转发帖子
        newPost.setOriginalPostId(originalPostId);
        newPost.setCreateTime(LocalDateTime.now());
        newPost.setUpdateTime(LocalDateTime.now());
        newPost.setIsDeleted(0);
        
        postMapper.insert(newPost);
        
        // 4. 记录转发关系
        PostForward postForward = new PostForward();
        postForward.setUserId(userId);
        postForward.setOriginalPostId(originalPostId);
        postForward.setNewPostId(newPost.getId());
        postForward.setForwardComment(request.getForwardComment());
        postForward.setCreateTime(LocalDateTime.now());
        postForward.setUpdateTime(LocalDateTime.now());
        postForward.setIsDeleted(0);
        
        postForwardMapper.insert(postForward);
        
        // 5. 更新原帖子的转发计数
        originalPost.setForwardCount(originalPost.getForwardCount() + 1);
        postMapper.updateById(originalPost);
        
        // 6. 构建返回结果
        return getPostDetail(userId, newPost.getId());
    }

    /**
     * 获取原始帖子信息
     *
     * @param userId 当前用户ID
     * @param originalPostId 原始帖子ID
     * @return 原始帖子响应
     */
    @Override
    public PostResponse getOriginalPost(Long userId, Long originalPostId) {
        Post post = postMapper.selectById(originalPostId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new RuntimeException("原始帖子不存在或已删除");
        }
        
        // 检查权限
        if (post.getVisibility() == 1 && !userId.equals(post.getUserId())) {
            // 如果是仅好友可见，需要检查是否是好友关系
            LambdaQueryWrapper<Friendship> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Friendship::getUserId, userId)
                    .eq(Friendship::getFriendId, post.getUserId())
                    .eq(Friendship::getStatus, 1)
                    .eq(Friendship::getIsDeleted, 0);
            Friendship friendship = friendshipMapper.selectOne(queryWrapper);
            if (friendship == null) {
                throw new RuntimeException("无权限查看该帖子");
            }
        }
        
        return buildPostResponse(post, userId);
    }

    // 修改buildPostResponse方法，添加对转发帖子的处理
    private PostResponse buildPostResponse(Post post, Long currentUserId) {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setUserId(post.getUserId());
        postResponse.setUsername(post.getUsername());
        postResponse.setAvatar(post.getAvatar());
        postResponse.setContent(post.getContent());
        postResponse.setImageUrls(post.getImageUrls());
        postResponse.setLocation(post.getLocation());
        postResponse.setLikeCount(post.getLikeCount());
        postResponse.setCommentCount(post.getCommentCount());
        postResponse.setVisibility(post.getVisibility());
        postResponse.setCreateTime(post.getCreateTime());
        postResponse.setForwardCount(post.getForwardCount());
        postResponse.setIsForward(post.getIsForward() == 1);
        postResponse.setOriginalPostId(post.getOriginalPostId());
        
        // 如果是转发的帖子，获取原始帖子信息
        if (post.getIsForward() == 1 && post.getOriginalPostId() > 0) {
            Post originalPost = postMapper.selectById(post.getOriginalPostId());
            
            // 如果原始帖子存在且未删除
            if (originalPost != null && originalPost.getIsDeleted() == 0) {
                postResponse.setOriginalPost(buildPostResponse(originalPost, currentUserId));
                
                // 获取转发评论
                LambdaQueryWrapper<PostForward> forwardWrapper = new LambdaQueryWrapper<>();
                forwardWrapper.eq(PostForward::getNewPostId, post.getId())
                        .eq(PostForward::getOriginalPostId, post.getOriginalPostId())
                        .eq(PostForward::getIsDeleted, 0);
                PostForward postForward = postForwardMapper.selectOne(forwardWrapper);
                if (postForward != null) {
                    postResponse.setForwardComment(postForward.getForwardComment());
                }
            } else {
                // 如果原始帖子已删除，创建一个占位显示
                PostResponse deletedPost = new PostResponse();
                deletedPost.setId(post.getOriginalPostId());
                deletedPost.setContent("该帖子已被删除");
                postResponse.setOriginalPost(deletedPost);
            }
        }
        
        return postResponse;
    }
} 