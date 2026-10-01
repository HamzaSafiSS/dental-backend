package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.content.dto.ModerateReviewRequest;
import com.dental.dentalbackend.content.dto.ReviewResponse;
import com.dental.dentalbackend.content.dto.SubmitReviewRequest;
import com.dental.dentalbackend.content.entity.Review;
import com.dental.dentalbackend.content.entity.ReviewStatus;
import com.dental.dentalbackend.content.repository.ReviewRepository;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.user.entity.User;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("Patient submit review creates review in PENDING status")
    void submitReviewStartsInPending() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Smith");

        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setUser(user);

        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));

        Review savedReview = new Review();
        savedReview.setId(UUID.randomUUID());
        savedReview.setPatient(patient);
        savedReview.setRating(5);
        savedReview.setComment("Great clinic!");
        savedReview.setStatus(ReviewStatus.PENDING);
        savedReview.setFeatured(false);

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .rating(5)
                .comment("Great clinic!")
                .build();

        ReviewResponse response = reviewService.submitReview(userId, request, httpRequest);

        assertNotNull(response);
        assertEquals(ReviewStatus.PENDING, response.getStatus());
        assertEquals(5, response.getRating());
        assertFalse(response.isFeatured());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Admin moderating review changes status to APPROVED")
    void moderateReviewToApproved() {
        UUID reviewId = UUID.randomUUID();
        Review existing = new Review();
        existing.setId(reviewId);
        existing.setStatus(ReviewStatus.PENDING);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModerateReviewRequest request = ModerateReviewRequest.builder()
                .status(ReviewStatus.APPROVED)
                .adminResponse("Thank you for your feedback!")
                .featured(true)
                .build();

        UUID adminId = UUID.randomUUID();
        ReviewResponse response = reviewService.moderateReview(reviewId, request, adminId, httpRequest);

        assertNotNull(response);
        assertEquals(ReviewStatus.APPROVED, response.getStatus());
        assertEquals("Thank you for your feedback!", response.getAdminResponse());
        assertTrue(response.isFeatured());
    }
}
