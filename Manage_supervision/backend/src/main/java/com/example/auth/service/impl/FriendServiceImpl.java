package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.FriendRequestMapper;
import com.example.auth.mapper.FriendshipMapper;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.FriendRequestDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.*;
import com.example.auth.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 好友关系服务实现类
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    
    private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);
    
    private final FriendshipMapper friendshipMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public List<Map<String, Object>> getFriendsByUserId(Long userId) {
        logger.info("获取用户ID为{}的好友列表", userId);
        
        List<User> friends = friendshipMapper.findFriendsByUserId(userId);
        return friends.stream()
            .map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("name", user.getNickname() != null && !user.getNickname().isEmpty() 
                          ? user.getNickname() 
                          : (user.getRealName() != null && !user.getRealName().isEmpty() 
                            ? user.getRealName() 
                            : user.getUsername()));
                map.put("avatar", user.getAvatar());
                return map;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> searchUsers(String keyword, Long currentUserId) {
        logger.info("搜索用户, 关键字: {}, 当前用户ID: {}", keyword, currentUserId);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 查询符合条件的用户
        List<User> users = userMapper.searchUsers("%" + keyword.trim() + "%");
        
        Role adminRole = roleMapper.findByName("ADMIN");
        
        final Long adminRoleId = adminRole != null ? adminRole.getId() : -1L;
        
        return users.stream()
            .filter(user -> {
                // 过滤掉自己
                if (user.getId().equals(currentUserId)) {
                    return false;
                }
                
                // 加载用户角色
                List<Role> roles = roleMapper.findRolesByUserId(user.getId());
                user.setRoles(new HashSet<>(roles));
                
                // 过滤掉管理员
                boolean isAdmin = roles.stream()
                    .anyMatch(role -> role.getId().equals(adminRoleId));
                
                return !isAdmin;
            })
            .map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("name", user.getNickname() != null && !user.getNickname().isEmpty() 
                          ? user.getNickname() 
                          : (user.getRealName() != null && !user.getRealName().isEmpty() 
                            ? user.getRealName() 
                            : user.getUsername()));
                map.put("avatar", user.getAvatar());
                
                // 检查是否已经是好友
                boolean isFriend = checkFriendship(currentUserId, user.getId());
                map.put("isFriend", isFriend);
                
                // 检查是否有待处理的好友请求
                int pendingRequest = friendRequestMapper.checkPendingRequest(currentUserId, user.getId());
                map.put("hasPendingRequest", pendingRequest > 0);
                
                return map;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        logger.info("发送好友请求, 发送者ID: {}, 接收者ID: {}", fromUserId, toUserId);
        
        // 检查用户是否存在
        User fromUser = userMapper.selectById(fromUserId);
        User toUser = userMapper.selectById(toUserId);
        
        if (fromUser == null || toUser == null) {
            logger.error("发送好友请求失败: 用户不存在");
            return false;
        }
        
        // 检查是否已经是好友
        if (checkFriendship(fromUserId, toUserId)) {
            logger.error("发送好友请求失败: 已经是好友关系");
            return false;
        }
        
        // 检查是否已经有待处理的请求
        int pendingRequests = friendRequestMapper.checkPendingRequest(fromUserId, toUserId);
        if (pendingRequests > 0) {
            logger.error("发送好友请求失败: 已经有待处理的请求");
            return false;
        }
        
        // 创建好友请求
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus(0); // 待处理
        request.setCreatedTime(LocalDateTime.now());
        request.setUpdatedTime(LocalDateTime.now());
        
        int result = friendRequestMapper.insert(request);
        
        // 如果请求创建成功，发送WebSocket通知
        if (result > 0) {
            sendFriendRequestNotification(request, toUser);
        }
        
        return result > 0;
    }
    
    /**
     * 发送好友请求通知
     */
    private void sendFriendRequestNotification(FriendRequest request, User toUser) {
        try {
            // 加载请求相关用户信息
            User fromUser = userMapper.selectById(request.getFromUserId());
            
            // 创建通知数据
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_REQUEST");
            
            // 添加请求信息
            FriendRequestDTO requestDTO = new FriendRequestDTO();
            BeanUtils.copyProperties(request, requestDTO);
            
            // 添加发送者信息
            if (fromUser != null) {
                UserDTO fromUserDTO = new UserDTO();
                fromUserDTO.setId(fromUser.getId());
                fromUserDTO.setUsername(fromUser.getUsername());
                fromUserDTO.setRealName(fromUser.getRealName());
                fromUserDTO.setNickname(fromUser.getNickname());
                fromUserDTO.setAvatar(fromUser.getAvatar());
                requestDTO.setFromUser(fromUserDTO);
            }
            
            // 添加接收者信息
            if (toUser != null) {
                UserDTO toUserDTO = new UserDTO();
                toUserDTO.setId(toUser.getId());
                toUserDTO.setUsername(toUser.getUsername());
                toUserDTO.setRealName(toUser.getRealName());
                toUserDTO.setNickname(toUser.getNickname());
                toUserDTO.setAvatar(toUser.getAvatar());
                requestDTO.setToUser(toUserDTO);
            }
            
            notification.put("request", requestDTO);
            
            // 发送WebSocket通知
            messagingTemplate.convertAndSendToUser(
                toUser.getId().toString(),
                "/queue/notifications",
                notification
            );
            
            logger.info("发送好友请求通知成功, 请求ID: {}, 接收者ID: {}", request.getId(), toUser.getId());
        } catch (Exception e) {
            logger.error("发送好友请求通知失败", e);
        }
    }
    
    @Override
    @Transactional
    public boolean processFriendRequest(Long requestId, Long userId, boolean accept) {
        logger.info("处理好友请求, 请求ID: {}, 用户ID: {}, 是否接受: {}", requestId, userId, accept);
        
        // 查询请求
        FriendRequest request = friendRequestMapper.selectById(requestId);
        
        if (request == null) {
            logger.error("处理好友请求失败: 请求不存在");
            return false;
        }
        
        // 检查请求是否是发给当前用户的
        if (!request.getToUserId().equals(userId)) {
            logger.error("处理好友请求失败: 没有权限处理此请求");
            return false;
        }
        
        // 检查请求状态
        if (request.getStatus() != 0) {
            logger.error("处理好友请求失败: 请求已经被处理");
            return false;
        }
        
        // 更新请求状态
        request.setStatus(accept ? 1 : 2); // 1-已接受, 2-已拒绝
        request.setUpdatedTime(LocalDateTime.now());
        friendRequestMapper.updateById(request);
        
        // 如果接受请求，建立好友关系
        if (accept) {
            // 建立双向好友关系
            Friendship friendship1 = new Friendship();
            friendship1.setUserId(request.getFromUserId());
            friendship1.setFriendId(request.getToUserId());
            friendship1.setCreateTime(LocalDateTime.now());
            friendshipMapper.insert(friendship1);
            
            Friendship friendship2 = new Friendship();
            friendship2.setUserId(request.getToUserId());
            friendship2.setFriendId(request.getFromUserId());
            friendship2.setCreateTime(LocalDateTime.now());
            friendshipMapper.insert(friendship2);
        }
        
        // 发送请求状态变更通知
        sendFriendRequestStatusNotification(request);
        
        return true;
    }
    
    /**
     * 发送好友请求状态变更通知
     */
    private void sendFriendRequestStatusNotification(FriendRequest request) {
        try {
            // 加载请求相关用户信息
            User fromUser = userMapper.selectById(request.getFromUserId());
            User toUser = userMapper.selectById(request.getToUserId());
            
            // 创建通知数据
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REQUEST_UPDATED");
            
            // 添加请求信息
            FriendRequestDTO requestDTO = new FriendRequestDTO();
            BeanUtils.copyProperties(request, requestDTO);
            
            // 添加发送者信息
            if (fromUser != null) {
                UserDTO fromUserDTO = new UserDTO();
                fromUserDTO.setId(fromUser.getId());
                fromUserDTO.setUsername(fromUser.getUsername());
                fromUserDTO.setRealName(fromUser.getRealName());
                fromUserDTO.setNickname(fromUser.getNickname());
                fromUserDTO.setAvatar(fromUser.getAvatar());
                requestDTO.setFromUser(fromUserDTO);
            }
            
            // 添加接收者信息
            if (toUser != null) {
                UserDTO toUserDTO = new UserDTO();
                toUserDTO.setId(toUser.getId());
                toUserDTO.setUsername(toUser.getUsername());
                toUserDTO.setRealName(toUser.getRealName());
                toUserDTO.setNickname(toUser.getNickname());
                toUserDTO.setAvatar(toUser.getAvatar());
                requestDTO.setToUser(toUserDTO);
            }
            
            notification.put("request", requestDTO);
            
            // 向发送请求的用户发送通知
            messagingTemplate.convertAndSendToUser(
                fromUser.getId().toString(),
                "/queue/notifications",
                notification
            );
            
            logger.info("发送好友请求状态变更通知成功, 请求ID: {}, 发送者ID: {}", request.getId(), fromUser.getId());
        } catch (Exception e) {
            logger.error("发送好友请求状态变更通知失败", e);
        }
    }
    
    @Override
    public List<FriendRequestDTO> getReceivedRequests(Long userId) {
        logger.info("获取用户ID为{}收到的好友请求", userId);
        
        List<FriendRequest> requests = friendRequestMapper.findReceivedRequests(userId);
        return convertToFriendRequestDTOList(requests);
    }
    
    @Override
    public List<FriendRequestDTO> getSentRequests(Long userId) {
        logger.info("获取用户ID为{}发送的好友请求", userId);
        
        List<FriendRequest> requests = friendRequestMapper.findSentRequests(userId);
        return convertToFriendRequestDTOList(requests);
    }
    
    @Override
    public boolean checkFriendship(Long userId1, Long userId2) {
        return friendshipMapper.checkFriendship(userId1, userId2) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        logger.info("删除好友关系, 用户ID: {}, 好友ID: {}", userId, friendId);
        
        // 删除双向好友关系
        LambdaQueryWrapper<Friendship> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, friendId);
        
        LambdaQueryWrapper<Friendship> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friendship::getUserId, friendId)
                .eq(Friendship::getFriendId, userId);
        
        int result1 = friendshipMapper.delete(wrapper1);
        int result2 = friendshipMapper.delete(wrapper2);
        
        return result1 > 0 && result2 > 0;
    }
    
    /**
     * 将FriendRequest列表转换为FriendRequestDTO列表
     */
    private List<FriendRequestDTO> convertToFriendRequestDTOList(List<FriendRequest> requests) {
        List<FriendRequestDTO> dtoList = new ArrayList<>();
        
        for (FriendRequest request : requests) {
            FriendRequestDTO dto = new FriendRequestDTO();
            BeanUtils.copyProperties(request, dto);
            
            // 加载发送者和接收者信息
            User fromUser = userMapper.selectById(request.getFromUserId());
            User toUser = userMapper.selectById(request.getToUserId());
            
            if (fromUser != null) {
                UserDTO fromUserDTO = new UserDTO();
                fromUserDTO.setId(fromUser.getId());
                fromUserDTO.setUsername(fromUser.getUsername());
                fromUserDTO.setRealName(fromUser.getRealName());
                fromUserDTO.setNickname(fromUser.getNickname());
                fromUserDTO.setAvatar(fromUser.getAvatar());
                dto.setFromUser(fromUserDTO);
            }
            
            if (toUser != null) {
                UserDTO toUserDTO = new UserDTO();
                toUserDTO.setId(toUser.getId());
                toUserDTO.setUsername(toUser.getUsername());
                toUserDTO.setRealName(toUser.getRealName());
                toUserDTO.setNickname(toUser.getNickname());
                toUserDTO.setAvatar(toUser.getAvatar());
                dto.setToUser(toUserDTO);
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
} 