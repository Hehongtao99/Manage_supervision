package com.example.auth.common;

import lombok.Data; // 引入 Lombok 以简化代码

@Data // 使用 Lombok 生成 Getters, Setters, toString, equals, hashCode
public class Result {
    private int code;
    private String message;
    private Object data;

    public Result() {
    }

    public Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法，方便创建成功结果
    public static Result success(Object data) {
        return new Result(200, "Success", data); // 通常成功代码为 200
    }

    // 静态工厂方法，创建不带数据的成功结果
    public static Result success() {
        return new Result(200, "Success", null);
    }

    // 静态工厂方法，方便创建错误结果
    public static Result error(int code, String message) {
        return new Result(code, message, null);
    }

    // 静态工厂方法，创建默认错误码 (例如 500) 的错误结果
    public static Result error(String message) {
        return new Result(500, message, null); // 假设默认错误代码为 500
    }
} 