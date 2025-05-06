package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友请求实体类
 */
@Data
@TableName("friend_requests")
public class FriendRequest {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 发送请求的用户ID
     */
    @TableField("from_user_id")
    private Long fromUserId;
    
    /**
     * 接收请求的用户ID
     */
    @TableField("to_user_id")
    private Long toUserId;
    
    /**
     * 请求消息
     */
    @TableField("message")
    private String message;
    
    /**
     * 状态：0-待处理，1-已接受，2-已拒绝
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private User fromUser;
    
    @TableField(exist = false)
    private User toUser;
} 