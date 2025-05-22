package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.SocialTakedown;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社交内容下架记录Mapper接口
 */
@Mapper
public interface SocialTakedownMapper extends BaseMapper<SocialTakedown> {
} 