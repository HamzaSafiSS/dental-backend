package com.dental.dentalbackend.dashboard.dto;

import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSummaryResponse {

    private long todayCount;
    private long thisWeekCount;
    private long thisMonthCount;
    private long upcomingCount;
    private List<AppointmentItemDto> todayAppointments;
    private List<AppointmentItemDto> upcomingAppointments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentItemDto {
        private UUID id;
        private UUID patientId;
        private String patientName;
        private UUID doctorId;
        private String doctorName;
        private UUID serviceId;
        private String serviceName;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private AppointmentStatus status;
    }
}
