package com.example.auth.interceptor;

import com.example.auth.entity.ApiEndpoint;
import com.example.auth.repository.ApiEndpointRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ApiEndpointInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiEndpointInterceptor.class);
    
    @Autowired
    private ApiEndpointRepository apiEndpointRepository;
    
    private ThreadLocal<Long> startTime = new ThreadLocal<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        startTime.set(System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 不做任何处理
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                String controllerName = handlerMethod.getBeanType().getSimpleName();
                String methodName = handlerMethod.getMethod().getName();
                String path = request.getRequestURI();
                String method = request.getMethod();
                
                // 只处理API请求
                if (path != null && path.startsWith("/api/")) {
                    Long startTimeValue = startTime.get();
                    if (startTimeValue != null) {
                        long executionTime = System.currentTimeMillis() - startTimeValue;
                        updateApiEndpoint(path, method, controllerName, methodName, executionTime);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理API端点统计时发生错误", e);
        } finally {
            startTime.remove(); // 确保在任何情况下都移除ThreadLocal
        }
    }
    
    private void updateApiEndpoint(String path, String method, String controllerName, String methodName, long executionTime) {
        try {
            Optional<ApiEndpoint> existingEndpoint = apiEndpointRepository.findByPathAndMethod(path, method);
            
            if (existingEndpoint.isPresent()) {
                ApiEndpoint endpoint = existingEndpoint.get();
                endpoint.setLastAccessed(LocalDateTime.now());
                endpoint.setAccessCount(endpoint.getAccessCount() + 1);
                
                // 更新平均响应时间
                long totalTime = endpoint.getAverageResponseTime() * (endpoint.getAccessCount() - 1) + executionTime;
                endpoint.setAverageResponseTime(totalTime / endpoint.getAccessCount());
                
                apiEndpointRepository.save(endpoint);
            } else {
                // 新增API端点记录
                ApiEndpoint newEndpoint = new ApiEndpoint();
                newEndpoint.setPath(path);
                newEndpoint.setMethod(method);
                newEndpoint.setControllerName(controllerName);
                newEndpoint.setMethodName(methodName);
                newEndpoint.setLastAccessed(LocalDateTime.now());
                newEndpoint.setAccessCount(1L);
                newEndpoint.setAverageResponseTime(executionTime);
                
                apiEndpointRepository.save(newEndpoint);
                logger.info("新增API端点记录: {} {}", method, path);
            }
        } catch (Exception e) {
            logger.error("更新API端点记录失败", e);
        }
    }
} 