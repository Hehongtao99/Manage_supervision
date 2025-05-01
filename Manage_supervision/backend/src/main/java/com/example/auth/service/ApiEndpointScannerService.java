package com.example.auth.service;

import com.example.auth.annotation.ApiDescription;
import com.example.auth.entity.ApiEndpoint;
import com.example.auth.repository.ApiEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiEndpointScannerService implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiEndpointScannerService.class);
    
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    
    @Autowired
    private ApiEndpointRepository apiEndpointRepository;
    
    @Value("${spring.application.name:API}")
    private String applicationName;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("开始扫描API接口...");
        scanApiEndpoints();
    }
    
    public void scanApiEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            
            // 获取控制器和方法信息
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            Method method = handlerMethod.getMethod();
            
            // 获取HTTP方法和路径
            Set<RequestMethod> requestMethods = mappingInfo.getMethodsCondition().getMethods();
            
            // 获取路径模式 - 使用兼容不同Spring Boot版本的方式
            Set<String> paths = new HashSet<>();
            
            try {
                // 首先尝试使用新API获取路径（Spring Boot 2.6+）
                paths = mappingInfo.getPatternValues();
                
                // 如果获取的路径为空，尝试使用旧API（向后兼容）
                if (paths.isEmpty() && mappingInfo.getPatternsCondition() != null) {
                    paths = mappingInfo.getPatternsCondition().getPatterns();
                }
                
                // 如果仍然为空，尝试使用反射获取路径
                if (paths.isEmpty()) {
                    logger.warn("无法通过标准API获取路径，尝试使用反射: {}.{}", controllerName, methodName);
                    
                    // 检查类上的RequestMapping注解
                    RequestMapping classMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
                    String[] classPaths = classMapping != null ? classMapping.value() : new String[]{""};
                    
                    // 检查方法上的各种Mapping注解
                    String[] methodPaths = new String[]{""};
                    
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        methodPaths = method.getAnnotation(RequestMapping.class).value();
                    } else if (method.isAnnotationPresent(GetMapping.class)) {
                        methodPaths = method.getAnnotation(GetMapping.class).value();
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        methodPaths = method.getAnnotation(PostMapping.class).value();
                    } else if (method.isAnnotationPresent(PutMapping.class)) {
                        methodPaths = method.getAnnotation(PutMapping.class).value();
                    } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                        methodPaths = method.getAnnotation(DeleteMapping.class).value();
                    } else if (method.isAnnotationPresent(PatchMapping.class)) {
                        methodPaths = method.getAnnotation(PatchMapping.class).value();
                    }
                    
                    // 组合类路径和方法路径
                    for (String classPath : classPaths) {
                        for (String methodPath : methodPaths) {
                            paths.add(combinePath(classPath, methodPath));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("获取API路径时出错: {}", e.getMessage());
                continue; // 跳过当前处理，继续下一个
            }
            
            // 如果没有指定HTTP方法，默认为GET
            List<String> httpMethods = requestMethods.isEmpty() 
                ? List.of("GET") 
                : requestMethods.stream().map(RequestMethod::name).collect(Collectors.toList());
            
            // 获取API描述
            String description = "";
            String requiredRoles = "";
            ApiDescription apiDescriptionAnnotation = method.getAnnotation(ApiDescription.class);
            if (apiDescriptionAnnotation != null) {
                description = apiDescriptionAnnotation.value();
                requiredRoles = apiDescriptionAnnotation.requiredRoles();
            }
            
            // 获取参数信息
            String requestParams = Arrays.stream(method.getParameters())
                    .map(param -> param.getType().getSimpleName() + " " + param.getName())
                    .collect(Collectors.joining(", "));
            
            // 对每个路径和HTTP方法组合创建或更新API记录
            for (String path : paths) {
                if (path.isEmpty() || !path.startsWith("/api/")) {
                    continue; // 只处理API路径
                }
                
                for (String httpMethod : httpMethods) {
                    Optional<ApiEndpoint> existingEndpoint = apiEndpointRepository.findByPathAndMethod(path, httpMethod);
                    
                    if (existingEndpoint.isPresent()) {
                        // 更新现有记录
                        ApiEndpoint endpoint = existingEndpoint.get();
                        endpoint.setControllerName(controllerName);
                        endpoint.setMethodName(methodName);
                        endpoint.setDescription(description);
                        endpoint.setRequiredRoles(requiredRoles);
                        endpoint.setRequestParams(requestParams);
                        
                        apiEndpointRepository.save(endpoint);
                    } else {
                        // 创建新记录
                        ApiEndpoint newEndpoint = new ApiEndpoint();
                        newEndpoint.setPath(path);
                        newEndpoint.setMethod(httpMethod);
                        newEndpoint.setControllerName(controllerName);
                        newEndpoint.setMethodName(methodName);
                        newEndpoint.setDescription(description);
                        newEndpoint.setRequiredRoles(requiredRoles);
                        newEndpoint.setRequestParams(requestParams);
                        
                        apiEndpointRepository.save(newEndpoint);
                        logger.info("发现新API: {} {} 在 {}.{}", httpMethod, path, controllerName, methodName);
                    }
                }
            }
        }
        
        logger.info("API接口扫描完成，共发现 {} 个接口", apiEndpointRepository.count());
    }
    
    // 组合类路径和方法路径
    private String combinePath(String classPath, String methodPath) {
        if (classPath.isEmpty()) return methodPath;
        if (methodPath.isEmpty()) return classPath;
        
        boolean classEndsWithSlash = classPath.endsWith("/");
        boolean methodStartsWithSlash = methodPath.startsWith("/");
        
        if (classEndsWithSlash && methodStartsWithSlash) {
            return classPath + methodPath.substring(1);
        } else if (!classEndsWithSlash && !methodStartsWithSlash) {
            return classPath + "/" + methodPath;
        } else {
            return classPath + methodPath;
        }
    }
} 