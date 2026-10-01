package com.dental.dentalbackend.content.dto;

import com.dental.dentalbackend.content.entity.BlogPostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostStatusRequest {

    @NotNull(message = "Status is required")
    private BlogPostStatus status;
}
