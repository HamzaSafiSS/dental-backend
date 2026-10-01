package com.dental.dentalbackend.clinical.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.clinical.dto.*;
import com.dental.dentalbackend.clinical.entity.*;
import com.dental.dentalbackend.clinical.repository.DentalRecordRepository;
import com.dental.dentalbackend.clinical.repository.PrescriptionRepository;
import com.dental.dentalbackend.clinical.repository.TreatmentPlanRepository;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.doctor.entity.Doctor;
import com.dental.dentalbackend.doctor.repository.DoctorRepository;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.service.repository.DentalServiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicalService {

    private final DentalRecordRepository dentalRecordRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final AuditService auditService;

    // ══════════════════════════════════════════════════════════════
    // DENTAL RECORDS
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public DentalRecordResponse createDentalRecord(UUID patientId, CreateDentalRecordRequest request,
                                                     UUID doctorUserId, HttpServletRequest httpRequest) {
        Patient patient = findPatientOrThrow(patientId);
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));

        DentalRecord record = new DentalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setRecordType(request.getRecordType());
        record.setChiefComplaint(request.getChiefComplaint());
        record.setClinicalFindings(request.getClinicalFindings());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatmentPerformed(request.getTreatmentPerformed());
        record.setNotes(request.getNotes());
        record.setToothNumber(request.getToothNumber());
        record = dentalRecordRepository.save(record);

        auditService.log(doctor.getUser().getId(), "DENTAL_RECORD_CREATED", "DentalRecord", record.getId(),
                "Dental record created for patient " + patient.getUser().getEmail(), httpRequest);

        return mapRecordResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<DentalRecordResponse> getPatientDentalRecords(UUID patientId, Pageable pageable) {
        findPatientOrThrow(patientId);
        return dentalRecordRepository.findAllByPatientId(patientId, pageable)
                .map(this::mapRecordResponse);
    }

    // ══════════════════════════════════════════════════════════════
    // TREATMENT PLANS
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public TreatmentPlanResponse createTreatmentPlan(UUID patientId, CreateTreatmentPlanRequest request,
                                                       UUID doctorUserId, HttpServletRequest httpRequest) {
        Patient patient = findPatientOrThrow(patientId);
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));

        TreatmentPlan plan = new TreatmentPlan();
        plan.setPatient(patient);
        plan.setDoctor(doctor);
        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setStatus("PROPOSED");
        plan.setStartDate(request.getStartDate());
        plan.setEstimatedEndDate(request.getEstimatedEndDate());
        plan.setTotalEstimatedCost(request.getTotalEstimatedCost());
        plan.setNotes(request.getNotes());

        if (request.getItems() != null) {
            for (TreatmentPlanItemRequest itemReq : request.getItems()) {
                TreatmentPlanItem item = new TreatmentPlanItem();
                item.setTreatmentPlan(plan);
                item.setToothNumber(itemReq.getToothNumber());
                item.setDescription(itemReq.getDescription());
                item.setSequenceOrder(itemReq.getSequenceOrder());
                item.setEstimatedCost(itemReq.getEstimatedCost());
                item.setNotes(itemReq.getNotes());
                item.setStatus("PENDING");

                if (itemReq.getServiceId() != null) {
                    dentalServiceRepository.findById(itemReq.getServiceId())
                            .ifPresent(item::setService);
                }

                plan.getItems().add(item);
            }
        }

        plan = treatmentPlanRepository.save(plan);

        auditService.log(doctor.getUser().getId(), "TREATMENT_PLAN_CREATED", "TreatmentPlan", plan.getId(),
                "Treatment plan created: " + plan.getTitle(), httpRequest);

        return mapPlanResponse(plan);
    }

    @Transactional(readOnly = true)
    public Page<TreatmentPlanResponse> getPatientTreatmentPlans(UUID patientId, Pageable pageable) {
        findPatientOrThrow(patientId);
        return treatmentPlanRepository.findAllByPatientId(patientId, pageable)
                .map(this::mapPlanResponse);
    }

    @Transactional(readOnly = true)
    public TreatmentPlanResponse getTreatmentPlanById(UUID planId) {
        TreatmentPlan plan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TreatmentPlan", "id", planId));
        return mapPlanResponse(plan);
    }

    // ══════════════════════════════════════════════════════════════
    // PRESCRIPTIONS
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public PrescriptionResponse createPrescription(UUID patientId, CreatePrescriptionRequest request,
                                                     UUID doctorUserId, HttpServletRequest httpRequest) {
        Patient patient = findPatientOrThrow(patientId);
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setNotes(request.getNotes());

        if (request.getItems() != null) {
            for (PrescriptionItemRequest itemReq : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(prescription);
                item.setMedicationName(itemReq.getMedicationName());
                item.setDosage(itemReq.getDosage());
                item.setFrequency(itemReq.getFrequency());
                item.setDuration(itemReq.getDuration());
                item.setQuantity(itemReq.getQuantity());
                item.setInstructions(itemReq.getInstructions());
                prescription.getItems().add(item);
            }
        }

        prescription = prescriptionRepository.save(prescription);

        auditService.log(doctor.getUser().getId(), "PRESCRIPTION_CREATED", "Prescription", prescription.getId(),
                "Prescription created for patient " + patient.getUser().getEmail(), httpRequest);

        return mapPrescriptionResponse(prescription);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPatientPrescriptions(UUID patientId, Pageable pageable) {
        findPatientOrThrow(patientId);
        return prescriptionRepository.findAllByPatientId(patientId, pageable)
                .map(this::mapPrescriptionResponse);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(UUID prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));
        return mapPrescriptionResponse(prescription);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Patient findPatientOrThrow(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
    }

    private DentalRecordResponse mapRecordResponse(DentalRecord r) {
        return DentalRecordResponse.builder()
                .id(r.getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getUser().getFirstName() + " " + r.getPatient().getUser().getLastName())
                .doctorId(r.getDoctor().getId())
                .doctorName(r.getDoctor().getUser().getFirstName() + " " + r.getDoctor().getUser().getLastName())
                .appointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null)
                .recordType(r.getRecordType())
                .chiefComplaint(r.getChiefComplaint())
                .clinicalFindings(r.getClinicalFindings())
                .diagnosis(r.getDiagnosis())
                .treatmentPerformed(r.getTreatmentPerformed())
                .notes(r.getNotes())
                .toothNumber(r.getToothNumber())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private TreatmentPlanResponse mapPlanResponse(TreatmentPlan p) {
        return TreatmentPlanResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getUser().getFirstName() + " " + p.getPatient().getUser().getLastName())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getUser().getFirstName() + " " + p.getDoctor().getUser().getLastName())
                .appointmentId(p.getAppointment() != null ? p.getAppointment().getId() : null)
                .title(p.getTitle())
                .description(p.getDescription())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .estimatedEndDate(p.getEstimatedEndDate())
                .totalEstimatedCost(p.getTotalEstimatedCost())
                .notes(p.getNotes())
                .items(p.getItems() != null ? p.getItems().stream().map(this::mapPlanItemResponse).toList() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private TreatmentPlanItemResponse mapPlanItemResponse(TreatmentPlanItem i) {
        return TreatmentPlanItemResponse.builder()
                .id(i.getId())
                .serviceId(i.getService() != null ? i.getService().getId() : null)
                .serviceName(i.getService() != null ? i.getService().getName() : null)
                .toothNumber(i.getToothNumber())
                .description(i.getDescription())
                .sequenceOrder(i.getSequenceOrder())
                .estimatedCost(i.getEstimatedCost())
                .status(i.getStatus())
                .notes(i.getNotes())
                .build();
    }

    private PrescriptionResponse mapPrescriptionResponse(Prescription p) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getUser().getFirstName() + " " + p.getPatient().getUser().getLastName())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getUser().getFirstName() + " " + p.getDoctor().getUser().getLastName())
                .appointmentId(p.getAppointment() != null ? p.getAppointment().getId() : null)
                .prescriptionDate(p.getPrescriptionDate())
                .diagnosis(p.getDiagnosis())
                .notes(p.getNotes())
                .items(p.getItems() != null ? p.getItems().stream().map(this::mapPrescriptionItemResponse).toList() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PrescriptionItemResponse mapPrescriptionItemResponse(PrescriptionItem i) {
        return PrescriptionItemResponse.builder()
                .id(i.getId())
                .medicationName(i.getMedicationName())
                .dosage(i.getDosage())
                .frequency(i.getFrequency())
                .duration(i.getDuration())
                .quantity(i.getQuantity())
                .instructions(i.getInstructions())
                .build();
    }
}
