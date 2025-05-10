package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.CourseApplicationMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.CourseApplicationDTO;
import com.example.auth.model.entity.CourseApplication;
import com.example.auth.model.entity.User;
import com.example.auth.service.CourseApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 课程申请服务实现
 */
@Service
public class CourseApplicationServiceImpl implements CourseApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseApplicationServiceImpl.class);
    
    @Autowired
    private CourseApplicationMapper courseApplicationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${file.upload.path:/uploads}")
    private String uploadPath;
    
    @Value("${app.upload.course-images-dir:course-images}")
    private String courseImagesDir;
    
    // 时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Long createApplication(Long teacherId, String title, String subject, String hourlyPrice, String description, 
                                  Integer workTimeStart, Integer workTimeEnd, List<MultipartFile> images) {
        logger.info("创建课程申请 - 教师ID: {}, 标题: {}, 科目: {}", teacherId, title, subject);
        
        try {
            // 检查教师是否存在
            User teacher = userMapper.selectById(teacherId);
            if (teacher == null) {
                logger.error("教师不存在, ID: {}", teacherId);
                return null;
            }
            
            // 创建新的课程申请
            CourseApplication application = new CourseApplication();
            application.setTeacherId(teacherId);
            application.setCourseId(null); // 初始课程申请时没有关联的课程ID
            application.setTitle(title);
            application.setSubject(subject);
            
            try {
                application.setHourlyPrice(new BigDecimal(hourlyPrice));
            } catch (NumberFormatException e) {
                logger.error("价格格式错误: {}", hourlyPrice, e);
                return null;
            }
            
            application.setDescription(description);
            application.setWorkTimeStart(workTimeStart);
            application.setWorkTimeEnd(workTimeEnd);
            application.setStatus("PENDING");
            application.setCreateTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            
            // 处理图片上传
            if (images != null && !images.isEmpty()) {
                List<String> imagePaths = saveImages(images);
                try {
                    application.setImagePaths(objectMapper.writeValueAsString(imagePaths));
                } catch (JsonProcessingException e) {
                    logger.error("图片路径转JSON失败", e);
                    application.setImagePaths("[]");
                }
            } else {
                application.setImagePaths("[]");
            }
            
            // 保存申请
            courseApplicationMapper.insert(application);
            logger.info("课程申请创建成功, ID: {}", application.getId());
            
            return application.getId();
        } catch (Exception e) {
            logger.error("创建课程申请出错", e);
            return null;
        }
    }
    
    @Override
    public List<CourseApplicationDTO> getApplicationsByTeacher(Long teacherId) {
        logger.info("获取教师的课程申请列表, 教师ID: {}", teacherId);
        
        try {
            List<CourseApplication> applications = courseApplicationMapper.getApplicationsByTeacherId(teacherId);
            return convertToDTOList(applications);
        } catch (Exception e) {
            logger.error("获取教师课程申请出错", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<CourseApplicationDTO> getAllApplications() {
        logger.info("获取所有课程申请列表");
        
        try {
            List<CourseApplication> applications = courseApplicationMapper.getAllApplicationsWithTeacherName();
            return convertToDTOList(applications);
        } catch (Exception e) {
            logger.error("获取所有课程申请出错", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public CourseApplicationDTO getApplicationById(Long applicationId) {
        logger.info("获取课程申请详情, ID: {}", applicationId);
        
        try {
            CourseApplication application = courseApplicationMapper.getApplicationWithTeacherName(applicationId);
            if (application == null) {
                logger.warn("未找到课程申请, ID: {}", applicationId);
                return null;
            }
            
            return convertToDTO(application);
        } catch (Exception e) {
            logger.error("获取课程申请详情出错", e);
            return null;
        }
    }
    
    @Override
    public boolean reviewApplication(Long applicationId, boolean approved, String rejectionReason) {
        logger.info("审核课程申请 - ID: {}, 是否通过: {}", applicationId, approved);
        
        try {
            CourseApplication application = courseApplicationMapper.selectById(applicationId);
            if (application == null) {
                logger.warn("未找到课程申请, ID: {}", applicationId);
                return false;
            }
            
            if (approved) {
                application.setStatus("APPROVED");
                application.setRejectionReason(null);
            } else {
                application.setStatus("REJECTED");
                application.setRejectionReason(rejectionReason);
            }
            
            application.setUpdateTime(LocalDateTime.now());
            courseApplicationMapper.updateById(application);
            logger.info("课程申请审核完成, 状态: {}", application.getStatus());
            
            return true;
        } catch (Exception e) {
            logger.error("审核课程申请出错", e);
            return false;
        }
    }
    
    @Override
    public boolean resubmitApplication(Long applicationId, String title, String subject, String hourlyPrice, 
                                      String description, Integer workTimeStart, Integer workTimeEnd, 
                                      List<MultipartFile> images) {
        logger.info("重新提交课程申请 - ID: {}, 标题: {}", applicationId, title);
        
        try {
            CourseApplication application = courseApplicationMapper.selectById(applicationId);
            if (application == null) {
                logger.warn("未找到课程申请, ID: {}", applicationId);
                return false;
            }
            
            // 更新申请信息
            application.setTitle(title);
            application.setSubject(subject);
            // 保留原有的courseId值，不做修改
            
            try {
                application.setHourlyPrice(new BigDecimal(hourlyPrice));
            } catch (NumberFormatException e) {
                logger.error("价格格式错误: {}", hourlyPrice, e);
                return false;
            }
            
            application.setDescription(description);
            application.setWorkTimeStart(workTimeStart);
            application.setWorkTimeEnd(workTimeEnd);
            application.setStatus("PENDING");
            application.setRejectionReason(null);
            application.setUpdateTime(LocalDateTime.now());
            
            // 处理图片上传
            if (images != null && !images.isEmpty()) {
                List<String> imagePaths = saveImages(images);
                try {
                    application.setImagePaths(objectMapper.writeValueAsString(imagePaths));
                } catch (JsonProcessingException e) {
                    logger.error("图片路径转JSON失败", e);
                    application.setImagePaths("[]");
                }
            }
            // 如果没有上传新图片，保留原有图片
            
            courseApplicationMapper.updateById(application);
            logger.info("课程申请重新提交成功");
            
            return true;
        } catch (Exception e) {
            logger.error("重新提交课程申请出错", e);
            return false;
        }
    }
    
    /**
     * 保存上传的多个图片
     */
    private List<String> saveImages(List<MultipartFile> files) throws IOException {
        List<String> imagePaths = new ArrayList<>();
        
        // 确保主目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 确保课程图片子目录存在
        File courseImagesFolder = new File(uploadDir, courseImagesDir);
        if (!courseImagesFolder.exists()) {
            courseImagesFolder.mkdirs();
        }
        
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + extension;
            
            // 保存文件到课程图片子目录
            Path targetPath = Paths.get(uploadPath, courseImagesDir, filename);
            Files.copy(file.getInputStream(), targetPath);
            
            // 返回相对路径，保持与前端访问一致
            imagePaths.add("/uploads/" + courseImagesDir + "/" + filename);
        }
        
        return imagePaths;
    }
    
    /**
     * 将实体转换为DTO
     */
    private CourseApplicationDTO convertToDTO(CourseApplication application) {
        if (application == null) {
            return null;
        }
        
        CourseApplicationDTO dto = new CourseApplicationDTO();
        dto.setId(application.getId());
        dto.setTeacherId(application.getTeacherId());
        dto.setTeacherName(application.getTeacherName());
        dto.setCourseId(application.getCourseId());
        dto.setTitle(application.getTitle());
        dto.setSubject(application.getSubject());
        dto.setHourlyPrice(application.getHourlyPrice());
        dto.setDescription(application.getDescription());
        
        // 处理图片路径 - 将JSON字符串转换为List
        List<String> imagePaths = new ArrayList<>();
        if (application.getImagePaths() != null && !application.getImagePaths().isEmpty()) {
            try {
                imagePaths = objectMapper.readValue(application.getImagePaths(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                logger.error("解析图片路径JSON失败", e);
            }
        }
        dto.setImagePaths(imagePaths);
        
        dto.setWorkTimeStart(application.getWorkTimeStart());
        dto.setWorkTimeEnd(application.getWorkTimeEnd());
        dto.setStatus(application.getStatus());
        dto.setRejectionReason(application.getRejectionReason());
        
        if (application.getCreateTime() != null) {
            dto.setCreateTime(application.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        
        if (application.getUpdateTime() != null) {
            dto.setUpdateTime(application.getUpdateTime().format(DATE_TIME_FORMATTER));
        }
        
        // 获取教师用户信息，包含简历相关字段
        User teacher = userMapper.selectById(application.getTeacherId());
        if (teacher != null) {
            dto.setTeacherBio(teacher.getBio());
            dto.setTeacherGraduationSchool(teacher.getGraduationSchool());
            dto.setTeacherTeachingSubjects(teacher.getTeachingSubjects());
        }
        
        return dto;
    }
    
    private List<CourseApplicationDTO> convertToDTOList(List<CourseApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<CourseApplicationDTO> dtoList = new ArrayList<>(applications.size());
        for (CourseApplication application : applications) {
            CourseApplicationDTO dto = convertToDTO(application);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        
        return dtoList;
    }
    
    @Override
    public List<CourseApplicationDTO> getAllApprovedCourseApplications() {
        logger.info("获取所有已审核通过的课程申请");
        try {
            // 使用mapper自定义方法查询已审核通过的课程，并包含教师名称
            List<CourseApplication> applications = courseApplicationMapper.getAllApprovedApplicationsWithTeacherName();
            
            // 如果上面的方法不存在，可以使用以下替代方案
            if (applications == null || applications.isEmpty()) {
                LambdaQueryWrapper<CourseApplication> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(CourseApplication::getStatus, "APPROVED");
                
                applications = courseApplicationMapper.selectList(queryWrapper);
                
                // 手动获取教师信息
                for (CourseApplication app : applications) {
                    User teacher = userMapper.selectById(app.getTeacherId());
                    if (teacher != null) {
                        app.setTeacherName(teacher.getUsername());
                    }
                }
            }
            
            return convertToDTOList(applications);
        } catch (Exception e) {
            logger.error("获取已审核通过的课程申请出错", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public CourseApplicationDTO getCourseApplicationById(Long courseId) {
        logger.info("获取课程申请详情: {}", courseId);
        try {
            CourseApplication application = courseApplicationMapper.selectById(courseId);
            if (application == null) {
                logger.warn("未找到课程申请, ID: {}", courseId);
                return null;
            }
            
            return convertToDTO(application);
        } catch (Exception e) {
            logger.error("获取课程申请详情出错", e);
            return null;
        }
    }
} 