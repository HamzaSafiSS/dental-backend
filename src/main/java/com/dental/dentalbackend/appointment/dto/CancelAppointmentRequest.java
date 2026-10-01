package com.dental.dentalbackend.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to cancel an appointment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentRequest {

    private String cancellationReason;
}
