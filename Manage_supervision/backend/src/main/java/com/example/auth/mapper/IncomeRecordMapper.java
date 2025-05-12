package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.IncomeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收入记录Mapper接口
 */
@Mapper
public interface IncomeRecordMapper extends BaseMapper<IncomeRecord> {
} 