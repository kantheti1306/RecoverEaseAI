package com.recoverease.service;

import com.recoverease.config.JwtService;
import com.recoverease.dto.AuthResponse;
import com.recoverease.dto.LoginRequest;
import com.recoverease.dto.SignupRequest;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setRole("INDIVIDUAL");
        testUser.setOnboardingComplete(false);
    }

    // ===== Signup Tests =====

    @Test
    void signup_success_returnsAuthResponse() {
        SignupRequest req = new SignupRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("password123");
        req.setRole("individual");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.signup(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("INDIVIDUAL");
        assertThat(response.isOnboardingComplete()).isFalse();

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_duplicateEmail_throwsRuntimeException() {
        SignupRequest req = new SignupRequest();
        req.setEmail("john@example.com");
        req.setName("John");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_roleIsUppercased() {
        SignupRequest req = new SignupRequest();
        req.setName("Jane");
        req.setEmail("jane@example.com");
        req.setPassword("pass123");
        req.setRole("caregiver");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("token");

        // Capture the saved user to verify role is uppercased
        when(userRepository.save(argThat(u -> "CAREGIVER".equals(u.getRole()))))
                .thenAnswer(inv -> {
                    User u = inv.getArgument(0);
                    u.setId(2L);
                    return u;
                });

        AuthResponse response = authService.signup(req);
        assertThat(response.getRole()).isEqualTo("CAREGIVER");
    }

    // ===== Login Tests =====

    @Test
    void login_success_returnsAuthResponse() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void login_userNotFound_throwsRuntimeException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@example.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_wrongPassword_throwsRuntimeException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("wrongpass");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtService, never()).generateToken(any(), any(), any());
    }
}
