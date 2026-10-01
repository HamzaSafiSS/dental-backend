package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.content.dto.BlogPostDetailResponse;
import com.dental.dentalbackend.content.dto.CreateBlogPostRequest;
import com.dental.dentalbackend.content.entity.BlogPost;
import com.dental.dentalbackend.content.entity.BlogPostStatus;
import com.dental.dentalbackend.content.repository.BlogCategoryRepository;
import com.dental.dentalbackend.content.repository.BlogPostRepository;
import com.dental.dentalbackend.user.entity.Role;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogCategoryRepository categoryRepository;

    @Mock
    private BlogPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private BlogService blogService;

    @Test
    @DisplayName("Create post auto-generates slug from title if slug not provided")
    void createPostAutoGeneratesSlug() {
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
                .title("10 Tips for Healthy Teeth!")
                .content("Detailed content about dental hygiene...")
                .status(BlogPostStatus.DRAFT)
                .build();

        UUID authorId = UUID.randomUUID();
        User author = new User();
        author.setId(authorId);
        author.setFirstName("Dr. Sarah");
        author.setLastName("Connor");
        author.setRole(Role.DOCTOR);

        when(postRepository.existsBySlug(any())).thenReturn(false);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(postRepository.save(any(BlogPost.class))).thenAnswer(invocation -> {
            BlogPost post = invocation.getArgument(0);
            post.setId(UUID.randomUUID());
            return post;
        });

        BlogPostDetailResponse response = blogService.createPost(request, authorId, httpRequest);

        assertNotNull(response);
        assertEquals("10-tips-for-healthy-teeth", response.getSlug());
        assertEquals(BlogPostStatus.DRAFT, response.getStatus());
        verify(postRepository).save(any(BlogPost.class));
    }

    @Test
    @DisplayName("Create post with PUBLISHED status automatically sets publishedAt")
    void createPublishedPostSetsTimestamp() {
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
                .title("New Treatment Available")
                .content("Announcing our new dental implant services...")
                .status(BlogPostStatus.PUBLISHED)
                .build();

        UUID authorId = UUID.randomUUID();
        when(postRepository.existsBySlug(any())).thenReturn(false);
        when(postRepository.save(any(BlogPost.class))).thenAnswer(invocation -> {
            BlogPost post = invocation.getArgument(0);
            post.setId(UUID.randomUUID());
            return post;
        });

        BlogPostDetailResponse response = blogService.createPost(request, authorId, httpRequest);

        assertNotNull(response);
        assertEquals(BlogPostStatus.PUBLISHED, response.getStatus());
        assertNotNull(response.getPublishedAt());
    }
}
