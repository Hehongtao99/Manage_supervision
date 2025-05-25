package com.example.auth.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "assignTime", LocalDateTime.class, LocalDateTime.now());
        
        // 如果有用户ID上下文，可以在这里设置创建人和更新人
        // this.strictInsertFill(metaObject, "createdBy", Long.class, getCurrentUserId());
        // this.strictInsertFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 如果有用户ID上下文，可以在这里设置更新人
        // this.strictUpdateFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
    }
    
    /**
     * 获取当前用户ID（示例方法，需要根据实际情况实现）
     */
    private Long getCurrentUserId() {
        // 这里可以从SecurityContext或其他地方获取当前用户ID
        // 由于项目不使用安全框架，可以从ThreadLocal或其他方式获取
        return null;
    }
} 