package com.dental.dentalbackend.availability.repository;

import com.dental.dentalbackend.availability.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {

    List<DoctorAvailability> findAllByDoctorId(UUID doctorId);

    List<DoctorAvailability> findAllByDoctorIdAndDayOfWeekAndAvailableTrue(UUID doctorId, String dayOfWeek);

    void deleteAllByDoctorId(UUID doctorId);
}
