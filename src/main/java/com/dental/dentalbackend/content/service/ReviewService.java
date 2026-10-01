package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.content.dto.ModerateReviewRequest;
import com.dental.dentalbackend.content.dto.ReviewResponse;
import com.dental.dentalbackend.content.dto.SubmitReviewRequest;
import com.dental.dentalbackend.content.entity.Review;
import com.dental.dentalbackend.content.entity.ReviewStatus;
import com.dental.dentalbackend.content.repository.ReviewRepository;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    @Transactional
    public ReviewResponse submitReview(UUID patientUserId, SubmitReviewRequest request, HttpServletRequest httpRequest) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", patientUserId));

        Review review = new Review();
        review.setPatient(patient);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(ReviewStatus.PENDING);
        review.setFeatured(false);

        review = reviewRepository.save(review);

        auditService.log(patientUserId, "REVIEW_SUBMITTED", "Review", review.getId(),
                "Patient submitted review with rating " + review.getRating(), httpRequest);

        return mapReviewResponse(review, false);
    }

    @Transactional
    public ReviewResponse moderateReview(UUID id, ModerateReviewRequest request, UUID adminId, HttpServletRequest httpRequest) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));

        review.setStatus(request.getStatus());
        if (request.getAdminResponse() != null) {
            review.setAdminResponse(request.getAdminResponse());
        }
        if (request.getFeatured() != null) {
            review.setFeatured(request.getFeatured());
        }

        review = reviewRepository.save(review);

        auditService.log(adminId, "REVIEW_MODERATED", "Review", review.getId(),
                "Moderated review status to " + request.getStatus() + ", featured=" + review.isFeatured(), httpRequest);

        return mapReviewResponse(review, false);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listPublicReviews(Boolean featuredOnly, Pageable pageable) {
        Page<Review> page = Boolean.TRUE.equals(featuredOnly)
                ? reviewRepository.findByStatusAndFeaturedTrue(ReviewStatus.APPROVED, pageable)
                : reviewRepository.findByStatus(ReviewStatus.APPROVED, pageable);

        return page.map(review -> mapReviewResponse(review, true));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listAllReviews(ReviewStatus status, Pageable pageable) {
        Page<Review> page = status != null
                ? reviewRepository.findByStatus(status, pageable)
                : reviewRepository.findAll(pageable);

        return page.map(review -> mapReviewResponse(review, false));
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        return mapReviewResponse(review, false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ReviewResponse mapReviewResponse(Review review, boolean anonymizePatientName) {
        String patientName = null;
        if (review.getPatient() != null && review.getPatient().getUser() != null) {
            String firstName = review.getPatient().getUser().getFirstName();
            String lastName = review.getPatient().getUser().getLastName();
            if (anonymizePatientName) {
                String lastInitial = (lastName != null && !lastName.isBlank())
                        ? lastName.substring(0, 1).toUpperCase() + "."
                        : "";
                patientName = (firstName + " " + lastInitial).trim();
            } else {
                patientName = (firstName + " " + lastName).trim();
            }
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .patientId(review.getPatient() != null ? review.getPatient().getId() : null)
                .patientName(patientName)
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .adminResponse(review.getAdminResponse())
                .featured(review.isFeatured())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
