package com.example.auth.service.impl;

import com.example.auth.dto.ParentTeacherMessageDTO;
import com.example.auth.dto.ParentTeacherMessageRequest;
import com.example.auth.entity.ParentTeacherMessage;
import com.example.auth.entity.User;
import com.example.auth.repository.ParentTeacherMessageRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ParentTeacherMessageService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 家长-教师留言服务实现类
 */
@Service
public class ParentTeacherMessageServiceImpl implements ParentTeacherMessageService {

    @Autowired
    private ParentTeacherMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 家长发送留言给教师
     */
    @Override
    @Transactional
    public ParentTeacherMessageDTO sendMessage(User parent, ParentTeacherMessageRequest request) {
        try {
            System.out.println("开始处理留言发送 - 家长ID: " + parent.getId());
            
            // 验证请求参数
            if (request.getTeacherId() == null) {
                System.err.println("错误: 教师ID为空");
                throw new IllegalArgumentException("教师ID不能为空");
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                System.err.println("错误: 留言内容为空");
                throw new IllegalArgumentException("留言内容不能为空");
            }

            // 获取教师用户
            System.out.println("查询教师 - ID: " + request.getTeacherId());
            Optional<User> teacherOpt = userRepository.findById(request.getTeacherId());
            if (!teacherOpt.isPresent()) {
                System.err.println("错误: 未找到教师 - ID: " + request.getTeacherId());
                throw new IllegalArgumentException("教师不存在");
            }
            User teacher = teacherOpt.get();
            System.out.println("找到教师 - ID: " + teacher.getId() + ", 用户名: " + teacher.getUsername());

            // 验证教师角色
            System.out.println("验证教师角色 - 用户ID: " + teacher.getId() + ", 用户名: " + teacher.getUsername());
            if (teacher.getRoles() != null) {
                System.out.println("教师角色信息: " + teacher.getRoles());
                // 详细打印每个角色
                teacher.getRoles().forEach(role -> {
                    System.out.println("角色ID: " + role.getId() + ", 角色名称: " + role.getName());
                });
            }

            // 检查是否拥有教师角色或督导员角色 - 不区分大小写
            boolean isTeacher = teacher.getRoles().stream()
                .anyMatch(role -> "TEACHER".equalsIgnoreCase(role.getName()) || 
                           "teacher".equalsIgnoreCase(role.getName()) ||
                           "SUPERVISOR".equalsIgnoreCase(role.getName()) ||
                           role.getName().toUpperCase().contains("TEACHER"));

            System.out.println("验证教师角色 - 是否为教师: " + isTeacher);
            if (!isTeacher) {
                System.err.println("错误: 用户不是教师 - ID: " + teacher.getId() + ", 用户名: " + teacher.getUsername());
                throw new IllegalArgumentException("指定用户不是教师或督导员，无法接收留言");
            }

            User student = null;
            // 如果提供了学生ID并且不为0，则验证学生
            if (request.getStudentId() != null && request.getStudentId() > 0) {
                System.out.println("查询学生 - ID: " + request.getStudentId());
                // 获取学生用户
                Optional<User> studentOpt = userRepository.findById(request.getStudentId());
                if (!studentOpt.isPresent()) {
                    System.err.println("错误: 未找到学生 - ID: " + request.getStudentId());
                    throw new IllegalArgumentException("学生不存在");
                }
                student = studentOpt.get();
                System.out.println("找到学生 - ID: " + student.getId() + ", 用户名: " + student.getUsername());
            } else {
                System.out.println("未提供学生ID或ID为0，不关联学生");
            }

            // 创建留言
            System.out.println("创建留言对象");
            ParentTeacherMessage message = new ParentTeacherMessage();
            message.setParent(parent);
            message.setTeacher(teacher);
            message.setStudent(student); // 可能为null
            message.setContent(request.getContent());
            
            // 设置文件信息（如果有）
            if (request.getFileUrl() != null && !request.getFileUrl().trim().isEmpty()) {
                System.out.println("设置文件信息 - URL: " + request.getFileUrl());
                message.setFileUrl(request.getFileUrl());
                message.setFileName(request.getFileName());
                message.setFileType(request.getFileType());
                message.setFileSize(request.getFileSize());
            }
            
            // 保存留言
            System.out.println("开始保存留言到数据库");
            ParentTeacherMessage savedMessage = messageRepository.save(message);
            System.out.println("留言保存成功 - ID: " + savedMessage.getId());
            
            // 返回DTO
            ParentTeacherMessageDTO dto = new ParentTeacherMessageDTO(savedMessage);
            System.out.println("留言DTO创建成功 - ID: " + dto.getId());
            return dto;
        } catch (Exception e) {
            System.err.println("处理留言时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让调用者处理
        }
    }

    // 实现获取家长留言列表的方法
    @Override
    public Page<ParentTeacherMessageDTO> getMessagesByParent(Long parentId, int page, int size) {
        System.out.println("开始查询家长留言: parentId=" + parentId + ", page=" + page + ", size=" + size);
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<ParentTeacherMessage> messagePage = messageRepository.findByParentId(parentId, pageRequest);
            
            System.out.println("家长留言查询结果: 总记录数=" + messagePage.getTotalElements() + ", 总页数=" + messagePage.getTotalPages());
            
            if (messagePage.isEmpty()) {
                System.out.println("家长无留言记录");
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
            System.err.println("获取家长留言异常: " + e.getMessage());
            e.printStackTrace();
            // 返回空列表而不是抛出异常
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }

    // 实现标记家长留言回复为已读的方法
    @Override
    @Transactional
    public int markAllRepliesAsReadByParent(Long parentId) {
        System.out.println("开始标记家长留言回复为已读: parentId=" + parentId);
        try {
            int count = messageRepository.markAllRepliesAsReadByParent(parentId);
            System.out.println("标记家长留言回复已读成功: 更新记录数=" + count);
            return count;
        } catch (Exception e) {
            System.err.println("标记家长留言回复已读异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 