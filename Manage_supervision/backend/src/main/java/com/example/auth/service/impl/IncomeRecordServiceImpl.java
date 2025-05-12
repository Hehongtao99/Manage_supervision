package com.example.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.IncomeRecordMapper;
import com.example.auth.model.entity.IncomeRecord;
import com.example.auth.service.IncomeRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收入记录服务实现类
 */
@Service
public class IncomeRecordServiceImpl extends ServiceImpl<IncomeRecordMapper, IncomeRecord> implements IncomeRecordService {
    
    private static final Logger logger = LoggerFactory.getLogger(IncomeRecordServiceImpl.class);
    
    @Override
    @Transactional
    public boolean addOrderIncome(Long teacherId, Long orderId, BigDecimal amount, String remark) {
        logger.info("添加订单收入记录: 教师ID={}, 订单ID={}, 金额={}", teacherId, orderId, amount);
        
        IncomeRecord record = new IncomeRecord();
        record.setTeacherId(teacherId);
        record.setOrderId(orderId);
        record.setAmount(amount);
        record.setType("ORDER");
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        
        return save(record);
    }
    
    @Override
    @Transactional
    public boolean addRefundDeduction(Long teacherId, Long orderId, BigDecimal amount, String remark) {
        logger.info("添加退款扣除记录: 教师ID={}, 订单ID={}, 金额={}", teacherId, orderId, amount);
        
        // 确保金额为负数
        BigDecimal deductionAmount = amount.abs().negate();
        
        IncomeRecord record = new IncomeRecord();
        record.setTeacherId(teacherId);
        record.setOrderId(orderId);
        record.setAmount(deductionAmount); // 负数表示扣除
        record.setType("REFUND");
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        
        return save(record);
    }
    
    @Override
    @Transactional
    public boolean addAppealDeduction(Long teacherId, Long orderId, BigDecimal amount, String remark) {
        logger.info("添加申诉扣除记录: 教师ID={}, 订单ID={}, 金额={}", teacherId, orderId, amount);
        
        // 确保金额为负数
        BigDecimal deductionAmount = amount.abs().negate();
        
        IncomeRecord record = new IncomeRecord();
        record.setTeacherId(teacherId);
        record.setOrderId(orderId);
        record.setAmount(deductionAmount); // 负数表示扣除
        record.setType("APPEAL");
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        
        return save(record);
    }
} 