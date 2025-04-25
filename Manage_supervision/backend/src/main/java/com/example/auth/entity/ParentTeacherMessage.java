package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parent_teacher_messages")
public class ParentTeacherMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;

    @Column(name = "status")
    private String status;

    @Column(name = "is_parent_read")
    private boolean isParentRead;
    
    @Column(name = "reply_read")
    private boolean replyRead;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "reply_time")
    private LocalDateTime replyTime;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        status = "unread"; // 默认状态为未读
        isParentRead = true; // 默认家长已读（因为是家长发送的消息）
        replyRead = false; // 默认回复未读
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getParent() {
        return parent;
    }

    public User getTeacher() {
        return teacher;
    }

    public User getStudent() {
        return student;
    }

    public String getContent() {
        return content;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public String getStatus() {
        return status;
    }

    public boolean isParentRead() {
        return isParentRead;
    }
    
    public boolean isReplyRead() {
        return replyRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setParentRead(boolean parentRead) {
        isParentRead = parentRead;
    }
    
    public void setReplyRead(boolean replyRead) {
        this.replyRead = replyRead;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
} 