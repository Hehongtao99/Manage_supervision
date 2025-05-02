package com.example.auth.model.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 学生详细信息的数据传输对象，包含课程和活动信息
 */
public class StudentDetailDTO extends StudentDTO implements Serializable {
    private List<CourseDTO> courses; // 学生课程列表
    private List<ActivityDTO> activities; // 学生活动列表

    public StudentDetailDTO() {
        super();
    }

    // 主要构造函数
    public StudentDetailDTO(Long id, String name, String studentId, String className,
                          String email, String phone, String status, String lastLogin, int progress,
                          List<CourseDTO> courses, List<ActivityDTO> activities) {
        super(id, name, studentId, className, email, phone, status, lastLogin, progress);
        this.courses = courses;
        this.activities = activities;
    }

    // 从StudentDTO创建StudentDetailDTO
    public static StudentDetailDTO fromStudentDTO(StudentDTO studentDTO, List<CourseDTO> courses, List<ActivityDTO> activities) {
        return new StudentDetailDTO(
            studentDTO.getId(),
            studentDTO.getName(),
            studentDTO.getStudentId(),
            studentDTO.getClassName(),
            studentDTO.getEmail(),
            studentDTO.getPhone(),
            studentDTO.getStatus(),
            studentDTO.getLastLogin(),
            studentDTO.getProgress(),
            courses,
            activities
        );
    }

    // Getters and Setters
    public List<CourseDTO> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseDTO> courses) {
        this.courses = courses;
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDTO> activities) {
        this.activities = activities;
    }
} 