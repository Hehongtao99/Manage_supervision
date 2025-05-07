package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Data
@TableName("user_friendships")
public class Friendship {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 好友ID
     */
    @TableField("friend_id")
    private Long friendId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 好友关系状态：0-待接受，1-已接受
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;
    
    // 非数据库字段
    @TableField(exist = false)
    private User friend;
} 