package com.example.auth.dto;

import com.example.auth.entity.ParentTeacherMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ParentTeacherMessageDTO {
    private Long id;
    private Long parentId;
    private String parentName;
    private String parentUsername;
    private Long teacherId;
    private String teacherName;
    private String teacherUsername;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private String studentUserNumber;
    private String className;
    private String content;
    private String replyContent;
    private String status;
    private boolean isParentRead;
    private boolean replyRead;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime replyTime;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public ParentTeacherMessageDTO(ParentTeacherMessage message) {
        this.id = message.getId();
        
        try {
            if (message.getParent() != null) {
                this.parentId = message.getParent().getId();
                this.parentName = message.getParent().getRealName() != null ? 
                    message.getParent().getRealName() : message.getParent().getUsername();
                this.parentUsername = message.getParent().getUsername();
                System.out.println("家长信息设置成功: ID=" + this.parentId + ", 姓名=" + this.parentName);
            } else {
                System.out.println("留言[" + message.getId() + "]没有关联的家长信息");
            }
            
            if (message.getTeacher() != null) {
                this.teacherId = message.getTeacher().getId();
                this.teacherName = message.getTeacher().getRealName() != null ? 
                    message.getTeacher().getRealName() : message.getTeacher().getUsername();
                this.teacherUsername = message.getTeacher().getUsername();
                System.out.println("教师信息设置成功: ID=" + this.teacherId + ", 姓名=" + this.teacherName);
            } else {
                System.out.println("留言[" + message.getId() + "]没有关联的教师信息");
            }
            
            if (message.getStudent() != null) {
                this.studentId = message.getStudent().getId();
                this.studentName = message.getStudent().getRealName() != null ? 
                    message.getStudent().getRealName() : message.getStudent().getUsername();
                this.studentUsername = message.getStudent().getUsername();
                this.studentUserNumber = message.getStudent().getUserNumber();
                System.out.println("学生信息设置成功: ID=" + this.studentId + ", 姓名=" + this.studentName);
            } else {
                System.out.println("留言[" + message.getId() + "]没有关联的学生信息");
                // 明确设置为空值，不使用默认的"未指定"
                this.studentId = null;
                this.studentName = null;
                this.studentUsername = null;
            }
            
            this.content = message.getContent();
            this.replyContent = message.getReplyContent();
            this.status = message.getStatus();
            this.isParentRead = message.isParentRead();
            this.replyRead = message.isReplyRead();
            this.createTime = message.getCreateTime();
            this.updateTime = message.getUpdateTime();
            this.replyTime = message.getReplyTime();
            this.fileUrl = message.getFileUrl();
            this.fileName = message.getFileName();
            this.fileType = message.getFileType();
            this.fileSize = message.getFileSize();
            
            System.out.println("留言DTO构建完成: ID=" + this.id + ", 状态=" + this.status);
        } catch (Exception e) {
            System.err.println("构建ParentTeacherMessageDTO时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常以便上层捕获
        }
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public String getParentUsername() {
        return parentUsername;
    }
    
    public void setParentUsername(String parentUsername) {
        this.parentUsername = parentUsername;
    }
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public String getTeacherUsername() {
        return teacherUsername;
    }
    
    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getStudentUsername() {
        return studentUsername;
    }
    
    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }
    
    public String getStudentUserNumber() {
        return studentUserNumber;
    }
    
    public void setStudentUserNumber(String studentUserNumber) {
        this.studentUserNumber = studentUserNumber;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getReplyContent() {
        return replyContent;
    }
    
    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isParentRead() {
        return isParentRead;
    }
    
    public void setParentRead(boolean parentRead) {
        isParentRead = parentRead;
    }
    
    public boolean isReplyRead() {
        return replyRead;
    }
    
    public void setReplyRead(boolean replyRead) {
        this.replyRead = replyRead;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public LocalDateTime getReplyTime() {
        return replyTime;
    }
    
    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
} 