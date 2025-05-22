package com.example.auth.config;

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
    
    @Value("${file.upload.path:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("配置拦截器，验证用户权限");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置CORS，允许前端访问");
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源，简化开发
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // 预检请求的有效期，单位秒

        logger.info("CORS配置完成，允许所有来源访问");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传目录的绝对路径
        String uploadPath = new File(uploadDir).getAbsolutePath();
        
        // 配置静态资源映射，将 /uploads/** 映射到实际的文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + File.separator);
                
        logger.info("配置资源映射: /uploads/** -> file:{}{}", uploadPath, File.separator);
    }
} 