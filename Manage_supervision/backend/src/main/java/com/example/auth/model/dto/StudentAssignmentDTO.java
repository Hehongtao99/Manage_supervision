package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudentAssignmentDTO {
    private Long teacherId;
    private List<Long> studentIds;
} 