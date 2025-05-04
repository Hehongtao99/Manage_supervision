package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "avatar")
    private String avatar;
    
    @Column(name = "real_name")
    private String realName;
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "bio", length = 500)
    private String bio;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "user_number", unique = true)
    private String userNumber;
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
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

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
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

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}