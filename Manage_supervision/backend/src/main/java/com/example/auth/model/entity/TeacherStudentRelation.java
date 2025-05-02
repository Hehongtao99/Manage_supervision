package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("teacher_student_relations")
public class TeacherStudentRelation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("student_id")
    private Long studentId;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("status")
    private String status;

    @TableField(exist = false)
    private User teacher;

    @TableField(exist = false)
    private User student;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public User getTeacher() {
        return teacher;
    }

    public User getStudent() {
        return student;
    }

    public LocalDateTime getAssignTime() {
        return assignTime;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
        if (teacher != null) {
            this.teacherId = teacher.getId();
        }
    }

    public void setStudent(User student) {
        this.student = student;
        if (student != null) {
            this.studentId = student.getId();
        }
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 