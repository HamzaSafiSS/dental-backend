package com.dental.dentalbackend.patient.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.patient.dto.*;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.entity.PatientAllergy;
import com.dental.dentalbackend.patient.entity.PatientEmergencyContact;
import com.dental.dentalbackend.patient.entity.PatientMedicalHistory;
import com.dental.dentalbackend.patient.repository.PatientAllergyRepository;
import com.dental.dentalbackend.patient.repository.PatientEmergencyContactRepository;
import com.dental.dentalbackend.patient.repository.PatientMedicalHistoryRepository;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.user.entity.Role;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientEmergencyContactRepository emergencyContactRepository;
    private final PatientMedicalHistoryRepository medicalHistoryRepository;
    private final PatientAllergyRepository allergyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ── Admin/Receptionist: Create patient ────────────────────────

    @Transactional
    public PatientResponse createPatient(CreatePatientRequest request, UUID performedBy, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create user account
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.PATIENT);
        user.setActive(true);
        user = userRepository.save(user);

        // Create patient profile
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setSecondaryPhone(request.getSecondaryPhone());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        patient.setBloodType(request.getBloodType());
        patient.setNotes(request.getNotes());
        patient = patientRepository.save(patient);

        auditService.log(performedBy, "PATIENT_CREATED", "Patient", patient.getId(),
                "Patient registered: " + user.getEmail(), httpRequest);

        log.info("Patient created by staff: {}", user.getEmail());
        return mapToResponse(patient, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    // ── Admin/Receptionist: List patients ─────────────────────────

    @Transactional(readOnly = true)
    public Page<PatientResponse> listPatients(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(p -> mapToResponse(p, null, null, null));
    }

    // ── Admin/Receptionist: Get patient by ID ─────────────────────

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID patientId) {
        Patient patient = findPatientOrThrow(patientId);
        List<PatientEmergencyContact> contacts = emergencyContactRepository.findAllByPatientId(patientId);
        List<PatientMedicalHistory> history = medicalHistoryRepository.findAllByPatientId(patientId);
        List<PatientAllergy> allergies = allergyRepository.findAllByPatientId(patientId);
        return mapToResponse(patient, contacts, history, allergies);
    }

    // ── Admin/Receptionist: Update patient ────────────────────────

    @Transactional
    public PatientResponse updatePatient(UUID patientId, UpdatePatientRequest request,
                                          UUID performedBy, HttpServletRequest httpRequest) {
        Patient patient = findPatientOrThrow(patientId);
        User user = patient.getUser();

        // Update user fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getActive() != null) user.setActive(request.getActive());
        userRepository.save(user);

        // Update patient profile fields
        if (request.getDateOfBirth() != null) patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) patient.setGender(request.getGender());
        if (request.getSecondaryPhone() != null) patient.setSecondaryPhone(request.getSecondaryPhone());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());
        if (request.getCity() != null) patient.setCity(request.getCity());
        if (request.getState() != null) patient.setState(request.getState());
        if (request.getZipCode() != null) patient.setZipCode(request.getZipCode());
        if (request.getBloodType() != null) patient.setBloodType(request.getBloodType());
        if (request.getNotes() != null) patient.setNotes(request.getNotes());
        patient = patientRepository.save(patient);

        auditService.log(performedBy, "PATIENT_UPDATED", "Patient", patient.getId(),
                "Patient updated: " + user.getEmail(), httpRequest);

        return getPatientById(patientId);
    }

    // ── Patient self-service: View own profile ────────────────────

    @Transactional(readOnly = true)
    public PatientResponse getMyProfile(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", userId));

        List<PatientEmergencyContact> contacts = emergencyContactRepository.findAllByPatientId(patient.getId());
        List<PatientMedicalHistory> history = medicalHistoryRepository.findAllByPatientId(patient.getId());
        List<PatientAllergy> allergies = allergyRepository.findAllByPatientId(patient.getId());
        return mapToResponse(patient, contacts, history, allergies);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Patient findPatientOrThrow(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
    }

    private PatientResponse mapToResponse(Patient patient,
                                           List<PatientEmergencyContact> contacts,
                                           List<PatientMedicalHistory> history,
                                           List<PatientAllergy> allergies) {
        User user = patient.getUser();
        return PatientResponse.builder()
                .id(patient.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .secondaryPhone(patient.getSecondaryPhone())
                .address(patient.getAddress())
                .city(patient.getCity())
                .state(patient.getState())
                .zipCode(patient.getZipCode())
                .bloodType(patient.getBloodType())
                .profileImageUrl(patient.getProfileImageUrl())
                .notes(patient.getNotes())
                .active(user.isActive())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .emergencyContacts(contacts != null ? contacts.stream().map(this::mapContact).toList() : null)
                .medicalHistory(history != null ? history.stream().map(this::mapHistory).toList() : null)
                .allergies(allergies != null ? allergies.stream().map(this::mapAllergy).toList() : null)
                .build();
    }

    private EmergencyContactResponse mapContact(PatientEmergencyContact c) {
        return EmergencyContactResponse.builder()
                .id(c.getId()).name(c.getName()).relationship(c.getRelationship())
                .phone(c.getPhone()).email(c.getEmail()).build();
    }

    private MedicalHistoryResponse mapHistory(PatientMedicalHistory h) {
        return MedicalHistoryResponse.builder()
                .id(h.getId()).condition(h.getCondition()).description(h.getDescription())
                .diagnosedDate(h.getDiagnosedDate()).current(h.isCurrent()).build();
    }

    private AllergyResponse mapAllergy(PatientAllergy a) {
        return AllergyResponse.builder()
                .id(a.getId()).allergen(a.getAllergen()).severity(a.getSeverity())
                .reaction(a.getReaction()).build();
    }
}
