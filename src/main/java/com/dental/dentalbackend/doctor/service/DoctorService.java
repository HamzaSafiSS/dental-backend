package com.dental.dentalbackend.doctor.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.doctor.dto.*;
import com.dental.dentalbackend.doctor.entity.Doctor;
import com.dental.dentalbackend.doctor.repository.DoctorRepository;
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

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ── Admin: Create doctor ──────────────────────────────────────

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request, UUID performedBy, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.DOCTOR);
        user.setActive(true);
        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setProfileImageUrl(request.getProfileImageUrl());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setActive(true);
        doctor = doctorRepository.save(doctor);

        auditService.log(performedBy, "DOCTOR_CREATED", "Doctor", doctor.getId(),
                "Doctor account created: " + user.getEmail(), httpRequest);

        log.info("Doctor created: {}", user.getEmail());
        return mapToResponse(doctor);
    }

    // ── Admin: List all doctors ───────────────────────────────────

    @Transactional(readOnly = true)
    public Page<DoctorResponse> listDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ── Admin: Get doctor by ID ───────────────────────────────────

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID doctorId) {
        return mapToResponse(findDoctorOrThrow(doctorId));
    }

    // ── Admin: Update doctor ──────────────────────────────────────

    @Transactional
    public DoctorResponse updateDoctor(UUID doctorId, UpdateDoctorRequest request,
                                        UUID performedBy, HttpServletRequest httpRequest) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        User user = doctor.getUser();

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        userRepository.save(user);

        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getQualification() != null) doctor.setQualification(request.getQualification());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getBio() != null) doctor.setBio(request.getBio());
        if (request.getLicenseNumber() != null) doctor.setLicenseNumber(request.getLicenseNumber());
        if (request.getProfileImageUrl() != null) doctor.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getActive() != null) {
            doctor.setActive(request.getActive());
            user.setActive(request.getActive());
            userRepository.save(user);
        }
        doctor = doctorRepository.save(doctor);

        auditService.log(performedBy, "DOCTOR_UPDATED", "Doctor", doctor.getId(),
                "Doctor updated: " + user.getEmail(), httpRequest);

        return mapToResponse(doctor);
    }

    // ── Doctor: View own profile ──────────────────────────────────
    // NOTE: Doctor self-update is intentionally NOT implemented per business requirement.

    @Transactional(readOnly = true)
    public DoctorResponse getMyProfile(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", userId));
        return mapToResponse(doctor);
    }

    // ── Public: List active doctors ───────────────────────────────

    @Transactional(readOnly = true)
    public Page<DoctorPublicResponse> listPublicDoctors(Pageable pageable) {
        return doctorRepository.findAllByActiveTrue(pageable).map(this::mapToPublicResponse);
    }

    // ── Public: Get doctor public profile ─────────────────────────

    @Transactional(readOnly = true)
    public DoctorPublicResponse getPublicDoctorById(UUID doctorId) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        if (!doctor.isActive()) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }
        return mapToPublicResponse(doctor);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        User user = doctor.getUser();
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .bio(doctor.getBio())
                .licenseNumber(doctor.getLicenseNumber())
                .profileImageUrl(doctor.getProfileImageUrl())
                .consultationFee(doctor.getConsultationFee())
                .active(doctor.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(doctor.getCreatedAt())
                .build();
    }

    private DoctorPublicResponse mapToPublicResponse(Doctor doctor) {
        User user = doctor.getUser();
        return DoctorPublicResponse.builder()
                .id(doctor.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .bio(doctor.getBio())
                .profileImageUrl(doctor.getProfileImageUrl())
                .build();
    }
}
