package com.dental.dentalbackend.clinical.entity;

import com.dental.dentalbackend.common.entity.BaseEntity;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "treatment_plan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    private TreatmentPlan treatmentPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private DentalService service;

    @Column(name = "tooth_number")
    private String toothNumber;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder = 0;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "notes")
    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
