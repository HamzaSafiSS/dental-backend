package com.dental.dentalbackend.content.repository;

import com.dental.dentalbackend.content.entity.BeforeAfterCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BeforeAfterCaseRepository extends JpaRepository<BeforeAfterCase, UUID> {

    List<BeforeAfterCase> findByActiveTrueAndHasPatientConsentTrueOrderByDisplayOrderAsc();

    List<BeforeAfterCase> findByServiceIdAndActiveTrueAndHasPatientConsentTrueOrderByDisplayOrderAsc(UUID serviceId);

    Page<BeforeAfterCase> findByServiceId(UUID serviceId, Pageable pageable);
}
