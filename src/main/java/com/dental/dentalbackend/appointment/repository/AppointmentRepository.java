package com.dental.dentalbackend.appointment.repository;

import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

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


    long countByStatus(AppointmentStatus status);

    long countByAppointmentDate(LocalDate date);

    long countByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date ORDER BY a.startTime ASC")
    List<Appointment> findTodayAppointments(LocalDate date);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.appointmentDate >= :fromDate
              AND a.status NOT IN (com.dental.dentalbackend.appointment.entity.AppointmentStatus.CANCELLED, com.dental.dentalbackend.appointment.entity.AppointmentStatus.NO_SHOW)
            ORDER BY a.appointmentDate ASC, a.startTime ASC
            """)
    List<Appointment> findUpcomingAppointments(LocalDate fromDate, Pageable pageable);
}
