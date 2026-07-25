package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.AuthResponse;
import com.recoverease.dto.LoginRequest;
import com.recoverease.dto.SignupRequest;
import com.recoverease.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    private final AuthResponse sampleResponse =
            new AuthResponse("mock-token", "John", "john@example.com", "INDIVIDUAL", false);

    @Test
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("RecoverEase AI Backend is running"));
    }

    @Test
    void signup_validRequest_returns200WithToken() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenReturn(sampleResponse);

        SignupRequest req = new SignupRequest();
        req.setName("John");
        req.setEmail("john@example.com");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("INDIVIDUAL"));
    }

    @Test
    void signup_missingName_returns400() throws Exception {
        String body = "{\"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"INDIVIDUAL\"}";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_invalidEmail_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("John");
        req.setEmail("not-an-email");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shortPassword_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("John");
        req.setEmail("john@example.com");
        req.setPassword("123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validRequest_returns200WithToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(sampleResponse);

        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void login_missingEmail_returns400() throws Exception {
        String body = "{\"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("not-an-email");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
