package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.RunningPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 跑步偏好数据访问接口
 */
@Mapper
public interface RunningPreferenceMapper extends BaseMapper<RunningPreference> {
    // 继承BaseMapper即可使用MyBatis-Plus提供的基础CRUD方法
} 