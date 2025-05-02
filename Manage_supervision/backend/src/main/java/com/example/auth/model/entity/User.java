package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    @TableField(exist = false)
    private Set<Role> roles;

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
}