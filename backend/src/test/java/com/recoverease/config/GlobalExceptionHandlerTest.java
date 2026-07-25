package com.recoverease.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    @Test
    void handleRuntime_emailAlreadyRegistered_returns400WithErrorBody() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Email already registered"));

        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("dup@example.com");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void handleRuntime_invalidCredentials_returns400WithErrorBody() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void handleValidation_missingName_returns400WithFieldError() throws Exception {
        // Missing required 'name' — causes MethodArgumentNotValidException
        String body = "{\"email\": \"bad-email\", \"password\": \"ab\", \"role\": \"INDIVIDUAL\"}";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void handleValidation_invalidEmailFormat_returns400WithEmailError() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("not-an-email");
        req.setPassword("password123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void handleValidation_shortPassword_returns400WithPasswordError() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setName("Test");
        req.setEmail("valid@example.com");
        req.setPassword("123");
        req.setRole("INDIVIDUAL");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }
}
