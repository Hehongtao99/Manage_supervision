package com.example.auth.model.enums;

import lombok.Getter;

/**
 * 成就等级枚举
 */
@Getter
public enum AchievementLevel {

    BRONZE("青铜", 10.0, 50.0),
    SILVER("白银", 50.0, 100.0),
    GOLD("黄金", 100.0, 200.0),
    PLATINUM("铂金", 200.0, 500.0),
    DIAMOND("钻石", 500.0, Double.MAX_VALUE);

    /**
     * 等级名称
     */
    private final String name;

    /**
     * 达到该等级的最小里程（公里）
     */
    private final Double minDistance;

    /**
     * 该等级的最大里程（公里）
     */
    private final Double maxDistance;

    AchievementLevel(String name, Double minDistance, Double maxDistance) {
        this.name = name;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    /**
     * 根据总里程获取当前等级
     *
     * @param totalDistance 总里程
     * @return 当前等级
     */
    public static AchievementLevel getLevelByDistance(Double totalDistance) {
        if (totalDistance == null || totalDistance < BRONZE.minDistance) {
            return null;
        }

        for (AchievementLevel level : AchievementLevel.values()) {
            if (totalDistance >= level.minDistance && totalDistance < level.maxDistance) {
                return level;
            }
        }

        return DIAMOND;
    }

    /**
     * 获取下一个等级
     *
     * @return 下一个等级
     */
    public AchievementLevel getNextLevel() {
        int ordinal = this.ordinal();
        if (ordinal < AchievementLevel.values().length - 1) {
            return AchievementLevel.values()[ordinal + 1];
        }
        return this;
    }
} 