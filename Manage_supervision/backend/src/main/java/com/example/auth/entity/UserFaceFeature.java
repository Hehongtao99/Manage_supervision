package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_face_features")
public class UserFaceFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Lob
    @Column(name = "face_feature", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] faceFeature;

    @Lob
    @Column(name = "face_image", columnDefinition = "MEDIUMBLOB")
    private byte[] faceImage;

    @Column(name = "feature_type", length = 50)
    private String featureType = "primary"; // 默认为主要特征

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public byte[] getFaceFeature() {
        return faceFeature;
    }

    public byte[] getFaceImage() {
        return faceImage;
    }

    public String getFeatureType() {
        return featureType;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFaceFeature(byte[] faceFeature) {
        this.faceFeature = faceFeature;
    }

    public void setFaceImage(byte[] faceImage) {
        this.faceImage = faceImage;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
} 