package com.dental.dentalbackend.clinical.repository;

import com.dental.dentalbackend.clinical.entity.TreatmentPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, UUID> {

    Page<TreatmentPlan> findAllByPatientId(UUID patientId, Pageable pageable);
}
