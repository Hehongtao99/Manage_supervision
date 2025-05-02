package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 朋友圈帖子评论Mapper接口
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
} 