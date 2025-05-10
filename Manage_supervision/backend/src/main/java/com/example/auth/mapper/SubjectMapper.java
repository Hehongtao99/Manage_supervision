package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Subject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程科目Mapper接口
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {
} 