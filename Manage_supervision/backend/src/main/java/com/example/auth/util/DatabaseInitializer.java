package com.example.auth.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化工具，用于在应用启动时执行SQL脚本
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查表是否已存在
        boolean tableExists = checkIfTableExists("running_preferences");
        
        // 如果表不存在，创建表
        if (!tableExists) {
            createRunningPreferencesTable();
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean checkIfTableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?", Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 创建跑步偏好表
     */
    private void createRunningPreferencesTable() {
        try {
            // 创建表
            jdbcTemplate.execute("CREATE TABLE running_preferences (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "frequency VARCHAR(50)," +
                "preferred_distance VARCHAR(50)," +
                "pace VARCHAR(50)," +
                "environment VARCHAR(50)," +
                "motto VARCHAR(255)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "CONSTRAINT FK_running_preferences_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE," +
                "CONSTRAINT UK_running_preferences_user UNIQUE (user_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci");
            
            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_running_preferences_user_id ON running_preferences(user_id)");
            
            System.out.println("跑步偏好表创建成功");
        } catch (Exception e) {
            System.err.println("创建跑步偏好表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 