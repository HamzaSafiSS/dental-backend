package com.dental.dentalbackend.clinical.entity;

import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.common.entity.BaseEntity;
import com.dental.dentalbackend.doctor.entity.Doctor;
import com.dental.dentalbackend.patient.entity.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dental_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DentalRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "record_type")
    private String recordType;

    @Column(name = "chief_complaint")
    private String chiefComplaint;

    @Column(name = "clinical_findings")
    private String clinicalFindings;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "treatment_performed")
    private String treatmentPerformed;

    @Column(name = "notes")
    private String notes;

    @Column(name = "tooth_number")
    private String toothNumber;
}
