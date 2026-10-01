package com.dental.dentalbackend.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Doctor adds notes to a completed appointment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorNotesRequest {

    private String doctorNotes;
}
