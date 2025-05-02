package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 朋友圈帖子点赞Mapper接口
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
} 