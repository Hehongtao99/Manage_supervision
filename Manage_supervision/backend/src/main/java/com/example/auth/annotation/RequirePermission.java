package com.example.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解，用于控制接口访问权限
 * 通过权限编码进行验证
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * 权限编码数组
     * 当有多个权限时，只需要满足其中一个即可访问
     */
    String[] value() default {};
    
    /**
     * 是否需要所有权限，默认为false
     * 为true时，需要满足所有指定的权限才能访问
     */
    boolean allMatch() default false;
} 