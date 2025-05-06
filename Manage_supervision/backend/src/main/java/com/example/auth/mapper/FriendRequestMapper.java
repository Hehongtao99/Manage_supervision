package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    
    /**
     * 查询用户收到的好友请求
     */
    @Select("SELECT * FROM friend_requests WHERE to_user_id = #{userId} ORDER BY created_time DESC")
    List<FriendRequest> findReceivedRequests(@Param("userId") Long userId);
    
    /**
     * 查询用户发送的好友请求
     */
    @Select("SELECT * FROM friend_requests WHERE from_user_id = #{userId} ORDER BY created_time DESC")
    List<FriendRequest> findSentRequests(@Param("userId") Long userId);
    
    /**
     * 检查是否已经存在未处理的好友请求
     */
    @Select("SELECT COUNT(*) FROM friend_requests " +
            "WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND status = 0")
    int checkPendingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
} 