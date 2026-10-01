package com.dental.dentalbackend.content.repository;

import com.dental.dentalbackend.content.entity.BlogPost;
import com.dental.dentalbackend.content.entity.BlogPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    Optional<BlogPost> findBySlug(String slug);

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    boolean existsBySlug(String slug);

    Page<BlogPost> findByStatus(BlogPostStatus status, Pageable pageable);

    Page<BlogPost> findByCategoryIdAndStatus(UUID categoryId, BlogPostStatus status, Pageable pageable);

    Page<BlogPost> findByCategoryId(UUID categoryId, Pageable pageable);

    @Modifying
    @Query("UPDATE BlogPost b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") UUID id);
}
