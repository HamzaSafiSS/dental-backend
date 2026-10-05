package com.dental.dentalbackend.appointment.controller;

import com.dental.dentalbackend.appointment.dto.GuestAppointmentRequest;
import com.dental.dentalbackend.appointment.service.AppointmentEmailService;
import com.dental.dentalbackend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/appointments")
@RequiredArgsConstructor
@Tag(name = "Public Appointments", description = "Endpoints for unauthenticated visitors to request appointments")
public class GuestAppointmentController {

    private final AppointmentEmailService appointmentEmailService;

    @PostMapping("/request")
    @Operation(summary = "Submit an appointment request as a guest",
            description = "Sends an email to the clinic with the visitor's appointment request details. No authentication required.")
    public ResponseEntity<ApiResponse<Void>> requestAppointment(@RequestBody GuestAppointmentRequest request) {
        
        appointmentEmailService.sendGuestAppointmentRequestToClinic(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Your appointment request has been sent! We will contact you shortly to confirm.")
        );
    }
}
