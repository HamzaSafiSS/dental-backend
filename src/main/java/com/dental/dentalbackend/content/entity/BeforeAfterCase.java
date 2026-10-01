package com.dental.dentalbackend.content.entity;

import com.dental.dentalbackend.common.entity.BaseEntity;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.service.entity.DentalService;
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
@Table(name = "before_after_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeforeAfterCase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private DentalService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "before_image_url", nullable = false, length = 500)
    private String beforeImageUrl;

    @Column(name = "after_image_url", nullable = false, length = 500)
    private String afterImageUrl;

    @Column(name = "has_patient_consent", nullable = false)
    private boolean hasPatientConsent = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
