package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 查询会话中的所有消息（按发送时间升序）
     */
    @Select("SELECT * FROM chat_messages WHERE conversation_id = #{conversationId} ORDER BY sent_time ASC")
    List<ChatMessage> findByConversationIdOrderBySentTimeAsc(@Param("conversationId") Long conversationId);
    
    /**
     * 分页查询会话中的消息（按发送时间降序）
     */
    @Select("SELECT * FROM chat_messages WHERE conversation_id = #{conversationId} ORDER BY sent_time DESC")
    IPage<ChatMessage> findByConversationIdOrderBySentTimeDesc(Page<ChatMessage> page, @Param("conversationId") Long conversationId);
    
    /**
     * 查询比指定消息ID更早的消息
     */
    @Select("SELECT * FROM chat_messages WHERE conversation_id = #{conversationId} AND id < #{messageId} ORDER BY sent_time DESC")
    IPage<ChatMessage> findOlderMessages(
        Page<ChatMessage> page,
        @Param("conversationId") Long conversationId,
        @Param("messageId") Long messageId
    );
    
    /**
     * 将会话中特定用户的未读消息标记为已读
     */
    @Update("UPDATE chat_messages SET is_read = true " +
            "WHERE conversation_id = #{conversationId} AND recipient_id = #{recipientId} AND is_read = false")
    int markAllAsRead(@Param("conversationId") Long conversationId, @Param("recipientId") Long recipientId);
    
    /**
     * 计算会话中用户的未读消息数
     */
    @Select("SELECT COUNT(*) FROM chat_messages " +
            "WHERE conversation_id = #{conversationId} AND recipient_id = #{recipientId} AND is_read = false")
    int countUnreadMessages(@Param("conversationId") Long conversationId, @Param("recipientId") Long recipientId);
    
    /**
     * 计算用户的所有未读消息数
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE recipient_id = #{recipientId} AND is_read = false")
    int countTotalUnreadMessagesByUser(@Param("recipientId") Long recipientId);
} 