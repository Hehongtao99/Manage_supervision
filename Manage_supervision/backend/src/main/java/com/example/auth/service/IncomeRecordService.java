package com.example.auth.service;

import com.example.auth.model.entity.IncomeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * 收入记录服务接口
 */
public interface IncomeRecordService extends IService<IncomeRecord> {
    
    /**
     * 添加订单收入记录
     * @param teacherId 教师ID
     * @param orderId 订单ID
     * @param amount 金额
     * @param remark 备注
     * @return 是否成功
     */
    boolean addOrderIncome(Long teacherId, Long orderId, BigDecimal amount, String remark);
    
    /**
     * 添加退款扣除记录
     * @param teacherId 教师ID
     * @param orderId 订单ID
     * @param amount 金额（正数，会自动转为负数记录）
     * @param remark 备注
     * @return 是否成功
     */
    boolean addRefundDeduction(Long teacherId, Long orderId, BigDecimal amount, String remark);
    
    /**
     * 添加申诉扣除记录
     * @param teacherId 教师ID
     * @param orderId 订单ID
     * @param amount 金额（正数，会自动转为负数记录）
     * @param remark 备注
     * @return 是否成功
     */
    boolean addAppealDeduction(Long teacherId, Long orderId, BigDecimal amount, String remark);
} 