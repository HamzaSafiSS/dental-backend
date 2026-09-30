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

@Entity
@Table(name = "patient_emergency_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientEmergencyContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;
}
