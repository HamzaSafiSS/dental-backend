package com.dental.dentalbackend.content.repository;

import com.dental.dentalbackend.content.entity.Review;
import com.dental.dentalbackend.content.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    Page<Review> findByStatusAndFeaturedTrue(ReviewStatus status, Pageable pageable);

    List<Review> findByPatientId(UUID patientId);
}
