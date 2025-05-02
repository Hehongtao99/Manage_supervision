package com.example.auth.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.example.auth.mapper.RunningRecordMapper;
import com.example.auth.model.dto.response.AchievementResponse;
import com.example.auth.model.entity.RunningRecord;
import com.example.auth.model.enums.AchievementLevel;
import com.example.auth.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 成就服务实现类
 */
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final RunningRecordMapper runningRecordMapper;

    @Override
    public AchievementResponse getUserAchievements(Long userId) {
        // 获取用户总跑步里程
        Double totalDistance = calculateTotalDistance(userId);
        
        // 根据总里程获取当前等级
        AchievementLevel currentLevel = AchievementLevel.getLevelByDistance(totalDistance);
        
        // 如果尚未达到最低等级，设为青铜
        if (currentLevel == null) {
            currentLevel = AchievementLevel.BRONZE;
        }
        
        // 获取下一等级
        AchievementLevel nextLevel = currentLevel.getNextLevel();
        
        // 获取已达到的所有等级
        List<String> achievedLevels = getAchievedLevels(totalDistance);
        
        return AchievementResponse.builder()
                .totalDistance(totalDistance)
                .currentLevel(currentLevel.name())
                .nextLevel(nextLevel.name())
                .achievedLevels(achievedLevels)
                .build();
    }
    
    /**
     * 计算用户总跑步里程
     *
     * @param userId 用户ID
     * @return 总里程
     */
    private Double calculateTotalDistance(Long userId) {
        List<RunningRecord> records = new LambdaQueryChainWrapper<>(runningRecordMapper)
                .eq(RunningRecord::getUserId, userId)
                .list();
        
        if (records.isEmpty()) {
            return 0.0;
        }
        
        return records.stream()
                .mapToDouble(record -> record.getDistance().doubleValue())
                .sum();
    }
    
    /**
     * 获取已达到的所有等级
     *
     * @param totalDistance 总里程
     * @return 已达到的等级列表
     */
    private List<String> getAchievedLevels(Double totalDistance) {
        List<String> levels = new ArrayList<>();
        
        for (AchievementLevel level : AchievementLevel.values()) {
            if (totalDistance >= level.getMinDistance()) {
                levels.add(level.name());
            } else {
                break;
            }
        }
        
        return levels;
    }
} 