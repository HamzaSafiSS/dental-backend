package com.dental.dentalbackend.service.repository;

import com.dental.dentalbackend.service.entity.DentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DentalServiceRepository extends JpaRepository<DentalService, UUID> {

    Optional<DentalService> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<DentalService> findAllByActiveTrue(Pageable pageable);

    Page<DentalService> findAllByCategoryId(UUID categoryId, Pageable pageable);

    Page<DentalService> findAllByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);
}
