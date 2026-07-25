package com.recoverease.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "RecoverEaseAISecretKey2025SuperSecureHackathonJWTToken";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken("user@example.com", 1L, "INDIVIDUAL");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateToken("user@example.com", 1L, "INDIVIDUAL");
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void extractUserId_returnsCorrectUserId() {
        String token = jwtService.generateToken("user@example.com", 42L, "INDIVIDUAL");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken("user@example.com", 1L, "INDIVIDUAL");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken("user@example.com", 1L, "INDIVIDUAL");
        assertThat(jwtService.isTokenValid(token + "corrupted")).isFalse();
    }

    @Test
    void isTokenValid_malformedToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    void isTokenValid_emptyToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    void generateToken_expiredToken_isInvalid() {
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "secret", SECRET);
        ReflectionTestUtils.setField(expiredJwtService, "expiration", -1000L);

        String token = expiredJwtService.generateToken("user@example.com", 1L, "INDIVIDUAL");
        assertThat(expiredJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String token1 = jwtService.generateToken("user1@example.com", 1L, "INDIVIDUAL");
        String token2 = jwtService.generateToken("user2@example.com", 2L, "CAREGIVER");
        assertThat(token1).isNotEqualTo(token2);
    }
}
