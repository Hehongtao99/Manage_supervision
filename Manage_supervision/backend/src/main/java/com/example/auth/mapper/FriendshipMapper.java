package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Friendship;
import com.example.auth.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {
    
    /**
     * 查询用户的所有好友
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_friendships f ON u.id = f.friend_id " +
            "WHERE f.user_id = #{userId}")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户的所有好友ID
     */
    @Select("SELECT friend_id FROM user_friendships WHERE user_id = #{userId}")
    List<Long> findFriendUserIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 检查两个用户是否是好友关系
     */
    @Select("SELECT COUNT(*) FROM user_friendships " +
            "WHERE (user_id = #{userId1} AND friend_id = #{userId2}) " +
            "OR (user_id = #{userId2} AND friend_id = #{userId1})")
    int checkFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
} 