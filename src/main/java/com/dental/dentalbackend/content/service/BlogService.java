package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.content.dto.*;
import com.dental.dentalbackend.content.entity.BlogCategory;
import com.dental.dentalbackend.content.entity.BlogPost;
import com.dental.dentalbackend.content.entity.BlogPostStatus;
import com.dental.dentalbackend.content.repository.BlogCategoryRepository;
import com.dental.dentalbackend.content.repository.BlogPostRepository;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogCategoryRepository categoryRepository;
    private final BlogPostRepository postRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ── Categories ────────────────────────────────────────────────────────

    @Transactional
    public BlogCategoryResponse createCategory(BlogCategoryRequest request, UUID adminId, HttpServletRequest httpRequest) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("BlogCategory", "name", request.getName());
        }

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? generateSlug(request.getSlug())
                : generateSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("BlogCategory", "slug", slug);
        }

        BlogCategory category = new BlogCategory();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setActive(request.getActive() != null ? request.getActive() : true);

        category = categoryRepository.save(category);

        auditService.log(adminId, "BLOG_CATEGORY_CREATED", "BlogCategory", category.getId(),
                "Created blog category: " + category.getName(), httpRequest);

        return mapCategoryResponse(category);
    }

    @Transactional
    public BlogCategoryResponse updateCategory(UUID id, BlogCategoryRequest request, UUID adminId, HttpServletRequest httpRequest) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogCategory", id));

        if (!category.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("BlogCategory", "name", request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = generateSlug(request.getSlug());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new DuplicateResourceException("BlogCategory", "slug", newSlug);
            }
            category.setSlug(newSlug);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        category = categoryRepository.save(category);

        auditService.log(adminId, "BLOG_CATEGORY_UPDATED", "BlogCategory", category.getId(),
                "Updated blog category: " + category.getName(), httpRequest);

        return mapCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<BlogCategoryResponse> listCategories(boolean onlyActive) {
        List<BlogCategory> categories = onlyActive
                ? categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
                : categoryRepository.findAllByOrderByDisplayOrderAsc();

        return categories.stream().map(this::mapCategoryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlogCategoryResponse getCategoryById(UUID id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogCategory", id));
        return mapCategoryResponse(category);
    }

    // ── Posts ─────────────────────────────────────────────────────────────

    @Transactional
    public BlogPostDetailResponse createPost(CreateBlogPostRequest request, UUID authorId, HttpServletRequest httpRequest) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? generateSlug(request.getSlug())
                : generateSlug(request.getTitle());

        if (postRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("BlogPost", "slug", slug);
        }

        BlogPost post = new BlogPost();
        post.setTitle(request.getTitle());
        post.setSlug(slug);
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setFeaturedImageUrl(request.getFeaturedImageUrl());
        post.setMetaTitle(request.getMetaTitle());
        post.setMetaDescription(request.getMetaDescription());
        post.setTags(request.getTags());
        post.setViewCount(0);

        BlogPostStatus status = request.getStatus() != null ? request.getStatus() : BlogPostStatus.DRAFT;
        post.setStatus(status);
        if (status == BlogPostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }

        if (request.getCategoryId() != null) {
            BlogCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("BlogCategory", request.getCategoryId()));
            post.setCategory(category);
        }

        if (authorId != null) {
            User author = userRepository.findById(authorId).orElse(null);
            post.setAuthor(author);
        }

        post = postRepository.save(post);

        auditService.log(authorId, "BLOG_POST_CREATED", "BlogPost", post.getId(),
                "Created blog post: " + post.getTitle(), httpRequest);

        return mapDetailResponse(post);
    }

    @Transactional
    public BlogPostDetailResponse updatePost(UUID id, UpdateBlogPostRequest request, UUID adminId, HttpServletRequest httpRequest) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = generateSlug(request.getSlug());
            if (!newSlug.equals(post.getSlug()) && postRepository.existsBySlug(newSlug)) {
                throw new DuplicateResourceException("BlogPost", "slug", newSlug);
            }
            post.setSlug(newSlug);
        }

        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setFeaturedImageUrl(request.getFeaturedImageUrl());
        post.setMetaTitle(request.getMetaTitle());
        post.setMetaDescription(request.getMetaDescription());
        post.setTags(request.getTags());

        if (request.getStatus() != null) {
            if (request.getStatus() == BlogPostStatus.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
            post.setStatus(request.getStatus());
        }

        if (request.getCategoryId() != null) {
            BlogCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("BlogCategory", request.getCategoryId()));
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        post = postRepository.save(post);

        auditService.log(adminId, "BLOG_POST_UPDATED", "BlogPost", post.getId(),
                "Updated blog post: " + post.getTitle(), httpRequest);

        return mapDetailResponse(post);
    }

    @Transactional
    public BlogPostDetailResponse changePostStatus(UUID id, BlogPostStatus newStatus, UUID adminId, HttpServletRequest httpRequest) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));

        if (newStatus == BlogPostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        post.setStatus(newStatus);
        post = postRepository.save(post);

        auditService.log(adminId, "BLOG_POST_STATUS_CHANGED", "BlogPost", post.getId(),
                "Changed status of post " + post.getTitle() + " to " + newStatus, httpRequest);

        return mapDetailResponse(post);
    }

    @Transactional
    public void deletePost(UUID id, UUID adminId, HttpServletRequest httpRequest) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));

        postRepository.delete(post);

        auditService.log(adminId, "BLOG_POST_DELETED", "BlogPost", id,
                "Deleted blog post: " + post.getTitle(), httpRequest);
    }

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> listPublicPosts(UUID categoryId, Pageable pageable) {
        Page<BlogPost> page = categoryId != null
                ? postRepository.findByCategoryIdAndStatus(categoryId, BlogPostStatus.PUBLISHED, pageable)
                : postRepository.findByStatus(BlogPostStatus.PUBLISHED, pageable);

        return page.map(this::mapSummaryResponse);
    }

    @Transactional
    public BlogPostDetailResponse getPublicPostBySlug(String slug) {
        BlogPost post = postRepository.findBySlugAndStatus(slug, BlogPostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", "slug", slug));

        postRepository.incrementViewCount(post.getId());
        post.setViewCount(post.getViewCount() + 1);

        return mapDetailResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> listAllPosts(UUID categoryId, BlogPostStatus status, Pageable pageable) {
        Page<BlogPost> page;
        if (categoryId != null && status != null) {
            page = postRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        } else if (categoryId != null) {
            page = postRepository.findByCategoryId(categoryId, pageable);
        } else if (status != null) {
            page = postRepository.findByStatus(status, pageable);
        } else {
            page = postRepository.findAll(pageable);
        }

        return page.map(this::mapSummaryResponse);
    }

    @Transactional(readOnly = true)
    public BlogPostDetailResponse getPostById(UUID id) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));
        return mapDetailResponse(post);
    }

    // ── Mapping Helpers ───────────────────────────────────────────────────

    private String generateSlug(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private BlogCategoryResponse mapCategoryResponse(BlogCategory category) {
        return BlogCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private BlogPostSummaryResponse mapSummaryResponse(BlogPost post) {
        return BlogPostSummaryResponse.builder()
                .id(post.getId())
                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null)
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .categorySlug(post.getCategory() != null ? post.getCategory().getSlug() : null)
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorName(post.getAuthor() != null ? post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName() : null)
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .featuredImageUrl(post.getFeaturedImageUrl())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private BlogPostDetailResponse mapDetailResponse(BlogPost post) {
        return BlogPostDetailResponse.builder()
                .id(post.getId())
                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null)
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .categorySlug(post.getCategory() != null ? post.getCategory().getSlug() : null)
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorName(post.getAuthor() != null ? post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName() : null)
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .featuredImageUrl(post.getFeaturedImageUrl())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .metaTitle(post.getMetaTitle())
                .metaDescription(post.getMetaDescription())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
