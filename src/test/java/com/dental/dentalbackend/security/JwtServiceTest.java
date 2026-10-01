package com.dental.dentalbackend.security;

import com.dental.dentalbackend.user.entity.Role;
import com.dental.dentalbackend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private CustomUserDetails userDetails;

    // 256-bit Base64-encoded test secret
    private static final String TEST_SECRET = "ZGVudGFsLWNsaW5pYy1tYW5hZ2VtZW50LXN5c3RlbS1qd3Qtc2VjcmV0LWtleS0yMDI0LXByb2R1Y3Rpb24=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L); // 15 min
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L); // 7 days

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("doctor@test.com");
        user.setPasswordHash("hashed_pw");
        user.setRole(Role.DOCTOR);
        user.setActive(true);
        user.setFirstName("Jane");
        user.setLastName("Doe");

        userDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("Generate access token and verify subject and validity")
    void generateAndValidateToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String username = jwtService.extractUsername(token);
        assertEquals("doctor@test.com", username);

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token validation fails for wrong user")
    void invalidForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);

        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setEmail("other@test.com");
        anotherUser.setPasswordHash("hashed_pw");
        anotherUser.setRole(Role.PATIENT);
        anotherUser.setActive(true);

        CustomUserDetails otherDetails = new CustomUserDetails(anotherUser);

        assertFalse(jwtService.isTokenValid(token, otherDetails));
    }

    @Test
    @DisplayName("Invalid token string returns false")
    void invalidTokenStringReturnsFalse() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token", userDetails));
    }
}
