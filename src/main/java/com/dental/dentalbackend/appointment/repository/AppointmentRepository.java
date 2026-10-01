package com.dental.dentalbackend.appointment.repository;

import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findAllByPatientId(UUID patientId, Pageable pageable);

    Page<Appointment> findAllByDoctorId(UUID doctorId, Pageable pageable);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate = :date
              AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'RESCHEDULED')
            """)
    List<Appointment> findActiveAppointmentsForDoctorOnDate(UUID doctorId, LocalDate date);

    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate = :date
              AND a.startTime = :startTime
              AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'RESCHEDULED')
            """)
    boolean existsActiveAppointmentAtSlot(UUID doctorId, LocalDate date, LocalTime startTime);

    @Query("""
            SELECT a FROM Appointment a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:doctorId IS NULL OR a.doctor.id = :doctorId)
              AND (:patientId IS NULL OR a.patient.id = :patientId)
              AND (:fromDate IS NULL OR a.appointmentDate >= :fromDate)
              AND (:toDate IS NULL OR a.appointmentDate <= :toDate)
            ORDER BY a.appointmentDate DESC, a.startTime DESC
            """)
    Page<Appointment> findFiltered(
            AppointmentStatus status,
            UUID doctorId,
            UUID patientId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );
}
