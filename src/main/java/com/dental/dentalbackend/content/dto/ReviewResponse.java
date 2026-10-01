package com.dental.dentalbackend.content.dto;

import com.dental.dentalbackend.content.entity.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID patientId;
    private String patientName;
    private int rating;
    private String comment;
    private ReviewStatus status;
    private String adminResponse;
    private boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
