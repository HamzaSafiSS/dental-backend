package com.dental.dentalbackend.content.repository;

import com.dental.dentalbackend.content.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaqCategoryRepository extends JpaRepository<FaqCategory, UUID> {

    Optional<FaqCategory> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<FaqCategory> findByActiveTrueOrderByDisplayOrderAsc();

    List<FaqCategory> findAllByOrderByDisplayOrderAsc();
}
