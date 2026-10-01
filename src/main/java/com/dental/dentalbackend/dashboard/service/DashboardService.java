package com.dental.dentalbackend.dashboard.service;

import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import com.dental.dentalbackend.appointment.repository.AppointmentRepository;
import com.dental.dentalbackend.audit.entity.AuditLog;
import com.dental.dentalbackend.audit.repository.AuditLogRepository;
import com.dental.dentalbackend.dashboard.dto.AppointmentSummaryResponse;
import com.dental.dentalbackend.dashboard.dto.DashboardStatsResponse;
import com.dental.dentalbackend.dashboard.dto.RecentActivityResponse;
import com.dental.dentalbackend.doctor.repository.DoctorRepository;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.payment.repository.AppointmentPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentPaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long activeDoctors = doctorRepository.countByActiveTrue();

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }

        List<Object[]> dbStatusCounts = appointmentRepository.countAppointmentsByStatus();
        for (Object[] row : dbStatusCounts) {
            AppointmentStatus status = (AppointmentStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status.name(), count);
        }

        BigDecimal totalRevenue = paymentRepository.sumTotalVerifiedRevenue();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        BigDecimal todayRevenue = paymentRepository.sumVerifiedRevenueBetween(startOfDay, endOfDay);

        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        BigDecimal monthRevenue = paymentRepository.sumVerifiedRevenueBetween(startOfMonth, endOfMonth);

        return DashboardStatsResponse.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .activeDoctors(activeDoctors)
                .appointmentsByStatus(statusCounts)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .monthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO)
                .build();
    }

    @Transactional(readOnly = true)
    public AppointmentSummaryResponse getAppointmentSummary() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        long todayCount = appointmentRepository.countByAppointmentDate(today);
        long thisWeekCount = appointmentRepository.countByAppointmentDateBetween(startOfWeek, endOfWeek);
        long thisMonthCount = appointmentRepository.countByAppointmentDateBetween(startOfMonth, endOfMonth);

        List<Appointment> todayAppointmentsList = appointmentRepository.findTodayAppointments(today);
        List<Appointment> upcomingAppointmentsList = appointmentRepository.findUpcomingAppointments(
                today.plusDays(1), PageRequest.of(0, 10));

        long upcomingCount = upcomingAppointmentsList.size();

        return AppointmentSummaryResponse.builder()
                .todayCount(todayCount)
                .thisWeekCount(thisWeekCount)
                .thisMonthCount(thisMonthCount)
                .upcomingCount(upcomingCount)
                .todayAppointments(todayAppointmentsList.stream().map(this::mapAppointmentItem).collect(Collectors.toList()))
                .upcomingAppointments(upcomingAppointmentsList.stream().map(this::mapAppointmentItem).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<RecentActivityResponse> getRecentActivity(Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return auditLogs.map(this::mapActivityItem);
    }

    // ── Mapping Helpers ───────────────────────────────────────────────────

    private AppointmentSummaryResponse.AppointmentItemDto mapAppointmentItem(Appointment a) {
        String patientName = null;
        if (a.getPatient() != null && a.getPatient().getUser() != null) {
            patientName = (a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName()).trim();
        }

        String doctorName = null;
        if (a.getDoctor() != null && a.getDoctor().getUser() != null) {
            doctorName = "Dr. " + (a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName()).trim();
        }

        return AppointmentSummaryResponse.AppointmentItemDto.builder()
                .id(a.getId())
                .patientId(a.getPatient() != null ? a.getPatient().getId() : null)
                .patientName(patientName)
                .doctorId(a.getDoctor() != null ? a.getDoctor().getId() : null)
                .doctorName(doctorName)
                .serviceId(a.getService() != null ? a.getService().getId() : null)
                .serviceName(a.getService() != null ? a.getService().getName() : null)
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .build();
    }

    private RecentActivityResponse mapActivityItem(AuditLog log) {
        String userName = null;
        if (log.getUser() != null) {
            userName = (log.getUser().getFirstName() + " " + log.getUser().getLastName()).trim();
        }

        return RecentActivityResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(userName)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
