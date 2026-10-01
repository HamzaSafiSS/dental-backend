package com.dental.dentalbackend.clinical.repository;

import com.dental.dentalbackend.clinical.entity.DentalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DentalRecordRepository extends JpaRepository<DentalRecord, UUID> {

    Page<DentalRecord> findAllByPatientId(UUID patientId, Pageable pageable);

    Page<DentalRecord> findAllByDoctorId(UUID doctorId, Pageable pageable);

    Page<DentalRecord> findAllByAppointmentId(UUID appointmentId, Pageable pageable);
}
