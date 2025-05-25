package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.auth.model.College;
import com.example.auth.model.Major;
import com.example.auth.model.ClassEntity;
import java.time.LocalDateTime;
import java.util.Set;

@TableName("users")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    @TableField("create_time")
    private LocalDateTime createTime;

    private String avatar;
    
    @TableField("real_name")
    private String realName;
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String bio;
    
    private String status;
    
    @TableField("user_number")
    private String userNumber;
    
    @TableField("college_id")
    private Long collegeId;
    
    @TableField("major_id")
    private Long majorId;
    
    @TableField("class_id")
    private Long classId;

    @TableField(exist = false)
    private Set<Role> roles;
    
    @TableField(exist = false)
    private College college;
    
    @TableField(exist = false)
    private Major major;
    
    @TableField(exist = false)
    private ClassEntity classEntity;
    
    // 用于接收SQL查询中的学院专业班级名称字段
    @TableField(exist = false)
    private String collegeName;
    
    @TableField(exist = false)
    private String majorName;
    
    @TableField(exist = false)
    private String className;

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getAvatar() {
        return avatar;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getBio() {
        return bio;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getUserNumber() {
        return userNumber;
    }
    
    public Long getCollegeId() {
        return collegeId;
    }
    
    public Long getMajorId() {
        return majorId;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public College getCollege() {
        return college;
    }
    
    public Major getMajor() {
        return major;
    }
    
    public ClassEntity getClassEntity() {
        return classEntity;
    }
    
    public String getCollegeName() {
        return collegeName;
    }
    
    public String getMajorName() {
        return majorName;
    }
    
    public String getClassName() {
        return className;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }
    
    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }
    
    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public void setCollege(College college) {
        this.college = college;
    }
    
    public void setMajor(Major major) {
        this.major = major;
    }
    
    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }
    
    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }
    
    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
}