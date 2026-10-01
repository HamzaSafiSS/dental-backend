package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.BusinessRuleException;
import com.dental.dentalbackend.content.dto.BeforeAfterCaseResponse;
import com.dental.dentalbackend.content.dto.CreateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.dto.UpdateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.entity.BeforeAfterCase;
import com.dental.dentalbackend.content.repository.BeforeAfterCaseRepository;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.service.repository.DentalServiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeforeAfterServiceTest {

    @Mock
    private BeforeAfterCaseRepository caseRepository;

    @Mock
    private DentalServiceRepository dentalServiceRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private BeforeAfterService beforeAfterService;

    @Test
    @DisplayName("Cannot create active before/after case without patient consent")
    void createActiveCaseWithoutConsentThrowsException() {
        CreateBeforeAfterCaseRequest request = CreateBeforeAfterCaseRequest.builder()
                .title("Teeth Whitening Case")
                .beforeImageUrl("http://example.com/before.jpg")
                .afterImageUrl("http://example.com/after.jpg")
                .active(true)
                .hasPatientConsent(false)
                .build();

        UUID adminId = UUID.randomUUID();

        assertThrows(BusinessRuleException.class, () ->
                beforeAfterService.createCase(request, adminId, httpRequest));

        verify(caseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Can create active before/after case when patient consent is provided")
    void createActiveCaseWithConsentSucceeds() {
        CreateBeforeAfterCaseRequest request = CreateBeforeAfterCaseRequest.builder()
                .title("Teeth Whitening Case")
                .beforeImageUrl("http://example.com/before.jpg")
                .afterImageUrl("http://example.com/after.jpg")
                .active(true)
                .hasPatientConsent(true)
                .build();

        BeforeAfterCase savedCase = new BeforeAfterCase();
        savedCase.setId(UUID.randomUUID());
        savedCase.setTitle(request.getTitle());
        savedCase.setBeforeImageUrl(request.getBeforeImageUrl());
        savedCase.setAfterImageUrl(request.getAfterImageUrl());
        savedCase.setActive(true);
        savedCase.setHasPatientConsent(true);

        when(caseRepository.save(any(BeforeAfterCase.class))).thenReturn(savedCase);

        UUID adminId = UUID.randomUUID();
        BeforeAfterCaseResponse response = beforeAfterService.createCase(request, adminId, httpRequest);

        assertNotNull(response);
        assertTrue(response.isActive());
        assertTrue(response.isHasPatientConsent());
        verify(caseRepository).save(any(BeforeAfterCase.class));
    }

    @Test
    @DisplayName("Cannot update before/after case to active if consent is missing")
    void updateToActiveWithoutConsentThrowsException() {
        UUID caseId = UUID.randomUUID();
        BeforeAfterCase existingCase = new BeforeAfterCase();
        existingCase.setId(caseId);
        existingCase.setActive(false);
        existingCase.setHasPatientConsent(false);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(existingCase));

        UpdateBeforeAfterCaseRequest request = UpdateBeforeAfterCaseRequest.builder()
                .title("Updated Title")
                .beforeImageUrl("http://example.com/b.jpg")
                .afterImageUrl("http://example.com/a.jpg")
                .active(true)
                .hasPatientConsent(false)
                .build();

        UUID adminId = UUID.randomUUID();

        assertThrows(BusinessRuleException.class, () ->
                beforeAfterService.updateCase(caseId, request, adminId, httpRequest));

        verify(caseRepository, never()).save(any());
    }
}
