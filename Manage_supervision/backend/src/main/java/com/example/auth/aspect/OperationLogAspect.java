package com.example.auth.aspect;

import com.example.auth.annotation.RequireRole;
import com.example.auth.entity.User;
import com.example.auth.service.OperationLogService;
import com.example.auth.util.JwtUtil;
import com.example.auth.util.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志切面类，用于自动记录操作日志
 */
@Aspect
@Component
public class OperationLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 排除不需要记录日志的请求参数类型
    private static final String[] EXCLUDE_PROPERTIES = {"password", "file", "multipartFile"};
    // 不记录日志的控制器方法
    private static final String[] EXCLUDE_METHODS = {"getSystemLogs", "getOperationLogs", "getOperationLogDetail"};
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 定义切入点：所有控制器方法
     */
    @Pointcut("execution(public * com.example.auth.controller.*.*(..))")
    public void controllerMethod() {}
    
    /**
     * 方法执行后记录日志
     */
    @AfterReturning(value = "controllerMethod()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 检查是否需要排除该方法
            if (isExcludeMethod(joinPoint)) {
                return;
            }
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                logger.warn("RequestContextHolder.getRequestAttributes() 返回 null，无法记录操作日志");
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取当前用户
            User user = getCurrentUser(request);
            if (user == null) {
                logger.warn("当前用户未登录或未找到用户信息，无法记录操作日志");
                return;
            }
            
            // 构建日志信息
            String methodDesc = getMethodDescription(joinPoint);
            String module = getMethodModule(joinPoint);
            String operation = methodDesc != null ? methodDesc : joinPoint.getSignature().getName();
            String method = request.getMethod() + " " + request.getRequestURI();
            String params = getRequestParams(joinPoint, request);
            
            // 记录操作日志
            operationLogService.recordLogAsync(
                    user.getId(),
                    user.getUsername(),
                    operation,
                    method,
                    params,
                    "成功",
                    module,
                    request
            );
        } catch (Exception e) {
            logger.error("记录操作日志失败", e);
        }
    }
    
    /**
     * 方法抛出异常后记录日志
     */
    @AfterThrowing(value = "controllerMethod()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        try {
            // 检查是否需要排除该方法
            if (isExcludeMethod(joinPoint)) {
                return;
            }
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                logger.warn("RequestContextHolder.getRequestAttributes() 返回 null，无法记录操作日志");
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取当前用户
            User user = getCurrentUser(request);
            if (user == null) {
                logger.warn("当前用户未登录或未找到用户信息，无法记录操作日志");
                return;
            }
            
            // 构建日志信息
            String methodDesc = getMethodDescription(joinPoint);
            String module = getMethodModule(joinPoint);
            String operation = methodDesc != null ? methodDesc : joinPoint.getSignature().getName();
            String method = request.getMethod() + " " + request.getRequestURI();
            String params = getRequestParams(joinPoint, request);
            
            // 记录操作日志（失败状态）
            operationLogService.recordLogAsync(
                    user.getId(),
                    user.getUsername(),
                    operation,
                    method,
                    params,
                    "失败：" + e.getMessage(),
                    module,
                    request
            );
        } catch (Exception ex) {
            logger.error("记录操作日志失败", ex);
        }
    }
    
    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        try {
            // 尝试从UserContext获取
            User user = userContext.getCurrentUser();
            if (user != null) {
                return user;
            }
            
            // 如果UserContext中没有，尝试从JWT Token解析
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                return userContext.getUserByUsername(username);
            }
            
            return null;
        } catch (Exception e) {
            logger.warn("获取当前用户信息失败", e);
            return null;
        }
    }
    
    /**
     * 获取方法描述
     */
    private String getMethodDescription(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 尝试从各种注解中获取描述信息
        if (method.isAnnotationPresent(GetMapping.class)) {
            return method.getAnnotation(GetMapping.class).name();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return method.getAnnotation(PostMapping.class).name();
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return method.getAnnotation(PutMapping.class).name();
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return method.getAnnotation(DeleteMapping.class).name();
        }
        
        // 如果注解中没有描述，使用方法名
        return null;
    }
    
    /**
     * 从请求路径获取模块名称
     */
    private String getMethodModule(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        
        // 获取类级别的RequestMapping
        Class<?> targetClass = signature.getDeclaringType();
        if (targetClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping classMapping = targetClass.getAnnotation(RequestMapping.class);
            String[] paths = classMapping.value();
            if (paths.length > 0) {
                String path = paths[0];
                
                // 从路径中提取模块名
                if (path.startsWith("/api/")) {
                    String module = path.replace("/api/", "");
                    // 首字母大写
                    return module.substring(0, 1).toUpperCase() + module.substring(1);
                }
                return path;
            }
        }
        
        // 默认返回类名
        return targetClass.getSimpleName().replace("Controller", "");
    }
    
    /**
     * 获取请求参数字符串
     */
    private String getRequestParams(JoinPoint joinPoint, HttpServletRequest request) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        Map<String, Object> paramsMap = new HashMap<>();
        
        if (paramNames != null && paramNames.length > 0) {
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object value = args[i];
                
                // 排除不需要记录的参数类型
                if (isExcludeParam(paramName) || value == null) {
                    continue;
                }
                
                // 处理参数值
                if (value instanceof HttpServletRequest) {
                    continue;
                }
                
                paramsMap.put(paramName, value);
            }
        }
        
        // 如果是GET请求且参数为空，尝试从请求URL中获取参数
        if (paramsMap.isEmpty() && "GET".equalsIgnoreCase(request.getMethod())) {
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                if (!isExcludeParam(entry.getKey())) {
                    paramsMap.put(entry.getKey(), 
                            entry.getValue().length == 1 ? entry.getValue()[0] : Arrays.toString(entry.getValue()));
                }
            }
        }
        
        try {
            return objectMapper.writeValueAsString(paramsMap);
        } catch (JsonProcessingException e) {
            logger.warn("序列化请求参数失败", e);
            return paramsMap.toString();
        }
    }
    
    /**
     * 判断是否排除记录该参数
     */
    private boolean isExcludeParam(String paramName) {
        for (String excludeProperty : EXCLUDE_PROPERTIES) {
            if (paramName.toLowerCase().contains(excludeProperty.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否排除记录该方法的日志
     */
    private boolean isExcludeMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        
        for (String excludeMethod : EXCLUDE_METHODS) {
            if (methodName.equals(excludeMethod)) {
                return true;
            }
        }
        
        return false;
    }
} 