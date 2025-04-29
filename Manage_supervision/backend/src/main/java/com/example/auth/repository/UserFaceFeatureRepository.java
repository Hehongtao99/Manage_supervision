package com.example.auth.repository;

import com.example.auth.entity.UserFaceFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFaceFeatureRepository extends JpaRepository<UserFaceFeature, Long> {
    /**
     * 通过用户ID查找用户人脸特征
     */
    List<UserFaceFeature> findByUserId(Long userId);
    
    /**
     * 通过用户ID查找最新的用户人脸特征
     */
    Optional<UserFaceFeature> findTopByUserIdOrderByUpdateTimeDesc(Long userId);
    
    /**
     * 删除用户的所有人脸特征
     */
    void deleteByUserId(Long userId);
} 