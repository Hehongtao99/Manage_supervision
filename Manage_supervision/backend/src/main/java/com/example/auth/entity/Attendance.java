package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendances")
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class studentClass;
    
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;
    
    @Column(name = "face_recognized")
    private boolean faceRecognized;
    
    @Column(name = "recognition_details", length = 500)
    private String recognitionDetails;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }
} 