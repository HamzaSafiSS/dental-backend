package com.dental.dentalbackend.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String slug;

    private String description;

    @Size(max = 500)
    private String iconUrl;

    private Integer displayOrder;
}
