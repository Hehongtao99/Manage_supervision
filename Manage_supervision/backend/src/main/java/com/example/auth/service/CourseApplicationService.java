package com.example.auth.service;

import com.example.auth.model.dto.CourseApplicationDTO;
import com.example.auth.model.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 课程申请服务接口
 */
public interface CourseApplicationService {
    
    /**
     * 创建课程申请
     * @param teacherId 教师ID
     * @param title 课程标题
     * @param subject 科目名称
     * @param hourlyPrice 每小时价格
     * @param description 课程描述
     * @param workTimeStart 工作开始时间
     * @param workTimeEnd 工作结束时间
     * @param images 课程图片列表
     * @return 创建的申请ID
     */
    Long createApplication(Long teacherId, 
                           String title,
                           String subject, 
                           String hourlyPrice, 
                           String description, 
                           Integer workTimeStart,
                           Integer workTimeEnd,
                           List<MultipartFile> images);
    
    /**
     * 获取指定教师的所有课程申请
     * @param teacherId 教师ID
     * @return 课程申请列表
     */
    List<CourseApplicationDTO> getApplicationsByTeacher(Long teacherId);
    
    /**
     * 获取所有课程申请
     * @return 所有课程申请
     */
    List<CourseApplicationDTO> getAllApplications();
    
    /**
     * 获取课程申请详情
     * @param applicationId 申请ID
     * @return 课程申请详情
     */
    CourseApplicationDTO getApplicationById(Long applicationId);
    
    /**
     * 管理员审核课程申请
     * @param applicationId 申请ID
     * @param approved 是否通过
     * @param rejectionReason 拒绝理由（如果未通过）
     * @return 是否成功
     */
    boolean reviewApplication(Long applicationId, boolean approved, String rejectionReason);
    
    /**
     * 教师重新提交被拒绝的申请
     * @param applicationId 申请ID
     * @param title 课程标题
     * @param subject 科目名称
     * @param hourlyPrice 每小时价格
     * @param description 课程描述
     * @param workTimeStart 工作开始时间
     * @param workTimeEnd 工作结束时间
     * @param images 课程图片列表
     * @return 是否成功
     */
    boolean resubmitApplication(Long applicationId,
                                String title,
                                String subject,
                                String hourlyPrice,
                                String description,
                                Integer workTimeStart,
                                Integer workTimeEnd,
                                List<MultipartFile> images);
    
    /**
     * 获取所有已审核通过的课程申请
     * @return 已审核通过的课程申请列表
     */
    List<CourseApplicationDTO> getAllApprovedCourseApplications();
    
    /**
     * 获取单个课程申请详情（通过ID）
     * @param courseId 课程ID
     * @return 课程申请详情
     */
    CourseApplicationDTO getCourseApplicationById(Long courseId);

    /**
     * 获取所有已审核通过的课程申请（分页）
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<CourseApplicationDTO> getAllApprovedCourseApplicationsPaged(int page, int size);

    /**
     * 获取所有课程申请（分页）
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<CourseApplicationDTO> getAllApplicationsPaged(int page, int size);
} 