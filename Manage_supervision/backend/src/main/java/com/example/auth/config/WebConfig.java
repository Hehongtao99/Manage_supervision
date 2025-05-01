package com.example.auth.config;

import com.example.auth.interceptor.ApiEndpointInterceptor;
import com.example.auth.interceptor.AuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private ApiEndpointInterceptor apiEndpointInterceptor;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("配置拦截器，验证用户权限");
        // 注册API拦截器，用于收集API访问信息
        registry.addInterceptor(apiEndpointInterceptor)
                .addPathPatterns("/api/**");
                
        // 注册认证拦截器，用于验证用户权限
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置CORS，允许前端访问");
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:8080", "http://127.0.0.1:8080")  // 明确指定允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // 预检请求的有效期，单位秒

        logger.info("CORS配置完成，允许指定来源访问");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传目录的绝对路径
        String uploadPath = new File(uploadDir).getAbsolutePath();
        
        // 配置静态资源映射，将 /uploads/** 映射到实际的文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
                
        logger.info("配置资源映射: /uploads/** -> file:{}/", uploadPath);
    }
} 