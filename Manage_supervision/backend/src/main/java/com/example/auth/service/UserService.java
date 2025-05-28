package com.example.auth.service;

import com.example.auth.model.dto.RunnerDTO;
import com.example.auth.model.dto.RunnerDetailDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    User register(String username, String password);
    User findByUsername(String username);
    User findById(Long id);
    boolean validatePassword(User user, String password);
    void changePassword(User user, String currentPassword, String newPassword);
    void updateAvatar(User user, String avatarUrl);
    User updateProfile(User user, Map<String, String> profileData);
    
    // 跑步爱好者管理相关方法
    List<RunnerDTO> getAllRunners();
    RunnerDetailDTO getRunnerDetails(Long id);
    boolean updateRunnerStatus(Long id, String status);
    boolean deleteRunner(Long id);

    /**
     * 获取用户发布的帖子数量
     * @param userId 用户ID
     * @return 帖子数量
     */
    int getUserPostCount(Long userId);
    
    /**
     * 获取用户的跑步记录数量
     * @param userId 用户ID
     * @return 跑步记录数量
     */
    int getUserRunningRecordCount(Long userId);
} 