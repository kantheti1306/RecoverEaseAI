package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.TestSecurityUtils;
import com.recoverease.dto.OnboardingRequest;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Always delete and re-create so each test starts fresh
        userRepository.findByEmail("user_ctrl_test@example.com")
                .ifPresent(userRepository::delete);

        testUser = new User();
        testUser.setName("Pavan");
        testUser.setEmail("user_ctrl_test@example.com");
        testUser.setPasswordHash("$2a$10$dummy");
        testUser.setRole("INDIVIDUAL");
        testUser.setOnboardingComplete(false);
        testUser = userRepository.save(testUser);
    }

    // ─── GET /api/user/profile ─────────────────────────────────────────────────

    @Test
    void getProfile_withoutAuth_returns401or403() throws Exception {
        int status = mockMvc.perform(get("/api/user/profile")).andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    void getProfile_withAuth_returns200WithUserData() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pavan"))
                .andExpect(jsonPath("$.email").value("user_ctrl_test@example.com"))
                .andExpect(jsonPath("$.role").value("INDIVIDUAL"));
    }

    // ─── POST /api/user/onboarding ────────────────────────────────────────────

    @Test
    void completeOnboarding_withoutAuth_returns401or403() throws Exception {
        int status = mockMvc.perform(post("/api/user/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOnboardingRequest())))
                .andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    void completeOnboarding_validRequest_returns200WithSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/user/onboarding")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOnboardingRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Onboarding complete"))
                .andExpect(jsonPath("$.onboardingComplete").value(true));
    }

    @Test
    void completeOnboarding_persistsAllFields() throws Exception {
        mockMvc.perform(post("/api/user/onboarding")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOnboardingRequest())))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail("user_ctrl_test@example.com").orElseThrow();
        assertThat(updated.isOnboardingComplete()).isTrue();
        assertThat(updated.getTriggers()).isEqualTo("stress, social situations");
        assertThat(updated.getCalmingStrategies()).isEqualTo("deep breathing, walking");
        assertThat(updated.getPersonalReminder()).isEqualTo("You are stronger than this moment.");
        assertThat(updated.getPrimaryContactName()).isEqualTo("Mom");
        assertThat(updated.getPrimaryContactPhone()).isEqualTo("555-0000");
        assertThat(updated.getPrimaryContactRelation()).isEqualTo("Mother");
        assertThat(updated.isConsentToAlert()).isTrue();
        assertThat(updated.getPreferredLanguage()).isEqualTo("es");
    }

    @Test
    void completeOnboarding_nullPreferredLanguage_keepsExistingLanguage() throws Exception {
        testUser.setPreferredLanguage("fr");
        userRepository.save(testUser);

        OnboardingRequest req = buildOnboardingRequest();
        req.setPreferredLanguage(null);

        mockMvc.perform(post("/api/user/onboarding")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail("user_ctrl_test@example.com").orElseThrow();
        assertThat(updated.getPreferredLanguage()).isEqualTo("fr");
    }

    @Test
    void completeOnboarding_setsOnboardingCompleteToTrue() throws Exception {
        assertThat(testUser.isOnboardingComplete()).isFalse();

        mockMvc.perform(post("/api/user/onboarding")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOnboardingRequest())))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail("user_ctrl_test@example.com").orElseThrow();
        assertThat(updated.isOnboardingComplete()).isTrue();
    }

    private OnboardingRequest buildOnboardingRequest() {
        OnboardingRequest req = new OnboardingRequest();
        req.setTriggers("stress, social situations");
        req.setCalmingStrategies("deep breathing, walking");
        req.setWarningSignsPersonal("irritability, isolation");
        req.setPersonalReminder("You are stronger than this moment.");
        req.setPrimaryContactName("Mom");
        req.setPrimaryContactPhone("555-0000");
        req.setPrimaryContactRelation("Mother");
        req.setConsentToAlert(true);
        req.setPreferredLanguage("es");
        return req;
    }
}
