package com.example.auth.service;

import com.example.auth.model.dto.FriendRequestDTO;
import com.example.auth.model.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 好友关系服务接口
 */
public interface FriendService {
    
    /**
     * 获取用户的好友列表
     */
    List<Map<String, Object>> getFriendsByUserId(Long userId);
    
    /**
     * 搜索用户
     */
    List<Map<String, Object>> searchUsers(String keyword, Long currentUserId);
    
    /**
     * 发送好友请求
     */
    boolean sendFriendRequest(Long fromUserId, Long toUserId, String message);
    
    /**
     * 处理好友请求
     */
    boolean processFriendRequest(Long requestId, Long userId, boolean accept);
    
    /**
     * 获取收到的好友请求
     */
    List<FriendRequestDTO> getReceivedRequests(Long userId);
    
    /**
     * 获取发送的好友请求
     */
    List<FriendRequestDTO> getSentRequests(Long userId);
    
    /**
     * 检查两个用户是否是好友
     */
    boolean checkFriendship(Long userId1, Long userId2);
    
    /**
     * 删除好友
     */
    boolean deleteFriend(Long userId, Long friendId);
} 