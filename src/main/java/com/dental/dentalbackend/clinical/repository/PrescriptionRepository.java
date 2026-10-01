package com.dental.dentalbackend.clinical.repository;

import com.dental.dentalbackend.clinical.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    Page<Prescription> findAllByPatientId(UUID patientId, Pageable pageable);
}
