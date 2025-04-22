package com.example.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudentAssignmentDTO {
    private Long teacherId;
    private List<Long> studentIds;
} 