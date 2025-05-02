package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * 朋友圈帖子Mapper接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
} 