package com.dental.dentalbackend.patient.repository;

import com.dental.dentalbackend.patient.entity.PatientEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientEmergencyContactRepository extends JpaRepository<PatientEmergencyContact, UUID> {

    List<PatientEmergencyContact> findAllByPatientId(UUID patientId);

    void deleteAllByPatientId(UUID patientId);
}
