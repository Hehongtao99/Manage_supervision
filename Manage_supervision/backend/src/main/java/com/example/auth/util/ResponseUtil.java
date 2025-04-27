package com.example.auth.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应工具类，统一处理接口返回格式
 */
public class ResponseUtil {
    
    /**
     * 成功响应
     * @param data 响应数据
     * @return 响应结果
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "操作成功");
        response.put("data", data);
        return response;
    }
    
    /**
     * 成功响应（无数据）
     * @return 响应结果
     */
    public static Map<String, Object> success() {
        return success(null);
    }
    
    /**
     * 成功响应（自定义消息）
     * @param message 成功消息
     * @return 响应结果
     */
    public static Map<String, Object> successWithMessage(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
    
    /**
     * 失败响应
     * @param message 错误消息
     * @return 响应结果
     */
    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
    
    /**
     * 失败响应（自定义错误码）
     * @param code 错误码
     * @param message 错误消息
     * @return 响应结果
     */
    public static Map<String, Object> error(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
    
    /**
     * 未授权响应
     * @return 响应结果
     */
    public static Map<String, Object> unauthorized() {
        return error(401, "未授权或授权已过期");
    }
    
    /**
     * 禁止访问响应
     * @return 响应结果
     */
    public static Map<String, Object> forbidden() {
        return error(403, "无权限执行此操作");
    }
} 