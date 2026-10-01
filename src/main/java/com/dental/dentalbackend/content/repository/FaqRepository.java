package com.dental.dentalbackend.content.repository;

import com.dental.dentalbackend.content.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqRepository extends JpaRepository<Faq, UUID> {

    List<Faq> findByActiveTrueOrderByDisplayOrderAsc();

    List<Faq> findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(UUID categoryId);

    List<Faq> findByCategoryIdOrderByDisplayOrderAsc(UUID categoryId);

    List<Faq> findAllByOrderByDisplayOrderAsc();

    Page<Faq> findByCategoryId(UUID categoryId, Pageable pageable);
}
