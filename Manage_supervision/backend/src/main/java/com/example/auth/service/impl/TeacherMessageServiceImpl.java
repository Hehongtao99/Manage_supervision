package com.example.auth.service.impl;

import com.example.auth.dto.ParentTeacherMessageDTO;
import com.example.auth.entity.ParentTeacherMessage;
import com.example.auth.entity.User;
import com.example.auth.repository.ParentTeacherMessageRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.TeacherMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师留言服务实现类
 */
@Service
public class TeacherMessageServiceImpl implements TeacherMessageService {

    @Autowired
    private ParentTeacherMessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Page<ParentTeacherMessageDTO> getMessagesByTeacher(Long teacherId, int page, int size) {
        System.out.println("开始查询教师留言: teacherId=" + teacherId + ", page=" + page + ", size=" + size);
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<ParentTeacherMessage> messagePage = messageRepository.findByTeacherId(teacherId, pageRequest);
            
            System.out.println("教师留言查询结果: 总记录数=" + messagePage.getTotalElements() + ", 总页数=" + messagePage.getTotalPages());
            
            if (messagePage.isEmpty()) {
                System.out.println("教师无留言记录");
                return new PageImpl<>(new ArrayList<>(), pageRequest, 0);
            }
            
            List<ParentTeacherMessageDTO> dtoList = messagePage.getContent().stream()
                    .map(message -> {
                        try {
                            return new ParentTeacherMessageDTO(message);
                        } catch (Exception e) {
                            System.err.println("转换留言DTO异常: messageId=" + message.getId() + ", 错误: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            
            System.out.println("转换后DTO列表大小: " + dtoList.size());
            return new PageImpl<>(dtoList, pageRequest, messagePage.getTotalElements());
        } catch (Exception e) {
            System.err.println("获取教师留言异常: " + e.getMessage());
            e.printStackTrace();
            // 返回空列表而不是抛出异常
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }
    
    @Override
    public Page<ParentTeacherMessageDTO> getMessagesByTeacherAndStudent(Long teacherId, Long studentId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ParentTeacherMessage> messagePage = messageRepository.findByTeacherIdAndStudentId(teacherId, studentId, pageRequest);
        
        List<ParentTeacherMessageDTO> dtoList = messagePage.getContent().stream()
                .map(ParentTeacherMessageDTO::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageRequest, messagePage.getTotalElements());
    }
    
    @Override
    @Transactional
    public ParentTeacherMessageDTO replyMessage(Long messageId, Long teacherId, String replyContent) {
        // 查询留言
        ParentTeacherMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("留言不存在"));
        
        // 验证教师权限
        if (!message.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("无权回复此留言");
        }
        
        // 更新留言信息
        message.setReplyContent(replyContent);
        message.setStatus("replied"); // 状态更新为已回复
        message.setReplyTime(LocalDateTime.now());
        message.setParentRead(false); // 设置为家长未读（因为是教师新回复的）
        message.setReplyRead(false); // 设置回复未读
        
        // 保存更新
        ParentTeacherMessage updatedMessage = messageRepository.save(message);
        
        // 返回DTO
        return new ParentTeacherMessageDTO(updatedMessage);
    }
    
    @Override
    @Transactional
    public int markAllAsRead(Long teacherId) {
        return messageRepository.markAllAsRead(teacherId, "read");
    }
} 