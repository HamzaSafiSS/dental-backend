package com.dental.dentalbackend.content.dto;

import com.dental.dentalbackend.content.entity.BlogPostStatus;
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
public class BlogPostSummaryResponse {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String categorySlug;
    private UUID authorId;
    private String authorName;
    private String title;
    private String slug;
    private String summary;
    private String featuredImageUrl;
    private BlogPostStatus status;
    private LocalDateTime publishedAt;
    private String tags;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
