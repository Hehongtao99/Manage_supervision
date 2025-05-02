package com.example.auth.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    
    private int code;
    private String message;

    /**
     * 默认业务异常
     */
    public BusinessException() {
        this.code = 400;
        this.message = "业务异常";
    }

    /**
     * 自定义消息的业务异常
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        this.code = 400;
        this.message = message;
    }

    /**
     * 自定义消息和错误码的业务异常
     *
     * @param code    错误码
     * @param message 异常消息
     */
    public BusinessException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取异常消息
     *
     * @return 异常消息
     */
    @Override
    public String getMessage() {
        return message;
    }
} 