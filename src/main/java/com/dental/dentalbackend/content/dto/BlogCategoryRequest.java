package com.dental.dentalbackend.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 200, message = "Slug must not exceed 200 characters")
    private String slug;

    private String description;

    private Integer displayOrder;

    private Boolean active;
}
