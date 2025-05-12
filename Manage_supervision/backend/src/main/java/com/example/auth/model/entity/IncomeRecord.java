package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收入记录实体类
 */
@Data
@TableName("income_record")
public class IncomeRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 相关订单ID
     */
    private Long orderId;
    
    /**
     * 金额（正数表示收入，负数表示扣除）
     */
    private BigDecimal amount;
    
    /**
     * 类型：ORDER-订单收入, REFUND-退款扣除, APPEAL-申诉扣除
     */
    private String type;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 