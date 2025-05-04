package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价数据访问层
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
} 