package com.dental.dentalbackend.auth.service;

import com.dental.dentalbackend.auth.dto.*;
import com.dental.dentalbackend.auth.entity.PasswordResetToken;
import com.dental.dentalbackend.auth.entity.RefreshToken;
import com.dental.dentalbackend.auth.repository.PasswordResetTokenRepository;
import com.dental.dentalbackend.auth.repository.RefreshTokenRepository;
import com.dental.dentalbackend.common.exception.BusinessRuleException;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.patient.entity.Patient;
import com.dental.dentalbackend.patient.repository.PatientRepository;
import com.dental.dentalbackend.security.CustomUserDetails;
import com.dental.dentalbackend.security.JwtService;
import com.dental.dentalbackend.user.dto.UserResponse;
import com.dental.dentalbackend.user.entity.Role;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create user with PATIENT role only
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.PATIENT);
        user.setActive(true);
        user = userRepository.save(user);

        // Create patient profile linked to user
        Patient patient = new Patient();
        patient.setUser(user);
        patientRepository.save(patient);

        log.info("Patient registered: {}", user.getEmail());

        return generateTokenResponse(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (!user.isActive()) {
            throw new BusinessRuleException("Account is deactivated. Please contact support.");
        }

        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        return generateTokenResponse(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new BusinessRuleException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Refresh token has expired");
        }

        User user = refreshToken.getUser();

        // Revoke the old refresh token (rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", user.getEmail());

        return generateTokenResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("User logged out, refresh token revoked");
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success to avoid user enumeration
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(token);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            // TODO: Send reset email via NotificationService (Phase 9)
            log.info("Password reset token generated for user: {} — Token: {}", user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new BusinessRuleException("Reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Reset token has expired");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password reset successful for user: {}", user.getEmail());
    }
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password changed successful for user: {}", user.getEmail());
    }
    public UserResponse getCurrentUser(User user) {
        return mapToUserResponse(user);
    }

    // ── Private helpers ─────────────────────────────────────────────

    private TokenResponse generateTokenResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);

        // Create opaque refresh token
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(LocalDateTime.now()
                .plusSeconds(jwtService.getRefreshTokenExpiration() / 1000));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
