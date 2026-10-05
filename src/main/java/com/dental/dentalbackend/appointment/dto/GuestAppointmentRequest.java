package com.dental.dentalbackend.appointment.dto;

import lombok.Data;

@Data
public class GuestAppointmentRequest {
    private String service;
    private String dentistName;
    private String date;
    private String time;
    private String fullName;
    private String phone;
    private String email;
    private String dob;
    private String patientType;
    private String contactMethod;
    private String reason;
}
