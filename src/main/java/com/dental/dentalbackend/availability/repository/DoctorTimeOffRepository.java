package com.dental.dentalbackend.availability.repository;

import com.dental.dentalbackend.availability.entity.DoctorTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorTimeOffRepository extends JpaRepository<DoctorTimeOff, UUID> {

    List<DoctorTimeOff> findAllByDoctorId(UUID doctorId);

    @Query("""
            SELECT t FROM DoctorTimeOff t
            WHERE t.doctor.id = :doctorId
              AND t.approved = true
              AND t.startDate <= :date
              AND t.endDate >= :date
            """)
    List<DoctorTimeOff> findApprovedTimeOffForDate(UUID doctorId, LocalDate date);
}
