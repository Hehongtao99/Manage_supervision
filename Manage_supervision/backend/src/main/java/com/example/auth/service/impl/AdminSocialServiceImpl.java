package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.exception.BusinessException;
import com.example.auth.mapper.*;
import com.example.auth.model.dto.response.CommentResponse;
import com.example.auth.model.dto.response.PostResponse;
import com.example.auth.model.entity.*;
import com.example.auth.service.AdminSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 管理员社交服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminSocialServiceImpl implements AdminSocialService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostForwardMapper postForwardMapper;
    private final UserMapper userMapper;

    @Override
    public Page<PostResponse> getAllPosts(Integer page, Integer size, String keyword) {
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0); // 只查询未删除的帖子
        
        // 添加关键词搜索条件
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Post::getContent, keyword);
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(Post::getCreateTime);
        
        // 分页查询帖子
        Page<Post> resultPage = postMapper.selectPage(postPage, wrapper);
        
        // 转换成响应对象
        return convertToPostResponsePage(resultPage);
    }

    @Override
    public Page<PostResponse> getUserPosts(Long userId, Integer page, Integer size) {
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId);
        wrapper.eq(Post::getIsDeleted, 0); // 只查询未删除的帖子
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(Post::getCreateTime);
        
        // 分页查询帖子
        Page<Post> resultPage = postMapper.selectPage(postPage, wrapper);
        
        // 转换成响应对象
        return convertToPostResponsePage(resultPage);
    }

    @Override
    public PostResponse getPostDetail(Long postId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 查询发布帖子的用户
        User user = userMapper.selectById(post.getUserId());
        if (user == null) {
            throw new BusinessException("帖子发布者不存在");
        }
        
        // 查询帖子图片
        List<String> imageUrls = getPostImageUrls(post.getId());
        
        // 构建响应对象
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setContent(post.getContent());
        response.setImageUrls(imageUrls);
        response.setLocation(post.getLocation());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setForwardCount(post.getForwardCount());
        response.setVisibility(post.getVisibility());
        response.setIsForward(post.getIsForward() == 1);
        response.setOriginalPostId(post.getOriginalPostId());
        response.setCreateTime(post.getCreateTime());
        response.setUpdateTime(post.getUpdateTime());
        
        // 如果是转发的帖子，查询原始帖子
        if (post.getIsForward() == 1 && post.getOriginalPostId() > 0) {
            Post originalPost = postMapper.selectById(post.getOriginalPostId());
            if (originalPost != null && originalPost.getIsDeleted() == 0) {
                PostResponse originalPostResponse = getPostDetail(originalPost.getId());
                response.setOriginalPost(originalPostResponse);
                
                // 查询转发评论
                LambdaQueryWrapper<PostForward> forwardWrapper = new LambdaQueryWrapper<>();
                forwardWrapper.eq(PostForward::getUserId, post.getUserId());
                forwardWrapper.eq(PostForward::getOriginalPostId, post.getOriginalPostId());
                forwardWrapper.eq(PostForward::getNewPostId, post.getId());
                
                PostForward postForward = postForwardMapper.selectOne(forwardWrapper);
                if (postForward != null) {
                    response.setForwardComment(postForward.getForwardComment());
                }
            } else {
                // 原帖已删除
                PostResponse deletedPost = new PostResponse();
                deletedPost.setId(post.getOriginalPostId());
                deletedPost.setContent("该帖子已被删除");
                response.setOriginalPost(deletedPost);
            }
        }
        
        return response;
    }

    @Override
    @Transactional
    public Boolean deletePost(Long postId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 逻辑删除帖子
        post.setIsDeleted(1);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        
        return true;
    }

    @Override
    public List<CommentResponse> getPostComments(Long postId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException("帖子不存在或已删除");
        }
        
        // 查询一级评论（parentId = 0）
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComment::getPostId, postId);
        wrapper.eq(PostComment::getParentId, 0L);
        wrapper.eq(PostComment::getIsDeleted, 0);
        wrapper.orderByDesc(PostComment::getCreateTime);
        
        List<PostComment> comments = postCommentMapper.selectList(wrapper);
        
        // 查询所有子评论
        LambdaQueryWrapper<PostComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(PostComment::getPostId, postId);
        replyWrapper.ne(PostComment::getParentId, 0L);
        replyWrapper.eq(PostComment::getIsDeleted, 0);
        replyWrapper.orderByAsc(PostComment::getCreateTime);
        
        List<PostComment> replies = postCommentMapper.selectList(replyWrapper);
        
        // 按父评论ID分组子评论
        Map<Long, List<PostComment>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(PostComment::getParentId));
        
        // 查询所有评论用户的信息
        Set<Long> userIds = new HashSet<>();
        comments.forEach(comment -> {
            if(comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
        });
        replies.forEach(reply -> {
            if(reply.getUserId() != null) {
                userIds.add(reply.getUserId());
            }
            if(reply.getReplyUserId() != null && reply.getReplyUserId() > 0) {
                userIds.add(reply.getReplyUserId());
            }
        });
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        }
        
        // 构建评论树
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
            commentResponse.setCreateTime(comment.getCreateTime());
            commentResponse.setUpdateTime(comment.getUpdateTime());
            
            // 设置子评论
            List<PostComment> childComments = replyMap.getOrDefault(comment.getId(), new ArrayList<>());
            List<CommentResponse> childResponses = new ArrayList<>();
            
            for (PostComment reply : childComments) {
                User replyUser = userMap.get(reply.getUserId());
                User replyToUser = userMap.get(reply.getReplyUserId());
                
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
                replyResponse.setReplyToUsername(replyToUser != null ? replyToUser.getUsername() : null);
                replyResponse.setLikeCount(reply.getLikeCount());
                replyResponse.setCreateTime(reply.getCreateTime());
                replyResponse.setUpdateTime(reply.getUpdateTime());
                
                childResponses.add(replyResponse);
            }
            
            commentResponse.setChildren(childResponses);
            result.add(commentResponse);
        }
        
        return result;
    }

    @Override
    public Page<CommentResponse> getAllComments(Integer page, Integer size, String keyword) {
        // 创建分页对象
        Page<PostComment> commentPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComment::getIsDeleted, 0); // 只查询未删除的评论
        
        // 添加关键词搜索条件
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PostComment::getContent, keyword);
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(PostComment::getCreateTime);
        
        // 分页查询评论
        Page<PostComment> resultPage = postCommentMapper.selectPage(commentPage, wrapper);
        
        // 转换成响应对象
        return convertToCommentResponsePage(resultPage);
    }

    @Override
    @Transactional
    public Boolean deleteComment(Long commentId) {
        // 查询评论
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new BusinessException("评论不存在或已删除");
        }
        
        // 逻辑删除评论
        comment.setIsDeleted(1);
        comment.setUpdateTime(LocalDateTime.now());
        postCommentMapper.updateById(comment);
        
        // 如果是父评论，同时删除其下的所有子评论
        if (comment.getParentId() == 0) {
            LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostComment::getParentId, commentId);
            wrapper.eq(PostComment::getIsDeleted, 0);
            
            List<PostComment> childComments = postCommentMapper.selectList(wrapper);
            for (PostComment childComment : childComments) {
                childComment.setIsDeleted(1);
                childComment.setUpdateTime(LocalDateTime.now());
                postCommentMapper.updateById(childComment);
            }
        }
        
        // 更新帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postMapper.updateById(post);
        }
        
        return true;
    }
    
    /**
     * 查询帖子图片URL列表
     */
    private List<String> getPostImageUrls(Long postId) {
        LambdaQueryWrapper<PostImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostImage::getPostId, postId);
        wrapper.orderByAsc(PostImage::getSortOrder);
        
        List<PostImage> postImages = postImageMapper.selectList(wrapper);
        return postImages.stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
    }
    
    /**
     * 将Post分页结果转换为PostResponse分页结果
     */
    private Page<PostResponse> convertToPostResponsePage(Page<Post> postPage) {
        Page<PostResponse> responsePage = new Page<>();
        responsePage.setCurrent(postPage.getCurrent());
        responsePage.setSize(postPage.getSize());
        responsePage.setTotal(postPage.getTotal());
        responsePage.setPages(postPage.getPages());
        
        List<Post> records = postPage.getRecords();
        if (records.isEmpty()) {
            responsePage.setRecords(new ArrayList<>());
            return responsePage;
        }
        
        // 收集所有帖子ID和用户ID
        Set<Long> postIds = records.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<Long> userIds = records.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // 收集所有转发的原始帖子ID
        Set<Long> originalPostIds = records.stream()
                .filter(p -> p.getIsForward() != null && p.getIsForward() == 1 && p.getOriginalPostId() != null && p.getOriginalPostId() > 0)
                .map(Post::getOriginalPostId)
                .collect(Collectors.toSet());
        
        postIds.addAll(originalPostIds);
        
        // 查询所有原始帖子
        Map<Long, Post> originalPostMap = new HashMap<>();
        if (!originalPostIds.isEmpty()) {
            LambdaQueryWrapper<Post> originalPostWrapper = new LambdaQueryWrapper<>();
            originalPostWrapper.in(Post::getId, originalPostIds);
            originalPostWrapper.eq(Post::getIsDeleted, 0);
            
            List<Post> originalPosts = postMapper.selectList(originalPostWrapper);
            originalPostMap = originalPosts.stream()
                    .collect(Collectors.toMap(Post::getId, p -> p));
            
            // 收集原始帖子的用户ID
            originalPosts.forEach(p -> userIds.add(p.getUserId()));
        }
        
        // 查询所有帖子图片
        Map<Long, List<String>> postImageMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            LambdaQueryWrapper<PostImage> imageWrapper = new LambdaQueryWrapper<>();
            imageWrapper.in(PostImage::getPostId, postIds);
            imageWrapper.orderByAsc(PostImage::getSortOrder);
            
            List<PostImage> postImages = postImageMapper.selectList(imageWrapper);
            postImageMap = postImages.stream()
                    .collect(Collectors.groupingBy(
                            PostImage::getPostId,
                            Collectors.mapping(PostImage::getImageUrl, Collectors.toList())
                    ));
        }
        
        // 查询所有用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }
        
        // 转换为响应对象
        List<PostResponse> responseList = new ArrayList<>();
        
        for (Post post : records) {
            User user = userMap.get(post.getUserId());
            if (user == null) continue;
            
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setUserId(post.getUserId());
            response.setUsername(user.getUsername());
            response.setAvatar(user.getAvatar());
            response.setContent(post.getContent());
            response.setImageUrls(postImageMap.getOrDefault(post.getId(), new ArrayList<>()));
            response.setLocation(post.getLocation());
            response.setLikeCount(post.getLikeCount());
            response.setCommentCount(post.getCommentCount());
            response.setForwardCount(post.getForwardCount());
            response.setLiked(false); // 管理员视角不需要这个字段
            response.setVisibility(post.getVisibility());
            response.setIsForward(post.getIsForward() == 1);
            response.setOriginalPostId(post.getOriginalPostId());
            response.setCreateTime(post.getCreateTime());
            response.setUpdateTime(post.getUpdateTime());
            
            // 如果是转发的帖子，设置原始帖子信息
            if (post.getIsForward() == 1 && post.getOriginalPostId() > 0) {
                Post originalPost = originalPostMap.get(post.getOriginalPostId());
                if (originalPost != null) {
                    User originalUser = userMap.get(originalPost.getUserId());
                    if (originalUser != null) {
                        PostResponse originalResponse = new PostResponse();
                        originalResponse.setId(originalPost.getId());
                        originalResponse.setUserId(originalPost.getUserId());
                        originalResponse.setUsername(originalUser.getUsername());
                        originalResponse.setAvatar(originalUser.getAvatar());
                        originalResponse.setContent(originalPost.getContent());
                        originalResponse.setImageUrls(postImageMap.getOrDefault(originalPost.getId(), new ArrayList<>()));
                        originalResponse.setLocation(originalPost.getLocation());
                        originalResponse.setLikeCount(originalPost.getLikeCount());
                        originalResponse.setCommentCount(originalPost.getCommentCount());
                        originalResponse.setForwardCount(originalPost.getForwardCount());
                        originalResponse.setLiked(false);
                        originalResponse.setVisibility(originalPost.getVisibility());
                        originalResponse.setCreateTime(originalPost.getCreateTime());
                        originalResponse.setUpdateTime(originalPost.getUpdateTime());
                        
                        response.setOriginalPost(originalResponse);
                    }
                } else {
                    // 原帖已删除
                    PostResponse deletedPost = new PostResponse();
                    deletedPost.setId(post.getOriginalPostId());
                    deletedPost.setContent("该帖子已被删除");
                    response.setOriginalPost(deletedPost);
                }
            }
            
            responseList.add(response);
        }
        
        responsePage.setRecords(responseList);
        return responsePage;
    }
    
    /**
     * 将PostComment分页结果转换为CommentResponse分页结果
     */
    private Page<CommentResponse> convertToCommentResponsePage(Page<PostComment> commentPage) {
        Page<CommentResponse> responsePage = new Page<>();
        responsePage.setCurrent(commentPage.getCurrent());
        responsePage.setSize(commentPage.getSize());
        responsePage.setTotal(commentPage.getTotal());
        responsePage.setPages(commentPage.getPages());
        
        List<PostComment> records = commentPage.getRecords();
        if (records.isEmpty()) {
            responsePage.setRecords(new ArrayList<>());
            return responsePage;
        }
        
        // 收集所有用户ID
        Set<Long> userIds = new HashSet<>();
        records.forEach(comment -> {
            if(comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            if(comment.getReplyUserId() != null && comment.getReplyUserId() > 0) {
                userIds.add(comment.getReplyUserId());
            }
        });
        
        // 查询所有用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }
        
        // 转换为响应对象
        List<CommentResponse> responseList = new ArrayList<>();
        
        for (PostComment comment : records) {
            User commentUser = userMap.get(comment.getUserId());
            if (commentUser == null) continue;
            
            CommentResponse response = new CommentResponse();
            response.setId(comment.getId());
            response.setPostId(comment.getPostId());
            response.setUserId(comment.getUserId());
            response.setUsername(commentUser.getUsername());
            response.setAvatar(commentUser.getAvatar());
            response.setContent(comment.getContent());
            response.setParentId(comment.getParentId());
            response.setReplyUserId(comment.getReplyUserId());
            
            if (comment.getReplyUserId() > 0) {
                User replyToUser = userMap.get(comment.getReplyUserId());
                if (replyToUser != null) {
                    response.setReplyToUsername(replyToUser.getUsername());
                }
            } else {
                response.setReplyToUsername(null);
            }
            
            response.setLikeCount(comment.getLikeCount());
            response.setLiked(false); // 管理员视角不需要这个字段
            response.setCreateTime(comment.getCreateTime());
            response.setUpdateTime(comment.getUpdateTime());
            
            responseList.add(response);
        }
        
        responsePage.setRecords(responseList);
        return responsePage;
    }
} 