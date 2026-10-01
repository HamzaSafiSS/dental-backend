package com.dental.dentalbackend.dashboard.service;

import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import com.dental.dentalbackend.appointment.repository.AppointmentRepository;
import com.dental.dentalbackend.audit.repository.AuditLogRepository;
import com.dental.dentalbackend.dashboard.dto.AppointmentSummaryResponse;
import com.dental.dentalbackend.dashboard.dto.DashboardStatsResponse;
import com.dental.dentalbackend.doctor.repository.DoctorRepository;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.payment.repository.AppointmentPaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentPaymentRepository paymentRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getStats calculates patient count, doctor count, status distribution, and revenues correctly")
    void getStatsCalculatesCorrectly() {
        when(patientRepository.count()).thenReturn(150L);
        when(doctorRepository.count()).thenReturn(10L);
        when(doctorRepository.countByActiveTrue()).thenReturn(8L);

        List<Object[]> statusCounts = List.of(
                new Object[]{AppointmentStatus.CONFIRMED, 25L},
                new Object[]{AppointmentStatus.COMPLETED, 100L}
        );
        when(appointmentRepository.countAppointmentsByStatus()).thenReturn(statusCounts);

        when(paymentRepository.sumTotalVerifiedRevenue()).thenReturn(new BigDecimal("50000.00"));
        when(paymentRepository.sumVerifiedRevenueBetween(any(), any()))
                .thenReturn(new BigDecimal("1500.00"));

        DashboardStatsResponse stats = dashboardService.getStats();

        assertNotNull(stats);
        assertEquals(150L, stats.getTotalPatients());
        assertEquals(10L, stats.getTotalDoctors());
        assertEquals(8L, stats.getActiveDoctors());
        assertEquals(25L, stats.getAppointmentsByStatus().get("CONFIRMED"));
        assertEquals(100L, stats.getAppointmentsByStatus().get("COMPLETED"));
        assertEquals(0L, stats.getAppointmentsByStatus().get("CANCELLED"));
        assertEquals(new BigDecimal("50000.00"), stats.getTotalRevenue());
    }

    @Test
    @DisplayName("getAppointmentSummary returns counts for today, this week, and this month")
    void getAppointmentSummaryReturnsCounts() {
        when(appointmentRepository.countByAppointmentDate(any(LocalDate.class))).thenReturn(5L);
        when(appointmentRepository.countByAppointmentDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(20L);
        when(appointmentRepository.findTodayAppointments(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(appointmentRepository.findUpcomingAppointments(any(LocalDate.class), any())).thenReturn(Collections.emptyList());

        AppointmentSummaryResponse summary = dashboardService.getAppointmentSummary();

        assertNotNull(summary);
        assertEquals(5L, summary.getTodayCount());
        assertEquals(20L, summary.getThisWeekCount());
        assertEquals(20L, summary.getThisMonthCount());
    }
}
