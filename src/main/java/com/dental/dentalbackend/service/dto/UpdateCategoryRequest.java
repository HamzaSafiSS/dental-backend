package com.dental.dentalbackend.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @Size(max = 200)
    private String name;

    private String description;

    @Size(max = 500)
    private String iconUrl;

    private Integer displayOrder;
    private Boolean active;
}
