package com.dental.dentalbackend.content.repository;

import com.dental.dentalbackend.content.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, UUID> {

    Optional<BlogCategory> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<BlogCategory> findByActiveTrueOrderByDisplayOrderAsc();

    List<BlogCategory> findAllByOrderByDisplayOrderAsc();
}
