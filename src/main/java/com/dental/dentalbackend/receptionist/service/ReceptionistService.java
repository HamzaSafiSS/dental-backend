package com.dental.dentalbackend.receptionist.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.receptionist.dto.*;
import com.dental.dentalbackend.receptionist.entity.StaffProfile;
import com.dental.dentalbackend.receptionist.repository.StaffProfileRepository;
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
public class ReceptionistService {

    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ── Admin: Create receptionist ────────────────────────────────

    @Transactional
    public ReceptionistResponse createReceptionist(CreateReceptionistRequest request,
                                                     UUID performedBy, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.RECEPTIONIST);
        user.setActive(true);
        user = userRepository.save(user);

        StaffProfile profile = new StaffProfile();
        profile.setUser(user);
        profile.setPosition(request.getPosition());
        profile.setDepartment(request.getDepartment());
        profile.setProfileImageUrl(request.getProfileImageUrl());
        profile.setActive(true);
        profile = staffProfileRepository.save(profile);

        auditService.log(performedBy, "RECEPTIONIST_CREATED", "StaffProfile", profile.getId(),
                "Receptionist account created: " + user.getEmail(), httpRequest);

        log.info("Receptionist created: {}", user.getEmail());
        return mapToResponse(profile);
    }

    // ── Admin: List receptionists ─────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReceptionistResponse> listReceptionists(Pageable pageable) {
        return staffProfileRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ── Admin: Get receptionist by ID ─────────────────────────────

    @Transactional(readOnly = true)
    public ReceptionistResponse getReceptionistById(UUID profileId) {
        return mapToResponse(findOrThrow(profileId));
    }

    // ── Admin: Update receptionist ────────────────────────────────

    @Transactional
    public ReceptionistResponse updateReceptionist(UUID profileId, UpdateReceptionistRequest request,
                                                     UUID performedBy, HttpServletRequest httpRequest) {
        StaffProfile profile = findOrThrow(profileId);
        User user = profile.getUser();

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        userRepository.save(user);

        if (request.getPosition() != null) profile.setPosition(request.getPosition());
        if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
        if (request.getProfileImageUrl() != null) profile.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getActive() != null) {
            profile.setActive(request.getActive());
            user.setActive(request.getActive());
            userRepository.save(user);
        }
        profile = staffProfileRepository.save(profile);

        auditService.log(performedBy, "RECEPTIONIST_UPDATED", "StaffProfile", profile.getId(),
                "Receptionist updated: " + user.getEmail(), httpRequest);

        return mapToResponse(profile);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private StaffProfile findOrThrow(UUID profileId) {
        return staffProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist", "id", profileId));
    }

    private ReceptionistResponse mapToResponse(StaffProfile profile) {
        User user = profile.getUser();
        return ReceptionistResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .position(profile.getPosition())
                .department(profile.getDepartment())
                .profileImageUrl(profile.getProfileImageUrl())
                .active(profile.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
