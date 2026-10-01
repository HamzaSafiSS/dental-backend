package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.BusinessRuleException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.content.dto.BeforeAfterCaseResponse;
import com.dental.dentalbackend.content.dto.CreateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.dto.UpdateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.entity.BeforeAfterCase;
import com.dental.dentalbackend.content.repository.BeforeAfterCaseRepository;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.service.entity.DentalService;
import com.dental.dentalbackend.service.repository.DentalServiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeforeAfterService {

    private final BeforeAfterCaseRepository caseRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    @Transactional
    public BeforeAfterCaseResponse createCase(CreateBeforeAfterCaseRequest request, UUID adminId, HttpServletRequest httpRequest) {
        boolean active = Boolean.TRUE.equals(request.getActive());
        boolean hasConsent = Boolean.TRUE.equals(request.getHasPatientConsent());

        if (active && !hasConsent) {
            throw new BusinessRuleException("Cannot activate a before/after case without verified patient consent.");
        }

        BeforeAfterCase caseItem = new BeforeAfterCase();
        caseItem.setTitle(request.getTitle());
        caseItem.setDescription(request.getDescription());
        caseItem.setBeforeImageUrl(request.getBeforeImageUrl());
        caseItem.setAfterImageUrl(request.getAfterImageUrl());
        caseItem.setHasPatientConsent(hasConsent);
        caseItem.setActive(active);
        caseItem.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        if (request.getServiceId() != null) {
            DentalService service = dentalServiceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("DentalService", request.getServiceId()));
            caseItem.setService(service);
        }

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));
            caseItem.setPatient(patient);
        }

        caseItem = caseRepository.save(caseItem);

        auditService.log(adminId, "BEFORE_AFTER_CASE_CREATED", "BeforeAfterCase", caseItem.getId(),
                "Created before/after case: " + caseItem.getTitle(), httpRequest);

        return mapCaseResponse(caseItem);
    }

    @Transactional
    public BeforeAfterCaseResponse updateCase(UUID id, UpdateBeforeAfterCaseRequest request, UUID adminId, HttpServletRequest httpRequest) {
        BeforeAfterCase caseItem = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BeforeAfterCase", id));

        boolean willBeActive = request.getActive() != null ? request.getActive() : caseItem.isActive();
        boolean willHaveConsent = request.getHasPatientConsent() != null ? request.getHasPatientConsent() : caseItem.isHasPatientConsent();

        if (willBeActive && !willHaveConsent) {
            throw new BusinessRuleException("Cannot activate a before/after case without verified patient consent.");
        }

        caseItem.setTitle(request.getTitle());
        caseItem.setDescription(request.getDescription());
        caseItem.setBeforeImageUrl(request.getBeforeImageUrl());
        caseItem.setAfterImageUrl(request.getAfterImageUrl());
        caseItem.setHasPatientConsent(willHaveConsent);
        caseItem.setActive(willBeActive);
        if (request.getDisplayOrder() != null) {
            caseItem.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getServiceId() != null) {
            DentalService service = dentalServiceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("DentalService", request.getServiceId()));
            caseItem.setService(service);
        } else {
            caseItem.setService(null);
        }

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));
            caseItem.setPatient(patient);
        } else {
            caseItem.setPatient(null);
        }

        caseItem = caseRepository.save(caseItem);

        auditService.log(adminId, "BEFORE_AFTER_CASE_UPDATED", "BeforeAfterCase", caseItem.getId(),
                "Updated before/after case: " + caseItem.getTitle(), httpRequest);

        return mapCaseResponse(caseItem);
    }

    @Transactional
    public void deleteCase(UUID id, UUID adminId, HttpServletRequest httpRequest) {
        BeforeAfterCase caseItem = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BeforeAfterCase", id));

        caseRepository.delete(caseItem);

        auditService.log(adminId, "BEFORE_AFTER_CASE_DELETED", "BeforeAfterCase", id,
                "Deleted before/after case: " + caseItem.getTitle(), httpRequest);
    }

    @Transactional(readOnly = true)
    public List<BeforeAfterCaseResponse> listPublicCases(UUID serviceId) {
        List<BeforeAfterCase> cases = serviceId != null
                ? caseRepository.findByServiceIdAndActiveTrueAndHasPatientConsentTrueOrderByDisplayOrderAsc(serviceId)
                : caseRepository.findByActiveTrueAndHasPatientConsentTrueOrderByDisplayOrderAsc();

        return cases.stream().map(this::mapCaseResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BeforeAfterCaseResponse> listAllCases(UUID serviceId, Pageable pageable) {
        Page<BeforeAfterCase> page = serviceId != null
                ? caseRepository.findByServiceId(serviceId, pageable)
                : caseRepository.findAll(pageable);

        return page.map(this::mapCaseResponse);
    }

    @Transactional(readOnly = true)
    public BeforeAfterCaseResponse getCaseById(UUID id) {
        BeforeAfterCase caseItem = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BeforeAfterCase", id));
        return mapCaseResponse(caseItem);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private BeforeAfterCaseResponse mapCaseResponse(BeforeAfterCase caseItem) {
        return BeforeAfterCaseResponse.builder()
                .id(caseItem.getId())
                .serviceId(caseItem.getService() != null ? caseItem.getService().getId() : null)
                .serviceName(caseItem.getService() != null ? caseItem.getService().getName() : null)
                .patientId(caseItem.getPatient() != null ? caseItem.getPatient().getId() : null)
                .title(caseItem.getTitle())
                .description(caseItem.getDescription())
                .beforeImageUrl(caseItem.getBeforeImageUrl())
                .afterImageUrl(caseItem.getAfterImageUrl())
                .hasPatientConsent(caseItem.isHasPatientConsent())
                .active(caseItem.isActive())
                .displayOrder(caseItem.getDisplayOrder())
                .createdAt(caseItem.getCreatedAt())
                .updatedAt(caseItem.getUpdatedAt())
                .build();
    }
}
