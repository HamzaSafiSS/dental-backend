package com.dental.dentalbackend.appointment.repository;

import com.dental.dentalbackend.appointment.entity.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, UUID> {

    List<AppointmentStatusHistory> findAllByAppointmentIdOrderByCreatedAtAsc(UUID appointmentId);
}
