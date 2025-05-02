package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.RunningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 跑步记录数据访问接口
 */
@Mapper
public interface RunningRecordMapper extends BaseMapper<RunningRecord> {
} 