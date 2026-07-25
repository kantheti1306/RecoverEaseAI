package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.TestSecurityUtils;
import com.recoverease.dto.CaregiverRequest;
import com.recoverease.dto.CaregiverResponseDto;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import com.recoverease.service.CaregiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CaregiverControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CaregiverService caregiverService;
    @Autowired private UserRepository userRepository;

    private User caregiverUser;

    @BeforeEach
    void setUp() {
        caregiverUser = userRepository.findByEmail("caregiver_ctrl_test@example.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Carol");
                    u.setEmail("caregiver_ctrl_test@example.com");
                    u.setPasswordHash("$2a$10$dummy");
                    u.setRole("CAREGIVER");
                    return userRepository.save(u);
                });
    }

    @Test
    void getGuidance_withoutAuth_returns401or403() throws Exception {
        CaregiverRequest req = new CaregiverRequest();
        req.setScenario("anxious");

        int status = mockMvc.perform(post("/api/caregiver/guidance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    void getGuidance_validRequest_returns200WithDto() throws Exception {
        CaregiverResponseDto dto = new CaregiverResponseDto();
        dto.setScenarioLabel("Person appears anxious or panicked");
        dto.setWhatToSay("I'm here with you.");
        dto.setAvoidSaying(List.of("Why can't you calm down?"));
        dto.setNextSteps(List.of("Sit with them", "Call a professional"));

        when(caregiverService.getGuidance(any(CaregiverRequest.class), any(User.class))).thenReturn(dto);

        CaregiverRequest req = new CaregiverRequest();
        req.setScenario("anxious");

        mockMvc.perform(post("/api/caregiver/guidance")
                        .with(TestSecurityUtils.asUser(caregiverUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenarioLabel").value("Person appears anxious or panicked"))
                .andExpect(jsonPath("$.whatToSay").value("I'm here with you."))
                .andExpect(jsonPath("$.avoidSaying[0]").value("Why can't you calm down?"))
                .andExpect(jsonPath("$.nextSteps[0]").value("Sit with them"));
    }

    @Test
    void getGuidance_missingScenario_returns400() throws Exception {
        String body = "{\"context\": \"some context\"}";

        mockMvc.perform(post("/api/caregiver/guidance")
                        .with(TestSecurityUtils.asUser(caregiverUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGuidance_blankScenario_returns400() throws Exception {
        String body = "{\"scenario\": \"   \", \"context\": \"extra\"}";

        mockMvc.perform(post("/api/caregiver/guidance")
                        .with(TestSecurityUtils.asUser(caregiverUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGuidance_withContext_returns200() throws Exception {
        CaregiverResponseDto dto = new CaregiverResponseDto();
        dto.setScenarioLabel("Person is agitated or showing anger");
        dto.setWhatToSay("Stay calm.");
        dto.setAvoidSaying(List.of("Stop it"));
        dto.setNextSteps(List.of("Give space"));

        when(caregiverService.getGuidance(any(CaregiverRequest.class), any(User.class))).thenReturn(dto);

        CaregiverRequest req = new CaregiverRequest();
        req.setScenario("angry");
        req.setContext("He threw things around");

        mockMvc.perform(post("/api/caregiver/guidance")
                        .with(TestSecurityUtils.asUser(caregiverUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatToSay").value("Stay calm."));
    }
}
