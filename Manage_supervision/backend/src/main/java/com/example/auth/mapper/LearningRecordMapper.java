package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习记录Mapper接口
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {
} 