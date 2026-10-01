package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.content.dto.BlogCategoryResponse;
import com.dental.dentalbackend.content.dto.BlogPostDetailResponse;
import com.dental.dentalbackend.content.dto.BlogPostSummaryResponse;
import com.dental.dentalbackend.content.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/blog")
@RequiredArgsConstructor
@Tag(name = "Public Blog", description = "Public-facing blog endpoints")
public class PublicBlogController {

    private final BlogService blogService;

    @GetMapping
    @Operation(summary = "List published blog posts (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<BlogPostSummaryResponse>>> listPublishedPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<BlogPostSummaryResponse> result = blogService.listPublicPosts(categoryId, pageable);

        PagedResponse<BlogPostSummaryResponse> pagedResponse = PagedResponse.<BlogPostSummaryResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get published blog post details by slug")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> getPostBySlug(@PathVariable String slug) {
        BlogPostDetailResponse response = blogService.getPublicPostBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categories")
    @Operation(summary = "List active blog categories")
    public ResponseEntity<ApiResponse<List<BlogCategoryResponse>>> listActiveCategories() {
        List<BlogCategoryResponse> categories = blogService.listCategories(true);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
