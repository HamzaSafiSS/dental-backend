package com.dental.dentalbackend.patient.entity;

import com.dental.dentalbackend.common.entity.BaseEntity;
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

import java.time.LocalDate;

@Entity
@Table(name = "patient_medical_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "condition", nullable = false)
    private String condition;

    @Column(name = "description")
    private String description;

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Column(name = "is_current", nullable = false)
    private boolean current = true;
}
