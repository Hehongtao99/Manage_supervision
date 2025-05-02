package com.example.auth.model.vo;

import java.io.Serializable;

/**
 * 通用响应对象
 * @param <T> 响应数据类型
 */
public class ResponseVO<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码，200表示成功
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    // 构造函数
    private ResponseVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // 静态方法：成功响应（带数据）
    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<>(200, "操作成功", data);
    }
    
    // 静态方法：成功响应（不带数据）
    public static <T> ResponseVO<T> success() {
        return new ResponseVO<>(200, "操作成功", null);
    }
    
    // 静态方法：成功响应（自定义消息）
    public static <T> ResponseVO<T> success(String message) {
        return new ResponseVO<>(200, message, null);
    }
    
    // 静态方法：成功响应（自定义消息和数据）
    public static <T> ResponseVO<T> success(String message, T data) {
        return new ResponseVO<>(200, message, data);
    }
    
    // 静态方法：失败响应
    public static <T> ResponseVO<T> error(Integer code, String message) {
        return new ResponseVO<>(code, message, null);
    }
    
    // 静态方法：失败响应（默认错误码500）
    public static <T> ResponseVO<T> error(String message) {
        return new ResponseVO<>(500, message, null);
    }
    
    // Getters and Setters
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
} 