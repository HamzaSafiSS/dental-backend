package com.dental.dentalbackend.appointment.service;

import com.dental.dentalbackend.appointment.dto.AppointmentResponse;
import com.dental.dentalbackend.appointment.dto.CreateAppointmentRequest;
import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import com.dental.dentalbackend.appointment.entity.AppointmentStatusHistory;
import com.dental.dentalbackend.appointment.repository.AppointmentRepository;
import com.dental.dentalbackend.appointment.repository.AppointmentStatusHistoryRepository;
import com.dental.dentalbackend.appointment.statemachine.AppointmentStateMachine;
import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.BusinessRuleException;
import com.dental.dentalbackend.common.exception.InvalidStateTransitionException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.doctor.entity.Doctor;
import com.dental.dentalbackend.doctor.repository.DoctorRepository;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.service.entity.DentalService;
import com.dental.dentalbackend.service.repository.DentalServiceRepository;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository statusHistoryRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ── Create appointment ────────────────────────────────────────

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, UUID patientUserId,
                                                   UUID performedBy, HttpServletRequest httpRequest) {
        // Resolve patient
        Patient patient;
        if (patientUserId != null) {
            // Patient creating their own appointment
            patient = patientRepository.findByUserId(patientUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", patientUserId));
        } else {
            throw new BusinessRuleException("Patient identification is required");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        if (!doctor.isActive()) {
            throw new BusinessRuleException("Doctor is not currently active");
        }

        DentalService service = dentalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        if (!service.isActive()) {
            throw new BusinessRuleException("Service is not currently active");
        }

        // Validate appointment is in the future
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Appointment date must be in the future");
        }

        // Calculate end time from service duration
        LocalTime endTime = request.getStartTime().plusMinutes(service.getDurationMinutes());

        // Check for double-booking
        if (appointmentRepository.existsActiveAppointmentAtSlot(
                request.getDoctorId(), request.getAppointmentDate(), request.getStartTime())) {
            throw new BusinessRuleException("This time slot is already booked");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setPatientNotes(request.getPatientNotes());
        appointment = appointmentRepository.save(appointment);

        // Record status history
        recordStatusChange(appointment, null, AppointmentStatus.PENDING_PAYMENT, performedBy, "Appointment created");

        auditService.log(performedBy, "APPOINTMENT_CREATED", "Appointment", appointment.getId(),
                "Appointment created for " + patient.getUser().getEmail(), httpRequest);

        log.info("Appointment created: {} for patient {}", appointment.getId(), patient.getUser().getEmail());

        return mapToResponse(appointment);
    }

    // ── Create appointment by receptionist (specifying patient) ───

    @Transactional
    public AppointmentResponse createAppointmentForPatient(CreateAppointmentRequest request, UUID patientId,
                                                             UUID performedBy, HttpServletRequest httpRequest) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        if (!doctor.isActive()) {
            throw new BusinessRuleException("Doctor is not currently active");
        }

        DentalService service = dentalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        if (!service.isActive()) {
            throw new BusinessRuleException("Service is not currently active");
        }

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Appointment date must be in the future");
        }

        LocalTime endTime = request.getStartTime().plusMinutes(service.getDurationMinutes());

        if (appointmentRepository.existsActiveAppointmentAtSlot(
                request.getDoctorId(), request.getAppointmentDate(), request.getStartTime())) {
            throw new BusinessRuleException("This time slot is already booked");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setPatientNotes(request.getPatientNotes());
        appointment = appointmentRepository.save(appointment);

        recordStatusChange(appointment, null, AppointmentStatus.PENDING_PAYMENT, performedBy,
                "Appointment created by staff");

        auditService.log(performedBy, "APPOINTMENT_CREATED", "Appointment", appointment.getId(),
                "Appointment created by staff for patient " + patient.getUser().getEmail(), httpRequest);

        return mapToResponse(appointment);
    }

    // ── Query appointments ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(UUID patientUserId, Pageable pageable) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", patientUserId));
        return appointmentRepository.findAllByPatientId(patient.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(UUID doctorUserId, Pageable pageable) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));
        return appointmentRepository.findAllByDoctorId(doctor.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> listAppointments(AppointmentStatus status, UUID doctorId,
                                                        UUID patientId, LocalDate fromDate,
                                                        LocalDate toDate, Pageable pageable) {
        return appointmentRepository.findFiltered(status, doctorId, patientId, fromDate, toDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        return mapToResponse(findAppointmentOrThrow(appointmentId));
    }

    // ── Helpers ───────────────────────────────────────────────────

    public Appointment findAppointmentOrThrow(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
    }

    public void transitionStatus(Appointment appointment, AppointmentStatus newStatus,
                                   UUID performedBy, String reason) {
        AppointmentStatus oldStatus = appointment.getStatus();

        if (!AppointmentStateMachine.canTransition(oldStatus, newStatus)) {
            throw new InvalidStateTransitionException(
                    "Appointment", oldStatus.name(), newStatus.name());
        }

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);

        recordStatusChange(appointment, oldStatus, newStatus, performedBy, reason);
    }

    private void recordStatusChange(Appointment appointment, AppointmentStatus previousStatus,
                                     AppointmentStatus newStatus, UUID userId, String reason) {
        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(appointment);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setReason(reason);

        if (userId != null) {
            userRepository.findById(userId).ifPresent(history::setChangedBy);
        }

        statusHistoryRepository.save(history);
    }

    public AppointmentResponse mapToResponse(Appointment apt) {
        return AppointmentResponse.builder()
                .id(apt.getId())
                .patientId(apt.getPatient().getId())
                .patientName(apt.getPatient().getUser().getFirstName() + " " + apt.getPatient().getUser().getLastName())
                .doctorId(apt.getDoctor().getId())
                .doctorName(apt.getDoctor().getUser().getFirstName() + " " + apt.getDoctor().getUser().getLastName())
                .doctorSpecialization(apt.getDoctor().getSpecialization())
                .serviceId(apt.getService().getId())
                .serviceName(apt.getService().getName())
                .durationMinutes(apt.getService().getDurationMinutes())
                .requiredPaymentAmount(apt.getService().getRequiredPaymentAmount())
                .appointmentDate(apt.getAppointmentDate())
                .startTime(apt.getStartTime())
                .endTime(apt.getEndTime())
                .status(apt.getStatus().name())
                .reasonForVisit(apt.getReasonForVisit())
                .patientNotes(apt.getPatientNotes())
                .doctorNotes(apt.getDoctorNotes())
                .cancellationReason(apt.getCancellationReason())
                .createdAt(apt.getCreatedAt())
                .updatedAt(apt.getUpdatedAt())
                .build();
    }
}
