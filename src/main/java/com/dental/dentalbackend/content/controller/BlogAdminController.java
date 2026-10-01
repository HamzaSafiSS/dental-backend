package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.content.dto.*;
import com.dental.dentalbackend.content.entity.BlogPostStatus;
import com.dental.dentalbackend.content.service.BlogService;
import com.dental.dentalbackend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Blog Management", description = "Admin manages blog posts and categories")
public class BlogAdminController {

    private final BlogService blogService;

    // ── Posts ─────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/blog")
    @Operation(summary = "List all blog posts (including drafts and archived)")
    public ResponseEntity<ApiResponse<PagedResponse<BlogPostSummaryResponse>>> listAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BlogPostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BlogPostSummaryResponse> result = blogService.listAllPosts(categoryId, status, pageable);

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

    @GetMapping("/api/v1/blog/{id}")
    @Operation(summary = "Get blog post by ID")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> getPostById(@PathVariable UUID id) {
        BlogPostDetailResponse response = blogService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/blog")
    @Operation(summary = "Create a new blog post")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> createPost(
            @Valid @RequestBody CreateBlogPostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BlogPostDetailResponse response = blogService.createPost(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog post created successfully", response));
    }

    @PutMapping("/api/v1/blog/{id}")
    @Operation(summary = "Update a blog post")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBlogPostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BlogPostDetailResponse response = blogService.updatePost(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Blog post updated successfully", response));
    }

    @PatchMapping("/api/v1/blog/{id}")
    @Operation(summary = "Change blog post status (e.g. DRAFT to PUBLISHED)")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BlogPostStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BlogPostDetailResponse response = blogService.changePostStatus(
                id, request.getStatus(), userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Blog post status updated successfully", response));
    }

    @DeleteMapping("/api/v1/blog/{id}")
    @Operation(summary = "Delete a blog post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        blogService.deletePost(id, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Blog post deleted successfully", null));
    }

    // ── Categories ────────────────────────────────────────────────────────

    @GetMapping("/api/v1/blog-categories")
    @Operation(summary = "List all blog categories")
    public ResponseEntity<ApiResponse<List<BlogCategoryResponse>>> listCategories() {
        List<BlogCategoryResponse> categories = blogService.listCategories(false);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/api/v1/blog-categories")
    @Operation(summary = "Create a blog category")
    public ResponseEntity<ApiResponse<BlogCategoryResponse>> createCategory(
            @Valid @RequestBody BlogCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BlogCategoryResponse response = blogService.createCategory(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog category created successfully", response));
    }

    @PutMapping("/api/v1/blog-categories/{id}")
    @Operation(summary = "Update a blog category")
    public ResponseEntity<ApiResponse<BlogCategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody BlogCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BlogCategoryResponse response = blogService.updateCategory(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Blog category updated successfully", response));
    }
}
