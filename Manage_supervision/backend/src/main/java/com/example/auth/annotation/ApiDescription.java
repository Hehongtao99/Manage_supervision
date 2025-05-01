package com.example.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API描述注解，用于标注API的描述信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDescription {
    /**
     * API描述
     */
    String value() default "";
    
    /**
     * 所需角色，多个角色用逗号分隔
     */
    String requiredRoles() default "";
} 