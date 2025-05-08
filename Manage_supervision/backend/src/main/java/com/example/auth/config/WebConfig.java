package com.example.auth.config;

import com.example.auth.interceptor.AuthInterceptor;
import jakarta.annotation.PostConstruct;
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
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.course-images-dir:course-images}")
    private String courseImagesDir;
    
    /**
     * 应用启动时初始化上传目录
     */
    @PostConstruct
    public void init() {
        // 创建上传根目录
        File uploadRoot = new File(uploadDir);
        if (!uploadRoot.exists()) {
            boolean created = uploadRoot.mkdirs();
            if (created) {
                logger.info("成功创建上传根目录: {}", uploadRoot.getAbsolutePath());
            } else {
                logger.warn("无法创建上传根目录: {}", uploadRoot.getAbsolutePath());
            }
        }
        
        // 创建课程图片子目录
        File courseImagesPath = new File(uploadRoot, courseImagesDir);
        if (!courseImagesPath.exists()) {
            boolean created = courseImagesPath.mkdirs();
            if (created) {
                logger.info("成功创建课程图片目录: {}", courseImagesPath.getAbsolutePath());
            } else {
                logger.warn("无法创建课程图片目录: {}", courseImagesPath.getAbsolutePath());
            }
        }
        
        logger.info("上传目录初始化完成，根目录: {}", uploadRoot.getAbsolutePath());
    }

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
        
        // 确保路径以文件分隔符结尾
        if (!uploadPath.endsWith(File.separator)) {
            uploadPath = uploadPath + File.separator;
        }
        
        // 配置静态资源映射，将 /uploads/** 映射到实际的文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
                
        logger.info("配置资源映射: /uploads/** -> file:{}", uploadPath);
    }
} 