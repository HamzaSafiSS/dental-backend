package com.dental.dentalbackend.availability.service;

import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.repository.AppointmentRepository;
import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.availability.dto.*;
import com.dental.dentalbackend.availability.entity.DoctorAvailability;
import com.dental.dentalbackend.availability.entity.DoctorTimeOff;
import com.dental.dentalbackend.availability.repository.DoctorAvailabilityRepository;
import com.dental.dentalbackend.availability.repository.DoctorTimeOffRepository;
import com.dental.dentalbackend.common.exception.BusinessRuleException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.doctor.entity.Doctor;
import com.dental.dentalbackend.doctor.repository.DoctorRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorTimeOffRepository timeOffRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;

    // ── Availability CRUD ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getDoctorAvailability(UUID doctorId) {
        findDoctorOrThrow(doctorId);
        return availabilityRepository.findAllByDoctorId(doctorId)
                .stream().map(this::mapAvailability).toList();
    }

    @Transactional
    public List<AvailabilityResponse> setDoctorAvailability(UUID doctorId, List<AvailabilityRequest> requests,
                                                              UUID performedBy, HttpServletRequest httpRequest) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        // Replace all existing availability
        availabilityRepository.deleteAllByDoctorId(doctorId);

        List<DoctorAvailability> saved = new ArrayList<>();
        for (AvailabilityRequest req : requests) {
            if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().equals(req.getEndTime())) {
                throw new BusinessRuleException("Start time must be before end time for " + req.getDayOfWeek());
            }

            DoctorAvailability avail = new DoctorAvailability();
            avail.setDoctor(doctor);
            avail.setDayOfWeek(req.getDayOfWeek().toUpperCase());
            avail.setStartTime(req.getStartTime());
            avail.setEndTime(req.getEndTime());
            avail.setAvailable(req.getAvailable() != null ? req.getAvailable() : true);
            saved.add(availabilityRepository.save(avail));
        }

        auditService.log(performedBy, "AVAILABILITY_SET", "Doctor", doctorId,
                "Availability updated for doctor", httpRequest);

        return saved.stream().map(this::mapAvailability).toList();
    }

    // ── Time-off CRUD ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TimeOffResponse> getDoctorTimeOff(UUID doctorId) {
        findDoctorOrThrow(doctorId);
        return timeOffRepository.findAllByDoctorId(doctorId)
                .stream().map(this::mapTimeOff).toList();
    }

    @Transactional
    public TimeOffResponse addTimeOff(UUID doctorId, TimeOffRequest request,
                                       UUID performedBy, HttpServletRequest httpRequest) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Start date must be before or equal to end date");
        }

        DoctorTimeOff timeOff = new DoctorTimeOff();
        timeOff.setDoctor(doctor);
        timeOff.setStartDate(request.getStartDate());
        timeOff.setEndDate(request.getEndDate());
        timeOff.setReason(request.getReason());
        timeOff.setApproved(true);
        timeOff = timeOffRepository.save(timeOff);

        auditService.log(performedBy, "TIME_OFF_ADDED", "Doctor", doctorId,
                "Time-off added: " + request.getStartDate() + " to " + request.getEndDate(), httpRequest);

        return mapTimeOff(timeOff);
    }

    // ── Available slots calculation ───────────────────────────────

    @Transactional(readOnly = true)
    public List<AvailableSlot> getAvailableSlots(UUID doctorId, LocalDate date, int durationMinutes) {
        findDoctorOrThrow(doctorId);

        // 1. Check if doctor has approved time-off on this date
        if (!timeOffRepository.findApprovedTimeOffForDate(doctorId, date).isEmpty()) {
            return List.of(); // Doctor is on time-off
        }

        // 2. Get availability blocks for the day of week
        String dayOfWeek = date.getDayOfWeek().name();
        List<DoctorAvailability> blocks = availabilityRepository
                .findAllByDoctorIdAndDayOfWeekAndAvailableTrue(doctorId, dayOfWeek);

        if (blocks.isEmpty()) {
            return List.of(); // No availability configured for this day
        }

        // 3. Get already-booked appointments for this date
        List<Appointment> bookedAppointments = appointmentRepository
                .findActiveAppointmentsForDoctorOnDate(doctorId, date);

        // 4. Generate all possible slots and filter out booked ones
        List<AvailableSlot> availableSlots = new ArrayList<>();

        for (DoctorAvailability block : blocks) {
            LocalTime slotStart = block.getStartTime();

            while (slotStart.plusMinutes(durationMinutes).compareTo(block.getEndTime()) <= 0) {
                LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);

                // Check if this slot conflicts with any booked appointment
                final LocalTime checkStart = slotStart;
                boolean isBooked = bookedAppointments.stream().anyMatch(apt ->
                        checkStart.isBefore(apt.getEndTime()) && slotEnd.isAfter(apt.getStartTime())
                );

                if (!isBooked) {
                    availableSlots.add(AvailableSlot.builder()
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .available(true)
                            .build());
                }

                slotStart = slotEnd;
            }
        }

        return availableSlots;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
    }

    private AvailabilityResponse mapAvailability(DoctorAvailability a) {
        return AvailabilityResponse.builder()
                .id(a.getId()).dayOfWeek(a.getDayOfWeek())
                .startTime(a.getStartTime()).endTime(a.getEndTime())
                .available(a.isAvailable()).build();
    }

    private TimeOffResponse mapTimeOff(DoctorTimeOff t) {
        return TimeOffResponse.builder()
                .id(t.getId()).startDate(t.getStartDate())
                .endDate(t.getEndDate()).reason(t.getReason())
                .approved(t.isApproved()).build();
    }
}
