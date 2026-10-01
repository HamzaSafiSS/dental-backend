package com.dental.dentalbackend.content.dto;

import com.dental.dentalbackend.content.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerateReviewRequest {

    @NotNull(message = "Review status is required")
    private ReviewStatus status;

    private String adminResponse;

    private Boolean featured;
}
