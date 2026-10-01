package com.dental.dentalbackend.dashboard.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.dashboard.dto.AppointmentSummaryResponse;
import com.dental.dentalbackend.dashboard.dto.DashboardStatsResponse;
import com.dental.dentalbackend.dashboard.dto.RecentActivityResponse;
import com.dental.dentalbackend.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Dashboard analytics, appointment metrics, and recent activities")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get overall clinic stats (patients, doctors, appointment status breakdown, revenues)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse response = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/appointments")
    @Operation(summary = "Get appointment summaries (today, this week, this month, upcoming)")
    public ResponseEntity<ApiResponse<AppointmentSummaryResponse>> getAppointmentsSummary() {
        AppointmentSummaryResponse response = dashboardService.getAppointmentSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent audit activity logs (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<RecentActivityResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RecentActivityResponse> result = dashboardService.getRecentActivity(pageable);

        PagedResponse<RecentActivityResponse> pagedResponse = PagedResponse.<RecentActivityResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }
}
