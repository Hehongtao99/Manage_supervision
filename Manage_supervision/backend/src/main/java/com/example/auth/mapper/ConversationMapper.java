package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    /**
     * 根据两个用户ID查找会话
     */
    @Select("SELECT * FROM conversations WHERE " +
            "(user1_id = #{user1Id} AND user2_id = #{user2Id}) OR " +
            "(user1_id = #{user2Id} AND user2_id = #{user1Id}) LIMIT 1")
    Conversation findByUserIds(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    /**
     * 查询用户参与的所有会话，按最后消息时间降序排序
     */
    @Select("SELECT * FROM conversations WHERE user1_id = #{userId} OR user2_id = #{userId} " +
            "ORDER BY last_message_time DESC")
    List<Conversation> findByUserIdOrderByLastMessageTimeDesc(@Param("userId") Long userId);
    
    /**
     * 计算用户有未读消息的会话数
     */
    @Select("SELECT COUNT(*) FROM conversations WHERE " +
            "((user1_id = #{userId} AND unread_count_user1 > 0) OR " +
            "(user2_id = #{userId} AND unread_count_user2 > 0))")
    int countConversationsWithUnreadMessages(@Param("userId") Long userId);
} 