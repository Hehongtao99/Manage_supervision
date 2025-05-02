package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.PostRunningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 朋友圈帖子关联跑步记录数据访问接口
 */
@Mapper
public interface PostRunningRecordMapper extends BaseMapper<PostRunningRecord> {
} 