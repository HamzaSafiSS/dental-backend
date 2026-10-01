package com.dental.dentalbackend.content.dto;

import com.dental.dentalbackend.content.entity.BlogPostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogPostRequest {

    private UUID categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Size(max = 500, message = "Slug must not exceed 500 characters")
    private String slug;

    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500, message = "Featured image URL must not exceed 500 characters")
    private String featuredImageUrl;

    private BlogPostStatus status;

    @Size(max = 200, message = "Meta title must not exceed 200 characters")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;
}
