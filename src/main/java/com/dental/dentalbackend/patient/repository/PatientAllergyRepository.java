package com.dental.dentalbackend.patient.repository;

import com.dental.dentalbackend.patient.entity.PatientAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, UUID> {

    List<PatientAllergy> findAllByPatientId(UUID patientId);
}
